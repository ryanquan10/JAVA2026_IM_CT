package org.tio.mg.web.server.utils;

import cn.hutool.core.codec.Base32;
import org.jboss.aerogear.security.otp.Totp;

public class GoogleAuthUtils {

    /**
     * 生成随机密钥（Base32 编码）
     */
    public static String generateSecretKey() {
        byte[] keyBytes = new byte[10]; // 生成 10 字节的随机密钥
        new java.security.SecureRandom().nextBytes(keyBytes);
        return Base32.encode(keyBytes); // 使用 Hutool 的 Base32 编码
    }

    /**
     * 验证动态验证码是否正确
     */
    public static boolean validateCode(String secretKey, String code) {
        Totp totp = new Totp(secretKey);
        return totp.verify(code);
    }

    /**
     * 生成二维码内容（符合 Google Authenticator 格式）
     */
    public static String generateQrCodeUrl(String secretKey, String userId, String issuer) {
        try {
            String encodedIssuer = java.net.URLEncoder.encode(issuer, "UTF-8");
            String encodedUserId = java.net.URLEncoder.encode(userId, "UTF-8");
            return String.format(
                    "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                    encodedIssuer,
                    encodedUserId,
                    secretKey,
                    encodedIssuer
            );
        } catch (Exception e) {
            throw new RuntimeException("生成二维码内容失败", e);
        }
    }
}