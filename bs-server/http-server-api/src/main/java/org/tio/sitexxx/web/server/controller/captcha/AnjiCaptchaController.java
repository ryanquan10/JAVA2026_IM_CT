
/*
 *Copyright © 2018 anji-plus
 *安吉加加信息技术有限公司
 *http://www.anji-plus.com
 *All rights reserved.
 */
package org.tio.sitexxx.web.server.controller.captcha;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.server.annotation.RequestPath;
import org.tio.sitexxx.service.service.captcha.CaptchaLocalService;
import org.tio.utils.resp.Resp;

import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;

/**
 * 图形验证码
 * @author lixinji
 * 2020年12月18日 下午1:40:12
 */
@RequestPath(value = "/anjiCaptcha")
public class AnjiCaptchaController {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(AnjiCaptchaController.class);

	private static final CaptchaService captchaService = CaptchaLocalService.captchaService;

	/**
	 * 获取图片
	 * @param captchaVO
	 * @return
	 * @author lixinji
	 * 2020年12月18日 下午1:40:26
	 */
	@RequestPath(value = "/get")
	public Resp get(CaptchaVO captchaVO) {
		ResponseModel model = captchaService.get(captchaVO);
		if (model.isError()) {
			return Resp.fail(model.getRepMsg());
		}
		return Resp.ok(model.getRepData());
	}

	/**
	 * 前端验证图片
	 * @param captchaVO
	 * @return
	 * @author lixinji
	 * 2020年12月18日 下午1:40:52
	 */
	@RequestPath(value = "/check")
	public Resp check(CaptchaVO captchaVO) {
		ResponseModel model = captchaService.check(captchaVO);
		if (model.isError()) {
			return Resp.fail(model.getRepMsg());
		}
		return Resp.ok(model.getRepData());
	}

	/**
	 * 服务端验证图片-客户端验证服务，不建议使用
	 * @param captchaVO
	 * @return
	 * @author lixinji
	 * 2020年12月18日 下午1:41:01
	 */
	@RequestPath(value = "/verify")
	public Resp verify(CaptchaVO captchaVO) {
		ResponseModel model = captchaService.verification(captchaVO);
		if (model.isError()) {
			return Resp.fail(model.getRepMsg());
		}
		return Resp.ok(model.getRepData());
	}

}
