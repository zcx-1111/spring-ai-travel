package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.TourUser;

/**
 * 游客用户 服务层（管理端CRUD + 前台登录注册）
 *
 * @author ruoyi
 * @date 2026-09-19
 */
public interface ITourUserService
{
    /**
     * 查询游客用户列表（管理端CRUD）
     *
     * @param tourUser 查询条件
     * @return 游客用户集合
     */
    public List<TourUser> selectTourUserList(TourUser tourUser);

    /**
     * 根据ID查询游客用户
     *
     * @param id 主键ID
     * @return 游客用户信息
     */
    public TourUser selectTourUserById(Long id);

    /**
     * 新增游客用户（管理端）
     *
     * @param tourUser 游客用户信息
     * @return 影响行数
     */
    public int insertTourUser(TourUser tourUser);

    /**
     * 修改游客用户（管理端）
     *
     * @param tourUser 游客用户信息
     * @return 影响行数
     */
    public int updateTourUser(TourUser tourUser);

    /**
     * 批量删除游客用户（软删除）
     *
     * @param ids 需要删除的数据主键集合
     * @return 影响行数
     */
    public int deleteTourUserByIds(Long[] ids);

    /**
     * 游客登录校验（前台）
     *
     * @param username 用户名
     * @param password 明文密码
     * @return 校验通过后返回游客用户信息
     */
    public TourUser login(String username, String password);

    /**
     * 游客注册（前台）
     *
     * @param username 用户名
     * @param password 明文密码
     * @param phone 手机号
     */
    public void register(String username, String password, String phone);
}
