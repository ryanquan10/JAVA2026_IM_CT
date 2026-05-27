
/**
 * 
 */
package org.tio.mg.service.lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import org.redisson.api.RedissonClient;
import org.tio.mg.service.init.RedisInit;

/**
 * 分布式锁
 * @author tanyaowu
 *
 */
public class DisLocks {

	/**
	 * 
	 */
	public DisLocks() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	public static Lock getLock(String name) {
		RedissonClient redisson = RedisInit.get();
		return redisson.getLock(name);
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	public static ReadWriteLock getReadWriteLock(String name) {
		RedissonClient redisson = RedisInit.get();
		return redisson.getReadWriteLock(name);
	}

}
