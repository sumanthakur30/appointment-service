package com.shopmanagement.ipdservice.family;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Map;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

@Component
public class FamilyPassQrService {

    private final String publicBaseUrl;

    public FamilyPassQrService(
            @Value("${ipd.family.public-base-url:http://localhost:4200}") String publicBaseUrl) {
        this.publicBaseUrl = trimSlash(publicBaseUrl);
    }

    public String portalUrl(String passCode) {
        return publicBaseUrl + "/family/" + (passCode == null ? "" : passCode.trim().toUpperCase());
    }

    public byte[] png(String passCode) {
        String url = portalUrl(passCode);
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(
                    url,
                    BarcodeFormat.QR_CODE,
                    280,
                    280,
                    Map.of(EncodeHintType.MARGIN, 1));
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Could not render pass QR: " + ex.getMessage(), ex);
        }
    }

    private static String trimSlash(String url) {
        if (url == null || url.isBlank()) {
            return "http://localhost:4200";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
