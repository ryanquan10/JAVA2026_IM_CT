
/**
 *
 */
package org.tio.sitexxx.im.common.bs.wx.webrtc;

import org.tio.sitexxx.im.common.bs.wx.webrtc.base.WxCallBase;
import org.tio.sitexxx.service.vo.Const;

/**
 * 同步信息
 * @author tanyaowu 
 * 2016年9月12日 下午3:09:08
 */
public class WxCallRespNtf extends WxCallBase {

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
	 * 通道id
	 */
	private String contextid;

	/**
	 * 是否是自己的回辞消息
	 */
	private Short self = Const.YesOrNo.NO;

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

	public Short getSelf() {
		return self;
	}

	public void setSelf(Short self) {
		this.self = self;
	}

	public String getContextid() {
		return contextid;
	}

	public void setContextid(String contextid) {
		this.contextid = contextid;
	}

	/**
	 * @param reason the reason to set
	 */
	public void setReason(String reason) {
		this.reason = reason;
	}
}
