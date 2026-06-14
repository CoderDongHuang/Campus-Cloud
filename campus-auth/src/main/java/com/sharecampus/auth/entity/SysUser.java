package com.sharecampus.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sharecampus.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    /** 手机号 */
    private String phone;
    /** 密码（BCrypt 加密） */
    private String password;
    /** 昵称 */
    private String nickname;
    /** 头像 */
    private String avatar;
    /** 用户类型：STUDENT / WORKER / ADMIN */
    private String userType;
    /** 状态：1=正常 0=禁用 */
    private Integer status;
    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;
}
