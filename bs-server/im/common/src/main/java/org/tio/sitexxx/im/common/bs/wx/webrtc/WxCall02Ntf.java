
/**
 *
 */
package org.tio.sitexxx.im.common.bs.wx.webrtc;

import org.tio.sitexxx.im.common.bs.wx.webrtc.base.WxCallBase;

/**
 * s --> b   s通知b，此时a和b要处于“占线”状态，后续呼入要直接拒绝
 * @author tanyaowu 
 * 2016年9月12日 下午3:09:08
 */
public class WxCall02Ntf extends WxCallBase {

	private static final long serialVersionUID = -7622496583705674037L;

	private String fromnick;

	private String fromavatar;

	public String getFromnick() {
		return fromnick;
	}

	public void setFromnick(String fromnick) {
		this.fromnick = fromnick;
	}

	public String getFromavatar() {
		return fromavatar;
	}

	public void setFromavatar(String fromavatar) {
		this.fromavatar = fromavatar;
	}

	public WxCall02Ntf() {
		super();
	}

}
