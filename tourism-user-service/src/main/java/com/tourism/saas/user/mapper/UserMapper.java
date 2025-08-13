package com.tourism.saas.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourism.saas.common.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据用户名查询用户（优化SQL，添加索引）
     */
    @Select("SELECT * FROM t_user WHERE username = #{username} AND deleted = 0")
    User selectByUsername(@Param("username") String username);
    
    /**
     * 根据邮箱查询用户
     */
    @Select("SELECT * FROM t_user WHERE email = #{email} AND deleted = 0")
    User selectByEmail(@Param("email") String email);
    
    /**
     * 根据手机号查询用户
     */
    @Select("SELECT * FROM t_user WHERE phone = #{phone} AND deleted = 0")
    User selectByPhone(@Param("phone") String phone);
    
    /**
     * 更新用户最后登录信息
     */
    @Update("UPDATE t_user SET last_login_time = #{lastLoginTime}, last_login_ip = #{lastLoginIp} " +
            "WHERE id = #{userId} AND deleted = 0")
    int updateLastLoginInfo(@Param("userId") Long userId, 
                           @Param("lastLoginTime") LocalDateTime lastLoginTime,
                           @Param("lastLoginIp") String lastLoginIp);
}