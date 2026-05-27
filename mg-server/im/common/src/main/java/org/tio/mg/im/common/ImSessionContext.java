
package org.tio.mg.im.common;

import org.lionsoul.ip2region.DataBlock;
import org.tio.monitor.RateLimiterWrap;
import org.tio.mg.im.common.bs.HandshakeReq;
import org.tio.mg.service.model.main.ChatroomJoinLeave;
import org.tio.websocket.common.WsSessionContext;

/**
 *
 * @author tanyaowu
 *
 */
public class ImSessionContext {
	/**
	 * 是Wx版否
	 */
	private boolean				isWx				= false;
	/**
	 * 消息请求频率控制器
	 */
	private RateLimiterWrap		requestRateLimiter	= null;
	/**
	 * 握手时的用户id，注意：就算后来被踢了，此值仍然不会被清空，可用于数据统计，但不能用此值来获取当前用户
	 */
	private Integer				uid;
	/**
	 * 当前Channel所在的groupid
	 */
	private String				groupid;
	/**
	 * 
	 */
	private ChatroomJoinLeave	chatroomJoinLeave	= null;
	/**
	 * 是否已经握过手
	 */
	private boolean				isHandshaked		= false;
	private boolean				isWebsocket			= false;
	private boolean				isIos				= false;
	private boolean				isAndroid			= false;
	private boolean				isSuper				= false;
	/**
	 * 上一次发群聊消息的时间
	 */
	private long				lastGroupChatTime	= 0;
	private HandshakeReq		handshakeReq;
	/**
	 * 如果是ws，则有此对象
	 */
	private WsSessionContext	wsSessionContext;
	/**
	 * ip地址信息
	 */
	private DataBlock			dataBlock;
	/**
	 * 音视频通话的id
	 */
	private Long				callId				= null;

	public RateLimiterWrap getRequestRateLimiter() {
		return requestRateLimiter;
	}

	public void setRequestRateLimiter(RateLimiterWrap requestRateLimiter) {
		this.requestRateLimiter = requestRateLimiter;
	}

	public boolean isHandshaked() {
		return isHandshaked;
	}

	public void setHandshaked(boolean isHandshaked) {
		this.isHandshaked = isHandshaked;
	}

	public DataBlock getDataBlock() {
		return dataBlock;
	}

	public void setDataBlock(DataBlock dataBlock) {
		this.dataBlock = dataBlock;
	}

	/**
	 * @return the isWebsocket
	 */
	public boolean isWebsocket() {
		return isWebsocket;
	}

	/**
	 * @param isWebsocket the isWebsocket to set
	 */
	public void setWebsocket(boolean isWebsocket) {
		this.isWebsocket = isWebsocket;
	}

	/**
	 * @return the wsSessionContext
	 */
	public WsSessionContext getWsSessionContext() {
		return wsSessionContext;
	}

	/**
	 * @param wsSessionContext the wsSessionContext to set
	 */
	public void setWsSessionContext(WsSessionContext wsSessionContext) {
		this.wsSessionContext = wsSessionContext;
	}

	/**
	 * @return the handshakeReq
	 */
	public HandshakeReq getHandshakeReq() {
		return handshakeReq;
	}

	/**
	 * @param handshakeReq the handshakeReq to set
	 */
	public void setHandshakeReq(HandshakeReq handshakeReq) {
		this.handshakeReq = handshakeReq;
	}

	/**
	 * 握手时的用户id，注意：就算后来被踢了，此值仍然不会被清空，可用于数据统计，但不能用此值来获取当前用户
	 * @return
	 * @author: tanyaowu
	 */
	public Integer getUid() {
		return uid;
	}

	/**
	 * 握手时的用户id，注意：就算后来被踢了，此值仍然不会被清空，可用于数据统计，但不能用此值来获取当前用户
	 * @param uid
	 * @author: tanyaowu
	 */
	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public long getLastGroupChatTime() {
		return lastGroupChatTime;
	}

	public void setLastGroupChatTime(long lastGroupChatTime) {
		this.lastGroupChatTime = lastGroupChatTime;
	}

	public boolean isIos() {
		return isIos;
	}

	public void setIos(boolean isIos) {
		this.isIos = isIos;
	}

	public boolean isAndroid() {
		return isAndroid;
	}

	public void setAndroid(boolean isAndroid) {
		this.isAndroid = isAndroid;
	}

	//	public SimpleUser getLastLoginSimpleUser() {
	//		return lastLoginSimpleUser;
	//	}
	//
	//	public void setLastLoginSimpleUser(SimpleUser lastLoginSimpleUser) {
	//		this.lastLoginSimpleUser = lastLoginSimpleUser;
	//	}

	public boolean isSuper() {
		return isSuper;
	}

	public void setSupper(boolean isSuper) {
		this.isSuper = isSuper;
	}

	/**
	 * @return the groupid
	 */
	public String getGroupid() {
		return groupid;
	}

	/**
	 * @param groupid the groupid to set
	 */
	public void setGroupid(String groupid) {
		this.groupid = groupid;
	}

	public ChatroomJoinLeave getChatroomJoinLeave() {
		return chatroomJoinLeave;
	}

	public void setChatroomJoinLeave(ChatroomJoinLeave chatroomJoinLeave) {
		this.chatroomJoinLeave = chatroomJoinLeave;
	}

	public boolean isWx() {
		return isWx;
	}

	public void setWx(boolean isWx) {
		this.isWx = isWx;
	}

	/**
	 * @return the callId
	 */
	public Long getCallId() {
		return callId;
	}

	/**
	 * @param callId the callId to set
	 */
	public void setCallId(Long callId) {
		this.callId = callId;
	}
}
