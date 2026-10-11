package com.ruoyi.system.ai;

import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.stereotype.Service;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.BizScenicSpot;
import com.ruoyi.system.domain.es.EsScenicSearchResult;
import com.ruoyi.system.domain.es.EsScenicSpot;
import com.ruoyi.system.mapper.BizScenicSpotMapper;
import com.ruoyi.system.repository.EsScenicSpotRepository;

/**
 * 景点 Elasticsearch 关键词全文检索服务
 *
 * <p>与 {@link ScenicVectorSearchService}（Qdrant 语义检索）平级，作为独立的关键词检索能力，
 * 对外提供同名方法 {@link #searchTopSpots(String, String, int)}，便于后续做"关键词+语义"混合召回。</p>
 *
 * <p><b>核心能力：</b></p>
 * <ul>
 *   <li>多字段全文检索：景点名称^5、城市^3、标签^2、地址^1、简介^1，
 *       名称 keyword 精确命中额外加权10倍；multi_match 带 fuzziness=AUTO 兼容错别字（模糊匹配）；</li>
 *   <li>城市锚定：先把用户输入归一化并解析为库中真实标准城市集合，再用 city.keyword 做 terms 硬过滤，
 *       从根本上杜绝搜"桂林"召回海南"小桂林"这类跨城误召回；</li>
 *   <li>分页 + 排序：默认相关度，可按景区等级权重（5A优先）/票价升降序排序；</li>
 *   <li>高可用：ES 关闭、不可达或查询异常时，自动降级为 MySQL 多字段 like 查询，异常绝不抛到前端；</li>
 *   <li>数据同步：提供全量批量同步与单条新增/修改/删除增量同步（均幂等）。</li>
 * </ul>
 *
 * @author ruoyi
 * @date 2026-10-05
 */
@Service
public class EsScenicSpotService
{
    private static final Logger log = LoggerFactory.getLogger(EsScenicSpotService.class);

    /** AI RAG检索默认召回条数（与向量检索服务保持一致） */
    public static final int DEFAULT_TOP_K = 5;

    /** 分页每页最大条数，防止一次拉取过多 */
    private static final int MAX_PAGE_SIZE = 100;

    /** 来源标识：ES / MySQL */
    private static final String SOURCE_ES = "elasticsearch";
    private static final String SOURCE_MYSQL = "mysql";

    /** 景区等级数字提取（5A→5、4A→4） */
    private static final Pattern LEVEL_DIGIT = Pattern.compile("(\\d+)");

    /**
     * MySQL降级拆词用的旅游口语停用词/短语（无景点指向性，拆分时作为分隔/过滤）。
     * 仅作用于ES不可用时的MySQL like降级链路；ES链路由IK分词器处理，不受此表影响。
     */
    private static final List<String> SPOKEN_STOPWORDS = Arrays.asList(
            // 人称/指代/语气
            "我们", "你们", "他们", "她们", "什么", "怎么", "为什么", "哪里", "哪个", "那种", "这种",
            "一个", "两个", "几个", "那个", "这个", "有个", "地方", "位置", "那里", "这里", "记得",
            // 意愿/评价类口语
            "我想去", "想去", "想要", "希望", "可以", "最好", "喜欢", "推荐", "适合", "偏好",
            "就是", "还是", "还有", "请问", "一下", "比较", "非常", "特别", "比较有", "有特色",
            // 泛化场景词（指向性太宽，单独命中噪音大）
            "景点", "景区", "室内", "供奉", "神像", "两尊", "三尊", "一尊", "的是", "是个",
            "人少景美", "不要太累", "不太累", "不累", "轻松", "顺路", "打卡", "拍照",
            // 行动/安排词
            "带孩子", "带老人", "老人", "小孩", "孩子", "行程", "安排", "游玩", "游览", "旅游", "旅行");

    /** 拆词片段长度范围：太短是单字噪音，太长是未被停用词切开的整句（like整句基本无意义） */
    private static final int KEYWORD_MIN_LEN = 2;
    private static final int KEYWORD_MAX_LEN = 6;

    /** 一次降级检索最多使用的拆分词数量，避免SQL过长 */
    private static final int MAX_KEYWORD_TERMS = 8;

    /** ES连接地址（spring.elasticsearch.uris，逗号分隔多个节点） */
    @Value("${spring.elasticsearch.uris:http://127.0.0.1:9200}")
    private List<String> esUris;

    /** ES检索总开关（travel.elasticsearch.enabled） */
    @Value("${travel.elasticsearch.enabled:true}")
    private boolean esEnabled;

    /** 全量同步每批条数 */
    @Value("${travel.elasticsearch.sync-batch-size:1000}")
    private int syncBatchSize;

    @Autowired
    private ElasticsearchOperations operations;

    @Autowired
    private EsScenicSpotRepository esScenicSpotRepository;

    @Autowired
    private BizScenicSpotMapper scenicSpotMapper;

    /** 降级告警限频：30秒内最多一条WARN，与向量检索服务保持同样策略 */
    private final AtomicLong lastFallbackWarnTime = new AtomicLong(0);
    private static final long FALLBACK_WARN_INTERVAL_MS = 30_000L;

    /* ==================== 对外检索方法 ==================== */

    /**
     * 关键词分页检索（游客端/管理端使用）
     *
     * <p><b>严格城市模式</b>：传入 city 但无法解析出标准城市时，直接返回空结果，
     * 绝不在城市查询中混入外地景点。</p>
     *
     * @param keyword  关键词（匹配名称/城市/标签/地址/简介），可空（空则按城市浏览）
     * @param city     城市名（如"桂林"/"桂林市"），可空（空则全国检索）
     * @param pageNum  页码，从1开始
     * @param pageSize 每页数
     * @param sort     排序：relevance=相关度(默认)、level=等级优先、priceAsc=票价升、priceDesc=票价降
     * @return 分页结果（ES异常时自动降级MySQL，source字段标识实际来源）
     */
    public EsScenicSearchResult searchSpots(String keyword, String city,
                                            int pageNum, int pageSize, String sort)
    {
        return doSearch(keyword, city, pageNum, pageSize, sort, true);
    }

    /**
     * RAG 场景检索（方法名与 {@link ScenicVectorSearchService#searchTopSpots} 保持一致，便于后续混合召回统一调用）
     *
     * <p>与严格模式不同：城市无法识别时退回全国关键词检索，保证 AI 上下文不为空。</p>
     *
     * @param query       检索关键词/偏好
     * @param destination 目的地城市
     * @param topK        召回条数
     * @return 景点实体列表
     */
    public List<BizScenicSpot> searchTopSpots(String query, String destination, int topK)
    {
        int limit = topK <= 0 ? DEFAULT_TOP_K : Math.min(topK, MAX_PAGE_SIZE);
        EsScenicSearchResult result = doSearch(query, destination, 1, limit, null, false);
        return result.getRows();
    }

    /**
     * 检索核心实现
     *
     * @param strictCity true=城市无法解析时返回空（面向用户）；false=退回全国检索（面向AI）
     */
    private EsScenicSearchResult doSearch(String keyword, String city, int pageNum, int pageSize,
                                          String sort, boolean strictCity)
    {
        int page = Math.max(pageNum, 1);
        int size = pageSize <= 0 ? 10 : Math.min(pageSize, MAX_PAGE_SIZE);
        String kw = keyword == null ? "" : keyword.trim();
        boolean hasCity = StringUtils.isNotEmpty(city);

        // ===== 阶段一：先定位城市锚点 =====
        List<String> stdCities = hasCity ? resolveStandardCities(city) : new ArrayList<>();
        if (hasCity && stdCities.isEmpty())
        {
            if (strictCity)
            {
                // 严格模式：城市查不到，直接空结果，杜绝跨城
                log.info("ES检索城市锚点：「{}」未匹配到知识库城市，严格模式返回空", city);
                return new EsScenicSearchResult(0, page, size, new ArrayList<>(),
                        isEsAvailable() ? SOURCE_ES : SOURCE_MYSQL);
            }
            // 非严格（AI兜底）：放弃城市过滤做全国检索
            log.info("ES检索城市锚点：「{}」未匹配到城市，退回全国关键词检索", city);
        }

        // ===== 阶段二：ES检索；不可用/异常则降级MySQL =====
        if (esEnabled && isEsAvailable())
        {
            try
            {
                return searchByEs(kw, stdCities, page, size, sort);
            }
            catch (Exception e)
            {
                warnFallbackThrottled("ES全文检索异常，降级MySQL模糊查询。原因：" + e.getMessage());
            }
        }
        else if (esEnabled)
        {
            warnFallbackThrottled("ES服务不可达(" + esUris + ")，本次检索降级MySQL");
        }
        String cityKey = hasCity ? normalizeCity(city) : null;
        return searchByMysql(kw, cityKey, page, size);
    }

    /* ==================== ES 检索 ==================== */

    /**
     * 构造并执行ES布尔检索：精确+模糊匹配 + 城市terms硬过滤 + 分页排序
     */
    private EsScenicSearchResult searchByEs(String kw, List<String> stdCities,
                                            int page, int size, String sort)
    {
        BoolQuery.Builder bool = new BoolQuery.Builder();

        // 关键词：精确命中（keyword，10倍权重）+ 多字段分词匹配（fuzziness=AUTO 容错），至少命中其一
        if (StringUtils.isNotEmpty(kw))
        {
            final String keyword = kw;
            bool.must(Query.of(q -> q.bool(bb -> bb
                    .should(s -> s.term(t -> t.field("spotName.keyword")
                            .value(FieldValue.of(fb -> fb.stringValue(keyword)))
                            .boost(10.0f)))
                    .should(s -> s.multiMatch(mm -> mm
                            .query(keyword)
                            .type(TextQueryType.BestFields)
                            .fuzziness("AUTO")
                            .fields("spotName^5", "city^3", "tags^2", "address^1", "spotIntro^1")))
                    .minimumShouldMatch("1"))));
        }

        // 城市硬过滤：city.keyword 必须命中标准城市集合之一（核心：防止跨城误召回）
        if (stdCities != null && !stdCities.isEmpty())
        {
            List<FieldValue> cityValues = stdCities.stream()
                    .map(c -> FieldValue.of(f -> f.stringValue(c)))
                    .collect(Collectors.toList());
            bool.filter(f -> f.terms(t -> t.field("city.keyword")
                    .terms(tv -> tv.value(cityValues))));
        }

        // Spring Data ES 6.x：NativeQuery.builder() 返回独立的 NativeQueryBuilder（非内部类）
        var builder = NativeQuery.builder()
                .withQuery(q -> q.bool(bool.build()))
                .withPageable(PageRequest.of(page - 1, size));

        // 排序：显式指定优先；无关键词的城市浏览默认按等级权重；有关词默认ES相关度评分
        if ("level".equalsIgnoreCase(sort))
        {
            builder.withSort(s -> s.field(f -> f.field("levelRank").order(SortOrder.Desc)));
        }
        else if ("priceAsc".equalsIgnoreCase(sort))
        {
            builder.withSort(s -> s.field(f -> f.field("adultPrice").order(SortOrder.Asc)));
        }
        else if ("priceDesc".equalsIgnoreCase(sort))
        {
            builder.withSort(s -> s.field(f -> f.field("adultPrice").order(SortOrder.Desc)));
        }
        else if (StringUtils.isEmpty(kw))
        {
            builder.withSort(s -> s.field(f -> f.field("levelRank").order(SortOrder.Desc)));
        }

        SearchHits<EsScenicSpot> hits = operations.search(builder.build(), EsScenicSpot.class);

        // 取ES命中ID（保持相关度排序），回查MySQL得到字段完整的业务实体，避免ES与MySQL字段不一致
        List<Long> hitIds = new ArrayList<>();
        for (SearchHit<EsScenicSpot> hit : hits.getSearchHits())
        {
            try
            {
                hitIds.add(Long.valueOf(hit.getId()));
            }
            catch (NumberFormatException ignored)
            {
                // 异常ID跳过
            }
        }

        List<BizScenicSpot> rows = new ArrayList<>();
        if (!hitIds.isEmpty())
        {
            List<BizScenicSpot> spots = scenicSpotMapper
                    .selectBizScenicSpotByIds(hitIds.toArray(new Long[0]));
            java.util.Map<Long, BizScenicSpot> spotMap = spots.stream()
                    .collect(Collectors.toMap(BizScenicSpot::getId, s -> s, (a, b) -> a));
            for (Long id : hitIds)
            {
                BizScenicSpot spot = spotMap.get(id);
                if (spot != null)
                {
                    rows.add(spot);
                }
            }
        }
        return new EsScenicSearchResult(hits.getTotalHits(), page, size, rows, SOURCE_ES);
    }

    /* ==================== MySQL 降级检索 ==================== */

    /**
     * MySQL多字段like分页检索（ES不可用时的降级链路）。
     * 关键：把用户整句按标点+口语停用词拆成若干实词（如"千里眼/顺风耳"），
     * 词间OR匹配，避免整句 like '%千里眼 顺风耳%' 命不中的问题。
     */
    private EsScenicSearchResult searchByMysql(String kw, String cityKey, int page, int size)
    {
        List<String> keywords = splitKeywords(kw);
        String city = StringUtils.isNotEmpty(cityKey) ? cityKey : null;
        int offset = (page - 1) * size;
        List<BizScenicSpot> rows = scenicSpotMapper.searchSpotByKeywordLike(
                keywords.isEmpty() ? null : keywords, city, offset, size);
        long total = scenicSpotMapper.countSpotByKeywordLike(
                keywords.isEmpty() ? null : keywords, city);
        return new EsScenicSearchResult(total, page, size, rows, SOURCE_MYSQL);
    }

    /**
     * 把用户自然语言描述拆分为实词关键词（仅MySQL降级链路使用）。
     *
     * <p>处理流程：①先把旅游口语停用短语替换为分隔符（切开"我记得有个地方室内供奉的是"这类长句）；
     * ②再按非汉字/字母/数字字符切分；③保留2~6字片段并过滤停用短语；④去重保序，上限8个。</p>
     *
     * <p>示例："我记得有个地方室内供奉的是"千里眼"和"顺风耳"两尊神像，我想去那里"
     * → ["千里眼", "顺风耳"]</p>
     */
    private List<String> splitKeywords(String text)
    {
        if (StringUtils.isEmpty(text))
        {
            return new ArrayList<>();
        }
        String cleaned = text;
        // 1. 口语停用短语统一替换为空格（长词优先替换，避免短词截断长词）
        List<String> stopwords = new ArrayList<>(SPOKEN_STOPWORDS);
        stopwords.sort((a, b) -> b.length() - a.length());
        for (String sw : stopwords)
        {
            if (cleaned.contains(sw))
            {
                cleaned = cleaned.replace(sw, " ");
            }
        }
        // 2. 非汉字/字母/数字字符一律视为分隔符
        String[] fragments = cleaned.split("[^\\p{IsHan}A-Za-z0-9]+");
        // 3. 过滤长度与停用短语，去重保序
        Set<String> seen = new LinkedHashSet<>();
        for (String frag : fragments)
        {
            String f = frag.trim();
            if (f.length() < KEYWORD_MIN_LEN || f.length() > KEYWORD_MAX_LEN)
            {
                continue;
            }
            if (SPOKEN_STOPWORDS.contains(f))
            {
                continue;
            }
            seen.add(f);
            if (seen.size() >= MAX_KEYWORD_TERMS)
            {
                break;
            }
        }
        return new ArrayList<>(seen);
    }

    /* ==================== 数据同步 ==================== */

    /**
     * 全量同步：MySQL景点（含标签）按ID游标分批 bulk 写入ES，幂等可重复执行
     *
     * @return 同步文档总数
     * @throws IllegalStateException ES不可用或同步失败时抛出（由管理端Controller捕获提示，区别于检索降级）
     */
    public int syncAll()
    {
        if (!isEsAvailable())
        {
            throw new IllegalStateException("Elasticsearch服务不可达(" + esUris + ")，请先启动ES后再同步");
        }
        try
        {
            // 1.索引不存在则按实体注解创建（含IK分词映射）
            initIndex();

            // 2.游标分批读取并bulk写入
            int batchSize = syncBatchSize > 0 ? syncBatchSize : 1000;
            long lastId = 0L;
            int total = 0;
            while (true)
            {
                List<BizScenicSpot> batch = scenicSpotMapper.selectSpotPageForEsSync(lastId, batchSize);
                if (batch == null || batch.isEmpty())
                {
                    break;
                }
                List<IndexQuery> indexQueries = new ArrayList<>(batch.size());
                for (BizScenicSpot spot : batch)
                {
                    EsScenicSpot doc = convertToDoc(spot);
                    indexQueries.add(new IndexQueryBuilder()
                            .withId(doc.getId())
                            .withObject(doc)
                            .build());
                    lastId = spot.getId();
                }
                operations.bulkIndex(indexQueries, EsScenicSpot.class);
                total += batch.size();
                log.info("[ES全量同步] 已同步 {} 条（当前最大ID={}）", total, lastId);
                if (batch.size() < batchSize)
                {
                    break;
                }
            }
            log.info("[ES全量同步] 完成，共同步景点 {} 个到索引 scenic_spot", total);
            return total;
        }
        catch (Exception e)
        {
            log.error("[ES全量同步] 失败", e);
            throw new IllegalStateException("ES全量同步失败：" + e.getMessage(), e);
        }
    }

    /**
     * 增量同步单个景点（新增/修改后调用，内部捕获异常，绝不影响主业务写入）
     * 景点已被软删除时，自动从ES删除对应文档。
     *
     * @param id 景点ID
     */
    public void upsertBySpotId(Long id)
    {
        if (id == null || !esEnabled)
        {
            return;
        }
        try
        {
            if (!isEsAvailable())
            {
                return;
            }
            BizScenicSpot spot = scenicSpotMapper.selectSpotForEsById(id);
            if (spot == null || (spot.getDelFlag() != null && spot.getDelFlag() == 1))
            {
                esScenicSpotRepository.deleteById(String.valueOf(id));
                return;
            }
            esScenicSpotRepository.save(convertToDoc(spot));
        }
        catch (Exception e)
        {
            // 增量同步失败只记录日志，不阻断MySQL主流程；下次全量同步可修复数据
            log.warn("[ES增量同步] 景点[{}]同步ES失败：{}", id, e.getMessage());
        }
    }

    /**
     * 删除ES景点文档（景点物理删除时调用，内部捕获异常）
     *
     * @param id 景点ID
     */
    public void deleteBySpotId(Long id)
    {
        if (id == null || !esEnabled)
        {
            return;
        }
        try
        {
            if (isEsAvailable())
            {
                esScenicSpotRepository.deleteById(String.valueOf(id));
            }
        }
        catch (Exception e)
        {
            log.warn("[ES增量同步] 景点[{}]从ES删除失败：{}", id, e.getMessage());
        }
    }

    /**
     * 索引不存在时按实体注解创建索引与映射（IK分词字段）
     */
    private void initIndex()
    {
        IndexOperations indexOps = operations.indexOps(EsScenicSpot.class);
        if (!indexOps.exists())
        {
            // createWithMapping：创建索引并写入@Field/@MultiField定义的mapping（含ik_max_word/ik_smart分析器）
            indexOps.createWithMapping();
            log.info("[ES索引初始化] 已创建索引 scenic_spot 及IK分词映射");
        }
    }

    /* ==================== 工具方法 ==================== */

    /**
     * MySQL实体 → ES文档实体
     */
    private EsScenicSpot convertToDoc(BizScenicSpot spot)
    {
        EsScenicSpot doc = new EsScenicSpot();
        doc.setId(String.valueOf(spot.getId()));
        doc.setSpotName(spot.getSpotName());
        doc.setProvince(spot.getProvince());
        doc.setCity(spot.getCity());
        doc.setAddress(spot.getAddress());
        doc.setTags(parseTags(spot.getTags()));
        doc.setSpotIntro(spot.getSpotIntro());
        doc.setSpotLevel(spot.getSpotLevel());
        doc.setLevelRank(calcLevelRank(spot.getSpotLevel()));
        BigDecimal price = spot.getAdultPrice();
        doc.setAdultPrice(price == null ? null : price.doubleValue());
        return doc;
    }

    /**
     * 逗号分隔标签字符串 → 标签集合（去空去重）
     */
    private List<String> parseTags(String tags)
    {
        if (StringUtils.isEmpty(tags))
        {
            return new ArrayList<>();
        }
        Set<String> set = new LinkedHashSet<>();
        for (String t : Arrays.asList(tags.split(",")))
        {
            String trim = t.trim();
            if (!trim.isEmpty())
            {
                set.add(trim);
            }
        }
        return new ArrayList<>(set);
    }

    /**
     * 景区等级 → 数值权重：5A=50、4A=40、3A=30、2A=20、1A=10、无等级=0
     */
    private int calcLevelRank(String spotLevel)
    {
        if (StringUtils.isEmpty(spotLevel))
        {
            return 0;
        }
        Matcher m = LEVEL_DIGIT.matcher(spotLevel);
        return m.find() ? Integer.parseInt(m.group(1)) * 10 : 0;
    }

    /**
     * 城市归一化 + 解析库中真实标准城市（与ScenicVectorSearchService同口径）
     */
    private List<String> resolveStandardCities(String city)
    {
        String key = normalizeCity(city);
        if (StringUtils.isEmpty(key))
        {
            return new ArrayList<>();
        }
        List<String> cities = scenicSpotMapper.selectCitiesByCityKey(key);
        if (cities == null)
        {
            return new ArrayList<>();
        }
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
     * 归一化城市名：去除行政后缀（"桂林市"→"桂林"、"阿坝藏族羌族自治州"→"阿坝藏族羌族"）。
     * 刻意不裁单字"州/盟"，避免误伤广州/杭州（与向量检索服务保持一致）。
     */
    private String normalizeCity(String raw)
    {
        if (raw == null)
        {
            return "";
        }
        String s = raw.trim();
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
     * ES REST端口TCP快速探测（0.5秒超时），与Qdrant探测方式一致
     */
    public boolean isEsAvailable()
    {
        InetSocketAddress address = probeAddress();
        try (Socket socket = new Socket())
        {
            socket.connect(address, 500);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * 从spring.elasticsearch.uris解析首个节点的host/port
     */
    private InetSocketAddress probeAddress()
    {
        try
        {
            String first = esUris.get(0).trim();
            URI uri = URI.create(first);
            String host = StringUtils.isNotEmpty(uri.getHost()) ? uri.getHost() : "127.0.0.1";
            int port = uri.getPort() > 0 ? uri.getPort() : 9200;
            return new InetSocketAddress(host, port);
        }
        catch (Exception e)
        {
            return new InetSocketAddress("127.0.0.1", 9200);
        }
    }

    /**
     * 降级告警限频，避免高并发刷日志
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
}
