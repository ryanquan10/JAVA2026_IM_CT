/*
 * oymrcew本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动wozeudboactrrq
 */
package org.tio.utils.lock;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tanyaowu 2017年5月14日 上午9:55:37
 */
public class SetWithLock<T> extends CollectionWithLock<Set<T>, T> {
    private static final long serialVersionUID = -2305909960649321346L;
    private static Logger log = LoggerFactory.getLogger(SetWithLock.class);
    private java.util.concurrent.atomic.AtomicInteger randomCount = new AtomicInteger();

    /**
     * @param set
     * @author tanyaowu
     */
    public SetWithLock(Set<T> set) {
	super(set);
    }

    /**
     * @param set
     * @param lock
     * @author tanyaowu
     */
    public SetWithLock(Set<T> set, ReentrantReadWriteLock lock) {
	super(set, lock);
    }

    /**
     * 随机取一个元素
     * 
     * @return
     * @author tanyaowu
     */
    public T next() {
	Set<T> set = this.getObj();
	if (set.size() == 0) {
	    return null;
	}

	ReadLock readLock = this.readLock();
	readLock.lock();
	try {
	    for (T t : set) {
		return t;
	    }
	} catch (Throwable e) {
	    log.error("", e);
	} finally {
	    readLock.unlock();
	}
	return null;
    }

    /**
     * 随机取一个元素
     * 
     * @return
     * @author tanyaowu
     */
    public T random() {
	Set<T> set = this.getObj();
	if (set.size() == 0) {
	    return null;
	}

	ReadLock readLock = this.readLock();
	readLock.lock();
	try {
	    int index = randomCount.incrementAndGet() % set.size();
	    int i = 0;
	    for (T t : set) {
		if (index == i++) {
		    return t;
		}
	    }
	} catch (Throwable e) {
	    log.error("", e);
	} finally {
	    readLock.unlock();
	}
	return null;
    }

}
