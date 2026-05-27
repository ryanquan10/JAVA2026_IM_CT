
/**
 * 
 */
package org.tio.sitexxx.service.utils;

import java.io.Serializable;
import java.util.List;

import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.utils.cache.CacheUtils;
import org.tio.utils.cache.FirsthandCreater;
import org.tio.utils.cache.ICache;

/**
 * @author tanyaowu
 *
 */
public class QueryUtils {

	/**
	 * 
	 */
	public QueryUtils() {

	}

	/**
	 * 
	 * @param cacheConfig CacheConfig.TIME_TO_LIVE_MINUTE_5
	 * @param cacheKey "stat.ipCountByDay_" + days
	 * @param db Const.Db.TIO_SITE_STAT
	 * @param sqlid stat.ipCountByDay
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Record> findWithCache(CacheConfig cacheConfig, String cacheKey, String db, String sqlid, boolean putTempToCacheIfNull, Object... params) {
		ICache cache = Caches.getCache(cacheConfig);
		Serializable ret = CacheUtils.get(cache, cacheKey, putTempToCacheIfNull, new FirsthandCreater<Serializable>() {
			@Override
			public Serializable create() {
				SqlPara sqlPara = Db.use(db).getSqlPara(sqlid);
				if (params != null) {
					for (Object object : params) {
						sqlPara.addPara(object);
					}
				}
				List<Record> list1 = Db.use(db).find(sqlPara);
				if (list1 != null && list1.size() == 0) {
					return null;
				}
				return (Serializable) list1;
			}
		});

		return (List<Record>) ret;
	}

	@SuppressWarnings("unchecked")
	public static List<Record> findBySqlWithCache(CacheConfig cacheConfig, String cacheKey, String db, String sql, boolean putTempToCacheIfNull, Object... params) {
		ICache cache = Caches.getCache(cacheConfig);
		Serializable ret = CacheUtils.get(cache, cacheKey, putTempToCacheIfNull, new FirsthandCreater<Serializable>() {
			@Override
			public Serializable create() {
				List<Record> list1 = Db.use(db).find(sql, params);
				if (list1 != null && list1.size() == 0) {
					return null;
				}
				return (Serializable) list1;
			}
		});

		return (List<Record>) ret;
	}

	public static Record findFirstWithCache(CacheConfig cacheConfig, String cacheKey, String db, String sqlid, boolean putTempToCacheIfNull, Object... params) {
		ICache cache = Caches.getCache(cacheConfig);

		Record ret = CacheUtils.get(cache, cacheKey, putTempToCacheIfNull, new FirsthandCreater<Record>() {
			@Override
			public Record create() {
				SqlPara sqlPara = Db.use(db).getSqlPara(sqlid);
				if (params != null) {
					for (Object object : params) {
						sqlPara.addPara(object);
					}
				}

				Record record = Db.use(db).findFirst(sqlPara);
				return record;
			}
		});

		return ret;

	}

	public static Record findByIdWithCache(CacheConfig cacheConfig, String cacheKey, String db, String tableName, Object id, boolean putTempToCacheIfNull) {
		ICache cache = Caches.getCache(cacheConfig);

		Record ret = CacheUtils.get(cache, cacheKey, putTempToCacheIfNull, new FirsthandCreater<Record>() {
			@Override
			public Record create() {
				Record record = Db.use(db).findById(tableName, id);
				return record;
			}
		});

		return ret;

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
