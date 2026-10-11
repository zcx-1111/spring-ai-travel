package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.BizUserTravelPreference;

/**
 * 用户旅游偏好Service接口
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
public interface IBizUserTravelPreferenceService 
{
    /**
     * 查询用户旅游偏好
     * 
     * @param id 用户旅游偏好主键
     * @return 用户旅游偏好
     */
    public BizUserTravelPreference selectBizUserTravelPreferenceById(Long id);

    /**
     * 查询用户旅游偏好列表
     * 
     * @param bizUserTravelPreference 用户旅游偏好
     * @return 用户旅游偏好集合
     */
    public List<BizUserTravelPreference> selectBizUserTravelPreferenceList(BizUserTravelPreference bizUserTravelPreference);

    /**
     * 新增用户旅游偏好
     * 
     * @param bizUserTravelPreference 用户旅游偏好
     * @return 结果
     */
    public int insertBizUserTravelPreference(BizUserTravelPreference bizUserTravelPreference);

    /**
     * 修改用户旅游偏好
     * 
     * @param bizUserTravelPreference 用户旅游偏好
     * @return 结果
     */
    public int updateBizUserTravelPreference(BizUserTravelPreference bizUserTravelPreference);

    /**
     * 批量删除用户旅游偏好
     * 
     * @param ids 需要删除的用户旅游偏好主键集合
     * @return 结果
     */
    public int deleteBizUserTravelPreferenceByIds(Long[] ids);

    /**
     * 删除用户旅游偏好信息
     * 
     * @param id 用户旅游偏好主键
     * @return 结果
     */
    public int deleteBizUserTravelPreferenceById(Long id);
}
