package com.gov.doc.engine.enums;

import lombok.Getter;

@Getter
public enum WfApprovalTypeEnum {

    PASS("pass", "通过"),
    REJECT("reject", "驳回"),
    RETURN("return", "退回修改"),
    TERMINATE("terminate", "终止"),
    DELEGATE("delegate", "转办"),
    ADD_SIGN("addSign", "加签"),
    COUNTERSIGN("countersign", "会签");

    private final String code;
    private final String name;

    WfApprovalTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (WfApprovalTypeEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value.getName();
            }
        }
        return code;
    }
}
