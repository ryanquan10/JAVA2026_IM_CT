
/**
 * 
 */
package org.tio.mg.view.topic;

import org.redisson.api.listener.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.mg.service.cache.CacheConfig;
import org.tio.mg.service.cache.Caches;
import org.tio.mg.view.http.WebViewHttpServerInterceptor;
import org.tio.mg.view.http.WebViewInit;
import org.tio.sitexxx.service.vo.topic.CleanViewCacheVo;
import org.tio.utils.cache.ICache;

/**
 * @author tanyaowu
 *
 */
public class TopicCleanViewCacheListener implements MessageListener<CleanViewCacheVo> {

	private static Logger log = LoggerFactory.getLogger(TopicCleanViewCacheListener.class);

	public static final TopicCleanViewCacheListener me = new TopicCleanViewCacheListener();

	/**
	 * 
	 */
	private TopicCleanViewCacheListener() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

	@Override
	public void onMessage(CharSequence channel, CleanViewCacheVo cleanViewCacheVo) {
		try {
			ICache cache = Caches.getCache(CacheConfig.MG_VIEW_HTML);
			cache.clear();

			WebViewHttpServerInterceptor.clearCache();
		} catch (Exception e) {
			log.error(e.toString(), e);
		}

		WebViewInit.requestHandler.clearStaticResCache();
	}

}
