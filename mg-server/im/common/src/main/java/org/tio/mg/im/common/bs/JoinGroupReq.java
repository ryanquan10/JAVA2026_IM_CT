
/**
 *
 */
package org.tio.mg.im.common.bs;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JoinGroupReq implements Serializable {
	private static final long serialVersionUID = -4098504817989939005L;

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(JoinGroupReq.class);

	/**
	 * 
	 */
	private String g; //groupid

	/**
	 * 第几次进入群组，不传、0、1表示第一次，其它值表示第几次
	 */
	private Integer c;

	/**
	 *
	 */
	public JoinGroupReq() {

	}

	/**
	 * @return the g
	 */
	public String getG() {
		return g;
	}

	/**
	 * @param g the g to set
	 */
	public void setG(String g) {
		this.g = g;
	}

	public Integer getC() {
		return c;
	}

	public void setC(Integer c) {
		this.c = c;
	}
}
