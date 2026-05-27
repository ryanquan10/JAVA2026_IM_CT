
package org.tio.mg.im.common.bs;

import java.io.Serializable;

/**
 * 我告诉服务器，张三发给我的私聊消息已读
 * @author tanyaowu 

 */
public class P2PAlreadyReadReq implements Serializable {
	private static final long serialVersionUID = -5440095646275589930L;

	/**
	 * 私聊的from userid
	 */
	private Integer	fromuid;
	private Long	mid	= 0L;

	public Integer getFromuid() {
		return fromuid;
	}

	public void setFromuid(Integer fromuid) {
		this.fromuid = fromuid;
	}

	public Long getMid() {
		return mid;
	}

	public void setMid(Long mid) {
		this.mid = mid;
	}

}