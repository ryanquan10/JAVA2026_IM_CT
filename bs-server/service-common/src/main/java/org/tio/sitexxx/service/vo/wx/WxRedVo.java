
package org.tio.sitexxx.service.vo.wx;

import java.io.Serializable;

/**
 * 红包vo
 * 
 * @author lixinji 2020年2月10日 下午3:12:31
 */
public class WxRedVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4279416613620223037L;

	/**
	 * 红包订单号
	 */
	private String serialnumber;

	/**
	 * 红包文案
	 */
	private String text;

	/**
	 * 类型：1：普通红包；2：手气红包
	 */
	private Short mode;

	/**
	 * 红包id
	 */
	private Integer rid;

	/**
	 * 状态
	 */
	private String status;

	public Integer getRid() {
		return rid;
	}

	public void setRid(Integer rid) {
		this.rid = rid;
	}

	public Short getMode() {
		return mode;
	}

	public void setMode(Short mode) {
		this.mode = mode;
	}

	public String getSerialnumber() {
		return serialnumber;
	}

	public void setSerialnumber(String serialnumber) {
		this.serialnumber = serialnumber;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @param args
	 * @author lixinji 2020年2月10日 下午3:12:59
	 */
	public static void main(String[] args) {

	}
}
