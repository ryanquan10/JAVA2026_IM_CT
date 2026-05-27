
package org.tio.sitexxx.service.service.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.model.stat.ImeiStat;
import org.tio.utils.cache.ICache;

import cn.hutool.core.util.StrUtil;

/**
 * @author tanyaowu 
 * 2016年10月25日 下午5:59:49
 */
public class ImeiStatService {
	private static Logger log = LoggerFactory.getLogger(ImeiStatService.class);

	public static final ImeiStatService me = new ImeiStatService();

	/**
	 * 
	 * @author tanyaowu
	 */
	private ImeiStatService() {
	}

	/**
	 * 
	 * @param imeiStat
	 * @author tanyaowu
	 */
	public void save(ImeiStat imeiStat) {
		String imei = imeiStat.getImei();
		if (StrUtil.isBlank(imei)) {
			return;
		}
		synchronized (log) {
			ImeiStat imeiStatInDb = getByImei(imei);
			if (imeiStatInDb != null) {
				return;
			}
			try {
				boolean saved = imeiStat.save();
				if (saved) {
					ICache cache = Caches.getCache(CacheConfig.IMEI_IMEISTAT);
					cache.put(imei, imeiStat);
				}
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}

	/**
	 * 更新
	 * @param imeiStat
	 * @author tanyaowu
	 */
	public void update(ImeiStat imeiStat) {
		try {
			boolean updated = imeiStat.update();
			if (updated) {
				ICache cache = Caches.getCache(CacheConfig.IMEI_IMEISTAT);
				cache.put(imeiStat.getImei(), imeiStat);
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}

	/**
	 * 
	 * @param imei
	 * @return
	 * @author tanyaowu
	 */
	public ImeiStat getByImei(String imei) {
		ICache cache = Caches.getCache(CacheConfig.IMEI_IMEISTAT);
		ImeiStat inCache = cache.get(imei, ImeiStat.class);
		if (inCache != null && inCache.getId() != null) {
			return inCache;
		}

		synchronized (log) {
			inCache = cache.get(imei, ImeiStat.class);
			if (inCache != null && inCache.getId() != null) {
				return inCache;
			}

			ImeiStat indb = ImeiStat.dao.findFirst("select * from imei_stat where imei=?", imei);
			if (indb != null) {
				cache.put(imei, indb);
				return indb;
			}
			return null;
		}
	}

	/**
	 * @param args
	 * @author tanyaowu
	 */
	public static void main(String[] args) {

	}
}
