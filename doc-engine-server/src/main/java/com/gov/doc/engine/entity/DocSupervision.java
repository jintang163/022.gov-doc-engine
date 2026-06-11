package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("doc_supervision")
public class DocSupervision extends BaseEntity {

    private String supervisionNo;
    private Long incomingId;
    private Long handlingId;
    private String sourceDocNumber;
    private String docTitle;
    private String supervisionType;
    private String responsibleUserId;
    private String responsibleUserName;
    private String responsibleDeptId;
    private String responsibleDeptName;
    private String leaderId;
    private String leaderName;
    private Integer overdueDays;
    private Integer urgeCount;
    private LocalDate deadline;
    private String supervisionContent;
    private String pushStatus;
    private LocalDateTime pushTime;
    private String status;
    private LocalDateTime completeTime;
    private String remark;
}
