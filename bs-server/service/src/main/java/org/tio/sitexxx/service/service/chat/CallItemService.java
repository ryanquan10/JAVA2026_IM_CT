
package org.tio.sitexxx.service.service.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.model.main.WxCallItem;
import org.tio.utils.cache.CacheUtils;
import org.tio.utils.cache.FirsthandCreater;

/**
 * 
 * @author tanyaowu 
 * 2020年2月19日 下午4:26:09
 */
public class CallItemService {
	@SuppressWarnings("unused")
	private static Logger				log	= LoggerFactory.getLogger(CallItemService.class);
	public static final CallItemService	me	= new CallItemService();

	/**
	 * 
	 * @param id
	 * @return
	 * @author tanyaowu
	 */
	public WxCallItem getById(Long id) {
		if (id == null) {
			return null;
		}
		String key = id + "";

		WxCallItem ret = CacheUtils.get(Caches.getCache(CacheConfig.WXCALLITEM_1), key, true, new FirsthandCreater<WxCallItem>() {
			@Override
			public WxCallItem create() {
				WxCallItem wxCallItem = WxCallItem.dao.findById(id);
				return wxCallItem;
			}
		});
		return ret;
	}

	/**
	 * 清除缓存
	 * @param id
	 * @author lixinji
	 * 2020年9月9日 上午10:37:46
	 */
	public void clearWxCallItem(Long id) {
		if (id == null) {
			return;
		}
		String key = id + "";
		Caches.getCache(CacheConfig.WXCALLITEM_1).remove(key);
	}

	private static final String sql_getCanceledItem = "select * from wx_call_item where fromuid = ? and status != 4 ";

	/**
	 * 获取可以取消的通话记录（只有一种情况可以取消：对方还没有作任何响应的时候，发起发方可以取消）
	 * @param fromuid
	 * @return
	 * @author tanyaowu
	 */
	public WxCallItem getCanceledItem(Integer fromuid) {
		if (fromuid == null) {
			return null;
		}

		WxCallItem wxCallItem = WxCallItem.dao.findFirst(sql_getCanceledItem, fromuid);
		return wxCallItem;
	}

}
