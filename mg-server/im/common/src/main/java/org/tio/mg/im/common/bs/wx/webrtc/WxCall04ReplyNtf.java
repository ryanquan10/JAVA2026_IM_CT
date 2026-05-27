
/**
 *
 */
package org.tio.mg.im.common.bs.wx.webrtc;

import org.tio.mg.im.common.bs.wx.webrtc.base.WxCallBase;

/**
 * b回复s：同意通话，或拒绝通话（1、同意通话、2、拒接）
 * @author tanyaowu 
 * 2016年9月12日 下午3:09:08
 */
public class WxCall04ReplyNtf extends WxCallBase {

	private static final long serialVersionUID = -7629291844755041469L;

	/**
	 * 不能通话的原因，当result=2时，此字段才有值
	 */
	private String reason = null;

	/**
	 * 1、同意通话、2、拒接
	 */
	private Short result = null;

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
