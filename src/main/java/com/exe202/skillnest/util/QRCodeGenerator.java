package com.exe202.skillnest.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class QRCodeGenerator {

    private static final int QR_CODE_WIDTH = 300;
    private static final int QR_CODE_HEIGHT = 300;

    /**
     * Generate QR code for VietQR bank transfer
     * Format: Bank code|Account number|Account name|Amount|Transfer note
     */
    public byte[] generateBankTransferQR(String bankCode, String accountNumber,
                                          String accountName, BigDecimal amount,
                                          String transferNote) throws Exception {
        try {
            // VietQR format for bank transfer
            String qrContent = String.format(
                "2|%s|%s|%s|%s|%s|0|0|%s",
                bankCode,
                accountNumber,
                accountName,
                amount.toPlainString(),
                transferNote,
                "VND"
            );

            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                qrContent,
                BarcodeFormat.QR_CODE,
                QR_CODE_WIDTH,
                QR_CODE_HEIGHT,
                hints
            );

            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "PNG", outputStream);

            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Error generating QR code: {}", e.getMessage(), e);
            throw new Exception("Failed to generate QR code: " + e.getMessage());
        }
    }

    /**
     * Generate simple text QR code
     */
    public byte[] generateTextQR(String text) throws Exception {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                text,
                BarcodeFormat.QR_CODE,
                QR_CODE_WIDTH,
                QR_CODE_HEIGHT,
                hints
            );

            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "PNG", outputStream);

            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Error generating text QR code: {}", e.getMessage(), e);
            throw new Exception("Failed to generate QR code: " + e.getMessage());
        }
    }
}

