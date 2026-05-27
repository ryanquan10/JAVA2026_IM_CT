
package org.tio.mg.im.common.bs.demo;

import java.io.Serializable;

/**
 *
 * @author tanyaowu
 */
public class DemoReq implements Serializable {
	private static final long serialVersionUID = -3152602617323397757L;
	private String text;
	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}
	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}
}
