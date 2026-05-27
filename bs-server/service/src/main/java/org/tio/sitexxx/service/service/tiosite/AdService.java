
package org.tio.sitexxx.service.service.tiosite;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.sitexxx.service.vo.Const;

/**
 * @author tanyaowu
 * 2016年7月22日 上午10:44:55
 */
public class AdService {
	@SuppressWarnings("unused")
	private static Logger			log	= LoggerFactory.getLogger(AdService.class);
	public static final AdService	me	= new AdService();

	public Record getById(Integer id) {
		//		String sqlid = "ad.all";
		//		String cacheKey = sqlid + id;
		String tableName = "ad";
		Record record = Db.use(Const.Db.TIO_SITE_MAIN).findById(tableName, id);
		return record;
		//		return QueryUtils.findByIdWithCache(CacheConfig.TIME_TO_LIVE_MINUTE_5_LOCAL, cacheKey, Const.Db.TIO_SITE_MAIN, tableName, id, true);
	}

	/**
	 * 首页广告
	 * @return
	 * @author tanyaowu
	 */
	public List<Record> queryHomepage() {
		String sqlid = "ad.homepage";
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara(sqlid);
		List<Record> list1 = Db.use(Const.Db.TIO_SITE_MAIN).find(sqlPara);
		return list1;
	}

	/**
	 * 右侧广告
	 * @return
	 * @author tanyaowu
	 */
	public List<Record> queryRight() {
		String sqlid = "ad.right";
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara(sqlid);
		List<Record> list1 = Db.use(Const.Db.TIO_SITE_MAIN).find(sqlPara);
		return list1;
	}
}
