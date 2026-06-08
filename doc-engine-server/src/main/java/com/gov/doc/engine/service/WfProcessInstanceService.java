package com.gov.doc.engine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.dto.WfProcessStartDTO;
import com.gov.doc.engine.entity.WfProcessInstance;
import com.gov.doc.engine.vo.WfProcessInstanceVO;

public interface WfProcessInstanceService extends IService<WfProcessInstance> {

    PageResult<WfProcessInstanceVO> pageList(Integer pageNum, Integer pageSize, String startUserId, String status, String keyword);

    WfProcessInstanceVO getDetail(Long id);

    WfProcessInstanceVO getByBusinessKey(String businessKey);

    Long startProcess(WfProcessStartDTO startDTO);

    void suspendProcess(Long id);

    void activateProcess(Long id);

    void terminateProcess(Long id, String reason);

    WfProcessInstanceVO convertToVO(WfProcessInstance instance);
}
