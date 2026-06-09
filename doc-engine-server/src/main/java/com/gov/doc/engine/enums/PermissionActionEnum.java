package com.gov.doc.engine.enums;

import lombok.Getter;

@Getter
public enum PermissionActionEnum {

    DRAFT("draft", "拟稿"),
    MODIFY("modify", "修改"),
    DELETE("delete", "删除"),
    SIGN("sign", "签章"),
    APPROVE("approve", "审批"),
    VIEW("view", "查看"),
    ARCHIVE("archive", "归档"),
    EXPORT("export", "导出"),
    PRINT("print", "打印");

    private final String code;
    private final String name;

    PermissionActionEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (PermissionActionEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value.getName();
            }
        }
        return code;
    }
}
