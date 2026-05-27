
/**
 * 
 */
package org.tio.sitexxx.service.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.utils.hutool.StrUtil;

public class LogUtils {
	private static Logger log = LoggerFactory.getLogger(LogUtils.class);

	private static long lastLogTime = System.currentTimeMillis();

	/**
	 * 打印jvm启动时间，大于2秒的用warn级别
	 * @param name
	 */
	public static void logJvmStartTime(String name) {
		RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
		long startTime = runtimeMxBean.getStartTime();
		long cost = System.currentTimeMillis() - lastLogTime;
		if (cost > 2000) {
			log.warn("Jvm start time:{}ms, 耗时:{}, {}", System.currentTimeMillis() - startTime, cost, name);
		} else {
			log.info("Jvm start time:{}ms, 耗时:{}, {}", System.currentTimeMillis() - startTime, cost, name);
		}

		lastLogTime = System.currentTimeMillis();
	}

	public static Logger getUserLog() {
		Logger logger = LoggerFactory.getLogger("userOper");
		return logger;
	}

	public static Logger getMobileLog() {
		Logger logger = LoggerFactory.getLogger("mobileLog");
		return logger;
	}

	/**
	 * 跟钱相关的日志，跟钱相关的日志都会记录在数据库中，如果数据库记录失败则记录到日志中。
	 * @return
	 */
	public static Logger getCoinLog() {
		Logger logger = LoggerFactory.getLogger("coinLog");
		return logger;
	}

	/**
	 * 
	 * @param content
	 * @param param
	 */
	public static void logim(String content, String param) {
		Logger logger = LoggerFactory.getLogger("imLog");
		StrUtil.log(content, param, logger);
	}

	/**
	 * 
	 * @param bodyStr
	
	 */
	public static void logim(String bodyStr) {
		//		if (StrUtil.isNotBlank(bodyStr)) {
		//			JSONArray array = JSON.parseArray(bodyStr);
		//			if (array != null && array.size() == 2) {
		//				logim((String) array.get(0), (String) array.get(1));
		//			}
		//		}
	}

	/**
	 * 
	 * @return
	 */
	public static Logger getQuerysqlLog() {
		Logger logger = LoggerFactory.getLogger("querysqlLog");
		return logger;
	}

	public static Logger getUpdatesqlLog() {
		Logger logger = LoggerFactory.getLogger("updatesqlLog");
		return logger;
	}

	/**
	 * @return
	 */
	public static Logger getHttprequestLog() {
		Logger logger = LoggerFactory.getLogger("httprequestLog");
		return logger;
	}

	public static Logger getTcpLog() {
		Logger logger = LoggerFactory.getLogger("tcpLog");
		return logger;
	}

	public static Logger getChatMsgLog() {
		Logger logger = LoggerFactory.getLogger("msgLog");
		return logger;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//		getUserLog().error("666666666666666");
		//		getMobileLog().error("getMobileLog");
	}

	/**
	 * 
	 */
	public LogUtils() {

	}
}
