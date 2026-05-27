/*
 * ruuyk本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动umndqcdnjknsdp
 */
/*
 * ruuyk本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动umndqcdnjknsdp
 * grantinfo
 */
package org.tio.clu.client;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.client.ClientChannelContext;
import org.tio.client.TioClientConfig;
import org.tio.client.ReconnConf;
import org.tio.client.TioClient;
import org.tio.client.intf.TioClientHandler;
import org.tio.client.intf.TioClientListener;
import org.tio.clu.client.handler.ScanLocation;
import org.tio.clu.common.Clu;
import org.tio.clu.common.CluConst;
import org.tio.clu.common.CluPacket;
import org.tio.clu.common.Command;
import org.tio.clu.common.bs.BestNodeReq;
import org.tio.clu.common.bs.BestNodeResp;
import org.tio.core.Node;
import org.tio.core.intf.Packet;
import org.tio.server.TioServerConfig;
import org.tio.utils.jfinal.P;

/**
 * @author tanyaowu
 *
 */
public class CluClientStarter {
	private static Logger log = LoggerFactory.getLogger(CluClientStarter.class);

	private static final String TIOCONFIG_NAME = "tio-clu-client";

	public static void main(String[] args) throws Exception {

	}

	private ReconnConf				reconnConf				= null;
	private TioClientHandler		tioClientHandler		= null;
	private TioClientListener		tioClientListener		= null;
	private TioClientConfig			tioClientConfig			= null;
	private TioClient				tioClient				= null;
	private Node					cluServerNode			= null;
	private ClientChannelContext[]	clientChannelContexts	= null;
	private TioServerConfig			bsTioServerConfig		= null;
	private Class<? extends Packet>	bsPacketClass			= null;
	private String					cgId					= null;

	private BindedData bindedData = null;

	// message

	long laststart = System.currentTimeMillis();

	/**
	 * 
	 * @param useSsl
	 * @param bsTioServerConfig
	 * @param cluServerNode
	 * @param bsPacketClass
	 * @throws Exception
	 * @author tanyaowu
	 */
	public CluClientStarter(boolean useSsl, TioServerConfig bsTioServerConfig, Node cluServerNode, Class<? extends Packet> bsPacketClass) throws Exception {
		this.bsTioServerConfig = bsTioServerConfig;
		this.bsPacketClass = bsPacketClass;
		this.reconnConf = new ReconnConf(P.getLong("tio.clu.client.reconn.time", 1000L));
		this.cluServerNode = cluServerNode;
		this.tioClientHandler = new CluTioClientHandler(new PacketDispatcher(new String[] { ScanLocation.class.getPackage().getName() }, bsTioServerConfig, bsPacketClass),
		        bsTioServerConfig);
		this.tioClientListener = new CluTioClientListener(this);

		init(useSsl);
	}

	/**
	 * @return the bindedData
	 */
	public BindedData getBindedData() {
		return bindedData;
	}

	/**
	 * @return the bsPacketClass
	 */
	public Class<? extends Packet> getBsPacketClass() {
		return bsPacketClass;
	}

	/**
	 * @return the bsTioServerConfig
	 */
	public TioServerConfig getBsTioServerConfig() {
		return bsTioServerConfig;
	}

	/**
	 * @return the cgId
	 */
	public String getCgId() {
		return cgId;
	}

	/**
	 * @return the clientChannelContext
	 */
	public ClientChannelContext[] getClientChannelContexts() {
		return clientChannelContexts;
	}

	/**
	 * @return the tioClientConfig
	 */
	public TioClientConfig getTioClientConfig() {
		return tioClientConfig;
	}

	/**
	 * @return the cluServerNode
	 */
	public Node getCluServerNode() {
		return cluServerNode;
	}

	/**
	 * @return the tioClient
	 */
	public TioClient getTioClient() {
		return tioClient;
	}

	/**
	 * 
	 * @param useSsl
	 * @param bsTioServerConfig
	 * @param cluTioClientListener
	 * @param reconnConf
	 * @param cluServerNode
	 * @throws Exception
	 * @author tanyaowu
	 */
	// public CluClientStarter(boolean useSsl, TioServerConfig bsTioServerConfig,
	// CluTioClientListener cluTioClientListener, ReconnConf reconnConf, Node
	// cluServerNode,
	// Class<? extends Packet> bsPacketClass) throws Exception {
	// this.bsTioServerConfig = bsTioServerConfig;
	// this.bsPacketClass = bsPacketClass;
	// this.reconnConf = reconnConf;
	// this.cluServerNode = cluServerNode;
	// this.tioClientHandler = new CluTioClientHandler(new PacketDispatcher(new
	// String[] { ScanLocation.class.getPackage().getName() }, bsTioServerConfig,
	// bsPacketClass),
	// bsTioServerConfig);
	// this.tioClientListener = cluTioClientListener;
	//
	// init(useSsl);
	// }

	private void init(boolean useSsl) throws Exception {
		this.tioClientConfig = new TioClientConfig(this.tioClientHandler, this.tioClientListener, this.reconnConf);
		this.tioClientConfig.setName(TIOCONFIG_NAME);

		this.tioClientConfig.setHeartbeatTimeout(CluConst.HEARTBEAT_TIMEOUT);
		if (useSsl) {
			this.tioClientConfig.useSsl();
		}
		this.tioClient = new TioClient(this.tioClientConfig);
		this.cgId = this.tioClientConfig.getTioUuid().uuid();

		bindedData = new BindedData();
		bindedData.init();

		tioClientConfig.set(CluConst.TIO_CLU_CLIENTCONFIG_BINDDATA_KEY, bindedData);
		tioClientConfig.set(CluConst.TIO_CLU_CLIENTCONFIG_CLUCLIENTSTARTER_KEY, this);
	}




	public void start() throws Exception {

		clientChannelContexts = new ClientChannelContext[P.getInt("tio.clu.client.connection.num", 10)];
		for (int i = 0; i < clientChannelContexts.length; i++) {
			ClientChannelContext clientChannelContext = tioClient.connect(cluServerNode, 5);
			clientChannelContexts[i] = clientChannelContext;
		}

		// 启动定时任务
		startTimeTask();
	}

	public void startTest() {

		// 性能测试
		testBestBsNode();
	}

	private void startTimeTask() {
		Thread cluClientTaskThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					if (bsTioServerConfig != null) {
						ClientChannelContext clientChannelContext = Cc.next(clientChannelContexts);
						if (clientChannelContext == null) {
							log.error("没有可用的连接");
						} else {
							CluClient.updateBsNode(clientChannelContext, bsTioServerConfig);
						}
					}

					try {
						Thread.sleep(5000L);
					} catch (InterruptedException e1) {
						log.error(e1.toString(), e1);
					}
				}
			}
		}, "clu-client-task-" + tioClientConfig.getId());
		cluClientTaskThread.setDaemon(true);
		cluClientTaskThread.setPriority(Thread.MIN_PRIORITY);
		cluClientTaskThread.start();
	}

	/**
	 * 测试选择服务器
	 */
	public void testBestBsNode() {
		java.util.concurrent.atomic.AtomicLong count = new AtomicLong();
		@SuppressWarnings("unused")
		java.util.concurrent.atomic.AtomicInteger useridseq = new AtomicInteger();

		long starttime = System.currentTimeMillis();

		for (int i = 0; i < 100; i++) {

			Thread cluClientTaskThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(3000L);
					} catch (InterruptedException e1) {
						log.error(e1.toString(), e1);
					}

					while (true) {
						// try {
						// Thread.sleep(1L);
						// } catch (InterruptedException e1) {
						// log.error(e1.toString(), e1);
						// }

						try {
							BestNodeReq bestNodeReq = new BestNodeReq();
							// bestNodeReq.setUid(useridseq.incrementAndGet());
							bestNodeReq.setUid(666);
							bestNodeReq.setProtocol("ws");

							CluPacket respPacket = Clu.synSend(Cc.next(tioClientConfig), Command.BestNodeReq, bestNodeReq);
							if (respPacket == null) {
								log.error("获取最优服务器，响应超时");
								// return null;
							}

							@SuppressWarnings("unused")
							BestNodeResp bestNodeResp = Clu.getBodyObj(respPacket, BestNodeResp.class);
							long c = count.incrementAndGet();
							long modbase = 100000L;

							if (c % modbase == 0) {
								long endtime = System.currentTimeMillis();
								long iv = endtime - starttime;
								double perSecond = 1000L * ((double) c / (double) iv);

								long iv2 = endtime - laststart;
								laststart = System.currentTimeMillis();

								double perSecond2 = 1000L * ((double) modbase / (double) iv2);
								System.out.println("选择服务器响应次数：" + c + ", 总每秒响应：" + perSecond + "次, 本阶段每秒响应：" + perSecond2);
							}
						} catch (Exception e) {
							log.error("", e);
						}

					}

				}
			}, "clu-client-task-test-" + tioClientConfig.getId() + "-" + i);
			cluClientTaskThread.setDaemon(true);
			cluClientTaskThread.setPriority(Thread.MIN_PRIORITY);
			cluClientTaskThread.start();
		}
	}


}
