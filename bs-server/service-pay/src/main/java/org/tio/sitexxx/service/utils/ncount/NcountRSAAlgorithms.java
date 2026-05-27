
/*
 * 使用本软件请从杭州钛特云有限公司获取授权，其它途径获取本软件的行为皆为侵权行为
 * 黄庆辉-4412********4310
 *//*
     * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
     * www.hnapay.com
     */

package org.tio.sitexxx.service.utils.ncount;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import org.tio.sitexxx.service.pay.exception.NCountException;
import org.tio.sitexxx.service.utils.HexStringByte;

/**
 * 签名验签
 */
public class NcountRSAAlgorithms {

	/**
	 * @param publicKey
	 *            公钥HEX字符串
	 * @return 返回公钥
	 * @throws Exception
	 */
	public static PublicKey getPublicKey(String publicKey) throws NCountException {
		PublicKey pubKey = null;
		try {
			byte[] encodedKey = HexStringByte.hexToByte(publicKey.getBytes());
			KeyFactory keyFactory = KeyFactory.getInstance(NcountKey.ALGORITHM);
			pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
		} catch (InvalidKeySpecException e) {
			throw new NCountException("公钥无效!", e);
		} catch (NoSuchAlgorithmException e) {
			throw new NCountException("算法类型无效!", e);
		}
		return pubKey;
	}

	/**
	 * @param priKey
	 *            私钥
	 * @param data
	 *            要签名的数据
	 * @return 签名消息
	 * @throws Exception
	 */
	public static byte[] sign(PrivateKey priKey, String data) throws NCountException {
		try {
			Signature signet = Signature.getInstance(NcountKey.SIGN_ALGORITHM);
			signet.initSign(priKey);
			signet.update(data.getBytes("UTF-8"));
			return signet.sign();
		} catch (Exception e) {
			throw new NCountException(e.getMessage());
		}
	}

	/**
	 * 验证签名
	 *
	 * @param publicKey
	 *            公钥HEX字符串
	 * @param merData
	 *            签名数据
	 * @param signMsg
	 *            签名消息
	 * @return 返回验证结果 true 成功 false 失败
	 * @throws Exception
	 */
	public static Boolean verify(String publicKey, String merData, String signMsg) throws NCountException {

		boolean bVerify = false;
		java.security.Signature signet = null;
		try {
			signet = Signature.getInstance(NcountKey.SIGN_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			throw new NCountException("算法类型不能为空!", e);
		}

		PublicKey pubKey = getPublicKey(publicKey);

		try {
			signet.initVerify(pubKey);
		} catch (InvalidKeyException e) {
			throw new NCountException("公钥无效!", e);
		}
		try {
			signet.update(merData.getBytes("UTF-8"));
		} catch (SignatureException e) {
			throw new NCountException("验签时符号异常!", e);
		} catch (UnsupportedEncodingException e) {
			throw new NCountException("不支持的编码方式", e);
		}

		try {
			bVerify = signet.verify(HexStringByte.hex2byte(signMsg));
		} catch (SignatureException e) {
			throw new NCountException("验签异常!", e);
		}
		return bVerify;
	}

}
