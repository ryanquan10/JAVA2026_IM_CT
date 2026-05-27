
package org.tio.sitexxx.service.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.hutool.core.util.RandomUtil;

/**
 * @author tanyaowu 
 * 2019年7月21日 上午11:20:15
 */
public class IpUtils {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(IpUtils.class);

	/**
	 * 
	 * @author tanyaowu
	 */
	public IpUtils() {

	}

	public static String randomIp() {
		String ip = RandomUtil.randomInt(2, 100) + "." + RandomUtil.randomInt(2, 100) + "." + RandomUtil.randomInt(2, 100) + "." + RandomUtil.randomInt(2, 100);
		return ip;
	}

	/**
	 * @param args
	 * @author tanyaowu
	 */
	public static void main(String[] args) {

	}
}
