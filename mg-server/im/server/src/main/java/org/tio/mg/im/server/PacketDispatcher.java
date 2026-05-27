
package org.tio.mg.im.server;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.Node;
import org.tio.core.Tio;
import org.tio.core.stat.ChannelStat;
import org.tio.http.common.HttpConst;
import org.tio.http.common.HttpRequest;
import org.tio.mg.im.common.Command;
import org.tio.mg.im.common.CommandHandler;
import org.tio.mg.im.common.CommandStat;
import org.tio.mg.im.common.ImPacket;
import org.tio.mg.im.common.ImSessionContext;
import org.tio.mg.im.common.utils.ImUtils;
import org.tio.mg.im.server.handler.ImServerHandler;
import org.tio.mg.service.model.main.User;
import org.tio.mg.service.model.stat.TioIpPullblackLog;
import org.tio.mg.service.service.base.IpInfoService;
import org.tio.mg.service.service.base.TioIpPullblackLogService;
import org.tio.monitor.RateLimiterWrap;
import org.tio.sitexxx.service.vo.Const;
import org.tio.utils.hutool.ClassScanAnnotationHandler;
import org.tio.utils.hutool.ClassUtil;
import org.tio.utils.jfinal.P;
import org.tio.utils.json.Json;

/**
 * @author tanyaowu 
 * 2016年9月7日 下午5:39:49
 */
public class PacketDispatcher {
	private static Logger log = LoggerFactory.getLogger(PacketDispatcher.class);

	/**
	 * 路径和对象映射
	 * key: Command
	 * value: object
	 */
	public Map<Command, ImServerHandler>	handlerMap		= new TreeMap<>();
	
	/**
	 * 路径和class映射
	 * 只是用来打印的
	 * key: Command
	 * value: Class
	 */
	public Map<Command, Class<?>>			pathClassMap	= new TreeMap<>();
	
	/**
	 * 分发消息到处理者
	 * @param imPacket
	 * @param channelContext
	 * @param isWebsocket
	 * @throws Exception
	 * @author: tanyaowu
	 */
	public void dispatch(ImPacket imPacket, ChannelContext channelContext, boolean isWebsocket) throws Exception {
		Command command = imPacket.getCommand();
		ImSessionContext imSessionContext = ImUtils.getImSessionContext(channelContext);

		log.debug("{}, 收到命令:{}", channelContext, command);

		//检查握手情况
		if (!imSessionContext.isHandshaked()) {
			if (command != Command.HandshakeReq && command != Command.WxHandshakeReq) {
				log.warn("{} 第一个业务包必须为握手包，本次命令:{}", channelContext.toString(), command);
				Tio.remove(channelContext, "第一个业务包必须为握手包");
				return;
			}
		}

		//检查消息发送频率
		ChannelStat channelStat = channelContext.stat;
		if (channelStat.receivedPackets.get() > P.getInt("skip.warn.count")) { //前面几条命令不计入令牌桶
			RateLimiterWrap rateLimiterWrap = null;

			rateLimiterWrap = imSessionContext.getRequestRateLimiter();

			boolean[] limitTest = rateLimiterWrap.tryAcquire();
			if (limitTest[0] == false && limitTest[1] == false) {
				String remark = channelContext.toString() + " 访问太频繁， 将拉黑其IP，本次命令:" + command;
				log.warn(remark);

				Node clientNode = channelContext.getClientNode();
				//				TioIpPullblackLogService.ME.addToBlack(clientNode.getIp(), client
				//				Node.getPort(), into);

				HttpRequest request = ImUtils.getHandshakeRequest(channelContext);

				User user = ImUtils.getUser(channelContext);
				Integer currId = user == null ? null : user.getId();

				String ip = clientNode.getIp();

				TioIpPullblackLog tioIpPullblackLog = new TioIpPullblackLog();
				tioIpPullblackLog.setIp(ip);
				tioIpPullblackLog.setIpid(IpInfoService.ME.save(ip).getId());
				tioIpPullblackLog.setRemark(remark);
				tioIpPullblackLog.setServer(Const.MY_IP);
				tioIpPullblackLog.setServerport(request.getChannelContext().getServerNode().getPort());
				tioIpPullblackLog.setTime(new Date());
				tioIpPullblackLog.setType(TioIpPullblackLog.Type.IM_REQUEST_TOO_FREQUENTLY);

				tioIpPullblackLog.setSessionid(ImUtils.getToken(channelContext));
				tioIpPullblackLog.setCookie(request.getHeader(HttpConst.RequestHeaderKey.Cookie));
				tioIpPullblackLog.setInitpath(request.requestLine.getInitPath());
				tioIpPullblackLog.setPath(command.name());
				tioIpPullblackLog.setRequestline(request.requestLine.toString());
				tioIpPullblackLog.setUid(currId);

				TioIpPullblackLogService.ME.addToBlack(tioIpPullblackLog);

				return;
			} else if (limitTest[0] == false && limitTest[1] == true) {
				log.warn("{} 访问太频繁，将警告一次，本次命令:{}", channelContext.toString(), command);
				return;
			}
		}

		ImServerHandler handler = handlerMap.get(command);
		if (handler != null) {
			log.debug("{} 收到消息:{}, isWebsocket:{}", channelContext, command, isWebsocket);
			handler.handler(imPacket, channelContext, isWebsocket);
			CommandStat.getCommandStat(command).handled.incrementAndGet();
			return;
		} else {
			log.warn("命令码[{}]没有对应的处理类", command);
			CommandStat.getCommandStat(command).handled.incrementAndGet();
			return;
		}
	}

	/**
	 * 
	 * @author: tanyaowu
	 */
	public PacketDispatcher(String[] scanPackages) {
		if (scanPackages != null) {
			for (String pkg : scanPackages) {
				try {
					ClassUtil.scanPackage(pkg, new ClassScanAnnotationHandler(CommandHandler.class) {
						@Override
						public void handlerAnnotation(Class<?> clazz) {
							try {
								Object bean = clazz.newInstance();

								if (!(bean instanceof ImServerHandler)) {
									log.error("{}没有实现 {}", clazz.getName(), ImServerHandler.class.getName());
									return;
								}

								ImServerHandler imServerHandler = (ImServerHandler) bean;

								CommandHandler mapping = clazz.getAnnotation(CommandHandler.class);
								Command command = mapping.value();

								Object obj = handlerMap.get(command);
								if (obj != null) {
									log.error("mapping[{}] already exists in class [{}]", command, obj.getClass().getName());
								} else {
									handlerMap.put(command, imServerHandler);
									pathClassMap.put(command, clazz);
								}

							} catch (Exception e) {
								log.error(e.toString(), e);
							}

						}
					});
				} catch (Exception e) {
					log.error(e.toString(), e);
				}
			}
			log.warn("command mapping\r\n{}", Json.toFormatedJson(pathClassMap));
		}
	}
	


	/**
	 * @param args
	 * @author: tanyaowu
	 */
	public static void main(String[] args) {

	}
}
