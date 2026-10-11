package com.ruoyi.system.ai;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.BizScenicSpot;
import com.ruoyi.system.mapper.BizScenicSpotMapper;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.VectorParams;
import io.qdrant.client.grpc.Common.PointId;
import io.qdrant.client.grpc.Points.ScrollPoints;

/**
 * 景点知识库启动同步监听器
 *
 * 职责：应用启动完成后，自动读取景点知识库表（biz_scenic_spot，del_flag=0启用的景点），
 * 把景点信息封装为 Spring AI Document，批量写入 Qdrant 向量库。
 * 每次后端重启都会执行一次同步：Document使用"scenic-景点ID"作为确定性ID，
 * Qdrant内部会映射为固定UUID执行upsert，因此重复同步不会产生重复数据。
 *
 * 容错：整个同步在守护线程中异步执行，Qdrant未启动或百炼嵌入服务不可用时，
 * 仅打印警告日志，不影响后端正常启动；在线接口会自动降级为MySQL关键词检索。
 *
 * @author ruoyi
 * @date 2026-09-21
 */
@Component
public class ScenicKnowledgeLoader
{
    private static final Logger log = LoggerFactory.getLogger(ScenicKnowledgeLoader.class);

    /** Qdrant集合名称（与application.yml中collection-name保持一致） */
    public static final String COLLECTION_NAME = "scenic_knowledge";
    /**
     * 嵌入向量维度：qwen3.7-text-embedding-flash 默认输出1024维，
     * 创建Qdrant集合时必须与嵌入模型实际维度一致
     */
    public static final int EMBEDDING_DIMENSION = 1024;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private BizScenicSpotMapper scenicSpotMapper;

    /** Qdrant gRPC主机（与application.yml配置保持一致） */
    @Value("${spring.ai.vectorstore.qdrant.host:127.0.0.1}")
    private String qdrantHost;

    /** Qdrant gRPC端口（与application.yml配置保持一致） */
    @Value("${spring.ai.vectorstore.qdrant.port:6334}")
    private int qdrantPort;

    /**
     * 应用完全启动后触发知识库同步（异步执行，不阻塞、不拖垮启动流程）
     */
    @EventListener(ApplicationReadyEvent.class)
    public void syncOnApplicationReady()
    {
        Thread worker = new Thread(() -> doSyncKnowledge(true), "scenic-knowledge-sync");
        worker.setDaemon(true);
        worker.start();
    }

    /**
     * 手动触发知识库全量同步（供管理接口调用，导入新CSV后无需重启后端）
     *
     * @return 同步结果：{ success, total, failedBatches, message }
     */
    public java.util.Map<String, Object> syncNow()
    {
        return doSyncKnowledge(false);
    }

    /**
     * 执行知识库同步：查MySQL启用景点 -> 确保集合存在 -> 分批向量化写入Qdrant
     *
     * @param silent 是否静默模式（启动自动同步=true：异常仅warn不抛出）
     */
    private java.util.Map<String, Object> doSyncKnowledge(boolean silent)
    {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        try
        {
            // 0. 快速TCP探测Qdrant端口（1秒超时），服务未启动时直接跳过同步，
            //    避免gRPC默认连接重试等待过久（降级为SQL检索，功能不受影响）
            if (!isQdrantReachable())
            {
                String msg = String.format("未检测到Qdrant服务(%s:%d)，请先启动Qdrant容器(docker start travel-qdrant)",
                        qdrantHost, qdrantPort);
                log.warn("[知识库同步] {}", msg);
                result.put("success", false);
                result.put("message", msg);
                return result;
            }

            // 1. 查询所有启用状态（del_flag=0）的景点，对应需求中的 enable=1 启用景点
            BizScenicSpot query = new BizScenicSpot();
            query.setDelFlag(0);
            List<BizScenicSpot> spots = scenicSpotMapper.selectBizScenicSpotList(query);
            if (spots == null || spots.isEmpty())
            {
                log.warn("[知识库同步] 景点知识库表暂无启用景点，跳过Qdrant同步");
                result.put("success", false);
                result.put("total", 0);
                result.put("message", "景点知识库表暂无启用景点");
                return result;
            }

            // 2. 确保Qdrant集合存在（不存在则按1024维余弦距离创建，已存在则忽略异常）
            ensureCollection();

            // 3. 增量检测：从Qdrant拉取已有点的contentHash，对比MySQL中景点文本的哈希，
            //    只对"新增"或"内容变更"的景点调用embedding，避免重复消耗百炼嵌入额度
            Map<Long, String> existingHashes = loadExistingHashes();
            List<Document> documents = new ArrayList<>();
            int skipCount = 0;
            for (BizScenicSpot spot : spots)
            {
                String text = buildDocumentText(spot);
                String hash = md5Hex(text);
                String oldHash = existingHashes.get(spot.getId());
                // 哈希相同说明内容未变，跳过embedding；不同或不存在则需要（重新）向量化
                if (hash.equals(oldHash))
                {
                    skipCount++;
                    continue;
                }
                documents.add(Document.builder()
                        .id(spotDocumentId(spot.getId()))
                        .text(text)
                        .metadata("spotId", spot.getId())
                        .metadata("spotName", nullToEmpty(spot.getSpotName()))
                        .metadata("city", nullToEmpty(spot.getCity()))
                        .metadata("province", nullToEmpty(spot.getProvince()))
                        .metadata("contentHash", hash)
                        .build());
            }

            // 4. 没有需要同步的景点（内容全部未变），直接返回，不消耗任何embedding额度
            if (documents.isEmpty())
            {
                log.info("[知识库同步] 共{}条景点，内容均未变化，跳过去重同步（节省embedding额度）", spots.size());
                result.put("success", true);
                result.put("total", spots.size());
                result.put("successCount", 0);
                result.put("skippedCount", skipCount);
                result.put("failedBatches", 0);
                result.put("collection", COLLECTION_NAME);
                result.put("message", String.format("无需同步：共%d条景点内容均未变化", spots.size()));
                return result;
            }

            // 5. 分批写入：百炼embedding接口单次批量上限25条，这里每批20条保险，
            //    Spring AI自动调用嵌入模型生成向量后存入Qdrant；
            //    Document使用确定性UUID，重复执行是upsert语义，不会产生重复数据
            int batchSize = 20;
            int successCount = 0;
            int failedBatches = 0;
            for (int i = 0; i < documents.size(); i += batchSize)
            {
                int end = Math.min(i + batchSize, documents.size());
                try
                {
                    vectorStore.add(documents.subList(i, end));
                    successCount += (end - i);
                }
                catch (Exception batchEx)
                {
                    // 单批失败（如某条文本超长/接口限流）不阻断其余批次
                    failedBatches++;
                    log.warn("[知识库同步] 第{}~{}条同步失败：{}", i + 1, end, batchEx.getMessage());
                }
            }
            log.info("[知识库同步] 景点知识库同步完成，集合{}，总数{}条，新增/变更{}条，跳过{}条，失败批次{}个",
                    COLLECTION_NAME, spots.size(), successCount, skipCount, failedBatches);
            result.put("success", failedBatches == 0);
            result.put("total", spots.size());
            result.put("successCount", successCount);
            result.put("skippedCount", skipCount);
            result.put("failedBatches", failedBatches);
            result.put("collection", COLLECTION_NAME);
            result.put("message", String.format("同步完成：共%d条景点，新增/变更%d条，跳过%d条，失败批次%d个",
                    spots.size(), successCount, skipCount, failedBatches));
            return result;
        }
        catch (Exception e)
        {
            // Qdrant未启动 / 百炼嵌入接口失败 / Key无效等场景：仅一行告警，不打印完整堆栈，
            // 系统可正常运行（在线检索自动降级SQL）；具体错误原因见异常信息
            log.warn("[知识库同步] Qdrant知识库同步失败，向量检索将降级为SQL检索。原因：{}", e.getMessage());
            result.put("success", false);
            result.put("message", "同步失败：" + e.getMessage());
            return result;
        }
    }

    /**
     * TCP快速探测Qdrant gRPC端口是否可达（连接超时1秒）
     */
    private boolean isQdrantReachable()
    {
        try (Socket socket = new Socket())
        {
            socket.connect(new InetSocketAddress(qdrantHost, qdrantPort), 1000);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * 确保Qdrant集合存在：通过原生客户端创建集合，集合已存在时Qdrant返回错误，忽略即可
     */
    private void ensureCollection()
    {
        Optional<QdrantClient> nativeClient = vectorStore.getNativeClient();
        if (nativeClient.isEmpty())
        {
            // 非Qdrant实现时不处理
            return;
        }
        try
        {
            VectorParams params = VectorParams.newBuilder()
                    .setSize(EMBEDDING_DIMENSION)
                    .setDistance(Distance.Cosine)
                    .build();
            nativeClient.get().createCollectionAsync(COLLECTION_NAME, params)
                    .get(15, TimeUnit.SECONDS);
            log.info("[知识库同步] Qdrant集合{}创建成功（{}维余弦距离）", COLLECTION_NAME, EMBEDDING_DIMENSION);
        }
        catch (Exception e)
        {
            // 集合已存在、连接超时等情况都忽略：add时若集合不存在会再次报错并由外层降级
            log.debug("[知识库同步] 集合创建检查（可能已存在）：{}", e.getMessage());
        }
    }

    /**
     * 构造单个景点的知识文本（内容越完整，语义检索效果越好）
     */
    private String buildDocumentText(BizScenicSpot spot)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("景点名称：").append(spot.getSpotName());
        if (StringUtils.isNotEmpty(spot.getProvince()))
        {
            sb.append("，所属省份：").append(spot.getProvince());
        }
        if (StringUtils.isNotEmpty(spot.getCity()))
        {
            sb.append("，所属城市：").append(spot.getCity());
        }
        if (StringUtils.isNotEmpty(spot.getAddress()))
        {
            sb.append("，详细地址：").append(spot.getAddress());
        }
        if (StringUtils.isNotEmpty(spot.getSpotLevel()))
        {
            sb.append("，景区等级：").append(spot.getSpotLevel());
        }
        if (StringUtils.isNotEmpty(spot.getSpotIntro()))
        {
            sb.append("。景点简介：").append(spot.getSpotIntro());
        }
        if (StringUtils.isNotEmpty(spot.getOpenTimeDesc()))
        {
            sb.append("。开放时间：").append(spot.getOpenTimeDesc());
        }
        sb.append("。建议游玩时长：")
          .append(spot.getPlayHours() == null ? "2小时" : spot.getPlayHours().stripTrailingZeros().toPlainString() + "小时");
        sb.append("，成人票价：")
          .append(spot.getAdultPrice() == null ? "免门票" : spot.getAdultPrice().stripTrailingZeros().toPlainString() + "元");
        if (StringUtils.isNotEmpty(spot.getBestSeason()))
        {
            sb.append("，最佳游玩季节：").append(spot.getBestSeason());
        }
        if (StringUtils.isNotEmpty(spot.getPlayNotice()))
        {
            sb.append("。游玩注意：").append(spot.getPlayNotice());
        }
        return sb.toString();
    }

    /**
     * 从Qdrant拉取集合中所有已有点的 spotId -> contentHash 映射。
     * 只读取payload（with_vectors=false），不下载向量，速度快、开销小。
     * 若Qdrant异常或集合为空，返回空Map（此时所有景点都会被判定为需要同步）。
     */
    @SuppressWarnings("unchecked")
    private Map<Long, String> loadExistingHashes()
    {
        Map<Long, String> hashes = new HashMap<>();
        Optional<QdrantClient> nativeClient = vectorStore.getNativeClient();
        if (nativeClient.isEmpty())
        {
            return hashes;
        }
        try
        {
            PointId offset = null;
            do
            {
                ScrollPoints.Builder builder = ScrollPoints.newBuilder()
                        .setCollectionName(COLLECTION_NAME)
                        .setLimit(500)
                        .setWithPayload(io.qdrant.client.grpc.Points.WithPayloadSelector.newBuilder().setEnable(true).build())
                        .setWithVectors(io.qdrant.client.grpc.Points.WithVectorsSelector.newBuilder().setEnable(false).build());
                if (offset != null)
                {
                    builder.setOffset(offset);
                }
                var response = nativeClient.get().scrollAsync(builder.build()).get(15, TimeUnit.SECONDS);
                for (var point : response.getResultList())
                {
                    // 从payload中提取spotId和contentHash
                    var payload = point.getPayloadMap();
                    var spotIdVal = payload.get("spotId");
                    var hashVal = payload.get("contentHash");
                    if (spotIdVal != null && spotIdVal.hasIntegerValue() && hashVal != null && hashVal.hasStringValue())
                    {
                        hashes.put(spotIdVal.getIntegerValue(), hashVal.getStringValue());
                    }
                }
                offset = response.hasNextPageOffset() ? response.getNextPageOffset() : null;
            }
            while (offset != null);
        }
        catch (Exception e)
        {
            // 集合不存在或Qdrant异常时返回空Map，后续会全量同步
            log.debug("[知识库同步] 读取已有哈希失败（集合可能为空）：{}", e.getMessage());
        }
        return hashes;
    }

    /**
     * 计算字符串的MD5哈希（32位十六进制），用于对比景点内容是否变化
     */
    private String md5Hex(String text)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(32);
            for (byte b : digest)
            {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (Exception e)
        {
            // MD5是JDK内置算法，不可能失败；兜底返回空串（相当于每次都同步）
            return "";
        }
    }

    /**
     * 由景点ID生成确定性UUID（同一景点ID永远映射同一UUID，保证Qdrant重复同步幂等）
     */
    public static String spotDocumentId(Long spotId)
    {
        return UUID.nameUUIDFromBytes(("scenic-" + spotId).getBytes(StandardCharsets.UTF_8)).toString();
    }

    private String nullToEmpty(String s)
    {
        return s == null ? "" : s;
    }
}
