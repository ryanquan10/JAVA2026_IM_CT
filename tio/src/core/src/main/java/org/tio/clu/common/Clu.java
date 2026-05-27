/*
 * bmitqmjfxqkbvx本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动gmdqvn
 */
package org.tio.clu.common;

import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.clu.common.bs.base.Base;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.core.intf.Packet;
import org.tio.utils.lock.ReadLockHandler;
import org.tio.utils.lock.SetWithLock;

/**
 * @author tanyaowu 2020年8月20日 下午3:57:02
 */
public class Clu {
    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(Clu.class);

    @SuppressWarnings("unchecked")
    public static <T> T getBodyObj(CluPacket cluPacket, Class<T> clazz) {
	T t = (T) cluPacket.getBody();
	return t;
    }

    /**
     * 
     * @param channelContext
     * @return
     * @author tanyaowu
     */
    public static CluSessionContext getCluSessionContext(ChannelContext channelContext) {
	CluSessionContext ret = (CluSessionContext) channelContext.getAttribute(CluConst.TIO_CLU_SESSION_KEY);
	return ret;
    }

    /**
     * 
     * @param channelContext
     * @param command
     * @param bodyObj
     * @author tanyaowu
     */
    public static void send(ChannelContext channelContext, Command command, Base bodyObj) {
	if (channelContext == null) {
	    return;
	}
	CluPacket cluPacket = CluPacket.from(command, bodyObj);
	Tio.send(channelContext, cluPacket);
    }

    /**
     * 
     * @param channelContext
     * @param setWithLock
     * @param bodyObj
     * @param command
     * @param skipSelf
     * @author tanyaowu
     */
    public static void sendToSet(ChannelContext channelContext, SetWithLock<ChannelContext> setWithLock, Base bodyObj,
	    Command command, boolean skipSelf) {
	if (setWithLock != null) {
	    setWithLock.handle(new ReadLockHandler<Set<ChannelContext>>() {
		@Override
		public void handler(Set<ChannelContext> set) {
		    for (ChannelContext channelContext1 : set) {
			if (skipSelf && Objects.equals(channelContext1, channelContext)) {
			    continue;
			}
			Clu.send(channelContext1, command, bodyObj);
		    }
		}
	    });
	}
    }

    /**
     * 
     * @param channelContext
     * @param command
     * @param bodyObj
     * @param reqPacket
     * @author tanyaowu
     */
    public static void synResp(ChannelContext channelContext, Command command, Base bodyObj, Packet reqPacket) {
	CluPacket cluPacket = CluPacket.from(command, bodyObj);
	cluPacket.setSynRespNo(reqPacket.getSynReqNo());
	Tio.send(channelContext, cluPacket);
    }

    /**
     * 
     * @param channelContext
     * @param command
     * @param bodyObj
     * @return
     * @author tanyaowu
     */
    public static CluPacket synSend(ChannelContext channelContext, Command command, Base bodyObj) {
	return synSend(channelContext, command, bodyObj, CluConst.DFT_SYNSEND_TIMEOUT);
    }

    /**
     * 
     * @param channelContext
     * @param command
     * @param bodyObj
     * @param timeout
     * @return
     * @author tanyaowu
     */
    public static CluPacket synSend(ChannelContext channelContext, Command command, Base bodyObj, long timeout) {
	if (channelContext == null) {
	    log.error("channelContext is null");
	    return null;
	}
	CluPacket cluPacket = CluPacket.from(command, bodyObj);
	return Tio.synSend(channelContext, cluPacket, timeout);
    }

    /**
     * init(useSsl);
     * 
     * @author tanyaowu
     */
    public Clu() {
    }

}
