package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.TourUser;

/**
 * 游客用户 数据层
 *
 * @author ruoyi
 * @date 2026-09-19
 */
public interface TourUserMapper
{
    /**
     * 查询游客用户列表（管理端CRUD）
     *
     * @param tourUser 查询条件
     * @return 游客用户集合
     */
    public List<TourUser> selectTourUserList(TourUser tourUser);

    /**
     * 根据用户名查询游客用户（登录用）
     *
     * @param username 用户名
     * @return 游客用户信息
     */
    public TourUser selectTourUserByUsername(String username);

    /**
     * 根据ID查询游客用户
     *
     * @param id 主键ID
     * @return 游客用户信息
     */
    public TourUser selectTourUserById(Long id);

    /**
     * 新增游客用户
     *
     * @param tourUser 游客用户信息
     * @return 影响行数
     */
    public int insertTourUser(TourUser tourUser);

    /**
     * 修改游客用户
     *
     * @param tourUser 游客用户信息
     * @return 影响行数
     */
    public int updateTourUser(TourUser tourUser);

    /**
     * 批量删除游客用户
     *
     * @param ids 需要删除的数据主键集合
     * @return 影响行数
     */
    public int deleteTourUserByIds(Long[] ids);
}
