
package org.tio.sitexxx.service.api.sms.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URL;
import org.tio.sitexxx.service.api.sms.BaseSmsIntf;
import org.tio.sitexxx.service.api.sms.BaseSmsResultVo;
import org.tio.sitexxx.service.api.sms.BaseSmsVo;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.model.conf.ClientConf;
import org.tio.sitexxx.service.vo.SentSmsResultVo;
import org.tio.utils.jfinal.P;
import org.tio.utils.json.Json;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;

/**
 * 短信宝短信实现接口
 * 
 * @author joker
 * 2020年12月18日 下午1:47:18
 */
public class SmsbaoSmsImpl implements BaseSmsIntf<BaseSmsVo, BaseSmsResultVo> {

	/** The username. */
	private static String username = P.get("sms.smsbao.keyid");

	/** The password. */
	private static String password = P.get("sms.smsbao.keysecret");

	private static Logger log = LoggerFactory.getLogger(SmsbaoSmsImpl.class);
	/**
	 * 签名
	 */
	private static String signName = P.get("sms.smsbao.signName");

	/**
	 * 发送短信
	 * @param mobile
	 * @param signName
	 * @param template
	 * @param param
	 * @return
	 * @author joker
	 * 2020年12月18日 下午1:47:39
	 */
	private BaseSmsResultVo sendSms(String mobile, String signName, String template, Map<String, String> param) throws UnsupportedEncodingException {
		BaseSmsResultVo sentResultVo = new BaseSmsResultVo();
		String httpUrl = "http://api.smsbao.com/sms";
		if ("2".equals(param.get("type"))) {
			httpUrl = "http://api.smsbao.com/wsms";
			mobile = URLEncoder.encode("+" + mobile, "UTF-8");
		}
		StringBuffer httpArg = new StringBuffer();
		String content = "【"+signName+"】" + template;
		for(Map.Entry<String, String> entry: param.entrySet()) {
			if (!entry.getKey().equals("type")) {
				String p = "{"+entry.getKey()+"}";
				content = content.replace(p, entry.getValue());
			}
		}
        httpArg.append("u=").append(username).append("&");
        httpArg.append("p=").append(SecureUtil.md5(password)).append("&");
        httpArg.append("m=").append(mobile).append("&");
        httpArg.append("c=").append(encodeUrlString(content, "UTF-8"));

		BufferedReader reader = null;
		String result = null;
		StringBuffer sbf = new StringBuffer();
		httpUrl = httpUrl + "?" + httpArg;
		try {
			URL url = new URL(httpUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();
			InputStream is = connection.getInputStream();
			reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String strRead = reader.readLine();
			if (strRead != null) {
				sbf.append(strRead);
				while ((strRead = reader.readLine()) != null) {
					sbf.append("\n");
					sbf.append(strRead);
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
			return sentResultVo;
		}
		result = sbf.toString();
		sentResultVo.setThirdCode(result);
		sentResultVo.setSuccess(true);
		return sentResultVo;
	}

	public static String encodeUrlString(String str, String charset) {
		String strret = null;
		if (str == null)
			return str;
		try {
			strret = java.net.URLEncoder.encode(str, charset);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return strret;
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
		String templateCode = P.get("sms.smsbao.templateCode." + bizStr);
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
