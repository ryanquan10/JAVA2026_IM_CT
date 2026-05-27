
package org.tio.sitexxx.service.vo.wx;

import java.io.Serializable;

/**
 * 焦点通知对象
 * 
 * @author lixinji 2020年8月25日 下午3:12:31
 */
public class FocusNtfVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5308233215714622555L;

	/**
	 * 设备类型
	 */
	private Short devicetype;

	/**
	 * 通道
	 */
	private String channelid;

	/**
	 * 会话id
	 */
	private Long chatlinkid;

	/**
	 * 群id
	 */
	private Long groupid;

	/**
	 * 会话模型
	 */
	private Short chatmode;

	/**
	 * ipid
	 */
	private Integer ipid;

	/**
	 * 用户id
	 */
	private Integer uid;

	/**
	 * 
	 */
	private Short oper;

	/**
	 * 操作Code
	 * @author lixinji
	 * 2020年6月5日 下午2:18:37
	 */
	public static interface operCode {
		/**
		 * 进入
		 */
		short JOIN = 1;

		/**
		 * 离开
		 */
		short LEAVE = 2;

		/**
		 * 刷新
		 */
		short REFRESH = 3;
	}

	public String getChannelid() {
		return channelid;
	}

	public void setChannelid(String channelid) {
		this.channelid = channelid;
	}

	public Short getOper() {
		return oper;
	}

	public void setOper(Short oper) {
		this.oper = oper;
	}

	public Short getChatmode() {
		return chatmode;
	}

	public Long getGroupid() {
		return groupid;
	}

	public void setGroupid(Long groupid) {
		this.groupid = groupid;
	}

	public void setChatmode(Short chatmode) {
		this.chatmode = chatmode;
	}

	public Short getDevicetype() {
		return devicetype;
	}

	public void setDevicetype(Short devicetype) {
		this.devicetype = devicetype;
	}

	public Long getChatlinkid() {
		return chatlinkid;
	}

	public void setChatlinkid(Long chatlinkid) {
		this.chatlinkid = chatlinkid;
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

	/**
	 * @param args
	 * @author lixinji 2020年2月10日 下午3:12:59
	 */
	public static void main(String[] args) {

	}
}
