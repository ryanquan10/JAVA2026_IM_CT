
/*
 * 使用本软件请从杭州钛特云有限公司获取授权，其它途径获取本软件的行为皆为侵权行为
 * 黄庆辉-4412********4310
 *//*
     * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
     * www.hnapay.com
     */

package org.tio.sitexxx.service.utils.ncount;

import java.util.Base64;

/**
 * base64编码工具类
 */
public class Base64Util {

	/**
	 * 将 s 进行 BASE64 编码
	 *
	 * @param s
	 * @return
	 */
	public static String encode(byte[] s) {
		if (s == null)
			return null;
		return Base64.getEncoder().encodeToString(s);
	}

	/**
	 * 将 s 进行 BASE64 编码
	 *
	 * @param s
	 * @return
	 */
	public static String encode(String s) {
		if (s == null)
			return null;
		return encode(s.getBytes());
	}

	/**
	 * 将 BASE64 编码的字符串 s 进行解码
	 *
	 * @param s
	 * @return
	 */
	public static byte[] decode(String s) {
		if (s == null)
			return null;
		try {
			byte[] b = Base64.getDecoder().decode(s);
			return b;
		} catch (Exception e) {
			return null;
		}
	}

}
