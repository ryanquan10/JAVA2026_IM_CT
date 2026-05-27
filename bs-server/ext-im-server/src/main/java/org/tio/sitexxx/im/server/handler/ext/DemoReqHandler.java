
package org.tio.sitexxx.im.server.handler.ext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.sitexxx.im.common.Command;
import org.tio.sitexxx.im.common.CommandHandler;
import org.tio.sitexxx.im.common.ImPacket;
import org.tio.sitexxx.im.common.ImSessionContext;
import org.tio.sitexxx.im.common.bs.demo.DemoNtf;
import org.tio.sitexxx.im.common.bs.demo.DemoReq;
import org.tio.sitexxx.im.common.utils.ImUtils;
import org.tio.sitexxx.im.server.Ims;
import org.tio.sitexxx.im.server.handler.AbsImServerHandler;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.vo.SimpleUser;
import org.tio.utils.json.Json;

/**
 * 
 * @author tanyaowu
 */
@CommandHandler(Command.DemoReq)
public class DemoReqHandler extends AbsImServerHandler {

	private static Logger log = LoggerFactory.getLogger(DemoReqHandler.class);

	public DemoReqHandler() {
	}

	public boolean needLogin() {
		return false;
	}

	@SuppressWarnings("unused")
	@Override
	public void handler(ImPacket packet, ChannelContext channelContext, boolean isWebsocket, User curr, SimpleUser currSimpleUser) throws Exception {
		ImSessionContext imSessionContext = ImUtils.getImSessionContext(channelContext);
		DemoReq demoReq = Json.toBean(packet.getBodyStr(), DemoReq.class);
		log.warn("receive");

		DemoNtf demoNtf = new DemoNtf();
		ImPacket imPacket = new ImPacket(Command.DemoNtf, demoNtf);
		Ims.send(channelContext, imPacket);
	}
}
