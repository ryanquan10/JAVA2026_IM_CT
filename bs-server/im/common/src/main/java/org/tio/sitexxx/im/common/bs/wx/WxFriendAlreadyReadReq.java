

package org.tio.sitexxx.im.common.bs.wx;

import java.io.Serializable;

/**
 * 我告诉服务器，张三发给我的私聊消息已读
 * @author tanyaowu 

 */
public class WxFriendAlreadyReadReq implements Serializable {
	private static final long serialVersionUID = -5440095646275589930L;

	/**
	 * 聊天对方的userid
	 */
	private Integer uid;

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

}