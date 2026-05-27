
/**
 *
 */
package org.tio.sitexxx.im.common.bs.wx.webrtc;

import java.util.Map;

import org.tio.sitexxx.im.common.bs.wx.webrtc.base.WxCallBase;

/**
 * b向a回复Answer，需要提供 e.candidate
 * @author tanyaowu 
 * 2016年9月12日 下午3:09:08
 */
public class WxCall12AnswerIceNtf extends WxCallBase {

	private static final long serialVersionUID = -7581282988753157107L;

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
