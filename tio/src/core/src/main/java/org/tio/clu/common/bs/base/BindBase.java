/*
 * jesieiucofn本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动mxriaquts
 */
/*
 * jesieiucofn本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动mxriaquts
 * grantinfo
 */
package org.tio.clu.common.bs.base;

/**
 * 和BindType相关的请求包
 * 
 * @author tanyaowu 2020年8月25日 下午3:51:32
 */
public class BindBase implements Base {

    private static final long serialVersionUID = -1831052812512843411L;
    private String v;
	private String[]			vs;
	private String				fromClientId		= org.tio.clu.client.Cc.CLIENT_ID;
    /**
     * bindType
     */
    private byte bt;

    /**
     *
     */
    public BindBase() {

    }

    /**
     * @return the bt
     */
    public byte getBt() {
	return bt;
    }

    /**
     * @return the v
     */
    public String getV() {
	return v;
    }

    /**
     * @return the vs
     */
    public String[] getVs() {
	return vs;
    }

    /**
     * @param bt the bt to set
     */
    public void setBt(byte bt) {
	this.bt = bt;
    }

    /**
     * @param v the v to set
     */
    public void setV(String v) {
	this.v = v;
    }

    /**
     * @param vs the vs to set
     */
    public void setVs(String[] vs) {
	this.vs = vs;
	}

	public String getFromClientId() {
		return fromClientId;
	}

	public void setFromClientId(String fromClientId) {
		this.fromClientId = fromClientId;
	}
}
