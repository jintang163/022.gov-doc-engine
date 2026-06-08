package com.gov.doc.engine.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class WfTaskCountVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer todoCount;
    private Integer doneCount;
    private Integer countersignCount;
    private Integer urgentCount;
    private Integer totalCount;
}
