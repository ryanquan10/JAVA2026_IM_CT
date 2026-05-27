/*
 * pluzzvm本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动mrpmnwvz
 * grantinfo
 */
package org.tio.clu.client.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.client.ClientChannelContext;
import org.tio.clu.client.Cc;
import org.tio.clu.client.bs.TransferListener;
import org.tio.clu.client.handler.base.AbsCluClientHandler;
import org.tio.clu.common.BindType;
import org.tio.clu.common.Clu;
import org.tio.clu.common.CluPacket;
import org.tio.clu.common.Command;
import org.tio.clu.common.CommandHandler;
import org.tio.clu.common.bs.BindUserToGroup;
import org.tio.clu.common.bs.TransferNtf;
import org.tio.clu.common.utils.FstUtils;
import org.tio.core.Tio;
import org.tio.core.intf.Packet;
import org.tio.server.TioServerConfig;
import org.tio.utils.hutool.StrUtil;

/**
 * @author tanyaowu
 */
@CommandHandler(Command.TransferNtf)
public class TransferNtfHandler extends AbsCluClientHandler {

	private static Logger log = LoggerFactory.getLogger(TransferNtfHandler.class);

	public TransferNtfHandler() {
	}

	@Override
	public void myHandler(CluPacket cluPacket, ClientChannelContext clientChannelContext, TioServerConfig bsTioServerConfig, Class<? extends Packet> bsPacketClass)
	        throws Exception {
		if (bsTioServerConfig == null) {
			return;
		}

		TransferNtf transferNtf = Clu.getBodyObj(cluPacket, TransferNtf.class);

		Packet p = FstUtils.asObject(transferNtf.getP(), Packet.class);
		p.setFromClu(true);

		byte tt = transferNtf.getBt();
		BindType bindType = BindType.from(tt);

		TransferListener transferListener = bsTioServerConfig.getTransferListener();
		if (transferListener != null) {
			boolean con = transferListener.onBeforeTransfer(p, bindType, transferNtf);
			if (!con) {
				return;
			}
		}

		String v = transferNtf.getV();
		String[] vs = transferNtf.getVs();

		switch (bindType) {
		case Group:
			Tio.sendToGroup(bsTioServerConfig, v, vs, p, false);
			break;
		case User:
			Tio.sendToUser(bsTioServerConfig, v, vs, p, false);
			break;
		case Token:
			Tio.sendToToken(bsTioServerConfig, v, vs, p, false);
			break;
		case Ip:
			Tio.sendToIp(bsTioServerConfig, v, vs, p, false);
			break;
		case BsId:
			Tio.sendToBsId(bsTioServerConfig, v, vs, p, false);
			break;
		case ChannelId:
			Tio.sendToId(bsTioServerConfig, v, vs, p, false);
			break;
		case All:
			Tio.sendToAll(bsTioServerConfig, p);
			break;
		case BindUserToGroup:
			CluPacket clup = (CluPacket)p;
			BindUserToGroup bindUserToGroup = Clu.getBodyObj(clup, BindUserToGroup.class);
			if (!StrUtil.equals(bindUserToGroup.getFromClientId(), Cc.CLIENT_ID)) {
				Tio.bindGroup(bsTioServerConfig, bindUserToGroup.getUserid(), bindUserToGroup.getGroup(), false);
			}
			break;
		default:
			log.error("can not find by BindType[{}]", bindType);
			break;

		}
		if (transferListener != null) {
			transferListener.onAfterTransfer(p, bindType, transferNtf);
		}

	}

}
