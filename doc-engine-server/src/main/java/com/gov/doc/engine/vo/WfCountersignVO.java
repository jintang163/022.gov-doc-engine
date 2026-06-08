package com.gov.doc.engine.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class WfCountersignVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String countersignNo;
    private Long processInstanceId;
    private Long processDefId;
    private String nodeId;
    private String nodeName;
    private Long taskId;
    private String businessKey;
    private String businessType;
    private String businessTitle;
    private String countersignType;
    private String countersignTypeName;
    private String voteType;
    private String voteTypeName;
    private Integer passPercentage;
    private List<Map<String, Object>> signOrder;
    private String status;
    private String statusName;
    private Integer totalCount;
    private Integer signedCount;
    private Integer passedCount;
    private Integer rejectedCount;
    private Integer abstainedCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration;
    private String durationText;
    private Map<String, Object> variables;
    private String remark;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;

    private List<WfCountersignItemVO> items;
    private Double passRate;
    private Boolean canSign;
    private Long currentSignItemId;
}
