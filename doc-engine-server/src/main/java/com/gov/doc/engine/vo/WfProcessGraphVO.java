package com.gov.doc.engine.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class WfProcessGraphVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long processDefId;
    private Long processInstanceId;
    private List<Node> nodes;
    private List<Edge> edges;

    @Data
    public static class Node implements Serializable {
        private String id;
        private String name;
        private String type;
        private Integer x;
        private Integer y;
        private Integer width;
        private Integer height;
        private String status;
        private String statusName;
        private Map<String, Object> data;
        private List<Map<String, Object>> history;
    }

    @Data
    public static class Edge implements Serializable {
        private String id;
        private String name;
        private String source;
        private String target;
        private String conditionLabel;
        private List<Map<String, Integer>> points;
        private String status;
        private String statusName;
    }
}
