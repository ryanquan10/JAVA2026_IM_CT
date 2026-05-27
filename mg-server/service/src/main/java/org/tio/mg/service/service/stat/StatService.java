
/**
 * 
 */
package org.tio.mg.service.service.stat;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Page;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.mg.service.cache.CacheConfig;
import org.tio.mg.service.model.stat.TioIpPathAccessStat;
import org.tio.mg.service.utils.QueryUtils;
import org.tio.sitexxx.service.vo.Const;

/**
 * @author tanyaowu
 *
 */
public class StatService {

	/**
	 * 
	 */
	public StatService() {
	}

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(StatService.class);

	public static final StatService me = new StatService();

	final TioIpPathAccessStat dao = new TioIpPathAccessStat().dao();

	private static final int MAX_STAT_DAY = 60;

	/**
	 *  -- 按天统计页面的访问量
		---------------------------
		--     d              path        count
		-- 2018-04-16   /case/index.html	 24
		-- 2018-04-16   /case/index.html	 109
		-- 2018-04-16   /doc/index.html	     18
		-- 2018-04-17   /doc/index.html	     97
		---------------------------
	 * @param path 访问路径，譬如/index.html
	 * @param days 统计多少天的
	 * @return
	 */
	public List<Record> requestCountByDay(Integer days) {
		if (days == null) {
			days = 7;
		} else {
			if (days > MAX_STAT_DAY) {
				days = MAX_STAT_DAY;
			}
			if (days < 0) {
				days = 7;
			}
		}

		String cacheKey = "stat.requestCountByDay_" + days;
		return QueryUtils.findWithCache(CacheConfig.MG_TIME_TO_LIVE_MINUTE_5, cacheKey, Const.Db.TIO_SITE_STAT, "stat.requestCountByDay", true, days);

	}

	/**
	 *  -- 按天统计不同的ip数
		---------------------------
		--    d         |  count
		-- 2018-04-16	|  35
		-- 2018-04-17	|  222
		---------------------------
	 * @param days
	 * @return
	 */
	public List<Record> ipCountByDay(Integer days) {
		if (days == null) {
			days = 7;
		} else {
			if (days > MAX_STAT_DAY) {
				days = MAX_STAT_DAY;
			}
			if (days < 0) {
				days = 7;
			}
		}

		String cacheKey = "stat.ipCountByDay_" + days;
		return QueryUtils.findWithCache(CacheConfig.MG_TIME_TO_LIVE_MINUTE_5, cacheKey, Const.Db.TIO_SITE_STAT, "stat.ipCountByDay", true, days);

		//		ICache cache = Caches.getCache(CacheConfig.MG_TIME_TO_LIVE_MINUTE_5);
		//		Object obj = cache.get(cacheKey);
		//		if (obj != null) {
		//			return (List<Record>) obj;
		//		}
		//
		//		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_STAT).getSqlPara("stat.ipCountByDay");
		//		sqlPara.addPara(days);
		//		List<Record> list = Db.use(Const.Db.TIO_SITE_STAT).find(sqlPara);
		//
		//		if (list == null) {
		//			list = new ArrayList<>(0);
		//		}
		//
		//		cache.put(cacheKey, (Serializable) list);
		//		return list;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	/**
	 * -- 按省统计点击量
		---------------------------------------------
		--  hitcount | ipcount      |  province
		--  6565     |   545	    |  浙江省
		--  5443     |   434	    |  湖南省
		---------------------------------------------
	 * @param days
	 * @return
	 */
	public List<Record> statIpAndHitsByProvince(Integer days) {
		if (days == null) {
			days = 7;
		} else {
			if (days > MAX_STAT_DAY) {
				days = MAX_STAT_DAY;
			}
			if (days < 0) {
				days = 7;
			}
		}

		String cacheKey = "stat.statIpAndHitsByProvince_" + days;
		return QueryUtils.findWithCache(CacheConfig.MG_TIME_TO_LIVE_MINUTE_5, cacheKey, Const.Db.TIO_SITE_STAT, "stat.statIpAndHitsByProvince", true, days);
	}

	/**
	 * 
	 * @param mergeRequest true:合并请求类型，即appType不分组，只按ip分组
	 * @param starttime
	 * @param endtime
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 * @author tanyaowu
	 */
	public Page<Record> ip(Boolean mergeRequest, String starttime, String endtime, Integer pageNumber, Integer pageSize) {
		Kv kv = Kv.by("starttime", starttime).set("endtime", endtime).set("mergeRequest", mergeRequest);
		Page<Record> page = Db.use(Const.Db.TIO_SITE_STAT).template("stat.ip", kv).paginate(pageNumber, pageSize);
		return page;
	}

}
