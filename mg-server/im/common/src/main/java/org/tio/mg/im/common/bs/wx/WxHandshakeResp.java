
package org.tio.mg.im.common.bs.wx;

import java.io.Serializable;

public class WxHandshakeResp implements Serializable {
	private static final long serialVersionUID = 3269628193908001549L;
	private String cid;

	/**
	 * @return the cid
	 */
	public String getCid() {
		return cid;
	}

	/**
	 * @param cid the cid to set
	 */
	public void setCid(String cid) {
		this.cid = cid;
	}
}
