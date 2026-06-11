package com.gov.doc.engine.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocUrgeLogVO {

    private Long id;
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
    private String statusName;
    private LocalDateTime acknowledgeTime;
    private String remark;
    private String createBy;
    private String createTime;
}
