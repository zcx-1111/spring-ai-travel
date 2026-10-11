package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.BizTravelPlanDayMapper;
import com.ruoyi.system.domain.BizTravelPlanDay;
import com.ruoyi.system.service.IBizTravelPlanDayService;

/**
 * 行程每日详情Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
@Service
public class BizTravelPlanDayServiceImpl implements IBizTravelPlanDayService 
{
    @Autowired
    private BizTravelPlanDayMapper bizTravelPlanDayMapper;

    /**
     * 查询行程每日详情
     * 
     * @param id 行程每日详情主键
     * @return 行程每日详情
     */
    @Override
    public BizTravelPlanDay selectBizTravelPlanDayById(Long id)
    {
        return bizTravelPlanDayMapper.selectBizTravelPlanDayById(id);
    }

    /**
     * 查询行程每日详情列表
     * 
     * @param bizTravelPlanDay 行程每日详情
     * @return 行程每日详情
     */
    @Override
    public List<BizTravelPlanDay> selectBizTravelPlanDayList(BizTravelPlanDay bizTravelPlanDay)
    {
        return bizTravelPlanDayMapper.selectBizTravelPlanDayList(bizTravelPlanDay);
    }

    /**
     * 新增行程每日详情
     * 
     * @param bizTravelPlanDay 行程每日详情
     * @return 结果
     */
    @Override
    public int insertBizTravelPlanDay(BizTravelPlanDay bizTravelPlanDay)
    {
        bizTravelPlanDay.setCreateTime(DateUtils.getNowDate());
        return bizTravelPlanDayMapper.insertBizTravelPlanDay(bizTravelPlanDay);
    }

    /**
     * 修改行程每日详情
     * 
     * @param bizTravelPlanDay 行程每日详情
     * @return 结果
     */
    @Override
    public int updateBizTravelPlanDay(BizTravelPlanDay bizTravelPlanDay)
    {
        bizTravelPlanDay.setUpdateTime(DateUtils.getNowDate());
        return bizTravelPlanDayMapper.updateBizTravelPlanDay(bizTravelPlanDay);
    }

    /**
     * 批量删除行程每日详情
     * 
     * @param ids 需要删除的行程每日详情主键
     * @return 结果
     */
    @Override
    public int deleteBizTravelPlanDayByIds(Long[] ids)
    {
        return bizTravelPlanDayMapper.deleteBizTravelPlanDayByIds(ids);
    }

    /**
     * 删除行程每日详情信息
     * 
     * @param id 行程每日详情主键
     * @return 结果
     */
    @Override
    public int deleteBizTravelPlanDayById(Long id)
    {
        return bizTravelPlanDayMapper.deleteBizTravelPlanDayById(id);
    }
}
