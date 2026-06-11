package com.gov.doc.engine.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DocSupervisionVO {

    private Long id;
    private String supervisionNo;
    private Long incomingId;
    private Long handlingId;
    private String sourceDocNumber;
    private String docTitle;
    private String supervisionType;
    private String supervisionTypeName;
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
    private String pushStatusName;
    private LocalDateTime pushTime;
    private String status;
    private String statusName;
    private LocalDateTime completeTime;
    private String remark;
    private String createBy;
    private String createTime;
    private String updateBy;
    private String updateTime;
    private DocIncomingVO incoming;
    private List<DocUrgeLogVO> urgeLogs;
}
