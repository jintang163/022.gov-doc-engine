package com.gov.doc.engine.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class DocSealVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String sealName;
    private String sealType;
    private String sealTypeName;
    private String sealCode;
    private Long ownerUnitId;
    private String ownerUnitName;
    private Long ownerDeptId;
    private String ownerDeptName;
    private Long ownerUserId;
    private String ownerUserName;
    private String sealImagePath;
    private String sealImageName;
    private Integer sealWidth;
    private Integer sealHeight;
    private String certificateSerial;
    private String certificateSubject;
    private String certificateIssuer;
    private LocalDateTime certificateValidFrom;
    private LocalDateTime certificateValidTo;
    private String privateKeyPath;
    private String algorithm;
    private Integer status;
    private String statusName;
    private String password;
    private String remark;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
}
