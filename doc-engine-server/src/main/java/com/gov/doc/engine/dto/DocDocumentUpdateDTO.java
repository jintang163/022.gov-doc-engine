package com.gov.doc.engine.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Map;

@Data
public class DocDocumentUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "公文ID不能为空")
    private Long id;

    private String docTitle;
    private String docNumber;
    private String docType;
    private String securityLevel;
    private String urgencyLevel;
    private String mainSendDept;
    private String copySendDept;
    private String signer;
    private LocalDate signDate;
    private LocalDate writtenDate;
    private String docContent;
    private String attachmentInfo;
    private Map<String, Object> fieldData;
    private String status;
    private String remark;
}
