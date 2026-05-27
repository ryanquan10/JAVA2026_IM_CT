
package org.tio.sitexxx.service.service.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.model.main.UserThirdOsc;
import org.tio.utils.cache.CacheUtils;
import org.tio.utils.cache.FirsthandCreater;
import org.tio.utils.cache.ICache;

/**
 * @author tanyaowu
 * 2016年8月10日 上午11:09:59
 */
public class UserThirdOscService {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(UserThirdOscService.class);

	public static final UserThirdOscService me = new UserThirdOscService();

	ICache userthirdidCache;

	/**
	 *
	 * @author tanyaowu
	 */
	private UserThirdOscService() {
		userthirdidCache = Caches.getCache(CacheConfig.UID_USERTHIRD);
	}

	/**
	 * 
	 * @param user_third_id
	 * @return
	 */
	public UserThirdOsc getByUserThirdId(Integer user_third_id) {
		if (user_third_id == null) {
			return null;
		}

		String key = "osc_" + user_third_id;
		boolean putTempToCacheIfNull = false;
		UserThirdOsc userThirdOsc = CacheUtils.get(userthirdidCache, key, putTempToCacheIfNull, new FirsthandCreater<UserThirdOsc>() {
			@Override
			public UserThirdOsc create() {
				UserThirdOsc userThirdOsc = UserThirdOsc.dao.findFirst("select * from user_third_osc where user_third_id=?", user_third_id);
				return userThirdOsc;
			}
		});
		return userThirdOsc;

	}
}
