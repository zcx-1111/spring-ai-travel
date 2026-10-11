package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.BizUserTravelPreference;

/**
 * 用户旅游偏好Mapper接口
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
public interface BizUserTravelPreferenceMapper 
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
     * 删除用户旅游偏好
     * 
     * @param id 用户旅游偏好主键
     * @return 结果
     */
    public int deleteBizUserTravelPreferenceById(Long id);

    /**
     * 批量删除用户旅游偏好
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBizUserTravelPreferenceByIds(Long[] ids);
}
