
package org.tio.mg.im.common.bs;


import java.io.Serializable;

import org.tio.mg.service.vo.SimpleUser;

public class UnSendMsgNtf implements Serializable {

	private static final long serialVersionUID = 2796430713670650904L;

	/**
	 * mid
	 */
	private String mid;

	/**
	 * 群组
	 */
	private String g = null;

	/**
	 * 执行撤消操作的用户
	 */
	private SimpleUser oper;

	/**
	 * 被撤消消息的用户，也就是聊天消息的发送者
	 */
	private SimpleUser beOper;

	/**
	 * @return the mid
	 */
	public String getMid() {
		return mid;
	}

	/**
	 * @param mid the mid to set
	 */
	public void setMid(String mid) {
		this.mid = mid;
	}

	/**
	 * @return the oper
	 */
	public SimpleUser getOper() {
		return oper;
	}

	/**
	 * @param oper the oper to set
	 */
	public void setOper(SimpleUser oper) {
		this.oper = oper;
	}

	/**
	 * @return the beOper
	 */
	public SimpleUser getBeOper() {
		return beOper;
	}

	/**
	 * @param beOper the beOper to set
	 */
	public void setBeOper(SimpleUser beOper) {
		this.beOper = beOper;
	}

	/**
	 * @return the g
	 */
	public String getG() {
		return g;
	}

	/**
	 * @param g the g to set
	 */
	public void setG(String g) {
		this.g = g;
	}
}
