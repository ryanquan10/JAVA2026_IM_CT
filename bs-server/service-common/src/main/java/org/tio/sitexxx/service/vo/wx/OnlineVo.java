
package org.tio.sitexxx.service.vo.wx;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 在线对象
 * 
 * @author lixinji
 * 2020年9月14日 上午11:44:43
 */
public class OnlineVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8393515913684620621L;

	/**
	 * 设备类型
	 */
	private Short devicetype;

	/**
	 * 通道
	 */
	private String channelid;

	/**
	 * ipid
	 */
	private Integer ipid;

	/**
	 * 同设备在线数
	 */
	public AtomicInteger count = new AtomicInteger(1);

	/**
	 * 用户id
	 */
	private Integer uid;

	public Short getDevicetype() {
		return devicetype;
	}

	public void setDevicetype(Short devicetype) {
		this.devicetype = devicetype;
	}

	public Integer getIpid() {
		return ipid;
	}

	public void setIpid(Integer ipid) {
		this.ipid = ipid;
	}

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public String getChannelid() {
		return channelid;
	}

	public void setChannelid(String channelid) {
		this.channelid = channelid;
	}

	/**
	 * @param args
	 * @author lixinji 2020年2月10日 下午3:12:59
	 */
	public static void main(String[] args) {

	}
}
