package com.sharecampus.file.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("file_info")
public class FileInfo {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private String objectName;
    private String originalName;
    private Long fileSize;
    private String contentType;
    private String url;
    private Integer refCount;
    private LocalDateTime createTime;
    private Integer isDeleted;
}
