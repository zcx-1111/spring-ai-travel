package com.ruoyi.system.ai;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.BizScenicSpot;
import com.ruoyi.system.mapper.BizScenicSpotMapper;

/**
 * 景点知识库向量检索服务
 *
 * 职责：把用户的自然语言需求（目的地+偏好/对话内容）交给 Qdrant 做语义相似度检索，
 * 返回TopN个景点实体作为大模型的RAG上下文（最多5条）。
 *
 * <p><b>检索策略（两阶段，严格同城，杜绝跨城误召回）：</b></p>
 * <ol>
 *   <li><b>先定位城市</b>：把用户输入（"桂林市"/"桂林"）归一化为城市核心名，
 *       再查知识库解析出真实存在的「标准城市名集合」作为唯一城市锚点；</li>
 *   <li><b>再检索该城市景点</b>：Qdrant 用 metadata 的 city 字段做过滤（in 标准城市集合），
 *       只在同城向量内做语义检索；SQL 侧同样严格按 city 召回作为补充/降级。
 *       两条链路结果都限定同城，<b>绝不会用外地景点补足</b>，
 *       从根本上避免搜"桂林"召回海南"小桂林"这类同名异地景点。</li>
 * </ol>
 *
 * 高可用设计：当Qdrant服务未启动、集合不存在或向量检索异常时，
 * 自动降级为MySQL同城关键词召回（按城市+景区等级排序），保证AI功能链路不中断。
 *
 * @author ruoyi
 * @date 2026-09-21
 */
@Service
public class ScenicVectorSearchService
{
    private static final Logger log = LoggerFactory.getLogger(ScenicVectorSearchService.class);

    /** RAG检索默认召回条数（语义通道上限） */
    public static final int DEFAULT_TOP_K = 5;

    /** 混合召回：关键词通道（ES全文检索）最多前置条数——用户备注明确指向的景点优先 */
    public static final int HYBRID_KEYWORD_LIMIT = 3;

    /** 混合召回总条数上限（关键词3条 + 语义5条去重后最多8条，3天行程编排足够且不撑大Prompt） */
    public static final int HYBRID_TOTAL_LIMIT = 8;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private BizScenicSpotMapper scenicSpotMapper;

    /** ES关键词全文检索（与向量检索平级的第二召回通道，ES异常时其内部自动降级MySQL） */
    @Autowired
    private EsScenicSpotService esScenicSpotService;

    /** Qdrant gRPC主机（与application.yml配置保持一致） */
    @Value("${spring.ai.vectorstore.qdrant.host:127.0.0.1}")
    private String qdrantHost;

    /** Qdrant gRPC端口（与application.yml配置保持一致） */
    @Value("${spring.ai.vectorstore.qdrant.port:6334}")
    private int qdrantPort;

    /** 降级告警限频：Qdrant不可用时30秒内最多打一条WARN，避免每个请求都刷日志 */
    private final AtomicLong lastFallbackWarnTime = new AtomicLong(0);
    private static final long FALLBACK_WARN_INTERVAL_MS = 30_000L;

    /**
     * 检索景点（两阶段：先定位标准城市，再严格只检索该城市景点）
     *
     * @param query       检索文本（用户偏好/对话内容，用于语义排序）
     * @param destination 目的地城市（如"桂林"/"桂林市"），城市过滤的唯一锚点
     * @param topK        召回条数上限
     * @return 全部属于目的地城市、按相关度排序的景点列表（不足TopK也绝不补外地景点）
     */
    public List<BizScenicSpot> searchTopSpots(String query, String destination, int topK)
    {
        int limit = topK <= 0 ? DEFAULT_TOP_K : Math.min(topK, DEFAULT_TOP_K);

        // ===== 阶段一：定位知识库中的标准城市 =====
        // 用户可能填"桂林市"，库里存的是"桂林"；先归一化再查出真实城市名集合，
        // 作为Qdrant向量过滤与SQL检索的统一锚点，避免靠景点名猜城市导致跨城
        List<String> stdCities = resolveStandardCities(destination);

        // 未识别出城市（目的地为空或库里确实没有该城市数据）时：
        // 退回纯语义全库检索（这种情况极少，通常是测试性输入）
        if (stdCities.isEmpty())
        {
            return searchGlobalWithoutCity(query, limit);
        }

        String cityKey = normalizeCity(destination);
        log.info("知识库检索城市锚点：用户输入「{}」→ 标准城市{}，只在该城市范围内召回", destination, stdCities);

        // ===== 阶段二：在该城市范围内检索景点 =====
        List<BizScenicSpot> result = new ArrayList<>();
        Set<Long> addedIds = new LinkedHashSet<>();

        // 2.1 Qdrant向量检索：用 city in(标准城市集合) 过滤，语义最相关的同城景点排前
        if (isQdrantReachable())
        {
            try
            {
                FilterExpressionBuilder feb = new FilterExpressionBuilder();
                // metadata的city字段必须命中标准城市集合之一，外地同名景点（如海南小桂林）从源头被排除
                Filter.Expression cityFilter = feb
                        .in("city", stdCities.toArray(new Object[0]))
                        .build();
                // 城市已硬过滤，向量侧按limit*2召回即可（至少10条）供语义排序
                int vectorK = Math.max(limit * 2, 10);
                SearchRequest request = SearchRequest.builder()
                        .query(StringUtils.isNotEmpty(query) ? query : destination)
                        .topK(vectorK)
                        .filterExpression(cityFilter)
                        .build();
                List<Document> docs = vectorStore.similaritySearch(request);

                List<Long> spotIds = new ArrayList<>();
                if (docs != null)
                {
                    for (Document doc : docs)
                    {
                        Object idObj = doc.getMetadata().get("spotId");
                        if (idObj != null)
                        {
                            try
                            {
                                spotIds.add(Long.valueOf(String.valueOf(idObj)));
                            }
                            catch (NumberFormatException ignored)
                            {
                                // 元数据异常的文档跳过
                            }
                        }
                    }
                }
                if (!spotIds.isEmpty())
                {
                    Map<Long, BizScenicSpot> spotMap = scenicSpotMapper
                            .selectBizScenicSpotByIds(spotIds.toArray(new Long[0])).stream()
                            .collect(Collectors.toMap(BizScenicSpot::getId, s -> s, (a, b) -> a));
                    for (Long id : spotIds)
                    {
                        BizScenicSpot spot = spotMap.get(id);
                        // 双保险：再次校验该景点确实属于标准城市，防止向量元数据脏数据混入
                        if (spot != null && addedIds.add(spot.getId()) && belongsToCities(spot, stdCities))
                        {
                            result.add(spot);
                        }
                    }
                }
                log.info("Qdrant城市过滤向量召回同城景点{}个，标准城市：{}", result.size(), stdCities);
            }
            catch (Exception e)
            {
                // Qdrant过滤检索异常（集合/嵌入接口问题）不影响后续SQL同城召回
                warnFallbackThrottled("Qdrant向量检索异常，改用MySQL同城检索。原因：" + e.getMessage());
            }
        }
        else
        {
            warnFallbackThrottled("Qdrant服务不可达(" + qdrantHost + ":" + qdrantPort
                    + ")，本次检索直接降级MySQL同城");
        }

        // 2.2 SQL严格同城补充：只按city字段召回（向量库未覆盖到的同城景点由此补齐，5A/4A优先）
        // 当前向量库若尚未全量同步，同城景点主要靠这一路保障，且天然100%同城
        if (result.size() < limit)
        {
            List<BizScenicSpot> sqlSpots = scenicSpotMapper.selectSpotsByDestination(cityKey);
            if (sqlSpots != null)
            {
                for (BizScenicSpot spot : sqlSpots)
                {
                    if (result.size() >= limit)
                    {
                        break;
                    }
                    if (addedIds.add(spot.getId()))
                    {
                        result.add(spot);
                    }
                }
            }
        }

        log.info("知识库检索完成：城市{}，返回同城景点{}个（严格同城，未混入外地景点）", stdCities, result.size());
        return result.size() > limit ? new ArrayList<>(result.subList(0, limit)) : result;
    }

    /**
     * 混合召回：ES关键词全文检索（精确指向） + Qdrant向量语义检索（泛化偏好）。
     *
     * <p><b>解决的问题</b>：用户在"额外备注"里用描述性语言指定具体景点时
     * （如"室内供奉千里眼和顺风耳神像的地方"实指南沙天后宫），纯语义向量容易漏召回；
     * 而ES对名称/简介/标签里的实体词有确定性命中能力。两条通道互补：</p>
     * <ul>
     *   <li><b>关键词通道置前</b>：ES按用户备注原文做城市内全文检索（ES异常其内部自动降级MySQL拆词like），
     *       命中的景点最多3条且排在最前，保证用户明确提到的景点一定进入大模型白名单；</li>
     *   <li><b>语义通道补充</b>：Qdrant按目的地+偏好+备注整体语义召回Top5（异常自动SQL降级），
     *       去重后补齐到8条，用于行程其余天数的常规编排。</li>
     * </ul>
     * 两条通道都严格同城，关键词通道额外做一次城市归属校验，杜绝跨城混入。
     *
     * @param semanticQuery 语义检索文本（目的地+偏好+人群+备注整句）
     * @param keyword       关键词检索文本（通常是用户额外备注原文），为空则退化为纯语义召回
     * @param destination   目的地城市锚点
     * @param topK          语义通道召回条数
     * @return 关键词命中置前、语义结果补充的混合景点列表，最多 {@link #HYBRID_TOTAL_LIMIT} 条
     */
    public List<BizScenicSpot> searchTopSpotsHybrid(String semanticQuery, String keyword,
                                                    String destination, int topK)
    {
        int semanticLimit = topK <= 0 ? DEFAULT_TOP_K : Math.min(topK, DEFAULT_TOP_K);

        // 1. 语义通道：Qdrant向量召回（内部已含城市锚定 + SQL同城补充/降级）
        List<BizScenicSpot> semanticSpots = searchTopSpots(semanticQuery, destination, semanticLimit);

        // 2. 关键词通道：ES全文检索（过短的备注不触发，避免单字噪音）
        List<BizScenicSpot> keywordSpots = new ArrayList<>();
        if (StringUtils.isNotEmpty(keyword) && keyword.trim().length() >= 2)
        {
            List<String> stdCities = resolveStandardCities(destination);
            List<BizScenicSpot> esHits = esScenicSpotService.searchTopSpots(
                    keyword.trim(), destination, HYBRID_KEYWORD_LIMIT);
            for (BizScenicSpot spot : esHits)
            {
                // 城市可识别时强制同城校验；城市无法识别（极少）时与语义兜底策略一致放行
                if (stdCities.isEmpty() || belongsToCities(spot, stdCities))
                {
                    keywordSpots.add(spot);
                }
            }
            log.info("混合召回：ES关键词通道命中同城景点{}个，关键词「{}」", keywordSpots.size(), keyword.trim());
        }

        // 3. 合并：关键词命中置前（用户明确诉求优先编排），语义结果按原顺序去重补充
        Set<Long> addedIds = new LinkedHashSet<>();
        List<BizScenicSpot> merged = new ArrayList<>();
        for (BizScenicSpot spot : keywordSpots)
        {
            if (addedIds.add(spot.getId()))
            {
                merged.add(spot);
            }
        }
        for (BizScenicSpot spot : semanticSpots)
        {
            if (merged.size() >= HYBRID_TOTAL_LIMIT)
            {
                break;
            }
            if (addedIds.add(spot.getId()))
            {
                merged.add(spot);
            }
        }
        log.info("混合召回完成：关键词{}个 + 语义{}个，合并去重后{}个（上限{}）",
                keywordSpots.size(), semanticSpots.size(), merged.size(), HYBRID_TOTAL_LIMIT);
        return merged;
    }

    /**
     * 无明确城市时的兜底：纯语义全库检索（不做城市过滤），仅用于目的地为空等极少数场景
     */
    private List<BizScenicSpot> searchGlobalWithoutCity(String query, int limit)
    {
        int fetchK = Math.max(limit * 2, 10);
        if (isQdrantReachable())
        {
            try
            {
                SearchRequest request = SearchRequest.builder()
                        .query(StringUtils.isNotEmpty(query) ? query : "热门旅游景点")
                        .topK(fetchK)
                        .build();
                List<Document> docs = vectorStore.similaritySearch(request);
                List<Long> spotIds = new ArrayList<>();
                if (docs != null)
                {
                    for (Document doc : docs)
                    {
                        Object idObj = doc.getMetadata().get("spotId");
                        if (idObj != null)
                        {
                            try
                            {
                                spotIds.add(Long.valueOf(String.valueOf(idObj)));
                            }
                            catch (NumberFormatException ignored)
                            {
                            }
                        }
                    }
                }
                if (!spotIds.isEmpty())
                {
                    Map<Long, BizScenicSpot> spotMap = scenicSpotMapper
                            .selectBizScenicSpotByIds(spotIds.toArray(new Long[0])).stream()
                            .collect(Collectors.toMap(BizScenicSpot::getId, s -> s, (a, b) -> a));
                    List<BizScenicSpot> ordered = new ArrayList<>();
                    for (Long id : spotIds)
                    {
                        BizScenicSpot spot = spotMap.get(id);
                        if (spot != null && !ordered.contains(spot))
                        {
                            ordered.add(spot);
                        }
                    }
                    if (!ordered.isEmpty())
                    {
                        return ordered.size() > limit ? new ArrayList<>(ordered.subList(0, limit)) : ordered;
                    }
                }
            }
            catch (Exception e)
            {
                warnFallbackThrottled("无城市向量检索异常，降级全库SQL。原因：" + e.getMessage());
            }
        }
        // 最终兜底：全库高等级景点
        return fallbackBySql(null, limit);
    }

    /**
     * 城市归一化 + 知识库标准城市解析
     *
     * @param destination 用户输入的目的地
     * @return 知识库中真实存在的标准城市名集合（去重）；无法识别时返回空列表
     */
    private List<String> resolveStandardCities(String destination)
    {
        if (StringUtils.isEmpty(destination))
        {
            return new ArrayList<>();
        }
        String key = normalizeCity(destination);
        if (StringUtils.isEmpty(key))
        {
            return new ArrayList<>();
        }
        List<String> cities = scenicSpotMapper.selectCitiesByCityKey(key);
        if (cities == null)
        {
            return new ArrayList<>();
        }
        // 去重、去空
        Set<String> set = new LinkedHashSet<>();
        for (String c : cities)
        {
            if (StringUtils.isNotEmpty(c))
            {
                set.add(c.trim());
            }
        }
        return new ArrayList<>(set);
    }

    /**
     * 归一化城市名：去除行政后缀，得到用于匹配的城市核心名。
     * 注意不裁单字"州/盟"（会误伤广州/杭州等），这类简称/全称差异交给双向包含匹配处理。
     * 例："桂林市"→"桂林"，"阿坝藏族羌族自治州"→"阿坝藏族羌族"，"桂林"→"桂林"
     */
    private String normalizeCity(String raw)
    {
        if (raw == null)
        {
            return "";
        }
        String s = raw.trim();
        // 省级行政区后缀（个别用户会误填到城市）
        String[] suffixes = new String[]{
                "维吾尔自治区", "壮族自治区", "回族自治区", "自治区",
                "特别行政区", "自治州", "地区", "市"
        };
        boolean changed = true;
        while (changed)
        {
            changed = false;
            for (String suf : suffixes)
            {
                if (s.endsWith(suf) && s.length() > suf.length())
                {
                    s = s.substring(0, s.length() - suf.length());
                    changed = true;
                    break;
                }
            }
        }
        return s.trim();
    }

    /**
     * 判断景点是否属于标准城市集合之一（严格只比对城市字段，不看景点名/地址）
     */
    private boolean belongsToCities(BizScenicSpot spot, List<String> stdCities)
    {
        if (spot == null || StringUtils.isEmpty(spot.getCity()))
        {
            return false;
        }
        String spotCore = normalizeCity(spot.getCity());
        for (String std : stdCities)
        {
            if (cityNameEquals(spotCore, normalizeCity(std)))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 城市核心名是否等价：相等，或双向包含（较短串至少2字，避免单字误伤）。
     * 用于兼容"桂林"↔"桂林市"、"阿坝"↔"阿坝藏族羌族"、"楚雄"↔"楚雄州"等写法差异。
     */
    private boolean cityNameEquals(String a, String b)
    {
        if (StringUtils.isEmpty(a) || StringUtils.isEmpty(b))
        {
            return false;
        }
        if (a.equals(b))
        {
            return true;
        }
        String shorter = a.length() <= b.length() ? a : b;
        String longer = a.length() <= b.length() ? b : a;
        return shorter.length() >= 2 && longer.contains(shorter);
    }

    /**
     * TCP快速探测Qdrant gRPC端口是否可达（连接超时0.5秒）
     */
    private boolean isQdrantReachable()
    {
        try (Socket socket = new Socket())
        {
            socket.connect(new InetSocketAddress(qdrantHost, qdrantPort), 500);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * 降级告警限频：30秒内最多输出一条WARN，避免高并发时每个请求都刷同一条降级日志
     */
    private void warnFallbackThrottled(String message)
    {
        long now = System.currentTimeMillis();
        long last = lastFallbackWarnTime.get();
        if (now - last > FALLBACK_WARN_INTERVAL_MS
                && lastFallbackWarnTime.compareAndSet(last, now))
        {
            log.warn(message);
        }
    }

    /**
     * SQL降级检索：严格按城市召回（5A/4A优先）；
     * destination为空（无城市场景）时取全库高分景点兜底
     */
    private List<BizScenicSpot> fallbackBySql(String destination, int limit)
    {
        List<BizScenicSpot> spots;
        if (StringUtils.isNotEmpty(destination))
        {
            spots = scenicSpotMapper.selectSpotsByDestination(destination);
        }
        else
        {
            spots = scenicSpotMapper.selectBizScenicSpotList(new BizScenicSpot());
        }
        if (spots == null || spots.isEmpty())
        {
            // 目的地无匹配时兜底取全库景点，避免上下文为空导致模型编造
            spots = scenicSpotMapper.selectBizScenicSpotList(new BizScenicSpot());
        }
        // 按景区等级（5A>4A>3A）与ID排序后截取TopN
        return spots.stream()
                .sorted(Comparator.comparing(
                        (BizScenicSpot s) -> s.getSpotLevel() == null ? "" : s.getSpotLevel(),
                        Comparator.reverseOrder()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 将景点实体列表格式化为大模型可读的知识库文本（RAG上下文）
     * 性能优化：只保留行程编排必需的核心字段（名称/等级/简介/时长/票价），
     * 简介截断到80字，避免prompt过长拖慢模型推理与输出
     */
    public String buildKnowledgeText(List<BizScenicSpot> spots)
    {
        if (spots == null || spots.isEmpty())
        {
            return "（暂未召回到相关景点知识）";
        }
        StringBuilder sb = new StringBuilder();
        int idx = 1;
        for (BizScenicSpot spot : spots)
        {
            sb.append(idx++).append(". ").append(spot.getSpotName());
            if (StringUtils.isNotEmpty(spot.getSpotLevel()))
            {
                sb.append("（").append(spot.getSpotLevel()).append("）");
            }
            // 简介截断到80字，保留特色描述即可
            if (StringUtils.isNotEmpty(spot.getSpotIntro()))
            {
                String intro = spot.getSpotIntro().length() > 80
                        ? spot.getSpotIntro().substring(0, 80) + "…" : spot.getSpotIntro();
                sb.append("：").append(intro);
            }
            sb.append("。约")
              .append(spot.getPlayHours() == null ? "2" : spot.getPlayHours().stripTrailingZeros().toPlainString())
              .append("小时");
            sb.append("，成人票价")
              .append(spot.getAdultPrice() == null ? "免票" : spot.getAdultPrice().stripTrailingZeros().toPlainString() + "元");
            sb.append("。\n");
        }
        return sb.toString();
    }
}
