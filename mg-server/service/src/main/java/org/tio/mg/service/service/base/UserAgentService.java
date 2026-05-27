
package org.tio.mg.service.service.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.mg.service.cache.CacheConfig;
import org.tio.mg.service.cache.Caches;
import org.tio.mg.service.model.main.UserAgent;
import org.tio.utils.cache.CacheUtils;
import org.tio.utils.cache.FirsthandCreater;
import org.tio.utils.cache.ICache;
import org.tio.utils.hutool.StrUtil;

/**
 * @author tanyaowu 
 * 2016年10月25日 下午5:59:49
 */
public class UserAgentService {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(UserAgentService.class);

	public static final UserAgentService ME = new UserAgentService();

	public static final UserAgent BLANK_USERAGENT = UserAgent.from("");

	/**
	 * 
	 * @author tanyaowu
	 */
	public UserAgentService() {
	}

	/**
	 * 会先检查数据库中是不是已经有了一样的记录，如果有，则用原来的记录，省得插入冗余的记录（此处不考虑并发重复数据，允许重复数据）
	 * @param userAgentStr
	 * @return UserAgent 其id字段可能为null（当id为null时，则表示这个userAgentStr是解析不了的）
	 * @author tanyaowu
	 */
	public UserAgent save(String userAgentStr) {
		if (StrUtil.isBlank(userAgentStr)) {
			return BLANK_USERAGENT;
		}
		ICache cache = Caches.getCache(CacheConfig.USER_AGENT);

		UserAgent userAgentInDb = CacheUtils.get(cache, userAgentStr, new FirsthandCreater<UserAgent>() {
			@Override
			public UserAgent create() {
				return UserAgent.dao.findFirst("select * from user_agent where userAgent=? limit 1", userAgentStr);
			}
		});
		if (userAgentInDb != null) {
			return userAgentInDb;
		}

		UserAgent userAgent = UserAgent.from(userAgentStr);
		if (userAgent.isSyntaxError()) {
			cache.putTemporary(userAgentStr, userAgent);
		} else {
			userAgent.save();
			cache.put(userAgentStr, userAgent);
		}
		return userAgent;
	}

}
