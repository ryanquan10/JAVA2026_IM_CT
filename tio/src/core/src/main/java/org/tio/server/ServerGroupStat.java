/*
 * qxjpzxsjmhncg本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动kbfgzslvke
 */
package org.tio.server;

import java.util.concurrent.atomic.AtomicLong;

import org.tio.core.stat.GroupStat;

/**
 *
 * @author tanyaowu
 *
 */
public class ServerGroupStat extends GroupStat {

    private static final long serialVersionUID = -139100692961946342L;
    /**
     * 接受了多少连接
     */
    public final AtomicLong accepted = new AtomicLong();

    /**
     *
     *
     * @author tanyaowu 2016年12月3日 下午2:29:28
     *
     */
    public ServerGroupStat() {
    }

    /**
     * @return the accepted
     */
    public AtomicLong getAccepted() {
	return accepted;
    }
}
