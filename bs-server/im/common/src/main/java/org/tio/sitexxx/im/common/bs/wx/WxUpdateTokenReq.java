
package org.tio.sitexxx.im.common.bs.wx;

import java.io.Serializable;

/**
 * 
 * @author lixinji
 * 2020年6月2日 上午10:27:37
 */
public class WxUpdateTokenReq implements Serializable {

	private static final long serialVersionUID = 2796430713670650904L;

	/**
	 * token
	 */
	private String t;

	/**
	 * token
	 */
	private String o;

	/**
	 * token
	 * @return
	 */
	public String getT() {
		return t;
	}

	/**
	 * token
	 */
	public void setT(String t) {
		this.t = t;
	}

	public String getO() {
		return o;
	}

	public void setO(String o) {
		this.o = o;
	}
}
