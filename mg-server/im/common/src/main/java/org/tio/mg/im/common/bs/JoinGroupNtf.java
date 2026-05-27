
/**
 *
 */
package org.tio.mg.im.common.bs;


import java.io.Serializable;

import org.tio.mg.service.vo.SimpleUser;
import org.tio.utils.SystemTimer;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 
 * @author tanyaowu 
 * 2016年9月12日 下午3:09:08
 */
public class JoinGroupNtf implements Serializable {
	private static final long	serialVersionUID	= -1244796335456901498L;
	private String				g;											//	群组id（groupid）
	private SimpleUser			u					= null;					//	进入群组的用户信息
	@JSONField(serialize = false)
	private Long				t					= SystemTimer.currTime;	//	进入群组的时间
	private Integer				online				= null;					//当前群组在线人数
	private Integer				ipcount				= null;					//一共多少个ip在线
	private String				cid					= null;					//连接id（connection_id）

	public String getG() {
		return g;
	}

	public void setG(String g) {
		this.g = g;
	}

	/**
	 * 进入群组的用户信息
	 * @return
	 * @author: tanyaowu
	 */
	public SimpleUser getU() {
		return u;
	}

	/**
	 * 进入群组的用户信息
	 * @param u
	 * @author: tanyaowu
	 */
	public void setU(SimpleUser u) {
		this.u = u;
	}

	public Long getT() {
		return t;
	}

	public void setT(Long t) {
		this.t = t;
	}

	public Integer getOnline() {
		return online;
	}

	public void setOnline(Integer online) {
		this.online = online;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}
	//	public String getToken() {
	//		return token;
	//	}
	//	public void setToken(String token) {
	//		this.token = token;
	//	}
	//	public SimpleUser getLastLoginSimpleUser() {
	//		return lastLoginSimpleUser;
	//	}
	//	public void setLastLoginSimpleUser(SimpleUser lastLoginSimpleUser) {
	//		this.lastLoginSimpleUser = lastLoginSimpleUser;
	//	}

	public Integer getIpcount() {
		return ipcount;
	}

	public void setIpcount(Integer ipcount) {
		this.ipcount = ipcount;
	}

}
