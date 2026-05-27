
package org.tio.sitexxx.service.service.conf;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.model.conf.EmailServer;
import org.tio.utils.cache.CacheUtils;
import org.tio.utils.cache.FirsthandCreater;
import org.tio.utils.cache.ICache;

/**
 * 邮箱服务器配置
 * @author tanyaowu
 *
 */
public class EmailServerService {

	private static Logger log = LoggerFactory.getLogger(EmailServerService.class);

	public static final EmailServerService me = new EmailServerService();

	final static EmailServer dao = new EmailServer().dao();

	private static int index = 0;

	/**
	 * 获取所有邮箱服务器
	 * @return
	 */
	public List<EmailServer> getAll() {
		ICache cache = Caches.getCache(CacheConfig.TIME_TO_LIVE_MINUTE_5_LOCAL);
		String key = "EmailServerList";
		ArrayList<EmailServer> list = CacheUtils.get(cache, key, true, new FirsthandCreater<ArrayList<EmailServer>>() {
			@Override
			public ArrayList<EmailServer> create() {
				ArrayList<EmailServer> list = (ArrayList<EmailServer>) dao.find("select * from email_server where status = 1");
				return list;
			}
		});
		return list;
	}

	/**
	 * 获取一个EmailServer
	 * @return
	 */
	public EmailServer next() {
		List<EmailServer> list = getAll();
		if (list == null || list.size() == 0) {
			log.error("没有配置邮箱服务器");
			return null;
		}

		int i = index++;
		if (i >= list.size()) {
			index = 0;
			i = index++;
		}

		return list.get(i);
	}

}
