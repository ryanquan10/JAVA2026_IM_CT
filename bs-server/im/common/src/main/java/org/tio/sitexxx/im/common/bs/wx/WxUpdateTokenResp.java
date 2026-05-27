
/**
 *
 */
package org.tio.sitexxx.im.common.bs.wx;

import java.io.Serializable;

/**
 * 
 * @author lixinji
 * 2020年6月2日 上午10:22:45
 */
public class WxUpdateTokenResp implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6762250307386131429L;

	private Short code = 1;

	public Short getCode() {
		return code;
	}

	public void setCode(Short code) {
		this.code = code;
	}
}
