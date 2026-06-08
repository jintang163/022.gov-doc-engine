package com.gov.doc.engine.enums;

import lombok.Getter;

@Getter
public enum WfOperationTypeEnum {

    ARRIVE("arrive", "到达"),
    COMPLETE("complete", "完成"),
    DELEGATE("delegate", "转办"),
    ADD_SIGN("addSign", "加签"),
    TERMINATE("terminate", "终止");

    private final String code;
    private final String name;

    WfOperationTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (WfOperationTypeEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value.getName();
            }
        }
        return code;
    }
}
