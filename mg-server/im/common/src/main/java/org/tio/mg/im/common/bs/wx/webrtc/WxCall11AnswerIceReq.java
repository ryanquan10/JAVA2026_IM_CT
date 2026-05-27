
/**
 *
 */
package org.tio.mg.im.common.bs.wx.webrtc;

import java.util.Map;

import org.tio.mg.im.common.bs.wx.webrtc.base.WxCallBase;

/**
 * b向a回复Answer，需要提供 e.candidate
 * @author tanyaowu
 */
public class WxCall11AnswerIceReq extends WxCallBase {

	private static final long serialVersionUID = -6904397543091665252L;

	private Map<String, Object> candidate = null;

	/**
	 * @return the candidate
	 */
	public Map<String, Object> getCandidate() {
		return candidate;
	}

	/**
	 * @param candidate the candidate to set
	 */
	public void setCandidate(Map<String, Object> candidate) {
		this.candidate = candidate;
	}
}
