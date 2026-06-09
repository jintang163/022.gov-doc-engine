package com.gov.doc.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.dto.DocDistributionCreateDTO;
import com.gov.doc.engine.entity.DocDistribution;
import com.gov.doc.engine.entity.DocDocument;
import com.gov.doc.engine.enums.DocDistributionTypeEnum;
import com.gov.doc.engine.enums.DocStatusEnum;
import com.gov.doc.engine.mapper.DocDistributionMapper;
import com.gov.doc.engine.mapper.DocDocumentMapper;
import com.gov.doc.engine.service.DocDistributionService;
import com.gov.doc.engine.service.DocStatusMachineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class DocDistributionServiceImpl extends ServiceImpl<DocDistributionMapper, DocDistribution> implements DocDistributionService {

    @Autowired
    private DocDistributionMapper docDistributionMapper;

    @Autowired
    private DocDocumentMapper docDocumentMapper;

    @Autowired
    private DocStatusMachineService statusMachineService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Random RANDOM = new Random();

    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_PENDING_NAME = "待分发";
    private static final String STATUS_DISTRIBUTED = "distributed";
    private static final String STATUS_DISTRIBUTED_NAME = "已分发";
    private static final String STATUS_CONFIRMED = "confirmed";
    private static final String STATUS_CONFIRMED_NAME = "已确认";
    private static final String STATUS_PRINTED = "printed";
    private static final String STATUS_PRINTED_NAME = "已打印";

    private static final String UNIT_TYPE_MAIN = "main";
    private static final String UNIT_TYPE_COPY = "copy";

    @Override
    public PageResult<DocDistribution> pageList(Integer pageNum, Integer pageSize, Long docId, String status) {
        LambdaQueryWrapper<DocDistribution> queryWrapper = new LambdaQueryWrapper<>();
        if (docId != null) {
            queryWrapper.eq(DocDistribution::getDocId, docId);
        }
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(DocDistribution::getStatus, status);
        }
        queryWrapper.orderByDesc(DocDistribution::getCreateTime);

        Page<DocDistribution> page = new Page<>(pageNum, pageSize);
        Page<DocDistribution> resultPage = docDistributionMapper.selectPage(page, queryWrapper);

        return PageResult.of(resultPage.getTotal(), resultPage.getRecords(), pageNum, pageSize);
    }

    @Override
    public DocDistribution getDetail(Long id) {
        return docDistributionMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<DocDistribution> distribute(DocDistributionCreateDTO dto, String operatorId, String operatorName) {
        if (dto.getDocId() == null) {
            throw new IllegalArgumentException("公文ID不能为空");
        }

        DocDocument doc = docDocumentMapper.selectById(dto.getDocId());
        if (doc == null) {
            throw new IllegalArgumentException("公文不存在: " + dto.getDocId());
        }

        if (!DocStatusEnum.SIGNED.getCode().equals(doc.getStatus())) {
            throw new IllegalStateException("公文状态为[" + DocStatusEnum.getNameByCode(doc.getStatus()) + "]，不允许分发");
        }

        if (CollectionUtils.isEmpty(dto.getMainSendUnits()) && CollectionUtils.isEmpty(dto.getCopySendUnits())) {
            throw new IllegalArgumentException("主送单位和抄送单位不能同时为空");
        }

        String distributionType = dto.getDistributionType();
        if (!StringUtils.hasText(distributionType)) {
            distributionType = DocDistributionTypeEnum.ELECTRONIC.getCode();
        }

        String distributionNo = generateDistributionNo();
        LocalDateTime now = LocalDateTime.now();
        List<DocDistribution> result = new ArrayList<>();

        if (!CollectionUtils.isEmpty(dto.getMainSendUnits())) {
            for (DocDistributionCreateDTO.DistributionUnitDTO unit : dto.getMainSendUnits()) {
                DocDistribution distribution = createDistributionItem(
                        dto, distributionNo, distributionType, UNIT_TYPE_MAIN,
                        unit, now, operatorId, operatorName
                );
                docDistributionMapper.insert(distribution);
                result.add(distribution);
            }
        }

        if (!CollectionUtils.isEmpty(dto.getCopySendUnits())) {
            for (DocDistributionCreateDTO.DistributionUnitDTO unit : dto.getCopySendUnits()) {
                DocDistribution distribution = createDistributionItem(
                        dto, distributionNo, distributionType, UNIT_TYPE_COPY,
                        unit, now, operatorId, operatorName
                );
                docDistributionMapper.insert(distribution);
                result.add(distribution);
            }
        }

        statusMachineService.transitionWithReason(
                dto.getDocId(),
                DocStatusEnum.DISTRIBUTING.getCode(),
                "分发公文",
                operatorId,
                operatorName,
                "分发文号: " + distributionNo + ", 共分发 " + result.size() + " 个单位"
        );

        log.info("Document {} distributed to {} units, distributionNo: {}", dto.getDocId(), result.size(), distributionNo);
        return result;
    }

    private DocDistribution createDistributionItem(
            DocDistributionCreateDTO dto, String distributionNo, String distributionType,
            String unitType, DocDistributionCreateDTO.DistributionUnitDTO unit,
            LocalDateTime now, String operatorId, String operatorName) {

        DocDistribution distribution = new DocDistribution();
        distribution.setDocId(dto.getDocId());
        distribution.setDistributionNo(distributionNo);
        distribution.setDistributionType(distributionType);
        distribution.setDistributionTypeName(DocDistributionTypeEnum.getNameByCode(distributionType));
        distribution.setUnitType(unitType);
        distribution.setUnitId(unit.getUnitId());
        distribution.setUnitName(unit.getUnitName());
        distribution.setUnitCode(unit.getUnitCode());
        distribution.setContactPerson(unit.getContactPerson());
        distribution.setContactPhone(unit.getContactPhone());
        if (dto.getPrintCount() != null) {
            distribution.setPrintCount(dto.getPrintCount().toString());
        }
        distribution.setDelivererId(operatorId);
        distribution.setDelivererName(operatorName);
        distribution.setDistributeTime(now);
        distribution.setStatus(STATUS_DISTRIBUTED);
        distribution.setStatusName(STATUS_DISTRIBUTED_NAME);
        distribution.setRemark(dto.getRemark());
        distribution.setCreateBy(operatorId);
        distribution.setUpdateBy(operatorId);
        return distribution;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceive(Long id, String receiverId, String receiverName, String remark, String receiveIp, String receiveUa) {
        DocDistribution distribution = docDistributionMapper.selectById(id);
        if (distribution == null) {
            throw new IllegalArgumentException("分发记录不存在: " + id);
        }

        if (STATUS_CONFIRMED.equals(distribution.getStatus())) {
            log.info("Distribution {} already confirmed, skip", id);
            return;
        }

        distribution.setReceiverId(receiverId);
        distribution.setReceiverName(receiverName);
        distribution.setReceiveTime(LocalDateTime.now());
        distribution.setReceiveIp(receiveIp);
        distribution.setReceiveUa(receiveUa);
        distribution.setStatus(STATUS_CONFIRMED);
        distribution.setStatusName(STATUS_CONFIRMED_NAME);
        distribution.setUpdateBy(receiverId);
        distribution.setUpdateTime(LocalDateTime.now());

        if (StringUtils.hasText(remark)) {
            distribution.setRemark(remark);
        }

        docDistributionMapper.updateById(distribution);

        log.info("Distribution {} confirmed by {}", id, receiverName);

        checkAllUnitsConfirmed(distribution.getDocId(), distribution.getDistributionNo(), receiverId, receiverName);
    }

    @Override
    public List<DocDistribution> getByDocId(Long docId) {
        LambdaQueryWrapper<DocDistribution> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocDistribution::getDocId, docId);
        queryWrapper.orderByAsc(DocDistribution::getUnitType, DocDistribution::getCreateTime);
        return docDistributionMapper.selectList(queryWrapper);
    }

    public Map<String, List<DocDistribution>> getByDocIdGrouped(Long docId) {
        List<DocDistribution> distributions = getByDocId(docId);
        return distributions.stream()
                .collect(Collectors.groupingBy(DocDistribution::getUnitType));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markPrinted(Long id, String operatorId, String operatorName) {
        DocDistribution distribution = docDistributionMapper.selectById(id);
        if (distribution == null) {
            throw new IllegalArgumentException("分发记录不存在: " + id);
        }

        distribution.setStatus(STATUS_PRINTED);
        distribution.setStatusName(STATUS_PRINTED_NAME);
        distribution.setUpdateBy(operatorId);
        distribution.setUpdateTime(LocalDateTime.now());

        docDistributionMapper.updateById(distribution);
        log.info("Distribution {} marked as printed by {}", id, operatorName);
    }

    private void checkAllUnitsConfirmed(Long docId, String distributionNo, String operatorId, String operatorName) {
        LambdaQueryWrapper<DocDistribution> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocDistribution::getDocId, docId);
        queryWrapper.eq(DocDistribution::getDistributionNo, distributionNo);
        List<DocDistribution> distributions = docDistributionMapper.selectList(queryWrapper);

        if (CollectionUtils.isEmpty(distributions)) {
            return;
        }

        boolean allConfirmed = distributions.stream()
                .allMatch(d -> STATUS_CONFIRMED.equals(d.getStatus()) || STATUS_PRINTED.equals(d.getStatus()));

        if (allConfirmed) {
            LambdaQueryWrapper<DocDistribution> allDistributionsWrapper = new LambdaQueryWrapper<>();
            allDistributionsWrapper.eq(DocDistribution::getDocId, docId);
            List<DocDistribution> allDistributions = docDistributionMapper.selectList(allDistributionsWrapper);

            boolean allBatchesConfirmed = allDistributions.stream()
                    .allMatch(d -> STATUS_CONFIRMED.equals(d.getStatus()) || STATUS_PRINTED.equals(d.getStatus()));

            if (allBatchesConfirmed) {
                statusMachineService.transitionWithReason(
                        docId,
                        DocStatusEnum.ARCHIVED.getCode(),
                        "所有单位已确认接收，自动归档",
                        operatorId,
                        operatorName,
                        "共分发 " + allDistributions.size() + " 个单位，已全部确认接收"
                );
                log.info("Document {} archived automatically, all {} units confirmed", docId, allDistributions.size());
            }
        }
    }

    private String generateDistributionNo() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        int random = RANDOM.nextInt(10000);
        return "DIS" + timestamp + String.format("%04d", random);
    }
}
