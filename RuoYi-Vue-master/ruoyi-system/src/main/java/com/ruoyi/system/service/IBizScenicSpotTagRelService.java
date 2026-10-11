package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.BizScenicSpotTagRel;

/**
 * 景点-标签关联Service接口
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
public interface IBizScenicSpotTagRelService 
{
    /**
     * 查询景点-标签关联
     * 
     * @param id 景点-标签关联主键
     * @return 景点-标签关联
     */
    public BizScenicSpotTagRel selectBizScenicSpotTagRelById(Long id);

    /**
     * 查询景点-标签关联列表
     * 
     * @param bizScenicSpotTagRel 景点-标签关联
     * @return 景点-标签关联集合
     */
    public List<BizScenicSpotTagRel> selectBizScenicSpotTagRelList(BizScenicSpotTagRel bizScenicSpotTagRel);

    /**
     * 新增景点-标签关联
     * 
     * @param bizScenicSpotTagRel 景点-标签关联
     * @return 结果
     */
    public int insertBizScenicSpotTagRel(BizScenicSpotTagRel bizScenicSpotTagRel);

    /**
     * 修改景点-标签关联
     * 
     * @param bizScenicSpotTagRel 景点-标签关联
     * @return 结果
     */
    public int updateBizScenicSpotTagRel(BizScenicSpotTagRel bizScenicSpotTagRel);

    /**
     * 批量删除景点-标签关联
     * 
     * @param ids 需要删除的景点-标签关联主键集合
     * @return 结果
     */
    public int deleteBizScenicSpotTagRelByIds(Long[] ids);

    /**
     * 删除景点-标签关联信息
     * 
     * @param id 景点-标签关联主键
     * @return 结果
     */
    public int deleteBizScenicSpotTagRelById(Long id);
}
