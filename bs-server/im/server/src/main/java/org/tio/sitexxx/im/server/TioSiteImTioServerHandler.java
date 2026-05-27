
package org.tio.sitexxx.im.server;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.clu.common.utils.FstUtils;
import org.tio.core.ChannelContext;
import org.tio.core.TioConfig;
import org.tio.core.exception.TioDecodeException;
import org.tio.core.intf.Packet;
import org.tio.core.utils.ByteBufferUtils;
import org.tio.server.intf.TioServerHandler;
import org.tio.sitexxx.im.common.Command;
import org.tio.sitexxx.im.common.ImConst;
import org.tio.sitexxx.im.common.ImPacket;
import org.tio.sitexxx.service.vo.Const;
import org.tio.utils.json.Json;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;

/**
 *
 * @author tanyaowu
 *
 */
public class TioSiteImTioServerHandler implements TioServerHandler {
	private static Logger log = LoggerFactory.getLogger(TioSiteImTioServerHandler.class);

	private PacketDispatcher packetDispatcher;

	/**
	 * 
	 * @param packetDispatcher
	 */
	public TioSiteImTioServerHandler(PacketDispatcher packetDispatcher) {
		this.setPacketDispatcher(packetDispatcher);
	}

	@Override
	public ImPacket decode(ByteBuffer buffer, int limit, int position, int readableLength, ChannelContext channelContext) throws TioDecodeException {
		int headerLength = ImPacket.LEAST_HEADER_LENGTH;
		if (readableLength < headerLength) {
			return null;
		}
		Integer synReqNo = null;
		Integer synRespNo = null;
		int bodyLength = ByteBufferUtils.readShort(buffer);
		if (bodyLength < 0) {
			String _bodyLenStr = String.valueOf(-bodyLength);
			if (ImPacket.hasRealLen(_bodyLenStr)) {
				bodyLength = buffer.getInt();//ByteBufferUtils.readInt(buffer);
				headerLength += 4;
			}
			if (bodyLength < 0) {
				throw new TioDecodeException("bodyLength [" + bodyLength + "] is not right, remote:" + channelContext.getClientNode());
			}
			if (ImPacket.hasReqNo(_bodyLenStr)) {
				synReqNo = ByteBufferUtils.readInt(buffer);
				headerLength += 4;
			}
			if (ImPacket.hasRespNo(_bodyLenStr)) {
				synRespNo = ByteBufferUtils.readInt(buffer);
				headerLength += 4;
			}
		} else {
			if (bodyLength > ImPacket.MAX_LENGTH_OF_BODY) {
				throw new TioDecodeException("bodyLength [" + bodyLength + "] is not right, remote:" + channelContext.getClientNode());
			}
		}

		int neededLength = headerLength + bodyLength;
		if (readableLength < neededLength) // 不够消息体长度(剩下的buffer组不了消息体)
		{
			channelContext.setPacketNeededLength(neededLength);
			return null;
		}

		short commandValue = ByteBufferUtils.readShort(buffer);
		Command command = Command.from(commandValue);
		if (command == null) {
			throw new TioDecodeException("消息命令码【" + commandValue + "】不正确");
		}

		byte gzip = ByteBufferUtils.read(buffer);
		ImPacket imPacket = new ImPacket();
		imPacket.setSynReqNo(synReqNo);
		imPacket.setSynRespNo(synRespNo);
		imPacket.setCommand(command);

		if (bodyLength > 0) { //有消息体
			byte[] dst = new byte[bodyLength];
			buffer.get(dst);
			boolean isGzipped = gzip == (byte) 1;
			if (isGzipped) {
				try {
					byte[] unGzippedBytes = ZipUtil.unGzip(dst);
					imPacket.setBody(unGzippedBytes);
				} catch (Throwable e) {
					log.error("{}, 解压失败, bodyLength:{}, buffer:{}", channelContext, bodyLength, buffer);
					throw new TioDecodeException(e);
				}
			} else {
				imPacket.setBody(dst);
			}

			if (!ImPacket.fst_set.contains(command)) {
				try {
					imPacket.setBodyStr(new String(imPacket.getBody(), "utf-8"));
				} catch (UnsupportedEncodingException e) {
					log.error("", e);
				}
			}
		}
		return imPacket;
	}

	/**
	 * 
	 * @param packet
	 * @param tioConfig
	 * @param channelContext
	 * @return
	 * @author: tanyaowu
	 */
	@Override
	public ByteBuffer encode(Packet packet, TioConfig tioConfig, ChannelContext channelContext) {
		ImPacket imPacket = (ImPacket) packet;

		byte[] body = imPacket.getBody();
		if (body == null) {
			String bodyStr = imPacket.getBodyStr();
			if (StrUtil.isBlank(bodyStr)) {
				Object bodyObj = imPacket.getBodyObj();
				if (bodyObj != null) {
					if (!ImPacket.fst_set.contains(imPacket.getCommand())) {
						bodyStr = Json.toJson(bodyObj);
					} else {
						body = FstUtils.asByteArray(bodyObj);
						log.info("{} command[{}],bodylength[{}]", channelContext, imPacket.getCommand(), body.length);
					}
				}
			}

			if (StrUtil.isNotBlank(bodyStr)) {
				try {
					body = bodyStr.getBytes(Const.CHARSET);
				} catch (UnsupportedEncodingException e) {
					log.error("", e);
				}
			}
		}

		int bodyLen = 0;
		boolean isCompress = false;
		if (body != null) {
			bodyLen = body.length;
			if (bodyLen > ImConst.SIZE_FOR_COMPRESS && !ImPacket.fst_set.contains(imPacket.getCommand())) {
				try {
					byte[] gzipedbody = ZipUtil.gzip(body);
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

		int headerLength = ImPacket.LEAST_HEADER_LENGTH;
		if (imPacket.getSynReqNo() != null) {
			headerLength += 4;
		}
		if (imPacket.getSynRespNo() != null) {
			headerLength += 4;
		}
		if (headerLength > ImPacket.LEAST_HEADER_LENGTH || bodyLen > Short.MAX_VALUE) {
			if (bodyLen > Short.MAX_VALUE) {
				log.warn("长度[{}]大于[{}]", bodyLen, Short.MAX_VALUE);
			}
			headerLength += 4;
		}
		int allLen = headerLength + bodyLen;

		ByteBuffer buffer = ByteBuffer.allocate(allLen);
		buffer.order(tioConfig.getByteOrder());

		if (headerLength > ImPacket.LEAST_HEADER_LENGTH) {
			StringBuilder sb = new StringBuilder(5);
			//			if (bodyLen > Short.MAX_VALUE) {
			sb.append(1); //只要第一个short不是真实长度，后面都需要带一个真实长度
			//			} else {
			//				sb.append(0);
			//			}
			if (imPacket.getSynReqNo() != null) {
				sb.append(1);
			} else {
				sb.append(0);
			}
			if (imPacket.getSynRespNo() != null) {
				sb.append(1);
			} else {
				sb.append(0);
			}
			sb.append(0);
			sb.append(0);
			short specialLeng = (short) (-Short.parseShort(sb.toString()));
			buffer.putShort(specialLeng);
			buffer.putInt(bodyLen);

			if (imPacket.getSynReqNo() != null) {
				buffer.putInt(imPacket.getSynReqNo());
			}
			if (imPacket.getSynRespNo() != null) {
				buffer.putInt(imPacket.getSynRespNo());
			}
		} else {
			buffer.putShort((short) bodyLen);
		}

		buffer.putShort(imPacket.getCommand().getValue());
		if (isCompress) {
			buffer.put((byte) 1);
		} else {
			buffer.put((byte) 0);
		}

		if (body != null) {
			buffer.put(body);
		}
		
		//		if (Command.FtDownloadFileResp == imPacket.getCommand()) {
		//			FtDownloadFileResp ftDownloadFileResp = (FtDownloadFileResp)imPacket.getBodyObj();
		//			log.warn("[{}],[{}], [{}]", channelContext, buffer, ftDownloadFileResp.getSubPath());
		//		}
		return buffer;
	}

	/**
	 * 
	 * @param packet
	 * @param channelContext
	 * @throws Exception
	 * @author: tanyaowu
	 */
	@Override
	public void handler(Packet packet, ChannelContext channelContext) throws Exception {
		ImPacket imPacket = (ImPacket) packet;
		packetDispatcher.dispatch(imPacket, channelContext, false);
	}

	/**
	 * @return the packetDispatcher
	 */
	public PacketDispatcher getPacketDispatcher() {
		return packetDispatcher;
	}

	/**
	 * @param packetDispatcher the packetDispatcher to set
	 */
	public void setPacketDispatcher(PacketDispatcher packetDispatcher) {
		this.packetDispatcher = packetDispatcher;
	}

}
