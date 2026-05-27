
package org.tio.mg.service.service.base;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.mg.service.model.stat.TioTokenPathAccessStat;
import org.tio.sitexxx.service.vo.Const;

/**
 * 
 * @author tanyaowu
 *
 */
public class TioTokenPathAccessStatService {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(TioTokenPathAccessStatService.class);

	public static final TioTokenPathAccessStatService ME = new TioTokenPathAccessStatService();

	/**
	 * 
	 * @author tanyaowu
	 */
	public TioTokenPathAccessStatService() {
	}

	/**
	 * 
	 * @param tioIpPathAccessStat
	 * @return
	 * @author tanyaowu
	 */
	public boolean save(TioTokenPathAccessStat tioTokenPathAccessStat) {
		if (tioTokenPathAccessStat != null) {
			return tioTokenPathAccessStat.save();
		}
		return false;
	}

	public int[] batchSave(List<TioTokenPathAccessStat> modelList) {
		return Db.use(Const.Db.TIO_SITE_STAT).batchSave(modelList, 100);
	}
}
