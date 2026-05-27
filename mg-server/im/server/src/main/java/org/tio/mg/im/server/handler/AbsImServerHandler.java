
package org.tio.mg.im.server.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.mg.im.common.ImPacket;
import org.tio.mg.im.server.utils.ImUtils;
import org.tio.mg.service.model.main.User;
import org.tio.mg.service.service.base.UserRoleService;
import org.tio.mg.service.vo.SimpleUser;

/**
 * @author tanyaowu 
 * 2019年9月11日 下午8:57:40
 */
public abstract class AbsImServerHandler implements ImServerHandler {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(AbsImServerHandler.class);

	/** 
	 * @param packet
	 * @param channelContext
	 * @param isWebsocket
	 * @throws Exception
	 * @author tanyaowu
	 */
	@Override
	public void handler(ImPacket packet, ChannelContext channelContext, boolean isWebsocket) throws Exception {
		User curr = ImUtils.getUser(channelContext);
		if (needLogin()) {
			if (curr == null) {
				ImUtils.pleaseLogin(channelContext, "请登录");
				return;
			}
			
			//状态非法，则啥也不提示
			boolean flag = UserRoleService.checkUserStatus(curr.getId());
			if (!flag) {
				return;
			}
		}
		SimpleUser currSimpleUser = ImUtils.getSimpleUser(channelContext);
		handler(packet, channelContext, isWebsocket, curr, currSimpleUser);
	}

	/**
	 * 是否需要登录， true：需要登录，false：不需要登录
	 * 大部分都需要登录，所以给个默认的方法
	 * @return
	 * @author tanyaowu
	 */
	public boolean needLogin() {
		return true;
	}
	/**
	 * 
	 * @param packet
	 * @param channelContext
	 * @param isWebsocket
	 * @param curr 当前User对象，没登录则为null
	 * @param currSimpleUser 当前SimpleUser对象，没登录则为null
	 * @throws Exception
	 * @author tanyaowu
	 */
	public abstract void handler(ImPacket packet, ChannelContext channelContext, boolean isWebsocket, User curr, SimpleUser currSimpleUser) throws Exception;
}
