/*
 * yjymkk本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动tzvybguy
 */
/*
 * yjymkk本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动tzvybguy
 * grantinfo
 */

package org.tio.clu.common.bs;

import org.tio.clu.common.bs.base.Base;

public class HandshakeResp implements Base {

    private static final long serialVersionUID = 717125275721290222L;
    private String cid;

    /**
     * @return the cid
     */
    public String getCid() {
	return cid;
    }

    /**
     * @param cid the cid to set
     */
    public void setCid(String cid) {
	this.cid = cid;
    }
}
