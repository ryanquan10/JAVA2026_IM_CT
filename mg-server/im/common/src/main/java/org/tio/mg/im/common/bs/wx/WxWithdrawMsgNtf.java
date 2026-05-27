
package org.tio.mg.im.common.bs.wx;

import java.io.Serializable;

public class WxWithdrawMsgNtf implements Serializable {
	private static final long serialVersionUID = -6500881346973672462L;

	/**
	 * mid
	 */
	private Long mid;

	/**
	 * 群组
	 */
	private Long g = null;

	/**
	 * 执行撤消操作的用户
	 */
	private Integer operUid;

	/**
	 * 被撤消消息的用户
	 */
	private Integer uid;

	public Long getMid() {
		return mid;
	}

	public void setMid(Long mid) {
		this.mid = mid;
	}

	public Long getG() {
		return g;
	}

	public void setG(Long g) {
		this.g = g;
	}

	public Integer getOperUid() {
		return operUid;
	}

	public void setOperUid(Integer operUid) {
		this.operUid = operUid;
	}

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

}
