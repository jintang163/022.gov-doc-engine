package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("doc_status_log")
public class DocStatusLog extends BaseEntity {

    private Long docId;
    private String fromStatus;
    private String fromStatusName;
    private String toStatus;
    private String toStatusName;
    private String transitionReason;
    private String operatorId;
    private String operatorName;
    private LocalDateTime operationTime;
    private String remark;
}
