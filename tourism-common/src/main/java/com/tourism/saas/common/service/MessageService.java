package com.tourism.saas.common.service;

import com.tourism.saas.common.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 消息发送服务类
 */
@Slf4j
@Service
public class MessageService {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    // ============================== 订单相关消息 ==============================
    
    /**
     * 发送订单创建消息
     */
    public void sendOrderCreateMessage(Long orderId, Long userId, Map<String, Object> orderData) {
        try {
            Map<String, Object> message = Map.of(
                "orderId", orderId,
                "userId", userId,
                "orderData", orderData,
                "timestamp", System.currentTimeMillis()
            );
            
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                "order.create",
                message
            );
            
            log.info("发送订单创建消息成功，订单ID: {}", orderId);
        } catch (Exception e) {
            log.error("发送订单创建消息失败，订单ID: {}", orderId, e);
        }
    }
    
    /**
     * 发送订单支付消息
     */
    public void sendOrderPayMessage(Long orderId, Long userId, String paymentMethod, Double amount) {
        try {
            Map<String, Object> message = Map.of(
                "orderId", orderId,
                "userId", userId,
                "paymentMethod", paymentMethod,
                "amount", amount,
                "timestamp", System.currentTimeMillis()
            );
            
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                "order.pay",
                message
            );
            
            log.info("发送订单支付消息成功，订单ID: {}", orderId);
        } catch (Exception e) {
            log.error("发送订单支付消息失败，订单ID: {}", orderId, e);
        }
    }
    
    /**
     * 发送订单取消消息
     */
    public void sendOrderCancelMessage(Long orderId, Long userId, String cancelReason) {
        try {
            Map<String, Object> message = Map.of(
                "orderId", orderId,
                "userId", userId,
                "cancelReason", cancelReason,
                "timestamp", System.currentTimeMillis()
            );
            
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                "order.cancel",
                message
            );
            
            log.info("发送订单取消消息成功，订单ID: {}", orderId);
        } catch (Exception e) {
            log.error("发送订单取消消息失败，订单ID: {}", orderId, e);
        }
    }
    
    // ============================== 用户行为分析消息 ==============================
    
    /**
     * 发送用户行为消息
     */
    public void sendUserBehaviorMessage(Long userId, String actionType, String targetType, 
                                       Long targetId, Map<String, Object> extraData) {
        try {
            Map<String, Object> message = Map.of(
                "userId", userId != null ? userId : 0L,
                "actionType", actionType,
                "targetType", targetType,
                "targetId", targetId != null ? targetId : 0L,
                "extraData", extraData != null ? extraData : Map.of(),
                "timestamp", System.currentTimeMillis()
            );
            
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.USER_BEHAVIOR_EXCHANGE,
                "user.behavior." + actionType,
                message
            );
            
            log.debug("发送用户行为消息成功，用户ID: {}, 行为: {}", userId, actionType);
        } catch (Exception e) {
            log.error("发送用户行为消息失败，用户ID: {}, 行为: {}", userId, actionType, e);
        }
    }
    
    /**
     * 发送页面浏览消息
     */
    public void sendPageViewMessage(Long userId, String pageUrl, String referrerUrl) {
        sendUserBehaviorMessage(userId, "view", "page", null, 
            Map.of("pageUrl", pageUrl, "referrerUrl", referrerUrl != null ? referrerUrl : ""));
    }
    
    /**
     * 发送产品浏览消息
     */
    public void sendProductViewMessage(Long userId, Long productId, String productName) {
        sendUserBehaviorMessage(userId, "view", "product", productId, 
            Map.of("productName", productName));
    }
    
    /**
     * 发送搜索行为消息
     */
    public void sendSearchMessage(Long userId, String keyword, Integer resultCount) {
        sendUserBehaviorMessage(userId, "search", "product", null, 
            Map.of("keyword", keyword, "resultCount", resultCount));
    }
    
    /**
     * 发送点击行为消息
     */
    public void sendClickMessage(Long userId, String targetType, Long targetId, String targetName) {
        sendUserBehaviorMessage(userId, "click", targetType, targetId, 
            Map.of("targetName", targetName));
    }
    
    // ============================== 数据统计消息 ==============================
    
    /**
     * 发送数据统计消息
     */
    public void sendAnalyticsMessage(String metricType, Map<String, Object> data) {
        try {
            Map<String, Object> message = Map.of(
                "metricType", metricType,
                "data", data,
                "timestamp", System.currentTimeMillis()
            );
            
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.ANALYTICS_EXCHANGE,
                "analytics." + metricType,
                message
            );
            
            log.debug("发送数据统计消息成功，指标类型: {}", metricType);
        } catch (Exception e) {
            log.error("发送数据统计消息失败，指标类型: {}", metricType, e);
        }
    }
    
    /**
     * 发送用户注册统计消息
     */
    public void sendUserRegistrationMetric(Long userId, String registrationSource) {
        sendAnalyticsMessage("user_registration", 
            Map.of("userId", userId, "source", registrationSource));
    }
    
    /**
     * 发送订单统计消息
     */
    public void sendOrderMetric(Long orderId, Long userId, Double amount, String status) {
        sendAnalyticsMessage("order_metric", 
            Map.of("orderId", orderId, "userId", userId, "amount", amount, "status", status));
    }
    
    /**
     * 发送收入统计消息
     */
    public void sendRevenueMetric(Double amount, String paymentMethod, String productCategory) {
        sendAnalyticsMessage("revenue_metric", 
            Map.of("amount", amount, "paymentMethod", paymentMethod, "category", productCategory));
    }
    
    // ============================== 通知消息 ==============================
    
    /**
     * 发送邮件通知消息
     */
    public void sendEmailMessage(String to, String subject, String content, String template) {
        try {
            Map<String, Object> message = Map.of(
                "to", to,
                "subject", subject,
                "content", content,
                "template", template != null ? template : "default",
                "timestamp", System.currentTimeMillis()
            );
            
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                "notification.email",
                message
            );
            
            log.info("发送邮件通知消息成功，收件人: {}", to);
        } catch (Exception e) {
            log.error("发送邮件通知消息失败，收件人: {}", to, e);
        }
    }
    
    /**
     * 发送短信通知消息
     */
    public void sendSmsMessage(String phone, String content, String template) {
        try {
            Map<String, Object> message = Map.of(
                "phone", phone,
                "content", content,
                "template", template != null ? template : "default",
                "timestamp", System.currentTimeMillis()
            );
            
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                "notification.sms",
                message
            );
            
            log.info("发送短信通知消息成功，手机号: {}", phone);
        } catch (Exception e) {
            log.error("发送短信通知消息失败，手机号: {}", phone, e);
        }
    }
    
    // ============================== 延迟消息 ==============================
    
    /**
     * 发送延迟消息（用于订单超时取消等场景）
     */
    public void sendDelayedMessage(String exchange, String routingKey, Object message, long delayMs) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message, msg -> {
                msg.getMessageProperties().setDelay((int) delayMs);
                return msg;
            });
            
            log.info("发送延迟消息成功，延迟时间: {}ms", delayMs);
        } catch (Exception e) {
            log.error("发送延迟消息失败，延迟时间: {}ms", delayMs, e);
        }
    }
    
    /**
     * 发送订单超时取消延迟消息
     */
    public void sendOrderTimeoutCancelMessage(Long orderId, long delayMs) {
        Map<String, Object> message = Map.of(
            "orderId", orderId,
            "action", "timeout_cancel",
            "timestamp", System.currentTimeMillis()
        );
        
        sendDelayedMessage(RabbitMQConfig.ORDER_EXCHANGE, "order.timeout", message, delayMs);
    }
}