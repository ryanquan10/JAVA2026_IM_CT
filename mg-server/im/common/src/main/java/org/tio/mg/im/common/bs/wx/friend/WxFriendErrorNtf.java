
package org.tio.mg.im.common.bs.wx.friend;

import java.io.Serializable;

/**
 * 好友聊天异常通知-- Server-->Client
 * 
 * @author xufei
 * 2020年1月19日 下午6:23:08
 */
public class WxFriendErrorNtf implements Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7934598637676652704L;

	private Long				mid;										//消息id，全局唯一，一条消息一个id
	private Long				t;											//消息发送时间
	private Integer				uid;										//消息发送者
	
	private Integer 			touid;										//消息接受者
	
	private Long 				chatlinkid;									//发送者的会话id
	
	private Integer				code;										//异常码
	
	private String				msg;										//错误信息
	
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

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public Integer getTouid() {
		return touid;
	}

	public void setTouid(Integer touid) {
		this.touid = touid;
	}

	public Long getChatlinkid() {
		return chatlinkid;
	}

	public void setChatlinkid(Long chatlinkid) {
		this.chatlinkid = chatlinkid;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
}
