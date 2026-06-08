package com.gov.doc.engine.enums;

import lombok.Getter;

@Getter
public enum WfSignResultEnum {

    AGREE("agree", "同意"),
    REJECT("reject", "反对"),
    ABSTAIN("abstain", "弃权");

    private final String code;
    private final String name;

    WfSignResultEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (WfSignResultEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value.getName();
            }
        }
        return code;
    }
}
