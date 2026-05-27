
package org.tio.mg.service.utils;

import cn.hutool.core.util.StrUtil;
import net.sourceforge.pinyin4j.PinyinHelper;

/**
 * 拼音工具类
 * 
 */
public class PyUtils {

	/**
	 * 获取字符首字母
	 * 1、汉字取第一个汉字的拼音首字母
	 * 2、字母取第一个字母
	 * 3、特殊符号、数字等其他字符返回ZA
	 * @param str
	 * @return
	 * @author xufei
	 * 2020年2月21日 上午12:03:05
	 */
	public static String getFristChat(String str) {
		if(StrUtil.isBlank(str)) {
			return "ZA";
		}
		String fristChat = "";
		if (String.valueOf(str.charAt(0)).matches("[\\u4E00-\\u9FA5]+")) {
			String[] pinChar = PinyinHelper.toHanyuPinyinStringArray(str.charAt(0));
			if(pinChar == null || StrUtil.isBlank(pinChar[0])) {
				return "ZA";
			}
			fristChat = pinChar[0].substring(0,1).toUpperCase();
		} else {
			fristChat = str.substring(0, 1).toUpperCase();
		}
		if(fristChat.matches("[a-zA-Z]+")) {
			return fristChat;
		}
		return "ZA";
	}
	
	public static String getAllChat(String str) {
		if(StrUtil.isBlank(str)) {
			return "";
		}
		char[] allChars = str.trim().toCharArray();
		String allStr = "";
		for(char chars : allChars) {
			if (String.valueOf(chars).matches("[\\u4E00-\\u9FA5]+")) {
				String[] pinChar = PinyinHelper.toHanyuPinyinStringArray(chars);
				if(pinChar == null || StrUtil.isBlank(pinChar[0])) {
					allStr += "ZA";
				}
				allStr += pinChar[0].toUpperCase();
			} else {
				allStr += chars;
			}
		}
		return allStr;
	}
	
	public static void main(String[] args) {
		System.out.println(getFristChat("徐飞¤行飞12345"));
	}

}
