/*
 * tvgjbpvheatzkt本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动ulomocafq
 */
package org.tio.clu.common.utils;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.clu.common.CluConst;
import org.tio.clu.common.CluPacket;
import org.tio.clu.common.Command;
import org.tio.clu.common.bs.base.Base;
import org.tio.core.ChannelContext;
import org.tio.core.TioConfig;
import org.tio.core.exception.TioDecodeException;
import org.tio.core.intf.Packet;
import org.tio.utils.SystemTimer;

import cn.hutool.core.util.ZipUtil;

/**
 * @author tanyaowu 2020年8月25日 上午10:28:16
 */
public class ProtocolUtils {
    private static Logger log = LoggerFactory.getLogger(ProtocolUtils.class);


    public final static byte VERSION = 1;
    public static final int MAX_LENGTH_OF_BODY = 1024 * 1024 * 20; // 只支持多少个字节的数据
    public static final int LEAST_HEADER_LENGTH = 5;
    public static final byte MASK_COMPRESS = 0B01000000;
    public static final byte MASK_SYN_REQ = 32;
    public static final byte MASK_SYN_RESP = 16;
    public static final byte MASK_4_BYTE_LENGTH = 8;
    public static final byte MASK_VERSION = 7;

    /**
     * @param synReq
     * @param synResp
     * @param is4ByteLength
     * @return
     * @author tanyaowu
     */
    public static int calcHeaderLen(boolean synReq, boolean synResp, boolean is4ByteLength) {
	return ProtocolUtils.LEAST_HEADER_LENGTH + (synReq ? 4 : 0) + (synResp ? 4 : 0) + (is4ByteLength ? 2 : 0);
    }

    /**
     * @param buffer
     * @param limit
     * @param position
     * @param readableLength
     * @param channelContext
     * @return
     * @throws TioDecodeException
     * @author tanyaowu
     */
    public static CluPacket decode(ByteBuffer buffer, int limit, int position, int readableLength,
	    ChannelContext channelContext) throws TioDecodeException {
	byte firstbyte = BufferUtil.read(buffer);
	@SuppressWarnings("unused")
	byte version = ProtocolUtils.decodeVersion(firstbyte);
	boolean isCompress = ProtocolUtils.decodeCompress(firstbyte);
	boolean synReq = ProtocolUtils.decodeSynReq(firstbyte);
	boolean synResp = ProtocolUtils.decodeSynResp(firstbyte);
	boolean is4ByteLength = ProtocolUtils.decode4ByteLength(firstbyte);

	int headerLength = calcHeaderLen(synReq, synResp, is4ByteLength);
	if (readableLength < headerLength) {
	    return null;
	}

	CluPacket cluPacket = new CluPacket();

	if (synReq) {
	    int synReqNo = BufferUtil.readInt(buffer);
	    cluPacket.setSynReqNo(synReqNo);
	}

	if (synResp) {
	    int synRespNo = BufferUtil.readInt(buffer);
	    cluPacket.setSynRespNo(synRespNo);
	}

	int bodyLength = is4ByteLength ? BufferUtil.readInt(buffer) : BufferUtil.readShort(buffer);
	if (bodyLength > ProtocolUtils.MAX_LENGTH_OF_BODY || bodyLength < 0) {
	    throw new TioDecodeException(
		    "bodyLength [" + bodyLength + "] is not right, remote:" + channelContext.getClientNode());
	}
	int neededLength = headerLength + bodyLength;
	if (readableLength < neededLength)
	{
	    channelContext.setPacketNeededLength(neededLength);
	    return null;
	}

	short commandValue = BufferUtil.readShort(buffer);
	Command command = Command.from(commandValue);
	if (command == null) {
	    throw new TioDecodeException("消息命令码【" + commandValue + "】不正确");
	}

	// byte gzip = BufferUtil.read(buffer);

	cluPacket.setCommand(command);
	byte[] body = null;
	if (bodyLength > 0) {
	    byte[] dst = new byte[bodyLength];
	    buffer.get(dst);
	    if (isCompress) {
		try {
		    byte[] unGzippedBytes = ZipUtil.unGzip(dst);
		    body = unGzippedBytes;
		} catch (Throwable e) {
		    log.error("{}, 解压失败, bodyLength:{}, buffer:{}", channelContext, bodyLength, buffer);
		    throw new TioDecodeException(e);
		}
	    } else {
		body = dst;
	    }

	    cluPacket.setBody(FstUtils.asObject(body, Base.class));
	}
	return cluPacket;
    }

    public static boolean decode4ByteLength(byte firstByte) {
	return (MASK_4_BYTE_LENGTH & firstByte) != 0;
    }

    public static boolean decodeCompress(byte firstByte) {
	return (MASK_COMPRESS & firstByte) != 0;
    }

    public static boolean decodeSynReq(byte firstByte) {
	return (MASK_SYN_REQ & firstByte) != 0;
    }

    public static boolean decodeSynResp(byte firstByte) {
	return (MASK_SYN_RESP & firstByte) != 0;
    }

    public static byte decodeVersion(byte version) {
	return (byte) (MASK_VERSION & version);
    }

    /**
     * @param packet
     * @param tioConfig
     * @param channelContext
     * @return
     * @author tanyaowu
     */
    public static ByteBuffer encode(Packet packet, TioConfig tioConfig, ChannelContext channelContext) {
	CluPacket cluPacket = (CluPacket) packet;

	byte[] body = FstUtils.asByteArray(cluPacket.getBody());

	int bodyLen = 0;
	boolean isCompress = false;
	if (body != null) {
	    bodyLen = body.length;
	    if (bodyLen > CluConst.SIZE_FOR_COMPRESS) {
		try {
		    long start = SystemTimer.currTime;
		    byte[] gzipedbody = ZipUtil.gzip(body);
		    long end = SystemTimer.currTime;
		    long iv = end - start;
		    if (iv > 1000L) {
			log.warn("gzip压缩耗时{}ms", iv);
		    }
		    if (gzipedbody.length < body.length) {
			log.info("压缩前:{}, 压缩后:{}", body.length, gzipedbody.length);
			body = gzipedbody;
			bodyLen = gzipedbody.length;
			isCompress = true;
		    }
		} catch (Throwable e) {
		    log.error(e.getMessage(), e);
		}
	    }
	}

	boolean synReq = cluPacket.getSynReqNo() != null;
	boolean synResp = cluPacket.getSynRespNo() != null;
	boolean is4ByteLength = false;
	if (bodyLen > Short.MAX_VALUE) {
	    is4ByteLength = true;
	}

	int headerLength = calcHeaderLen(synReq, synResp, is4ByteLength);
	int allLen = headerLength + bodyLen;

	ByteBuffer buffer = ByteBuffer.allocate(allLen);
	buffer.order(tioConfig.getByteOrder());

	byte firstbyte = ProtocolUtils.encodeCompress(ProtocolUtils.VERSION, isCompress);
	firstbyte = ProtocolUtils.encodeSynReq(firstbyte, synReq);
	firstbyte = ProtocolUtils.encodeSynResp(firstbyte, synResp);
	firstbyte = ProtocolUtils.encode4ByteLength(firstbyte, is4ByteLength);

	buffer.put(firstbyte);
	if (synReq) {
	    buffer.putInt(cluPacket.getSynReqNo());
	}
	if (synResp) {
	    buffer.putInt(cluPacket.getSynRespNo());
	}
	if (is4ByteLength) {
	    buffer.putInt(bodyLen);
	} else {
	    buffer.putShort((short) bodyLen);
	}

	buffer.putShort(cluPacket.getCommand().getValue());

	if (body != null) {
	    buffer.put(body);
	}
	return buffer;
    }

    public static byte encode4ByteLength(byte bs, boolean is4ByteLength) {
	if (is4ByteLength) {
	    return (byte) (bs | MASK_4_BYTE_LENGTH);
	} else {
	    return (byte) (bs & (MASK_4_BYTE_LENGTH ^ 0b01111111));
	}
    }

    public static byte encodeCompress(byte bs, boolean isCompress) {
	if (isCompress) {
	    return (byte) (bs | MASK_COMPRESS);
	} else {
	    return (byte) (bs & (MASK_COMPRESS ^ 0b01111111));
	}
    }

    public static byte encodeSynReq(byte bs, boolean synReq) {
	if (synReq) {
	    return (byte) (bs | MASK_SYN_REQ);
	} else {
	    return (byte) (bs & (MASK_SYN_REQ ^ 0b01111111));
	}
    }

    public static byte encodeSynResp(byte bs, boolean synResp) {
	if (synResp) {
	    return (byte) (bs | MASK_SYN_RESP);
	} else {
	    return (byte) (bs & (MASK_SYN_RESP ^ 0b01111111));
	}
    }

    /**
     * 
     * @author tanyaowu
     */
    public ProtocolUtils() {
    }
}
