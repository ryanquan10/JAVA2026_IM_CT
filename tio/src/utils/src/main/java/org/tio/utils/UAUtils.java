/*
 * rqjuqv本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动rdvuoohh
 */
/**
 * 
 */
package org.tio.utils;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.UserAgentAnalyzer.UserAgentAnalyzerBuilder;
import nl.basjes.parse.useragent.classify.UserAgentClassifier;

/**
 * User-Agent工具类
 * 
 * @author tanyaowu
 *
 */
public class UAUtils {

    private static UserAgentAnalyzer ua = null;

    static {// agent_name agent_version_major operating_system_name operating_system_version
	UserAgentAnalyzerBuilder builder = UserAgentAnalyzer.newBuilder();
	builder.withField(UserAgent.AGENT_NAME);
	builder.withField(UserAgent.AGENT_VERSION_MAJOR);

	builder.withField(UserAgent.OPERATING_SYSTEM_NAME);
	builder.withField(UserAgent.OPERATING_SYSTEM_VERSION);
	//
	builder.withField(UserAgent.DEVICE_CLASS);

	builder.hideMatcherLoadStats();
	builder.withCache(25000);
	builder.withUserAgentMaxLength(1024);

	ua = builder.build();
    }

    /**
     * 是否是
     * 
     * @param userAgent
     * @return
     * @author tanyaowu
     */
    public static boolean isMobile(UserAgent userAgent) {
	return UserAgentClassifier.isMobile(userAgent);
    }

    /**
     * 
     * @param userAgentString
     * @return
     */
    public static UserAgent parse(String userAgentString) {
	return ua.parse(userAgentString);
    }

    /**
     * 
     */
    public UAUtils() {

    }

}
