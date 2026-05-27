
package org.tio.sitexxx.service.api.sms.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.sitexxx.service.api.sms.BaseSmsIntf;
import org.tio.sitexxx.service.api.sms.BaseSmsResultVo;
import org.tio.sitexxx.service.api.sms.BaseSmsVo;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.vo.SentSmsResultVo;
import org.tio.utils.jfinal.P;
import org.tio.utils.json.Json;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ErrorType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;

import cn.hutool.core.util.StrUtil;

/**
 * 阿里云短信实现接口
 * 
 * @author lixinji
 * 2020年12月18日 下午1:47:18
 */
public class AliyunSmsImpl implements BaseSmsIntf<BaseSmsVo, BaseSmsResultVo> {

	/** The keyid. */
	private static String keyid = P.get("sms.aliyun.keyid");

	/** The keysecret. */
	private static String keysecret = P.get("sms.aliyun.keysecret");

	private static Logger log = LoggerFactory.getLogger(AliyunSmsImpl.class);

	/**
	 * 
	 */
	private static String regionId = P.get("sms.aliyun.regionId");

	/**
	 * 签名
	 */
	private static String signName = P.get("sms.aliyun.signName");

	/**
	 * 发送短信
	 * @param mobile
	 * @param signName
	 * @param template
	 * @param param
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年12月18日 下午1:47:39
	 */
	private BaseSmsResultVo sendSms(String mobile, String signName, String template, Map<String, String> param) throws Exception {
		BaseSmsResultVo sentResultVo = new BaseSmsResultVo();
		try {
			IClientProfile profile = DefaultProfile.getProfile(regionId, keyid, keysecret);
//			DefaultProfile.addEndpoint("cn-hangzhou", regionId, "Dysmsapi", "dysmsapi.aliyuncs.com");
			DefaultProfile.addEndpoint(regionId, "Dysmsapi", "dysmsapi.aliyuncs.com");

			IAcsClient client = new DefaultAcsClient(profile);
			SendSmsRequest request = new SendSmsRequest();
			//"控制台创建的签名名称"
			request.setSignName(signName);
			//"控制台创建的短信模板名"
			request.setTemplateCode(template);
			request.setTemplateParam(Json.toJson(param));
			request.setPhoneNumbers(mobile);
			SendSmsResponse httpResponse = client.getAcsResponse(request);
			String requestId = httpResponse.getRequestId();
			sentResultVo.setThirdCode(httpResponse.getCode());
			sentResultVo.setThirdMsg(httpResponse.getMessage());
			sentResultVo.setThirdId(requestId);
			sentResultVo.setSuccess(true);
			return sentResultVo;
		} catch (ClientException e) {
			String errorCode = e.getErrCode();
			ErrorType errorType = e.getErrorType();
			String errMsg = e.getErrMsg();
			String requestId = e.getRequestId();
			log.error("阿里云短信发送失败,errorType:" + errorType + ", errorCode:" + errorCode + ", errMsg:" + errMsg, e);
			sentResultVo.setThirdCode(errorCode);
			sentResultVo.setThirdMsg(errMsg);
			sentResultVo.setThirdId(requestId);
			sentResultVo.setSuccess(false);
			sentResultVo.setCode(SentSmsResultVo.Code.OTHER);
			return sentResultVo;
		}
	}

	/**
	 * 获取模板
	 * @param bizType
	 * @return
	 * @author lixinji
	 * 2020年12月16日 上午5:14:34
	 */
	private String getTemplate(Short bizType) {
		String bizStr = "";
		switch (bizType) {
		case BaseSmsVo.BaseSmsBizType.LOGIN:
			bizStr = "login";
			break;
		case BaseSmsVo.BaseSmsBizType.REGISTER:
			bizStr = "register";
			break;
		case BaseSmsVo.BaseSmsBizType.PWD_UPDATE:
			bizStr = "updatepwd";
			break;
		case BaseSmsVo.BaseSmsBizType.BIND_PHONE:
			bizStr = "bindphone";
			break;
		case BaseSmsVo.BaseSmsBizType.OLD_PHONE_CHECK:
			bizStr = "oldphonecheck";
			break;
		case BaseSmsVo.BaseSmsBizType.PWD_BACK:
			bizStr = "pwdback";
			break;
		case BaseSmsVo.BaseSmsBizType.BIND_NEW_PHONE:
			bizStr = "bindnewphone";
			break;
		case BaseSmsVo.BaseSmsBizType.THIRD_BIND_PHONE:
			bizStr = "thirdbind";
			break;
		case BaseSmsVo.BaseSmsBizType.PWD_PAY_BACK:
			bizStr = "resetpaypwd";
			break;
		case BaseSmsVo.BaseSmsBizType.LOGOUT:
			bizStr = "logout";
			break;
		default:
			return "";
		}
		String templateCode = P.get("sms.aliyun.templateCode." + bizStr);
		return templateCode;
	}

	/* 
	 * 见接口定义
	 */
	@Override
	public BaseSmsVo initSmsVo(String mobile, Short bizType, String code, Map<String, String> extParams) {
		BaseSmsVo baseSmsVo = new BaseSmsVo();
		baseSmsVo.setMobile(mobile);
		baseSmsVo.setBizType(bizType);
		baseSmsVo.setCode(code);
		if (extParams == null) {
			baseSmsVo.setExtParams(new HashMap<String, String>());
		} else {
			baseSmsVo.setExtParams(extParams);
		}
		baseSmsVo.setCodeAdd(false);
		return baseSmsVo;
	}

	/* 
	 * 见接口定义
	 */
	@Override
	public BaseSmsResultVo sendCode(BaseSmsVo smsVo, String code) throws Exception {
		BaseSmsResultVo baseSmsResultVo = new BaseSmsResultVo();
		String check = checkSmsVo(smsVo);
		if (StrUtil.isNotBlank(check)) {
			baseSmsResultVo.setThirdMsg(check);
			baseSmsResultVo.setSuccess(false);
			return baseSmsResultVo;
		}
		smsVo.getExtParams().put("code", code);
		return sendSms(smsVo.getMobile(), signName, getTemplate(smsVo.getBizType()), smsVo.getExtParams());
	}

	/* 
	 * 见接口定义
	 */
	@Override
	public BaseSmsResultVo send(BaseSmsVo smsVo) throws Exception {
		BaseSmsResultVo baseSmsResultVo = new BaseSmsResultVo();
		String check = checkSmsVo(smsVo);
		if (StrUtil.isNotBlank(check)) {
			baseSmsResultVo.setThirdMsg(check);
			baseSmsResultVo.setSuccess(false);
			return baseSmsResultVo;
		}
		return sendSms(smsVo.getMobile(), signName, getTemplate(smsVo.getBizType()), smsVo.getExtParams());
	}

	/* 
	 * 见接口定义
	 */
	@Override
	public String getCode(BaseSmsVo smsVo, boolean isAdd) throws Exception {
		String check = checkSmsVo(smsVo);
		if (StrUtil.isNotBlank(check)) {
			return check;
		}
		String code = (String) Caches.getCache(CacheConfig.SMS_MOBILE_CODE).get(smsVo.getCodeCacheKey());
		if (isAdd && StrUtil.isBlank(code)) {
			code = RandomUtils.nextInt(100000, 999999) + "";
			smsVo.setCodeAdd(true);
		} else {
			smsVo.setCodeAdd(false);
		}
		smsVo.setCode(code);
		return code;
	}

	/* 
	 * 见接口定义
	 */
	@Override
	public BaseSmsResultVo updateCode(BaseSmsVo smsVo) throws Exception {
		BaseSmsResultVo baseSmsResultVo = new BaseSmsResultVo();
		String check = checkSmsVo(smsVo);
		if (StrUtil.isNotBlank(check)) {
			baseSmsResultVo.setThirdMsg(check);
			baseSmsResultVo.setSuccess(false);
			return baseSmsResultVo;
		}
		if (smsVo.isCodeAdd()) {
			Caches.getCache(CacheConfig.SMS_MOBILE_CODE).put(smsVo.getCodeCacheKey(), smsVo.getCode());
		}
		baseSmsResultVo.setSuccess(true);
		return baseSmsResultVo;
	}

	/* 
	 * 见接口定义
	 */
	@Override
	public BaseSmsResultVo delCode(BaseSmsVo smsVo) throws Exception {
		BaseSmsResultVo baseSmsResultVo = new BaseSmsResultVo();
		String check = checkSmsVo(smsVo);
		if (StrUtil.isNotBlank(check)) {
			baseSmsResultVo.setThirdMsg(check);
			baseSmsResultVo.setSuccess(false);
			return baseSmsResultVo;
		}
		Caches.getCache(CacheConfig.SMS_MOBILE_CODE).remove(smsVo.getCodeCacheKey());
		baseSmsResultVo.setSuccess(true);
		return baseSmsResultVo;
	}

	@Override
	public Boolean verifyCode(String mobile, String code) {
		return null;
	}

	/**
	 * vo检查
	 * @param smsVo
	 * @return
	 * @author lixinji
	 * 2020年12月16日 上午5:28:44
	 */
	private String checkSmsVo(BaseSmsVo smsVo) {
		if (smsVo == null || StrUtil.isBlank(smsVo.getMobile()) || smsVo.getBizType() == null) {
			return "无效参数";
		}
		return "";
	}
}
