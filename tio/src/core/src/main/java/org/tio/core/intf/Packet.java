/*
 * gfrvrqatrxfu本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动uvirb
 */
package org.tio.core.intf;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tanyaowu 2017年4月1日 上午9:34:59
 */
public class Packet implements java.io.Serializable, Cloneable {
	private static Logger		log					= LoggerFactory.getLogger(Packet.class);
	private static final long	serialVersionUID	= 5275372187150637318L;

	/**
	 * 本packet在解码时，消耗的字节数
	 */
	private Integer			byteCount				= null;
	private PacketListener	packetListener			= null;
	private PacketMeta		meta					= null;
	/**
	 * 消息是否是另外一台机器通过topic转过来的，如果是就不要死循环地再一次转发啦 这个属性是tio内部使用，业务层的用户请勿使用
	 */
	private Boolean			isFromClu				= null;
	/**
	 * 同步发送时，需要的同步请求序列号
	 */
	private Integer			synReqNo				= null;
	/**
	 * 响应同步消息时，需要的同步响应序列号
	 */
	private Integer			synRespNo				= null;
	/**
	 * 预编码过的bytebuffer，如果此值不为null，框架则会忽略原来的encode()而直接用此值
	 */
	private ByteBuffer		preEncodedByteBuffer	= null;
	/**
	 * 是否已经进行ssl加密过
	 */
	private Boolean			isSslEncrypted			= null;

	@Override
	public Packet clone() {
		try {
			Packet ret = (Packet) super.clone();
			ret.setPreEncodedByteBuffer(null);
			ret.setSslEncrypted(null);
			ret.setPacketListener(null);
			ret.setMeta(null);
			return ret;
		} catch (CloneNotSupportedException e) {
			log.error("", e);
			return null;
		}
	}

	/**
	 * @return the byteCount
	 */
	public Integer getByteCount() {
		return byteCount;
	}

	public PacketMeta getMeta() {
		return meta;
	}

	/**
	 * @return the packetListener
	 */
	public PacketListener getPacketListener() {
		return packetListener;
	}

	/**
	 * @return the preEncodedByteBuffer
	 */
	public ByteBuffer getPreEncodedByteBuffer() {
		return preEncodedByteBuffer;
	}

	/**
	 * @return the synReqNo
	 */
	public Integer getSynReqNo() {
		return synReqNo;
	}

	/**
	 * @return the synRespNo
	 */
	public Integer getSynRespNo() {
		return synRespNo;
	}

	public Boolean isFromClu() {
		return isFromClu;
	}

	public Boolean isSslEncrypted() {
		return isSslEncrypted;
	}

	public String logstr() {
		return "";
	}

	/**
	 * @param byteCount the byteCount to set
	 */
	public void setByteCount(Integer byteCount) {
		this.byteCount = byteCount;
	}

	public void setFromClu(Boolean isFromClu) {
		this.isFromClu = isFromClu;
	}

	public void setMeta(PacketMeta meta) {
		this.meta = meta;
	}

	/**
	 * @param packetListener the packetListener to set
	 */
	public void setPacketListener(PacketListener packetListener) {
		this.packetListener = packetListener;
	}

	/**
	 * @param preEncodedByteBuffer the preEncodedByteBuffer to set
	 */
	public void setPreEncodedByteBuffer(ByteBuffer preEncodedByteBuffer) {
		this.preEncodedByteBuffer = preEncodedByteBuffer;
	}

	public void setSslEncrypted(Boolean isSslEncrypted) {
		this.isSslEncrypted = isSslEncrypted;
	}

	/**
	 * @param synReqNo the synReqNo to set
	 */
	public void setSynReqNo(Integer synReqNo) {
		this.synReqNo = synReqNo;
	}

	/**
	 * @param synRespNo the synRespNo to set
	 */
	public void setSynRespNo(Integer synRespNo) {
		this.synRespNo = synRespNo;
	}

//	public boolean hasSeqNo() {
//		return ((synReqNo != null && synReqNo > 0) || (synRespNo != null && synRespNo > 0));
//	}

}
