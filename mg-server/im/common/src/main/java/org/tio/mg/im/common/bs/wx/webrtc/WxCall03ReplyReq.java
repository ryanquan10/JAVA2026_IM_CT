
/**
 *
 */
package org.tio.mg.im.common.bs.wx.webrtc;

import org.tio.mg.im.common.bs.wx.webrtc.base.WxCallBase;

/**
 * b回复s：同意通话，或拒绝通话（1、同意通话、2、拒接）
 * @author tanyaowu
 */
public class WxCall03ReplyReq extends WxCallBase {

	private static final long serialVersionUID = -4127892663683835026L;

	/**
	 * 1、同意通话、2、拒接
	 */
	private Short result = null;
	/**
	 * 如果拒接，可以提供拒接原因
	 */
	private String reason = null;

	/**
	 * @return the result
	 */
	public Short getResult() {
		return result;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(Short result) {
		this.result = result;
	}

	/**
	 * @return the reason
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * @param reason the reason to set
	 */
	public void setReason(String reason) {
		this.reason = reason;
	}

}
