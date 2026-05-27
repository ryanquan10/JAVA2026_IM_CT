/*
 * bpuqgvaquajuy本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动jnpcznled
 */
/*
 * bpuqgvaquajuy本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动jnpcznled
 * grantinfo
 */
package org.tio.clu.client.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.client.ClientChannelContext;
import org.tio.clu.client.Cc;
import org.tio.clu.client.handler.base.AbsCluClientHandler;
import org.tio.clu.common.Clu;
import org.tio.clu.common.CluPacket;
import org.tio.clu.common.Command;
import org.tio.clu.common.CommandHandler;
import org.tio.clu.common.bs.UpdateBsNodeResp;
import org.tio.core.intf.Packet;
import org.tio.server.TioServerConfig;

/**
 * @author tanyaowu
 */
@CommandHandler(Command.UpdateBsNodeResp)
public class UpdateBsNodeRespHandler extends AbsCluClientHandler {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(UpdateBsNodeRespHandler.class);

	public UpdateBsNodeRespHandler() {
	}

	@Override
	public void myHandler(CluPacket cluPacket, ClientChannelContext clientChannelContext, TioServerConfig bsTioServerConfig, Class<? extends Packet> bsPacketClass)
	        throws Exception {
		if (bsTioServerConfig == null) {
			return;
		}

		UpdateBsNodeResp updateBsNodeResp = Clu.getBodyObj(cluPacket, UpdateBsNodeResp.class);
		if (updateBsNodeResp.isOk()) {
			Cc.getBindedData(clientChannelContext.getTioConfig()).setLastUpdateBsNodeReq(updateBsNodeResp.getUpdateBsNodeReq());
		}
	}
}
