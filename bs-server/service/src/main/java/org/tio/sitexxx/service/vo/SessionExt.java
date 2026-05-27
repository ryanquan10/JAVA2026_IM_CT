
package org.tio.sitexxx.service.vo;

import org.tio.sitexxx.service.model.main.LoginLog;
import org.tio.utils.SystemTimer;

/**
 * @author tanyaowu 
 * 2016年9月27日 上午10:42:10
 */
public class SessionExt implements java.io.Serializable {
	private static final long serialVersionUID = 801028575637420672L;

	public SessionExt() {
		super();
	}

	private long		createTime	= SystemTimer.currTime;
	/**
	 * 当前用户id
	 */
	private Integer		uid			= null;
	/**
	 * 登录时间
	 */
	private Long		loginTime	= null;
	/**
	 * 被T信息
	 */
	private LoginLog	kickedInfo	= null;

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public Long getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(Long loginTime) {
		this.loginTime = loginTime;
	}

	public LoginLog getKickedInfo() {
		return kickedInfo;
	}

	public void setKickedInfo(LoginLog kickedInfo) {
		this.kickedInfo = kickedInfo;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

}
