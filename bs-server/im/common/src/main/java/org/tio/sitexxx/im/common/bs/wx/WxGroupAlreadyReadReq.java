

package org.tio.sitexxx.im.common.bs.wx;

import java.io.Serializable;

/**
 * 已读请求： 告诉服务器，某群的信息已经阅读了
 * @author tanyaowu 

 */
public class WxGroupAlreadyReadReq implements Serializable {
	private static final long serialVersionUID = 416626707627204367L;

	/**
	 * groupid
	 */
	private Long	g;
	private Long	mid;	//此值可有可无，如果有则是表示该值以前的消息都标识为已读

	public Long getG() {
		return g;
	}

	public void setG(Long g) {
		this.g = g;
	}

	/**
	 * @return the mid
	 */
	public Long getMid() {
		return mid;
	}

	/**
	 * @param mid the mid to set
	 */
	public void setMid(Long mid) {
		this.mid = mid;
	}

}