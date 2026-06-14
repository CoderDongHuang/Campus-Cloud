package com.sharecampus.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sharecampus.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_address")
public class UserAddress extends BaseEntity {
    private Long userId;
    private String contactName;
    private String contactPhone;
    private String campusName;
    private String buildingName;
    private String roomNumber;
    private Integer isDefault;
}
