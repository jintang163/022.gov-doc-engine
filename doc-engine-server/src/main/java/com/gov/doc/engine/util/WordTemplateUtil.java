package com.gov.doc.engine.util;

import org.apache.poi.xwpf.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordTemplateUtil {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^{}]+)\\}\\}");
    private static final String TEMPLATE_STORAGE_PATH = System.getProperty("java.io.tmpdir") + "/doc-templates/";

    public static List<String> extractVariables(MultipartFile file) throws IOException {
        Set<String> variables = new LinkedHashSet<>();
        
        try (InputStream is = file.getInputStream();
             XWPFDocument document = new XWPFDocument(is)) {
            
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                extractVariablesFromParagraph(paragraph, variables);
            }
            
            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
                            extractVariablesFromParagraph(paragraph, variables);
                        }
                    }
                }
            }
            
            for (XWPFFooter footer : document.getFooterList()) {
                for (XWPFParagraph paragraph : footer.getParagraphs()) {
                    extractVariablesFromParagraph(paragraph, variables);
                }
            }
            
            for (XWPFHeader header : document.getHeaderList()) {
                for (XWPFParagraph paragraph : header.getParagraphs()) {
                    extractVariablesFromParagraph(paragraph, variables);
                }
            }
        }
        
        return new ArrayList<>(variables);
    }

    private static void extractVariablesFromParagraph(XWPFParagraph paragraph, Set<String> variables) {
        String text = paragraph.getText();
        Matcher matcher = VARIABLE_PATTERN.matcher(text);
        while (matcher.find()) {
            variables.add(matcher.group(1).trim());
        }
    }

    public static String saveTemplateFile(MultipartFile file, Long templateId) throws IOException {
        Path storagePath = Paths.get(TEMPLATE_STORAGE_PATH);
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
        }
        
        String originalFileName = file.getOriginalFilename();
        String fileExtension = originalFileName != null && originalFileName.contains(".") 
                ? originalFileName.substring(originalFileName.lastIndexOf(".")) 
                : ".docx";
        
        String newFileName = "template_" + templateId + "_" + System.currentTimeMillis() + fileExtension;
        Path filePath = storagePath.resolve(newFileName);
        
        try (InputStream is = file.getInputStream();
             OutputStream os = Files.newOutputStream(filePath)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }
        
        return filePath.toString();
    }

    public static byte[] generateDocument(String templatePath, Map<String, Object> variables) throws IOException {
        Path templateFilePath = Paths.get(templatePath);
        if (!Files.exists(templateFilePath)) {
            throw new FileNotFoundException("模板文件不存在: " + templatePath);
        }
        
        try (InputStream is = Files.newInputStream(templateFilePath);
             XWPFDocument document = new XWPFDocument(is);
             ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                replaceVariablesInParagraph(paragraph, variables);
            }
            
            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
                            replaceVariablesInParagraph(paragraph, variables);
                        }
                    }
                }
            }
            
            for (XWPFFooter footer : document.getFooterList()) {
                for (XWPFParagraph paragraph : footer.getParagraphs()) {
                    replaceVariablesInParagraph(paragraph, variables);
                }
            }
            
            for (XWPFHeader header : document.getHeaderList()) {
                for (XWPFParagraph paragraph : header.getParagraphs()) {
                    replaceVariablesInParagraph(paragraph, variables);
                }
            }
            
            document.write(os);
            return os.toByteArray();
        }
    }

    private static void replaceVariablesInParagraph(XWPFParagraph paragraph, Map<String, Object> variables) {
        List<XWPFRun> runs = paragraph.getRuns();
        if (runs.isEmpty()) {
            return;
        }

        StringBuilder fullText = new StringBuilder();
        for (XWPFRun run : runs) {
            String text = run.getText(0);
            if (text != null) {
                fullText.append(text);
            }
        }

        String paragraphText = fullText.toString();
        Matcher matcher = VARIABLE_PATTERN.matcher(paragraphText);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String varName = matcher.group(1).trim();
            Object value = variables.get(varName);
            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);

        String newText = sb.toString();
        if (!paragraphText.equals(newText)) {
            if (!runs.isEmpty()) {
                XWPFRun firstRun = runs.get(0);
                firstRun.setText(newText, 0);
                
                for (int i = 1; i < runs.size(); i++) {
                    runs.get(i).setText("", 0);
                }
            }
        }
    }

    public static void deleteTemplateFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
            }
        } catch (IOException e) {
            // 忽略删除错误
        }
    }
}
