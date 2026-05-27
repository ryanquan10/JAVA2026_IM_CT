
package org.tio.mg.im.server.handler;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.mg.im.common.Command;
import org.tio.mg.im.common.CommandHandler;
import org.tio.mg.im.common.ImPacket;
import org.tio.mg.im.server.utils.ImUtils;
import org.tio.mg.service.model.main.User;
import org.tio.sitexxx.service.vo.Devicetype;

/**
 * 心跳包处理者
 * @author tanyaowu 
 * 2016年9月13日 上午9:53:30
 */
@CommandHandler(Command.HeartbeatReq)
public class HeartbeatReqHandler implements ImServerHandler {
	private static Logger log = LoggerFactory.getLogger(HeartbeatReqHandler.class);

	public static final HeartbeatReqHandler me = new HeartbeatReqHandler();

	public HeartbeatReqHandler() {
	}

	@Override
	public void handler(ImPacket packet, ChannelContext channelContext, boolean isWebsocket) throws Exception {
		User curr = ImUtils.getUser(channelContext);
		if(curr != null) {
			Devicetype devicetype = ImUtils.getDevicetype(channelContext);
			try {
				if(Objects.equals(devicetype.getValue(), Devicetype.IOS.getValue()) || Objects.equals(devicetype.getValue(), Devicetype.ANDROID.getValue())) {
					devicetype = Devicetype.APP;
				}
			} catch (Exception e) {
				log.error(e.getMessage(),e);
			}
		}
	}

}
