package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("doc_seal")
public class DocSeal extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String sealName;

    private String sealType;

    private String sealCode;

    private String ownerUnitId;

    private String ownerUnitName;

    private String ownerDeptId;

    private String ownerDeptName;

    private String ownerUserId;

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

    private String password;

    private String remark;
}
