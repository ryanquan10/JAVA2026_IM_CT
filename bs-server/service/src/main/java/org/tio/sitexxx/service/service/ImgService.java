
package org.tio.sitexxx.service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.sitexxx.service.model.main.Img;
import org.tio.sitexxx.service.vo.Const;

/**
 * 
 * @author tanyaowu 
 * 2016年12月6日 下午9:03:32
 */
public class ImgService {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(ImgService.class);

	public static final ImgService me = new ImgService();

	final Img dao = new Img().dao();

	/**
	 * 保存视频
	 * @param video
	 * @return
	 */
	public boolean save(Img img) {
		return img.save();
	}



	/**
	 * 
	 * @param id
	 * @param status
	 * @return
	 */
	public boolean updateStatus(int id, short status) {
		String sql = "update img set status = ? where id = ?";
		Db.use(Const.Db.TIO_SITE_MAIN).update(sql, status, id);
		return true;
	}

}
