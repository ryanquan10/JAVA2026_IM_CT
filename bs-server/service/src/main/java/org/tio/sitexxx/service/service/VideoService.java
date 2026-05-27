
package org.tio.sitexxx.service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.sitexxx.service.model.main.Video;
import org.tio.sitexxx.service.vo.Const;

/**
 * 
 * @author tanyaowu 
 * 2016年12月6日 下午9:03:27
 */
public class VideoService {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(VideoService.class);

	public static final VideoService me = new VideoService();

	final Video dao = new Video().dao();

	/**
	 * 保存视频
	 * @param video
	 * @return
	 */
	public boolean save(Video video) {
		return video.save();
	}
	
	
	
	public boolean updateStatus(int id, short status) {
		String sql = "update video set status = ? where id = ?";
		Db.use(Const.Db.TIO_SITE_MAIN).update(sql, status, id);
		return true;
	}
	
	/**
	 * 重置视频title
	 * @param id
	 * @param title
	 * @return
	 * @author lixinji
	 */
	public boolean reTitle(int id, String  title) {
		String sql = "update video set title = ? where id = ?";
		Db.use(Const.Db.TIO_SITE_MAIN).update(sql, title, id);
		return true;
	}

}
