package com.gov.doc.engine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gov.doc.engine.dto.DocAttachmentDTO;
import com.gov.doc.engine.entity.DocAttachment;
import com.gov.doc.engine.vo.DocAttachmentVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocAttachmentService extends IService<DocAttachment> {

    List<DocAttachmentVO> listByDocumentId(Long documentId);

    DocAttachmentVO getDetail(Long id);

    Long saveAttachment(DocAttachmentDTO attachmentDTO);

    void updateAttachment(DocAttachmentDTO attachmentDTO);

    void deleteAttachment(Long id);

    DocAttachmentVO uploadAttachment(Long documentId, MultipartFile file, String attachmentName);

    byte[] downloadAttachment(Long id);

    String getPreviewUrl(Long id);
}
