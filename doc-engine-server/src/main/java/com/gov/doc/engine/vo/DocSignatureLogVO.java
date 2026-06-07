package com.gov.doc.engine.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class DocSignatureLogVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String operationType;
    private String operationTypeName;
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
    private String statusName;
    private String errorMessage;
    private LocalDateTime operateTime;
    private String operatorId;
    private String operatorName;
    private String operatorDeptId;
    private String operatorDeptName;
    private String remark;
    private LocalDateTime createTime;
}
