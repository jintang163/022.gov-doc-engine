package com.gov.doc.engine.common;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

public class BizNoGenerator {

    private static final AtomicLong SEQUENCE = new AtomicLong(0);
    private static final long PROCESS_ID;
    private static String lastDatePart = "";

    static {
        String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        PROCESS_ID = Math.abs(processName.hashCode() % 100);
    }

    private BizNoGenerator() {
    }

    public static String generateArchiveNo() {
        return generate("DA");
    }

    public static String generateBorrowNo() {
        return generate("BR");
    }

    public static String generateIncomingNo() {
        return generate("IC");
    }

    public static String generateHandlingNo() {
        return generate("HD");
    }

    public static String generateAuditLogNo() {
        return generate("AL");
    }

    private static synchronized String generate(String prefix) {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        if (!datePart.equals(lastDatePart)) {
            SEQUENCE.set(0);
            lastDatePart = datePart;
        }
        long seq = SEQUENCE.incrementAndGet();
        long threadPart = Thread.currentThread().getId() % 1000;
        long timePart = System.currentTimeMillis() % 100000;
        return prefix + datePart + String.format("%03d", PROCESS_ID)
                + String.format("%05d", timePart)
                + String.format("%06d", seq);
    }
}
