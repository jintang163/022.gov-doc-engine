package com.gov.doc.engine.util;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcePEMEncryptorBuilder;
import org.bouncycastle.util.encoders.Base64;

import java.io.*;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Slf4j
public class SM2Util {

    private static final String PROVIDER = "BC";
    private static final String ALGORITHM = "SM2";
    private static final String SIGNATURE_ALGORITHM = "SM2withSM3";
    private static final String CURVE_NAME = "sm2p256v1";

    static {
        if (Security.getProvider(PROVIDER) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private SM2Util() {
    }

    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(ALGORITHM, PROVIDER);
            ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec(CURVE_NAME);
            kpg.initialize(ecGenParameterSpec, new SecureRandom());
            return kpg.generateKeyPair();
        } catch (Exception e) {
            log.error("生成SM2密钥对失败", e);
            throw new RuntimeException("生成SM2密钥对失败: " + e.getMessage());
        }
    }

    public static byte[] sign(byte[] data, PrivateKey privateKey) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("待签名数据不能为空");
        }
        if (privateKey == null) {
            throw new IllegalArgumentException("私钥不能为空");
        }
        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM, PROVIDER);
            signature.initSign(privateKey);
            signature.update(data);
            return signature.sign();
        } catch (Exception e) {
            log.error("SM2签名失败", e);
            throw new RuntimeException("SM2签名失败: " + e.getMessage());
        }
    }

    public static boolean verify(byte[] data, byte[] sign, PublicKey publicKey) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("待验证数据不能为空");
        }
        if (sign == null || sign.length == 0) {
            throw new IllegalArgumentException("签名数据不能为空");
        }
        if (publicKey == null) {
            throw new IllegalArgumentException("公钥不能为空");
        }
        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM, PROVIDER);
            signature.initVerify(publicKey);
            signature.update(data);
            return signature.verify(sign);
        } catch (Exception e) {
            log.error("SM2验签失败", e);
            throw new RuntimeException("SM2验签失败: " + e.getMessage());
        }
    }

    public static String publicKeyToPem(PublicKey publicKey) {
        if (publicKey == null) {
            throw new IllegalArgumentException("公钥不能为空");
        }
        StringWriter stringWriter = new StringWriter();
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(stringWriter)) {
            pemWriter.writeObject(publicKey);
        } catch (IOException e) {
            log.error("公钥转PEM格式失败", e);
            throw new RuntimeException("公钥转PEM格式失败: " + e.getMessage());
        }
        return stringWriter.toString();
    }

    public static String privateKeyToPem(PrivateKey privateKey) {
        return privateKeyToPem(privateKey, null);
    }

    public static String privateKeyToPem(PrivateKey privateKey, String password) {
        if (privateKey == null) {
            throw new IllegalArgumentException("私钥不能为空");
        }
        StringWriter stringWriter = new StringWriter();
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(stringWriter)) {
            if (password != null && !password.isEmpty()) {
                JcePEMEncryptorBuilder encryptorBuilder = new JcePEMEncryptorBuilder("AES-256-CBC");
                pemWriter.writeObject(privateKey, encryptorBuilder.build(password.toCharArray()));
            } else {
                pemWriter.writeObject(privateKey);
            }
        } catch (IOException e) {
            log.error("私钥转PEM格式失败", e);
            throw new RuntimeException("私钥转PEM格式失败: " + e.getMessage());
        }
        return stringWriter.toString();
    }

    public static PublicKey pemToPublicKey(String pem) {
        if (pem == null || pem.trim().isEmpty()) {
            throw new IllegalArgumentException("PEM格式公钥不能为空");
        }
        try (PEMParser pemParser = new PEMParser(new StringReader(pem))) {
            Object object = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(PROVIDER);
            if (object instanceof org.bouncycastle.asn1.x509.SubjectPublicKeyInfo) {
                return converter.getPublicKey((org.bouncycastle.asn1.x509.SubjectPublicKeyInfo) object);
            } else {
                throw new IllegalArgumentException("PEM格式不正确，不是有效的公钥");
            }
        } catch (Exception e) {
            log.error("PEM转公钥失败", e);
            throw new RuntimeException("PEM转公钥失败: " + e.getMessage());
        }
    }

    public static PrivateKey pemToPrivateKey(String pem) {
        return pemToPrivateKey(pem, null);
    }

    public static PrivateKey pemToPrivateKey(String pem, String password) {
        if (pem == null || pem.trim().isEmpty()) {
            throw new IllegalArgumentException("PEM格式私钥不能为空");
        }
        try (PEMParser pemParser = new PEMParser(new StringReader(pem))) {
            Object object = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(PROVIDER);
            if (object instanceof PEMKeyPair) {
                PEMKeyPair pemKeyPair = (PEMKeyPair) object;
                return converter.getPrivateKey(pemKeyPair.getPrivateKeyInfo());
            } else if (object instanceof org.bouncycastle.asn1.pkcs.PrivateKeyInfo) {
                return converter.getPrivateKey((org.bouncycastle.asn1.pkcs.PrivateKeyInfo) object);
            } else if (object instanceof org.bouncycastle.openssl.PEMEncryptedKeyPair) {
                if (password == null || password.isEmpty()) {
                    throw new IllegalArgumentException("私钥已加密，需要提供密码");
                }
                org.bouncycastle.openssl.PEMEncryptedKeyPair encryptedKeyPair = (org.bouncycastle.openssl.PEMEncryptedKeyPair) object;
                org.bouncycastle.openssl.PEMDecryptorProvider decryptorProvider = new org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder().build(password.toCharArray());
                PEMKeyPair pemKeyPair = encryptedKeyPair.decryptKeyPair(decryptorProvider);
                return converter.getPrivateKey(pemKeyPair.getPrivateKeyInfo());
            } else {
                throw new IllegalArgumentException("PEM格式不正确，不是有效的私钥");
            }
        } catch (Exception e) {
            log.error("PEM转私钥失败", e);
            throw new RuntimeException("PEM转私钥失败: " + e.getMessage());
        }
    }

    public static String publicKeyToBase64(PublicKey publicKey) {
        if (publicKey == null) {
            throw new IllegalArgumentException("公钥不能为空");
        }
        return Base64.toBase64String(publicKey.getEncoded());
    }

    public static String privateKeyToBase64(PrivateKey privateKey) {
        if (privateKey == null) {
            throw new IllegalArgumentException("私钥不能为空");
        }
        return Base64.toBase64String(privateKey.getEncoded());
    }

    public static PublicKey base64ToPublicKey(String base64) {
        if (base64 == null || base64.trim().isEmpty()) {
            throw new IllegalArgumentException("Base64公钥不能为空");
        }
        try {
            byte[] keyBytes = Base64.decode(base64);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM, PROVIDER);
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            log.error("Base64转公钥失败", e);
            throw new RuntimeException("Base64转公钥失败: " + e.getMessage());
        }
    }

    public static PrivateKey base64ToPrivateKey(String base64) {
        if (base64 == null || base64.trim().isEmpty()) {
            throw new IllegalArgumentException("Base64私钥不能为空");
        }
        try {
            byte[] keyBytes = Base64.decode(base64);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM, PROVIDER);
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            log.error("Base64转私钥失败", e);
            throw new RuntimeException("Base64转私钥失败: " + e.getMessage());
        }
    }
}
