
package org.tio.sitexxx.im.server.utils;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.sitexxx.im.common.Command;
import org.tio.sitexxx.im.common.ImPacket;
import org.tio.sitexxx.im.common.bs.MsgTipNtf;
import org.tio.sitexxx.im.server.Ims;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.service.conf.ConfService;
import org.tio.sitexxx.service.service.conf.IpWhiteListService;
import org.tio.sitexxx.service.vo.Const;
import org.tio.utils.cache.ICache;

import cn.hutool.core.util.StrUtil;

/**
 * @author tanyaowu 
 * 2016年11月6日 下午2:39:59
 */
public class ImUtils extends org.tio.sitexxx.im.common.utils.ImUtils {
	private static Logger log = LoggerFactory.getLogger(ImUtils.class);

	/**
	 * 是否允许访问websocket
	 * @param request
	 * @param httpResponse
	 * @param channelContext
	 * @return true: 允许访问; false: 不允许访问
	 * @author tanyaowu
	 */
	public static boolean isAllowAccessWs(HttpRequest request, HttpResponse httpResponse, ChannelContext channelContext) {
		//看看access_token start
		String token = request.getParam(Const.Http.SESSION_COOKIE_NAME); // session id
		int accessTokenOn = ConfService.getInt("use.access.token.pc", 2);
		if (accessTokenOn == 1 && !IpWhiteListService.isWhiteIp(request.getClientIp())) {
			if (StrUtil.isBlank(token)) {
				return false;
			}

			//检查tio_access_token
			String accessToken = request.getParam(Const.AccessToken.COOKIENAME_FOR_ACCESSTOKEN);//request.getCookie(Const.AccessToken.COOKIENAME_FOR_ACCESSTOKEN);
			boolean needNewAccessToken = true;

			if (StrUtil.isNotBlank(accessToken)) {
				ICache cache2 = Caches.getCache(CacheConfig.TIO_ACCESS_TOKEN);
				String valueInCache = cache2.get(token, String.class);
				//					if (accessToken.startsWith(request.getClientIp())) {
				if (Objects.equals(accessToken, valueInCache)) {
					needNewAccessToken = false;
				} else {
					log.info("access_token这一关没过");
				}
				//					}

			}

			if (needNewAccessToken) {
				return false;//Resps.json(request, Resp.fail().code(AppCode.ForbidOper.NEED_ACCESS_TOKEN));
			}

		}
		//看看access_token end

		return true;
	}

	/**
	 * 发送消息提醒
	 * @param channelContext
	 * @param msg
	 * @param level
	 * @param code
	 */
	public static void sendMsgTip(ChannelContext channelContext, String msg, Short level, Short code) {
		MsgTipNtf msgTipNtf = new MsgTipNtf();
		msgTipNtf.setMsg(msg);

		if (level != null) {
			msgTipNtf.setLevel(level);
		}
		msgTipNtf.setCode(code);
		ImPacket imPacket = new ImPacket(Command.MsgTip, msgTipNtf);
		Ims.send(channelContext, imPacket);
	}

	/**
	 * 让用户登录
	 * @param channelContext
	 * @param msg
	 */
	public static void pleaseLogin(ChannelContext channelContext, String msg) {
		sendMsgTip(channelContext, msg, MsgTipNtf.Level.INFO, MsgTipNtf.Code.PLEASE_LOGIN);
	}

	/**
	 * 警告
	 * @param channelContext
	 * @param msg
	 * @param code
	 */
	public static void warn(ChannelContext channelContext, String msg, Short code) {
		sendMsgTip(channelContext, msg, MsgTipNtf.Level.WARN, code);
	}

	/**
	 * 提醒
	 * @param channelContext
	 * @param msg
	 * @param code
	 */
	public static void info(ChannelContext channelContext, String msg, Short code) {
		sendMsgTip(channelContext, msg, MsgTipNtf.Level.INFO, code);
	}
}
