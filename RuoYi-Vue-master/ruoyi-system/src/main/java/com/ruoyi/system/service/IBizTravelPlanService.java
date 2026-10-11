package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.BizTravelPlan;

/**
 * 行程方案主Service接口
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
public interface IBizTravelPlanService 
{
    /**
     * 查询行程方案主
     * 
     * @param id 行程方案主主键
     * @return 行程方案主
     */
    public BizTravelPlan selectBizTravelPlanById(Long id);

    /**
     * 查询行程方案主列表
     * 
     * @param bizTravelPlan 行程方案主
     * @return 行程方案主集合
     */
    public List<BizTravelPlan> selectBizTravelPlanList(BizTravelPlan bizTravelPlan);

    /**
     * 新增行程方案主
     * 
     * @param bizTravelPlan 行程方案主
     * @return 结果
     */
    public int insertBizTravelPlan(BizTravelPlan bizTravelPlan);

    /**
     * 修改行程方案主
     * 
     * @param bizTravelPlan 行程方案主
     * @return 结果
     */
    public int updateBizTravelPlan(BizTravelPlan bizTravelPlan);

    /**
     * 批量删除行程方案主
     * 
     * @param ids 需要删除的行程方案主主键集合
     * @return 结果
     */
    public int deleteBizTravelPlanByIds(Long[] ids);

    /**
     * 删除行程方案主信息
     * 
     * @param id 行程方案主主键
     * @return 结果
     */
    public int deleteBizTravelPlanById(Long id);
}
