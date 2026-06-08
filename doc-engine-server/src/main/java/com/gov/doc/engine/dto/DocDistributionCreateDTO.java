package com.gov.doc.engine.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DocDistributionCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long docId;
    private String distributionType;
    private List<DistributionUnitDTO> mainSendUnits;
    private List<DistributionUnitDTO> copySendUnits;
    private Integer printCount;
    private String remark;

    @Data
    public static class DistributionUnitDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private String unitId;
        private String unitName;
        private String unitCode;
        private String contactPerson;
        private String contactPhone;
    }
}
