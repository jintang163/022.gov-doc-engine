package com.gov.doc.engine.vo;

import lombok.Data;

@Data
public class DocIntegrityVO {

    private Long id;
    private Long docId;
    private String contentHash;
    private String signatureHash;
    private String hashAlgorithm;
    private Integer version;
    private String verifyStatus;
    private String verifyStatusName;
    private String verifyTime;
    private String verifiedBy;
    private String verifiedByName;
    private String remark;
    private String createBy;
    private String createTime;
    private String updateBy;
    private String updateTime;
}
