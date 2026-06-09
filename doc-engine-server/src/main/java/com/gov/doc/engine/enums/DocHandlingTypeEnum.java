package com.gov.doc.engine.enums;

import lombok.Getter;

@Getter
public enum DocHandlingTypeEnum {

    DRAFT_OPINION("draft_opinion", "拟办意见"),
    ASSIGN("assign", "转承办"),
    FEEDBACK("feedback", "反馈"),
    SIGN_RECEIPT("sign_receipt", "签收确认");

    private final String code;
    private final String name;

    DocHandlingTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (DocHandlingTypeEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value.getName();
            }
        }
        return code;
    }
}
