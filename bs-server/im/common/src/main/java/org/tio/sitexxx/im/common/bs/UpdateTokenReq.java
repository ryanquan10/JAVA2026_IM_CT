
package org.tio.sitexxx.im.common.bs;

import java.io.Serializable;

public class UpdateTokenReq implements Serializable {

	private static final long serialVersionUID = 2796430713670650904L;

	/**
	 * token
	 */
	private String t;

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
}
