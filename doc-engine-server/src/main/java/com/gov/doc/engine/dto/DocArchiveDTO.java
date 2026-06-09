package com.gov.doc.engine.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class DocArchiveDTO {

    @NotNull(message = "公文ID不能为空")
    private Long docId;

    private String archiveType;

    private String archiveLocation;

    private String retentionPeriod;

    private String remark;
}
