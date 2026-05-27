/*
 * xgync本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动ymmxalundd
 */
package org.tio.clu.client;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.client.ClientChannelContext;
import org.tio.client.TioClientConfig;
import org.tio.clu.common.Clu;
import org.tio.clu.common.CluConst;
import org.tio.clu.common.CluSessionContext;
import org.tio.server.TioServerConfig;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ArrayUtil;

/**
 * @author tanyaowu 2020-9-7 17:00:11
 */
public class Cc extends Clu {
	@SuppressWarnings("unused")
	private static Logger			log						= LoggerFactory.getLogger(Cc.class);
	private static AtomicInteger	NEXTCHANNELCONTEXT_BASE	= new AtomicInteger();
	public static final String		CLIENT_ID				= UUID.fastUUID().toString(true);

	/**
	 * 
	 * @param clientChannelContext
	 * @return
	 * @author tanyaowu
	 */
	public static BindedData getBindedData(TioClientConfig tioClientConfig) {
		return (BindedData) tioClientConfig.get(CluConst.TIO_CLU_CLIENTCONFIG_BINDDATA_KEY);
	}

	/**
	 * 
	 * @param clientChannelContext
	 * @return
	 * @author tanyaowu
	 */
	public static String getCgid(ClientChannelContext clientChannelContext) {
		return Cc.getCluClientStarter(clientChannelContext).getCgId();
	}

	/**
	 * 获取CluClientSessionContext
	 * 
	 * @param clientChannelContext
	 * @return
	 * @author tanyaowu
	 */
	public static CluClientSessionContext getCluClientSessionContext(ClientChannelContext clientChannelContext) {
		CluClientSessionContext ret = (CluClientSessionContext) getCluSessionContext(clientChannelContext);
		return ret;
	}

	/**
	 * 
	 * @param clientChannelContext
	 * @return
	 * @author tanyaowu
	 */
	public static CluClientStarter getCluClientStarter(ClientChannelContext clientChannelContext) {
		return (CluClientStarter) clientChannelContext.tioConfig.get(CluConst.TIO_CLU_CLIENTCONFIG_CLUCLIENTSTARTER_KEY);
	}

	/**
	 * 
	 * @param tioConfig
	 * @return
	 * @author tanyaowu
	 */
	public static CluClientStarter getCluClientStarter(TioClientConfig tioClientConfig) {
		return (CluClientStarter) tioClientConfig.get(CluConst.TIO_CLU_CLIENTCONFIG_CLUCLIENTSTARTER_KEY);
	}

	public static void main(String[] args) {
		for (int i = 0; i < 55; i++) {
			int x = NEXTCHANNELCONTEXT_BASE.getAndIncrement() % 5;
			System.out.println(x);
		}
	}

	public static ClientChannelContext next(ClientChannelContext[] clientChannelContexts) {
		if (ArrayUtil.isEmpty(clientChannelContexts)) {
			return null;
		}
		int index = NEXTCHANNELCONTEXT_BASE.getAndIncrement() % clientChannelContexts.length;// RandomUtil.randomInt(0,
		ClientChannelContext ret = clientChannelContexts[index];
		CluSessionContext cluSessionContext = Clu.getCluSessionContext(ret);
		if (cluSessionContext.isHandshaked()) {
			return ret;
		}

		for (int i = 0; i < clientChannelContexts.length; i++) {
			ret = clientChannelContexts[i];
			cluSessionContext = Clu.getCluSessionContext(ret);
			if (cluSessionContext.isHandshaked()) {
				return ret;
			}
		}
		return null;
	}

	public static ClientChannelContext next(TioClientConfig tioClientConfig) {
		return next(Cc.getCluClientStarter(tioClientConfig).getClientChannelContexts());
	}

	public static ClientChannelContext next(TioServerConfig bsTioConfig) {
		return next(bsTioConfig.getCluClientChannelContexts());
	}

	/**
	 * 
	 * @author tanyaowu
	 */
	public Cc() {
	}
}
