
package org.tio.sitexxx.web.server.http;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HeaderName;
import org.tio.http.common.HeaderValue;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.http.common.session.limiter.SessionRateLimiter;
import org.tio.http.common.session.limiter.SessionRateVo;
import org.tio.sitexxx.service.service.conf.IpWhiteListService;
import org.tio.sitexxx.service.vo.AppCode;
import org.tio.utils.SystemTimer;
import org.tio.utils.json.Json;
import org.tio.utils.resp.Resp;

import cn.hutool.core.util.StrUtil;

public class WebApiSessionRateLimiter implements SessionRateLimiter {
	private static Logger log = LoggerFactory.getLogger(WebApiSessionRateLimiter.class);

	public static final WebApiSessionRateLimiter me = new WebApiSessionRateLimiter();

	/**
	 * key:   访问路径<br>
	 * value: 相邻两次访问必须间隔的最小时间，单位：毫秒<br>
	 */
	private final Map<String, Integer> intervalMap = new HashMap<>();

	/**
	 * key:   访问路径的前缀<br>
	 * value: 相邻两次访问必须间隔的最小时间，单位：毫秒<br>
	 */
	private final Map<String, Integer> prefixMap = new HashMap<>();

	/**
	 * 每分钟允许访问的次数
	 */
	private final Map<String, Integer> allowCountMap = new HashMap<>();

	private HttpResponse response;

	private WebApiSessionRateLimiter() {

		intervalMap.put("/logout", 2000);

		intervalMap.put("/register/submit", 5000); //注册
		intervalMap.put("/register/retrievePwd", 5000); //找回密码步骤一，发邮件
		intervalMap.put("/register/setNewPwd", 5000); //找回密码步骤二，设置密码

		
		
		intervalMap.put("/wx/friend/applyFriend", 4000);// 请求添加好友
		intervalMap.put("/chat/createGroup",3000);//3秒内只能创建一个群
		//		intervalMap.put("/register/emailRegister", 15000);

		//		allowCountMap.put("/search", 12);
		allowCountMap.put("/login", 12);
		//		allowCountMap.put("/logout", 12);
		//		allowCountMap.put("/register/submit", 12);

		allowCountMap.put("/user/search", 30); //用户搜索


		allowCountMap.put("/stat/ip", 15); // ip流量明细

		allowCountMap.put("/wx/friend/replyApplyFriend", 60); // 同意或拒绝某人添加自己为好友
		allowCountMap.put("/chat/list", 60);

		response = new HttpResponse();
		try {
			response.setBody(Json.toJson(Resp.fail("The operation is too fast, please operate late").code(AppCode.ForbidOper.REFUSE)).getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			log.error("", e);
		}
		response.addHeader(HeaderName.Content_Type, HeaderValue.Content_Type.TEXT_PLAIN_JSON);
	}

	@Override
	public boolean allow(HttpRequest request, SessionRateVo sessionRateVo) {
		if (IpWhiteListService.isWhiteIp(request.getClientIp())) {
			return true;
		}

		String path = request.getRequestLine().getPath();

		Integer allowCount = allowCountMap.get(path);
		if (allowCount == null) {
			allowCount = 120;
		}
		if (sessionRateVo.getAccessCount().get() > allowCount) {
			log.error("path({}), access count of curr session({}) is greater than allow count({})", path, sessionRateVo.getAccessCount().get(), allowCount);
			return false;
		}

		Integer iv = intervalMap.get(path);
		if (iv == null) {
			if (prefixMap.size() > 0) {
				Set<Entry<String, Integer>> set = prefixMap.entrySet();
				for (Entry<String, Integer> entry : set) {
					if (StrUtil.startWith(path, entry.getKey())) {
						iv = entry.getValue();
						break;
					}
				}
			}
		}

		if (iv != null) {
			if (SystemTimer.currTime - sessionRateVo.getLastAccessTime() < iv) {
				log.error("path({}), curr time ({}) - last access time ({}) < iv({})", path, SystemTimer.currTime, sessionRateVo.getLastAccessTime(), iv);
				return false;
			}
		}

		return true;
	}

	@Override
	public HttpResponse response(HttpRequest request, SessionRateVo sessionRateVo) {
		return response;
		//		return Resps.json(request, Resp.fail("操作过快，请稍后再操作").code(AppCode.ForbidOper.REFUSE));
	}

}
