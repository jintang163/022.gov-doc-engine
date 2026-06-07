package com.gov.doc.engine.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class DocPlaceholderResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String content;
    private List<String> foundPlaceholders;
    private List<String> replacedPlaceholders;
    private List<String> missingPlaceholders;
    private Map<String, String> replacementMap;
}
