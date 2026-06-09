package com.gov.doc.engine.enums;

import lombok.Getter;

@Getter
public enum DocBorrowStatusEnum {

    PENDING("pending", "待审批"),
    APPROVED("approved", "已批准"),
    REJECTED("rejected", "已驳回"),
    ACTIVE("active", "借阅中"),
    RETURNED("returned", "已归还"),
    OVERDUE("overdue", "已逾期");

    private final String code;
    private final String name;

    DocBorrowStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (DocBorrowStatusEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value.getName();
            }
        }
        return code;
    }
}
