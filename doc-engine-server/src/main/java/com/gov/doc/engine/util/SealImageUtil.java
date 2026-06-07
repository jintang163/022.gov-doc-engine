package com.gov.doc.engine.util;

import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class SealImageUtil {

    private static final String SEAL_STORAGE_PATH = System.getProperty("java.io.tmpdir") + "/doc-seals/";
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".png", ".jpg", ".jpeg", ".gif", ".bmp");

    public static String saveSealImage(MultipartFile file, Long sealId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("印章图片不能为空");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            throw new RuntimeException("文件名不能为空");
        }

        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
            throw new RuntimeException("只支持png、jpg、jpeg、gif、bmp格式的图片文件");
        }

        Path storagePath = Paths.get(SEAL_STORAGE_PATH);
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
        }

        String newFileName = "seal_" + sealId + "_" + System.currentTimeMillis() + fileExtension;
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

    public static int[] getImageDimensions(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("图片文件不能为空");
        }

        try (InputStream is = file.getInputStream()) {
            BufferedImage image = ImageIO.read(is);
            if (image == null) {
                throw new RuntimeException("无法读取图片文件");
            }
            return new int[]{image.getWidth(), image.getHeight()};
        }
    }

    public static void deleteSealImage(String filePath) {
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

    public static byte[] readSealImage(String filePath) throws IOException {
        if (filePath == null || filePath.isEmpty()) {
            throw new RuntimeException("文件路径不能为空");
        }

        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("印章图片不存在: " + filePath);
        }

        return Files.readAllBytes(path);
    }
}
