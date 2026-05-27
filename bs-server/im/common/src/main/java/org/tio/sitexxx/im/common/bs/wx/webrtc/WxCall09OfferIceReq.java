
/**
 *
 */
package org.tio.sitexxx.im.common.bs.wx.webrtc;

import java.util.Map;

import org.tio.sitexxx.im.common.bs.wx.webrtc.base.WxCallBase;

/**
 * a向b提供offer，需要提供 e.candidate
 * @author tanyaowu
 */
public class WxCall09OfferIceReq extends WxCallBase {

	private static final long serialVersionUID = -5429433571371631520L;

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
