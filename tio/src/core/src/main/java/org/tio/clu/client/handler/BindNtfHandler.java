/*
 * wilbiueckhxd本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动jdfmbkzerc
 */
/*
 * wilbiueckhxd本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动jdfmbkzerc
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
import org.tio.clu.common.bs.BindNtf;
import org.tio.core.intf.Packet;
import org.tio.server.TioServerConfig;
import org.tio.utils.lock.SetWithLock;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @author tanyaowu
 */
@CommandHandler(Command.BindNtf)
public class BindNtfHandler extends AbsCluClientHandler {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(BindNtfHandler.class);

	public BindNtfHandler() {
	}

	/**
	 * @param packet
	 * @param clientChannelContext
	 * @param bsTioServerConfig
	 * @param bsPacketClass
	 * @throws Exception
	 * @author tanyaowu
	 */
	@Override
	public void myHandler(CluPacket cluPacket, ClientChannelContext clientChannelContext, TioServerConfig bsTioServerConfig, Class<? extends Packet> bsPacketClass)
	        throws Exception {
		BindNtf bindNtf = Clu.getBodyObj(cluPacket, BindNtf.class);

		byte tt = bindNtf.getBt();
		BindType bindType = BindType.from(tt);

		String v = bindNtf.getV();
		String[] vs = bindNtf.getVs();

		SetWithLock<String> setWithLock = Cc.getBindedData(clientChannelContext.getTioConfig()).getBindSet(bindType);

		if (StrUtil.isNotBlank(v)) {
			setWithLock.add(v);
		}
		if (ArrayUtil.isNotEmpty(vs)) {
			setWithLock.addAll(vs);
		}
	}

}
