
package org.tio.sitexxx.service.vo;

import java.io.Serializable;

import org.tio.sitexxx.service.model.main.WxGroupMsg;

/**
 * 消息发送后处理Vo
 * @author lixinji
 * 2020年10月12日 下午1:58:00
 */
public class AfterGroupMsgSendVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4144598704556724322L;

	public AfterGroupMsgSendVo() {

	}

	private int index = 1;

	/**
	 * @param msg
	 * @param notreadcount
	 * @param viewflag
	 * @param ats
	 */
	public AfterGroupMsgSendVo(WxGroupMsg msg, Short notreadcount, String ats) {
		this.msg = msg;
		this.notreadcount = notreadcount;
		this.ats = ats;
	}

	private WxGroupMsg msg;

	private Short notreadcount;

	private String ats;

	private Short oper;

	private Long chatlinkid;

	public Short getOper() {
		return oper;
	}

	public void setOper(Short oper) {
		this.oper = oper;
	}

	public Long getChatlinkid() {
		return chatlinkid;
	}

	public void setChatlinkid(Long chatlinkid) {
		this.chatlinkid = chatlinkid;
	}

	public String getAts() {
		return ats;
	}

	public void setAts(String ats) {
		this.ats = ats;
	}

	public WxGroupMsg getMsg() {
		return msg;
	}

	public void setMsg(WxGroupMsg msg) {
		this.msg = msg;
	}

	public Short getNotreadcount() {
		return notreadcount;
	}

	public void setNotreadcount(Short notreadcount) {
		this.notreadcount = notreadcount;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
}
