package com.gov.doc.engine.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class DocSignatureLogQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String operationType;
    private Long sealId;
    private Long documentId;
    private Long signatureId;
    private String operatorId;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大为100")
    private Integer pageSize = 10;
}
