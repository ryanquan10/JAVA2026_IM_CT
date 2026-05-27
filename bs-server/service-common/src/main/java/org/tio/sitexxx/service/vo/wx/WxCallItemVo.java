
package org.tio.sitexxx.service.vo.wx;

import java.io.Serializable;

/**
 * 音视频对象
 * 
 * @author lixinji 2020年2月10日 下午3:12:31
 */
public class WxCallItemVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5500652530550301909L;

	/**
	 * 音视频类型
	 */
	private Short calltype;

	/**
	 * 挂断类型
	 */
	private Short hanguptype;

	/**
	 * 挂断用户
	 */
	private Integer hangupuid;

	private Short devicetype;

	/**
	 * 通话时长
	 */
	private Long duration;

	public Short getDevicetype() {
		return devicetype;
	}

	public void setDevicetype(Short devicetype) {
		this.devicetype = devicetype;
	}

	public Short getCalltype() {
		return calltype;
	}

	public void setCalltype(Short calltype) {
		this.calltype = calltype;
	}

	public Short getHanguptype() {
		return hanguptype;
	}

	public void setHanguptype(Short hanguptype) {
		this.hanguptype = hanguptype;
	}

	public Integer getHangupuid() {
		return hangupuid;
	}

	public void setHangupuid(Integer hangupuid) {
		this.hangupuid = hangupuid;
	}

	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	/**
	 * @param args
	 * @author lixinji 2020年2月10日 下午3:12:59
	 */
	public static void main(String[] args) {

	}
}
