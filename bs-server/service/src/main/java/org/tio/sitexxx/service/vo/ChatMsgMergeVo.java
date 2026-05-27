
package org.tio.sitexxx.service.vo;

import java.io.Serializable;

/**
 * 消息后处理合并Vo
 * @author lixinji
 * 2020年10月12日 下午1:58:00
 */
public class ChatMsgMergeVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4144598704556724322L;

	/**
	 * 当前未读
	 */
	private Integer currCount;

	/**
	 * at偏差
	 */
	private Integer atDev = new Integer(0);

	public ChatMsgMergeVo(Integer currcount) {
		this.currCount = currcount;
	}

	public ChatMsgMergeVo() {

	}

	public int addAndGet(int addcount) {
		currCount = currCount + addcount;
		return currCount;
	}

	public Integer getCurrCount() {
		return currCount;
	}

	public void setCurrCount(Integer currCount) {
		this.currCount = currCount;
	}

	public Integer getAtDev() {
		return atDev;
	}

	public void setAtDev(Integer atDev) {
		this.atDev = atDev;
	}

	public int addAndGetDev(int addcount) {
		atDev = atDev + addcount;
		return atDev;
	}
}
