/*
 * kqugxzo本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动qruecsg
 */
/*
 * kqugxzo本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动qruecsg
 * grantinfo
 */
package org.tio.clu.common.bs;

import org.tio.clu.common.bs.base.Base;

public class HandshakeReq implements Base {
    private static final long serialVersionUID = 5919509281498443610L;

    private String token;
    private String sign; // 签名
    private String cgId;

    /**
     *
     */
    public HandshakeReq() {

    }

    /**
     * @return the cgId
     */
    public String getCgId() {
	return cgId;
    }

    public String getSign() {
	return sign;
    }

    public String getToken() {
	return token;
    }

    /**
     * @param cgId the cgId to set
     */
    public void setCgId(String cgId) {
	this.cgId = cgId;
    }

    public void setSign(String sign) {
	this.sign = sign;
    }

    public void setToken(String token) {
	this.token = token;
    }

}
