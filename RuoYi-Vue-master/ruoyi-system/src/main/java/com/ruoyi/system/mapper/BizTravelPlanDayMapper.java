package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.BizTravelPlanDay;

/**
 * 行程每日详情Mapper接口
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
public interface BizTravelPlanDayMapper 
{
    /**
     * 查询行程每日详情
     * 
     * @param id 行程每日详情主键
     * @return 行程每日详情
     */
    public BizTravelPlanDay selectBizTravelPlanDayById(Long id);

    /**
     * 查询行程每日详情列表
     * 
     * @param bizTravelPlanDay 行程每日详情
     * @return 行程每日详情集合
     */
    public List<BizTravelPlanDay> selectBizTravelPlanDayList(BizTravelPlanDay bizTravelPlanDay);

    /**
     * 新增行程每日详情
     * 
     * @param bizTravelPlanDay 行程每日详情
     * @return 结果
     */
    public int insertBizTravelPlanDay(BizTravelPlanDay bizTravelPlanDay);

    /**
     * 修改行程每日详情
     * 
     * @param bizTravelPlanDay 行程每日详情
     * @return 结果
     */
    public int updateBizTravelPlanDay(BizTravelPlanDay bizTravelPlanDay);

    /**
     * 删除行程每日详情
     * 
     * @param id 行程每日详情主键
     * @return 结果
     */
    public int deleteBizTravelPlanDayById(Long id);

    /**
     * 批量删除行程每日详情
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBizTravelPlanDayByIds(Long[] ids);
}
