/*
 * vcrezjdv本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动pzslfqnbn
 */
/*
 * vcrezjdv本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动pzslfqnbn
 * grantinfo
 */
package org.tio.clu.client.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.client.ClientChannelContext;
import org.tio.clu.client.Cc;
import org.tio.clu.client.handler.base.AbsCluClientHandler;
import org.tio.clu.common.BindType;
import org.tio.clu.common.Clu;
import org.tio.clu.common.CluPacket;
import org.tio.clu.common.Command;
import org.tio.clu.common.CommandHandler;
import org.tio.clu.common.bs.UnbindNtf;
import org.tio.core.intf.Packet;
import org.tio.server.TioServerConfig;
import org.tio.utils.lock.SetWithLock;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @author tanyaowu
 */
@CommandHandler(Command.UnbindNtf)
public class UnbindNtfHandler extends AbsCluClientHandler {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(UnbindNtfHandler.class);

	public UnbindNtfHandler() {
	}

	@Override
	public void myHandler(CluPacket cluPacket, ClientChannelContext clientChannelContext, TioServerConfig bsTioServerConfig, Class<? extends Packet> bsPacketClass)
	        throws Exception {
		UnbindNtf unbindNtf = Clu.getBodyObj(cluPacket, UnbindNtf.class);

		byte tt = unbindNtf.getBt();
		BindType bindType = BindType.from(tt);

		String v = unbindNtf.getV();
		String[] vs = unbindNtf.getVs();

		SetWithLock<String> setWithLock = Cc.getBindedData(clientChannelContext.getTioConfig()).getBindSet(bindType);

		if (StrUtil.isNotBlank(v)) {
			setWithLock.remove(v);
		}
		if (ArrayUtil.isNotEmpty(vs)) {
			setWithLock.removeAll(vs);
		}
	}

}
