package com.gov.doc.engine.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.doc.engine.common.UserContext;
import com.gov.doc.engine.dto.WfCountersignSignDTO;
import com.gov.doc.engine.entity.*;
import com.gov.doc.engine.enums.WfCountersignStatusEnum;
import com.gov.doc.engine.enums.WfCountersignTypeEnum;
import com.gov.doc.engine.enums.WfSignResultEnum;
import com.gov.doc.engine.enums.WfVoteTypeEnum;
import com.gov.doc.engine.mapper.WfCountersignItemMapper;
import com.gov.doc.engine.mapper.WfCountersignMapper;
import com.gov.doc.engine.mapper.WfProcessInstanceMapper;
import com.gov.doc.engine.mapper.WfProcessNodeMapper;
import com.gov.doc.engine.service.WfCountersignService;
import com.gov.doc.engine.service.WfProcessEngineService;
import com.gov.doc.engine.vo.WfCountersignItemVO;
import com.gov.doc.engine.vo.WfCountersignVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WfCountersignServiceImpl extends ServiceImpl<WfCountersignMapper, WfCountersign> implements WfCountersignService {

    @Autowired
    private WfCountersignItemMapper countersignItemMapper;

    @Autowired
    private WfProcessEngineService processEngineService;

    @Autowired
    private WfProcessInstanceMapper processInstanceMapper;

    @Autowired
    private WfProcessNodeMapper processNodeMapper;

    @Override
    public WfCountersignVO getDetail(Long id) {
        WfCountersign countersign = this.getById(id);
        if (countersign == null) {
            return null;
        }
        WfCountersignVO vo = convertToVO(countersign);

        List<WfCountersignItemVO> items = getCountersignItems(id);
        vo.setItems(items);

        UserContext currentUser = UserContext.getCurrentUser();
        WfCountersignItem pendingItem = getPendingItemForUser(id, String.valueOf(currentUser.getUserId()));
        vo.setCanSign(pendingItem != null);
        if (pendingItem != null) {
            vo.setCurrentSignItemId(pendingItem.getId());
        }

        return vo;
    }

    @Override
    public WfCountersignVO getByProcessInstanceId(Long processInstanceId) {
        WfCountersign countersign = this.getOne(
                new LambdaQueryWrapper<WfCountersign>()
                        .eq(WfCountersign::getProcessInstanceId, processInstanceId)
                        .orderByDesc(WfCountersign::getCreateTime)
                        .last("LIMIT 1"));
        return countersign != null ? convertToVO(countersign) : null;
    }

    @Override
    public List<WfCountersignItemVO> getCountersignItems(Long countersignId) {
        List<WfCountersignItem> items = countersignItemMapper.selectList(
                new LambdaQueryWrapper<WfCountersignItem>()
                        .eq(WfCountersignItem::getCountersignId, countersignId)
                        .orderByAsc(WfCountersignItem::getSignOrder));
        return items.stream().map(this::convertItemToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void signCountersign(WfCountersignSignDTO signDTO) {
        WfCountersignItem item = countersignItemMapper.selectById(signDTO.getCountersignItemId());
        if (item == null) {
            throw new RuntimeException("会签项不存在");
        }
        if (!"pending".equals(item.getStatus())) {
            throw new RuntimeException("会签项状态不允许签署");
        }

        UserContext currentUser = UserContext.getCurrentUser();
        if (!item.getSignUserId().equals(String.valueOf(currentUser.getUserId()))) {
            throw new RuntimeException("您无权签署此会签项");
        }

        item.setSignResult(signDTO.getSignResult());
        item.setSignOpinion(signDTO.getSignOpinion());
        if (signDTO.getVariables() != null && !signDTO.getVariables().isEmpty()) {
            item.setVariables(JSON.toJSONString(signDTO.getVariables()));
        }
        item.setRemark(signDTO.getRemark());

        completeCountersignItem(item);

        WfCountersign countersign = this.getById(item.getCountersignId());
        updateCountersignStats(countersign, item);

        checkAndCompleteCountersign(countersign.getId());

        if (WfCountersignTypeEnum.SEQUENTIAL.getCode().equals(countersign.getCountersignType())) {
            activateNextSequentialItem(countersign.getId(), item.getSignOrder());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startCountersign(WfCountersign countersign) {
        countersign.setStatus(WfCountersignStatusEnum.SIGNING.getCode());
        countersign.setStartTime(LocalDateTime.now());
        this.updateById(countersign);

        List<WfCountersignItem> items = countersignItemMapper.selectList(
                new LambdaQueryWrapper<WfCountersignItem>()
                        .eq(WfCountersignItem::getCountersignId, countersign.getId())
                        .orderByAsc(WfCountersignItem::getSignOrder));

        if (WfCountersignTypeEnum.PARALLEL.getCode().equals(countersign.getCountersignType())) {
            for (WfCountersignItem item : items) {
                item.setStatus("pending");
                countersignItemMapper.updateById(item);
            }
        } else if (WfCountersignTypeEnum.SEQUENTIAL.getCode().equals(countersign.getCountersignType())) {
            if (!items.isEmpty()) {
                WfCountersignItem firstItem = items.get(0);
                firstItem.setStatus("pending");
                countersignItemMapper.updateById(firstItem);
            }
        }
    }

    @Override
    public void completeCountersignItem(WfCountersignItem item) {
        item.setStatus("completed");
        item.setSignTime(LocalDateTime.now());
        if (item.getCreateTime() != null && item.getSignTime() != null) {
            item.setDuration(Duration.between(item.getCreateTime(), item.getSignTime()).getSeconds());
        }
        countersignItemMapper.updateById(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkAndCompleteCountersign(Long countersignId) {
        WfCountersign countersign = this.getById(countersignId);
        if (countersign == null) {
            return;
        }
        if (!WfCountersignStatusEnum.SIGNING.getCode().equals(countersign.getStatus())) {
            return;
        }

        List<WfCountersignItem> items = countersignItemMapper.selectList(
                new LambdaQueryWrapper<WfCountersignItem>()
                        .eq(WfCountersignItem::getCountersignId, countersignId));

        long completedCount = items.stream().filter(item -> "completed".equals(item.getStatus())).count();
        boolean allCompleted = completedCount == items.size();

        String voteType = countersign.getVoteType();
        boolean isPassed = false;

        if (WfVoteTypeEnum.ONE_PASS.getCode().equals(voteType)) {
            isPassed = items.stream().anyMatch(item -> WfSignResultEnum.AGREE.getCode().equals(item.getSignResult()));
            if (isPassed) {
                allCompleted = true;
            }
        } else if (WfVoteTypeEnum.ALL_PASS.getCode().equals(voteType)) {
            if (allCompleted) {
                isPassed = items.stream().allMatch(item -> WfSignResultEnum.AGREE.getCode().equals(item.getSignResult()));
            }
        } else if (WfVoteTypeEnum.ONE_REJECT.getCode().equals(voteType)) {
            boolean hasReject = items.stream().anyMatch(item -> WfSignResultEnum.REJECT.getCode().equals(item.getSignResult()));
            if (hasReject) {
                isPassed = false;
                allCompleted = true;
            } else if (allCompleted) {
                isPassed = true;
            }
        } else if (WfVoteTypeEnum.PERCENTAGE.getCode().equals(voteType)) {
            if (allCompleted) {
                long agreeCount = items.stream().filter(item -> WfSignResultEnum.AGREE.getCode().equals(item.getSignResult())).count();
                double passRate = (double) agreeCount / items.size() * 100;
                isPassed = passRate >= (countersign.getPassPercentage() != null ? countersign.getPassPercentage() : 50);
            }
        }

        if (allCompleted) {
            countersign.setStatus(isPassed ? WfCountersignStatusEnum.COMPLETED.getCode() : WfCountersignStatusEnum.REJECTED.getCode());
            countersign.setEndTime(LocalDateTime.now());
            if (countersign.getStartTime() != null && countersign.getEndTime() != null) {
                countersign.setDuration(Duration.between(countersign.getStartTime(), countersign.getEndTime()).getSeconds());
            }
            this.updateById(countersign);

            WfProcessInstance instance = processInstanceMapper.selectById(countersign.getProcessInstanceId());
            WfProcessNode currentNode = processNodeMapper.selectOne(
                    new LambdaQueryWrapper<WfProcessNode>()
                            .eq(WfProcessNode::getProcessDefId, countersign.getProcessDefId())
                            .eq(WfProcessNode::getNodeId, countersign.getNodeId()));
            if (instance != null && currentNode != null) {
                Map<String, Object> variables = StringUtils.hasText(countersign.getVariables())
                        ? JSON.parseObject(countersign.getVariables(), Map.class)
                        : new java.util.HashMap<>();
                variables.put("countersignResult", isPassed ? "pass" : "reject");
                processEngineService.leaveNode(instance, currentNode, variables);
            }
        }
    }

    @Override
    public WfCountersignVO convertToVO(WfCountersign countersign) {
        WfCountersignVO vo = new WfCountersignVO();
        BeanUtils.copyProperties(countersign, vo);

        vo.setCountersignTypeName(WfCountersignTypeEnum.getNameByCode(countersign.getCountersignType()));
        vo.setVoteTypeName(WfVoteTypeEnum.getNameByCode(countersign.getVoteType()));
        vo.setStatusName(WfCountersignStatusEnum.getNameByCode(countersign.getStatus()));

        if (countersign.getTotalCount() != null && countersign.getTotalCount() > 0) {
            double passRate = countersign.getPassedCount() != null
                    ? (double) countersign.getPassedCount() / countersign.getTotalCount() * 100
                    : 0;
            vo.setPassRate(Math.round(passRate * 100) / 100.0);
        } else {
            vo.setPassRate(0.0);
        }

        if (StringUtils.hasText(countersign.getSignOrder())) {
            vo.setSignOrder(JSON.parseArray(countersign.getSignOrder(), Map.class));
        }
        if (StringUtils.hasText(countersign.getVariables())) {
            vo.setVariables(JSON.parseObject(countersign.getVariables(), Map.class));
        }
        if (countersign.getDuration() != null) {
            vo.setDurationText(formatDuration(countersign.getDuration()));
        }

        return vo;
    }

    @Override
    public WfCountersignItemVO convertItemToVO(WfCountersignItem item) {
        WfCountersignItemVO vo = new WfCountersignItemVO();
        BeanUtils.copyProperties(item, vo);

        vo.setStatusName(getItemStatusName(item.getStatus()));
        if (StringUtils.hasText(item.getSignResult())) {
            vo.setSignResultName(WfSignResultEnum.getNameByCode(item.getSignResult()));
        }
        if (item.getDuration() != null) {
            vo.setDurationText(formatDuration(item.getDuration()));
        }
        if (StringUtils.hasText(item.getVariables())) {
            vo.setVariables(JSON.parseObject(item.getVariables(), Map.class));
        }

        return vo;
    }

    @Override
    public WfCountersignItem getPendingItemForUser(Long countersignId, String userId) {
        return countersignItemMapper.selectOne(
                new LambdaQueryWrapper<WfCountersignItem>()
                        .eq(WfCountersignItem::getCountersignId, countersignId)
                        .eq(WfCountersignItem::getSignUserId, userId)
                        .eq(WfCountersignItem::getStatus, "pending"));
    }

    private void updateCountersignStats(WfCountersign countersign, WfCountersignItem item) {
        countersign.setSignedCount(countersign.getSignedCount() != null ? countersign.getSignedCount() + 1 : 1);

        if (WfSignResultEnum.AGREE.getCode().equals(item.getSignResult())) {
            countersign.setPassedCount(countersign.getPassedCount() != null ? countersign.getPassedCount() + 1 : 1);
        } else if (WfSignResultEnum.REJECT.getCode().equals(item.getSignResult())) {
            countersign.setRejectedCount(countersign.getRejectedCount() != null ? countersign.getRejectedCount() + 1 : 1);
        } else if (WfSignResultEnum.ABSTAIN.getCode().equals(item.getSignResult())) {
            countersign.setAbstainedCount(countersign.getAbstainedCount() != null ? countersign.getAbstainedCount() + 1 : 1);
        }

        this.updateById(countersign);
    }

    private void activateNextSequentialItem(Long countersignId, Integer currentOrder) {
        List<WfCountersignItem> items = countersignItemMapper.selectList(
                new LambdaQueryWrapper<WfCountersignItem>()
                        .eq(WfCountersignItem::getCountersignId, countersignId)
                        .orderByAsc(WfCountersignItem::getSignOrder));

        WfCountersignItem nextItem = items.stream()
                .filter(item -> item.getSignOrder() != null && item.getSignOrder() > currentOrder)
                .filter(item -> "inactive".equals(item.getStatus()))
                .min(Comparator.comparing(WfCountersignItem::getSignOrder))
                .orElse(null);

        if (nextItem != null) {
            nextItem.setStatus("pending");
            countersignItemMapper.updateById(nextItem);
        }
    }

    private String getItemStatusName(String status) {
        switch (status) {
            case "pending":
                return "待签署";
            case "completed":
                return "已签署";
            case "inactive":
            default:
                return "未激活";
        }
    }

    private String formatDuration(Long seconds) {
        if (seconds == null || seconds <= 0) {
            return "0秒";
        }
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("天");
        }
        if (hours > 0) {
            sb.append(hours).append("小时");
        }
        if (minutes > 0) {
            sb.append(minutes).append("分钟");
        }
        if (secs > 0 || sb.length() == 0) {
            sb.append(secs).append("秒");
        }
        return sb.toString();
    }
}
