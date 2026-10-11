package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.BizScenicSpot;

/**
 * 景点主Mapper接口
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
public interface BizScenicSpotMapper 
{
    /**
     * 查询景点主
     * 
     * @param id 景点主主键
     * @return 景点主
     */
    public BizScenicSpot selectBizScenicSpotById(Long id);

    /**
     * 查询景点主列表
     * 
     * @param bizScenicSpot 景点主
     * @return 景点主集合
     */
    public List<BizScenicSpot> selectBizScenicSpotList(BizScenicSpot bizScenicSpot);

    /**
     * 新增景点主
     * 
     * @param bizScenicSpot 景点主
     * @return 结果
     */
    public int insertBizScenicSpot(BizScenicSpot bizScenicSpot);

    /**
     * 修改景点主
     * 
     * @param bizScenicSpot 景点主
     * @return 结果
     */
    public int updateBizScenicSpot(BizScenicSpot bizScenicSpot);

    /**
     * 删除景点主
     * 
     * @param id 景点主主键
     * @return 结果
     */
    public int deleteBizScenicSpotById(Long id);

    /**
     * 批量删除景点主
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBizScenicSpotByIds(Long[] ids);

    /**
     * 严格按「城市」检索景点（知识库同城召回入口）
     *
     * <p><b>注意：只匹配 city 字段，不匹配景点名称</b>。
     * 历史版本曾同时模糊匹配 spot_name，导致搜"桂林"时把海南"小桂林"等
     * 外地同名景点误召回，造成AI跨城编排，现已移除该条件。</p>
     *
     * @param cityKey 城市核心名（已去除"市/自治州/地区"等后缀），如 "桂林"、"张家界"
     * @return 该城市下的景点列表（按景区等级排序）
     */
    public List<BizScenicSpot> selectSpotsByDestination(@Param("destination") String cityKey);

    /**
     * 城市名归一化：根据用户输入的城市核心名，查询知识库中真实存在的标准城市名集合
     *
     * <p>用于把"桂林市/桂林"统一解析为库里存储的标准城市（如"桂林"），
     * 再以标准城市作为Qdrant向量过滤与SQL检索的统一锚点，
     * 实现「先确定城市，再检索该城市景点」。</p>
     *
     * @param cityKey 城市核心名，如 "桂林"、"阿坝"
     * @return 命中的标准城市名去重列表（可能有多个，如"兴安"命中"兴安盟"）
     */
    public List<String> selectCitiesByCityKey(@Param("cityKey") String cityKey);

    /**
     * 根据景点ID集合批量查询景点（行程详情组装景点名称用）
     *
     * @param ids 景点ID集合
     * @return 景点列表
     */
    public List<BizScenicSpot> selectBizScenicSpotByIds(Long[] ids);

    /**
     * 批量插入景点（CSV导入用）
     *
     * @param list 景点列表
     * @return 插入条数
     */
    public int batchInsertBizScenicSpot(@Param("list") List<BizScenicSpot> list);

    /**
     * 按景点名称查询已存在的数量（导入查重用）
     *
     * @param names 景点名称列表
     * @return 已存在的名称-数量映射（Map的key为景点名称）
     */
    public List<String> selectExistingSpotNames(@Param("names") List<String> names);

    /**
     * ES全量同步：按主键游标分页查询景点（含标签名聚合），避免大偏移量分页性能问题
     *
     * @param lastId 上一批最大ID，首批传0
     * @param size   每批条数
     * @return 景点列表（tags字段为逗号分隔的标签名）
     */
    public List<BizScenicSpot> selectSpotPageForEsSync(@Param("lastId") Long lastId, @Param("size") int size);

    /**
     * ES增量同步：按ID查询单个景点（含标签名聚合）
     *
     * @param id 景点ID
     * @return 景点对象（tags字段为逗号分隔的标签名）
     */
    public BizScenicSpot selectSpotForEsById(@Param("id") Long id);

    /**
     * ES降级查询：MySQL多字段关键词模糊检索（名称/地址/简介/标签），显式物理分页。
     * 支持多个拆分词：每个词在名称/地址/简介/标签任一字段命中即可（词间OR，贴近ES分词召回），
     * 景点名称命中的词越多排序越靠前。
     *
     * @param keywords 拆分后的关键词列表（为空则不限制关键词）
     * @param cityKey  城市核心名（为空则不限制）
     * @param offset   偏移量
     * @param size     每页数
     * @return 景点列表（名称命中数优先，其次5A/4A等级）
     */
    public List<BizScenicSpot> searchSpotByKeywordLike(@Param("keywords") List<String> keywords,
                                                       @Param("cityKey") String cityKey,
                                                       @Param("offset") int offset,
                                                       @Param("size") int size);

    /**
     * ES降级查询：统计关键词模糊检索命中总数（与searchSpotByKeywordLike条件一致）
     *
     * @param keywords 拆分后的关键词列表
     * @param cityKey  城市核心名
     * @return 总条数
     */
    public long countSpotByKeywordLike(@Param("keywords") List<String> keywords,
                                       @Param("cityKey") String cityKey);
}
