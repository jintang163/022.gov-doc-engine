package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("doc_urge_log")
public class DocUrgeLog extends BaseEntity {

    private Long incomingId;
    private Long handlingId;
    private String urgeNo;
    private String urgeType;
    private String urgedUserId;
    private String urgedUserName;
    private String urgedDeptId;
    private String urgedDeptName;
    private String urgeContent;
    private String status;
    private LocalDateTime acknowledgeTime;
    private String remark;
}
