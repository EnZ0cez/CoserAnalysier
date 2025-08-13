package com.tourism.saas.common.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 */
@Configuration
public class RabbitMQConfig {
    
    // 订单相关交换机和队列
    public static final String ORDER_EXCHANGE = "tourism.order.exchange";
    public static final String ORDER_CREATE_QUEUE = "tourism.order.create.queue";
    public static final String ORDER_PAY_QUEUE = "tourism.order.pay.queue";
    public static final String ORDER_CANCEL_QUEUE = "tourism.order.cancel.queue";
    
    // 用户行为分析交换机和队列
    public static final String USER_BEHAVIOR_EXCHANGE = "tourism.user.behavior.exchange";
    public static final String USER_BEHAVIOR_QUEUE = "tourism.user.behavior.queue";
    
    // 数据统计交换机和队列
    public static final String ANALYTICS_EXCHANGE = "tourism.analytics.exchange";
    public static final String ANALYTICS_QUEUE = "tourism.analytics.queue";
    
    // 邮件通知交换机和队列
    public static final String NOTIFICATION_EXCHANGE = "tourism.notification.exchange";
    public static final String EMAIL_QUEUE = "tourism.email.queue";
    public static final String SMS_QUEUE = "tourism.sms.queue";
    
    /**
     * JSON消息转换器
     */
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    /**
     * RabbitTemplate配置
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        
        // 开启发布确认
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                System.out.println("消息发送成功: " + correlationData);
            } else {
                System.err.println("消息发送失败: " + cause);
            }
        });
        
        // 开启返回确认
        template.setReturnsCallback(returned -> {
            System.err.println("消息未被路由到队列: " + returned.getMessage());
        });
        
        return template;
    }
    
    /**
     * 监听器工厂配置
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return factory;
    }
    
    // ============================== 订单相关 ==============================
    
    /**
     * 订单交换机
     */
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE, true, false);
    }
    
    /**
     * 订单创建队列
     */
    @Bean
    public Queue orderCreateQueue() {
        return QueueBuilder.durable(ORDER_CREATE_QUEUE)
                .withArgument("x-dead-letter-exchange", ORDER_EXCHANGE + ".dlx")
                .withArgument("x-dead-letter-routing-key", "order.create.dead")
                .build();
    }
    
    /**
     * 订单支付队列
     */
    @Bean
    public Queue orderPayQueue() {
        return QueueBuilder.durable(ORDER_PAY_QUEUE)
                .withArgument("x-dead-letter-exchange", ORDER_EXCHANGE + ".dlx")
                .withArgument("x-dead-letter-routing-key", "order.pay.dead")
                .build();
    }
    
    /**
     * 订单取消队列
     */
    @Bean
    public Queue orderCancelQueue() {
        return QueueBuilder.durable(ORDER_CANCEL_QUEUE)
                .withArgument("x-dead-letter-exchange", ORDER_EXCHANGE + ".dlx")
                .withArgument("x-dead-letter-routing-key", "order.cancel.dead")
                .build();
    }
    
    /**
     * 绑定订单创建队列
     */
    @Bean
    public Binding orderCreateBinding() {
        return BindingBuilder.bind(orderCreateQueue())
                .to(orderExchange())
                .with("order.create");
    }
    
    /**
     * 绑定订单支付队列
     */
    @Bean
    public Binding orderPayBinding() {
        return BindingBuilder.bind(orderPayQueue())
                .to(orderExchange())
                .with("order.pay");
    }
    
    /**
     * 绑定订单取消队列
     */
    @Bean
    public Binding orderCancelBinding() {
        return BindingBuilder.bind(orderCancelQueue())
                .to(orderExchange())
                .with("order.cancel");
    }
    
    // ============================== 用户行为分析 ==============================
    
    /**
     * 用户行为交换机
     */
    @Bean
    public TopicExchange userBehaviorExchange() {
        return new TopicExchange(USER_BEHAVIOR_EXCHANGE, true, false);
    }
    
    /**
     * 用户行为队列
     */
    @Bean
    public Queue userBehaviorQueue() {
        return QueueBuilder.durable(USER_BEHAVIOR_QUEUE).build();
    }
    
    /**
     * 绑定用户行为队列
     */
    @Bean
    public Binding userBehaviorBinding() {
        return BindingBuilder.bind(userBehaviorQueue())
                .to(userBehaviorExchange())
                .with("user.behavior.*");
    }
    
    // ============================== 数据统计 ==============================
    
    /**
     * 数据统计交换机
     */
    @Bean
    public TopicExchange analyticsExchange() {
        return new TopicExchange(ANALYTICS_EXCHANGE, true, false);
    }
    
    /**
     * 数据统计队列
     */
    @Bean
    public Queue analyticsQueue() {
        return QueueBuilder.durable(ANALYTICS_QUEUE).build();
    }
    
    /**
     * 绑定数据统计队列
     */
    @Bean
    public Binding analyticsBinding() {
        return BindingBuilder.bind(analyticsQueue())
                .to(analyticsExchange())
                .with("analytics.*");
    }
    
    // ============================== 通知服务 ==============================
    
    /**
     * 通知交换机
     */
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE, true, false);
    }
    
    /**
     * 邮件队列
     */
    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(EMAIL_QUEUE).build();
    }
    
    /**
     * 短信队列
     */
    @Bean
    public Queue smsQueue() {
        return QueueBuilder.durable(SMS_QUEUE).build();
    }
    
    /**
     * 绑定邮件队列
     */
    @Bean
    public Binding emailBinding() {
        return BindingBuilder.bind(emailQueue())
                .to(notificationExchange())
                .with("notification.email");
    }
    
    /**
     * 绑定短信队列
     */
    @Bean
    public Binding smsBinding() {
        return BindingBuilder.bind(smsQueue())
                .to(notificationExchange())
                .with("notification.sms");
    }
}