package com.gov.doc.engine.util;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

import java.io.*;
import java.security.MessageDigest;
import java.security.Security;

@Slf4j
public class SM3Util {

    private static final String PROVIDER = "BC";
    private static final String ALGORITHM = "SM3";
    private static final int BUFFER_SIZE = 8192;

    static {
        if (Security.getProvider(PROVIDER) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private SM3Util() {
    }

    public static byte[] hash(byte[] data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("待哈希数据不能为空");
        }
        try {
            SM3Digest digest = new SM3Digest();
            digest.update(data, 0, data.length);
            byte[] result = new byte[digest.getDigestSize()];
            digest.doFinal(result, 0);
            return result;
        } catch (Exception e) {
            log.error("计算SM3哈希失败", e);
            throw new RuntimeException("计算SM3哈希失败: " + e.getMessage());
        }
    }

    public static String hashToHex(byte[] data) {
        return Hex.toHexString(hash(data));
    }

    public static String hashToBase64(byte[] data) {
        return Base64.toBase64String(hash(data));
    }

    public static byte[] hashFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("文件路径不能为空");
        }
        File file = new File(filePath);
        if (!file.exists()) {
            throw new RuntimeException("文件不存在: " + filePath);
        }
        if (!file.isFile()) {
            throw new RuntimeException("路径不是文件: " + filePath);
        }
        try (InputStream is = new FileInputStream(file)) {
            return hashInputStream(is);
        } catch (IOException e) {
            log.error("计算文件SM3哈希失败: {}", filePath, e);
            throw new RuntimeException("计算文件SM3哈希失败: " + e.getMessage());
        }
    }

    public static String hashFileToHex(String filePath) {
        return Hex.toHexString(hashFile(filePath));
    }

    public static String hashFileToBase64(String filePath) {
        return Base64.toBase64String(hashFile(filePath));
    }

    public static byte[] hashFile(File file) {
        if (file == null) {
            throw new IllegalArgumentException("文件不能为空");
        }
        if (!file.exists()) {
            throw new RuntimeException("文件不存在: " + file.getPath());
        }
        if (!file.isFile()) {
            throw new RuntimeException("路径不是文件: " + file.getPath());
        }
        try (InputStream is = new FileInputStream(file)) {
            return hashInputStream(is);
        } catch (IOException e) {
            log.error("计算文件SM3哈希失败: {}", file.getPath(), e);
            throw new RuntimeException("计算文件SM3哈希失败: " + e.getMessage());
        }
    }

    public static String hashFileToHex(File file) {
        return Hex.toHexString(hashFile(file));
    }

    public static String hashFileToBase64(File file) {
        return Base64.toBase64String(hashFile(file));
    }

    public static byte[] hashInputStream(InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("输入流不能为空");
        }
        try {
            SM3Digest digest = new SM3Digest();
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            byte[] result = new byte[digest.getDigestSize()];
            digest.doFinal(result, 0);
            return result;
        } catch (IOException e) {
            log.error("计算输入流SM3哈希失败", e);
            throw new RuntimeException("计算输入流SM3哈希失败: " + e.getMessage());
        }
    }

    public static String hashInputStreamToHex(InputStream inputStream) {
        return Hex.toHexString(hashInputStream(inputStream));
    }

    public static String hashInputStreamToBase64(InputStream inputStream) {
        return Base64.toBase64String(hashInputStream(inputStream));
    }

    public static byte[] hash(String data) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("待哈希字符串不能为空");
        }
        return hash(data.getBytes());
    }

    public static String hashToHex(String data) {
        return Hex.toHexString(hash(data));
    }

    public static String hashToBase64(String data) {
        return Base64.toBase64String(hash(data));
    }

    public static byte[] hashJce(byte[] data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("待哈希数据不能为空");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM, PROVIDER);
            return digest.digest(data);
        } catch (Exception e) {
            log.error("JCE方式计算SM3哈希失败", e);
            throw new RuntimeException("JCE方式计算SM3哈希失败: " + e.getMessage());
        }
    }

    public static String hashJceToHex(byte[] data) {
        return Hex.toHexString(hashJce(data));
    }

    public static String hashJceToBase64(byte[] data) {
        return Base64.toBase64String(hashJce(data));
    }
}
