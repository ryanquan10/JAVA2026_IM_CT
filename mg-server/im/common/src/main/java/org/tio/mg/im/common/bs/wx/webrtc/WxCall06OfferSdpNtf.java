
/**
 *
 */
package org.tio.mg.im.common.bs.wx.webrtc;

import java.util.Map;

import org.tio.mg.im.common.bs.wx.webrtc.base.WxCallBase;

/**
 * a向b提供offer，需要提供 sdp
 * @author tanyaowu 
 * 2016年9月12日 下午3:09:08
 */
public class WxCall06OfferSdpNtf extends WxCallBase {

	private static final long	serialVersionUID	= -5029952299037917305L;
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
