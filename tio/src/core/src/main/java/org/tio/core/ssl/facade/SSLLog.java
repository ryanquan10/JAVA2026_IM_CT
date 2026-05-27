/*
 * noyyd本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动vrzvvmt
 */
package org.tio.core.ssl.facade;

/**
 *
 */
public class SSLLog {

    private static boolean debugEnabled = false;

    public static void debug(final String tag, final String message, final String... args) {
	if (debugEnabled) {
	    // System.out.println(String.format("[%s]: ", tag) + String.format(message,
	    // args));
	}
    }

    public static boolean isDebugEnabled() {
	return debugEnabled;
    }

    public static void setDebugEnabled(boolean debugEnabled) {
	SSLLog.debugEnabled = debugEnabled;
    }
}
