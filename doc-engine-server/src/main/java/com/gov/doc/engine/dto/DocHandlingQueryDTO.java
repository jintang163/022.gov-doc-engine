package com.gov.doc.engine.dto;

import lombok.Data;

@Data
public class DocHandlingQueryDTO {

    private String keyword;
    private Long incomingId;
    private String handlingType;
    private String handlerId;
    private String handlerDeptId;
    private String targetDeptId;
    private String status;
    private Integer pageNum;
    private Integer pageSize;
}
