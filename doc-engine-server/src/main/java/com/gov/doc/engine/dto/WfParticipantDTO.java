package com.gov.doc.engine.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class WfParticipantDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String nodeId;
    private String participantType;
    private String participantValue;
    private String participantName;
    private String assignmentType;
    private Integer sortOrder;
    private String remark;
}
