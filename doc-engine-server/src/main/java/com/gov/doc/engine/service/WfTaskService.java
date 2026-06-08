package com.gov.doc.engine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.dto.WfApprovalDTO;
import com.gov.doc.engine.dto.WfTaskAddSignDTO;
import com.gov.doc.engine.dto.WfTaskDelegateDTO;
import com.gov.doc.engine.dto.WfTaskQueryDTO;
import com.gov.doc.engine.entity.WfTask;
import com.gov.doc.engine.vo.WfTaskCountVO;
import com.gov.doc.engine.vo.WfTaskVO;

import java.util.List;

public interface WfTaskService extends IService<WfTask> {

    PageResult<WfTaskVO> pageTodoList(Integer pageNum, Integer pageSize, WfTaskQueryDTO queryDTO);

    PageResult<WfTaskVO> pageDoneList(Integer pageNum, Integer pageSize, WfTaskQueryDTO queryDTO);

    PageResult<WfTaskVO> pageList(Integer pageNum, Integer pageSize, WfTaskQueryDTO queryDTO);

    WfTaskVO getDetail(Long id);

    List<WfTaskVO> getByProcessInstanceId(Long processInstanceId);

    void claimTask(Long taskId);

    void unclaimTask(Long taskId);

    void completeTask(WfApprovalDTO approvalDTO);

    void delegateTask(WfTaskDelegateDTO delegateDTO);

    void addSignTask(WfTaskAddSignDTO addSignDTO);

    WfTaskCountVO getTaskCount(String userId);

    WfTaskVO convertToVO(WfTask task);

    List<WfTaskVO> findTodoTasks(String assigneeId, Long processInstanceId);
}
