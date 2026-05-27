
package org.tio.sitexxx.im.common.bs.wx.chatitem;

import java.io.Serializable;

/**
 * 获取会话信息--请求-- Client-->Server
 * 
 * @author lixinji
 * 2020年3月10日 下午4:16:52
 */
public class WxChatItemInfoReq implements Serializable {

	private static final long	serialVersionUID	= 5486977980835561814L;
	/**
	 * 聊天会话
	 */
	private Long				chatlinkid;

	public WxChatItemInfoReq() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

	public Long getChatlinkid() {
		return chatlinkid;
	}

	public void setChatlinkid(Long chatlinkid) {
		this.chatlinkid = chatlinkid;
	}

}
