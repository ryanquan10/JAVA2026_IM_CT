/*
 * twxgwkzgow本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动xqxvnqqw
 */
package org.tio.utils;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tanyaowu 2017年9月15日 下午4:09:59
 */
public class Uuid {
    private static Logger log = LoggerFactory.getLogger(Uuid.class);

    /**
     * 系统启动时，重设此两值，只
     */
    private static Integer workid = ThreadLocalRandom.current().nextInt(0, 31);

    private static boolean workidSetted = false;
    /**
     * 
     */
    private static Integer datacenterid = ThreadLocalRandom.current().nextInt(0, 31);

    private static boolean datacenteridSetted = false;
    public static int getDatacenterid() {
	return datacenterid;
    }

    public static int getWorkid() {
	return workid;
    }

    public static void setDatacenterid(Integer datacenterid) {
	synchronized (log) {
	    if (datacenteridSetted) {
		if (!Objects.equals(datacenterid, Uuid.datacenterid)) {
		    log.error("datacenterid只允许设置一次");
		}
		return;
	    }
	    if (datacenterid == null) {
		log.error("datacenterid不允许为null");
		return;
	    }

	    Uuid.datacenterid = datacenterid;
	    datacenteridSetted = true;
	}
    }

    public static void setWorkid(Integer workid) {
	synchronized (log) {
	    if (workidSetted) {
		if (!Objects.equals(workid, Uuid.workid)) {
		    log.error("workid只允许设置一次");
		}
		return;
	    }
	    if (workid == null) {
		log.error("workid不允许为null");
		return;
	    }
	    Uuid.workid = workid;
	    workidSetted = true;
	}

    }

    /**
     * 
     * @author: tanyaowu
     */
    public Uuid() {
    }

}
