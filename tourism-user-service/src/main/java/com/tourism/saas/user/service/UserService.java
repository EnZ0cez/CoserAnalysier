package com.tourism.saas.user.service;

import com.tourism.saas.common.entity.User;
import com.tourism.saas.common.result.Result;
import com.tourism.saas.user.dto.UserLoginDTO;
import com.tourism.saas.user.dto.UserRegisterDTO;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 用户注册
     */
    Result<String> register(UserRegisterDTO registerDTO);
    
    /**
     * 用户登录
     */
    Result<String> login(UserLoginDTO loginDTO, String clientIp);
    
    /**
     * 根据用户名查询用户
     */
    User getUserByUsername(String username);
    
    /**
     * 根据用户ID查询用户
     */
    User getUserById(Long userId);
    
    /**
     * 验证用户名是否已存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 验证邮箱是否已存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 验证手机号是否已存在
     */
    boolean existsByPhone(String phone);
    
    /**
     * 更新用户最后登录信息
     */
    void updateLastLoginInfo(Long userId, String clientIp);
}