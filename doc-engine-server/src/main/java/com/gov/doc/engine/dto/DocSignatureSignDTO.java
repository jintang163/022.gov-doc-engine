package com.gov.doc.engine.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class DocSignatureSignDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "公文ID不能为空")
    private Long documentId;

    @NotNull(message = "印章ID不能为空")
    private Long sealId;

    @NotBlank(message = "签章类型不能为空")
    private String signatureType;

    private Integer pageNumber;

    @NotNull(message = "签章位置X坐标不能为空")
    private Integer positionX;

    @NotNull(message = "签章位置Y坐标不能为空")
    private Integer positionY;

    private Integer sealWidth;

    private Integer sealHeight;

    private String signatureReason;

    private String signatureLocation;

    private String password;

    private String remark;
}
