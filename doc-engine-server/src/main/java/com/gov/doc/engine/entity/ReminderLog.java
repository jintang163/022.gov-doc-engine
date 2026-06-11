package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("reminder_log")
public class ReminderLog extends BaseEntity {

    private String sourceType;
    private String sourceId;
    private Long incomingId;
    private Long handlingId;
    private String docNumber;
    private String docTitle;
    private String urgeNo;
    private String urgeType;
    private String urgedUserId;
    private String urgedUserName;
    private String urgedDeptId;
    private String urgedDeptName;
    private String urgeContent;
    private String urgeStatus;
    private LocalDateTime acknowledgeTime;
    private LocalDateTime extractTime;
}
