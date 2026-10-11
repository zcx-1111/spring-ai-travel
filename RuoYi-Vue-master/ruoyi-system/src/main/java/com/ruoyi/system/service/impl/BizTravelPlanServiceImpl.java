package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.BizTravelPlanMapper;
import com.ruoyi.system.domain.BizTravelPlan;
import com.ruoyi.system.service.IBizTravelPlanService;

/**
 * 行程方案主Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
@Service
public class BizTravelPlanServiceImpl implements IBizTravelPlanService 
{
    @Autowired
    private BizTravelPlanMapper bizTravelPlanMapper;

    /**
     * 查询行程方案主
     * 
     * @param id 行程方案主主键
     * @return 行程方案主
     */
    @Override
    public BizTravelPlan selectBizTravelPlanById(Long id)
    {
        return bizTravelPlanMapper.selectBizTravelPlanById(id);
    }

    /**
     * 查询行程方案主列表
     * 
     * @param bizTravelPlan 行程方案主
     * @return 行程方案主
     */
    @Override
    public List<BizTravelPlan> selectBizTravelPlanList(BizTravelPlan bizTravelPlan)
    {
        return bizTravelPlanMapper.selectBizTravelPlanList(bizTravelPlan);
    }

    /**
     * 新增行程方案主
     * 
     * @param bizTravelPlan 行程方案主
     * @return 结果
     */
    @Override
    public int insertBizTravelPlan(BizTravelPlan bizTravelPlan)
    {
        bizTravelPlan.setCreateTime(DateUtils.getNowDate());
        return bizTravelPlanMapper.insertBizTravelPlan(bizTravelPlan);
    }

    /**
     * 修改行程方案主
     * 
     * @param bizTravelPlan 行程方案主
     * @return 结果
     */
    @Override
    public int updateBizTravelPlan(BizTravelPlan bizTravelPlan)
    {
        bizTravelPlan.setUpdateTime(DateUtils.getNowDate());
        return bizTravelPlanMapper.updateBizTravelPlan(bizTravelPlan);
    }

    /**
     * 批量删除行程方案主
     * 
     * @param ids 需要删除的行程方案主主键
     * @return 结果
     */
    @Override
    public int deleteBizTravelPlanByIds(Long[] ids)
    {
        return bizTravelPlanMapper.deleteBizTravelPlanByIds(ids);
    }

    /**
     * 删除行程方案主信息
     * 
     * @param id 行程方案主主键
     * @return 结果
     */
    @Override
    public int deleteBizTravelPlanById(Long id)
    {
        return bizTravelPlanMapper.deleteBizTravelPlanById(id);
    }
}
