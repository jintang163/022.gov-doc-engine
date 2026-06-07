package com.gov.doc.engine.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class DocSignatureVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long documentId;
    private String documentTitle;
    private Integer documentVersion;
    private Long sealId;
    private String sealName;
    private String sealType;
    private String signatureType;
    private String signatureTypeName;
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
    private String verifyStatusName;
    private LocalDateTime verifyTime;
    private Integer verifyCount;
    private Integer isValid;
    private String isValidName;
    private String revokeReason;
    private LocalDateTime revokeTime;
    private String revokeBy;
    private String remark;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
}
