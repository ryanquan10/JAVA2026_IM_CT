
package org.tio.sitexxx.service.api.sms.impl;

import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.tio.sitexxx.service.api.sms.BaseSmsIntf;
import org.tio.sitexxx.service.api.sms.BaseSmsResultVo;
import org.tio.sitexxx.service.api.sms.BaseSmsVo;
import org.tio.sitexxx.service.api.sms.CheckSumBuilder;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.vo.SentSmsResultVo;
import org.tio.utils.jfinal.P;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import org.springframework.http.HttpEntity;
import com.alibaba.fastjson.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
/**
 * 阿里云短信实现接口
 * 
 * @author lixinji
 * 2020年12月18日 下午1:47:18
 */
public class YunSmsImpl implements BaseSmsIntf<BaseSmsVo, BaseSmsResultVo> {
	private static Logger log = LoggerFactory.getLogger(YunSmsImpl.class);

	// 发送验证码的URL
	private static final String SERVER_URL = "https://api.netease.im/sms/sendcode.action";
	// 校验验证码的URL
	private static final String VERIFY_URL = "https://api.netease.im/sms/verifycode.action";
	// 网易云信分配的账号
//	private static final String APP_KEY = "46c0c96d306fee3bbda484a8c6b525d9";
	private static final String APP_KEY = P.get("yun.appkey");
	// 网易云信分配的密钥
//	private static final String APP_SECRET = "83f3e190f65e";
	private static final String APP_SECRET = P.get("yun.appsecret");;
	// 随机数（认证人的生日）
//	private static final String NONCE = "123456";
	private static final String NONCE = P.get("yun.nonce");;
	// 短信模板ID
//	private static final String TEMPLATE_ID = "22536649";
	private static final String TEMPLATE_ID = P.get("yun.templateid");;
	// 验证码长度，范围4～10，默认为4
	private static final String CODE_LEN = "6";

	/**
	 * 发送短信
	 * @param mobile
	 * @param template
	 * @param param
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年12月18日 下午1:47:39
	 */
	private BaseSmsResultVo sendSms(String mobile, String template, Map<String, String> param) throws Exception {
		BaseSmsResultVo sentResultVo = new BaseSmsResultVo();
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(SERVER_URL);
		String curTime = String.valueOf((new Date()).getTime() / 1000L);
		/*
		 * 参考计算CheckSum的java代码，在上述文档的参数列表中，有CheckSum的计算文档示例
		 */
		String checkSum = CheckSumBuilder.getCheckSum(APP_SECRET, NONCE, curTime);

		// 设置请求的header
		httpPost.addHeader("AppKey", APP_KEY);
		httpPost.addHeader("Nonce", NONCE);
		httpPost.addHeader("CurTime", curTime);
		httpPost.addHeader("CheckSum", checkSum);
		httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

		// 设置请求的的参数，requestBody参数
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		/*
		 * 1.如果是模板短信，请注意参数mobile是有s的，详细参数配置请参考“发送模板短信文档”
		 * 2.参数格式是jsonArray的格式，例如 "['13888888888','13666666666']"
		 * 3.params是根据你模板里面有几个参数，那里面的参数也是jsonArray格式
		 */
		nvps.add(new BasicNameValuePair("templateid", TEMPLATE_ID));
		nvps.add(new BasicNameValuePair("mobile", mobile));
		nvps.add(new BasicNameValuePair("codeLen", CODE_LEN));

		httpPost.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));

		// 执行请求
		HttpResponse httpResponse = httpClient.execute(httpPost);
		/*
		 * 1.打印执行结果，打印结果一般会200、315、403、404、413、414、500
		 * 2.具体的code有问题的可以参考官网的Code状态表
		 */
//		System.out.println(EntityUtils.toString(httpResponse.getEntity(), "utf-8"));
		log.info(EntityUtils.toString(httpResponse.getEntity(), "utf-8"));
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			sentResultVo.setSuccess(true);
			sentResultVo.setThirdCode(SentSmsResultVo.ThreeCode.SUCCESS);
		} else {
			sentResultVo.setSuccess(false);
			sentResultVo.setCode(SentSmsResultVo.Code.OTHER);
		}
		return sentResultVo;
	}


	// 检验验证码是否有效
	public Boolean verifyCode(String phone, String code) throws IOException {

		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(VERIFY_URL);
		String curTime = String.valueOf((new Date()).getTime() / 1000L);
		/*
		 * 参考计算CheckSum的java代码，在上述文档的参数列表中，有CheckSum的计算文档示例
		 */
		String checkSum = CheckSumBuilder.getCheckSum(APP_SECRET, NONCE, curTime);

		// 设置请求的header
		httpPost.addHeader("AppKey", APP_KEY);
		httpPost.addHeader("Nonce", NONCE);
		httpPost.addHeader("CurTime", curTime);
		httpPost.addHeader("CheckSum", checkSum);
		httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

		// 设置请求的的参数，requestBody参数
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		/*
		 * 1.如果是模板短信，请注意参数mobile是有s的，详细参数配置请参考“发送模板短信文档”
		 * 2.参数格式是jsonArray的格式，例如 "['13888888888','13666666666']"
		 * 3.params是根据你模板里面有几个参数，那里面的参数也是jsonArray格式
		 */
		nvps.add(new BasicNameValuePair("code", code));
		nvps.add(new BasicNameValuePair("mobile", phone));

		httpPost.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));

		// 执行请求
		HttpResponse httpResponse = httpClient.execute(httpPost);
		/*
		 * 1.打印执行结果，打印结果一般会200、315、403、404、413、414、500
		 * 2.具体的code有问题的可以参考官网的Code状态表
		 */
//		System.out.println(EntityUtils.toString(httpResponse.getEntity(), "utf-8"));
		String res = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
		JSONObject jsonObject = JSONObject.parseObject(res);

//
//
//		// 添加请求体
//		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
//		body.add("mobile", phone);
//		body.add("code", code);
//
//		// 添加请求头
//		HttpHeaders headers = new HttpHeaders();
//		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//		headers.add("AppKey", APP_KEY);
//		headers.add("Nonce", NONCE);
//		headers.add("CurTime", curTime);
//		headers.add("CheckSum", checkSum);
//
//		// 封装请求头和请求体
//		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
//		// 发送请求，获取到返回结果
//		ResponseEntity<JSONObject> response = new RestTemplate().postForEntity(VERIFY_URL, request, JSONObject.class);
//		// 获取到结果里面的Body内容
//		JSONObject object = response.getBody();
//		assert object != null;
//		// 如果code为200说明为true，否则为false
		return jsonObject.get("code").toString().equals("200");
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
		return sendSms(smsVo.getMobile(), getTemplate(smsVo.getBizType()), smsVo.getExtParams());
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
		return sendSms(smsVo.getMobile(), getTemplate(smsVo.getBizType()), smsVo.getExtParams());
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
