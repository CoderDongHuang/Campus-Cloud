package com.sharecampus.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sharecampus.auth.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    // BaseMapper 提供了 CRUD 方法，无需额外编码
}
