package com.gov.doc.engine.enums;

import lombok.Getter;

@Getter
public enum WfParticipantTypeEnum {

    USER("user", "用户"),
    POST("post", "岗位"),
    DEPT("dept", "部门"),
    ROLE("role", "角色"),
    EXPRESSION("expression", "表达式");

    private final String code;
    private final String name;

    WfParticipantTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (WfParticipantTypeEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value.getName();
            }
        }
        return code;
    }
}
