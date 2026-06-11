package com.gov.doc.engine.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gov.doc.engine.common.BizNoGenerator;
import com.gov.doc.engine.entity.DocHandling;
import com.gov.doc.engine.entity.DocUrgeLog;
import com.gov.doc.engine.enums.DocHandlingStatusEnum;
import com.gov.doc.engine.enums.UrgeStatusEnum;
import com.gov.doc.engine.mapper.DocHandlingMapper;
import com.gov.doc.engine.mapper.DocUrgeLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class SupervisionScheduleTask {

    @Autowired
    private DocHandlingMapper docHandlingMapper;

    @Autowired
    private DocUrgeLogMapper docUrgeLogMapper;

    @Scheduled(cron = "0 0 9 * * ?")
    public void autoUrgeTimeoutHandlings() {
        log.info("开始执行超时公文自动催办任务...");

        try {
            LambdaQueryWrapper<DocHandling> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DocHandling::getStatus, DocHandlingStatusEnum.PENDING.getCode())
                    .le(DocHandling::getDeadline, LocalDate.now())
                    .isNotNull(DocHandling::getDeadline);

            List<DocHandling> timeoutHandlings = docHandlingMapper.selectList(queryWrapper);
            log.info("发现 {} 条超时待处理记录", timeoutHandlings.size());

            int urgeCount = 0;
            for (DocHandling handling : timeoutHandlings) {
                if (hasUrgedToday(handling.getId())) {
                    continue;
                }

                DocUrgeLog urgeLog = new DocUrgeLog();
                urgeLog.setIncomingId(handling.getIncomingId());
                urgeLog.setHandlingId(handling.getId());
                urgeLog.setUrgeNo(BizNoGenerator.generateUrgeNo());
                urgeLog.setUrgeType("system");
                urgeLog.setUrgedUserId(handling.getTargetUserId());
                urgeLog.setUrgedUserName(handling.getTargetUserName());
                urgeLog.setUrgedDeptId(handling.getTargetDeptId());
                urgeLog.setUrgedDeptName(handling.getTargetDeptName());
                urgeLog.setUrgeContent("系统自动催办：该公文已超过办理期限，请尽快处理");
                urgeLog.setStatus(UrgeStatusEnum.SENT.getCode());

                docUrgeLogMapper.insert(urgeLog);
                urgeCount++;
            }

            log.info("超时公文自动催办任务完成，本次新增催办记录 {} 条", urgeCount);
        } catch (Exception e) {
            log.error("超时公文自动催办任务执行失败", e);
        }
    }

    private boolean hasUrgedToday(Long handlingId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);

        LambdaQueryWrapper<DocUrgeLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocUrgeLog::getHandlingId, handlingId)
                .eq(DocUrgeLog::getUrgeType, "system")
                .ge(DocUrgeLog::getCreateTime, startOfDay)
                .le(DocUrgeLog::getCreateTime, endOfDay);

        return docUrgeLogMapper.selectCount(queryWrapper) > 0;
    }
}
