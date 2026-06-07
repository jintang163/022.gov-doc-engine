package com.gov.doc.engine.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class DocSealCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "印章名称不能为空")
    private String sealName;

    @NotBlank(message = "印章类型不能为空")
    private String sealType;

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

    private String password;

    private String remark;
}
