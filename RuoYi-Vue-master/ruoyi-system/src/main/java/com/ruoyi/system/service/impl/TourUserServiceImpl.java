package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.TourUser;
import com.ruoyi.system.mapper.TourUserMapper;
import com.ruoyi.system.service.ITourUserService;

/**
 * 游客用户 服务层实现（管理端CRUD + 前台登录注册）
 *
 * @author ruoyi
 * @date 2026-09-19
 */
@Service
public class TourUserServiceImpl implements ITourUserService
{
    /** 手机号格式：1开头的11位数字 */
    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";

    @Autowired
    private TourUserMapper tourUserMapper;

    /**
     * 查询游客用户列表（管理端CRUD）
     */
    @Override
    public List<TourUser> selectTourUserList(TourUser tourUser)
    {
        return tourUserMapper.selectTourUserList(tourUser);
    }

    /**
     * 根据ID查询游客用户
     */
    @Override
    public TourUser selectTourUserById(Long id)
    {
        return tourUserMapper.selectTourUserById(id);
    }

    /**
     * 新增游客用户（管理端，密码BCrypt加密）
     */
    @Override
    public int insertTourUser(TourUser tourUser)
    {
        // 用户名唯一性校验
        if (tourUserMapper.selectTourUserByUsername(tourUser.getUsername()) != null)
        {
            throw new ServiceException("用户名已被占用");
        }
        // 密码存在时BCrypt加密存储
        if (StringUtils.isNotEmpty(tourUser.getPassword()))
        {
            tourUser.setPassword(SecurityUtils.encryptPassword(tourUser.getPassword()));
        }
        tourUser.setCreateTime(DateUtils.getNowDate());
        return tourUserMapper.insertTourUser(tourUser);
    }

    /**
     * 修改游客用户（管理端，新密码BCrypt加密）
     */
    @Override
    public int updateTourUser(TourUser tourUser)
    {
        // 若传入明文新密码则加密（BCrypt密文长度60且以$2开头，据此区分）
        if (StringUtils.isNotEmpty(tourUser.getPassword()) && !tourUser.getPassword().startsWith("$2"))
        {
            tourUser.setPassword(SecurityUtils.encryptPassword(tourUser.getPassword()));
        }
        tourUser.setUpdateTime(DateUtils.getNowDate());
        return tourUserMapper.updateTourUser(tourUser);
    }

    /**
     * 批量删除游客用户（软删除）
     */
    @Override
    public int deleteTourUserByIds(Long[] ids)
    {
        return tourUserMapper.deleteTourUserByIds(ids);
    }

    /**
     * 游客登录校验（前台）
     * 注意：token 的生成放在控制器层完成（TokenService 位于 framework 模块）
     */
    @Override
    public TourUser login(String username, String password)
    {
        // 参数非空校验
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password))
        {
            throw new ServiceException("用户名和密码不能为空");
        }
        // 查询用户
        TourUser tourUser = tourUserMapper.selectTourUserByUsername(username);
        if (tourUser == null)
        {
            throw new ServiceException("用户名或密码错误");
        }
        // 账号状态校验（1=正常，0=停用）
        if (tourUser.getStatus() != null && tourUser.getStatus() == 0)
        {
            throw new ServiceException("该账号已被停用，请联系管理员");
        }
        // 密码校验（BCrypt比对）
        if (!SecurityUtils.matchesPassword(password, tourUser.getPassword()))
        {
            throw new ServiceException("用户名或密码错误");
        }
        // 登录成功，返回用户信息（不回传密码）
        tourUser.setPassword(null);
        return tourUser;
    }

    /**
     * 游客注册（前台）：校验唯一性 -> BCrypt加密密码 -> 写入tour_user表
     */
    @Override
    public void register(String username, String password, String phone)
    {
        // 参数非空校验
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password))
        {
            throw new ServiceException("用户名和密码不能为空");
        }
        // 长度校验（与前端规则保持一致）
        if (username.length() < 3 || username.length() > 20)
        {
            throw new ServiceException("用户名长度必须为3到20个字符");
        }
        if (password.length() < 5 || password.length() > 20)
        {
            throw new ServiceException("密码长度必须为5到20个字符");
        }
        // 手机号格式校验（选填时也需格式正确）
        if (StringUtils.isNotEmpty(phone) && !phone.matches(PHONE_REGEX))
        {
            throw new ServiceException("手机号格式不正确");
        }
        // 用户名唯一性校验
        if (tourUserMapper.selectTourUserByUsername(username) != null)
        {
            throw new ServiceException("用户名已被占用，请换一个");
        }
        // 构建用户对象并入库
        TourUser tourUser = new TourUser();
        tourUser.setUsername(username);
        // BCrypt加密后存储
        tourUser.setPassword(SecurityUtils.encryptPassword(password));
        // 昵称默认与用户名一致
        tourUser.setNickName(username);
        tourUser.setPhone(StringUtils.isNotEmpty(phone) ? phone : null);
        // 1=正常
        tourUser.setStatus(1);
        tourUser.setCreateTime(DateUtils.getNowDate());
        tourUserMapper.insertTourUser(tourUser);
    }
}
