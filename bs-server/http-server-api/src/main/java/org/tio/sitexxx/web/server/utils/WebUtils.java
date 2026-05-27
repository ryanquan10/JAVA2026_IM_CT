
package org.tio.sitexxx.web.server.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.UploadFile;
import org.tio.http.common.session.HttpSession;
import org.tio.sitexxx.service.model.main.Img;
import org.tio.sitexxx.service.model.main.IpInfo;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.UserAgent;
import org.tio.sitexxx.service.model.main.UserToken;
import org.tio.sitexxx.service.service.ImgService;
import org.tio.sitexxx.service.service.base.IpInfoService;
import org.tio.sitexxx.service.service.base.UserAgentService;
import org.tio.sitexxx.service.service.base.UserRoleService;
import org.tio.sitexxx.service.service.base.UserService;
import org.tio.sitexxx.service.service.base.UserTokenService;
import org.tio.sitexxx.service.topic.Topics;
import org.tio.sitexxx.service.utils.ImgUtils;
import org.tio.sitexxx.service.utils.UploadUtils;
import org.tio.sitexxx.service.vo.ClearHttpCache;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.RequestExt;
import org.tio.sitexxx.service.vo.RequestKey;
import org.tio.sitexxx.service.vo.SessionExt;
import org.tio.sitexxx.service.vo.SessionKey;
import org.tio.sitexxx.web.server.init.WebApiInit;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpResponse;

/**
 * @author tanyaowu
 * 2016年8月10日 上午10:59:58
 */
public class WebUtils {
	private static Logger		log			= LoggerFactory.getLogger(WebUtils.class);
	private static UserService	userService	= UserService.ME;

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
		String contextpath = org.tio.sitexxx.service.vo.Const.API_CONTEXTPATH;
		String suffix = org.tio.sitexxx.service.vo.Const.API_SUFFIX;

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
	 * 获取当前用户
	 * @param session
	 * @return
	 * @author tanyaowu
	 */
	public static User currUser(HttpRequest request) {
		Integer userid = currUserId(request);
		if (userid != null) {
			User user = userService.getById(userid);
			return UserRoleService.checkUserStatus(user) ? user : null;
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
	 * 是否是外部链接
	 * @param href 形如："http://xxx.com"， "//xxx.com"， "/ddd/x.png"
	 * @return true：是外部链接
	 * @author tanyaowu
	 */
	public static boolean isOtherSite(String href) {
		if (org.tio.utils.hutool.StrUtil.startWithIgnoreCase(href, "//")) {
			href = "https:" + href;
		}
		if (href.startsWith("http://") || href.startsWith("https://")) {
			if (!href.startsWith(Const.SITE) && !href.startsWith(Const.RES_SERVER)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 将图片存到本地，并返回图片的url地址
	 * @param curr
	 * @param request
	 * @param imgSrc
	 * @param subDir
	 * @param comeFrom
	 * @return 形如:"https://res.sxx.xx/ddd/ddd.png"
	 * @author tanyaowu
	 */
	public static Img processImg(User curr, HttpRequest request, String imgSrc, String subDir, short comeFrom, int maxWidth) {
		imgSrc = StrUtil.trim(imgSrc);
		if (org.tio.utils.hutool.StrUtil.startWithIgnoreCase(imgSrc, "//")) {
			imgSrc = "https:" + imgSrc;
		}
		String initSrc = imgSrc;

		if (imgSrc.startsWith("data:image")) { // base64
			try {
				String ext = StrUtil.subBetween(imgSrc, "data:image/", ";");
				String base64ImgData = StrUtil.subAfter(imgSrc, "base64,", false);
				byte[] bs = Base64.decode(base64ImgData);
				Img img = processImg(curr, request, bs, subDir, comeFrom, ext, null, maxWidth, true);
				if (img == null) {
					return img;
				}
				//		    String newImgSrc = WebUtils.resUrl(img.getCoverurl());
				//		    imgSrc = newImgSrc;
				return img;

			} catch (Exception e) {
				log.error("", e);
				return null;
			}
		} else {
			boolean fromOtherSite = WebUtils.isOtherSite(imgSrc);
			if (!fromOtherSite) {
				if (imgSrc.startsWith("/res/emoji") || imgSrc.startsWith("/res/editormd/plugins")) {
					imgSrc = Const.SITE + imgSrc;
				} else {
					imgSrc = UploadUtils.resUrl(imgSrc);
				}
			} 
			
			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				// HttpUtil.download(imgSrc, out, true);

				final HttpResponse response = cn.hutool.http.HttpRequest.get(imgSrc).executeAsync();
				if (false == response.isOk()) {
					throw new HttpException("Server response error with status code: [{}]", response.getStatus());
				}
				response.writeBody(out, true, null);
				String contentType = response.header("Content-Type");

				byte[] bytes = out.toByteArray();
				Img img = processImg(curr, request, bytes, subDir, comeFrom, FileUtil.extName(StrUtil.replace(contentType, "/", ".")), initSrc, maxWidth, fromOtherSite);
				return img;
			} catch (Exception e) {
				log.error("", e);
				return null;
			}
		
		}
	}

	public static Img processImg(User curr, HttpRequest request, byte[] bytes, String subDir, short comeFrom, String extName, String initSrc, int maxWidth, boolean fromOtherSite) throws Exception {
		ByteArrayInputStream input = new ByteArrayInputStream(bytes);
		BufferedImage bi = ImageIO.read(input);
		if (bi == null) {
			return null;
		}

		float scale = ImgUtils.calcScaleWithWidth(maxWidth, bi);
		UploadFile uploadFile = new UploadFile();
		uploadFile.setData(bytes);
		uploadFile.setName(curr.getId() + "." + extName);
		uploadFile.setSize(bytes.length);
		Img img = ImgUtils.processImg(subDir, curr.getId(), uploadFile, scale);
		img.setComefrom(comeFrom);
		img.setStatus((short) 1);
		img.setSession(request.getHttpSession().getId());
		img.setIniturl(initSrc);

		if (fromOtherSite) {
			boolean f = ImgService.me.save(img);

			if (!f) {
				log.error("图片保存失败, {}", img);
				return null;
			}
		}
		
		return img;
	}

	/**
	 * 
	 * @param request
	 * @param href
	 * @return
	 * @author tanyaowu
	 */
	public static String processHref(HttpRequest request, String href) {
		href = StrUtil.trim(href);
		boolean fromOtherSite = WebUtils.isOtherSite(href);
		if (fromOtherSite) {
			try {
				href = "/2/pagePrompt/index.html?linkUrl=" + URLEncoder.encode(href, request.httpConfig.getCharset());
			} catch (UnsupportedEncodingException e) {
				log.error("", e);
			}
			return href;
		}
		return href;
	}
}
