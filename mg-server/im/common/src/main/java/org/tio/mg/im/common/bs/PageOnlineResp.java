
package org.tio.mg.im.common.bs;


import java.io.Serializable;

import org.tio.mg.service.vo.SimpleUser;
import org.tio.utils.page.Page;

/**
 * 分页获取在线观众响应
 * @author tanyaowu
 *
 */
public class PageOnlineResp implements Serializable {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 2576703830092738601L;
	/**
	 * 哪种客户端的在线列表，1：pc，2：安卓，3：IOS，null：所有端
	 */
	private Integer				type;
	/**
	 * all端
	 */
	private Page<SimpleUser>	all;
	/**
	 * pc端
	 */
	private Page<SimpleUser>	pc;
	/**
	 * android端
	 */
	private Page<SimpleUser>	android;
	/**
	 * ios端
	 */
	private Page<SimpleUser>	ios;

	public Page<SimpleUser> getPc() {
		return pc;
	}

	public void setPc(Page<SimpleUser> pc) {
		this.pc = pc;
	}

	public Page<SimpleUser> getAndroid() {
		return android;
	}

	public void setAndroid(Page<SimpleUser> android) {
		this.android = android;
	}

	public Page<SimpleUser> getIos() {
		return ios;
	}

	public void setIos(Page<SimpleUser> ios) {
		this.ios = ios;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Page<SimpleUser> getAll() {
		return all;
	}

	public void setAll(Page<SimpleUser> all) {
		this.all = all;
	}

}
