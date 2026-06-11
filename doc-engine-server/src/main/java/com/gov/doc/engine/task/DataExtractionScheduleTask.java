package com.gov.doc.engine.task;

import com.gov.doc.engine.service.DataExtractionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataExtractionScheduleTask {

    @Autowired
    private DataExtractionService dataExtractionService;

    @Scheduled(cron = "0 30 1 * * ?")
    public void scheduledExtraction() {
        log.info("开始执行定时数据抽取任务...");
        try {
            dataExtractionService.extractAll();
            log.info("定时数据抽取任务执行完成");
        } catch (Exception e) {
            log.error("定时数据抽取任务执行失败", e);
        }
    }
}
