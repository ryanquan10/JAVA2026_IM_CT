/*
 * pqdrdhmjsi本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动utirqeap
 */
package org.tio.http.common.session.limiter;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tanyaowu 2018年12月5日 下午10:36:28
 */
public class SessionRateVo implements Serializable {
    private static final long serialVersionUID = 5585145117550534333L;
    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(SessionRateVo.class);

    public static SessionRateVo create(String path) {
	return new SessionRateVo(path);
    }

    /**
     * @param args
     * @author tanyaowu
     */
    public static void main(String[] args) {

    }

    private String path = null;

    /**
     * 上一次访问时间
     */
    private long lastAccessTime = 0;

    /**
     * 已经访问了多少次（一分钟）
     */
    private AtomicInteger accessCount = new AtomicInteger();

    /**
     * 
     * @author tanyaowu
     */
    public SessionRateVo(String path) {
	this.path = path;
    }

    /**
     * @return the accessCount
     */
    public AtomicInteger getAccessCount() {
	return accessCount;
    }

    /**
     * @return the lastAccessTime
     */
    public long getLastAccessTime() {
	return lastAccessTime;
    }

    public String getPath() {
	return path;
    }

    /**
     * @param accessCount the accessCount to set
     */
    public void setAccessCount(AtomicInteger accessCount) {
	this.accessCount = accessCount;
    }

    /**
     * @param lastAccessTime the lastAccessTime to set
     */
    public void setLastAccessTime(long lastAccessTime) {
	this.lastAccessTime = lastAccessTime;
    }

    public void setPath(String path) {
	this.path = path;
    }
}
