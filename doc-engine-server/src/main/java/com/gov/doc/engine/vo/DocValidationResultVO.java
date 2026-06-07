package com.gov.doc.engine.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class DocValidationResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean valid;
    private List<String> errors;
    private List<String> warnings;
}
