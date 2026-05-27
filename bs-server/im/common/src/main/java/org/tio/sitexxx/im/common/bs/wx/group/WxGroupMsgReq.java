
package org.tio.sitexxx.im.common.bs.wx.group;

import java.io.Serializable;

/**
 *获取群聊聊天记录--请求-- Client-->Server
 * 
 * @author lixinji
 * 2020年3月10日 下午3:41:42
 */
public class WxGroupMsgReq implements Serializable {

	private static final long	serialVersionUID	= 5486977980835561814L;
	/**
	 * 聊天会话
	 */
	private Long				chatlinkid;

	/**
	 * 起始消息id
	 */
	private Long startmid;

	/**
	 * 截止消息id
	 */
	private Long endmid;

	public WxGroupMsgReq() {
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

	public Long getStartmid() {
		return startmid;
	}

	public void setStartmid(Long startmid) {
		this.startmid = startmid;
	}

	public Long getEndmid() {
		return endmid;
	}

	public void setEndmid(Long endmid) {
		this.endmid = endmid;
	}

}
