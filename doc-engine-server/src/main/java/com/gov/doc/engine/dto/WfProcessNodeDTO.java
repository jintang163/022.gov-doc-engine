package com.gov.doc.engine.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class WfProcessNodeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String nodeId;
    private String nodeName;
    private String nodeType;
    private Map<String, Object> nodeConfig;
    private List<Map<String, Object>> formProperties;
    private Integer x;
    private Integer y;
    private Integer width;
    private Integer height;
    private Integer sortOrder;
    private String remark;

    private List<WfParticipantDTO> participants;
}
