package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.TourUserPreferenceMapper;
import com.ruoyi.system.domain.TourUserPreference;
import com.ruoyi.system.service.ITourUserPreferenceService;

/**
 * 用户旅游偏好Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
@Service
public class TourUserPreferenceServiceImpl implements ITourUserPreferenceService 
{
    @Autowired
    private TourUserPreferenceMapper tourUserPreferenceMapper;

    /**
     * 查询用户旅游偏好
     * 
     * @param id 用户旅游偏好主键
     * @return 用户旅游偏好
     */
    @Override
    public TourUserPreference selectTourUserPreferenceById(Long id)
    {
        return tourUserPreferenceMapper.selectTourUserPreferenceById(id);
    }

    /**
     * 查询用户旅游偏好列表
     * 
     * @param tourUserPreference 用户旅游偏好
     * @return 用户旅游偏好
     */
    @Override
    public List<TourUserPreference> selectTourUserPreferenceList(TourUserPreference tourUserPreference)
    {
        return tourUserPreferenceMapper.selectTourUserPreferenceList(tourUserPreference);
    }

    /**
     * 新增用户旅游偏好
     * 
     * @param tourUserPreference 用户旅游偏好
     * @return 结果
     */
    @Override
    public int insertTourUserPreference(TourUserPreference tourUserPreference)
    {
        tourUserPreference.setCreateTime(DateUtils.getNowDate());
        return tourUserPreferenceMapper.insertTourUserPreference(tourUserPreference);
    }

    /**
     * 修改用户旅游偏好
     * 
     * @param tourUserPreference 用户旅游偏好
     * @return 结果
     */
    @Override
    public int updateTourUserPreference(TourUserPreference tourUserPreference)
    {
        tourUserPreference.setUpdateTime(DateUtils.getNowDate());
        return tourUserPreferenceMapper.updateTourUserPreference(tourUserPreference);
    }

    /**
     * 批量删除用户旅游偏好
     * 
     * @param ids 需要删除的用户旅游偏好主键
     * @return 结果
     */
    @Override
    public int deleteTourUserPreferenceByIds(Long[] ids)
    {
        return tourUserPreferenceMapper.deleteTourUserPreferenceByIds(ids);
    }

    /**
     * 删除用户旅游偏好信息
     * 
     * @param id 用户旅游偏好主键
     * @return 结果
     */
    @Override
    public int deleteTourUserPreferenceById(Long id)
    {
        return tourUserPreferenceMapper.deleteTourUserPreferenceById(id);
    }
}
