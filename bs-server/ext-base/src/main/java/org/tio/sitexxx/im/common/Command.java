
package org.tio.sitexxx.im.common;

import java.util.Objects;

public enum Command {

	/**
	 * 执行一段JS脚本
	 */
	RunJsNtf((short) 14),
	/**
	 * 消息提示
	 */
	MsgTip((short) 16),
	/**
	 * 更新token
	 */
	UpdateTokenReq((short) 20),
	/**
	 * 更新token响应
	 */
	UpdateTokenResp((short) 21),
	
	/**
	 * 用户动作日志
	 */
	UserActionLogReq((short) 24),
	
	/**************************************lixinji-钛信-链接-begin**********************************************************/
	
	/**
	 * 心跳请求
	 */
	HeartbeatReq((short) 1),
	
	/**
	 * 握手请求
	 */
	WxHandshakeReq((short) 599),
	/**
	 * 握手响应
	 */
	WxHandshakeResp((short) 600),
	
	/**************************************lixinji-钛信-链接--end--**********************************************************/
	
	
	/**************************************lixinji-钛信-请求/响应-begin**********************************************************/
	/**
	 *  朋友聊天请求，wx_friend_msg-- Client-->Server
	 */
	WxFriendChatReq((short) 602),

	/**
	 *  朋友聊天通知，wx_friend_msg-- Server-->Client
	 */
	WxFriendChatNtf((short) 603),
	
	/**
	 * 获取两好友间聊天记录--请求-- Client-->Server
	 */
	WxFriendMsgReq((short) 604),

	/**
	 * 获取两好友间聊天记录--响应-- Server-->Client
	 */
	WxFriendMsgResp((short) 605),

	/**
	 * 群聊请求-- Client-->Server
	 */
	WxGroupChatReq((short) 606),
	
	/**
	 * 群聊通知-- Server-->Client
	 */
	WxGroupChatNtf((short) 607),

	ForbiddenNotify((short) 622),

	
	/**
	 * 获取群聊聊天记录--请求-- Client-->Server
	 */
	WxGroupMsgReq((short) 620),

	/**
	 * 获取群聊聊天记录--响应-- Server-->Client
	 */
	WxGroupMsgResp((short) 621),
	
	/**
	 * 用户会话信息请求
	 */
	WxChatItemInfoReq((short) 708),
	
	/**
	 * 用户会话信息响应
	 */
	WxChatItemInfoResp((short) 709),
	
	
	/**
	 * 用户会话操作
	 */
	WxSessionOperReq((short) 710),
	
	/**
	 * 用户消息操作
	 */
	WxMsgOperReq((short) 711),
	
	/**
	 * 用户消息操作响应
	 */
	WxMsgOperResp((short) 712),
	
	/**
	 * 更新token
	 */
	WxUpdateTokenReq((short) 760),
	
	/**
	 * 更新token响应
	 */
	WxUpdateTokenResp((short) 761),
	
	/**
	 * 焦点请求
	 */
	WxFocusReq((short) 776),

	/**
	 * 焦点刷新
	 */
	WxFocusRefReq((short) 775),
	
	/**
	 * 焦点通知
	 */
	WxFocusNtf((short) 777),
	
	/**************************************lixinji-钛信-请求/响应--end--**********************************************************/
	
	/**************************************lixinji-钛信-通知-begin**********************************************************/
	
	/**
	 * 用户操作通知
	 */
	WxUserOperNtf((short) 700),

	/**
	 * 用户发布朋友圈消息通知
	 */
	WxMomentsOperNtf((short) 301),

	/**
	 * 用户发布朋友圈评论或喜欢消息通知
	 */
	WxMomentsCommentsOrLikesOperNtf((short) 302),

	/**
	 * 用户发布朋友圈评论或喜欢消息通知
	 */
	CircleIdCard((short) 303),

	/**
	 * 笔记
	 */
	Note((short) 304),

	/**
	 * 用户相关错误通知
	 */
	WxFriendErrorNtf((short) 701),
	
	/**
	 * 用户系统通知
	 */
	WxUserSysNtf((short) 738),

	/**
	 * 群操作通知
	 */
	WxGroupOperNtf((short) 750),
	
	
	/**************************************lixinji-钛信-通知--end--**********************************************************/
	
	/**************************************lixinji-钛信-同步通知-begin**********************************************************/
	
	/**
	 * 记录同步通知
	 */
	WxSynRecordNtf((short) 911),
	/**************************************lixinji-钛信-同步通知--end--**********************************************************/
	
	
	/* ----------------- webrtc 相关命令  start  -----------------  */
	// a请求和b通话，代码实现（s：服务器）
	/**
	 * a --> s   a向b发起通话请求
	 */
	WxCall01Req((short) 800),
	/**
	 * s --> b   s通知b，此时a和b要处于“占线”状态，后续呼入要直接拒绝
	 */
	WxCall02Ntf((short) 801),
	/**
	 * b --> s   b回复s：同意通话，或拒绝通话（拒绝原因：1、对方拒接，2、对方不在线， 3、对方占线，99、其它原因）
	 */
	WxCall03ReplyReq((short) 802),
	/**
	 * s --> a   s转告a
	 */
	WxCall04ReplyNtf((short) 803),
	/**
	 * a --> s   a向b提供offer，需要提供 sdp
	 */
	WxCall05OfferSdpReq((short) 804),
	/**
	 * s --> b   s转发给b
	 */
	WxCall06OfferSdpNtf((short) 805),
	/**
	 * b --> s   b向a回复Answer，需要提供 sdp
	 */
	WxCall07AnswerSdpReq((short) 806),
	/**
	 * s --> a   s转发给a
	 */
	WxCall08AnswerSdpNtf((short) 807),
	/**
	 * a --> s   a向b提供offer，需要提供 e.candidate
	 */
	WxCall09OfferIceReq((short) 808),
	/**
	 * s --> b   s转发给b
	 */
	WxCall10OfferIceNtf((short) 809),
	/**
	 * b --> s   b向a回复Answer，需要提供 e.candidate
	 */
	WxCall11AnswerIceReq((short) 810),
	/**
	 * s --> a   s转发给a
	 */
	WxCall12AnswerIceNtf((short) 811),
	/**
	 * a或b --> s   发起结束通话请求
	 */
	WxCall13EndReq((short) 812),
	/**
	 * s    --> a和b   通知结束通话，通话原因：1、对方主动挂电话；2、网络不好
	 */
	WxCall14EndNtf((short) 813),
	
	/**
	 * a    --> s   取消通话请求（a发起通话后，在对方响应前进行了取消操作）
	 */
	WxCall02_1CancelReq((short) 814),
	/**
	 * s    --> a&b   取消通话通知（a发起通话后，在对方响应前进行了取消操作）
	 */
	WxCall02_2CancelNtf((short) 815),
	
	/**
	 * 客户端最新同步信息
	 */
	WxCallRespNtf((short) 888),
	/* ----------------- webrtc 相关命令  end  -----------------  */
	
	
	/* ----------------- file transfer 相关命令  start  -----------------  */
	FtResSynReq((short) 2100), //资源文件同步请求
	FtResSynResp((short) 2101), //资源文件同步响应
	FtDownloadFileReq((short) 2102), //下载文件请求
	FtDownloadFileResp((short) 2103), //下载文件响应
	/* ----------------- file transfer 相关命令  end  -----------------  */
	/**
	 *  DEMO 请求消息
	 */
	DemoReq((short) 10000),

	/**
	 *  DEMO 通知消息
	 */
	DemoNtf((short) 10001),


	/**
	 * 用户在线离线通知
	 */
	YxOnOffLinePush((short)10006),


	xxxxx((short) 99999);

	public static Command from(Short value) {
		Command[] values = Command.values();
		for (Command v : values) {
			if (Objects.equals(v.value, value)) {
				return v;
			}
		}
		return null;
	}

	Short value;

	private Command(Short value) {
		this.value = value;
	}

	public Short getValue() {
		return value;
	}

	public void setValue(Short value) {
		this.value = value;
	}
}
