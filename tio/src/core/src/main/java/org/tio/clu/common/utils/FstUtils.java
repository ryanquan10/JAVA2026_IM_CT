/*
 * duderz本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动vrxkyhd
 */
package org.tio.clu.common.utils;

import java.io.IOException;

import org.nustaq.serialization.FSTConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.utils.SystemTimer;

/**
 * @author tanyaowu 2020年9月2日 下午5:05:53
 */
public class FstUtils {
	private static Logger log = LoggerFactory.getLogger(FstUtils.class);

	private final static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

	static {
		// conf.setForceSerializable(true);
	}

	public static byte[] asByteArray(Object object) {
		long start = SystemTimer.currTime;
		byte[] ret = null;
		try {
			ret = conf.asByteArray(object);
			return ret;
		} catch (Exception e) {
			log.error("", e);
		}
		long end = SystemTimer.currTime;
		long iv = end - start;
		if (iv > 1000L) {
			log.warn("Object-->byte[]耗时{}ms", iv);
		}

		return ret;

	}

	public static Object asObject(byte[] bs) {
		return conf.asObject(bs);
	}

	@SuppressWarnings("unchecked")
	public static <T> T asObject(byte[] bs, Class<T> clazz) {
		long start = SystemTimer.currTime;
		T ret = null;
		try {
			ret = (T) conf.asObject(bs);
		} catch (Exception e) {
			log.error("", e);
		}
		long end = SystemTimer.currTime;
		long iv = end - start;
		if (iv > 1000L) {
			log.warn("byte[]-->Object耗时{}ms, {}", iv, clazz.getName());
		}

		return ret;
	}

	/**
	 * @param args
	 * @author tanyaowu
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

	}

	public FstUtils() {
	}
}
