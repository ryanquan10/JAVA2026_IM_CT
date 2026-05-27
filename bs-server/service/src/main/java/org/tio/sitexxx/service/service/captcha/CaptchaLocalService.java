
package org.tio.sitexxx.service.service.captcha;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anji.captcha.service.CaptchaService;

/**
 * 图形本地服务
 * @author lixinji
 * 2020年2月13日 下午7:01:05
 */
public class CaptchaLocalService {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(CaptchaLocalService.class);

	public static final CaptchaLocalService me = new CaptchaLocalService();

	public static final CaptchaService captchaService = CaptchaConfig.captchaService();

}
