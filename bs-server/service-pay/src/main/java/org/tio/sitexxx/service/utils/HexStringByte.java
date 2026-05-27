
/*
 * 使用本软件请从杭州钛特云有限公司获取授权，其它途径获取本软件的行为皆为侵权行为
 * 黄庆辉-4412********4310
 *//*
     * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
     * www.hnapay.com
     */

package org.tio.sitexxx.service.utils;

/**
 * 十六进制转换
 */
public class HexStringByte {

	/**
	* 字符串转换成十六进制值
	* @param bin String 我们看到的要转换成十六进制的字符串
	* @return 
	*/
	public static String stringToHex(String bin) {
		char[] digital = "0123456789ABCDEF".toCharArray();
		StringBuffer sb = new StringBuffer("");
		byte[] bs = bin.getBytes();
		int bit;
		for (int i = 0; i < bs.length; i++) {
			bit = (bs[i] & 0x0f0) >> 4;
			sb.append(digital[bit]);
			bit = bs[i] & 0x0f;
			sb.append(digital[bit]);
		}
		return sb.toString();
	}

	/**
	 * 十六进制转换字符串
	 * @param hex String 十六进制
	 * @return String 转换后的字符串
	 */
	public static String hexToString(String hex) {
		String digital = "0123456789ABCDEF";
		char[] hex2char = hex.toCharArray();
		byte[] bytes = new byte[hex.length() / 2];
		int temp;
		for (int i = 0; i < bytes.length; i++) {
			temp = digital.indexOf(hex2char[2 * i]) * 16;
			temp += digital.indexOf(hex2char[2 * i + 1]);
			bytes[i] = (byte) (temp & 0xff);
		}
		return new String(bytes);
	}

	/** 
	 * java字节码转字符串 
	 * @param bts
	 * @return 
	 */

	public static String byteToHex(byte[] bts) {
		String des = "";
		String tmp = null;
		for (int i = 0; i < bts.length; i++) {
			tmp = (Integer.toHexString(bts[i] & 0xFF));
			if (tmp.length() == 1) {
				des += "0";
			}
			des += tmp;
		}
		return des;
	}

	/**
	 * hex2byte.
	 *
	 * @param hexStr hexStr
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

	/**
	 * 字符串转java字节码
	 * @param b
	 * @return
	 */
	public static byte[] hexToByte(byte[] b) {
		if ((b.length % 2) != 0) {
			throw new IllegalArgumentException("长度不是偶数");
		}
		byte[] b2 = new byte[b.length / 2];
		for (int n = 0; n < b.length; n += 2) {
			String item = new String(b, n, 2);
			// 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个进制字节
			b2[n / 2] = (byte) Integer.parseInt(item, 16);
		}
		b = null;
		return b2;
	}
}
