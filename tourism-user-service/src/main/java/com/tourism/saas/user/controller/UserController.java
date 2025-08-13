package com.tourism.saas.user.controller;

import com.tourism.saas.common.result.Result;
import com.tourism.saas.user.dto.UserLoginDTO;
import com.tourism.saas.user.dto.UserRegisterDTO;
import com.tourism.saas.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 用户控制器
 */
@Slf4j
@Api(tags = "用户管理")
@RestController
@RequestMapping("/api/user")
@Validated
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 用户注册
     */
    @ApiOperation("用户注册")
    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        log.info("用户注册请求: {}", registerDTO.getUsername());
        return userService.register(registerDTO);
    }
    
    /**
     * 用户登录
     */
    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody UserLoginDTO loginDTO, 
                               HttpServletRequest request) {
        log.info("用户登录请求: {}", loginDTO.getUsername());
        String clientIp = getClientIpAddress(request);
        return userService.login(loginDTO, clientIp);
    }
    
    /**
     * 检查用户名是否可用
     */
    @ApiOperation("检查用户名是否可用")
    @GetMapping("/check-username")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        boolean exists = userService.existsByUsername(username);
        return Result.success(!exists);
    }
    
    /**
     * 检查邮箱是否可用
     */
    @ApiOperation("检查邮箱是否可用")
    @GetMapping("/check-email")
    public Result<Boolean> checkEmail(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        return Result.success(!exists);
    }
    
    /**
     * 检查手机号是否可用
     */
    @ApiOperation("检查手机号是否可用")
    @GetMapping("/check-phone")
    public Result<Boolean> checkPhone(@RequestParam String phone) {
        boolean exists = userService.existsByPhone(phone);
        return Result.success(!exists);
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}