
/**
 *
 */
package org.tio.sitexxx.im.common.bs.wx.friend;

import java.io.Serializable;

import org.tio.sitexxx.service.model.main.WxChatItems;
import org.tio.sitexxx.service.model.main.WxFriendMsg;

/**
 * 用户操作通知-- Server-->Client
 * @author lixinji
 * 2020年1月19日 下午2:56:14
 */
public class WxUserOperNtf implements Serializable {
	private static final long	serialVersionUID	= 4487525418584644680L;
	private Long				mid;										//消息id，全局唯一，一条消息一个id
	private Long				t;											//消息发送时间
	private String				c;											//聊天内容
	private Integer				uid;										//发送方的userid
	private Short				oper				= 1;					//操作码：1:删除聊天会话；2：拉黑；3：恢复拉黑 见：org.tio.sitexxx.service.vo.Const.WxUserOper
	private Long				chatlinkid			= null;					//聊天会话
	private Short				actflag				= 2;					//激活状态
	private Long				g;											//群组id

	private Short		chatmode;		//会话模型
	private String		actname;		//激活时的名称
	private String		actavatar;		//激活时的头像
	private Short		joinnum	= 2;	//激活时的会话数
	private Short		grouprole;		//激活时群的角色
	private String		operbizdata;	//操作业务id
	private WxChatItems	chatItems;

	public static WxUserOperNtf from(WxFriendMsg wxFriendMsg) {
		WxUserOperNtf ret = new WxUserOperNtf();
		ret.setC(wxFriendMsg.getText());
		ret.setMid(wxFriendMsg.getId());
		ret.setT(wxFriendMsg.getTime().getTime());
		ret.setUid(wxFriendMsg.getUid());
		return ret;
	}

	public Short getGrouprole() {
		return grouprole;
	}

	public void setGrouprole(Short grouprole) {
		this.grouprole = grouprole;
	}

	public Long getG() {
		return g;
	}

	public void setG(Long g) {
		this.g = g;
	}

	public Short getJoinnum() {
		return joinnum;
	}

	public WxChatItems getChatItems() {
		return chatItems;
	}

	public void setChatItems(WxChatItems chatItems) {
		this.chatItems = chatItems;
	}

	public String getOperbizdata() {
		return operbizdata;
	}

	public void setOperbizdata(String operbizdata) {
		this.operbizdata = operbizdata;
	}

	public void setJoinnum(Short joinnum) {
		this.joinnum = joinnum;
	}

	public Short getChatmode() {
		return chatmode;
	}

	public void setChatmode(Short chatmode) {
		this.chatmode = chatmode;
	}

	public String getActname() {
		return actname;
	}

	public void setActname(String actname) {
		this.actname = actname;
	}

	public String getActavatar() {
		return actavatar;
	}

	public void setActavatar(String actavatar) {
		this.actavatar = actavatar;
	}

	public Short getOper() {
		return oper;
	}

	public void setOper(Short oper) {
		this.oper = oper;
	}

	public Long getChatlinkid() {
		return chatlinkid;
	}

	public void setChatlinkid(Long chatlinkid) {
		this.chatlinkid = chatlinkid;
	}

	public Long getMid() {
		return mid;
	}

	public void setMid(Long mid) {
		this.mid = mid;
	}

	public Long getT() {
		return t;
	}

	public void setT(Long t) {
		this.t = t;
	}

	public String getC() {
		return c;
	}

	public void setC(String c) {
		this.c = c;
	}

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public Short getActflag() {
		return actflag;
	}

	public void setActflag(Short actflag) {
		this.actflag = actflag;
	}
}
