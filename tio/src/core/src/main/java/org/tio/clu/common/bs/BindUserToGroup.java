/*
 * skhrlezwiazwv本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动xazgpua
 */
/*
 * skhrlezwiazwv本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动xazgpua
 * grantinfo
 */
package org.tio.clu.common.bs;

import org.tio.clu.common.bs.base.TransferBase;

public class BindUserToGroup extends TransferBase {

	private static final long serialVersionUID = 7946872123227029414L;

	public BindUserToGroup() {
		super();
	}
	/**
	 * 
	 * @param userid
	 * @param group
	 */
	public BindUserToGroup(String userid, String group) {
		super();
		this.userid = userid;
		this.group = group;
	}

	private String	userid;
	private String	group;

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

}
