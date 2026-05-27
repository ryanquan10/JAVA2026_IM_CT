
package org.tio.mg.im.common;

import java.util.Objects;

public enum Command {

	/**
	 * 心跳请求
	 */
	HeartbeatReq((short) 1),
	/**
	 * 握手请求
	 */
	HandshakeReq((short) 2),
	/**
	 * 握手响应
	 */
	HandshakeResp((short) 3),
	/**
	 * 进入群组请求
	 */
	JoinGroupReq((short) 4),
	/**
	 * 进入群组响应
	 */
	JoinGroupResp((short) 5),
	/**
	 * 进入群组通知
	 */
	JoinGroupNtf((short) 6),
	/**
	 * 离开群组通知
	 */
	LeaveGroupNtf((short) 7),
	/**
	 * 点对点聊天（私聊）请求
	 */
	P2pChatReq((short) 8),
	/**
	 * 点对点聊天（私聊）通知
	 */
	P2pChatNtf((short) 9),
	/**
	 * 群聊请求
	 */
	GroupChatReq((short) 10),
	/**
	 * 群聊通知
	 */
	GroupChatNtf((short) 11),
	/**
	 * 获取p2p聊天记录数据-请求
	 */
	P2pQueryChatRecordReq((short) 12),
	/**
	 * 执行一段JS脚本
	 */
	RunJsNtf((short) 14),
	/**
	 * 让客户端关闭当前页面（只作用于WEB端）
	 */
	ClosePage((short) 15),
	/**
	 * 消息提示
	 */
	MsgTip((short) 16),
	/**
	 * 分页获取在线观众请求
	 */
	PageOnlineReq((short) 18),
	/**
	 * 分页获取在线观众响应
	 */
	PageOnlineResp((short) 19),
	/**
	 * 更新token
	 */
	UpdateTokenReq((short) 20),
	/**
	 * 更新token响应
	 */
	UpdateTokenResp((short) 21),
	/**
	 * 撤回消息
	 */
	UnsendMsgReq((short) 22),
	/**
	 * 撤回消息通知
	 */
	UnsendMsgNtf((short) 23),
	/**
	 * 用户动作日志
	 */
	UserActionLogReq((short) 24),
	/**
	 * 已读请求： 我告诉服务器，张三发给我的私聊消息已读
	 */
	P2pAlreadyReadReq((short) 25),
	/**
	 * 已读通知： 服务器告诉张三，张三发给李四的私聊，李四已经阅读
	 */
	P2pAlreadyReadNtf((short) 26),
	/**
	 *  查询最近私聊列表请求
	 */
	P2pRecentChatListReq((short) 27),
	/**
	 *  查询最近私聊列表响应
	 */
	P2pRecentChatListResp((short) 28),

    //	 ----------- 下面是微信的命令码 --------------------------------------------------------------------------------
    //	/**
    //	 *  微信功能。添加好友请求（用http）
    //	 */
    //	WX_APPLY_FRIEND_REQ((short) 600),

	/**
	 * 握手请求
	 */
	WxHandshakeReq((short) 599),
	/**
	 * 握手响应
	 */
	WxHandshakeResp((short) 600),

	/**
	 *  服务器通知用户"有人请求加你为好友啦"-- Server-->Client
	 */
	WxApplyFriendNtf((short) 601),

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

	/**
	 * 已读请求： 告诉服务器，和某人的私聊信息已经阅读了
	 */
	WxFriendAlreadyReadReq((short) 608),
	/**
	 * 已读通知： 服务器转告张三，张三发给李四的私聊，李四已经阅读
	 */
	WxFriendAlreadyReadNtf((short) 609),

	/**
	 * 已读请求： 告诉服务器，某群的信息已经阅读了
	 */
	WxGroupAlreadyReadReq((short) 610),
    /**
     * 已读通知： 服务器转告群员，张三已经阅读过群消息（暂不实现）
     */
    //	WX_GROUP_ALREADY_READ_NTY((short) 611),

	/**
	 * 撤回消息请求
	 * 规则：
	 * 1、自己只能撤回两分钟以内的消息
	   2、超级管理员可以不受限制地随时随地撤回任何人的消息（前端用isSuper标识的，后端会二次检查）
	 */
	WxWithdrawMsgReq((short) 612),
	/**
	 * 撤回消息通知
	 */
	WxWithdrawMsgNtf((short) 613),
	/**
	 * 离群通知。当某用户被T出群，或群被删除时，用户会收到这个通知
	* 消息体中有个type字段，用以标示离群原因：1：主动退群；2：被T出群；3：群被删除
	 */
	WxLeaveGroupNtf((short) 614),
	
	/**
	 * 你们不是好友
	 * 你发消息给对方时，你并不是对方的好友，这时候前端提示当前用户发送申请好友请求
	 */
	WxNotFriendNtf((short) 615),
	/**************************************xufei-im-friend-begin**********************************************************/
	/**
	 * 获取群聊聊天记录--请求-- Client-->Server
	 */
	WxGroupMsgReq((short) 620),

	/**
	 * 获取群聊聊天记录--响应-- Server-->Client
	 */
	WxGroupMsgResp((short) 621),
	
	/**
	 * 用户操作通知
	 */
	WxUserOperNtf((short) 700),
	
	/**
	 * 用户相关错误通知
	 */
	WxFriendErrorNtf((short) 701),
	
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
	 * 用户系统通知
	 */
	WxUserSysNtf((short) 738),
	
	/**
	 * 群操作通知
	 */
	WxGroupOperNtf((short) 750),
	
	/**************************************xufei-im-friend--end-**********************************************************/
	
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
	/* ----------------- webrtc 相关命令  end  -----------------  */
	
	/**
	 *  DEMO 请求消息
	 */
	DemoReq((short) 10000),

	/**
	 *  DEMO 通知消息
	 */
	DemoNtf((short) 10001),

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
