package com.gov.doc.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.doc.engine.common.UserContext;
import com.gov.doc.engine.dto.DocIntegrityDTO;
import com.gov.doc.engine.dto.DocIntegrityVerifyDTO;
import com.gov.doc.engine.entity.DocIntegrity;
import com.gov.doc.engine.enums.IntegrityStatusEnum;
import com.gov.doc.engine.mapper.DocIntegrityMapper;
import com.gov.doc.engine.service.DocIntegrityService;
import com.gov.doc.engine.vo.DocIntegrityVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class DocIntegrityServiceImpl extends ServiceImpl<DocIntegrityMapper, DocIntegrity> implements DocIntegrityService {

    @Autowired
    private DocIntegrityMapper docIntegrityMapper;

    @Override
    public DocIntegrityVO createRecord(DocIntegrityDTO dto) {
        Long docId = dto.getDocId();

        LambdaQueryWrapper<DocIntegrity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocIntegrity::getDocId, docId)
                .orderByDesc(DocIntegrity::getVersion)
                .last("LIMIT 1");
        DocIntegrity latest = docIntegrityMapper.selectOne(queryWrapper);

        int nextVersion = (latest != null) ? latest.getVersion() + 1 : 1;

        DocIntegrity integrity = new DocIntegrity();
        integrity.setDocId(docId);
        integrity.setContentHash(dto.getContentHash() != null ? dto.getContentHash() : "");
        integrity.setSignatureHash(dto.getSignatureHash());
        integrity.setHashAlgorithm("SHA-256");
        integrity.setVersion(nextVersion);
        integrity.setVerifyStatus(IntegrityStatusEnum.PENDING.getCode());

        docIntegrityMapper.insert(integrity);
        return convertToVO(integrity);
    }

    @Override
    public DocIntegrityVO verify(DocIntegrityVerifyDTO dto) {
        Long docId = dto.getDocId();

        LambdaQueryWrapper<DocIntegrity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocIntegrity::getDocId, docId)
                .orderByDesc(DocIntegrity::getVersion)
                .last("LIMIT 1");
        DocIntegrity latest = docIntegrityMapper.selectOne(queryWrapper);

        if (latest == null) {
            throw new RuntimeException("公文完整性记录不存在");
        }

        String currentHash = computeHash(dto.getCurrentContent());
        String storedHash = latest.getContentHash();

        UserContext currentUser = UserContext.getCurrentUser();

        boolean isTampered = !currentHash.equals(storedHash);

        latest.setVerifyStatus(isTampered ? IntegrityStatusEnum.TAMPERED.getCode() : IntegrityStatusEnum.VERIFIED.getCode());
        latest.setVerifyTime(LocalDateTime.now());
        latest.setVerifiedBy(String.valueOf(currentUser.getUserId()));
        latest.setVerifiedByName(currentUser.getRealName());

        docIntegrityMapper.updateById(latest);
        return convertToVO(latest);
    }

    @Override
    public DocIntegrityVO getByDocId(Long docId) {
        LambdaQueryWrapper<DocIntegrity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocIntegrity::getDocId, docId)
                .orderByDesc(DocIntegrity::getVersion)
                .last("LIMIT 1");
        DocIntegrity integrity = docIntegrityMapper.selectOne(queryWrapper);
        return integrity != null ? convertToVO(integrity) : null;
    }

    @Override
    public List<DocIntegrityVO> getHistoryByDocId(Long docId) {
        LambdaQueryWrapper<DocIntegrity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocIntegrity::getDocId, docId)
                .orderByDesc(DocIntegrity::getVersion);
        return docIntegrityMapper.selectList(queryWrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public String computeHash(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("计算哈希失败", e);
        }
    }

    private DocIntegrityVO convertToVO(DocIntegrity integrity) {
        DocIntegrityVO vo = new DocIntegrityVO();
        BeanUtils.copyProperties(integrity, vo);
        vo.setVerifyStatusName(IntegrityStatusEnum.getNameByCode(integrity.getVerifyStatus()));
        return vo;
    }
}
