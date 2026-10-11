package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.TourUserPreference;

/**
 * 用户旅游偏好Service接口
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
public interface ITourUserPreferenceService 
{
    /**
     * 查询用户旅游偏好
     * 
     * @param id 用户旅游偏好主键
     * @return 用户旅游偏好
     */
    public TourUserPreference selectTourUserPreferenceById(Long id);

    /**
     * 查询用户旅游偏好列表
     * 
     * @param tourUserPreference 用户旅游偏好
     * @return 用户旅游偏好集合
     */
    public List<TourUserPreference> selectTourUserPreferenceList(TourUserPreference tourUserPreference);

    /**
     * 新增用户旅游偏好
     * 
     * @param tourUserPreference 用户旅游偏好
     * @return 结果
     */
    public int insertTourUserPreference(TourUserPreference tourUserPreference);

    /**
     * 修改用户旅游偏好
     * 
     * @param tourUserPreference 用户旅游偏好
     * @return 结果
     */
    public int updateTourUserPreference(TourUserPreference tourUserPreference);

    /**
     * 批量删除用户旅游偏好
     * 
     * @param ids 需要删除的用户旅游偏好主键集合
     * @return 结果
     */
    public int deleteTourUserPreferenceByIds(Long[] ids);

    /**
     * 删除用户旅游偏好信息
     * 
     * @param id 用户旅游偏好主键
     * @return 结果
     */
    public int deleteTourUserPreferenceById(Long id);
}
