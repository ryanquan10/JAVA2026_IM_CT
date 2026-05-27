/*
 * bweirzlyizqa本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动audnhgrzq
 */
package org.tio.utils.lock;

import java.io.Serializable;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自带读写锁的对象.
 *
 * @author tanyaowu
 */
public class ObjWithLock<T> implements Serializable {

    private static final long serialVersionUID = -3048283373239453901L;

    private static Logger log = LoggerFactory.getLogger(ObjWithLock.class);

    /**
     * 
     */
    private T obj = null;

    /**
     * 
     */
    private ReentrantReadWriteLock lock = null;

    /**
     * 
     * @param obj
     * @author tanyaowu
     */
    public ObjWithLock(T obj) {
	this(obj, new ReentrantReadWriteLock());
    }

    /**
     * 
     * @param obj
     * @param lock
     * @author tanyaowu
     */
    public ObjWithLock(T obj, ReentrantReadWriteLock lock) {
	super();
	this.obj = obj;
	if (lock == null) {
		throw new RuntimeException("lock is null");
	}
	this.lock = lock;
    }

    /**
     * 
     * @return
     * @author tanyaowu
     */
    public ReentrantReadWriteLock getLock() {
	return lock;
    }

    /**
     * 
     * @return
     * @author tanyaowu
     */
    public T getObj() {
	return obj;
    }

    /**
     * 操作obj时，带上读锁
     * 
     * @param readLockHandler
     */
    public void handle(ReadLockHandler<T> readLockHandler) {
	ReadLock readLock = lock.readLock();
	readLock.lock();
	try {
	    readLockHandler.handler(obj);
	} catch (Throwable e) {
	    log.error("", e);
	} finally {
	    readLock.unlock();
	}
    }

    /**
     * 操作obj时，带上写锁
     * 
     * @param writeLockHandler
     */
    public void handle(WriteLockHandler<T> writeLockHandler) {
	WriteLock writeLock = lock.writeLock();
	writeLock.lock();
	try {
	    writeLockHandler.handler(obj);
	} catch (Throwable e) {
	    log.error(e.getMessage(), e);
	} finally {
	    writeLock.unlock();
	}
    }

    /**
     * 获取读锁
     * 
     * @return
     */
    public ReadLock readLock() {
	return lock.readLock();
    }

    /**
     * 
     * @param obj
     * @author tanyaowu
     */
    public void setObj(T obj) {
	this.obj = obj;
    }

    /**
     * 获取写锁
     * 
     * @return
     */
    public WriteLock writeLock() {
	return lock.writeLock();
    }

}
