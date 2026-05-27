
/**
 *
 */
package org.tio.mg.im.common.bs.wx;

import java.io.Serializable;

/**
 * 心跳包-- Client-->Server
 * @author xufei
 * 2020年2月3日 下午3:23:44
 */
public class HeartbeatReq implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -462048823973668176L;
	private Long				chatlinkid;											//	聊天会话
	public Long getChatlinkid() {
		return chatlinkid;
	}
	public void setChatlinkid(Long chatlinkid) {
		this.chatlinkid = chatlinkid;
	}
}
