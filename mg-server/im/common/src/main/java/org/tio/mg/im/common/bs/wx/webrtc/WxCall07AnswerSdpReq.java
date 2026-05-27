
/**
 *
 */
package org.tio.mg.im.common.bs.wx.webrtc;

import java.util.Map;

import org.tio.mg.im.common.bs.wx.webrtc.base.WxCallBase;

/**
 * b向a回复Answer，需要提供 sdp
 * @author tanyaowu
 */
public class WxCall07AnswerSdpReq extends WxCallBase {

	private static final long	serialVersionUID	= 930882512167587163L;
	private Map<String, Object>	sdp					= null;

	/**
	 * @return the sdp
	 */
	public Map<String, Object> getSdp() {
		return sdp;
	}

	/**
	 * @param sdp the sdp to set
	 */
	public void setSdp(Map<String, Object> sdp) {
		this.sdp = sdp;
	}
}
