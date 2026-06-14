package com.sharecampus.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sharecampus.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("worker_certification")
public class WorkerCertification extends BaseEntity {
    private Long userId;
    private String realName;
    private String idCard;
    private String idCardFront;
    private String idCardBack;
    private String skillCertificate;
    private String skills;       // JSON 数组
    private Integer status;      // 0待审核 1通过 2驳回
    private String auditRemark;
}
