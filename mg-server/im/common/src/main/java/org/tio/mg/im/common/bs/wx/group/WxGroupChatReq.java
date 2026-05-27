
/**
 *
 */
package org.tio.mg.im.common.bs.wx.group;

import java.io.Serializable;

/**
 * 新版chat群聊
 * @author xufei
 * 2020年2月13日 下午6:40:01
 */
public class WxGroupChatReq implements Serializable {
	private static final long	serialVersionUID	= 623723297327475963L;
	private String				c;											//聊天内容	//举例：大家好
	private String			at;											//艾特哪些用户。此值可为null	  //举例：[434343, 9898989]
//	TODO:XUFEI-删除G
	private Long				g;											//groupid	//举例：45454
	private Long 				chatlinkid; 								// 用户聊天会话
	private Long				cardid;										//名片id
	private Short				cardtype;									//名片类型
	
	public Long getCardid() {
		return cardid;
	}

	public void setCardid(Long cardid) {
		this.cardid = cardid;
	}

	public Short getCardtype() {
		return cardtype;
	}

	public void setCardtype(Short cardtype) {
		this.cardtype = cardtype;
	}

	public Long getChatlinkid() {
		return chatlinkid;
	}

	public void setChatlinkid(Long chatlinkid) {
		this.chatlinkid = chatlinkid;
	}

	public String getC() {
		return c;
	}

	public void setC(String c) {
		this.c = c;
	}
	
	public String getAt() {
		return at;
	}

	public void setAt(String at) {
		this.at = at;
	}

	public Long getG() {
		return g;
	}

	public void setG(Long g) {
		this.g = g;
	}

}
