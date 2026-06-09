package com.gov.doc.engine.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class DocIncomingDTO {

    @NotBlank(message = "公文标题不能为空")
    private String docTitle;

    private String source;

    private String sourceUnit;

    private String sourceDocNumber;

    private String docType;

    private String securityLevel;

    private String urgencyLevel;

    @NotNull(message = "收文日期不能为空")
    private LocalDate receivedDate;

    private String receivedMethod;

    private Integer copies;

    private Integer pages;

    private String docContent;

    private String attachmentInfo;

    private String keyword;

    private String abstractText;

    private String remark;
}
