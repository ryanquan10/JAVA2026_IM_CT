
/**
 *
 */
package org.tio.mg.im.common.bs.wx.group;


import java.io.Serializable;

import org.tio.mg.service.model.main.WxGroupMsg;

/**
 * 群操作通知-- Server-->Client
 * @author xufei
 * 2020年1月19日 下午2:56:14
 */
public class WxGroupOperNtf implements Serializable {
	private static final long	serialVersionUID	= 4487525418584644680L;
	private Long				mid;										//消息id，全局唯一，一条消息一个id
	private Long				t;											//消息发送时间
	private String				c;											//聊天内容
	private Long				g					= null;					//groupid
	private Integer				uid;										//发送方的userid
	private Short				oper				= 1;					//操作码：1:删除群；2:转移群；3：被转移群
	private Long				chatlinkid 			= null;					//聊天会话
	
	private Short 				chatmode = 2 ;									//会话模型
	private String				actname;									//激活时的名称
	private String				actavatar;									//激活时的头像
	private Short				grouprole;									//群角色
	
	private String 				bizdata;									//业务数据
	
	public static WxGroupOperNtf from(WxGroupMsg msg) {
		WxGroupOperNtf ret = new WxGroupOperNtf();
		ret.setC(msg.getText());
		ret.setMid(msg.getId());
		ret.setG(msg.getGroupid());
		ret.setChatlinkid(-msg.getGroupid());
		ret.setT(msg.getTime().getTime());
		ret.setUid(msg.getUid());
		return ret;
	}
	
	public Short getGrouprole() {
		return grouprole;
	}

	public void setGrouprole(Short grouprole) {
		this.grouprole = grouprole;
	}

	public String getBizdata() {
		return bizdata;
	}

	public void setBizdata(String bizdata) {
		this.bizdata = bizdata;
	}

	public Long getG() {
		return g;
	}

	public void setG(Long g) {
		this.g = g;
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
}
