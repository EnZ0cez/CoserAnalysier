package com.tourism.saas.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode {
    
    // 通用状态码
    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    
    // 用户相关
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    USERNAME_OR_PASSWORD_ERROR(1003, "用户名或密码错误"),
    PASSWORD_ERROR(1004, "密码错误"),
    USER_DISABLED(1005, "用户已被禁用"),
    TOKEN_INVALID(1006, "Token无效"),
    TOKEN_EXPIRED(1007, "Token已过期"),
    
    // 产品相关
    PRODUCT_NOT_FOUND(2001, "产品不存在"),
    PRODUCT_STOCK_INSUFFICIENT(2002, "产品库存不足"),
    PRODUCT_OFFLINE(2003, "产品已下线"),
    
    // 订单相关
    ORDER_NOT_FOUND(3001, "订单不存在"),
    ORDER_STATUS_ERROR(3002, "订单状态错误"),
    ORDER_CANNOT_CANCEL(3003, "订单无法取消"),
    ORDER_ALREADY_PAID(3004, "订单已支付"),
    
    // 系统相关
    SYSTEM_BUSY(9001, "系统繁忙，请稍后重试"),
    DATABASE_ERROR(9002, "数据库操作失败"),
    REDIS_ERROR(9003, "缓存操作失败"),
    MQ_ERROR(9004, "消息队列操作失败");
    
    private final Integer code;
    private final String message;
}