/*
 * ypimzbouqkxekt本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动cptrryjuh
 */
package org.tio.flash.policy.server;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.core.TioConfig;
import org.tio.core.exception.LengthOverflowException;
import org.tio.core.exception.TioDecodeException;
import org.tio.core.intf.Packet;
import org.tio.core.utils.ByteBufferUtils;
import org.tio.server.intf.TioServerHandler;

/**
 * 
 * @author tanyaowu 2017年10月31日 下午4:27:31
 */
public class FlashPolicyTioServerHandler implements TioServerHandler {
    private static Logger log = LoggerFactory.getLogger(FlashPolicyTioServerHandler.class);

    public static final String REQUEST_STR = "<policy-file-request/>";

    public static byte[] RESPONSE_BYTES;

    static {
	RESPONSE_BYTES = ("<cross-domain-policy><allow-access-from domain=\"*\" to-ports=\"*\" /></cross-domain-policy>\0")
		.getBytes();
    }

    /**
     * <policy-file-request/>
     * 
     * @param buffer
     * @param channelContext
     * @return
     * @throws TioDecodeException
     * @author tanyaowu
     */
    @Override
    public FlashPolicyPacket decode(ByteBuffer buffer, int limit, int position, int readableLength,
	    ChannelContext channelContext) throws TioDecodeException {
	// 收到的数据组不了业务包，则返回null以告诉框架数据不够
	if (readableLength < FlashPolicyPacket.MIN_LENGHT) {
	    return null;
	}

	String line = null;

	try {
	    line = ByteBufferUtils.readString(buffer, Const.CHARSET, '\0', FlashPolicyPacket.MAX_LING_LENGHT);
	} catch (LengthOverflowException e) {
	    throw new TioDecodeException(e);
	}

	if (line == null) {
	    return null;
	} else {
	    log.info("收到消息:{}", line);
	    if (REQUEST_STR.equalsIgnoreCase(line)) {
		return FlashPolicyPacket.REQUEST;
	    } else {
		throw new TioDecodeException("");
	    }
	}
    }

    /**
     * 
     * @param packet
     * @param tioConfig
     * @param channelContext
     * @return
     * @author tanyaowu
     */
    @Override
    public ByteBuffer encode(Packet packet, TioConfig tioConfig, ChannelContext channelContext) {
	ByteBuffer ret = ByteBuffer.wrap(RESPONSE_BYTES);
	// ret.position(ret.limit());
	return ret;
    }

    /**
     * 处理消息
     */
    @Override
    public void handler(Packet packet, ChannelContext channelContext) throws Exception {
	Tio.send(channelContext, FlashPolicyPacket.RESPONSE);
	// Tio.close(channelContext, "消息发送完毕");
    }

}
