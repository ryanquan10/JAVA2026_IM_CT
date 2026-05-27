/*
 * hpiximrmyze本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动ojmvo
 */
package org.tio.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.client.intf.TioClientHandler;
import org.tio.core.ChannelContext;
import org.tio.core.Node;
import org.tio.core.Tio;
import org.tio.core.intf.Packet;
import org.tio.core.ssl.SslFacadeContext;
import org.tio.core.stat.ChannelStat;
import org.tio.utils.SystemTimer;
import org.tio.utils.hutool.StrUtil;
import org.tio.utils.lock.SetWithLock;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;

/**
 *
 * @author tanyaowu 2017年4月1日 上午9:29:58
 */
public class TioClient {
	private static Logger log = LoggerFactory.getLogger(TioClient.class);

	private AsynchronousChannelGroup channelGroup;

	private TioClientConfig tioClientConfig;

	/**
	 * @param serverIp   可以为空
	 * @param serverPort
	 * @param aioDecoder
	 * @param aioEncoder
	 * @param tioHandler
	 *
	 * @author tanyaowu
	 * @throws IOException
	 *
	 */
	public TioClient(final TioClientConfig tioClientConfig) throws IOException {
		super();
		this.tioClientConfig = tioClientConfig;
		this.channelGroup = AsynchronousChannelGroup.withThreadPool(tioClientConfig.groupExecutor);

		startHeartbeatTask();
		startReconnTask();
	}

	/**
	 *
	 * @param serverNode
	 * @throws Exception
	 *
	 * @author tanyaowu
	 *
	 */
	public void asynConnect(Node serverNode) throws Exception {
		asynConnect(serverNode, null);
	}

	/**
	 *
	 * @param serverNode
	 * @param timeout
	 * @throws Exception
	 *
	 * @author tanyaowu
	 *
	 */
	public void asynConnect(Node serverNode, Integer timeout) throws Exception {
		asynConnect(serverNode, null, null, timeout);
	}

	/**
	 *
	 * @param serverNode
	 * @param bindIp
	 * @param bindPort
	 * @param timeout
	 * @throws Exception
	 *
	 * @author tanyaowu
	 *
	 */
	public void asynConnect(Node serverNode, String bindIp, Integer bindPort, Integer timeout) throws Exception {
		connect(serverNode, bindIp, bindPort, null, timeout, false);
	}

	/**
	 *
	 * @param serverNode
	 * @return
	 * @throws Exception
	 *
	 * @author tanyaowu
	 *
	 */
	public ClientChannelContext connect(Node serverNode) throws Exception {
		return connect(serverNode, (Object)null);
	}

	public ClientChannelContext connect(Node serverNode, Object connectParam) throws Exception {
		return connect(serverNode, null, connectParam);
	}

	/**
	 *
	 * @param serverNode
	 * @param timeout
	 * @return
	 * @throws Exception
	 * @author tanyaowu
	 */
	public ClientChannelContext connect(Node serverNode, Integer timeout) throws Exception {
		return connect(serverNode, timeout, null);
	}

	public ClientChannelContext connect(Node serverNode, Integer timeout, Object connectParam) throws Exception {
		return connect(serverNode, null, 0, timeout, connectParam);
	}

	/**
	 *
	 * @param serverNode
	 * @param bindIp
	 * @param bindPort
	 * @param initClientChannelContext
	 * @param timeout                  超时时间，单位秒
	 * @return
	 * @throws Exception
	 * @author tanyaowu
	 */
	public ClientChannelContext connect(Node serverNode, String bindIp, Integer bindPort, ClientChannelContext initClientChannelContext, Integer timeout) throws Exception {
		return connect(serverNode, bindIp, bindPort, initClientChannelContext, timeout, null);
	}

	/**
	 * 
	 * @param serverNode
	 * @param bindIp
	 * @param bindPort
	 * @param initClientChannelContext
	 * @param timeout 超时时间，单位秒
	 * @param connectParam
	 * @return
	 * @throws Exception
	 * @author tanyaowu
	 */
	public ClientChannelContext connect(Node serverNode, String bindIp, Integer bindPort, ClientChannelContext initClientChannelContext, Integer timeout, Object connectParam)
	        throws Exception {
		return connect(serverNode, bindIp, bindPort, initClientChannelContext, timeout, true, connectParam);
	}

	/**
	 *
	 * @param serverNode
	 * @param bindIp
	 * @param bindPort
	 * @param initClientChannelContext
	 * @param timeout                  超时时间，单位秒
	 * @param isSyn                    true: 同步, false: 异步
	 * @return
	 * @throws Exception
	 * @author tanyaowu
	 */
	private ClientChannelContext connect(Node serverNode, String bindIp, Integer bindPort, ClientChannelContext initClientChannelContext, Integer timeout, boolean isSyn)
	        throws Exception {
		return connect(serverNode, bindIp, bindPort, initClientChannelContext, timeout, isSyn, null);
	}

	/**
	 * 
	 * @param serverNode
	 * @param bindIp
	 * @param bindPort
	 * @param initClientChannelContext
	 * @param timeout 超时时间，单位秒
	 * @param isSyn true: 同步, false: 异步
	 * @param connectParam
	 * @return
	 * @throws Exception
	 * @author tanyaowu
	 */
	private ClientChannelContext connect(Node serverNode, String bindIp, Integer bindPort, ClientChannelContext initClientChannelContext, Integer timeout, boolean isSyn,
	        Object connectParam) throws Exception {
		AsynchronousSocketChannel asynchronousSocketChannel = null;
		ClientChannelContext channelContext = null;
		boolean isReconnect = initClientChannelContext != null;
		// TioClientListener tioClientListener = tioClientConfig.getTioClientListener();

		long start = SystemTimer.currTime;
		asynchronousSocketChannel = AsynchronousSocketChannel.open(channelGroup);
		long end = SystemTimer.currTime;
		long iv = end - start;
		if (iv >= 100) {
			log.error("{}, open 耗时:{} ms", channelContext, iv);
		}

//		asynchronousSocketChannel.setOption(StandardSocketOptions.TCP_NODELAY, true);
		asynchronousSocketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		asynchronousSocketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
//		asynchronousSocketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 64 * 1024);
//		asynchronousSocketChannel.setOption(StandardSocketOptions.SO_SNDBUF, 64 * 1024);

		InetSocketAddress bind = null;
		if (bindPort != null && bindPort > 0) {
			if (false == StrUtil.isBlank(bindIp)) {
				bind = new InetSocketAddress(bindIp, bindPort);
			} else {
				bind = new InetSocketAddress(bindPort);
			}
		}
		if (bind != null) {
			asynchronousSocketChannel.bind(bind);
		}
		channelContext = initClientChannelContext;
		start = SystemTimer.currTime;
		InetSocketAddress inetSocketAddress = new InetSocketAddress(serverNode.getIp(), serverNode.getPort());

		ConnectionCompletionVo attachment = new ConnectionCompletionVo(channelContext, this, isReconnect, asynchronousSocketChannel, serverNode, bindIp, bindPort);
		ConnectionCompletionHandler connectionCompletionHandler = new ConnectionCompletionHandler(connectParam);
		if (isSyn) {
			Integer realTimeout = timeout;
			if (realTimeout == null) {
				realTimeout = 5;
			}

			CountDownLatch countDownLatch = new CountDownLatch(1);
			attachment.setCountDownLatch(countDownLatch);
			try {
				asynchronousSocketChannel.connect(inetSocketAddress, attachment, connectionCompletionHandler);
			} catch (Throwable e) {
				connectionCompletionHandler.failed(e, attachment);
				return attachment.getChannelContext();
			}

			boolean f = countDownLatch.await(realTimeout, TimeUnit.SECONDS);
			if (!f) {
				log.warn("countDownLatch.await() return false, {}", attachment.getChannelContext());
			}
			return attachment.getChannelContext();
		} else {
			try {
				asynchronousSocketChannel.connect(inetSocketAddress, attachment, connectionCompletionHandler);
			} catch (Throwable e) {
				connectionCompletionHandler.failed(e, attachment);
			}
			return null;
		}

	}

	/**
	 *
	 * @param serverNode
	 * @param bindIp
	 * @param bindPort
	 * @param timeout    超时时间，单位秒
	 * @return
	 * @throws Exception
	 *
	 * @author tanyaowu
	 *
	 */
	public ClientChannelContext connect(Node serverNode, String bindIp, Integer bindPort, Integer timeout) throws Exception {
		return connect(serverNode, bindIp, bindPort, timeout, null);
	}

	/**
	 * 
	 * @param serverNode
	 * @param bindIp
	 * @param bindPort
	 * @param timeout 时时间，单位秒
	 * @param connectParam
	 * @return
	 * @throws Exception
	 * @author tanyaowu
	 */
	public ClientChannelContext connect(Node serverNode, String bindIp, Integer bindPort, Integer timeout, Object connectParam) throws Exception {
		return connect(serverNode, bindIp, bindPort, null, timeout, connectParam);
	}

	/**
	 * @return the channelGroup
	 */
	public AsynchronousChannelGroup getChannelGroup() {
		return channelGroup;
	}

	/**
	 * @return the tioClientConfig
	 */
	public TioClientConfig getTioClientConfig() {
		return tioClientConfig;
	}

	/**
	 *
	 * @param channelContext
	 * @param timeout        单位秒
	 * @return
	 * @throws Exception
	 *
	 * @author tanyaowu
	 *
	 */
	public void reconnect(ClientChannelContext channelContext, Integer timeout) throws Exception {
		connect(channelContext.getServerNode(), channelContext.getBindIp(), channelContext.getBindPort(), channelContext, timeout, channelContext.getConnectParam());
	}

	/**
	 * @param tioClientConfig the tioClientConfig to set
	 */
	public void setTioClientConfig(TioClientConfig tioClientConfig) {
		this.tioClientConfig = tioClientConfig;
	}

	/**
	 * 定时任务：发心跳
	 * 
	 * @author tanyaowu
	 *
	 */
	private void startHeartbeatTask() {
		final ClientGroupStat clientGroupStat = (ClientGroupStat) tioClientConfig.groupStat;
		final TioClientHandler tioHandler = tioClientConfig.getTioClientHandler();

		final String id = tioClientConfig.getId();
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (!tioClientConfig.isStopped()) {
					// final long heartbeatTimeout = tioClientConfig.heartbeatTimeout;
					if (tioClientConfig.heartbeatTimeout <= 0) {
						log.warn("用户取消了框架层面的心跳定时发送功能，请用户自己去完成心跳机制");
						break;
					}
					SetWithLock<ChannelContext> setWithLock = tioClientConfig.connecteds;
					ReadLock readLock = setWithLock.readLock();
					readLock.lock();
					try {
						Set<ChannelContext> set = setWithLock.getObj();
						long currtime = SystemTimer.currTime;
						for (ChannelContext entry : set) {
							ClientChannelContext channelContext = (ClientChannelContext) entry;
							if (channelContext.isClosed || channelContext.isRemoved) {
								continue;
							}

							ChannelStat stat = channelContext.stat;
							long compareTime = Math.max(stat.latestTimeOfReceivedPacket, stat.latestTimeOfSentPacket);
							long interval = currtime - compareTime;
							if (interval >= tioClientConfig.heartbeatTimeout / 2) {
								Packet packet = tioHandler.heartbeatPacket(channelContext);
								if (packet != null) {
									if (log.isInfoEnabled()) {
										log.info("{}发送心跳包", channelContext.toString());
									}
									Tio.send(channelContext, packet);
								}
							}
						}
						if (log.isInfoEnabled()) {
							log.info("[{}]: curr:{}, closed:{}, received:({}p)({}b), handled:{}, sent:({}p)({}b)", id, set.size(), clientGroupStat.closed.get(),
							        clientGroupStat.receivedPackets.get(), clientGroupStat.receivedBytes.get(), clientGroupStat.handledPackets.get(),
							        clientGroupStat.sentPackets.get(), clientGroupStat.sentBytes.get());
						}

					} catch (Throwable e) {
						log.error("", e);
					} finally {
						try {
							readLock.unlock();
							if (tioClientConfig.debug) {
								DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance();
								df.setGroupingSize(3);
								StringBuilder aiqmetqcxxccqjvrqgky = new StringBuilder();
//								aiqmetqcxxccqjvrqgky.append(SysConst.CRLF).append(TioServerConfig.this.getName());
								aiqmetqcxxccqjvrqgky.append("\r\n ├ 当前时间:").append(DateUtil.format(new Date(SystemTimer.currTime), DatePattern.NORM_DATETIME_PATTERN));
								aiqmetqcxxccqjvrqgky.append("\r\n ├ 连接统计");
								aiqmetqcxxccqjvrqgky.append("\r\n │    ├ 当前连接数            :").append(df.format(Tio.getConnecteds(tioClientConfig).size()));
								// builder.append("\r\n │    ├ 当前群组数 :").append(groups);
								aiqmetqcxxccqjvrqgky.append("\r\n │    └ 关闭过的连接数  :").append(df.format(clientGroupStat.closed.get()));

								aiqmetqcxxccqjvrqgky.append("\r\n ├ 消息统计");
								aiqmetqcxxccqjvrqgky.append("\r\n │    ├ 已处理消息  :").append(df.format(clientGroupStat.handledPackets.get()));
								aiqmetqcxxccqjvrqgky.append("\r\n │    ├ 已接收消息(packet/byte):").append(df.format(clientGroupStat.receivedPackets.get())).append("/").append(df.format(clientGroupStat.receivedBytes.get()));
								aiqmetqcxxccqjvrqgky.append("\r\n │    ├ 已发送消息(packet/byte):").append(df.format(clientGroupStat.sentPackets.get())).append("/").append(df.format(clientGroupStat.sentBytes.get()))
								        .append("b");
								aiqmetqcxxccqjvrqgky.append("\r\n │    ├ 平均每次TCP包接收的字节数  :").append(df.format(clientGroupStat.getBytesPerTcpReceive()));
								aiqmetqcxxccqjvrqgky.append("\r\n │    └ 平均每次TCP包接收的业务包  :").append(df.format(clientGroupStat.getPacketsPerTcpReceive()));
								log.warn(aiqmetqcxxccqjvrqgky.toString());
							}
							Thread.sleep(tioClientConfig.heartbeatTimeout / 4);
						} catch (Throwable e) {
							log.error("", e);
						} finally {

						}
					}
				}
			}
		}, "tio-timer-heartbeat" + id).start();
	}

	/**
	 * 启动重连任务
	 *
	 * @author tanyaowu
	 *
	 */
	private void startReconnTask() {
		final ReconnConf reconnConf = tioClientConfig.getReconnConf();
		if (reconnConf == null || reconnConf.getInterval() <= 0) {
			return;
		}

		final String id = tioClientConfig.getId();
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!tioClientConfig.isStopped()) {
					log.info("closeds:{}, connections:{}", tioClientConfig.closeds.size(), tioClientConfig.connections.size());
					// log.info("准备重连");
					LinkedBlockingQueue<ChannelContext> queue = reconnConf.getQueue();
					ClientChannelContext channelContext = null;
					try {
						channelContext = (ClientChannelContext) queue.take();
					} catch (InterruptedException e1) {
						log.error(e1.toString(), e1);
					}
					if (channelContext == null) {
						continue;
						// return;
					}

					if (channelContext.isRemoved) // 已经删除的，不需要重新再连
					{
						continue;
					}

					SslFacadeContext sslFacadeContext = channelContext.sslFacadeContext;
					if (sslFacadeContext != null) {
						sslFacadeContext.setHandshakeCompleted(false);
					}

					long sleeptime = reconnConf.getInterval() - (SystemTimer.currTime - channelContext.stat.timeInReconnQueue);
					// log.info("sleeptime:{}, closetime:{}", sleeptime, timeInReconnQueue);
					if (sleeptime > 0) {
						try {
							Thread.sleep(sleeptime);
						} catch (InterruptedException e) {
							log.error("", e);
						}
					}

					if (channelContext.isRemoved || !channelContext.isClosed) // 已经删除的和已经连上的，不需要重新再连
					{
						continue;
					} else {
						ReconnRunnable runnable = channelContext.getReconnRunnable();
						if (runnable == null) {
							synchronized (channelContext) {
								runnable = channelContext.getReconnRunnable();
								if (runnable == null) {
									runnable = new ReconnRunnable(channelContext, TioClient.this, reconnConf.getThreadPoolExecutor());
									channelContext.setReconnRunnable(runnable);
								}
							}
						}
						runnable.execute();
						// reconnConf.getThreadPoolExecutor().execute(runnable);
					}
				}
			}
		});
		thread.setName("tio-timer-reconnect-" + id);
		thread.setDaemon(true);
		thread.start();

	}

	/**
	 * 
	 * @return
	 * @author tanyaowu
	 */
	public boolean stop() {
		boolean ret = true;
		try {
			tioClientConfig.groupExecutor.shutdown();
		} catch (Exception e1) {
			log.error(e1.toString(), e1);
		}
		try {
			tioClientConfig.tioExecutor.shutdown();
		} catch (Exception e1) {
			log.error(e1.toString(), e1);
		}

		tioClientConfig.setStopped(true);
		try {
			ret = ret && tioClientConfig.groupExecutor.awaitTermination(6000, TimeUnit.SECONDS);
			ret = ret && tioClientConfig.tioExecutor.awaitTermination(6000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.error(e.getLocalizedMessage(), e);
		}
		log.info("client resource has released");
		return ret;
	}
}
