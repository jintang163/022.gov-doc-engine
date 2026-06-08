package com.gov.doc.engine.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class WfProcessEdgeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String edgeId;
    private String edgeName;
    private String sourceNodeId;
    private String targetNodeId;
    private String conditionExpression;
    private String conditionLabel;
    private List<Map<String, Integer>> edgePoints;
    private Integer sortOrder;
    private String remark;
}
