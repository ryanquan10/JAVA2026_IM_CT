/*
 * xahjuulsdb本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动tmksycfvrcqj
 */
/*
 * xahjuulsdb本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动tmksycfvrcqj
 * grantinfo
 */
package org.tio.clu.common.bs;

import org.tio.clu.common.bs.base.Base;

public class BestNodeReq implements Base {

    private static final long serialVersionUID = -459230460371664532L;
    private Integer uid = null;
    private String protocol;

    public String getProtocol() {
	return protocol;
    }

    /**
     * @return the uid
     */
    public Integer getUid() {
	return uid;
    }

    public void setProtocol(String protocol) {
	this.protocol = protocol;
    }

    /**
     * @param uid the uid to set
     */
    public void setUid(Integer uid) {
	this.uid = uid;
    }

}
