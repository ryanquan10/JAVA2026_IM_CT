
package org.tio.sitexxx.web.server.controller.base.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.kit.Ret;
import org.tio.sitexxx.service.api.sms.BaseSmsVo;
import org.tio.sitexxx.service.model.conf.ClientConf;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.UserThird;
import org.tio.sitexxx.service.service.base.UserService;
import org.tio.sitexxx.service.service.base.UserThirdService;
import org.tio.sitexxx.service.service.base.sms.SmsService;
import org.tio.sitexxx.service.service.captcha.CaptchaLocalService;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.web.server.controller.base.thirdlogin.ThirdLoginFactory;
import org.tio.sitexxx.web.server.utils.WebUtils;
import org.tio.utils.resp.Resp;

import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;

import cn.hutool.core.lang.Validator;

import java.util.HashMap;
import java.util.Objects;

/**
 * 
 * 短信
 * @author lixinji
 * 2020年12月18日 下午1:39:38
 */
@RequestPath(value = "/sms")
public class SmsController {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(SmsController.class);

	private static final CaptchaService captchaService = CaptchaLocalService.captchaService;

	/**
	 * @param args
	 * @author tanyaowu
	 */
	public static void main(String[] args) {

	}

	/**
	 *
	 * @author tanyaowu
	 */
	public SmsController() {
	}



	/**
	 * 发送短信
	 * @param type
	 * @param request
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年12月10日 下午2:59:54
	 */
	@RequestPath(value = "/send")
	public Resp send(CaptchaVO captchaVO, Short biztype, String mobile, String type, HttpRequest request) throws Exception {
		ClientConf clientConf = ClientConf.dao.findFirst("select * from client_conf where name = 'isOpenVerifyCode'");
		if(!Const.SMS_OPENFLAG || clientConf.getValue().equals(0)) {
			return Resp.ok(RetUtils.OPER_RIGHT);
		}
		if (biztype == null || mobile == null || captchaVO == null) {
			return RetUtils.getInvalidResp();
		}
//		ResponseModel model = captchaService.verification(captchaVO);
//		if (model.isError()) {
//			return Resp.fail(model.getRepMsg());
//		}
		String ip = request.getClientIp();
		String sessionid = request.getHttpSession().getId();
		String referer = request.getReferer();
		HashMap<String, String> extParams = new HashMap<>();
		extParams.put("type", type);
		Ret ret = SmsService.me.send(mobile, biztype, ip, sessionid, referer, "", extParams);
		if (ret.isFail()) {
			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}
		//		return Resp.ok(RetUtils.getOkData(ret));
		return Resp.ok(RetUtils.OPER_RIGHT);
	}

	/**
	 * 手机短信前检查
	 * @param biztype
	 * @param mobile
	 * @param request
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年12月16日 下午4:06:10
	 */
	@RequestPath(value = "/beforeCheck")
	public Resp beforeCheck(Short biztype, String mobile, HttpRequest request) throws Exception {
		return bizPhoneCheck(biztype, mobile, request);
	}

	/**
	 * 验证短信code
	 * @param code
	 * @param request
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年12月10日 下午2:59:56
	 */
	@RequestPath(value = "/check")
	public Resp check(Short biztype, String mobile, String code, HttpRequest request) throws Exception {
		ClientConf clientConf = ClientConf.dao.findFirst("select * from client_conf where name = 'isOpenVerifyCode'");
		if(!Const.SMS_OPENFLAG || clientConf.getValue().equals(0)) {
			return Resp.ok("验证码正确");
		}

		Ret ret = SmsService.me.checkCode(mobile, biztype, code, null, false);
		if (ret.isFail()) {
			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}
		return Resp.ok("验证码正确");
	}

	/**
	 * @param biztype
	 * @param mobile
	 * @param request
	 * @author lixinji
	 * 2020年12月24日 下午4:56:49
	 * @return 
	 */
	public static Resp bizPhoneCheck(Short biztype, String mobile, HttpRequest request) {
		if(!Const.SMS_OPENFLAG) {
			return Resp.ok();
		}
		if (biztype == null || mobile == null) {
			return RetUtils.getInvalidResp();
		}
		if (!Validator.isMobile(mobile)) {
			return Resp.fail().msg("无效手机号");
		}
		switch (biztype) {
		case BaseSmsVo.BaseSmsBizType.REGISTER:
		case BaseSmsVo.BaseSmsBizType.BIND_NEW_PHONE:
		case BaseSmsVo.BaseSmsBizType.BIND_PHONE:
			User register = UserService.ME.getByPhone(mobile, null);
			if (register != null) {
				return Resp.fail("手机号已注册");
			}
			break;
		case BaseSmsVo.BaseSmsBizType.THIRD_BIND_PHONE:
			User bindUser = UserService.ME.getByPhone(mobile, null);
			if (bindUser != null) {
				User curr = WebUtils.currUser(request);
				String typeSplit = ThirdLoginFactory.getSimilarTypesStr(curr.getThirdtype());
				UserThird check = UserThirdService.me.checkExist(bindUser.getId(), typeSplit);
				if (check != null) {
					return Resp.fail("当前手机号已被其它" + UserThird.getThirdLoginTitle(curr.getThirdtype()) + "绑定,请更换其它手机号");
				}
				return Resp.ok(Const.YesOrNo.YES);
			} else {
				return Resp.ok(Const.YesOrNo.NO);
			}
		case BaseSmsVo.BaseSmsBizType.PWD_BACK:
		case BaseSmsVo.BaseSmsBizType.PWD_PAY_BACK:
		case BaseSmsVo.BaseSmsBizType.PWD_UPDATE:
		case BaseSmsVo.BaseSmsBizType.OLD_PHONE_CHECK:
		case BaseSmsVo.BaseSmsBizType.LOGIN:
		case BaseSmsVo.BaseSmsBizType.LOGOUT:
			User login = UserService.ME.getByPhone(mobile, null);
			if (login == null) {
				return Resp.fail("手机号未注册");
			}
			break;
		default:
		}
		return Resp.ok();
	}

	public static Resp bizPhoneCheckForAbroad(Short biztype, String mobile, HttpRequest request) {
		if(!Const.SMS_OPENFLAG) {
			return Resp.ok();
		}
		if (biztype == null || mobile == null) {
			return RetUtils.getInvalidResp();
		}
		if (!mobile.matches("\\d{6,21}")) {
			return Resp.fail().msg("无效手机号");
		}
		switch (biztype) {
			case BaseSmsVo.BaseSmsBizType.REGISTER:
			case BaseSmsVo.BaseSmsBizType.BIND_NEW_PHONE:
			case BaseSmsVo.BaseSmsBizType.BIND_PHONE:
				User register = UserService.ME.getByPhone(mobile, null);
				if (register != null) {
					return Resp.fail("手机号已注册");
				}
				break;
			case BaseSmsVo.BaseSmsBizType.THIRD_BIND_PHONE:
				User bindUser = UserService.ME.getByPhone(mobile, null);
				if (bindUser != null) {
					User curr = WebUtils.currUser(request);
					String typeSplit = ThirdLoginFactory.getSimilarTypesStr(curr.getThirdtype());
					UserThird check = UserThirdService.me.checkExist(bindUser.getId(), typeSplit);
					if (check != null) {
						return Resp.fail("当前手机号已被其它" + UserThird.getThirdLoginTitle(curr.getThirdtype()) + "绑定,请更换其它手机号");
					}
					return Resp.ok(Const.YesOrNo.YES);
				} else {
					return Resp.ok(Const.YesOrNo.NO);
				}
			case BaseSmsVo.BaseSmsBizType.PWD_BACK:
			case BaseSmsVo.BaseSmsBizType.PWD_PAY_BACK:
			case BaseSmsVo.BaseSmsBizType.PWD_UPDATE:
			case BaseSmsVo.BaseSmsBizType.OLD_PHONE_CHECK:
			case BaseSmsVo.BaseSmsBizType.LOGIN:
			case BaseSmsVo.BaseSmsBizType.LOGOUT:
				User login = UserService.ME.getByPhone(mobile, null);
				if (login == null) {
					return Resp.fail("手机号未注册");
				}
				break;
			default:
		}
		return Resp.ok();
	}
}
