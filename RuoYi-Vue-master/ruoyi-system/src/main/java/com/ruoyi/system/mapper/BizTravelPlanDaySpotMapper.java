package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.BizTravelPlanDaySpot;

/**
 * 每日行程景点关联Mapper接口
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
public interface BizTravelPlanDaySpotMapper 
{
    /**
     * 查询每日行程景点关联
     * 
     * @param id 每日行程景点关联主键
     * @return 每日行程景点关联
     */
    public BizTravelPlanDaySpot selectBizTravelPlanDaySpotById(Long id);

    /**
     * 查询每日行程景点关联列表
     * 
     * @param bizTravelPlanDaySpot 每日行程景点关联
     * @return 每日行程景点关联集合
     */
    public List<BizTravelPlanDaySpot> selectBizTravelPlanDaySpotList(BizTravelPlanDaySpot bizTravelPlanDaySpot);

    /**
     * 新增每日行程景点关联
     * 
     * @param bizTravelPlanDaySpot 每日行程景点关联
     * @return 结果
     */
    public int insertBizTravelPlanDaySpot(BizTravelPlanDaySpot bizTravelPlanDaySpot);

    /**
     * 修改每日行程景点关联
     * 
     * @param bizTravelPlanDaySpot 每日行程景点关联
     * @return 结果
     */
    public int updateBizTravelPlanDaySpot(BizTravelPlanDaySpot bizTravelPlanDaySpot);

    /**
     * 删除每日行程景点关联
     * 
     * @param id 每日行程景点关联主键
     * @return 结果
     */
    public int deleteBizTravelPlanDaySpotById(Long id);

    /**
     * 批量删除每日行程景点关联
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBizTravelPlanDaySpotByIds(Long[] ids);
}
