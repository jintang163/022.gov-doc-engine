package com.gov.doc.engine.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.dto.WfProcessDefinitionQueryDTO;
import com.gov.doc.engine.dto.WfProcessDefinitionSaveDTO;
import com.gov.doc.engine.dto.WfProcessEdgeDTO;
import com.gov.doc.engine.dto.WfProcessNodeDTO;
import com.gov.doc.engine.entity.*;
import com.gov.doc.engine.enums.WfNodeTypeEnum;
import com.gov.doc.engine.mapper.*;
import com.gov.doc.engine.service.WfProcessDefinitionService;
import com.gov.doc.engine.vo.WfProcessDefinitionVO;
import com.gov.doc.engine.vo.WfProcessGraphVO;
import com.gov.doc.engine.vo.WfProcessHistoryVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class WfProcessDefinitionServiceImpl extends ServiceImpl<WfProcessDefinitionMapper, WfProcessDefinition> implements WfProcessDefinitionService {

    @Autowired
    private WfProcessNodeMapper processNodeMapper;

    @Autowired
    private WfProcessEdgeMapper processEdgeMapper;

    @Autowired
    private WfParticipantMapper participantMapper;

    @Autowired
    private WfProcessHistoryService processHistoryService;

    @Autowired
    private WfProcessInstanceMapper processInstanceMapper;

    @Override
    public PageResult<WfProcessDefinitionVO> pageList(Integer pageNum, Integer pageSize, WfProcessDefinitionQueryDTO queryDTO) {
        LambdaQueryWrapper<WfProcessDefinition> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getProcessCode())) {
            wrapper.like(WfProcessDefinition::getProcessCode, queryDTO.getProcessCode());
        }
        if (StringUtils.hasText(queryDTO.getProcessName())) {
            wrapper.like(WfProcessDefinition::getProcessName, queryDTO.getProcessName());
        }
        if (StringUtils.hasText(queryDTO.getProcessType())) {
            wrapper.eq(WfProcessDefinition::getProcessType, queryDTO.getProcessType());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(WfProcessDefinition::getStatus, queryDTO.getStatus());
        }
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like(WfProcessDefinition::getProcessName, queryDTO.getKeyword())
                    .or().like(WfProcessDefinition::getProcessCode, queryDTO.getKeyword()));
        }
        wrapper.orderByDesc(WfProcessDefinition::getCreateTime);

        Page<WfProcessDefinition> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<WfProcessDefinitionVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(page.getTotal(), voList);
    }

    @Override
    public WfProcessDefinitionVO getDetail(Long id) {
        WfProcessDefinition definition = this.getById(id);
        if (definition == null) {
            return null;
        }
        WfProcessDefinitionVO vo = convertToVO(definition);

        List<WfProcessNode> nodes = processNodeMapper.selectList(
                new LambdaQueryWrapper<WfProcessNode>()
                        .eq(WfProcessNode::getProcessDefId, id)
                        .orderByAsc(WfProcessNode::getSortOrder));
        vo.setNodes(nodes.stream().map(this::convertNodeToDTO).collect(Collectors.toList()));

        List<WfProcessEdge> edges = processEdgeMapper.selectList(
                new LambdaQueryWrapper<WfProcessEdge>()
                        .eq(WfProcessEdge::getProcessDefId, id)
                        .orderByAsc(WfProcessEdge::getSortOrder));
        vo.setEdges(edges.stream().map(this::convertEdgeToDTO).collect(Collectors.toList()));

        return vo;
    }

    @Override
    public WfProcessDefinitionVO getByCode(String processCode) {
        WfProcessDefinition definition = this.getOne(
                new LambdaQueryWrapper<WfProcessDefinition>()
                        .eq(WfProcessDefinition::getProcessCode, processCode)
                        .eq(WfProcessDefinition::getIsCurrentVersion, 1));
        return definition != null ? convertToVO(definition) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveProcessDefinition(WfProcessDefinitionSaveDTO saveDTO) {
        WfProcessDefinition definition = new WfProcessDefinition();
        BeanUtils.copyProperties(saveDTO, definition);
        definition.setStatus(0);
        definition.setVersion(1);
        definition.setIsCurrentVersion(1);
        this.save(definition);

        saveNodesAndEdges(definition.getId(), saveDTO);
        return definition.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProcessDefinition(WfProcessDefinitionSaveDTO saveDTO) {
        if (saveDTO.getId() == null) {
            throw new RuntimeException("流程定义ID不能为空");
        }
        WfProcessDefinition definition = this.getById(saveDTO.getId());
        if (definition == null) {
            throw new RuntimeException("流程定义不存在");
        }
        if (definition.getStatus() == 1) {
            WfProcessDefinition newVersion = new WfProcessDefinition();
            BeanUtils.copyProperties(saveDTO, newVersion);
            newVersion.setId(null);
            newVersion.setVersion(definition.getVersion() + 1);
            newVersion.setStatus(0);
            newVersion.setIsCurrentVersion(1);
            this.save(newVersion);

            definition.setIsCurrentVersion(0);
            this.updateById(definition);

            saveNodesAndEdges(newVersion.getId(), saveDTO);
        } else {
            BeanUtils.copyProperties(saveDTO, definition);
            this.updateById(definition);

            processNodeMapper.delete(new LambdaQueryWrapper<WfProcessNode>()
                    .eq(WfProcessNode::getProcessDefId, definition.getId()));
            processEdgeMapper.delete(new LambdaQueryWrapper<WfProcessEdge>()
                    .eq(WfProcessEdge::getProcessDefId, definition.getId()));
            participantMapper.delete(new LambdaQueryWrapper<WfParticipant>()
                    .eq(WfParticipant::getProcessDefId, definition.getId()));

            saveNodesAndEdges(definition.getId(), saveDTO);
        }
    }

    private void saveNodesAndEdges(Long processDefId, WfProcessDefinitionSaveDTO saveDTO) {
        if (saveDTO.getNodes() != null && !saveDTO.getNodes().isEmpty()) {
            for (WfProcessNodeDTO nodeDTO : saveDTO.getNodes()) {
                WfProcessNode node = new WfProcessNode();
                BeanUtils.copyProperties(nodeDTO, node);
                node.setId(null);
                node.setProcessDefId(processDefId);
                if (nodeDTO.getNodeConfig() != null) {
                    node.setNodeConfig(JSON.toJSONString(nodeDTO.getNodeConfig()));
                }
                if (nodeDTO.getFormProperties() != null) {
                    node.setFormProperties(JSON.toJSONString(nodeDTO.getFormProperties()));
                }
                processNodeMapper.insert(node);

                if (nodeDTO.getParticipants() != null && !nodeDTO.getParticipants().isEmpty()) {
                    for (com.gov.doc.engine.dto.WfParticipantDTO participantDTO : nodeDTO.getParticipants()) {
                        WfParticipant participant = new WfParticipant();
                        BeanUtils.copyProperties(participantDTO, participant);
                        participant.setId(null);
                        participant.setProcessDefId(processDefId);
                        participant.setNodeId(node.getNodeId());
                        participantMapper.insert(participant);
                    }
                }
            }
        }

        if (saveDTO.getEdges() != null && !saveDTO.getEdges().isEmpty()) {
            for (WfProcessEdgeDTO edgeDTO : saveDTO.getEdges()) {
                WfProcessEdge edge = new WfProcessEdge();
                BeanUtils.copyProperties(edgeDTO, edge);
                edge.setId(null);
                edge.setProcessDefId(processDefId);
                if (edgeDTO.getEdgePoints() != null) {
                    edge.setEdgePoints(JSON.toJSONString(edgeDTO.getEdgePoints()));
                }
                processEdgeMapper.insert(edge);
            }
        }

        if (saveDTO.getParticipants() != null && !saveDTO.getParticipants().isEmpty()) {
            for (com.gov.doc.engine.dto.WfParticipantDTO participantDTO : saveDTO.getParticipants()) {
                WfParticipant participant = new WfParticipant();
                BeanUtils.copyProperties(participantDTO, participant);
                participant.setId(null);
                participant.setProcessDefId(processDefId);
                participantMapper.insert(participant);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(Long id) {
        WfProcessDefinition definition = this.getById(id);
        if (definition == null) {
            throw new RuntimeException("流程定义不存在");
        }
        definition.setStatus(1);
        this.updateById(definition);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void suspend(Long id) {
        WfProcessDefinition definition = this.getById(id);
        if (definition == null) {
            throw new RuntimeException("流程定义不存在");
        }
        definition.setStatus(2);
        this.updateById(definition);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProcessDefinition(Long id) {
        this.removeById(id);
        processNodeMapper.delete(new LambdaQueryWrapper<WfProcessNode>()
                .eq(WfProcessNode::getProcessDefId, id));
        processEdgeMapper.delete(new LambdaQueryWrapper<WfProcessEdge>()
                .eq(WfProcessEdge::getProcessDefId, id));
        participantMapper.delete(new LambdaQueryWrapper<WfParticipant>()
                .eq(WfParticipant::getProcessDefId, id));
    }

    @Override
    public List<WfProcessDefinitionVO> listAllPublished() {
        List<WfProcessDefinition> list = this.list(
                new LambdaQueryWrapper<WfProcessDefinition>()
                        .eq(WfProcessDefinition::getStatus, 1)
                        .eq(WfProcessDefinition::getIsCurrentVersion, 1)
                        .orderByDesc(WfProcessDefinition::getCreateTime));
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public WfProcessGraphVO getProcessGraph(Long processDefId, Long processInstanceId) {
        WfProcessGraphVO graphVO = new WfProcessGraphVO();
        graphVO.setProcessDefId(processDefId);
        graphVO.setProcessInstanceId(processInstanceId);

        List<WfProcessNode> nodes = processNodeMapper.selectList(
                new LambdaQueryWrapper<WfProcessNode>()
                        .eq(WfProcessNode::getProcessDefId, processDefId)
                        .orderByAsc(WfProcessNode::getSortOrder));

        List<WfProcessEdge> edges = processEdgeMapper.selectList(
                new LambdaQueryWrapper<WfProcessEdge>()
                        .eq(WfProcessEdge::getProcessDefId, processDefId)
                        .orderByAsc(WfProcessEdge::getSortOrder));

        Map<String, List<WfProcessHistoryVO>> nodeHistoryMap = new HashMap<>();
        String currentNodeId = null;
        if (processInstanceId != null) {
            WfProcessInstance instance = processInstanceMapper.selectById(processInstanceId);
            if (instance != null) {
                currentNodeId = instance.getCurrentNodeId();
                List<WfProcessHistoryVO> historyList = processHistoryService.getByProcessInstanceId(processInstanceId);
                for (WfProcessHistoryVO history : historyList) {
                    nodeHistoryMap.computeIfAbsent(history.getNodeId(), k -> new ArrayList<>())
                            .add(history);
                }
            }
        }

        List<WfProcessGraphVO.Node> nodeList = new ArrayList<>();
        for (WfProcessNode node : nodes) {
            WfProcessGraphVO.Node graphNode = new WfProcessGraphVO.Node();
            graphNode.setId(node.getNodeId());
            graphNode.setName(node.getNodeName());
            graphNode.setType(node.getNodeType());
            graphNode.setX(node.getX());
            graphNode.setY(node.getY());
            graphNode.setWidth(node.getWidth());
            graphNode.setHeight(node.getHeight());

            String status = "idle";
            if (processInstanceId != null) {
                if (node.getNodeId().equals(currentNodeId)) {
                    status = "active";
                } else if (nodeHistoryMap.containsKey(node.getNodeId())) {
                    status = "completed";
                }
            }
            graphNode.setStatus(status);
            graphNode.setStatusName(getStatusName(status));

            if (nodeHistoryMap.containsKey(node.getNodeId())) {
                List<Map<String, Object>> historyData = new ArrayList<>();
                for (WfProcessHistoryVO history : nodeHistoryMap.get(node.getNodeId())) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("operatorName", history.getOperatorName());
                    item.put("operationType", history.getOperationType());
                    item.put("operationTypeName", history.getOperationTypeName());
                    item.put("enterTime", history.getEnterTime());
                    item.put("leaveTime", history.getLeaveTime());
                    item.put("durationText", history.getDurationText());
                    item.put("opinion", history.getOpinion());
                    historyData.add(item);
                }
                graphNode.setHistory(historyData);
            }

            nodeList.add(graphNode);
        }
        graphVO.setNodes(nodeList);

        List<WfProcessGraphVO.Edge> edgeList = new ArrayList<>();
        for (WfProcessEdge edge : edges) {
            WfProcessGraphVO.Edge graphEdge = new WfProcessGraphVO.Edge();
            graphEdge.setId(edge.getEdgeId());
            graphEdge.setName(edge.getEdgeName());
            graphEdge.setSource(edge.getSourceNodeId());
            graphEdge.setTarget(edge.getTargetNodeId());
            graphEdge.setConditionLabel(edge.getConditionLabel());
            if (StringUtils.hasText(edge.getEdgePoints())) {
                graphEdge.setPoints(JSON.parseArray(edge.getEdgePoints(), Map.class));
            }
            edgeList.add(graphEdge);
        }
        graphVO.setEdges(edgeList);

        return graphVO;
    }

    private String getStatusName(String status) {
        switch (status) {
            case "active":
                return "进行中";
            case "completed":
                return "已完成";
            case "idle":
            default:
                return "未开始";
        }
    }

    @Override
    public void deployProcessDefinition(Long id) {
        publish(id);
    }

    private WfProcessDefinitionVO convertToVO(WfProcessDefinition definition) {
        WfProcessDefinitionVO vo = new WfProcessDefinitionVO();
        BeanUtils.copyProperties(definition, vo);

        vo.setProcessTypeName(definition.getProcessType().equals("approval") ? "审批流程" : "会签流程");
        vo.setStatusName(definition.getStatus() == 0 ? "草稿" : definition.getStatus() == 1 ? "已发布" : "已停用");

        return vo;
    }

    private WfProcessNodeDTO convertNodeToDTO(WfProcessNode node) {
        WfProcessNodeDTO dto = new WfProcessNodeDTO();
        BeanUtils.copyProperties(node, dto);
        if (StringUtils.hasText(node.getNodeConfig())) {
            dto.setNodeConfig(JSON.parseObject(node.getNodeConfig(), Map.class));
        }
        if (StringUtils.hasText(node.getFormProperties())) {
            dto.setFormProperties(JSON.parseArray(node.getFormProperties(), Map.class));
        }
        return dto;
    }

    private WfProcessEdgeDTO convertEdgeToDTO(WfProcessEdge edge) {
        WfProcessEdgeDTO dto = new WfProcessEdgeDTO();
        BeanUtils.copyProperties(edge, dto);
        if (StringUtils.hasText(edge.getEdgePoints())) {
            dto.setEdgePoints(JSON.parseArray(edge.getEdgePoints(), Map.class));
        }
        return dto;
    }
}
