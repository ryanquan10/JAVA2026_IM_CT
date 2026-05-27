
/**
 *
 */
package org.tio.mg.im.common.bs.demo;

import java.io.Serializable;

/**
 * 
 * @author tanyaowu 
 * 2016年9月12日 下午3:09:08
 */
public class DemoNtf implements Serializable {

	private static final long serialVersionUID = 2088352299449577670L;
	private String msg;

	/**
	 * @return the msg
	 */
	public String getMsg() {
		return msg;
	}

	/**
	 * @param msg the msg to set
	 */
	public void setMsg(String msg) {
		this.msg = msg;
	}
}
