package com.tourism.saas.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Swagger API文档配置
 */
@Configuration
@EnableOpenApi
public class SwaggerConfig implements WebMvcConfigurer {
    
    /**
     * 用户服务API文档
     */
    @Bean
    public Docket userApi() {
        return new Docket(DocumentationType.OAS_30)
                .groupName("用户服务")
                .apiInfo(userApiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.tourism.saas.user.controller"))
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts());
    }
    
    /**
     * 产品服务API文档
     */
    @Bean
    public Docket productApi() {
        return new Docket(DocumentationType.OAS_30)
                .groupName("产品服务")
                .apiInfo(productApiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.tourism.saas.product.controller"))
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts());
    }
    
    /**
     * 订单服务API文档
     */
    @Bean
    public Docket orderApi() {
        return new Docket(DocumentationType.OAS_30)
                .groupName("订单服务")
                .apiInfo(orderApiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.tourism.saas.order.controller"))
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts());
    }
    
    /**
     * 后台管理服务API文档
     */
    @Bean
    public Docket adminApi() {
        return new Docket(DocumentationType.OAS_30)
                .groupName("后台管理")
                .apiInfo(adminApiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.tourism.saas.admin.controller"))
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts());
    }
    
    /**
     * 用户服务API信息
     */
    private ApiInfo userApiInfo() {
        return new ApiInfoBuilder()
                .title("智慧旅游SaaS平台 - 用户服务API")
                .description("提供用户注册、登录、认证等功能的RESTful API")
                .version("1.0.0")
                .contact(new Contact("开发团队", "https://tourism-saas.com", "dev@tourism-saas.com"))
                .license("MIT License")
                .licenseUrl("https://opensource.org/licenses/MIT")
                .build();
    }
    
    /**
     * 产品服务API信息
     */
    private ApiInfo productApiInfo() {
        return new ApiInfoBuilder()
                .title("智慧旅游SaaS平台 - 产品服务API")
                .description("提供旅游产品查询、搜索、推荐等功能的RESTful API")
                .version("1.0.0")
                .contact(new Contact("开发团队", "https://tourism-saas.com", "dev@tourism-saas.com"))
                .license("MIT License")
                .licenseUrl("https://opensource.org/licenses/MIT")
                .build();
    }
    
    /**
     * 订单服务API信息
     */
    private ApiInfo orderApiInfo() {
        return new ApiInfoBuilder()
                .title("智慧旅游SaaS平台 - 订单服务API")
                .description("提供订单创建、查询、支付、取消等功能的RESTful API")
                .version("1.0.0")
                .contact(new Contact("开发团队", "https://tourism-saas.com", "dev@tourism-saas.com"))
                .license("MIT License")
                .licenseUrl("https://opensource.org/licenses/MIT")
                .build();
    }
    
    /**
     * 后台管理API信息
     */
    private ApiInfo adminApiInfo() {
        return new ApiInfoBuilder()
                .title("智慧旅游SaaS平台 - 后台管理API")
                .description("提供运营数据统计、产品管理、用户管理等功能的RESTful API")
                .version("1.0.0")
                .contact(new Contact("开发团队", "https://tourism-saas.com", "dev@tourism-saas.com"))
                .license("MIT License")
                .licenseUrl("https://opensource.org/licenses/MIT")
                .build();
    }
    
    /**
     * 安全方案配置
     */
    private List<SecurityScheme> securitySchemes() {
        return Collections.singletonList(
                new ApiKey("JWT", "Authorization", "header")
        );
    }
    
    /**
     * 安全上下文配置
     */
    private List<SecurityContext> securityContexts() {
        return Collections.singletonList(
                SecurityContext.builder()
                        .securityReferences(defaultAuth())
                        .operationSelector(operationContext -> 
                            operationContext.requestMappingPattern().matches("/api/.*"))
                        .build()
        );
    }
    
    /**
     * 默认认证配置
     */
    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return Collections.singletonList(
                new SecurityReference("JWT", authorizationScopes)
        );
    }
    
    /**
     * 静态资源处理
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
    }
}