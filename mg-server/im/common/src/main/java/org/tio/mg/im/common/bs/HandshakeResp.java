
package org.tio.mg.im.common.bs;

import java.io.Serializable;

public class HandshakeResp implements Serializable {

	private static final long serialVersionUID = 3136144083795894404L;

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
