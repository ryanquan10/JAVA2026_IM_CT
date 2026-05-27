
package org.tio.sitexxx.view.http;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HeaderName;
import org.tio.http.common.HeaderValue;
import org.tio.http.common.HttpConfig;
import org.tio.http.common.HttpConst;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.http.common.HttpResponseStatus;
import org.tio.http.common.MimeType;
import org.tio.http.common.RequestLine;
import org.tio.http.common.session.HttpSession;
import org.tio.http.server.intf.HttpServerInterceptor;
import org.tio.http.server.util.Resps;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.model.main.LoginLog;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.service.conf.ConfService;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.RequestExt;
import org.tio.sitexxx.service.vo.RequestKey;
import org.tio.sitexxx.service.vo.SessionExt;
import org.tio.sitexxx.view.WebViewStarter;
import org.tio.sitexxx.web.server.auth.AccessCtrlConfig;
import org.tio.sitexxx.web.server.auth.AccessCtrlService;
import org.tio.sitexxx.web.server.http.WebApiHttpServerInterceptor;
import org.tio.sitexxx.web.server.utils.WebUtils;
import org.tio.utils.SystemTimer;
import org.tio.utils.cache.ICache;
import org.tio.utils.jfinal.P;
import org.tio.utils.lock.LockUtils;
import org.tio.utils.lock.MapWithLock;
import org.tio.webpack.cache.CacheVo;
import org.tio.webpack.compress.ResCompressor;
import org.tio.webpack.compress.ResCompressorFactory;
import org.tio.webpack.model.Root;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @author tanyaowu 
 * 2016年11月17日 下午3:09:27
 */
public class WebViewHttpServerInterceptor implements HttpServerInterceptor {
	private static Logger								log							= LoggerFactory.getLogger(WebViewHttpServerInterceptor.class);
	private AccessCtrlConfig							accessCtrlConfig			= null;
	public static final WebViewHttpServerInterceptor	me							= new WebViewHttpServerInterceptor();
	private final String								httpCacheLockKey			= this.getClass().getName() + ".httpCacheLockKey";
	private static boolean								useHttpcache				= P.getInt("web.view.use.http.cache", 1) == 1;
	public static final HeaderName						tio_view_from_cache			= HeaderName.from("tio_view_from_cache");
	public static final HeaderValue						tio_view_from_cache_value	= HeaderValue.from("1");
	private static final Set<String>					noCachePaths				= new HashSet<>();
	private static final Set<String>					skipCompressPaths			= new HashSet<>();
	private static final Set<String>					skipCompressPathItems		= new HashSet<>();

	static {
		noCachePaths.add(Const.Path.WECHAT_PAY); //微信支付页面
		noCachePaths.add("/http-api/app/httpclient-lixian-data.js");

		String skipCompressPathStr = ConfService.getString("web.view.skip.js.compress", "");
		if (StrUtil.isNotBlank(skipCompressPathStr)) {
			String[] skipCompressPathArray = StrUtil.splitToArray(skipCompressPathStr, ";");
			StrUtil.trim(skipCompressPathArray);
			Collections.addAll(skipCompressPaths, skipCompressPathArray);
		}
	}

	/**
	 * 不需要比较初始值是否一样的缓存，譬如html
	 */
	//	@SuppressWarnings("unused")
	//	private static final MapWithLock<String, CacheVo>	CACHEMAP_NEED_COMPARE		= new MapWithLock<>(128);
	/**
	 * 不需要比较初始值是否一样的缓存，譬如js,css
	 */
	private static final MapWithLock<String, CacheVo> CACHEMAP_NOT_NEED_COMPARE = new MapWithLock<>(512);

	private Root model;

	/**
	 * 
	 * @author tanyaowu
	 */
	private WebViewHttpServerInterceptor() {
	}

	private final String fromCache = "fromCache";

	@Override
	public HttpResponse doBeforeHandler(HttpRequest request, RequestLine requestLine, HttpResponse responseFromCache) throws Exception {
		RequestExt requestExt = new RequestExt();
		request.setAttribute(RequestKey.REQUEST_EXT, requestExt);

		String path = requestLine.getPath();
		HttpSession session = request.getHttpSession();
		//		Integer userid = session.getAttribute(SessionKey.CURR_USERID, Integer.class);

		User curr = WebUtils.currUser(request);
		Integer userid = null;
		if (curr != null) {
			userid = curr.getId();
		}

		boolean b = AccessCtrlService.canAccess(accessCtrlConfig, userid, path);
		HttpConfig httpConfig = request.httpConfig;
		if (!b) //没有权限
		{
			String msg = null;
			int showLogin = 1;
			if (curr != null) {
				showLogin = 2;
				msg = "该页面只对部分用户开放";
			} else {
				SessionExt sessionExt = WebUtils.getSessionExt(session);
				LoginLog loginLog = sessionExt.getKickedInfo();

				if (loginLog != null) {
					String ip = loginLog.getIp();
					Date time = loginLog.getTime();
					String deviceinfo = loginLog.getDeviceinfo();
					msg = "异地登录，您的帐号于" + DateUtil.formatDateTime(time) + "在" + ip + "登录过";
					if (StrUtil.isNotBlank(deviceinfo)) {
						msg += "，登录设备【" + deviceinfo + "】";
					}
				} else {
					msg = "您尚未登录或登录超时";
				}
			}

			//			String p = Const.Path.LOGIN 
			//					+ "?" + Const.UrlParamName.REDIRECT_URI_AFTER_LOGIN + "=" + URLEncoder.encode(request.requestLine.getPathAndQuery(), httpConfig.getCharset())
			//					+ "&msg=" + URLEncoder.encode(msg, httpConfig.getCharset())
			//					+ "&showLogin=" + showLogin;

			StringBuilder sb = new StringBuilder(64);
			//			if (path.startsWith("/h5/")) {
			//				sb.append("/h5/wx/login");
			//				sb.append("?").append(Const.UrlParamName.REDIRECT_URI_AFTER_LOGIN).append("=")
			//				        .append(URLEncoder.encode(request.requestLine.getPathAndQuery(), httpConfig.getCharset()));
			//				sb.append("&msg=").append(URLEncoder.encode(msg, httpConfig.getCharset()));
			//				sb.append("&showLogin=").append(showLogin);
			//			} else {
			sb.append(Const.Path.LOGIN);
			sb.append("?").append(Const.UrlParamName.REDIRECT_URI_AFTER_LOGIN).append("=")
			        .append(URLEncoder.encode(request.requestLine.getPathAndQuery(), httpConfig.getCharset()));
			sb.append("&msg=").append(URLEncoder.encode(msg, httpConfig.getCharset()));
			sb.append("&showLogin=").append(showLogin);
			//			}
			//			return Resps.forward(request, sb.toString());
			return Resps.redirect(request, sb.toString());
		}

		ICache cache = null;//Caches.getCache(CacheConfig.VIEW_HTML);
		String cachePath = getCachePath(request);
		HttpResponse responseInCache = null;

		if (StrUtil.isNotBlank(cachePath)) {
			cache = Caches.getCache(CacheConfig.VIEW_HTML);
			responseInCache = cache.get(cachePath, HttpResponse.class);
		}

		if (responseInCache != null) {
			responseInCache = HttpResponse.cloneResponse(request, responseInCache);
			request.setAttribute(fromCache, 1);

			String If_Modified_Since = request.getHeader(HttpConst.RequestHeaderKey.If_Modified_Since);//If-Modified-Since
			if (StrUtil.isNotBlank(If_Modified_Since)) {
				try {
					//					Long xx = Long.parseLong(If_Modified_Since);
					HeaderValue Last_Modified = responseInCache.getLastModified();//responseInCache.getHeader(HeaderName.Last_Modified);
					if (Last_Modified != null) {
						if (Objects.equals(Last_Modified.value, If_Modified_Since)) {
							HttpResponse ret = new HttpResponse(request);
							ret.setStatus(HttpResponseStatus.C304);
							return ret;
						}
					}
				} catch (NumberFormatException e) {
					log.warn("{}, {}不是整数，浏览器信息:{}", request.getClientIp(), If_Modified_Since, request.getUserAgent());
				}
			}
		} else {
			//根据httpcache表配置缓存
			responseInCache = WebApiHttpServerInterceptor.doHttpCacheOnBeforeHandler(request, requestExt, path, httpCacheLockKey, useHttpcache);
		}
		return responseInCache;
	}

	private CacheVo processCompress(byte[] respBodyInitBytes, ResCompressor resCompressor, HttpRequest request, String path) throws Exception {
		byte[] compressedBytes = getCompressedBytes(respBodyInitBytes, resCompressor, request, path);

		CacheVo cacheVo = new CacheVo();
		//		cacheVo.setInitStr(initStr);
		//		cacheVo.setCompressedStr(compressedStr);
		cacheVo.setInitBytes(respBodyInitBytes);
		cacheVo.setCompressedBytes(compressedBytes);
		return cacheVo;
	}

	/**
	 * true : 略过js压缩
	 * @param path
	 * @return
	 * @author tanyaowu
	 */
	private boolean skipJsCompress(String path) {
		if (skipCompressPathItems.contains(path)) {
			return true;
		}
		if (skipCompressPaths != null && !skipCompressPaths.isEmpty()) {
			for (String skipCompressPath : skipCompressPaths) {
				if (path.startsWith(skipCompressPath)) {
					skipCompressPathItems.add(path);
					return true;
				}
			}
		}
		return false;
	}

	private byte[] getCompressedBytes(byte[] respBodyInitBytes, ResCompressor resCompressor, HttpRequest request, String path) throws Exception {
		String initStr = new String(respBodyInitBytes, request.getCharset());
		//		String compressedStr = null;
		byte[] compressedBytes = null;
		if (initStr != null && initStr.length() > 256 && !skipJsCompress(path)) {
			//			compressedStr = resCompressor.compress(path, initStr);
			compressedBytes = resCompressor.compress(path, initStr).getBytes(request.getCharset());
		} else {
			//			compressedStr = initStr;
			compressedBytes = respBodyInitBytes;
		}
		return compressedBytes;
	}

	private static void putToCache(String path, CacheVo cacheVo) {

		CACHEMAP_NOT_NEED_COMPARE.put(path, cacheVo);
	}

	private static CacheVo getCache(String path) {
		CacheVo ret = CACHEMAP_NOT_NEED_COMPARE.get(path);
		return ret;
	}

	public static void clearCache() {
		CACHEMAP_NOT_NEED_COMPARE.clear();
	}

	@Override
	public void doAfterHandler(HttpRequest request, RequestLine requestLine, HttpResponse response, long cost) throws Exception {
		RequestExt requestExt = WebUtils.getRequestExt(request);
		try {
			if (response == null) {
				return;
			}

			if (Objects.equals(1, request.getAttribute(fromCache))) {
				return;
			}

			if (requestExt.isFromCache()) {
				return;
			}

			HeaderValue contentType = response.getContentType();
			if (contentType == null) {
				return;
			}

			MimeType mimeType = MimeType.fromType(contentType.value);
			if (mimeType != null) {
				String extension = mimeType.getExtension();
				String path = requestLine.getPath();
				//				String ext = FileUtil.extName(path);
				ResCompressor resCompressor1 = null;
				boolean needCompress = neededCompress(path);// && (resCompressor != null);
				if (needCompress) {
					needCompress = ResCompressorFactory.isNeedCompress(model, extension);
					if (needCompress) {
						resCompressor1 = ResCompressorFactory.get(extension);
						needCompress = resCompressor1 != null;
					}
				}

				if (needCompress) {
					ResCompressor resCompressor = resCompressor1;
					byte[] respBodyInitBytes = response.getBody();
					if ((getCachePath(request) != null)) {
						byte[] compressedBsInCache = null;
						CacheVo cacheVo = getCache(path);
						//						boolean isWrite = false;
						//						ReadWriteRet readWriteRet = null;
						if (cacheVo == null) {
							LockUtils.runWriteOrWaitRead("_tiohttp_view_wv_" + path, this, () -> {
							    //								@Override
							    //								public void read() throws Exception {
							    //								}

							    //								CacheVo cacheVo1 = getCache(path, ext);
							    if (getCache(path) == null) {
									putToCache(path, processCompress(respBodyInitBytes, resCompressor, request, path));
								}

							});

							//							isWrite = readWriteRet.isWriteRunned;

							cacheVo = getCache(path);
							if (cacheVo == null) {
								User curr = WebUtils.currUser(request);
								String nick = null;
								if (curr != null) {
									nick = curr.getNick();
								} else {
									nick = "null";
								}

								log.error("path:{}, curr:{}, ip:{}", path, nick, request.getClientIp());
							} else {
								compressedBsInCache = cacheVo.getCompressedBytes();
							}
						}

						// 未压缩的响应体和Cache中的响应体是否一样
						boolean isSame = false;
						if (WebViewInit.isDevMode) {
							isSame = Arrays.equals(respBodyInitBytes, cacheVo.getInitBytes());
						} else {
							isSame = true;
						}

						if (isSame) {
							compressedBsInCache = cacheVo.getCompressedBytes();
							response.addHeader(HeaderName.tio_webpack_used_cache, HeaderValue.Tio_Webpack_Used_Cache.V_1);
						} else {
							cacheVo = processCompress(respBodyInitBytes, resCompressor, request, path);
							compressedBsInCache = cacheVo.getCompressedBytes();

							if (getCachePath(request) != null) {
								putToCache(path, cacheVo);
							}
						}
						response.setBody(compressedBsInCache);
					} else {
						byte[] compressedBytes = getCompressedBytes(respBodyInitBytes, resCompressor, request, path);
						response.setBody(compressedBytes);
					}
				}
			}

			if (!WebViewInit.isDevMode) {
				//生产模式
				HttpResponseStatus status = response.getStatus();
				if (Objects.equals(status, HttpResponseStatus.C200)/** || Objects.equals(status, HttpResponseStatus.C304)*/
				) {
					String cachePath = getCachePath(request);
					if (StrUtil.isNotBlank(cachePath)) {
						HeaderValue lastModified = HeaderValue.from(SystemTimer.currTime + "");
						response.addHeader(HeaderName.Cache_Control, HeaderValue.Cache_Control.MAX_AGE_60);
						response.setLastModified(lastModified);

						//					String contentType = response.getHeader(HeaderName.Content_Type);
						HeaderValue contentEncoding = response.getHeader(HeaderName.Content_Encoding);

						Map<HeaderName, HeaderValue> headersInCache = new HashMap<>();
						if (contentType != null) {
							headersInCache.put(HeaderName.Content_Type, contentType);
						}
						if (contentEncoding != null) {
							headersInCache.put(HeaderName.Content_Encoding, contentEncoding);
						}

						headersInCache.put(HeaderName.Cache_Control, response.getHeader(HeaderName.Cache_Control));

						HttpResponse responseInCache = new HttpResponse(request);
						responseInCache.setStatus(response.getStatus());
						responseInCache.addHeaders(headersInCache);
						responseInCache.setLastModified(lastModified);
						responseInCache.setBody(response.getBody());
						responseInCache.setHasGzipped(response.isHasGzipped());
						responseInCache.addHeader(tio_view_from_cache, tio_view_from_cache_value);

						ICache cache = Caches.getCache(CacheConfig.VIEW_HTML);
						cache.put(cachePath, responseInCache);
					}
				}
			}

		} catch (Exception e) {
			log.error(requestLine.toString(), e);
		} finally {
			response = WebApiHttpServerInterceptor.doHttpCacheOnAfterHandler(response, request, requestExt, requestLine.path, useHttpcache, httpCacheLockKey);
			WebApiHttpServerInterceptor.saveSlowRequest(request, requestLine, response, cost, (short) 2);
		}
	}

	/**
	 * 获取缓存path，返回null表示不允许被缓存
	 * 被ModelGeneratorPath注释过的，将会返回null，
	 * @param request
	 * @return
	 */
	public static String getCachePath(HttpRequest request) {
		RequestLine requestLine = request.getRequestLine();
		String path = requestLine.getPath();
		//		String cachepath = path;

		if (noCachePaths.contains(path)) { //不需要缓存的页面
			return null;
		}

		if (WebViewStarter.tioWebpackModelGenerator.generatorMap.containsKey(path)) { //配了PathModelGenerator的路径，不缓存
			return null;
		}
		return path;
	}

	public static boolean neededCompress(String path) {
		if (path.startsWith("/tioim/") || path.startsWith("/tioims/")) {
			return false;
		}
		return true;
	}

	public Root getModel() {
		return model;
	}

	public void setModel(Root model) {
		this.model = model;
	}

	public AccessCtrlConfig getAccessCtrlConfig() {
		return accessCtrlConfig;
	}

	public void setAccessCtrlConfig(AccessCtrlConfig accessCtrlConfig) {
		this.accessCtrlConfig = accessCtrlConfig;
	}
}
