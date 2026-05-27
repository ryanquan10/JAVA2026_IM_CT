/*
 * frnejrfce本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动rykigrwex
 */
/*
 * frnejrfce本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动rykigrwex
 * grantinfo
 */
package org.tio.clu.client;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.client.ClientChannelContext;
import org.tio.clu.client.handler.base.CluClientHandler;
import org.tio.clu.common.CluPacket;
import org.tio.clu.common.Command;
import org.tio.clu.common.CommandHandler;
import org.tio.core.intf.Packet;
import org.tio.server.TioServerConfig;
import org.tio.utils.hutool.ClassScanAnnotationHandler;
import org.tio.utils.hutool.ClassUtil;
import org.tio.utils.json.Json;

/**
 * @author tanyaowu 2016年9月7日 下午5:39:49
 */
public class PacketDispatcher {
    private static Logger log = LoggerFactory.getLogger(PacketDispatcher.class);
    /**
     * 路径和对象映射 key: Command value: object
     */
    private static Map<Command, CluClientHandler> handlerMap = new HashMap<>();
    private TioServerConfig bsTioServerConfig = null;
    private Class<? extends Packet> bsPacketClass = null;
    /**
     * 路径和class映射 只是用来打印的 key: Command value: Class
     */
    public Map<Command, Class<?>> pathClassMap = new TreeMap<>();

    /**
     * @author: tanyaowu
     */
    public PacketDispatcher(String[] scanPackages, TioServerConfig bsTioServerConfig,
	    Class<? extends Packet> bsPacketClass) {
	this.bsTioServerConfig = bsTioServerConfig;
	this.bsPacketClass = bsPacketClass;
	if (scanPackages != null) {
	    for (String pkg : scanPackages) {
		try {
		    ClassUtil.scanPackage(pkg, new ClassScanAnnotationHandler(CommandHandler.class) {
			@Override
			public void handlerAnnotation(Class<?> clazz) {
			    try {
				Object bean = clazz.newInstance();

				if (!(bean instanceof CluClientHandler)) {
				    log.error("{}没有实现 {}", clazz.getName(), CluClientHandler.class.getName());
				    return;
				}

				CluClientHandler cluClientHandler = (CluClientHandler) bean;

				CommandHandler mapping = clazz.getAnnotation(CommandHandler.class);
				Command command = mapping.value();

				Object obj = handlerMap.get(command);
				if (obj != null) {
				    log.error("mapping[{}] already exists in class [{}]", command,
					    obj.getClass().getName());
				} else {
				    handlerMap.put(command, cluClientHandler);
				    pathClassMap.put(command, clazz);
				}

			    } catch (Exception e) {
				log.error("", e);
			    }
			}
		    });
		} catch (Exception e) {
		    log.error("", e);
		}
	    }
	    log.warn("command mapping\r\n{}", Json.toFormatedJson(pathClassMap));
	}
    }

    /**
     * 
     * @param cluPacket
     * @param clientChannelContext
     * @throws Exception
     * @author tanyaowu
     */
    public void dispatch(CluPacket cluPacket, ClientChannelContext clientChannelContext) throws Exception {
	Command command = cluPacket.getCommand();
	CluClientHandler handler = handlerMap.get(command);
	if (handler != null) {
	    log.info("{} 收到消息:{}", clientChannelContext, command);
	    handler.handler(cluPacket, clientChannelContext, bsTioServerConfig, bsPacketClass);
	    // CommandStat.getCommandStat(command).handled.incrementAndGet();
	    return;
	} else {
	    log.warn("命令码[{}]没有对应的处理类", command);
	    // CommandStat.getCommandStat(command).handled.incrementAndGet();
	    return;
	}
    }
}
