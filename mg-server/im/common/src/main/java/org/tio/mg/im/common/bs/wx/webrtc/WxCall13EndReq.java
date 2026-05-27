
/**
 *
 */
package org.tio.mg.im.common.bs.wx.webrtc;

import org.tio.mg.im.common.bs.wx.webrtc.base.WxCallBase;

/**
 * a或b --> s   发起结束通话请求
 * 需要传hanguptype
 * @author tanyaowu
 */
public class WxCall13EndReq extends WxCallBase {
	private static final long serialVersionUID = -45874627811555944L;
	private String msg = null;
	/**
	 * @return the msg
	 */
	public String getMsg() {
		return msg;
	}
	/**
	 * @param msg the msg to set
	 */
	public void setMsg(String msg) {
		this.msg = msg;
	}

}
