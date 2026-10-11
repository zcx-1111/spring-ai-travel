package com.ruoyi.system.service.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.ai.ScenicKnowledgeLoader;
import com.ruoyi.system.domain.BizScenicSpot;
import com.ruoyi.system.mapper.BizScenicSpotMapper;

/**
 * 景点CSV批量导入服务
 *
 * <p>解析去哪儿网格式的景点CSV文件（字段：名字,链接,地址,介绍,开放时间,图片链接,评分,建议游玩时间,建议季节,门票,小贴士,Page），
 * 自动清洗数据后批量写入 biz_scenic_spot 表，并触发Qdrant向量知识库同步。</p>
 *
 * <p>清洗规则：</p>
 * <ul>
 *   <li>景点名称：去掉尾部英文译名（如"永定土楼Yongding Earth Building"→"永定土楼"）</li>
 *   <li>地址：剥离"地址:"前缀、电话、官网等附加信息</li>
 *   <li>景区等级：从介绍文本中正则提取5A/4A/3A（评分不等于等级，不冒用）</li>
 *   <li>游玩时长："1-2小时"取上限，"3天"按6h/天，无值默认2h</li>
 *   <li>门票价格：从dict或文本中提取首个¥价格，无价格记0</li>
 *   <li>介绍为空时用名称兜底，保证向量检索有文本</li>
 * </ul>
 *
 * @author ruoyi
 * @date 2026-09-22
 */
@Service
public class ScenicCsvImportService
{
    private static final Logger log = LoggerFactory.getLogger(ScenicCsvImportService.class);

    @Autowired
    private BizScenicSpotMapper scenicSpotMapper;

    @Autowired
    private ScenicKnowledgeLoader scenicKnowledgeLoader;

    /** CSV表头列名（去哪儿网标准格式） */
    private static final String HEADER_NAME = "名字";
    private static final String HEADER_ADDRESS = "地址";
    private static final String HEADER_INTRO = "介绍";
    private static final String HEADER_OPEN_TIME = "开放时间";
    private static final String HEADER_IMAGE = "图片链接";
    private static final String HEADER_HOURS = "建议游玩时间";
    private static final String HEADER_SEASON = "建议季节";
    private static final String HEADER_TICKET = "门票";
    private static final String HEADER_TIPS = "小贴士";

    /** 单批插入上限（MySQL单条INSERT的VALUES子句不宜过多，50条安全） */
    private static final int BATCH_INSERT_SIZE = 50;

    /**
     * 导入CSV文件到景点知识库
     *
     * @param file     CSV文件（MultipartFile）
     * @param province 省份（如"福建省"），由前端指定
     * @param city     城市（如"龙岩市"），由前端指定
     * @return 导入结果信息
     */
    public String importCsv(MultipartFile file, String province, String city)
    {
        if (file == null || file.isEmpty())
        {
            throw new ServiceException("上传文件不能为空");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv"))
        {
            throw new ServiceException("仅支持CSV格式文件");
        }
        if (province == null || province.trim().isEmpty()
                || city == null || city.trim().isEmpty())
        {
            throw new ServiceException("省份和城市不能为空");
        }

        // 1. 解析CSV（自动识别编码UTF-8/GBK）
        List<String[]> rows = parseCsv(file);
        if (rows.isEmpty())
        {
            throw new ServiceException("CSV文件内容为空");
        }

        // 2. 解析表头，建立列名到列索引的映射
        String[] header = rows.get(0);
        int idxName = -1, idxAddr = -1, idxIntro = -1, idxOpen = -1;
        int idxImg = -1, idxHours = -1, idxSeason = -1, idxTicket = -1, idxTips = -1;
        for (int i = 0; i < header.length; i++)
        {
            String col = header[i].trim();
            if (HEADER_NAME.equals(col)) idxName = i;
            else if (HEADER_ADDRESS.equals(col)) idxAddr = i;
            else if (HEADER_INTRO.equals(col)) idxIntro = i;
            else if (HEADER_OPEN_TIME.equals(col)) idxOpen = i;
            else if (HEADER_IMAGE.equals(col)) idxImg = i;
            else if (HEADER_HOURS.equals(col)) idxHours = i;
            else if (HEADER_SEASON.equals(col)) idxSeason = i;
            else if (HEADER_TICKET.equals(col)) idxTicket = i;
            else if (HEADER_TIPS.equals(col)) idxTips = i;
        }
        if (idxName < 0)
        {
            throw new ServiceException("CSV缺少必需的「名字」列，请检查文件格式");
        }

        // 3. 逐行解析清洗
        String operator = "csv-import";
        List<BizScenicSpot> toInsert = new ArrayList<>();
        Set<String> seenNames = new HashSet<>();  // CSV内部去重
        int skipEmpty = 0;
        for (int r = 1; r < rows.size(); r++)
        {
            String[] cols = rows.get(r);
            String name = cleanName(getCell(cols, idxName));
            if (name.isEmpty() || seenNames.contains(name))
            {
                skipEmpty++;
                continue;
            }
            seenNames.add(name);

            String intro = getCell(cols, idxIntro).trim();
            if (intro.isEmpty())
            {
                intro = name + "，位于" + city + "，是当地值得游览的景点。";
            }

            BizScenicSpot spot = new BizScenicSpot();
            spot.setSpotName(clip(name, 100));
            spot.setProvince(province.trim());
            spot.setCity(city.trim());
            spot.setAddress(clip(cleanAddress(getCell(cols, idxAddr)), 200));
            spot.setSpotLevel(parseLevel(getCell(cols, idxIntro)));
            spot.setPlayHours(parseHours(getCell(cols, idxHours)));
            spot.setAdultPrice(parsePrice(getCell(cols, idxTicket)));
            spot.setOpenTimeDesc(clip(getCell(cols, idxOpen).trim(), 200));
            spot.setBestSeason(clip(getCell(cols, idxSeason).trim(), 50));
            spot.setSpotIntro(intro);
            spot.setPlayNotice(clip(getCell(cols, idxTips).trim(), 500));
            spot.setCoverImg(clip(getCell(cols, idxImg).trim(), 255));
            spot.setCreateBy(operator);
            spot.setCreateTime(DateUtils.getNowDate());
            toInsert.add(spot);
        }

        if (toInsert.isEmpty())
        {
            throw new ServiceException("CSV解析后无有效数据（可能全部为空名或重名）");
        }

        // 4. 与数据库已有数据查重（排除CSV内部已去重后的名称）
        List<String> allNames = new ArrayList<>(seenNames);
        List<String> existing = scenicSpotMapper.selectExistingSpotNames(allNames);
        Set<String> existingSet = new HashSet<>(existing);
        List<BizScenicSpot> finalList = new ArrayList<>();
        int dupCount = 0;
        for (BizScenicSpot spot : toInsert)
        {
            if (!existingSet.contains(spot.getSpotName()))
            {
                finalList.add(spot);
            }
            else
            {
                dupCount++;
            }
        }

        if (finalList.isEmpty())
        {
            return String.format("CSV共%d条，全部与知识库已存在数据重名，跳过导入", toInsert.size());
        }

        // 5. 分批插入MySQL
        int inserted = 0;
        for (int i = 0; i < finalList.size(); i += BATCH_INSERT_SIZE)
        {
            int end = Math.min(i + BATCH_INSERT_SIZE, finalList.size());
            inserted += scenicSpotMapper.batchInsertBizScenicSpot(finalList.subList(i, end));
        }

        log.info("[CSV导入] 文件={}，解析{}条，去重跳过{}条，库内重名{}条，成功插入{}条",
                filename, toInsert.size(), skipEmpty, dupCount, inserted);

        // 6. 触发Qdrant向量知识库同步（不阻塞返回，但同步结果会在日志中体现）
        try
        {
            java.util.Map<String, Object> syncResult = scenicKnowledgeLoader.syncNow();
            log.info("[CSV导入] 知识库同步结果：{}", syncResult.get("message"));
        }
        catch (Exception e)
        {
            log.warn("[CSV导入] 知识库同步失败，可稍后手动调用同步接口。原因：{}", e.getMessage());
        }

        return String.format("导入完成：CSV共%d条，成功入库%d条，跳过空名/重名%d条，库内重名%d条；知识库已触发同步",
                toInsert.size(), inserted, skipEmpty, dupCount);
    }

    // ==================== CSV解析 ====================

    /**
     * 解析CSV文件，自动尝试UTF-8和GBK编码
     */
    private List<String[]> parseCsv(MultipartFile file)
    {
        byte[] raw;
        try
        {
            raw = file.getBytes();
        }
        catch (IOException e)
        {
            throw new ServiceException("读取文件失败：" + e.getMessage());
        }
        // 尝试UTF-8（含BOM）、GBK
        for (Charset cs : new Charset[]{StandardCharsets.UTF_8, Charset.forName("GBK")})
        {
            try
            {
                return parseCsvText(new String(raw, cs));
            }
            catch (Exception e)
            {
                // 继续尝试下一个编码
            }
        }
        throw new ServiceException("CSV文件编码无法识别（请使用UTF-8或GBK编码）");
    }

    /**
     * 简易CSV行解析（处理引号包裹的多行字段）
     */
    private List<String[]> parseCsvText(String content)
    {
        List<String[]> rows = new ArrayList<>();
        List<String> current = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < content.length(); i++)
        {
            char c = content.charAt(i);
            if (inQuotes)
            {
                if (c == '"')
                {
                    if (i + 1 < content.length() && content.charAt(i + 1) == '"')
                    {
                        field.append('"');  // 双引号转义
                        i++;
                    }
                    else
                    {
                        inQuotes = false;
                    }
                }
                else
                {
                    field.append(c);
                }
            }
            else
            {
                if (c == '"')
                {
                    inQuotes = true;
                }
                else if (c == ',')
                {
                    current.add(field.toString());
                    field.setLength(0);
                }
                else if (c == '\n' || c == '\r')
                {
                    if (c == '\r' && i + 1 < content.length() && content.charAt(i + 1) == '\n')
                    {
                        i++;  // 跳过\n
                    }
                    current.add(field.toString());
                    field.setLength(0);
                    if (!current.stream().allMatch(s -> s.trim().isEmpty()))
                    {
                        rows.add(current.toArray(new String[0]));
                    }
                    current.clear();
                }
                else
                {
                    field.append(c);
                }
            }
        }
        // 最后一行
        if (!field.isEmpty() || !current.isEmpty())
        {
            current.add(field.toString());
            if (!current.stream().allMatch(s -> s.trim().isEmpty()))
            {
                rows.add(current.toArray(new String[0]));
            }
        }
        return rows;
    }

    // ==================== 数据清洗方法 ====================

    /** 去掉景点名中的英文译名部分 */
    private String cleanName(String raw)
    {
        // 去掉连续英文字母+空格组成的译名尾部
        String cleaned = raw.replaceAll("[A-Za-z].*$", "").trim();
        return cleaned.isEmpty() ? raw.trim() : cleaned;
    }

    /** 从地址字段提取纯地址，剥离"地址:"前缀和电话/官网 */
    private String cleanAddress(String raw)
    {
        if (raw == null || raw.isEmpty())
        {
            return "";
        }
        // 匹配 "地址: xxx   电话: xxx   官网: xxx" 格式
        Matcher m = Pattern.compile("地址[:：]\\s*(.*?)(?:\\s{2,}|\\s+电话[:：]|\\s+官网[:：]|$)")
                .matcher(raw);
        if (m.find())
        {
            return m.group(1).trim();
        }
        return raw.trim();
    }

    /** 从介绍文本中提取景区等级（5A/4A/3A），评分不等于等级 */
    private String parseLevel(String intro)
    {
        if (intro == null || intro.isEmpty())
        {
            return "";
        }
        Matcher m = Pattern.compile("([2345])\\s*A\\s*级?\\s*景区").matcher(intro);
        return m.find() ? m.group(1) + "A" : "";
    }

    /** 解析游玩时长："1-2小时"取上限，"3天"按6h/天，无值默认2 */
    private java.math.BigDecimal parseHours(String raw)
    {
        if (raw == null || raw.isEmpty())
        {
            return new java.math.BigDecimal("2.0");
        }
        // 收集所有"X小时"匹配，取最后一个（即范围上限）
        Matcher h = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*小时").matcher(raw);
        java.util.List<String> hours = new java.util.ArrayList<>();
        while (h.find())
        {
            hours.add(h.group(1));
        }
        if (!hours.isEmpty())
        {
            return new java.math.BigDecimal(hours.get(hours.size() - 1));
        }
        Matcher d = Pattern.compile("(\\d+)\\s*天").matcher(raw);
        if (d.find())
        {
            return new java.math.BigDecimal(d.group(1)).multiply(new java.math.BigDecimal("6"));
        }
        return new java.math.BigDecimal("2.0");
    }

    /** 从门票字段提取首个¥/￥价格，无价格记0 */
    private java.math.BigDecimal parsePrice(String raw)
    {
        if (raw == null || raw.isEmpty())
        {
            return java.math.BigDecimal.ZERO;
        }
        Matcher m = Pattern.compile("[¥￥]\\s*(\\d+(?:\\.\\d+)?)").matcher(raw);
        return m.find() ? new java.math.BigDecimal(m.group(1)) : java.math.BigDecimal.ZERO;
    }

    /** 安全取列值 */
    private String getCell(String[] cols, int idx)
    {
        return (idx >= 0 && idx < cols.length) ? cols[idx] : "";
    }

    /** 字符串截断到指定长度 */
    private String clip(String s, int max)
    {
        if (s == null)
        {
            return "";
        }
        return s.length() > max ? s.substring(0, max) : s;
    }
}
