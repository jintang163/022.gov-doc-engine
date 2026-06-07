package com.gov.doc.engine.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class DocSignatureVerifyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "签章记录ID不能为空")
    private Long signatureId;

    private String remark;
}
