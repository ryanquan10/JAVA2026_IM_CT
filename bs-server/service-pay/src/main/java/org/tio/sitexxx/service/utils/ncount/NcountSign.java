
/*
 * 使用本软件请从杭州钛特云有限公司获取授权，其它途径获取本软件的行为皆为侵权行为
 * 黄庆辉-4412********4310
 *//*
     * Copyright (c) 2015.
     * www.hnapay.com
     */

package org.tio.sitexxx.service.utils.ncount;

import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.tio.sitexxx.service.pay.exception.NCountException;
import org.tio.sitexxx.service.utils.HexStringByte;

/**
 * 签名 验签 加解密
 */
public class NcountSign {

	/**
	 * RSA最大加密明文大小
	 */
	private static final int MAX_ENCRYPT_BLOCK = 117;

	/**
	 * 签名
	 *
	 * @param custId
	 *            企业用户编号
	 * @param merData
	 *            明文串
	 * @param transType
	 *            交易类型 0--收款交易 1--付款交易 2--辅助类交易
	 * @return 签名后的消息
	 * @throws NCountException
	 */
	public static byte[] sign(PrivateKey privateKey, String merData) throws NCountException {

		try {
			return NcountRSAAlgorithms.sign(privateKey, merData);
		} catch (Exception e) {
			throw new NCountException("100F1002", "读取密钥异常", e);
		}

	}

	/**
	 * 验证签名
	 *
	 * @param merData
	 *            明文串
	 * @param signMsg
	 *            签名消息
	 * @return 验证签名的结果 true--成功 false--失败
	 * @throws NCountException
	 */
	public static boolean verify(String merData, String signMsg) throws NCountException {
		boolean result = false;
		String hexPublicKey = HexStringByte.byteToHex(Base64Util.decode(NcountKey.NCOUNT_PUBLIC_KEY));
		String signVal = HexStringByte.byteToHex(Base64Util.decode(signMsg));
		result = verifySignatureByRSA(merData, signVal, "UTF-8", hexPublicKey);
		return result;
	}

	/**
	 * @param src
	 * @param dit
	 * @param charsetType
	 * @param publicKey
	 * @return
	 * @throws NCountException
	 */
	private static boolean verifySignatureByRSA(String src, String dit, String charsetType, String publicKey) throws NCountException {
		if ((src == null) || ("".equals(src.trim()))) {
			throw new NCountException("src is empty ,verifySignatureByRSA无法执行");
		}
		if ((dit == null) || ("".equals(dit.trim()))) {
			throw new NCountException("dit is empty ,verifySignatureByRSA无法执行");
		}
		try {
			return NcountRSAAlgorithms.verify(publicKey, src, dit);
		} catch (Exception e) {
			e.printStackTrace();
			throw new NCountException("验证签名出现异常：请检查输入参数", e.getMessage());
		}
	}

	/**
	 * <p>
	 * 公钥加密
	 * </p>
	 *
	 * @param data
	 *            源数据
	 * @return
	 * @throws Exception
	 */
	public static byte[] encryptByPublicKey(byte[] data, String key) throws Exception {
		if (data == null) {
			throw new Exception("需要加密的数据为空");
		}
		PublicKey publicKey = getPublicKey(key);
		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
		KeyFactory keyFactory = KeyFactory.getInstance(NcountKey.ALGORITHM);
		Key publicK = keyFactory.generatePublic(x509KeySpec);
		// 对数据加密
		Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, publicK);
		int inputLen = data.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
		// 对数据分段加密
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
				cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
			} else {
				cache = cipher.doFinal(data, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * MAX_ENCRYPT_BLOCK;
		}
		byte[] encryptedData = out.toByteArray();
		out.close();
		return encryptedData;
	}

	/**
	 * @param key
	 *            密钥信息
	 * @return 返回公钥
	 * @throws Exception
	 */
	private static PublicKey getPublicKey(String publicKey) throws Exception {
		PublicKey pubKey = null;
		try {
			KeyFactory keyFactory = KeyFactory.getInstance(NcountKey.ALGORITHM);
			pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(Base64Util.decode(publicKey)));
		} catch (InvalidKeySpecException e) {
			throw new Exception("公钥无效!", e);
		}
		return pubKey;
	}

	/**
	 * hex2byte.
	 *
	 * @param hexStr
	 *            hexStr
	 * @return byte[]
	 */
	public static byte[] hex2byte(String hexStr) {
		byte[] bts = new byte[hexStr.length() / 2];
		for (int i = 0, j = 0; j < bts.length; j++) {
			bts[j] = (byte) Integer.parseInt(hexStr.substring(i, i + 2), 16);
			i += 2;
		}
		return bts;
	}
}
