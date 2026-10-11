package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.BizUserTravelPreferenceMapper;
import com.ruoyi.system.domain.BizUserTravelPreference;
import com.ruoyi.system.service.IBizUserTravelPreferenceService;

/**
 * 用户旅游偏好Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
@Service
public class BizUserTravelPreferenceServiceImpl implements IBizUserTravelPreferenceService 
{
    @Autowired
    private BizUserTravelPreferenceMapper bizUserTravelPreferenceMapper;

    /**
     * 查询用户旅游偏好
     * 
     * @param id 用户旅游偏好主键
     * @return 用户旅游偏好
     */
    @Override
    public BizUserTravelPreference selectBizUserTravelPreferenceById(Long id)
    {
        return bizUserTravelPreferenceMapper.selectBizUserTravelPreferenceById(id);
    }

    /**
     * 查询用户旅游偏好列表
     * 
     * @param bizUserTravelPreference 用户旅游偏好
     * @return 用户旅游偏好
     */
    @Override
    public List<BizUserTravelPreference> selectBizUserTravelPreferenceList(BizUserTravelPreference bizUserTravelPreference)
    {
        return bizUserTravelPreferenceMapper.selectBizUserTravelPreferenceList(bizUserTravelPreference);
    }

    /**
     * 新增用户旅游偏好
     * 
     * @param bizUserTravelPreference 用户旅游偏好
     * @return 结果
     */
    @Override
    public int insertBizUserTravelPreference(BizUserTravelPreference bizUserTravelPreference)
    {
        bizUserTravelPreference.setCreateTime(DateUtils.getNowDate());
        return bizUserTravelPreferenceMapper.insertBizUserTravelPreference(bizUserTravelPreference);
    }

    /**
     * 修改用户旅游偏好
     * 
     * @param bizUserTravelPreference 用户旅游偏好
     * @return 结果
     */
    @Override
    public int updateBizUserTravelPreference(BizUserTravelPreference bizUserTravelPreference)
    {
        bizUserTravelPreference.setUpdateTime(DateUtils.getNowDate());
        return bizUserTravelPreferenceMapper.updateBizUserTravelPreference(bizUserTravelPreference);
    }

    /**
     * 批量删除用户旅游偏好
     * 
     * @param ids 需要删除的用户旅游偏好主键
     * @return 结果
     */
    @Override
    public int deleteBizUserTravelPreferenceByIds(Long[] ids)
    {
        return bizUserTravelPreferenceMapper.deleteBizUserTravelPreferenceByIds(ids);
    }

    /**
     * 删除用户旅游偏好信息
     * 
     * @param id 用户旅游偏好主键
     * @return 结果
     */
    @Override
    public int deleteBizUserTravelPreferenceById(Long id)
    {
        return bizUserTravelPreferenceMapper.deleteBizUserTravelPreferenceById(id);
    }
}
