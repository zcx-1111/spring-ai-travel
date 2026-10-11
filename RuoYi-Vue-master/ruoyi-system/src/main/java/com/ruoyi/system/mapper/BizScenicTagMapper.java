package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.BizScenicTag;

/**
 * 景点标签Mapper接口
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
public interface BizScenicTagMapper 
{
    /**
     * 查询景点标签
     * 
     * @param id 景点标签主键
     * @return 景点标签
     */
    public BizScenicTag selectBizScenicTagById(Long id);

    /**
     * 查询景点标签列表
     * 
     * @param bizScenicTag 景点标签
     * @return 景点标签集合
     */
    public List<BizScenicTag> selectBizScenicTagList(BizScenicTag bizScenicTag);

    /**
     * 新增景点标签
     * 
     * @param bizScenicTag 景点标签
     * @return 结果
     */
    public int insertBizScenicTag(BizScenicTag bizScenicTag);

    /**
     * 修改景点标签
     * 
     * @param bizScenicTag 景点标签
     * @return 结果
     */
    public int updateBizScenicTag(BizScenicTag bizScenicTag);

    /**
     * 删除景点标签
     * 
     * @param id 景点标签主键
     * @return 结果
     */
    public int deleteBizScenicTagById(Long id);

    /**
     * 批量删除景点标签
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBizScenicTagByIds(Long[] ids);
}
