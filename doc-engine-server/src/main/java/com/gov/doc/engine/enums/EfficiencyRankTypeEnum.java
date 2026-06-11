package com.gov.doc.engine.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EfficiencyRankTypeEnum {

    DEPARTMENT("dept", "部门"),
    PERSON("person", "个人");

    private final String code;
    private final String desc;

    public static EfficiencyRankTypeEnum of(String code) {
        for (EfficiencyRankTypeEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
