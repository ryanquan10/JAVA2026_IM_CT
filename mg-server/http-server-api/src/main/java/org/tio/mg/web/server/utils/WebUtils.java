
package org.tio.mg.web.server.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.session.HttpSession;
import org.tio.mg.service.model.main.IpInfo;
import org.tio.mg.service.model.main.UserAgent;
import org.tio.mg.service.model.main.UserToken;
import org.tio.mg.service.model.mg.MgUser;
import org.tio.mg.service.service.base.IpInfoService;
import org.tio.mg.service.service.base.UserAgentService;
import org.tio.mg.service.service.base.UserTokenService;
import org.tio.mg.service.service.mg.MgUserService;
import org.tio.mg.service.topic.Topics;
import org.tio.mg.service.vo.RequestExt;
import org.tio.mg.service.vo.SessionExt;
import org.tio.mg.web.server.init.WebApiInit;
import org.tio.sitexxx.service.vo.ClearHttpCache;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.RequestKey;
import org.tio.sitexxx.service.vo.SessionKey;

import cn.hutool.core.util.StrUtil;

/**
 * @author tanyaowu
 * 2016年8月10日 上午10:59:58
 */
public class WebUtils {
	@SuppressWarnings("unused")
	private static Logger			log			= LoggerFactory.getLogger(WebUtils.class);
	private static MgUserService	userService	= MgUserService.ME;

	/**
	 * 手动把某人登出
	 * @param uid 用户id
	 */
	public static void logout(Integer uid) {
		List<UserToken> list = UserTokenService.me.find(uid);
		if (list != null && list.size() > 0) {
			for (UserToken userToken : list) {
				String token = userToken.getToken();
				if (StrUtil.isNotBlank(token)) {
					HttpSession session = (HttpSession) WebApiInit.httpConfig.getSessionStore().get(token);

					if (session != null) {
						SessionExt sessionExt = session.getAttribute(SessionKey.SESSION_EXT, SessionExt.class, null, WebApiInit.httpConfig);
						if (sessionExt != null) {
							if (sessionExt.getUid() != null) {
								sessionExt.setUid(null);
								session.update(WebApiInit.httpConfig);//切记：每次修改SessionExt后，要调用一下update把数据更新到分布式缓存中
							}
						}
					}
				}
			}

			UserTokenService.me.delete(uid);
		}
	}

	/**
	 * 
	 * @param path
	 * @param params
	 * @param currUid
	 * @author tanyaowu
	 */
	public static void removeHttpcache(String path, Map<String, Object> params, Integer currUid) {
		Topics.notifyRemoveHttpCache(path, currUid, params, ClearHttpCache.ClearType.REMOVE);
	}

	/**
	 * 
	 * @param path
	 * @author tanyaowu
	 */
	public static void clearHttpcache(String path) {
		Topics.notifyRemoveHttpCache(path, null, null, ClearHttpCache.ClearType.CLEAR);
	}

	/**
	 * 把"/user/curr"这样的path变成"/api/user/curr.php"
	 * 把"/user/curr?name=tan"这样的path变成"/api/user/curr.php?name=tan"
	 * @param path
	 * @return
	 */
	public static String path(String path) {
		String contextpath = Const.API_CONTEXTPATH;
		String suffix = Const.API_SUFFIX;

		int x = StringUtils.indexOf(path, "?");
		if (x == -1) {
			return contextpath + path + suffix;
		} else {
			String path1 = path.substring(0, x);
			String queryStr = path.substring(x + 1, path.length());
			return contextpath + path1 + suffix + "?" + queryStr;
		}
	}

	/**
	 * @param request
	 * @return
	 * @author xufei
	 * 2020年5月26日 下午3:37:04
	 */
	public static MgUser currUser(HttpRequest request) {
		Integer userid = currUserId(request);
		if (userid != null) {
			MgUser user = userService.getById(userid);
			return user;
		}
		return null;
	}

	/**
	 * 获取当前用户的userid
	 * @param request
	 * @return
	 */
	public static Integer currUserId(HttpRequest request) {
		HttpSession session = request.getHttpSession();
		SessionExt sessionExt = WebUtils.getSessionExt(session);
		Integer userid = sessionExt.getUid();//(Integer) request.getHttpSession().getAttribute(SessionKey.CURR_USERID, Integer.class);
		return userid;
	}

	/**
	 * 获取SessionExt对象
	 * @param request
	 * @return
	 */
	public static SessionExt getSessionExt(HttpRequest request) {
		SessionExt sessionExt = getSessionExt(request.getHttpSession());
		return sessionExt;
	}

	/**
	 * 获取SessionExt对象
	 * @param session
	 * @return
	 */
	public static SessionExt getSessionExt(HttpSession session) {
		SessionExt sessionExt = session.getAttribute(SessionKey.SESSION_EXT, SessionExt.class);
		return sessionExt;
	}

	/**
	 * 根据HttpSession对象获取当前用户的userid
	 * @param session
	 * @return
	 */
	public static Integer getUserIdBySession(HttpSession session) {
		if (session == null) {
			return null;
		}
		SessionExt sessionExt = WebUtils.getSessionExt(session);
		Integer userid = sessionExt.getUid();
		return userid;
	}

	/**
	 * RequestExt对象是个非常有用的对象
	 * @param request
	 * @return
	 */
	public static RequestExt getRequestExt(HttpRequest request) {
		return (RequestExt) request.getAttribute(RequestKey.REQUEST_EXT);
	}

	/**
	 * 获取IM服务器的端口
	 * @param request
	 * @return
	 * @author: tanyaowu
	 */
	public static int getImServerPort(HttpRequest request) {
		RequestExt requestExt = WebUtils.getRequestExt(request);
		if (requestExt.isFromBrowser()) {
			return Const.ImPort.WS;
		} else {
			return Const.ImPort.APP;
		}
	}

	/**
	 * 
	 * @param request
	 * @return
	 * 
	 */
	public static Map<String, Object> getMapParams(HttpRequest request) {
		Map<String, Object> params = new HashMap<>();
		if (request.getParams() != null) {
			Map<String, Object[]> paramArray = request.getParams();
			for (String key : paramArray.keySet()) {
				Object[] param = paramArray.get(key);
				if (param != null && param.length >= 1) {
					params.put(key, param[0]);
				}
			}
		}
		return params;
	}

	/**
	 * 
	 * @param request
	 * @return
	 * @author tanyaowu
	 */
	public static IpInfo getIpInfo(HttpRequest request) {
		String ip = request.getClientIp();
		return IpInfoService.ME.save(ip);
	}

	/**
	 * 
	 * @param request
	 * @return
	 * @author tanyaowu
	 */
	public static UserAgent getUserAgent(HttpRequest request) {
		String userAgentStr = request.getUserAgent();
		return UserAgentService.ME.save(userAgentStr);
	}

	/**
	 * 返回资源路径
	 * @param path
	 * @return
	 */
	public static String resUrl(String path) {
		if (StrUtil.startWithIgnoreCase(path, "http://") || StrUtil.startWithIgnoreCase(path, "https://") || StrUtil.startWithIgnoreCase(path, "//")) {
			return path;
		}
		return Const.RES_SERVER + path;
	}
	
	/**
	 * 是否是外部链接
	 * @param href 形如："http://xxx.com"， "//xxx.com"， "/ddd/x.png"
	 * @return true：是外部链接
	 * @author tanyaowu
	 */
	public static boolean isOtherSite(String href) {
	    if (href.startsWith("//")) {
		href = "https:" + href;
	    }
	    if (href.startsWith("http://") || href.startsWith("https://")) {
		if (!href.startsWith(Const.SITE) && !href.startsWith(Const.RES_SERVER)) {
		    return true;
		}
	    }
	    return false;
	}
}
