package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("doc_signature")
public class DocSignature extends BaseEntity {

    private Long documentId;
    private String documentTitle;
    private Integer documentVersion;
    private Long sealId;
    private String sealName;
    private String sealType;
    private String signatureType;
    private Integer pageNumber;
    private Integer totalPages;
    private Integer positionX;
    private Integer positionY;
    private Integer sealWidth;
    private Integer sealHeight;
    private String signatureReason;
    private String signatureLocation;
    private String signedFilePath;
    private String signedFileName;
    private String fileHash;
    private String signatureValue;
    private String certificateSerial;
    private LocalDateTime signTime;
    private String signerId;
    private String signerName;
    private String signerDeptId;
    private String signerDeptName;
    private String algorithm;
    private Integer verifyStatus;
    private LocalDateTime verifyTime;
    private Integer verifyCount;
    private Integer isValid;
    private String revokeReason;
    private LocalDateTime revokeTime;
    private String revokeBy;
    private String remark;
}
