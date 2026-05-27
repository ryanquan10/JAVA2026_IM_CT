
/**
 *
 */
package org.tio.sitexxx.im.common.bs.wx.sys;

import java.io.Serializable;
import java.util.Map;

/**
 * 焦点变更通知-- Server-->Client
 * @author lixinji
 * 2020年8月25日 下午2:56:14
 */
public class WxFocusNtf implements Serializable {
	private static final long serialVersionUID = -2712989373320633810L;

	private Long		t	= System.currentTimeMillis();	//消息发送时间
	Map<String, Short>	focusMap;

	public Long getT() {
		return t;
	}

	public void setT(Long t) {
		this.t = t;
	}

	public Map<String, Short> getFocusMap() {
		return focusMap;
	}

	public void setFocusMap(Map<String, Short> focusMap) {
		this.focusMap = focusMap;
	}
}
