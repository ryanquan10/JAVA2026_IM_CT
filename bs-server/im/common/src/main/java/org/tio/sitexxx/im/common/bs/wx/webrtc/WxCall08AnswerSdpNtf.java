
/**
 *
 */
package org.tio.sitexxx.im.common.bs.wx.webrtc;

import java.util.Map;

import org.tio.sitexxx.im.common.bs.wx.webrtc.base.WxCallBase;

/**
 * b向a回复Answer，需要提供 sdp
 * @author tanyaowu 
 * 2016年9月12日 下午3:09:08
 */
public class WxCall08AnswerSdpNtf extends WxCallBase {

	private static final long	serialVersionUID	= -8825956076485401393L;
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
