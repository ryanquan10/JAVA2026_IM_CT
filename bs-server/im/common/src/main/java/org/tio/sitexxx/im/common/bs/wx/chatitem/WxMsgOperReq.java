

package org.tio.sitexxx.im.common.bs.wx.chatitem;

import java.io.Serializable;

/**
 * 会话消息操作
 * @author tanyaowu 

 */
public class WxMsgOperReq implements Serializable {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -8838786116225726932L;
	/**
	 * 会话id
	 */
	private Long				chatlinkid;

	/**
	 * 操作码：1：进入；2；离开会话
	 */
	private Short oper;

	public Long getChatlinkid() {
		return chatlinkid;
	}

	public void setChatlinkid(Long chatlinkid) {
		this.chatlinkid = chatlinkid;
	}

	public Short getOper() {
		return oper;
	}

	public void setOper(Short oper) {
		this.oper = oper;
	}

}