
package org.tio.mg.view.http;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpConfig;
import org.tio.http.common.HttpResponse;
import org.tio.http.common.session.id.impl.SnowflakeSessionIdGenerator;
import org.tio.http.common.view.ModelGenerator;
import org.tio.http.server.HttpServerStarter;
import org.tio.http.server.handler.DefaultHttpRequestHandler;
import org.tio.http.server.mvc.Routes;
import org.tio.http.server.session.DomainSessionCookieDecorator;
import org.tio.http.server.stat.ip.path.IpPathAccessStats;
import org.tio.http.server.stat.token.TokenPathAccessStats;
import org.tio.mg.service.init.PropInit;
import org.tio.mg.service.init.RedisInit;
import org.tio.mg.service.tio.TioSiteIpStatListener;
import org.tio.mg.service.vo.MgConst;
import org.tio.mg.web.server.auth.AccessCtrlConfig;
import org.tio.mg.web.server.http.WebApiHttpSessionListener;
import org.tio.mg.web.server.http.stat.TioSiteStatPathFilter;
import org.tio.mg.web.server.http.stat.ip.TioSiteIpPathAccessStatListener;
import org.tio.mg.web.server.http.stat.token.TioSiteCurrUseridGetter;
import org.tio.mg.web.server.http.stat.token.TioSiteTokenGetter;
import org.tio.mg.web.server.http.stat.token.TioSiteTokenPathAccessStatListener;
import org.tio.server.TioServerConfig;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.Const.TokenPathAccessStatDuration;
import org.tio.utils.Threads;
import org.tio.utils.cache.caffeineredis.CaffeineRedisCache;
import org.tio.utils.jfinal.P;
import org.tio.utils.json.Json;
import org.tio.utils.resp.Resp;
import org.tio.webpack.model.Root;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @author tanyaowu
 * 2016年7月19日 下午4:59:04
 */
public class WebViewInit {
	@SuppressWarnings("unused")
	private static Logger					log						= LoggerFactory.getLogger(WebViewInit.class);
	public static HttpServerStarter			httpServerStarter;
	public static TioServerConfig			tioServerConfig;
	public static HttpConfig				httpConfig;
	public static Routes					routes					= null;
	public static DefaultHttpRequestHandler	requestHandler;
	public static Integer					maxLiveTimeOfStaticRes	= 0;
	public static int						devMode					= 1;
	public static boolean					isDevMode				= false;

	/**
	 * 初始化tx/ws/bs.js文件的内容
	 * @param pageRoot
	 * @author tanyaowu
	 */
	public static void initWsBs(String pageRoot) {
		//\tx\ws\bs
		File wxbsDir = new File(pageRoot, "tx/ws/bs");
		File[] files = wxbsDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if ("bs".equals(FileUtil.mainName(pathname))) {
					return false;
				}
				return true;
			}
		});
		StringBuilder sb = new StringBuilder(512);
		sb.append("/*\r\n" + 
				"    注意：\r\n" + 
				"    1、该文件内容会自动将bs目录下的文件添加进来，不需要手动添加\r\n" + 
				"    2、不要在bs目录放命令执行函数以外的文件\r\n" + 
				"*/\r\n");
		for (int i = 0; i < files.length; i++) {
			//<#include "/tx/ws/bs/HandshakeResp.js" >
			File file = files[i];
			sb.append("<#include \"/tx/ws/bs/").append(FileUtil.getName(file)).append("\" >").append("\r\n\r\n");
		}
		FileUtil.writeUtf8String(sb.toString(), new File(pageRoot, "tx/ws/bs.js"));
	}

	public static void init(Root model, ModelGenerator modelGenerator) throws Exception {
		//		long start = SystemTimer.currTime;

		PropInit.init();

		devMode = P.getInt("dev.mode", 3);
		//html/css/js等的根目录，支持classpath:，也支持绝对路径
		String pageRoot = MgConst.VIEW_PAGE_ROOT;
//		String pageRoot = P.get("http.view.page", null);
//		initWsBs(pageRoot);

		//【【------------ 设置 httpconfig start ------------------
		//监听端口
		int port = P.getInt("http.view.port");
		//session超时时间
		long sessionTimeout = P.getLong("http.session.timeout", HttpConfig.DEFAULT_SESSION_TIMEOUT);//HttpConfig.DEFAULT_SESSION_TIMEOUT;
		String contextpath = P.get("http.view.contextpath", "");
		String suffix = P.get("http.view.suffix", "");
		httpConfig = new HttpConfig(port, sessionTimeout, contextpath, suffix);
		httpConfig.setSessionCacheName(P.get("http.session.cache.name", HttpConfig.SESSION_CACHE_NAME));

		httpConfig.setPageRoot(pageRoot);

		httpConfig.setWelcomeFile("index.html");
		httpConfig.setAppendRequestHeaderString(true);
		httpConfig.setPage404(P.get("http.view.page404", "/p400/404.html"));
		httpConfig.setPage500(P.get("http.view.page500", "/p500/500.html"));

		maxLiveTimeOfStaticRes = null;//P.getInt("http.maxLiveTimeOfStaticRes");

		if (devMode == 1) {
			maxLiveTimeOfStaticRes = -1;
			isDevMode = true;
		} else if (devMode == 2) {
			maxLiveTimeOfStaticRes = 30;
		} else {
			maxLiveTimeOfStaticRes = P.getInt("http.maxLiveTimeOfStaticRes");
		}

		if (maxLiveTimeOfStaticRes != null) {
			httpConfig.setMaxLiveTimeOfStaticRes(maxLiveTimeOfStaticRes);
		}

		// 上传时，允许的最大长度，单位：KB
		Integer max_length_of_multi_body = P.getInt("http.max_length_of_multi_body");
		if (max_length_of_multi_body != null) {
			httpConfig.setMaxLengthOfMultiBody(max_length_of_multi_body * 1024);
		}

		// POST时，允许的最大长度，单位：KB
		Integer max_length_of_post_body = P.getInt("http.max_length_of_post_body");
		if (max_length_of_post_body != null) {
			httpConfig.setMaxLengthOfPostBody(max_length_of_post_body * 1024);
		}

		byte[] bs = Json.toJson(Resp.fail("Your IP is on the blacklist")).getBytes();
		httpConfig.setRespForBlackIp(new HttpResponse(null, bs));
		httpConfig.setSessionRateLimiter(WebViewSessionRateLimiter.me);

		httpConfig.setProxied(P.getBoolean("http.isproxied", false));

		//------------ 设置 httpconfig end ------------------】】

		//【【------------ 设置 DefaultHttpRequestHandler start ------------------
		requestHandler = new DefaultHttpRequestHandler(httpConfig, routes);
		requestHandler.setCompatibilityAssignment(false);

//		String[] suffixes = StrUtil.splitToArray(P.get("freemarker.suffix", ""), "|");

		//这一行不能去掉
//		new FreemarkerConfig(httpConfig, modelGenerator, suffixes, FreemarkerConfigurationCreater.me);

		String cookieDomain = P.get("http.cookie.domain"); // 形如: ".baidu.com"
		DomainSessionCookieDecorator domainSessionCookieDecorator = new DomainSessionCookieDecorator(cookieDomain);
		requestHandler.setSessionCookieDecorator(domainSessionCookieDecorator);

		//分布式环境中，用于高效生成uuid的
		int workerid = P.getInt("uuid.workerid");
		int datacenter = P.getInt("uuid.datacenter");

		SnowflakeSessionIdGenerator sessionIdGenerator = new SnowflakeSessionIdGenerator(workerid, datacenter);//更高效的Snowflake sessionId生成器，取代默认的sessionId生成器
		httpConfig.setSessionIdGenerator(sessionIdGenerator);

		httpConfig.setSessionCookieName(Const.Http.SESSION_COOKIE_NAME);

		//用于存储HttpSession对象
		CaffeineRedisCache caffeineRedisCache = CaffeineRedisCache.register(RedisInit.get(), httpConfig.getSessionCacheName(), null, sessionTimeout);
		httpConfig.setSessionStore(caffeineRedisCache);

		requestHandler.setThrowableHandler(TioSiteViewThrowableHandler.ME);
		requestHandler.setHttpSessionListener(WebApiHttpSessionListener.ME);

		WebViewHttpServerInterceptor.me.setModel(model);
		AccessCtrlConfig accessCtrlConfig = new AccessCtrlConfig("access-url-role-view.properties", httpConfig.getStaticPaths(), true);

		WebViewHttpServerInterceptor.me.setAccessCtrlConfig(accessCtrlConfig);
		requestHandler.setHttpServerInterceptor(WebViewHttpServerInterceptor.me);

		//------------ 设置 DefaultHttpRequestHandler end ------------------】】

		httpConfig.setName("Web-View");
		//创建HttpServerStarter
		httpServerStarter = new HttpServerStarter(httpConfig, requestHandler, Threads.getTioExecutor(), Threads.getGroupExecutor());
		httpServerStarter.getTioServer().setCheckLastVersion(P.getBoolean("tio.setCheckLastVersion", false));

		tioServerConfig = httpServerStarter.getTioServerConfig();
		tioServerConfig.setName("Tio-Site View HttpServer");
		tioServerConfig.setReadBufferSize(P.getInt("http.view.readbuffersize", 1024));
		tioServerConfig.setIpStatListener(TioSiteIpStatListener.web_view);
		tioServerConfig.ipStats.addDurations(Const.IpStatDuration.IPSTAT_DURATIONS);

		tioServerConfig.logWhenDecodeError = P.getBoolean("http.view.logWhenDecodeError", false);

		IpPathAccessStats ipPathAccessStats = new IpPathAccessStats(TioSiteStatPathFilter.me, tioServerConfig, TioSiteIpPathAccessStatListener.ME_SITE_VIEW,
		        Const.IpPathAccessStatDuration.IP_PATH_ACCESS_STAT_DURATIONS);
		requestHandler.setIpPathAccessStats(ipPathAccessStats);

		TokenPathAccessStats tokenPathAccessStats = new TokenPathAccessStats(TioSiteStatPathFilter.me, TioSiteTokenGetter.me, TioSiteCurrUseridGetter.me, tioServerConfig,
		        TioSiteTokenPathAccessStatListener.ME_SITE_VIEW, TokenPathAccessStatDuration.TOKEN_PATH_ACCESS_STAT_DURATIONS);
		requestHandler.setTokenPathAccessStats(tokenPathAccessStats);

		//启动httpserver
		httpServerStarter.start(P.getInt("view.preAccess", 1) == 1);
		//		httpServerStarter.preAccess();

		//		long end = SystemTimer.currTime;
		//		long iv = end - start;
		//		log.info("TioHttpServer启动完毕,耗时:{}ms,访问地址:http://127.0.0.1:{}", iv, port);
	}

	/**
	 *
	 * @author tanyaowu
	 */
	public WebViewInit() {
	}

	public static void main(String[] args) {
		String[] sss = StrUtil.splitToArray("txt|js|css|html|htm|json|xml", "|");
		System.out.println(Arrays.toString(sss));

		boolean xx = ArrayUtil.contains(sss, "txt");

		System.out.println(xx);
	}
}
