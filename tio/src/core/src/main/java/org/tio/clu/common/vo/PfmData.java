/*
 * ztypnba本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动icweetv
 */
package org.tio.clu.common.vo;

/**
 * 性能数据
 * 
 * @author tanyaowu 2020年8月27日 上午11:32:37
 */
public class PfmData implements java.io.Serializable {

    private static final long serialVersionUID = -3995096244468989242L;
    /**
     * tcp连接数
     */
    private int tcpCount = 0;

    /**
     * 
     * @author tanyaowu
     */
    public PfmData() {
    }

    /**
     * @return the tcpCount
     */
    public int getTcpCount() {
	return tcpCount;
    }

    /**
     * @param tcpCount the tcpCount to set
     */
    public void setTcpCount(int tcpCount) {
	this.tcpCount = tcpCount;
    }
}
