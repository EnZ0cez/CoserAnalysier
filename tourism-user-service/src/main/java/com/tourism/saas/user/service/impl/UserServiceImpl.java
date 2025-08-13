package com.tourism.saas.user.service.impl;

import com.tourism.saas.common.entity.User;
import com.tourism.saas.common.result.Result;
import com.tourism.saas.common.result.ResultCode;
import com.tourism.saas.common.utils.JwtUtils;
import com.tourism.saas.user.dto.UserLoginDTO;
import com.tourism.saas.user.dto.UserRegisterDTO;
import com.tourism.saas.user.mapper.UserMapper;
import com.tourism.saas.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    private static final String USER_CACHE_PREFIX = "user:cache:";
    private static final String USER_TOKEN_PREFIX = "user:token:";
    private static final long CACHE_EXPIRE_TIME = 30; // 30分钟
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> register(UserRegisterDTO registerDTO) {
        try {
            // 验证密码确认
            if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
                return Result.error("两次输入的密码不一致");
            }
            
            // 验证用户名是否已存在
            if (existsByUsername(registerDTO.getUsername())) {
                return Result.error(ResultCode.USER_ALREADY_EXISTS);
            }
            
            // 验证邮箱是否已存在
            if (StringUtils.hasText(registerDTO.getEmail()) && existsByEmail(registerDTO.getEmail())) {
                return Result.error("邮箱已被注册");
            }
            
            // 验证手机号是否已存在
            if (StringUtils.hasText(registerDTO.getPhone()) && existsByPhone(registerDTO.getPhone())) {
                return Result.error("手机号已被注册");
            }
            
            // 创建用户对象
            User user = new User();
            user.setUsername(registerDTO.getUsername());
            user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
            user.setNickname(StringUtils.hasText(registerDTO.getNickname()) ? 
                            registerDTO.getNickname() : registerDTO.getUsername());
            user.setEmail(registerDTO.getEmail());
            user.setPhone(registerDTO.getPhone());
            user.setStatus(1); // 正常状态
            user.setUserType(1); // 普通用户
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            user.setDeleted(0);
            
            // 保存用户
            int result = userMapper.insert(user);
            if (result > 0) {
                log.info("用户注册成功: {}", registerDTO.getUsername());
                return Result.success("注册成功");
            } else {
                return Result.error("注册失败");
            }
            
        } catch (Exception e) {
            log.error("用户注册失败", e);
            return Result.error("注册失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result<String> login(UserLoginDTO loginDTO, String clientIp) {
        try {
            // 从缓存中查询用户
            String cacheKey = USER_CACHE_PREFIX + loginDTO.getUsername();
            User user = (User) redisTemplate.opsForValue().get(cacheKey);
            
            if (user == null) {
                // 缓存中没有，从数据库查询
                user = userMapper.selectByUsername(loginDTO.getUsername());
                if (user != null) {
                    // 存入缓存
                    redisTemplate.opsForValue().set(cacheKey, user, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
                }
            }
            
            if (user == null) {
                return Result.error(ResultCode.USERNAME_OR_PASSWORD_ERROR);
            }
            
            // 验证账户状态
            if (user.getStatus() == 0) {
                return Result.error(ResultCode.USER_DISABLED);
            }
            
            // 验证密码
            if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
                return Result.error(ResultCode.USERNAME_OR_PASSWORD_ERROR);
            }
            
            // 生成JWT Token
            String token = jwtUtils.generateToken(user.getId(), user.getUsername());
            
            // 将Token存入Redis，设置过期时间
            String tokenKey = USER_TOKEN_PREFIX + user.getId();
            redisTemplate.opsForValue().set(tokenKey, token, 24, TimeUnit.HOURS);
            
            // 更新用户最后登录信息
            updateLastLoginInfo(user.getId(), clientIp);
            
            log.info("用户登录成功: {}", loginDTO.getUsername());
            return Result.success("登录成功", token);
            
        } catch (Exception e) {
            log.error("用户登录失败", e);
            return Result.error("登录失败: " + e.getMessage());
        }
    }
    
    @Override
    public User getUserByUsername(String username) {
        // 先从缓存查询
        String cacheKey = USER_CACHE_PREFIX + username;
        User user = (User) redisTemplate.opsForValue().get(cacheKey);
        
        if (user == null) {
            // 从数据库查询
            user = userMapper.selectByUsername(username);
            if (user != null) {
                // 存入缓存
                redisTemplate.opsForValue().set(cacheKey, user, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
            }
        }
        
        return user;
    }
    
    @Override
    public User getUserById(Long userId) {
        if (userId == null) {
            return null;
        }
        
        // 先从缓存查询
        String cacheKey = USER_CACHE_PREFIX + "id:" + userId;
        User user = (User) redisTemplate.opsForValue().get(cacheKey);
        
        if (user == null) {
            // 从数据库查询
            user = userMapper.selectById(userId);
            if (user != null) {
                // 存入缓存
                redisTemplate.opsForValue().set(cacheKey, user, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
            }
        }
        
        return user;
    }
    
    @Override
    public boolean existsByUsername(String username) {
        User user = userMapper.selectByUsername(username);
        return user != null;
    }
    
    @Override
    public boolean existsByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        User user = userMapper.selectByEmail(email);
        return user != null;
    }
    
    @Override
    public boolean existsByPhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return false;
        }
        User user = userMapper.selectByPhone(phone);
        return user != null;
    }
    
    @Override
    public void updateLastLoginInfo(Long userId, String clientIp) {
        try {
            userMapper.updateLastLoginInfo(userId, LocalDateTime.now(), clientIp);
            
            // 清除相关缓存
            String cacheKey = USER_CACHE_PREFIX + "id:" + userId;
            redisTemplate.delete(cacheKey);
            
        } catch (Exception e) {
            log.error("更新用户最后登录信息失败", e);
        }
    }
}