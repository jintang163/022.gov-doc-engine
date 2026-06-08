package com.gov.doc.engine.service.impl;

import com.alibaba.fastjson2.JSON;
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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

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
    public DocDistribution distribute(DocDistributionCreateDTO dto, String operatorId, String operatorName) {
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

        String distributionType = dto.getDistributionType();
        if (!StringUtils.hasText(distributionType)) {
            distributionType = DocDistributionTypeEnum.ELECTRONIC.getCode();
        }

        DocDistribution distribution = new DocDistribution();
        BeanUtils.copyProperties(dto, distribution);
        distribution.setDistributionNo(generateDistributionNo());
        distribution.setDistributionType(distributionType);
        distribution.setDistributionTypeName(DocDistributionTypeEnum.getNameByCode(distributionType));

        if (!CollectionUtils.isEmpty(dto.getMainSendUnits())) {
            distribution.setMainSendUnits(JSON.toJSONString(dto.getMainSendUnits()));
        }
        if (!CollectionUtils.isEmpty(dto.getCopySendUnits())) {
            distribution.setCopySendUnits(JSON.toJSONString(dto.getCopySendUnits()));
        }
        if (dto.getPrintCount() != null) {
            distribution.setPrintCount(dto.getPrintCount().toString());
        }

        distribution.setDelivererId(operatorId);
        distribution.setDelivererName(operatorName);
        distribution.setDistributeTime(LocalDateTime.now());
        distribution.setStatus("distributed");
        distribution.setStatusName("已分发");
        distribution.setCreateBy(operatorId);
        distribution.setUpdateBy(operatorId);

        docDistributionMapper.insert(distribution);

        statusMachineService.transitionWithReason(
                dto.getDocId(),
                DocStatusEnum.DISTRIBUTING.getCode(),
                "分发公文",
                operatorId,
                operatorName,
                "分发文号: " + distribution.getDistributionNo()
        );

        return distribution;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceive(Long id, String receiverId, String receiverName, String remark) {
        DocDistribution distribution = docDistributionMapper.selectById(id);
        if (distribution == null) {
            throw new IllegalArgumentException("分发记录不存在: " + id);
        }

        if ("received".equals(distribution.getStatus())) {
            return;
        }

        distribution.setReceiverId(receiverId);
        distribution.setReceiverName(receiverName);
        distribution.setReceiveTime(LocalDateTime.now());
        distribution.setStatus("received");
        distribution.setStatusName("已接收");
        distribution.setUpdateBy(receiverId);
        distribution.setUpdateTime(LocalDateTime.now());

        if (StringUtils.hasText(remark)) {
            distribution.setRemark(remark);
        }

        docDistributionMapper.updateById(distribution);

        checkAllDistributedReceived(distribution.getDocId(), receiverId, receiverName);
    }

    @Override
    public List<DocDistribution> getByDocId(Long docId) {
        LambdaQueryWrapper<DocDistribution> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocDistribution::getDocId, docId);
        queryWrapper.orderByDesc(DocDistribution::getCreateTime);
        return docDistributionMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markPrinted(Long id, String operatorId, String operatorName) {
        DocDistribution distribution = docDistributionMapper.selectById(id);
        if (distribution == null) {
            throw new IllegalArgumentException("分发记录不存在: " + id);
        }

        distribution.setStatus("printed");
        distribution.setStatusName("已打印");
        distribution.setUpdateBy(operatorId);
        distribution.setUpdateTime(LocalDateTime.now());

        docDistributionMapper.updateById(distribution);
    }

    private void checkAllDistributedReceived(Long docId, String operatorId, String operatorName) {
        LambdaQueryWrapper<DocDistribution> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocDistribution::getDocId, docId);
        List<DocDistribution> distributions = docDistributionMapper.selectList(queryWrapper);

        if (CollectionUtils.isEmpty(distributions)) {
            return;
        }

        boolean allReceived = distributions.stream()
                .allMatch(d -> "received".equals(d.getStatus()) || "printed".equals(d.getStatus()));

        if (allReceived) {
            statusMachineService.transitionWithReason(
                    docId,
                    DocStatusEnum.ARCHIVED.getCode(),
                    "所有单位已接收，自动归档",
                    operatorId,
                    operatorName,
                    "共分发 " + distributions.size() + " 个单位，已全部接收"
            );
        }
    }

    private String generateDistributionNo() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        int random = RANDOM.nextInt(10000);
        return "DIS" + timestamp + String.format("%04d", random);
    }
}
