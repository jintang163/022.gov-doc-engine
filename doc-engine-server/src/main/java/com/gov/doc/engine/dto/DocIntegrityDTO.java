package com.gov.doc.engine.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class DocIntegrityDTO {

    @NotNull(message = "公文ID不能为空")
    private Long docId;

    private String contentHash;

    private String signatureHash;
}
