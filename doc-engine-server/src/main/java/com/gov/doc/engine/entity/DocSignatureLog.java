package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("doc_signature_log")
public class DocSignatureLog extends BaseEntity {

    private String operationType;
    private Long sealId;
    private String sealName;
    private Long documentId;
    private String documentTitle;
    private Long signatureId;
    private Long grantId;
    private String operationDetail;
    private String ipAddress;
    private String userAgent;
    private Integer status;
    private String errorMessage;
    private LocalDateTime operateTime;
    private String operatorId;
    private String operatorName;
    private String operatorDeptId;
    private String operatorDeptName;
    private String remark;
}
