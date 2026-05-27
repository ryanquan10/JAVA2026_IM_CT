
/**
 *
 */
package org.tio.mg.im.common.bs;

import java.io.Serializable;

/**
 * 私 聊
 * @author tanyaowu 

 */
public class P2PChatReq implements Serializable {
	private static final long	serialVersionUID	= 7365945567713702051L;
	private String				c;											//	聊天内容
	private Integer				to;											//	消息接收者的userid

	public String getC() {
		return c;
	}

	public void setC(String c) {
		this.c = c;
	}

	public Integer getTo() {
		return to;
	}

	public void setTo(Integer to) {
		this.to = to;
	}

}
