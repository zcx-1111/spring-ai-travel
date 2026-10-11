package com.ruoyi.web.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.framework.web.service.TokenService;
import com.ruoyi.system.domain.TourUser;
import com.ruoyi.system.service.ITourUserService;

/**
 * 前台游客登录注册Controller
 * 统一前缀 /api/tour，游客端专用接口（与后台管理端接口隔离）
 *
 * @author ruoyi
 * @date 2026-09-19
 */
@RestController
@RequestMapping("/api/tour/auth")
public class TourAuthController extends BaseController
{
    @Autowired
    private ITourUserService tourUserService;

    @Autowired
    private TokenService tokenService;

    /**
     * 游客登录
     * 请求体：{ username, password }
     * 返回：data = { token, userInfo: { id, username, nickName, avatar } }
     */
    @PostMapping("/login")
    public AjaxResult login(@RequestBody TourLoginBody loginBody)
    {
        // 1. 登录校验（用户存在性、状态、密码比对）
        TourUser tourUser = tourUserService.login(loginBody.getUsername(), loginBody.getPassword());
        // 2. 生成token（复用若依TokenService，后续请求凭此token访问 /api/tour/** 接口）
        String token = createTourToken(tourUser);
        // 3. 组装返回数据
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", tourUser.getId());
        userInfo.put("username", tourUser.getUsername());
        userInfo.put("nickName", tourUser.getNickName());
        userInfo.put("avatar", tourUser.getAvatar());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userInfo", userInfo);
        AjaxResult ajax = AjaxResult.success("登录成功");
        ajax.put(AjaxResult.DATA_TAG, data);
        return ajax;
    }

    /**
     * 游客注册
     * 请求体：{ username, password, phone }
     * 注册成功写入 tour_user 表（密码BCrypt加密）
     */
    @PostMapping("/register")
    public AjaxResult register(@RequestBody TourRegisterBody registerBody)
    {
        tourUserService.register(
                registerBody.getUsername(),
                registerBody.getPassword(),
                registerBody.getPhone());
        AjaxResult ajax = AjaxResult.success();
        ajax.put("msg", "注册成功");
        return ajax;
    }

    /**
     * 为游客用户生成若依体系token
     * 复用LoginUser模型：以tour_user的id作为userId，便于后续接口解析当前游客身份
     */
    private String createTourToken(TourUser tourUser)
    {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(tourUser.getId());
        sysUser.setUserName(tourUser.getUsername());
        sysUser.setNickName(tourUser.getNickName());
        sysUser.setAvatar(tourUser.getAvatar());
        // permissions传空集合：前台接口不使用权限注解校验
        LoginUser loginUser = new LoginUser(tourUser.getId(), null, sysUser, java.util.Collections.emptySet());
        return tokenService.createToken(loginUser);
    }

    /**
     * 游客登录请求体
     */
    public static class TourLoginBody
    {
        private String username;
        private String password;

        public String getUsername()
        {
            return username;
        }

        public void setUsername(String username)
        {
            this.username = username;
        }

        public String getPassword()
        {
            return password;
        }

        public void setPassword(String password)
        {
            this.password = password;
        }
    }

    /**
     * 游客注册请求体
     */
    public static class TourRegisterBody
    {
        private String username;
        private String password;
        private String phone;

        public String getUsername()
        {
            return username;
        }

        public void setUsername(String username)
        {
            this.username = username;
        }

        public String getPassword()
        {
            return password;
        }

        public void setPassword(String password)
        {
            this.password = password;
        }

        public String getPhone()
        {
            return phone;
        }

        public void setPhone(String phone)
        {
            this.phone = phone;
        }
    }
}
