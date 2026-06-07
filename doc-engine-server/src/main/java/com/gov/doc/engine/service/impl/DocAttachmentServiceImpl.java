package com.gov.doc.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gov.doc.engine.dto.DocAttachmentDTO;
import com.gov.doc.engine.entity.DocAttachment;
import com.gov.doc.engine.mapper.DocAttachmentMapper;
import com.gov.doc.engine.service.DocAttachmentService;
import com.gov.doc.engine.service.DocDocumentService;
import com.gov.doc.engine.vo.DocAttachmentVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class DocAttachmentServiceImpl extends ServiceImpl<DocAttachmentMapper, DocAttachment> implements DocAttachmentService {

    private static final String UPLOAD_DIR = "d:/doc-attachments/";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd/");

    @Autowired
    private DocAttachmentMapper docAttachmentMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DocDocumentService docDocumentService;

    @Override
    public List<DocAttachmentVO> listByDocumentId(Long documentId) {
        LambdaQueryWrapper<DocAttachment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocAttachment::getDocumentId, documentId);
        queryWrapper.orderByAsc(DocAttachment::getSortOrder);
        queryWrapper.orderByDesc(DocAttachment::getCreateTime);

        List<DocAttachment> attachments = docAttachmentMapper.selectList(queryWrapper);
        return attachments.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public DocAttachmentVO getDetail(Long id) {
        DocAttachment attachment = docAttachmentMapper.selectById(id);
        if (attachment == null) {
            return null;
        }
        return convertToVO(attachment);
    }

    @Override
    public Long saveAttachment(DocAttachmentDTO attachmentDTO) {
        DocAttachment attachment = new DocAttachment();
        BeanUtils.copyProperties(attachmentDTO, attachment);
        docAttachmentMapper.insert(attachment);
        return attachment.getId();
    }

    @Override
    public void updateAttachment(DocAttachmentDTO attachmentDTO) {
        if (attachmentDTO.getId() == null) {
            throw new RuntimeException("附件ID不能为空");
        }

        DocAttachment existingAttachment = docAttachmentMapper.selectById(attachmentDTO.getId());
        if (existingAttachment == null) {
            throw new RuntimeException("附件不存在");
        }

        DocAttachment attachment = new DocAttachment();
        BeanUtils.copyProperties(attachmentDTO, attachment);
        docAttachmentMapper.updateById(attachment);
    }

    @Override
    public void deleteAttachment(Long id) {
        DocAttachment attachment = docAttachmentMapper.selectById(id);
        if (attachment == null) {
            throw new RuntimeException("附件不存在");
        }

        String filePath = attachment.getFilePath();
        if (StringUtils.hasText(filePath)) {
            try {
                Path path = Paths.get(filePath);
                Files.deleteIfExists(path);
            } catch (IOException e) {
                throw new RuntimeException("删除附件文件失败: " + e.getMessage(), e);
            }
        }

        docAttachmentMapper.deleteById(id);
    }

    @Override
    public DocAttachmentVO uploadAttachment(Long documentId, MultipartFile file, String attachmentName) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("请选择要上传的文件");
        }

        String originalFilename = file.getOriginalFilename();
        String fileExt = "";
        String fileType = file.getContentType();
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExt = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }

        String dateDir = LocalDate.now().format(DATE_FORMATTER);
        String uuidFileName = UUID.randomUUID().toString().replace("-", "");
        String newFileName = uuidFileName + (StringUtils.hasText(fileExt) ? "." + fileExt : "");
        String relativePath = dateDir + newFileName;
        String fullPath = UPLOAD_DIR + relativePath;

        try {
            File dir = new File(UPLOAD_DIR + dateDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            Path path = Paths.get(fullPath);
            file.transferTo(path.toFile());
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败: " + e.getMessage(), e);
        }

        DocAttachment attachment = new DocAttachment();
        attachment.setDocumentId(documentId);
        attachment.setAttachmentName(StringUtils.hasText(attachmentName) ? attachmentName : originalFilename);
        attachment.setOriginalName(originalFilename);
        attachment.setFilePath(fullPath);
        attachment.setFileSize(file.getSize());
        attachment.setFileType(fileType);
        attachment.setFileExt(fileExt);
        docAttachmentMapper.insert(attachment);

        return convertToVO(attachment);
    }

    @Override
    public byte[] downloadAttachment(Long id) {
        DocAttachment attachment = docAttachmentMapper.selectById(id);
        if (attachment == null) {
            throw new RuntimeException("附件不存在");
        }

        String filePath = attachment.getFilePath();
        if (!StringUtils.hasText(filePath)) {
            throw new RuntimeException("附件文件路径不存在");
        }

        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new RuntimeException("附件文件不存在");
            }
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException("读取附件文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getPreviewUrl(Long id) {
        DocAttachment attachment = docAttachmentMapper.selectById(id);
        if (attachment == null) {
            throw new RuntimeException("附件不存在");
        }
        return "/api/doc/document/attachment/preview/" + id;
    }

    private DocAttachmentVO convertToVO(DocAttachment attachment) {
        DocAttachmentVO vo = new DocAttachmentVO();
        BeanUtils.copyProperties(attachment, vo);
        if (attachment.getFileSize() != null) {
            vo.setFileSizeDisplay(docDocumentService.formatFileSize(attachment.getFileSize()));
        }
        return vo;
    }
}
