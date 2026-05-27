/*
 * xxlxdjm本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动thrqnk
 */
package org.tio.utils.prop;

import java.util.HashMap;

import org.tio.utils.lock.MapWithLock;

/**
 * @author tanyaowu 2017年8月18日 下午5:36:02
 */
public class MapWithLockPropSupport implements IPropSupport {

	private static final long					serialVersionUID	= -8521189302541621433L;
	private final MapWithLock<String, Object>	props				= new MapWithLock<>(new HashMap<String, Object>(8));

	/**
	 *
	 * @author tanyaowu
	 */
	public MapWithLockPropSupport() {
	}

	/**
	 * 同：clearAttribute()
	 */
	public void clear() {
		clearAttribute();
	}

	@Override
	public void clearAttribute() {
		props.clear();
	}

	/**
	 * 同：getAttribute(String key)
	 * 
	 * @param key
	 * @return
	 */
	public Object get(String key) {
		return props.getObj().get(key);
	}

	@SuppressWarnings("unchecked")
	public <T> T get(String key, Class<T> clazz) {
		return (T) get(key);
	}

	/**
	 *
	 * @param key
	 * @return
	 * @author tanyaowu
	 */
	@Override
	public Object getAttribute(String key) {
		return get(key);
	}

	// private void initProps() {
	// if (props == null) {
	// synchronized (this) {
	// if (props == null) {
	// props = new MapWithLock<>(new HashMap<String, Object>(10));
	// }
	// }
	// }
	// }

	/**
	 * 同：removeAttribute(String key)
	 * 
	 * @param key
	 */
	public void remove(String key) {
		props.remove(key);
	}

	/**
	 * @param key
	 * @author tanyaowu
	 */
	@Override
	public void removeAttribute(String key) {
		remove(key);
	}

	/**
	 * 同：setAttribute(String key, Object value)
	 * 
	 * @param key
	 * @param value
	 */
	public void set(String key, Object value) {
		props.put(key, value);
	}

	/**
	 *
	 * @param key
	 * @param value
	 * @author tanyaowu
	 */
	@Override
	public void setAttribute(String key, Object value) {
		set(key, value);
	}
}
