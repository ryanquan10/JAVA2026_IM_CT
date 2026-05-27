
package org.tio.mg.im.common.bs;


import java.io.Serializable;

import org.tio.mg.service.vo.SimpleUser;
import org.tio.utils.SystemTimer;

/**
 * 离开群组(群组)通知
 * @author tanyaowu 
 * 2016年9月8日 下午2:10:00
 */
public class LeaveGroupNtf implements Serializable {

	private static final long serialVersionUID = 5203186788396414038L;

	private String g; //离开群组的id

	private Long t = SystemTimer.currTime;

	private SimpleUser u; //离开群组人员的信息

	private Integer online; //当前群组在线人数

	public String getG() {
		return g;
	}

	public void setG(String g) {
		this.g = g;
	}

	public long getT() {
		return t;
	}

	public LeaveGroupNtf(String g, SimpleUser u) {
		super();
		this.g = g;
		this.u = u;
	}

	public void setT(long t) {
		this.t = t;
	}

	public SimpleUser getU() {
		return u;
	}

	public void setU(SimpleUser u) {
		this.u = u;
	}

	/**
	 * 
	 * @author: tanyaowu
	 */
	public LeaveGroupNtf() {
	}

	/**
	 * @param args
	 * @author: tanyaowu
	 */
	public static void main(String[] args) {

	}

	/**
	 * @return the online
	 */
	public Integer getOnline() {
		return online;
	}

	/**
	 * @param online the online to set
	 */
	public void setOnline(Integer online) {
		this.online = online;
	}
}
