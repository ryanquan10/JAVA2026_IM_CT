
package org.tio.sitexxx.im.common.bs.wx;

import java.io.Serializable;

/**
 * 撤回消息请求体
 * @author tanyaowu 
 * 2016年11月24日 下午2:25:55
 */
public class WxWithdrawMsgReq implements Serializable {
	private static final long serialVersionUID = -8956671394570248443L;

	/**
	 * mid
	 */
	private Long mid;

	/**
	 * 群组，如果这空则表示是私聊
	 */
	private Long g = null;

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

}
