package com.gov.doc.engine.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class DocSignatureVerifyResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long signatureId;
    private Boolean valid;
    private Integer verifyStatus;
    private String verifyStatusName;
    private String message;
    private LocalDateTime verifyTime;
    private String signerName;
    private String signerDeptName;
    private LocalDateTime signTime;
    private String certificateSerial;
    private String algorithm;
    private String fileHash;
}
