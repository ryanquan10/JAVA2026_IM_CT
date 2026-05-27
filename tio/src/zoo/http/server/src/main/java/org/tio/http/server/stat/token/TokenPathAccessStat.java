/*
 * taqojotegb本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动esjevzudbmjwj
 */
package org.tio.http.server.stat.token;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.tio.utils.SystemTimer;
import org.tio.utils.hutool.BetweenFormater;
import org.tio.utils.hutool.BetweenFormater.Level;

/**
 * token访问路径统计
 * 
 * @author tanyaowu 2017年10月27日 下午1:53:03
 */
public class TokenPathAccessStat implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3463584577647075747L;

    private Long durationType;

    /**
     * 当前统计了多久，单位：毫秒
     */
    private long duration;

    /**
     * token
     */
    private String token;

    private String ip;

    private String uid;

    /**
     * 路径
     */
    private String path;

    /**
     * 第一次访问时间， 单位：毫秒
     */
    private long firstAccessTime = SystemTimer.currTime;

    /**
     * 最近一次访问时间， 单位：毫秒
     */
    private long lastAccessTime = SystemTimer.currTime;

    /**
     * 这个token访问这个路径的次数
     */
    public final AtomicInteger count = new AtomicInteger();

    /**
     * 这个token访问这个路径给服务器带来的时间消耗，单位：毫秒
     */
    public final AtomicLong timeCost = new AtomicLong();

    /**
     * 
     * @param durationType
     * @param token
     * @param path
     * @param ip
     * @param uid
     */
    public TokenPathAccessStat(Long durationType, String token, String path, String ip, String uid) {
	this.durationType = durationType;
	this.token = token;
	this.path = path;
	this.ip = ip;
	this.uid = uid;
    }

    public long getDuration() {
	duration = SystemTimer.currTime - this.firstAccessTime;
	return duration;
    }

    public Long getDurationType() {
	return durationType;
    }

    public long getFirstAccessTime() {
	return firstAccessTime;
    }

    /**
     * @return the duration
     */
    public String getFormatedDuration() {
	duration = SystemTimer.currTime - this.firstAccessTime;
	BetweenFormater betweenFormater = new BetweenFormater(duration, Level.MILLSECOND);
	return betweenFormater.format();
    }

    public String getIp() {
	return ip;
    }

    public long getLastAccessTime() {
	return lastAccessTime;
    }

    public String getPath() {
	return path;
    }

    public double getPerSecond() {
	int count = this.count.get();
	long duration = getDuration();
	double perSecond = (double) count / (double) duration * 1000;
	return perSecond;
    }

    public String getToken() {
	return token;
    }

    public String getUid() {
	return uid;
    }

    public void setDuration(long duration) {
	this.duration = duration;
    }

    public void setDurationType(Long durationType) {
	this.durationType = durationType;
    }

    public void setFirstAccessTime(long firstAccessTime) {
	this.firstAccessTime = firstAccessTime;
    }

    public void setIp(String ip) {
	this.ip = ip;
    }

    public void setLastAccessTime(long lastAccessTime) {
	this.lastAccessTime = lastAccessTime;
    }

    public void setPath(String path) {
	this.path = path;
    }

    public void setToken(String token) {
	this.token = token;
    }

    public void setUid(String uid) {
	this.uid = uid;
    }
}
