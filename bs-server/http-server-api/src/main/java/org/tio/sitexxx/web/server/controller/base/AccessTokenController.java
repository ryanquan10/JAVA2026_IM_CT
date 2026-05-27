
package org.tio.sitexxx.web.server.controller.base;

import java.io.Serializable;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.Tio;
import org.tio.http.common.HttpConfig;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.http.server.annotation.RequestPath;
import org.tio.http.server.util.Resps;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.init.PropInit;
import org.tio.sitexxx.service.vo.RequestExt;
import org.tio.sitexxx.web.server.utils.WebUtils;
import org.tio.utils.SystemTimer;
import org.tio.utils.cache.ICache;
import org.tio.utils.crypto.ACEUtils;
import org.tio.utils.crypto.Md5;
import org.tio.utils.jfinal.P;
import org.tio.utils.resp.Resp;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;

/**
 * 
 * @author tanyaowu
 */
@RequestPath(value = "/a")
public class AccessTokenController {
	private static Logger log = LoggerFactory.getLogger(AccessTokenController.class);

	public static class AccessTokenResp1 implements Serializable {
		private static final long serialVersionUID = -4042961444820016173L;

		public AccessTokenResp1(Long t) {
			this.t = t;
		}

		public static final int RANDOM_LEN = 9;
		//x、y、 z、i
		private String	x	= RandomUtil.randomString(RANDOM_LEN);
		private String	y	= RandomUtil.randomString(RANDOM_LEN);
		private String	z	= RandomUtil.randomString(RANDOM_LEN);
		private String	i	= RandomUtil.randomString(RANDOM_LEN);
		private Long	t	= null;

		public String getX() {
			return x;
		}

		public void setX(String x) {
			this.x = x;
		}

		public String getY() {
			return y;
		}

		public void setY(String y) {
			this.y = y;
		}

		public String getZ() {
			return z;
		}

		public void setZ(String z) {
			this.z = z;
		}

		public String getI() {
			return i;
		}

		public void setI(String i) {
			this.i = i;
		}

		public Long getT() {
			return t;
		}

		public void setT(Long t) {
			this.t = t;
		}
	}

	//	public static class AccessTokenResp2 implements Serializable {
	//		private String x = RandomUtil.randomUUID();
	//
	//		public String getX() {
	//			return x;
	//		}
	//
	//		public void setX(String x) {
	//			this.x = x;
	//		}
	//	}

	/**
	 * @param args
	 * @author tanyaowu
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		PropInit.init();

		String content = "test中文";

		//随机生成密钥
		byte[] key = SecureUtil.generateKey(SymmetricAlgorithm.AES.getValue()).getEncoded();
		System.out.println(new String(key));

		//构建
		AES aes = SecureUtil.aes(key);

		//加密
		byte[] encrypt = aes.encrypt(content);
		//解密
		byte[] decrypt = aes.decrypt(encrypt);

		//加密为16进制表示
		String encryptHex = aes.encryptHex(content);
		//解密为原字符串
		String decryptStr = aes.decryptStr(encryptHex);

		System.out.println(encryptHex);
		System.out.println(decryptStr);
	}

	/**
	 *
	 * @author tanyaowu
	 */
	public AccessTokenController() {
	}

	public static final String	androidKey1	= P.get("access.token.android.key1");
	public static final String	androidKey2	= P.get("access.token.android.key2");
	public static final String	androidKey3	= P.get("access.token.android.key3");

	public static final String	iosKey1	= P.get("access.token.ios.key1");
	public static final String	iosKey2	= P.get("access.token.ios.key2");
	public static final String	iosKey3	= P.get("access.token.ios.key3");

	public static final String	pcKey1	= P.get("access.token.pc.key1");
	public static final String	pcKey2	= P.get("access.token.pc.key2");
	public static final String	pcKey3	= P.get("access.token.pc.key3");

	public static final long MAX_TIME_INTERVAL = 3600 * 24 * 1000;

	private static void error(HttpRequest request, String msg) {
		log.error(request.getClientIp() + "\r\n" + request.getRequestLine() + "\r\n" + msg);
	}

	/**
	 * 获取令牌的第一次接口
	 * @param request
	 * @param r 9位随机字符
	 * @param t 客户端的当前时间，单位：毫秒
	 * @param s 签名。签名规则：md5("${" + key1 + r + t + "}")，其中key1由服务器给出
	 * @return 响应体数据会有如下字段：x、y、 z、i，4个字段字段，拿到这些字段后，必须在5秒内调用第二个接口
	 * @throws Exception
	 */
	@RequestPath(value = "/x")
	public HttpResponse step1(HttpRequest request, String r, Long t, String s) throws Exception {
		if (t == null) {
			error(request, "参数t为空");
			Tio.remove(request.getChannelContext(), "");
			return null;
		}

		if (r == null || r.length() != 9) {
			error(request, "参数r为空或其长度不为9");
			Tio.remove(request.getChannelContext(), "");
			return null;
		}

		long time = SystemTimer.currTime;
		if (Math.abs(time - t) > MAX_TIME_INTERVAL) {
			error(request, "参数t与服务器时间相隔超过1天");
			Tio.remove(request.getChannelContext(), "");
			return null;
		}

		if (StrUtil.isBlank(s)) {
			error(request, "参数s为空");
			Tio.remove(request.getChannelContext(), "");
			return null;
		}

		RequestExt requestExt = WebUtils.getRequestExt(request);

		String key1 = getKey1(request, requestExt);

		String sign = Md5.getMD5("${" + key1 + r + t + "}");
		if (!Objects.equals(sign, s)) {
			error(request, "验签失败");
			Tio.remove(request.getChannelContext(), "");
			return null;
		}
		AccessTokenResp1 accessTokenResp1 = new AccessTokenResp1(t);
		ICache cache = Caches.getCache(CacheConfig.ACCESS_TOKEN_RESP_1);
		cache.put(accessTokenResp1.getX() + request.getClientIp() + accessTokenResp1.getY(), accessTokenResp1);

		HttpResponse ret = Resps.json(request, Resp.ok(accessTokenResp1));
		return ret;
	}

	/**
	 * 获取令牌的第二次接口
	 * 调用这个请求前必须先调"获取令牌的第一次接口"
	 * @param request
	 * @param x 第一次接口服务器返回的y（看清了，不是x，而是y）
	 * @param y 第一次接口服务器返回的x（看清了，不是y，而是x）
	 * @param z 第一次接口服务器返回的i（看清了，不是z，而是i）
	 * @param i 第一次接口服务器返回的z（看清了，不是i，而是z）
	 * @param t 客户端的当前时间，单位：毫秒，此值必须大于第一次传递的t，但两者相隔不能大于5000（即5秒）
	 * @param s 签名。签名规则：md5("${" + key2 + x + z + "}")，其中x是第一次请求服务器返回的x，z是第一次请求服务器返回的z，key2由服务器给出
	 * @return 
	 * 响应体数据(即data字段)是个字符串zzz，客户端拿到这个字符串后，需要自己进行如下计算获取一个值
	 * xxxx = aesDecrypt(zzz, key3, key3)，(aesDecrypt是aes解密的意思，第二个参数是key, 第三个参数是iv)以后访问服务器的接口时，带上name为tPiJo, value为xxxx的cookie
	 * @throws Exception
	 */
	@RequestPath(value = "/y")
	public HttpResponse step2(HttpRequest request, String x, String y, String z, String i, Long t, String s) throws Exception {
		if (t == null) {
			error(request, "参数t为空");
			Tio.remove(request.getChannelContext(), "");
			return null;
		}

		if (StrUtil.isBlank(x) || x.length() != AccessTokenResp1.RANDOM_LEN) {
			error(request, "参数x为空或长度不对");
			Tio.remove(request.getChannelContext(), "");
			return null;
		}

		if (StrUtil.isBlank(y) || x.length() != AccessTokenResp1.RANDOM_LEN) {
			error(request, "参数y为空或长度不对");
			Tio.remove(request.getChannelContext(), "");
			return null;
		}

		if (StrUtil.isBlank(z) || x.length() != AccessTokenResp1.RANDOM_LEN) {
			error(request, "参数z为空或长度不对");
			Tio.remove(request.getChannelContext(), "");
			return null;
		}

		if (StrUtil.isBlank(i) || x.length() != AccessTokenResp1.RANDOM_LEN) {
			error(request, "参数i为空或长度不对");
			Tio.remove(request.getChannelContext(), "");
			return null;
		}

		long time = SystemTimer.currTime;
		if (Math.abs(time - t) > MAX_TIME_INTERVAL) {
			error(request, "参数t与服务器时间相隔超过1天");
			Tio.remove(request.getChannelContext(), "");
			return null;
		}

		if (StrUtil.isBlank(s)) {
			error(request, "参数s为空");
			Tio.remove(request.getChannelContext(), "");
			return null;
		}

		RequestExt requestExt = WebUtils.getRequestExt(request);

		String key2 = getKey2(request, requestExt);

		/*
		 *  x = 第一次接口的y
			y = 第一次接口的x
			z = 第一次接口的i
			i = 第一次接口的z
		 */
		// md5("${" + key2 + x + z + "}")
		// y -> x; i -> z
		String sign = Md5.getMD5("${" + key2 + y + i + "}");
		if (!Objects.equals(sign, s)) {
			error(request, "验签失败");
			Tio.remove(request.getChannelContext(), "");
			return null;
		}

		ICache cache = Caches.getCache(CacheConfig.ACCESS_TOKEN_RESP_1);
		//		cache.put(accessTokenResp1.getX() + request.getClientIp() + accessTokenResp1.getY(), accessTokenResp1);
		AccessTokenResp1 accessTokenResp1 = cache.get(y + request.getClientIp() + x, AccessTokenResp1.class);
		if (accessTokenResp1 == null) {
			error(request, "获取不到AccessTokenResp1，也许已经超时，或者这是个非法请求");
			Tio.remove(request.getChannelContext(), "");
			return null;
		}

		if (!Objects.equals(i, accessTokenResp1.getZ()) || (!Objects.equals(z, accessTokenResp1.getI()))) {
			error(request, "i和z的值不对");
			Tio.remove(request.getChannelContext(), "");
			return null;
		}

		long iv = t - accessTokenResp1.getT();
		if (iv <= 0 || iv > 5000) {
			error(request, "这次传的t和上次的t相隔" + iv + "毫秒");
			Tio.remove(request.getChannelContext(), "");
			return null;
		}

		//		String uuid = RandomUtil.simpleUUID();

		String key3 = getKey3(request, requestExt);
		//		AccessTokenResp2 accessTokenResp2 = new AccessTokenResp2();
		//		String value = Md5.getMD5("${" + accessTokenResp2.getX() + key3 + "}");

		//		HttpConfig httpConfig = request.getHttpConfig();
		//		String xxxx = /**request.getClientIp() + "_" + */httpConfig.getSessionIdGenerator().sessionId(httpConfig, request);//.simpleUUID();//明文
		//		String zzz = ACEUtils.encrypt(xxxx, key3, key3);//密文

		String sessionid = request.getHttpSession().getId();
		ICache cacheTemp = Caches.getCache(CacheConfig.TIO_ACCESS_TOKEN_TEMP);
		String xxxx = cacheTemp.get(sessionid, String.class);//明文
		if (StrUtil.isBlank(xxxx)) {
			HttpConfig httpConfig = request.httpConfig;
			xxxx = httpConfig.getSessionIdGenerator().sessionId(httpConfig, request);
			cacheTemp.put(sessionid, xxxx);
		}
		String zzz = ACEUtils.encrypt(xxxx, key3, key3);//密文

		ICache cache2 = Caches.getCache(CacheConfig.TIO_ACCESS_TOKEN);
		cache2.put(request.getHttpSession().getId(), xxxx); //本地存明文
		HttpResponse ret = Resps.json(request, Resp.ok(zzz)); //给客户端的是密文

		ICache cache3 = Caches.getCache(CacheConfig.TIO_ACCESSTOKEN_USERAGENT);
		cache3.put(xxxx, request.getUserAgent());
		return ret;
	}

	private static String getKey1(HttpRequest request, RequestExt requestExt) {
		boolean isFromAndroid = requestExt.isFromAppAndroid();
		if (isFromAndroid) {
			return androidKey1;
		}

		boolean isFromPc = requestExt.isFromBrowser();
		if (isFromPc) {
			return pcKey1;
		}

		boolean isFromIos = requestExt.isFromAppIos();
		if (isFromIos) {
			return iosKey1;
		}

		Tio.remove(request.getChannelContext(), "这个http请求不来自pc、ios、android任何一方");
		return null;
	}

	private static String getKey2(HttpRequest request, RequestExt requestExt) {
		boolean isFromAndroid = requestExt.isFromAppAndroid();
		if (isFromAndroid) {
			return androidKey2;
		}

		boolean isFromPc = requestExt.isFromBrowser();
		if (isFromPc) {
			return pcKey2;
		}

		boolean isFromIos = requestExt.isFromAppIos();
		if (isFromIos) {
			return iosKey2;
		}

		Tio.remove(request.getChannelContext(), "这个http请求不来自pc、ios、android任何一方");
		return null;
	}

	private static String getKey3(HttpRequest request, RequestExt requestExt) {
		boolean isFromAndroid = requestExt.isFromAppAndroid();
		if (isFromAndroid) {
			return androidKey3;
		}

		boolean isFromPc = requestExt.isFromBrowser();
		if (isFromPc) {
			return pcKey3;
		}

		boolean isFromIos = requestExt.isFromAppIos();
		if (isFromIos) {
			return iosKey3;
		}

		Tio.remove(request.getChannelContext(), "这个http请求不来自pc、ios、android任何一方");
		return null;
	}
}
