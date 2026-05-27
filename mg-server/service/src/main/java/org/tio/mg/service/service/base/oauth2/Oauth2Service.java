
/**
 * 
 */
package org.tio.mg.service.service.base.oauth2;

import org.tio.mg.service.cache.CacheConfig;
import org.tio.mg.service.cache.Caches;
import org.tio.mg.service.model.main.Oauth2Client;
import org.tio.utils.cache.CacheUtils;
import org.tio.utils.cache.FirsthandCreater;
import org.tio.utils.cache.ICache;

import cn.hutool.core.util.StrUtil;

/**
 * @author tanyaowu
 *
 */
public class Oauth2Service {

	public static final Oauth2Service me = new Oauth2Service();

	/**
	 * 
	 */
	private Oauth2Service() {
	}

	/**
	 * 
	 * @param clientid
	 * @return
	 */
	public Oauth2Client getByClientid(String clientid) {
		if (StrUtil.isBlank(clientid)) {
			return null;
		}

		ICache cache = Caches.getCache(CacheConfig.MG_CLIENTID_OAUTH2CLIENT);
		String key = clientid;

		Oauth2Client oauth2Client = CacheUtils.get(cache, key, true, new FirsthandCreater<Oauth2Client>() {
			@Override
			public Oauth2Client create() {
				return Oauth2Client.dao.findFirst("select * from oauth2_client where client_id=?", clientid);
			}
		});

		return oauth2Client;
	}

}
