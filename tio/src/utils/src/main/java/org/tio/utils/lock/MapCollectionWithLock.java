/*
 * ranbisrkurj本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动lfvvjaejt
 */
package org.tio.utils.lock;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.hutool.core.lang.UUID;

/**
 * MapWithLock<K, CollectionWithLock<CC, V>>
 * 
 * @author tanyaowu 2020年8月19日 下午5:31:05
 */
public abstract class MapCollectionWithLock<K, V, CC extends Collection<V>, C extends CollectionWithLock<CC, V>> extends ObjWithLock<MapWithLock<K, C>> {
	private static final long	serialVersionUID	= 3511183015908156445L;
	private static Logger		log					= LoggerFactory.getLogger(MapCollectionWithLock.class);
	private MapWithLock<K, C>	mapWithLock			= null;													// new MapWithLock<>(new HashMap<K, C>>());
	private final String		LOCK_KEY			= UUID.fastUUID().toString();

	public MapCollectionWithLock(MapWithLock<K, C> map) {
		super(map);
		mapWithLock = map;
	}

	public MapCollectionWithLock(MapWithLock<K, C> map, ReentrantReadWriteLock lock) {
		super(map, lock);
	}

	/**
	 * 清空数据
	 * 
	 * @author tanyaowu
	 */
	public void clear() {
		mapWithLock.clear();
	}

	/**
	 * 
	 * @param key
	 * @return
	 * @author tanyaowu
	 */
	public C get(K key) {
		return mapWithLock.get(key);
	}

	public abstract C newCollection();

	/**
	 * 
	 * @param key
	 * @param value
	 * @author tanyaowu
	 */
	public void put(K key, V value) {
		C setWithLock = putIfAbsent(key);
		setWithLock.add(value);
	}

	/**
	 * 
	 * @param key
	 * @param values
	 * @author tanyaowu
	 */
	public void putAll(K key, CC values) {
		C setWithLock = putIfAbsent(key);
		setWithLock.addAll(values);
	}

	public C putIfAbsent(K key) {
		if (Objects.isNull(key)) {
			return null;
		}
		C setWithLock = mapWithLock.get(key);
		try {

			if (setWithLock == null) {
				LockUtils.runWriteOrWaitRead(LOCK_KEY + "_" + key, this, () -> {
					if (mapWithLock.get(key) == null) {
						mapWithLock.put(key, newCollection());
					}
				});
				setWithLock = mapWithLock.get(key);
			}

		} catch (Throwable e) {
			log.error("", e);
		}
		return setWithLock;

	}

	/**
	 * 从map中删除key
	 * 
	 * @param key
	 * @author tanyaowu
	 */
	public void remove(K key) {
		if (key == null) {
			return;
		}

		mapWithLock.remove(key);
	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @param values
	 * @author tanyaowu
	 */
	public void remove(K key, V value, CC values) {
		if (key == null) {
			return;
		}

		Lock lock = mapWithLock.writeLock();
		lock.lock();
		try {
			Map<K, C> map = mapWithLock.getObj();
			C setWithLock = map.get(key);
			if (setWithLock == null) {
				return;
			}

			WriteLock writeLock = setWithLock.writeLock();
			writeLock.lock();
			try {
				CC set = setWithLock.getObj();
				if (value != null) {
					set.remove(value);
				}
				if (values != null && !values.isEmpty()) {
					set.removeAll(values);
				}

				if (set.isEmpty()) {
					map.remove(key);
				}
			} catch (Throwable e) {
				log.error(e.getMessage(), e);
			} finally {
				writeLock.unlock();
			}
		} catch (Throwable e) {
			throw e;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 从Set中删除指定的value，删除该value后，如果set为空，则在map中删除key
	 * 
	 * @param key
	 * @param value
	 * @author tanyaowu
	 */
	public void removeItem(K key, V value) {
		remove(key, value, null);
	}

	/**
	 * 从Set中删除指定的values，删除该value后，如果set为空，则在map中删除key
	 * 
	 * @param key
	 * @param values
	 * @author tanyaowu
	 */
	public void removeItems(K key, CC values) {
		remove(key, null, values);
	}

	/**
	 * 从value集合中删除值为指定值的记录，value集合为空时，删除key
	 * 
	 * @param value
	 * @param values
	 * @author tanyaowu
	 */
	public void removeValue(V value, CC values) {
		if (mapWithLock.size() == 0) {
			return;
		}
		Lock lock = mapWithLock.writeLock();
		lock.lock();
		try {
			Map<K, C> map = mapWithLock.getObj();
			Set<Entry<K, C>> set1 = map.entrySet();
			Set<K> keysForDel = new HashSet<>();
			for (Entry<K, C> entry : set1) {
				K key = entry.getKey();
				C setWithLock = entry.getValue();
				if (setWithLock == null) {
					continue;
				}

				WriteLock writeLock = setWithLock.writeLock();
				writeLock.lock();
				try {
					CC set = setWithLock.getObj();
					if (value != null) {
						set.remove(value);
					}
					if (values != null && !values.isEmpty()) {
						set.removeAll(values);
					}

					if (set.isEmpty()) {
						// map.remove(key);
						keysForDel.add(key);
					}
				} catch (Throwable e) {
					log.error(e.getMessage(), e);
				} finally {
					writeLock.unlock();
				}
			}

			// 不能在里面直接remove，需要放到外面来remove，否则有并发异常
			if (keysForDel.size() > 0) {
				for (K k : keysForDel) {
					map.remove(k);
				}
			}
		} catch (Throwable e) {
			throw e;
		} finally {
			lock.unlock();
		}
	}

}
