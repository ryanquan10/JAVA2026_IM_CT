/*
 * iyriyinyac本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动peadzzkdb
 */
/*
 * iyriyinyac本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动peadzzkdb
 * grantinfo
 */

package org.tio.clu.common.bs.base;

/**
 * 
 * @author tanyaowu 2020年8月25日 下午3:46:16
 */
public class BaseResp implements Base {
    private static final long serialVersionUID = -6827857662973403188L;

    /**
     * 消息，一般用于显示
     */
    private String msg;

    /**
     * 业务编码：一般是在失败情况下会用到这个，以便告知用户失败的原因是什么
     */
    private Integer code;
    /**
     * 
     */
    private boolean ok = true;

    public Integer getCode() {
	return code;
    }

    public String getMsg() {
	return msg;
    }

    public boolean isOk() {
	return ok;
    }

    public void setCode(Integer code) {
	this.code = code;
    }

    public void setMsg(String msg) {
	this.msg = msg;
    }

    public void setOk(boolean ok) {
	this.ok = ok;
    }

}
