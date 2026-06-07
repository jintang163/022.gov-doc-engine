package com.gov.doc.engine.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class DocDocumentValidationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "模板ID不能为空")
    private Long templateId;

    private String docTitle;
    private String docContent;
    private DocDocumentCreateDTO documentData;
}
