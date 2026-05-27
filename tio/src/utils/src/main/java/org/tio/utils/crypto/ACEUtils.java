/*
 * jlefyf本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动jaaui
 */
/**
 * 
 */
package org.tio.utils.crypto;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 *
 */
public class ACEUtils {

    private static Logger log = LoggerFactory.getLogger(ACEUtils.class);

    /**
     * 解密
     * 
     * @param sSrc
     * @param sKey
     * @param ivStr
     * @return
     * @throws Exception
     */
    public static String decrypt(String sSrc, String sKey, String ivStr) throws Exception {
	// 判断Key是否正确
	if (sKey == null) {
	    throw new Exception("Key为空");
	}
	// 判断Key是否为16位
	if (sKey.length() != 16) {
	    throw new Exception("Key长度不是16位");
	}
	byte[] raw = sKey.getBytes("ASCII");
	SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
	Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	IvParameterSpec iv = new IvParameterSpec(ivStr.getBytes());
	cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
	byte[] encrypted1 = Base64.getDecoder().decode(sSrc);// 先用base64解密
	byte[] original = cipher.doFinal(encrypted1);
	String originalString = new String(original);
	return originalString;
    }

    /**
     * 加密
     * 
     * @param sSrc
     * @param sKey
     * @param ivStr 使用CBC模式，需要一个向量iv，可增加加密算法的强度
     * @return
     * @throws Exception
     */
    public static String encrypt(String sSrc, String sKey, String ivStr) throws Exception {
	// 判断Key是否正确
	if (sKey == null) {
	    throw new Exception("Key为空");
	}
	// 判断Key是否为16位
	if (sKey.length() != 16) {
	    throw new Exception("Key长度不是16位");
	}
	byte[] raw = sKey.getBytes();
	SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
	Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// "算法/模式/补码方式"
	IvParameterSpec iv = new IvParameterSpec(ivStr.getBytes());// 使用CBC模式，需要一个向量iv，可增加加密算法的强度
	cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
	byte[] encrypted = cipher.doFinal(sSrc.getBytes());

	return Base64.getEncoder().encodeToString(encrypted);// 此处使用BASE64做转码功能，同时能起到2次加密的作用。
    }

    public static void main(String[] args) throws Exception {
	String cKey = "jhgyujkolkjhbvcf";

	// 需要解密的字串
	String descd = "eHiLAzPjV8eDWYwgp7D/K51lHioUmeM=";
	log.info("【{}】--->【{}】", descd, decrypt(descd, cKey, cKey));

	// 需要加密的字串
	String cSrc = "345";
	// 加密
	String enString = encrypt(cSrc, cKey, cKey);
	log.info("【{}】--->【{}】", cSrc, enString);
	// 解密
	log.info("【{}】--->【{}】", enString, decrypt(enString, cKey, cKey));
    }
    //
    // /**
    // * @param args
    // */
    // public static void main(String[] args) {
    // PropInit.init();
    //
    // String content = "test中文";
    //
    // //随机生成密钥
    // byte[] key =
    // "uPezilSoTLyzkMop".getBytes();//SecureUtil.generateKey(SymmetricAlgorithm.AES.getValue()).getEncoded();
    // log.info(new String(key));
    //
    // //构建
    // AES aes = SecureUtil.aes(key);
    //
    // //加密
    // byte[] encrypt = aes.encrypt(content);
    // //解密
    // byte[] decrypt = aes.decrypt(encrypt);
    //
    // //加密为16进制表示
    // String encryptHex = Base64.decodeToString(encrypt);//.encryptHex(content);
    // log.info(encryptHex);
    // //解密为原字符串
    // String decryptStr = aes.decryptStr(encryptHex);
    //
    // log.info(encryptHex);
    // log.info(decryptStr);
    // }

    /**
     * 
     */
    private ACEUtils() {

    }

}
