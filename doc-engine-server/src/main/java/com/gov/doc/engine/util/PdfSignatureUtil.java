package com.gov.doc.engine.util;

import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.*;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Calendar;

@Slf4j
public class PdfSignatureUtil {

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private PdfSignatureUtil() {
    }

    public static byte[] addSignatureImage(byte[] pdfBytes, byte[] imageBytes,
                                           int pageNumber, float x, float y,
                                           float width, float height) throws Exception {
        PdfReader reader = new PdfReader(pdfBytes);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfStamper stamper = PdfStamper.createSignature(reader, outputStream, '\0');

        try {
            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
            appearance.setReason("公文签章");
            appearance.setLocation("");
            appearance.setSignDate(Calendar.getInstance());

            Rectangle pageSize = reader.getPageSize(pageNumber);
            float llx = x;
            float lly = pageSize.getHeight() - y - height;
            float urx = x + width;
            float ury = pageSize.getHeight() - y;

            Rectangle rect = new Rectangle(llx, lly, urx, ury);
            appearance.setVisibleSignature(rect, pageNumber, null);

            Image image = Image.getInstance(imageBytes);
            appearance.setSignatureGraphic(image);
            appearance.setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC);

            appearance.setAcro6Layers(true);

            PrivateKeySignature externalSignature = new PrivateKeySignature(
                    getDummyPrivateKey(), "SM3", "BC");
            Certificate[] chain = new Certificate[]{getDummyCertificate()};

            MakeSignature.signDetached(appearance, externalSignature, chain,
                    null, null, null, 0, MakeSignature.CryptoStandard.CMS);
        } finally {
            try {
                stamper.close();
            } catch (Exception e) {
                log.warn("关闭PdfStamper时出现警告", e);
            }
            try {
                reader.close();
            } catch (Exception e) {
                log.warn("关闭PdfReader时出现警告", e);
            }
        }

        return outputStream.toByteArray();
    }

    public static byte[] addRidingSealImage(byte[] pdfBytes, byte[] imageBytes,
                                            float width, float height) throws Exception {
        PdfReader reader = new PdfReader(pdfBytes);
        int totalPages = reader.getNumberOfPages();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfStamper stamper = PdfStamper.createSignature(reader, outputStream, '\0');

        try {
            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
            appearance.setReason("骑缝章");
            appearance.setLocation("");
            appearance.setSignDate(Calendar.getInstance());

            Rectangle firstPageSize = reader.getPageSize(1);
            float x = firstPageSize.getWidth() - width / 2;
            float y = (firstPageSize.getHeight() - height * totalPages) / 2;

            Rectangle rect = new Rectangle(x, y, x + width / 2, y + height * totalPages);
            appearance.setVisibleSignature(rect, 1, "RidingSeal");

            Image image = Image.getInstance(imageBytes);
            appearance.setSignatureGraphic(image);
            appearance.setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC);
            appearance.setAcro6Layers(true);

            PrivateKeySignature externalSignature = new PrivateKeySignature(
                    getDummyPrivateKey(), "SM3", "BC");
            Certificate[] chain = new Certificate[]{getDummyCertificate()};

            MakeSignature.signDetached(appearance, externalSignature, chain,
                    null, null, null, 0, MakeSignature.CryptoStandard.CMS);
        } finally {
            try {
                stamper.close();
            } catch (Exception e) {
                log.warn("关闭PdfStamper时出现警告", e);
            }
            try {
                reader.close();
            } catch (Exception e) {
                log.warn("关闭PdfReader时出现警告", e);
            }
        }

        return outputStream.toByteArray();
    }

    public static byte[] addSealImageWithoutSign(byte[] pdfBytes, byte[] imageBytes,
                                                  int pageNumber, float x, float y,
                                                  float width, float height) throws Exception {
        PdfReader reader = new PdfReader(pdfBytes);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfStamper stamper = new PdfStamper(reader, outputStream);

        try {
            com.itextpdf.text.pdf.PdfContentByte content = stamper.getOverContent(pageNumber);
            Rectangle pageSize = reader.getPageSize(pageNumber);

            float llx = x;
            float lly = pageSize.getHeight() - y - height;

            Image image = Image.getInstance(imageBytes);
            image.setAbsolutePosition(llx, lly);
            image.scaleAbsolute(width, height);

            content.addImage(image);
        } finally {
            try {
                stamper.close();
            } catch (Exception e) {
                log.warn("关闭PdfStamper时出现警告", e);
            }
            try {
                reader.close();
            } catch (Exception e) {
                log.warn("关闭PdfReader时出现警告", e);
            }
        }

        return outputStream.toByteArray();
    }

    public static byte[] addRidingSealImageWithoutSign(byte[] pdfBytes, byte[] imageBytes,
                                                        float width, float height) throws Exception {
        PdfReader reader = new PdfReader(pdfBytes);
        int totalPages = reader.getNumberOfPages();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfStamper stamper = new PdfStamper(reader, outputStream);

        try {
            for (int i = 1; i <= totalPages; i++) {
                com.itextpdf.text.pdf.PdfContentByte content = stamper.getOverContent(i);
                Rectangle pageSize = reader.getPageSize(i);

                float sliceHeight = height / totalPages;
                float x = pageSize.getWidth() - width / 2;
                float y = pageSize.getHeight() - (sliceHeight * i) - (sliceHeight * (totalPages - i));

                if (i == 1) {
                    y = pageSize.getHeight() - sliceHeight - 20;
                } else if (i == totalPages) {
                    y = 20;
                } else {
                    y = pageSize.getHeight() - (sliceHeight * i) + sliceHeight / 2;
                }

                Image image = Image.getInstance(imageBytes);
                image.setAbsolutePosition(x, y);
                image.scaleAbsolute(width / 2, sliceHeight);

                content.addImage(image);
            }
        } finally {
            try {
                stamper.close();
            } catch (Exception e) {
                log.warn("关闭PdfStamper时出现警告", e);
            }
            try {
                reader.close();
            } catch (Exception e) {
                log.warn("关闭PdfReader时出现警告", e);
            }
        }

        return outputStream.toByteArray();
    }

    public static int getNumberOfPages(byte[] pdfBytes) throws Exception {
        PdfReader reader = new PdfReader(pdfBytes);
        try {
            return reader.getNumberOfPages();
        } finally {
            reader.close();
        }
    }

    public static Rectangle getPageSize(byte[] pdfBytes, int pageNumber) throws Exception {
        PdfReader reader = new PdfReader(pdfBytes);
        try {
            return reader.getPageSize(pageNumber);
        } finally {
            reader.close();
        }
    }

    private static PrivateKey getDummyPrivateKey() {
        return SM2Util.generateKeyPair().getPrivate();
    }

    private static X509Certificate getDummyCertificate() {
        return null;
    }

    public static void saveBytesToFile(byte[] bytes, String filePath) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(bytes);
        }
    }

    public static byte[] readFileToBytes(String filePath) throws Exception {
        java.io.File file = new java.io.File(filePath);
        java.io.FileInputStream fis = new java.io.FileInputStream(file);
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, read);
            }
            return bos.toByteArray();
        } finally {
            fis.close();
        }
    }
}
