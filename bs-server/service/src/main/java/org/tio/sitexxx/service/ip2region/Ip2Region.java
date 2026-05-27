
package org.tio.sitexxx.service.ip2region;

import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.DbSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tanyaowu 
 * 2016年10月19日 下午4:58:50
 */
public class Ip2Region {
	private static Logger log = LoggerFactory.getLogger(Ip2Region.class);

	public static DbSearcher dbSearcher = null;

	/**
	 * 
	 * @param ip
	 * @return
	 * @author: tanyaowu
	 */
	public static DataBlock getDataBlock(String ip) {
		DataBlock dataBlock = null;
		try {
			dataBlock = Ip2Region.dbSearcher.memorySearch(ip);
		} catch (Throwable e) {
			log.error(ip, e);
		} finally {
			if (dataBlock == null) {
				dataBlock = new DataBlock(0, "0|0|0|0|0", 0);
			}
		}
		return dataBlock;
	}
}
