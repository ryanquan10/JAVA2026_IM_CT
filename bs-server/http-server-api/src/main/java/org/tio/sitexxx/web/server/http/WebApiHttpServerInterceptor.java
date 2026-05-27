
package org.tio.sitexxx.web.server.http;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.Cookie;
import org.tio.http.common.HeaderName;
import org.tio.http.common.HeaderValue;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.http.common.HttpResponseStatus;
import org.tio.http.common.MimeType;
import org.tio.http.common.RequestLine;
import org.tio.http.common.session.HttpSession;
import org.tio.http.common.utils.HttpGzipUtils;
import org.tio.http.server.intf.HttpServerInterceptor;
import org.tio.http.server.util.Resps;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.model.conf.Httpcache;
import org.tio.sitexxx.service.model.main.IpInfo;
import org.tio.sitexxx.service.model.main.LoginLog;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.UserAgent;
import org.tio.sitexxx.service.model.stat.ImeiStat;
import org.tio.sitexxx.service.model.stat.TioIpPullblackLog;
import org.tio.sitexxx.service.model.stat.TioSlowRequest;
import org.tio.sitexxx.service.service.base.ImeiStatService;
import org.tio.sitexxx.service.service.base.IpInfoService;
import org.tio.sitexxx.service.service.base.UserAgentService;
import org.tio.sitexxx.service.service.conf.ConfService;
import org.tio.sitexxx.service.service.conf.HttpcacheService;
import org.tio.sitexxx.service.service.conf.IpWhiteListService;
import org.tio.sitexxx.service.vo.AppCode;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.Devicetype;
import org.tio.sitexxx.service.vo.RequestExt;
import org.tio.sitexxx.service.vo.RequestKey;
import org.tio.sitexxx.service.vo.SessionExt;
import org.tio.sitexxx.web.server.auth.AccessCtrlConfig;
import org.tio.sitexxx.web.server.auth.AccessCtrlService;
import org.tio.sitexxx.web.server.utils.TioIpPullblackUtils;
import org.tio.sitexxx.web.server.utils.WebUtils;
import org.tio.utils.SystemTimer;
import org.tio.utils.cache.ICache;
import org.tio.utils.jfinal.P;
import org.tio.utils.json.Json;
import org.tio.utils.lock.LockUtils;
import org.tio.utils.resp.Resp;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @author tanyaowu
 * 2016年8月3日 下午1:27:05
 */
public class WebApiHttpServerInterceptor implements HttpServerInterceptor {
	private static Logger log = LoggerFactory.getLogger(WebApiHttpServerInterceptor.class);

	public static final WebApiHttpServerInterceptor ME = new WebApiHttpServerInterceptor();

	private final String httpCacheLockKey = this.getClass().getName() + ".httpCacheLockKey";

	/**
	 * 参数名字：传递手机信息，譬如：huawei p6
	 */
	public static final String	HEADER_NAME_MOBILE_DEVICEINFO	= "tio-deviceinfo";
	/**
	 * 参数名字：APP版本号，譬如1.0.0
	 */
	public static final String	HEADER_NAME_TIO_APPVERSION		= "tio-appversion";
	/**
	 * 参数名字：渠道号，譬如baidu
	 */
	public static final String	HEADER_NAME_TIO_CID				= "tio-cid";
	/**
	 * 参数名字：手机分辨率，譬如1080,1344
	 */
	public static final String	HEADER_NAME_TIO_RESOLUTION		= "tio-resolution";
	/**
	 * 参数名字：手机imei
	 */
	public static final String	HEADER_NAME_TIO_IMEI			= "tio-imei";
	/**
	 * 参数名字：运营商:联通,移动
	 */
	public static final String	HEADER_NAME_TIO_OPERATOR		= "tio-operator";
	/**
	 * 参数名字：手机尺寸
	 */
	public static final String	HEADER_NAME_TIO_SIZE			= "tio-size";
	/**
	 * idfa
	 */
	public static final String	HEADER_NAME_TIO_IDFA			= "tio-idfa";

	/**
	 * tio-httpcache-old
	 */
	private static final HeaderName		HTTPCACHE_FLAG_HEADER_NAME	= HeaderName.from("tio-httpcache-old");
	/**
	 * tio-httpcache-new
	 */
	private static final HeaderName		HTTPCACHE_FIRST_HEADER_NAME	= HeaderName.from("tio-httpcache-new");
	/**
	 * tio-webapi-server
	 */
	private static final HeaderName		HEADER_NAME_WEBAPI_SERVER	= HeaderName.from("tio-webapi-server");
	/**
	 * value: Const.MY_IP
	 */
	private static final HeaderValue	HEADER_VALUE_WHICH_API		= HeaderValue.from(Const.MY_IP);

	/**
	 * 安卓APP的每个请求都要传这个参数: p_is_android=1
	 */
	public static final String	PARAM_NAME_IS_FROM_ANDROID	= "p_is_android";
	/**
	 * IOS APP的每个请求都要传这个参数: p_is_ios=1
	 */
	public static final String	PARAM_NAME_IS_FROM_IOS		= "p_is_ios";
	/**
	 * 电脑版client(注：不是web，是用java或者c写的程序) 的每个请求都要传这个参数: p_is_pc=1
	 */
	public static final String	PARAM_NAME_IS_FROM_PC		= "p_is_pc";

	private static final Object	lockForGetLock	= new Object();
	private AccessCtrlConfig	accessCtrlConfig;

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
	private WebApiHttpServerInterceptor() {
		//		Runnable r = new Runnable() {
		//			@Override
		//			public void run() {
		//				log.info("check.lockMapSize thread started");
		//				while (true) {
		//					try {
		//						Thread.sleep(1000 * 60 * 5);
		//					} catch (InterruptedException e) {
		//						log.error("", e);
		//					}
		//					if (lockMap.size() > 5000) {
		//						log.warn("lockMap.size() == {}, 将进行清空", lockMap.size());
		//						lockMap.clear();
		//					}
		//				}
		//			}
		//		};
		//		Thread thread = new Thread(r);
		//		thread.setName(WebApiHttpServerInterceptor.class.getSimpleName() + ".check.lockMapSize");
		//		thread.start();
	}

	private static byte[] BODY_BYTES_NEED_ACCESS_TOKEN = null;

	/**
	 * 不用进行access token检查的path
	 */
	private static Set<String> skipCheckAccessTokenPathSet = new HashSet<>();

	/**
	 * 必须进行access token检查的path（优先级最高 ）
	 */
	private static Set<String> neededCheckAccessTokenPathSet = new HashSet<>();

	//	private static Set<String> needCleanCheckAccessTokenPathSet = new HashSet<>();

	/**
	 * 不用进行access token检查的path的前缀
	 */
	private static String[] skipCheckAccessTokenPathprefix = new String[] { "/open/lastVersion1", "/open/lastVersion2", "/recharge/nf/", "/recharge/rt/", "/recharge/qrSubmit/",
	        "/tlogin/cb/p/", "/upload/video", "/upload/img", "/upload/all" };

	static {
		//不需要验证
		skipCheckAccessTokenPathSet.add("/a/x");
		skipCheckAccessTokenPathSet.add("/a/y");
		skipCheckAccessTokenPathSet.add("/wechat/service");
		skipCheckAccessTokenPathSet.add("/tlogin/");

		//博客上传图片，因为是没有走自己的ajax请求
		skipCheckAccessTokenPathSet.add("/blog/uploadimg");
		skipCheckAccessTokenPathSet.add("/ad/redirect");

		// 博客保存，编辑文章，时间可能会过长，防止误刷新
		skipCheckAccessTokenPathSet.add("/blog/save");
		//		skipCheckAccessTokenPathSet.add("/im/uploadimg");
		skipCheckAccessTokenPathSet.add("/test/checkStr");
		Resp resp = Resp.fail().code(AppCode.ForbidOper.NEED_ACCESS_TOKEN);
		String xx = Json.toJson(resp);
		try {
			BODY_BYTES_NEED_ACCESS_TOKEN = xx.getBytes(org.tio.http.common.HttpConst.CHARSET_NAME);
		} catch (UnsupportedEncodingException e) {
			log.error("", e);
		}
	}

	/**
	 * 判断是否需要进行accesstoken检查
	 * @param request
	 * @param path
	 * @return true: 需要进行accesstoken检查
	 */
	private static boolean needCheckAccessToken(HttpRequest request, String path) {
		if (neededCheckAccessTokenPathSet.contains(path)) {
			return true;
		}

		if (skipCheckAccessTokenPathSet.contains(path)) {
			return false;
		}
		for (String pathPrefix : skipCheckAccessTokenPathprefix) {
			if (StrUtil.startWith(path, pathPrefix)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 
	 * @param requestExt
	 * @return
	 * @author tanyaowu
	 */
	private static ImeiStat createImeiStat(HttpRequest request, RequestExt requestExt) {
		String ip = request.getClientIp();
		IpInfo ipInfo = IpInfoService.ME.save(ip);

		ImeiStat imeiStat = new ImeiStat();
		imeiStat.setAppversion(requestExt.getAppVersion());
		imeiStat.setCid(requestExt.getCid());
		imeiStat.setDeviceinfo(requestExt.getDeviceinfo());
		imeiStat.setImei(requestExt.getImei());
		imeiStat.setIp(ip);
		imeiStat.setIpid(ipInfo.getId());
		imeiStat.setResolution(requestExt.getResolution());
		imeiStat.setSize(requestExt.getSize());
		imeiStat.setTime(new Date());
		imeiStat.setType(requestExt.getDeviceType());
		imeiStat.setUrl(request.getRequestLine().getPath());
		imeiStat.setIdfa(requestExt.getIdfa());
		return imeiStat;
	}

	@Override
	public HttpResponse doBeforeHandler(HttpRequest request, RequestLine requestLine, HttpResponse httpResponseFromCache) throws Exception {
		RequestExt requestExt = new RequestExt();
		request.setAttribute(RequestKey.REQUEST_EXT, requestExt);
		requestExt.setCanCache(false);
		String path = requestLine.getPath(); //这个是去头和去尾的，形如: /video/videoList
		boolean isFromAndroid = "1".equals(request.getParam(PARAM_NAME_IS_FROM_ANDROID));
		boolean isFromIos = "1".equals(request.getParam(PARAM_NAME_IS_FROM_IOS));
		boolean isFromPc = "1".equals(request.getParam(PARAM_NAME_IS_FROM_PC));
		boolean isFromApp = isFromIos || isFromAndroid || isFromPc;
		@SuppressWarnings("unused")
		boolean isFromWeb = true;
		String clientTypeName = "Browser";
		if (isFromApp) {
			isFromWeb = false;
			if (isFromIos) {
				clientTypeName = "IOS";
			} else if (isFromAndroid) {
				clientTypeName = "Android";
			} else {
				clientTypeName = "PC";
			}
		}

		int accessTokenOn = 2;
		//		if (isFromWeb) {
		//			accessTokenOn = ConfService.getInt("use.access.token.pc", 2);
		//		} else if (isFromAndroid) {
		//			accessTokenOn = ConfService.getInt("use.access.token.android", 2);
		//		} else if (isFromIos) {
		//			accessTokenOn = ConfService.getInt("use.access.token.ios", 2);
		//		}

		if (isFromApp) {
			requestExt.setFromApp(true);
			requestExt.setFromBrowser(false);
			requestExt.setFromBrowserPc(false);
			requestExt.setFromBrowserMobile(false);

			// 安卓和ios只能登录一个
			requestExt.setAppDevice(Devicetype.APP.getValue());
			if (isFromIos) {
				requestExt.setFromAppIos(true);
				requestExt.setDeviceType(Devicetype.IOS.getValue());
			} else if (isFromAndroid) {
				requestExt.setFromAppAndroid(true);
				requestExt.setDeviceType(Devicetype.ANDROID.getValue());
			} else {
				requestExt.setFromPcClient(true);
				requestExt.setDeviceType(Devicetype.PC.getValue());
				requestExt.setAppDevice(Devicetype.PC.getValue());
			}
		}

		IpInfo ipInfo = IpInfoService.ME.save(request.getClientIp());
		requestExt.setIpInfo(ipInfo);

		if (requestExt.isFromApp()) {
			if (!requestExt.isFromPcClient()) {
				String appVersion = request.getHeader(HEADER_NAME_TIO_APPVERSION);
				requestExt.setAppVersion(appVersion);
				if (StrUtil.isBlank(appVersion)) {
					log.info("{} path:{}, 没有提供App版本号【{}】", clientTypeName, path, HEADER_NAME_TIO_APPVERSION);
				}

				String cid = request.getHeader(HEADER_NAME_TIO_CID);
				requestExt.setCid(cid);
				if (StrUtil.isBlank(cid)) {
					log.info("{} {}, path:{}, 没有提供渠道号【{}】", clientTypeName, appVersion, path, HEADER_NAME_TIO_CID);
				}

				String resolution = request.getHeader(HEADER_NAME_TIO_RESOLUTION);
				requestExt.setResolution(resolution);
				if (StrUtil.isBlank(resolution)) {
					log.info("{} {}, path:{}, 没有提供分辨率【{}】", clientTypeName, appVersion, path, HEADER_NAME_TIO_RESOLUTION);
				}

				String imei = request.getHeader(HEADER_NAME_TIO_IMEI);
				requestExt.setImei(imei);
				if (StrUtil.isBlank(imei)) {
					log.info("{} {}, path:{}, 没有提供IMEI【{}】", clientTypeName, appVersion, path, HEADER_NAME_TIO_IMEI);
				}

				String operator = request.getHeader(HEADER_NAME_TIO_OPERATOR);
				requestExt.setOperator(operator);
				if (StrUtil.isBlank(operator)) {
					log.info("{} {}, path:{}, 没有提供运营商【{}】", clientTypeName, appVersion, path, HEADER_NAME_TIO_OPERATOR);
				}

				String deviceinfo = request.getHeader(HEADER_NAME_MOBILE_DEVICEINFO);
				requestExt.setDeviceinfo(deviceinfo);
				if (StrUtil.isBlank(deviceinfo)) {
					log.info("{} {}, path:{}, 没有提供手机信息【{}】", clientTypeName, appVersion, path, HEADER_NAME_MOBILE_DEVICEINFO);
				}

				String size = request.getHeader(HEADER_NAME_TIO_SIZE);
				requestExt.setSize(size);
				if (StrUtil.isBlank(size)) {
					log.info("{} {}, path:{}, 没有提供手机尺寸【{}】", clientTypeName, appVersion, path, HEADER_NAME_TIO_SIZE);
				}

				String idfa = null;
				if (isFromIos) {
					idfa = request.getHeader(HEADER_NAME_TIO_IDFA);
					requestExt.setIdfa(idfa);
					if (StrUtil.isBlank(idfa)) {
						log.info("{} {}, path:{}, 没有提供idfa【{}】", clientTypeName, appVersion, path, HEADER_NAME_TIO_IDFA);
					}
				}

				if (StrUtil.isNotBlank(imei)) {
					ImeiStat imeiStat = ImeiStatService.me.getByImei(imei);
					if (imeiStat == null) {
						imeiStat = createImeiStat(request, requestExt);
						ImeiStatService.me.save(imeiStat);
					} else {
						String idfaInDb = imeiStat.getIdfa();
						if (StrUtil.isBlank(idfaInDb) && StrUtil.isNotBlank(idfa)) {
							ImeiStat newImeiStat = createImeiStat(request, requestExt);
							newImeiStat.setId(imeiStat.getId());
							ImeiStatService.me.update(newImeiStat);
						}
					}
				}
			}
		} else { //从浏览器（Browser）过来的
			String userAgentStr = request.getUserAgent();
			UserAgent userAgent = UserAgentService.ME.save(userAgentStr);
			requestExt.setUserAgent(userAgent);

			boolean isMobile = UserAgent.isMobile(userAgent);
			requestExt.setFromBrowser(true);
			requestExt.setFromBrowserPc(!isMobile); //不是移动浏览器就是PC浏览器
			requestExt.setFromBrowserMobile(isMobile);
			if (isMobile) {
				requestExt.setDeviceType(Devicetype.H5.getValue());
				requestExt.setAppDevice(Devicetype.H5.getValue());
			}
		}

		//白名单权限控制
		boolean isWhiteIp = IpWhiteListService.isWhiteIp(request.getClientIp());

		//看看access_token start
		if (!request.isForward()) {
			if (!isWhiteIp && accessTokenOn == 1) {
				if (needCheckAccessToken(request, path)) {
					//检查tio_access_token
					Cookie cookie = request.getCookie(Const.AccessToken.COOKIENAME_FOR_ACCESSTOKEN);
					boolean needNewAccessToken = true;
					if (cookie == null) {

					} else {
						String value = cookie.getValue();
						if (StrUtil.isBlank(value)) {

						} else {
							ICache cache2 = Caches.getCache(CacheConfig.TIO_ACCESS_TOKEN);
							String valueInCache = cache2.get(request.getHttpSession().getId(), String.class);

							//						if (value.startsWith(request.getClientIp())) {
							if (Objects.equals(value, valueInCache)) {
								//下面的代码是检查user-agent是否一致
								//							ICache cache3 = Caches.getCache(CacheConfig.TIO_ACCESSTOKEN_USERAGENT);
								//							String userAgentInCache = cache3.get(value, String.class);//(xxxx, request.getUserAgent());
								//							if (!Objects.equals(userAgentInCache, request.getUserAgent())) {
								//								//走到这，肯定是被程序攻击了，你懂的
								//								request.close("access_token是对的，但是UserAgent信息不一致, userAgentInCache:" + userAgentInCache + ", request UserAgent:" + request.getUserAgent());
								//								return null;
								//							} else {
								//								needNewAccessToken = false;
								//							}
								needNewAccessToken = false;
							}
							//						}
						}
					}

					if (needNewAccessToken) {
						//这样写，仅仅是为了节约性能，更简单的写法是后面那个被注释的写法
						HttpResponse ret = Resps.bytesWithContentType(request, BODY_BYTES_NEED_ACCESS_TOKEN, MimeType.TEXT_PLAIN_JSON.getType());
						//					ret.setSkipIpStat(true);
						//					ret.setSkipTokenStat(true);
						return ret;
						//					return Resps.json(request, Resp.fail().code(AppCode.ForbidOper.NEED_ACCESS_TOKEN));
					}
				}

				//清空access_token
				if (needClearAccessToken(path, request)) {
					ICache cache2 = Caches.getCache(CacheConfig.TIO_ACCESS_TOKEN);
					cache2.remove(request.getHttpSession().getId());
				}
			}
		}
		//看看access_token end

		//统一检查pageSize
		String pageSize = request.getParam("pageSize");
		if (StrUtil.isNotBlank(pageSize)) {
			String remark = null;
			//			Integer currId = WebUtils.currUserId(request);
			try {
				int _pageSize = Integer.parseInt(pageSize);
				if (_pageSize > 1000) {
					remark = "pageSize参数值[" + _pageSize + "]过大，被认为是攻击";
				}
			} catch (NumberFormatException e) {
				remark = "pageSize参数值[" + pageSize + "]不是数字，被认为是攻击";
			}

			if (remark != null) {
				TioIpPullblackUtils.addToBlack(request, request.getClientIp(), remark, TioIpPullblackLog.Type.ATTACK);
				request.close(remark);
				return null;
			}
		}

		HttpSession session = request.getHttpSession();
		//		Integer userid = session.getAttribute(SessionKey.CURR_USERID, Integer.class);

		User user = WebUtils.currUser(request);
		Integer userid = null;
		if (user != null) {
			userid = user.getId();
		}

		boolean b = AccessCtrlService.canAccess(accessCtrlConfig, userid, path);
		if (!b) //没有权限
		{
			if (user != null) {
				Resp resp = Resp.fail("You don't have authority").code(AppCode.ForbidOper.NOTPERMISSION);
				return Resps.json(request, resp);
			} else {
				//				KickedTokenInfo kickedTokenInfo = RedisSessionApi.getKickedInfo(token);
				//				if (kickedTokenInfo == null)
				//				{
				//					toError(request, response, 403, com.talent.web.AppCode.ForbidOper.NOTLOGIN, null);
				//				} else
				//				{
				//					toError(request, response, 403, com.talent.web.AppCode.ForbidOper.KICKTED, kickedTokenInfo);
				//				}
				SessionExt sessionExt = WebUtils.getSessionExt(session);
				LoginLog loginLog = sessionExt.getKickedInfo();
				if (loginLog != null) {
					String ip = loginLog.getIp();
					Date time = loginLog.getTime();
					String deviceinfo = loginLog.getDeviceinfo();
					String msg = "异地登录，您的帐号于" + DateUtil.formatDateTime(time) + "在" + ip + "登录过";
					if (StrUtil.isNotBlank(deviceinfo)) {
						msg += "，登录设备【" + deviceinfo + "】";
					}

					Resp resp = Resp.fail(msg).code(AppCode.ForbidOper.KICKTED);
					return Resps.json(request, resp);
				} else {
					Resp resp = Resp.fail("You're not logged in").code(AppCode.ForbidOper.NOTLOGIN);
					return Resps.json(request, resp);
				}
			}
		}

		requestExt.setCanCache(true);

		HttpResponse httpResponse = doHttpCacheOnBeforeHandler(request, requestExt, path, httpCacheLockKey, useHttpcache);
		return httpResponse;
	}

	public static HttpResponse doHttpCacheOnBeforeHandler(HttpRequest request, RequestExt requestExt, String path, String httpCacheLockKey, boolean useHttpcache) throws Exception {
		ICache cache = null;//HttpcacheService.getCache(path);
		//		boolean usehttpcache = P.getInt("web.api.use.http.cache", 1) == 1;
		if (useHttpcache) {
			cache = HttpcacheService.getCache(path);
		}
		//在表中配了http缓存
		if (useHttpcache && cache != null) {
			Httpcache httpcache = HttpcacheService.get(path);
			if (httpcache != null) {
				String cacheKey = getHttpcacheKey(request, cache, httpcache);
				HttpResponse httpResponse = cache.get(cacheKey, HttpResponse.class);//.put(cacheKey, value);
				if (httpResponse != null) {
					//					System.out.println("use httpcache " + path + ", key: " + cacheKey);
					return cloneAnd304(request, requestExt, httpResponse);
				} else {
					ReentrantReadWriteLock lock = LockUtils.getReentrantReadWriteLock(cacheKey, lockForGetLock);//(cacheKey);
					WriteLock writeLock = lock.writeLock();
					boolean tryWrite = writeLock.tryLock();
					if (tryWrite) {
						request.setAttribute(httpCacheLockKey, writeLock);
						httpResponse = cache.get(cacheKey, HttpResponse.class);
						if (httpResponse != null) {
							return cloneAnd304(request, requestExt, httpResponse);
						}
						return null;
					} else {
						ReadLock readLock = lock.readLock();
						boolean tryRead = readLock.tryLock(10, TimeUnit.SECONDS);
						if (tryRead) {
							request.setAttribute(httpCacheLockKey, readLock);
							httpResponse = cache.get(cacheKey, HttpResponse.class);
							if (httpResponse != null) {
								return cloneAnd304(request, requestExt, httpResponse);
							}
							return null;
						} else {
							return null;
						}
					}
				}
			}
		}

		return null;
	}

	//	String writeLockKey = this.getClass().getName() + ".writeLockKey";

	private static HttpResponse cloneAnd304(HttpRequest request, RequestExt requestExt, HttpResponse httpResponse) {
		HttpResponse clone = HttpResponse.cloneResponse(request, httpResponse);
		requestExt.setFromCache(true);

		HeaderValue lastModified = clone.getLastModified();
		if (lastModified != null) {
			try {
				long _lastModified = Long.parseLong(lastModified.value);
				HttpResponse r304 = Resps.try304(request, _lastModified);
				if (r304 != null) {
					r304.addHeader(HTTPCACHE_FLAG_HEADER_NAME, clone.getHeader(HTTPCACHE_FLAG_HEADER_NAME));
					return r304;
				}
			} catch (NumberFormatException e) {
				//							log.error("", e);
				return clone;
			}
		}

		return clone;
	}


	/**
	 * 验证完access_token后，部分path需要清空access_token
	 * @param request
	 * @return
	 * @author tanyaowu
	 */
	private boolean needClearAccessToken(String path, HttpRequest request) {
		return false;
	}

	//	private Map<String, ReentrantReadWriteLock> lockMap = new HashMap<>();

	//	/**
	//	 * key  : 形如：name=tan&id=123的cachekey
	//	 * value: ReentrantReadWriteLock
	//	 */
	//	private final MapWithLock<String, ReentrantReadWriteLock> lockMap = new MapWithLock<>(new HashMap<String, ReentrantReadWriteLock>());
	//
	//	private ReentrantReadWriteLock getLock(String cacheKey) {
	//		ReentrantReadWriteLock lock = lockMap.get(cacheKey);
	//		if (lock != null) {
	//			return lock;
	//		}
	//
	//		lock = lockMap.putIfAbsent(cacheKey, new ReentrantReadWriteLock());
	//		return lock;
	//
	//		//		synchronized (lockMap) {
	//		//			lock = lockMap.get(cacheKey);
	//		//			if (lock != null) {
	//		//				return lock;
	//		//			}
	//		//			lock = new ReentrantReadWriteLock();
	//		//			lockMap.put(cacheKey, lock);
	//		//			return lock;
	//		//		}
	//	}

	@Override
	public void doAfterHandler(HttpRequest request, RequestLine requestLine, HttpResponse response, long cost) throws Exception {
		RequestExt requestExt = WebUtils.getRequestExt(request);
		try {

		} catch (Exception e) {
			log.error("", e);
		} finally {
			response.addHeader(HeaderName.Access_Control_Allow_Credentials, HeaderValue.TRUE);
			doHttpCacheOnAfterHandler(response, request, requestExt, requestLine.path, useHttpcache, httpCacheLockKey);
			WebApiHttpServerInterceptor.saveSlowRequest(request, requestLine, response, cost, (short) 1);
		}
	}

	public static HttpResponse doHttpCacheOnAfterHandler(HttpResponse response, HttpRequest request, RequestExt requestExt, String path, boolean useHttpcache,
	        String httpCacheLockKey) {
		String cacheKey = null;
		try {

			if (requestExt.isFromCache()) {
				return response;
			}

			ICache cache = null;//HttpcacheService.getCache(path);
			//		boolean usehttpcache = P.getInt("web.api.use.http.cache", 1) == 1;
			if (useHttpcache) {
				cache = HttpcacheService.getCache(path);
			}
			//在表中配了http缓存
			if (useHttpcache && cache != null) {
				Httpcache httpcache = HttpcacheService.get(path);
				if (httpcache != null) {
					if (response != null) {
						if (response.getStatus() == HttpResponseStatus.C200 && requestExt.isCanCache()) {
							cacheKey = getHttpcacheKey(request, cache, httpcache);
							HeaderValue headerValueCacheKey = HeaderValue.from(cacheKey);
							HeaderValue lastModified = HeaderValue.from(SystemTimer.currTime + "");

							response.setLastModified(lastModified);
							HttpGzipUtils.gzip(request, response);

							HttpResponse responseForCache = HttpResponse.cloneResponse(request, response);
							responseForCache.addHeader(HTTPCACHE_FLAG_HEADER_NAME, headerValueCacheKey);
							cache.put(cacheKey, responseForCache);

							response.addHeader(HTTPCACHE_FIRST_HEADER_NAME, headerValueCacheKey);
							response.addHeader(HEADER_NAME_WEBAPI_SERVER, HEADER_VALUE_WHICH_API);

							return response;
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("", e);
		} finally {
			try {
				Lock lock = (Lock) request.getAttribute(httpCacheLockKey);
				if (lock != null) {
					if (path == null) {
						path = request.requestLine.getPath();
					}
					log.info("httpcache释放锁【{}】, 这是正常日志. path:【{}】, cacheKey:{}", lock.getClass().getName(), path, cacheKey);
					lock.unlock();
				}
			} catch (Exception e) {
				log.error(request.requestLine.toString(), e);
			}
		}

		return response;
	}

	private static boolean useHttpcache = P.getInt("web.api.use.http.cache", 1) == 1;

	public static void saveSlowRequest(HttpRequest request, RequestLine requestLine, HttpResponse response, long cost, short type) {
		int slow_request_cost = ConfService.getInt("slow_request_cost", 2000);
		if (cost >= slow_request_cost) {
			try {
				Date endtime = new Date();
				Date starttime = new Date(endtime.getTime() - cost);
				Integer uid = WebUtils.currUserId(request);
				String path = requestLine.getPathAndQuery();
				TioSlowRequest tioSlowRequest = new TioSlowRequest();
				tioSlowRequest.setType(type);
				tioSlowRequest.setCost(cost);
				tioSlowRequest.setPath(path);
				if (!Objects.equals("/register/submit", path)) {
					tioSlowRequest.setBody(StrUtil.subPre(request.getBodyString(), 1024));
				}
				tioSlowRequest.setEndtime(endtime);
				tioSlowRequest.setStarttime(starttime);
				tioSlowRequest.setUid(uid);
				tioSlowRequest.setSession(request.getHttpSession().getId());

				tioSlowRequest.save();
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}

	/**
	 * 
	 * @param request
	 * @param cache
	 * @param httpcache
	 * @return
	 */
	private static String getHttpcacheKey(HttpRequest request, ICache cache, Httpcache httpcache) {
		Integer currUid = WebUtils.currUserId(request);
		Map<String, Object> params = null;
		String[] paramArray = httpcache.getParamArray();
		if (paramArray != null && paramArray.length > 0) {
			params = new HashMap<>();
			for (String name : paramArray) {
				String value = request.getParam(name);
				params.put(name, value);
			}
		}

		return getHttpcacheKey(currUid, params, cache, httpcache);
	}

	public static String getHttpcacheKey(Integer currUid, Map<String, Object> params, ICache cache, Httpcache httpcache) {
		String[] paramArray = httpcache.getParamArray();
		//		String[] cookieArray = httpcache.getCookieArray();
		boolean isUseUidAsKey = httpcache.isUseUidAsKey(); //是否使用userid作为cachekey
		boolean isUseLoginedAsKey = httpcache.isUseLoginedAsKey(); //是否使用登录状态作为cachekey

		StringBuilder key = new StringBuilder(30);
		if (isUseUidAsKey && currUid != null) {
			key.append("u{").append(currUid).append("}");
		}

		if (isUseLoginedAsKey) {
			if (currUid != null) {
				key.append("l{1}");
			} else {
				key.append("l{0}");
			}
		}

		if (paramArray != null && params != null) {
			key.append("p{");
			for (String name : paramArray) {
				Object value = params.get(name);
				if (value != null) {
					key.append(name).append("=").append(value).append("&");
				}
			}
			key.append("}");
		}

		//		if (cookieArray != null) {
		//			key.append("c{");
		//			for (String name : cookieArray) {
		//				Cookie cookie = request.getCookie(name);
		//				if (cookie != null) {
		//					key.append(name).append("=").append(cookie.getValue()).append("&");
		//				}
		//			}
		//			key.append("}");
		//		}

		if (key.length() == 0) {
			return "t-io";
		} else {
			return key.toString();
		}
	}

	/**
	 * 
	 * @param path
	 * @param params
	 * @param currUid
	 */
	public static void removeHttpcache(String path, Map<String, Object> params, Integer currUid) {
		ICache cache = HttpcacheService.getCache(path);
		if (cache != null) {
			Httpcache httpcache = HttpcacheService.get(path);
			if (httpcache != null) {
				if (httpcache.isHasPageNumber()) {
					if (params == null) {
						params = new HashMap<>();
					}
					for (int i = 0; i < 15; i++) {
						params.put(Const.PARAM_NAME_PAGENUMBER, i);
						String cacheKey = getHttpcacheKey(currUid, params, cache, httpcache);
						cache.remove(cacheKey);
					}
				} else {
					String cacheKey = getHttpcacheKey(currUid, params, cache, httpcache);
					cache.remove(cacheKey);
				}
			}
		}
	}

	/**
	 * 
	 * @param path
	 * @author tanyaowu
	 */
	public static void clearHttpcache(String path) {
		ICache cache = HttpcacheService.getCache(path);
		if (cache != null) {
			cache.clear();
		}
	}

	public AccessCtrlConfig getAccessCtrlConfig() {
		return accessCtrlConfig;
	}

	public void setAccessCtrlConfig(AccessCtrlConfig accessCtrlConfig) {
		this.accessCtrlConfig = accessCtrlConfig;
	}
}
