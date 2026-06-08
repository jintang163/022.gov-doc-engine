package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("doc_distribution")
public class DocDistribution extends BaseEntity {

    private Long docId;
    private String distributionNo;
    private String distributionType;
    private String distributionTypeName;
    private String mainSendUnits;
    private String copySendUnits;
    private String printCount;
    private String delivererId;
    private String delivererName;
    private LocalDateTime distributeTime;
    private String receiverId;
    private String receiverName;
    private LocalDateTime receiveTime;
    private String status;
    private String statusName;
    private String remark;
}
