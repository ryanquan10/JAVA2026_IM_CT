
/**
 * 
 */
package org.tio.mg.web.server.utils;

import java.util.Date;

import org.tio.http.common.HttpConst;
import org.tio.http.common.HttpRequest;
import org.tio.mg.service.model.stat.TioIpPullblackLog;
import org.tio.mg.service.service.base.IpInfoService;
import org.tio.mg.service.service.base.TioIpPullblackLogService;
import org.tio.mg.service.service.conf.IpBlackListService;
import org.tio.sitexxx.service.vo.Const;

/**
 * @author tanyaowu
 *
 */
public class TioIpPullblackUtils {

	/**
	 * 
	 */
	public TioIpPullblackUtils() {
	}

	public static void addToBlack(HttpRequest request, String ip, String remark, short type) {
		Integer currId = WebUtils.currUserId(request);

		TioIpPullblackLog tioIpPullblackLog = new TioIpPullblackLog();
		tioIpPullblackLog.setIp(ip);
		tioIpPullblackLog.setIpid(IpInfoService.ME.save(request.getClientIp()).getId());
		tioIpPullblackLog.setRemark(remark);
		tioIpPullblackLog.setServer(Const.MY_IP);
		tioIpPullblackLog.setServerport(request.getChannelContext().getServerNode().getPort());
		tioIpPullblackLog.setTime(new Date());
		tioIpPullblackLog.setType(type);

		tioIpPullblackLog.setSessionid(request.getHttpSession().getId());
		tioIpPullblackLog.setCookie(request.getHeader(HttpConst.RequestHeaderKey.Cookie));
		tioIpPullblackLog.setInitpath(request.requestLine.getInitPath());
		tioIpPullblackLog.setPath(request.requestLine.getPath());
		tioIpPullblackLog.setRequestline(request.requestLine.toString());
		tioIpPullblackLog.setUid(currId);

		TioIpPullblackLogService.ME.addToBlack(tioIpPullblackLog);

		IpBlackListService.me.save(ip, remark);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
