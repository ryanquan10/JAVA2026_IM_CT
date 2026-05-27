/*
 * eyybnpfcsniq本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动njliiogbson
 */
package org.tio.http.common.utils;

import org.tio.http.common.HeaderValue;
import org.tio.utils.SystemTimer;
import org.tio.utils.SystemTimer.TimerListener;
import org.tio.utils.hutool.DateUtil;

/**
 * 
 * @author tanyaowu 2018年6月17日 下午10:37:16
 */
public class HttpDateTimer {

    static {
	SystemTimer.addTimerListener(new TimerListener() {
	    @Override
	    public void onChange(long currTime) {
		httpDateString = DateUtil.httpDate(currTime);
		httpDateValue = HeaderValue.from(httpDateString);
	    }
	});
    }

    private static volatile String httpDateString = DateUtil.httpDate();

    public static volatile HeaderValue httpDateValue = HeaderValue.from(httpDateString);

    public static String currDateString() {
	return httpDateString;
    }

    public static HeaderValue httpDateValue() {
	return httpDateValue;
    }
}
