package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.BizScenicSpot;

/**
 * 景点主Service接口
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
public interface IBizScenicSpotService 
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
     * 批量删除景点主
     * 
     * @param ids 需要删除的景点主主键集合
     * @return 结果
     */
    public int deleteBizScenicSpotByIds(Long[] ids);

    /**
     * 删除景点主信息
     * 
     * @param id 景点主主键
     * @return 结果
     */
    public int deleteBizScenicSpotById(Long id);
}
