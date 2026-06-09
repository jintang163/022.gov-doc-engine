package com.gov.doc.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.common.UserContext;
import com.gov.doc.engine.dto.DocArchiveDTO;
import com.gov.doc.engine.dto.DocArchiveQueryDTO;
import com.gov.doc.engine.entity.DocArchive;
import com.gov.doc.engine.entity.DocDocument;
import com.gov.doc.engine.enums.DocArchiveStatusEnum;
import com.gov.doc.engine.enums.DocStatusEnum;
import com.gov.doc.engine.mapper.DocArchiveMapper;
import com.gov.doc.engine.mapper.DocDocumentMapper;
import com.gov.doc.engine.service.DocArchiveService;
import com.gov.doc.engine.service.DocStatusMachineService;
import com.gov.doc.engine.vo.DocArchiveVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class DocArchiveServiceImpl extends ServiceImpl<DocArchiveMapper, DocArchive> implements DocArchiveService {

    @Autowired
    private DocArchiveMapper docArchiveMapper;

    @Autowired
    private DocDocumentMapper docDocumentMapper;

    @Autowired
    private DocStatusMachineService statusMachineService;

    @Override
    public PageResult<DocArchiveVO> pageList(DocArchiveQueryDTO queryDTO) {
        LambdaQueryWrapper<DocArchive> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getKeyword())) {
            String keyword = queryDTO.getKeyword();
            queryWrapper.and(wrapper -> wrapper
                    .like(DocArchive::getDocTitle, keyword)
                    .or()
                    .like(DocArchive::getDocNumber, keyword)
                    .or()
                    .like(DocArchive::getDocContentSnapshot, keyword)
                    .or()
                    .like(DocArchive::getUnitName, keyword));
        }
        if (queryDTO.getArchiveYear() != null) {
            queryWrapper.eq(DocArchive::getArchiveYear, queryDTO.getArchiveYear());
        }
        if (StringUtils.hasText(queryDTO.getArchiveType())) {
            queryWrapper.eq(DocArchive::getArchiveType, queryDTO.getArchiveType());
        }
        if (StringUtils.hasText(queryDTO.getArchiveDeptId())) {
            queryWrapper.eq(DocArchive::getArchiveDeptId, queryDTO.getArchiveDeptId());
        }
        if (StringUtils.hasText(queryDTO.getSecurityLevel())) {
            queryWrapper.eq(DocArchive::getSecurityLevel, queryDTO.getSecurityLevel());
        }
        if (StringUtils.hasText(queryDTO.getStatus())) {
            queryWrapper.eq(DocArchive::getStatus, queryDTO.getStatus());
        }
        if (StringUtils.hasText(queryDTO.getDocNumber())) {
            queryWrapper.like(DocArchive::getDocNumber, queryDTO.getDocNumber());
        }
        if (StringUtils.hasText(queryDTO.getDocType())) {
            queryWrapper.eq(DocArchive::getDocType, queryDTO.getDocType());
        }
        if (StringUtils.hasText(queryDTO.getUnitCode())) {
            queryWrapper.eq(DocArchive::getUnitCode, queryDTO.getUnitCode());
        }

        queryWrapper.orderByDesc(DocArchive::getCreateTime);

        int pageNum = queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10;
        Page<DocArchive> page = new Page<>(pageNum, pageSize);
        Page<DocArchive> resultPage = docArchiveMapper.selectPage(page, queryWrapper);

        List<DocArchiveVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(resultPage.getTotal(), voList, pageNum, pageSize);
    }

    @Override
    public DocArchiveVO getDetail(Long id) {
        DocArchive archive = docArchiveMapper.selectById(id);
        if (archive == null) {
            throw new RuntimeException("归档记录不存在");
        }
        return convertToVO(archive);
    }

    @Override
    public DocArchiveVO archiveDocument(DocArchiveDTO archiveDTO) {
        Long docId = archiveDTO.getDocId();
        DocDocument document = docDocumentMapper.selectById(docId);
        if (document == null) {
            throw new RuntimeException("公文不存在");
        }

        LambdaQueryWrapper<DocArchive> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(DocArchive::getDocId, docId);
        if (docArchiveMapper.selectCount(existWrapper) > 0) {
            throw new RuntimeException("该公文已归档，不能重复归档");
        }

        if (!DocStatusEnum.ARCHIVED.getCode().equals(document.getStatus())
                && !DocStatusEnum.SIGNED.getCode().equals(document.getStatus())
                && !DocStatusEnum.DISTRIBUTING.getCode().equals(document.getStatus())) {
            throw new RuntimeException("当前公文状态不允许归档，仅已签发/分发中/已归档状态的公文可归档");
        }

        DocArchive archive = new DocArchive();
        archive.setDocId(docId);
        archive.setArchiveNo(generateArchiveNo());
        archive.setArchiveYear(LocalDate.now().getYear());
        archive.setArchiveType(archiveDTO.getArchiveType() != null ? archiveDTO.getArchiveType() : document.getDocType());
        archive.setSecurityLevel(document.getSecurityLevel());
        archive.setArchiveMethod("manual");
        archive.setArchiveLocation(archiveDTO.getArchiveLocation());
        archive.setRetentionPeriod(archiveDTO.getRetentionPeriod());
        archive.setArchiveDate(LocalDate.now());
        archive.setIsLocked(1);
        archive.setStatus(DocArchiveStatusEnum.ARCHIVED.getCode());
        archive.setDocTitle(document.getDocTitle());
        archive.setDocNumber(document.getDocNumber());
        archive.setDocType(document.getDocType());
        archive.setDocContentSnapshot(document.getDocContent());
        archive.setUnitCode(document.getUnitCode());
        archive.setUnitName(document.getUnitName());
        archive.setRemark(archiveDTO.getRemark());

        UserContext currentUser = UserContext.getCurrentUser();
        archive.setArchiveDeptId(String.valueOf(currentUser.getDeptId()));
        archive.setArchiveDeptName(currentUser.getDeptName());

        docArchiveMapper.insert(archive);

        if (!DocStatusEnum.ARCHIVED.getCode().equals(document.getStatus())) {
            statusMachineService.transitionWithReason(
                    docId, DocStatusEnum.ARCHIVED.getCode(), "手动归档",
                    String.valueOf(currentUser.getUserId()), currentUser.getRealName(), null);
        }

        return convertToVO(archive);
    }

    @Override
    public void autoArchive(Long docId) {
        DocDocument document = docDocumentMapper.selectById(docId);
        if (document == null) {
            throw new RuntimeException("公文不存在");
        }

        LambdaQueryWrapper<DocArchive> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(DocArchive::getDocId, docId);
        if (docArchiveMapper.selectCount(existWrapper) > 0) {
            return;
        }

        DocArchive archive = new DocArchive();
        archive.setDocId(docId);
        archive.setArchiveNo(generateArchiveNo());
        archive.setArchiveYear(LocalDate.now().getYear());
        archive.setArchiveType(document.getDocType());
        archive.setSecurityLevel(document.getSecurityLevel());
        archive.setArchiveMethod("auto");
        archive.setArchiveDate(LocalDate.now());
        archive.setIsLocked(1);
        archive.setStatus(DocArchiveStatusEnum.ARCHIVED.getCode());
        archive.setDocTitle(document.getDocTitle());
        archive.setDocNumber(document.getDocNumber());
        archive.setDocType(document.getDocType());
        archive.setDocContentSnapshot(document.getDocContent());
        archive.setUnitCode(document.getUnitCode());
        archive.setUnitName(document.getUnitName());

        UserContext currentUser = UserContext.getCurrentUser();
        archive.setArchiveDeptId(String.valueOf(currentUser.getDeptId()));
        archive.setArchiveDeptName(currentUser.getDeptName());

        docArchiveMapper.insert(archive);

        if (!DocStatusEnum.ARCHIVED.getCode().equals(document.getStatus())) {
            statusMachineService.transitionWithReason(
                    docId, DocStatusEnum.ARCHIVED.getCode(), "自动归档",
                    String.valueOf(currentUser.getUserId()), currentUser.getRealName(), null);
        }
    }

    @Override
    public List<DocArchiveVO> listByYear(Integer archiveYear) {
        LambdaQueryWrapper<DocArchive> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocArchive::getArchiveYear, archiveYear);
        queryWrapper.orderByDesc(DocArchive::getArchiveDate);
        return docArchiveMapper.selectList(queryWrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getArchiveStats() {
        List<Map<String, Object>> stats = new ArrayList<>();

        List<Map<String, Object>> yearStats = docArchiveMapper.selectMaps(
                new LambdaQueryWrapper<DocArchive>()
                        .select(DocArchive::getArchiveYear)
                        .groupBy(DocArchive::getArchiveYear)
                        .orderByDesc(DocArchive::getArchiveYear));
        for (Map<String, Object> item : yearStats) {
            Map<String, Object> stat = new HashMap<>();
            stat.put("archiveYear", item.get("archive_year"));
            LambdaQueryWrapper<DocArchive> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.eq(DocArchive::getArchiveYear, item.get("archive_year"));
            stat.put("count", docArchiveMapper.selectCount(countWrapper));
            stats.add(stat);
        }

        return stats;
    }

    @Override
    public DocArchiveVO searchByDocNumber(String docNumber) {
        LambdaQueryWrapper<DocArchive> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocArchive::getDocNumber, docNumber);
        DocArchive archive = docArchiveMapper.selectOne(queryWrapper);
        if (archive == null) {
            return null;
        }
        return convertToVO(archive);
    }

    private String generateArchiveNo() {
        return "DA" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + String.format("%04d", new Random().nextInt(10000));
    }

    private DocArchiveVO convertToVO(DocArchive archive) {
        DocArchiveVO vo = new DocArchiveVO();
        BeanUtils.copyProperties(archive, vo);
        vo.setStatusName(DocArchiveStatusEnum.getNameByCode(archive.getStatus()));
        vo.setArchiveMethodName("auto".equals(archive.getArchiveMethod()) ? "自动归档" : "手动归档");
        vo.setRetentionPeriodName(getRetentionPeriodName(archive.getRetentionPeriod()));
        return vo;
    }

    private String getRetentionPeriodName(String retentionPeriod) {
        if (retentionPeriod == null) return null;
        switch (retentionPeriod) {
            case "permanent": return "永久";
            case "long": return "长期(30年)";
            case "short": return "短期(10年)";
            default: return retentionPeriod;
        }
    }
}
