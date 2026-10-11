package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.BizTravelPlanDaySpotMapper;
import com.ruoyi.system.domain.BizTravelPlanDaySpot;
import com.ruoyi.system.service.IBizTravelPlanDaySpotService;

/**
 * 每日行程景点关联Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
@Service
public class BizTravelPlanDaySpotServiceImpl implements IBizTravelPlanDaySpotService 
{
    @Autowired
    private BizTravelPlanDaySpotMapper bizTravelPlanDaySpotMapper;

    /**
     * 查询每日行程景点关联
     * 
     * @param id 每日行程景点关联主键
     * @return 每日行程景点关联
     */
    @Override
    public BizTravelPlanDaySpot selectBizTravelPlanDaySpotById(Long id)
    {
        return bizTravelPlanDaySpotMapper.selectBizTravelPlanDaySpotById(id);
    }

    /**
     * 查询每日行程景点关联列表
     * 
     * @param bizTravelPlanDaySpot 每日行程景点关联
     * @return 每日行程景点关联
     */
    @Override
    public List<BizTravelPlanDaySpot> selectBizTravelPlanDaySpotList(BizTravelPlanDaySpot bizTravelPlanDaySpot)
    {
        return bizTravelPlanDaySpotMapper.selectBizTravelPlanDaySpotList(bizTravelPlanDaySpot);
    }

    /**
     * 新增每日行程景点关联
     * 
     * @param bizTravelPlanDaySpot 每日行程景点关联
     * @return 结果
     */
    @Override
    public int insertBizTravelPlanDaySpot(BizTravelPlanDaySpot bizTravelPlanDaySpot)
    {
        bizTravelPlanDaySpot.setCreateTime(DateUtils.getNowDate());
        return bizTravelPlanDaySpotMapper.insertBizTravelPlanDaySpot(bizTravelPlanDaySpot);
    }

    /**
     * 修改每日行程景点关联
     * 
     * @param bizTravelPlanDaySpot 每日行程景点关联
     * @return 结果
     */
    @Override
    public int updateBizTravelPlanDaySpot(BizTravelPlanDaySpot bizTravelPlanDaySpot)
    {
        bizTravelPlanDaySpot.setUpdateTime(DateUtils.getNowDate());
        return bizTravelPlanDaySpotMapper.updateBizTravelPlanDaySpot(bizTravelPlanDaySpot);
    }

    /**
     * 批量删除每日行程景点关联
     * 
     * @param ids 需要删除的每日行程景点关联主键
     * @return 结果
     */
    @Override
    public int deleteBizTravelPlanDaySpotByIds(Long[] ids)
    {
        return bizTravelPlanDaySpotMapper.deleteBizTravelPlanDaySpotByIds(ids);
    }

    /**
     * 删除每日行程景点关联信息
     * 
     * @param id 每日行程景点关联主键
     * @return 结果
     */
    @Override
    public int deleteBizTravelPlanDaySpotById(Long id)
    {
        return bizTravelPlanDaySpotMapper.deleteBizTravelPlanDaySpotById(id);
    }
}
