/*
 * scqwlke本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动ltfaskqqevyif
 */
package org.tio.client;

import java.util.HashSet;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.client.intf.TioClientHandler;
import org.tio.client.intf.TioClientListener;
import org.tio.core.ChannelContext;
import org.tio.core.TioConfig;
import org.tio.core.intf.TioHandler;
import org.tio.core.intf.TioListener;
import org.tio.core.ssl.SslConfig;
import org.tio.utils.lock.SetWithLock;
import org.tio.utils.thread.pool.SynThreadPoolExecutor;

/**
 *
 * @author tanyaowu 2017年4月1日 上午9:31:31
 */
public class TioClientConfig extends TioConfig {
	private static final long	serialVersionUID	= 158156605759779013L;
	static Logger				log					= LoggerFactory.getLogger(TioClientConfig.class);
	private TioClientHandler	tioClientHandler	= null;
	private TioClientListener	tioClientListener	= null;
	protected ReconnConf		reconnConf;																// 重连配置

	public final SetWithLock<ChannelContext>	connecteds	= new SetWithLock<ChannelContext>(new HashSet<ChannelContext>());
	public final SetWithLock<ChannelContext>	closeds		= new SetWithLock<ChannelContext>(new HashSet<ChannelContext>());

	/**
	 * 不重连
	 * 
	 * @param tioHandler
	 * @param tioListener
	 * @author tanyaowu
	 */
	public TioClientConfig(TioClientHandler tioHandler, TioClientListener tioListener) {
		this(tioHandler, tioListener, null);
	}

	/**
	 * 
	 * @param tioHandler
	 * @param tioListener
	 * @param reconnConf  不用框架自动重连，就传null
	 */
	public TioClientConfig(TioClientHandler tioHandler, TioClientListener tioListener, ReconnConf reconnConf) {
		this(tioHandler, tioListener, reconnConf, null, null);
	}

	/**
	 * 
	 * @param tioHandler
	 * @param tioListener
	 * @param reconnConf    不用框架自动重连，就传null
	 * @param tioExecutor
	 * @param groupExecutor
	 */
	public TioClientConfig(TioClientHandler tioHandler, TioClientListener tioListener, ReconnConf reconnConf, SynThreadPoolExecutor tioExecutor, ThreadPoolExecutor groupExecutor) {
		super(tioExecutor, groupExecutor);
		this.groupStat = new ClientGroupStat();
		this.setTioClientHandler(tioHandler);
		this.setTioClientListener(tioListener);

		this.reconnConf = reconnConf;
	}

	/**
	 * @see org.tio.core.TioConfig#getTioHandler()
	 *
	 * @return
	 * @author tanyaowu 2016年12月20日 上午11:33:46
	 *
	 */
	@Override
	public TioHandler getTioHandler() {
		return this.getTioClientHandler();
	}

	/**
	 * @see org.tio.core.TioConfig#getTioListener()
	 *
	 * @return
	 * @author tanyaowu 2016年12月20日 上午11:33:46
	 *
	 */
	@Override
	public TioListener getTioListener() {
		return this.getTioClientListener();
	}

	/**
	 * @return the tioClientHandler
	 */
	public TioClientHandler getTioClientHandler() {
		return tioClientHandler;
	}

	/**
	 * @return the tioClientListener
	 */
	public TioClientListener getTioClientListener() {
		return tioClientListener;
	}

	/**
	 * 
	 * @return
	 * @author tanyaowu
	 */
	public ReconnConf getReconnConf() {
		return reconnConf;
	}

	/**
	 * @return
	 * @author tanyaowu
	 */
	@Override
	public boolean isServer() {
		return false;
	}

	/**
	 * @param tioClientHandler the tioClientHandler to set
	 */
	public void setTioClientHandler(TioClientHandler tioClientHandler) {
		this.tioClientHandler = tioClientHandler;
	}

	/**
	 * @param tioClientListener the tioClientListener to set
	 */
	public void setTioClientListener(TioClientListener tioClientListener) {
		this.tioClientListener = tioClientListener;
		if (this.tioClientListener == null) {
			this.tioClientListener = new DefaultTioClientListener();
		}
	}

	/**
	 * @param reconnConf the reconnConf to set
	 */
	public void setReconnConf(ReconnConf reconnConf) {
		this.reconnConf = reconnConf;
	}

	@Override
	public String toString() {
		return "TioClientConfig [name=" + name + "]";
	}

	/**
	 * 使用ssl访问
	 * 
	 * @throws Exception
	 * @author tanyaowu
	 */
	public void useSsl() throws Exception {
		SslConfig sslConfig = SslConfig.forClient();
		setSslConfig(sslConfig);
	}
}
