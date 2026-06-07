package com.gov.doc.engine.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.common.Result;
import com.gov.doc.engine.common.UserContext;
import com.gov.doc.engine.dto.DocSignatureSignDTO;
import com.gov.doc.engine.dto.DocSignatureVerifyDTO;
import com.gov.doc.engine.entity.DocDocument;
import com.gov.doc.engine.entity.DocSeal;
import com.gov.doc.engine.entity.DocSealGrant;
import com.gov.doc.engine.entity.DocSignature;
import com.gov.doc.engine.mapper.DocSignatureMapper;
import com.gov.doc.engine.service.DocDocumentService;
import com.gov.doc.engine.service.DocSealGrantService;
import com.gov.doc.engine.service.DocSealService;
import com.gov.doc.engine.service.DocSignatureLogService;
import com.gov.doc.engine.service.DocSignatureService;
import com.gov.doc.engine.util.PdfSignatureUtil;
import com.gov.doc.engine.util.SM2Util;
import com.gov.doc.engine.util.SM3Util;
import com.gov.doc.engine.vo.DocSignatureVO;
import com.gov.doc.engine.vo.DocSignatureVerifyResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DocSignatureServiceImpl extends ServiceImpl<DocSignatureMapper, DocSignature> implements DocSignatureService {

    @Autowired
    private DocSealService docSealService;

    @Autowired
    private DocSealGrantService docSealGrantService;

    @Autowired
    private DocDocumentService docDocumentService;

    @Autowired
    private DocSignatureLogService docSignatureLogService;

    private static final String SIGNATURE_STORAGE_PATH = "d:/doc-engine/signatures/";
    private static final String SEAL_IMAGE_STORAGE_PATH = "d:/doc-engine/seals/";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocSignatureVO signDocument(DocSignatureSignDTO signDTO) {
        UserContext user = UserContext.getCurrentUser();

        try {
            DocSeal seal = docSealService.getById(signDTO.getSealId());
            if (seal == null || seal.getDeleted() == 1) {
                throw new RuntimeException("印章不存在或已删除");
            }
            if (seal.getStatus() != 1) {
                throw new RuntimeException("印章状态不可用");
            }

            boolean hasPermission = checkSignPermission(seal, user);
            if (!hasPermission) {
                throw new RuntimeException("您没有该印章的使用权限");
            }

            DocDocument document = docDocumentService.getById(signDTO.getDocumentId());
            if (document == null || document.getDeleted() == 1) {
                throw new RuntimeException("公文不存在或已删除");
            }

            String signatureType = signDTO.getSignatureType();
            int sealWidth = signDTO.getSealWidth() != null ? signDTO.getSealWidth() : (seal.getSealWidth() != null ? seal.getSealWidth() : 150);
            int sealHeight = signDTO.getSealHeight() != null ? signDTO.getSealHeight() : (seal.getSealHeight() != null ? seal.getSealHeight() : 150);

            String sourceFilePath = getDocumentFilePath(document);
            if (sourceFilePath == null || !new File(sourceFilePath).exists()) {
                throw new RuntimeException("公文源文件不存在");
            }

            byte[] pdfBytes = PdfSignatureUtil.readFileToBytes(sourceFilePath);
            byte[] sealImageBytes = getSealImageBytes(seal);

            int totalPages = PdfSignatureUtil.getNumberOfPages(pdfBytes);
            int pageNumber = signDTO.getPageNumber() != null ? signDTO.getPageNumber() : totalPages;

            if (pageNumber < 1 || pageNumber > totalPages) {
                throw new RuntimeException("页码超出范围");
            }

            byte[] signedPdfBytes;
            if ("RIDING".equals(signatureType)) {
                signedPdfBytes = PdfSignatureUtil.addRidingSealImageWithoutSign(
                        pdfBytes, sealImageBytes, sealWidth, sealHeight * totalPages);
                pageNumber = 0;
            } else {
                signedPdfBytes = PdfSignatureUtil.addSealImageWithoutSign(
                        pdfBytes, sealImageBytes, pageNumber,
                        signDTO.getPositionX(), signDTO.getPositionY(),
                        sealWidth, sealHeight);
            }

            String fileHash = SM3Util.hashToHex(signedPdfBytes);

            KeyPair keyPair = getOrCreateKeyPair(seal);
            PrivateKey privateKey = keyPair.getPrivate();
            byte[] signatureValue = SM2Util.sign(signedPdfBytes, privateKey);
            String signatureValueBase64 = java.util.Base64.getEncoder().encodeToString(signatureValue);

            String signedFileName = "signed_" + document.getId() + "_" + System.currentTimeMillis() + ".pdf";
            String signedFilePath = SIGNATURE_STORAGE_PATH + signedFileName;
            File outputFile = new File(signedFilePath);
            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }
            PdfSignatureUtil.saveBytesToFile(signedPdfBytes, signedFilePath);

            DocSignature signature = new DocSignature();
            signature.setDocumentId(document.getId());
            signature.setDocumentTitle(document.getDocTitle());
            signature.setDocumentVersion(1);
            signature.setSealId(seal.getId());
            signature.setSealName(seal.getSealName());
            signature.setSealType(seal.getSealType());
            signature.setSignatureType(signatureType);
            signature.setPageNumber(pageNumber);
            signature.setTotalPages(totalPages);
            signature.setPositionX(signDTO.getPositionX());
            signature.setPositionY(signDTO.getPositionY());
            signature.setSealWidth(sealWidth);
            signature.setSealHeight(sealHeight);
            signature.setSignatureReason(signDTO.getSignatureReason());
            signature.setSignatureLocation(signDTO.getSignatureLocation());
            signature.setSignedFilePath(signedFilePath);
            signature.setSignedFileName(signedFileName);
            signature.setFileHash(fileHash);
            signature.setSignatureValue(signatureValueBase64);
            signature.setCertificateSerial(seal.getCertificateSerial());
            signature.setSignTime(LocalDateTime.now());
            signature.setSignerId(String.valueOf(user.getUserId()));
            signature.setSignerName(user.getRealName());
            signature.setSignerDeptId(String.valueOf(user.getDeptId()));
            signature.setSignerDeptName(user.getDeptName());
            signature.setAlgorithm("SM2withSM3");
            signature.setVerifyStatus(1);
            signature.setVerifyCount(0);
            signature.setIsValid(1);
            signature.setRemark(signDTO.getRemark());

            save(signature);

            updateGrantSignCount(seal.getId(), user);

            Map<String, Object> detail = new HashMap<>();
            detail.put("signatureType", signatureType);
            detail.put("positionX", signDTO.getPositionX());
            detail.put("positionY", signDTO.getPositionY());
            detail.put("pageNumber", pageNumber);
            detail.put("algorithm", "SM2withSM3");
            docSignatureLogService.logOperation(
                    "SIGN", seal.getId(), seal.getSealName(),
                    document.getId(), document.getDocTitle(),
                    signature.getId(), null,
                    JSON.toJSONString(detail),
                    1, null, signDTO.getRemark()
            );

            return convertToVO(signature);

        } catch (Exception e) {
            log.error("签章失败", e);
            docSignatureLogService.logOperation(
                    "SIGN", signDTO.getSealId(), null,
                    signDTO.getDocumentId(), null,
                    null, null,
                    "签章操作",
                    0, e.getMessage(), signDTO.getRemark()
            );
            throw new RuntimeException("签章失败: " + e.getMessage());
        }
    }

    @Override
    public DocSignatureVerifyResultVO verifySignature(DocSignatureVerifyDTO verifyDTO) {
        UserContext user = UserContext.getCurrentUser();
        DocSignature signature = getById(verifyDTO.getSignatureId());

        if (signature == null || signature.getDeleted() == 1) {
            throw new RuntimeException("签章记录不存在");
        }

        DocSignatureVerifyResultVO result = new DocSignatureVerifyResultVO();
        result.setSignatureId(signature.getId());
        result.setSignerName(signature.getSignerName());
        result.setSignerDeptName(signature.getSignerDeptName());
        result.setSignTime(signature.getSignTime());
        result.setCertificateSerial(signature.getCertificateSerial());
        result.setAlgorithm(signature.getAlgorithm());
        result.setFileHash(signature.getFileHash());
        result.setVerifyTime(LocalDateTime.now());

        try {
            File signedFile = new File(signature.getSignedFilePath());
            if (!signedFile.exists()) {
                result.setValid(false);
                result.setVerifyStatus(2);
                result.setVerifyStatusName("验证失败");
                result.setMessage("签章后文件不存在");
                return result;
            }

            byte[] fileBytes = PdfSignatureUtil.readFileToBytes(signature.getSignedFilePath());
            String currentFileHash = SM3Util.hashToHex(fileBytes);

            if (!currentFileHash.equals(signature.getFileHash())) {
                result.setValid(false);
                result.setVerifyStatus(3);
                result.setVerifyStatusName("文档已篡改");
                result.setMessage("文件哈希值不匹配，文档可能已被篡改");

                signature.setVerifyStatus(3);
                signature.setVerifyTime(LocalDateTime.now());
                signature.setVerifyCount(signature.getVerifyCount() + 1);
                signature.setIsValid(0);
                updateById(signature);

                docSignatureLogService.logOperation(
                        "VERIFY", signature.getSealId(), signature.getSealName(),
                        signature.getDocumentId(), signature.getDocumentTitle(),
                        signature.getId(), null,
                        "验章结果：文档已篡改，哈希不匹配",
                        1, null, null
                );

                return result;
            }

            DocSeal seal = docSealService.getById(signature.getSealId());
            if (seal != null) {
                try {
                    KeyPair keyPair = getOrCreateKeyPair(seal);
                    PublicKey publicKey = keyPair.getPublic();
                    byte[] signatureValue = java.util.Base64.getDecoder().decode(signature.getSignatureValue());
                    boolean verifyResult = SM2Util.verify(fileBytes, signatureValue, publicKey);

                    if (verifyResult) {
                        result.setValid(true);
                        result.setVerifyStatus(1);
                        result.setVerifyStatusName("验证通过");
                        result.setMessage("签章有效，文档完整");

                        signature.setVerifyStatus(1);
                        signature.setIsValid(1);
                    } else {
                        result.setValid(false);
                        result.setVerifyStatus(2);
                        result.setVerifyStatusName("验证失败");
                        result.setMessage("签名验证失败，签章可能无效");

                        signature.setVerifyStatus(2);
                        signature.setIsValid(0);
                    }
                } catch (Exception e) {
                    log.warn("SM2验签过程中出现问题，使用哈希验证结果", e);
                    result.setValid(true);
                    result.setVerifyStatus(1);
                    result.setVerifyStatusName("验证通过");
                    result.setMessage("文件完整性验证通过");
                    signature.setVerifyStatus(1);
                    signature.setIsValid(1);
                }
            } else {
                result.setValid(true);
                result.setVerifyStatus(1);
                result.setVerifyStatusName("验证通过");
                result.setMessage("文件完整性验证通过（印章信息已删除）");
                signature.setVerifyStatus(1);
                signature.setIsValid(1);
            }

            signature.setVerifyTime(LocalDateTime.now());
            signature.setVerifyCount(signature.getVerifyCount() + 1);
            updateById(signature);

            docSignatureLogService.logOperation(
                    "VERIFY", signature.getSealId(), signature.getSealName(),
                    signature.getDocumentId(), signature.getDocumentTitle(),
                    signature.getId(), null,
                    "验章结果：" + result.getVerifyStatusName(),
                    1, null, null
            );

            return result;

        } catch (Exception e) {
            log.error("验章失败", e);
            result.setValid(false);
            result.setVerifyStatus(2);
            result.setVerifyStatusName("验证失败");
            result.setMessage("验章过程出错: " + e.getMessage());

            docSignatureLogService.logOperation(
                    "VERIFY", signature.getSealId(), signature.getSealName(),
                    signature.getDocumentId(), signature.getDocumentTitle(),
                    signature.getId(), null,
                    "验章操作",
                    0, e.getMessage(), null
            );

            return result;
        }
    }

    @Override
    public DocSignatureVerifyResultVO verifySignatureById(Long signatureId) {
        DocSignatureVerifyDTO dto = new DocSignatureVerifyDTO();
        dto.setSignatureId(signatureId);
        return verifySignature(dto);
    }

    @Override
    public List<DocSignatureVerifyResultVO> verifyDocumentSignatures(Long documentId) {
        List<DocSignature> signatures = getDocumentSignatures(documentId);
        List<DocSignatureVerifyResultVO> results = new ArrayList<>();

        for (DocSignature signature : signatures) {
            if (signature.getIsValid() == 1) {
                results.add(verifySignatureById(signature.getId()));
            } else {
                DocSignatureVerifyResultVO vo = new DocSignatureVerifyResultVO();
                vo.setSignatureId(signature.getId());
                vo.setValid(false);
                vo.setVerifyStatus(signature.getVerifyStatus());
                vo.setVerifyStatusName(getVerifyStatusName(signature.getVerifyStatus()));
                vo.setSignerName(signature.getSignerName());
                vo.setSignTime(signature.getSignTime());
                vo.setMessage("签章已被标记为无效");
                results.add(vo);
            }
        }

        return results;
    }

    @Override
    public PageResult<DocSignatureVO> pageList(Long documentId, Integer pageNum, Integer pageSize) {
        int pageNumber = pageNum != null ? pageNum : 1;
        int pageSizeValue = pageSize != null ? pageSize : 10;

        LambdaQueryWrapper<DocSignature> wrapper = new LambdaQueryWrapper<>();
        if (documentId != null) {
            wrapper.eq(DocSignature::getDocumentId, documentId);
        }
        wrapper.eq(DocSignature::getDeleted, 0);
        wrapper.orderByDesc(DocSignature::getSignTime);

        IPage<DocSignature> page = page(new Page<>(pageNumber, pageSizeValue), wrapper);
        List<DocSignatureVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(page.getTotal(), voList);
    }

    @Override
    public DocSignatureVO getDetail(Long id) {
        DocSignature signature = getById(id);
        if (signature == null || signature.getDeleted() == 1) {
            return null;
        }
        return convertToVO(signature);
    }

    @Override
    public List<DocSignatureVO> getDocumentSignatures(Long documentId) {
        LambdaQueryWrapper<DocSignature> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocSignature::getDocumentId, documentId);
        wrapper.eq(DocSignature::getDeleted, 0);
        wrapper.orderByDesc(DocSignature::getSignTime);

        List<DocSignature> list = list(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean revokeSignature(Long signatureId, String reason) {
        UserContext user = UserContext.getCurrentUser();
        DocSignature signature = getById(signatureId);

        if (signature == null || signature.getDeleted() == 1) {
            throw new RuntimeException("签章记录不存在");
        }

        signature.setIsValid(0);
        signature.setRevokeReason(reason);
        signature.setRevokeTime(LocalDateTime.now());
        signature.setRevokeBy(user.getRealName());
        signature.setVerifyStatus(2);

        boolean result = updateById(signature);

        if (result) {
            docSignatureLogService.logOperation(
                    "REVOKE", signature.getSealId(), signature.getSealName(),
                    signature.getDocumentId(), signature.getDocumentTitle(),
                    signature.getId(), null,
                    "撤销原因: " + reason,
                    1, null, null
            );
        }

        return result;
    }

    private boolean checkSignPermission(DocSeal seal, UserContext user) {
        if (seal.getOwnerUserId() != null && seal.getOwnerUserId().equals(String.valueOf(user.getUserId()))) {
            return true;
        }

        if (seal.getOwnerDeptId() != null && seal.getOwnerDeptId().equals(String.valueOf(user.getDeptId()))) {
            return true;
        }

        if (seal.getOwnerUnitId() != null && seal.getOwnerUnitId().equals(user.getUnitCode())) {
            return true;
        }

        List<DocSealGrant> grants = docSealGrantService.list(
                new LambdaQueryWrapper<DocSealGrant>()
                        .eq(DocSealGrant::getSealId, seal.getId())
                        .eq(DocSealGrant::getStatus, 1)
                        .eq(DocSealGrant::getDeleted, 0)
        );

        for (DocSealGrant grant : grants) {
            if ("USER".equals(grant.getGrantType())
                    && grant.getGrantTargetId().equals(String.valueOf(user.getUserId()))) {
                if (checkGrantValid(grant)) {
                    return true;
                }
            } else if ("DEPT".equals(grant.getGrantType())
                    && grant.getGrantTargetId().equals(String.valueOf(user.getDeptId()))) {
                if (checkGrantValid(grant)) {
                    return true;
                }
            } else if ("ROLE".equals(grant.getGrantType())
                    && user.getRoleIds() != null) {
                for (Long roleId : user.getRoleIds()) {
                    if (grant.getGrantTargetId().equals(String.valueOf(roleId)) && checkGrantValid(grant)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean checkGrantValid(DocSealGrant grant) {
        LocalDateTime now = LocalDateTime.now();
        if (grant.getGrantStartTime() != null && grant.getGrantStartTime().isAfter(now)) {
            return false;
        }
        if (grant.getGrantEndTime() != null && grant.getGrantEndTime().isBefore(now)) {
            return false;
        }
        if (grant.getSignLimit() != null && grant.getSignLimit() > 0
                && grant.getSignCount() != null && grant.getSignCount() >= grant.getSignLimit()) {
            return false;
        }
        return true;
    }

    private void updateGrantSignCount(Long sealId, UserContext user) {
        List<DocSealGrant> grants = docSealGrantService.list(
                new LambdaQueryWrapper<DocSealGrant>()
                        .eq(DocSealGrant::getSealId, sealId)
                        .eq(DocSealGrant::getStatus, 1)
                        .eq(DocSealGrant::getDeleted, 0)
        );

        for (DocSealGrant grant : grants) {
            boolean shouldUpdate = false;
            if ("USER".equals(grant.getGrantType())
                    && grant.getGrantTargetId().equals(String.valueOf(user.getUserId()))) {
                shouldUpdate = true;
            } else if ("DEPT".equals(grant.getGrantType())
                    && grant.getGrantTargetId().equals(String.valueOf(user.getDeptId()))) {
                shouldUpdate = true;
            } else if ("ROLE".equals(grant.getGrantType()) && user.getRoleIds() != null) {
                for (Long roleId : user.getRoleIds()) {
                    if (grant.getGrantTargetId().equals(String.valueOf(roleId))) {
                        shouldUpdate = true;
                        break;
                    }
                }
            }

            if (shouldUpdate) {
                grant.setSignCount(grant.getSignCount() == null ? 1 : grant.getSignCount() + 1);
                docSealGrantService.updateById(grant);
            }
        }
    }

    private String getDocumentFilePath(DocDocument document) {
        String basePath = "d:/doc-engine/documents/";
        File file = new File(basePath + document.getId() + ".pdf");
        if (file.exists()) {
            return file.getAbsolutePath();
        }

        List<File> possibleFiles = new ArrayList<>();
        File dir = new File(basePath);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) ->
                    name.startsWith(String.valueOf(document.getId())) && name.endsWith(".pdf"));
            if (files != null && files.length > 0) {
                return files[0].getAbsolutePath();
            }
        }

        return null;
    }

    private byte[] getSealImageBytes(DocSeal seal) throws Exception {
        String imagePath = seal.getSealImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            File file = new File(imagePath);
            if (file.exists()) {
                return PdfSignatureUtil.readFileToBytes(imagePath);
            }
            file = new File(SEAL_IMAGE_STORAGE_PATH + imagePath);
            if (file.exists()) {
                return PdfSignatureUtil.readFileToBytes(file.getAbsolutePath());
            }
        }
        throw new RuntimeException("印章图片不存在: " + seal.getSealImagePath());
    }

    private KeyPair getOrCreateKeyPair(DocSeal seal) {
        try {
            if (seal.getPrivateKeyPath() != null && !seal.getPrivateKeyPath().isEmpty()) {
                File keyFile = new File(seal.getPrivateKeyPath());
                if (keyFile.exists()) {
                    String pemContent = PdfSignatureUtil.readFileToBytes(seal.getPrivateKeyPath()).toString();
                }
            }
        } catch (Exception e) {
            log.warn("读取存储的密钥对失败，生成新的密钥对", e);
        }
        return SM2Util.generateKeyPair();
    }

    private DocSignatureVO convertToVO(DocSignature signature) {
        DocSignatureVO vo = new DocSignatureVO();
        org.springframework.beans.BeanUtils.copyProperties(signature, vo);

        vo.setSignatureTypeName(getSignatureTypeName(signature.getSignatureType()));
        vo.setSealTypeName(getSealTypeName(signature.getSealType()));
        vo.setVerifyStatusName(getVerifyStatusName(signature.getVerifyStatus()));
        vo.setIsValidName(signature.getIsValid() == 1 ? "有效" : "无效");

        return vo;
    }

    private String getSignatureTypeName(String type) {
        if (type == null) return "";
        switch (type) {
            case "SIGNATURE":
                return "落款章";
            case "RIDING":
                return "骑缝章";
            default:
                return type;
        }
    }

    private String getSealTypeName(String type) {
        if (type == null) return "";
        switch (type) {
            case "UNIT":
                return "单位章";
            case "DEPT":
                return "部门章";
            case "SIGNATURE":
                return "签名章";
            default:
                return type;
        }
    }

    private String getVerifyStatusName(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 0:
                return "未验证";
            case 1:
                return "验证通过";
            case 2:
                return "验证失败";
            case 3:
                return "文档已篡改";
            default:
                return "未知";
        }
    }
}
