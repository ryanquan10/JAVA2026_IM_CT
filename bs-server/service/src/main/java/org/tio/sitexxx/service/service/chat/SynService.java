
package org.tio.sitexxx.service.service.chat;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.WxSynItem;
import org.tio.sitexxx.service.service.atom.AbsTxAtom;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.Devicetype;
import org.tio.sitexxx.service.vo.wx.FocusVo;
import org.tio.utils.lock.LockUtils;

/**
 * 同步服务
 * @author lixinji
 * 2020年8月25日 下午5:57:32
 */
public class SynService {
	private static Logger			log	= LoggerFactory.getLogger(SynService.class);
	public static final SynService	me	= new SynService();

	final WxSynItem synItemDao = new WxSynItem().dao();

	/**
	 * 同步会话
	 * @param user
	 * @param syntime
	 * @return
	 * @author lixinji
	 * 2020年8月26日 下午1:43:53
	 */
	public Ret chat(User user, Short devicetype, Date syntime) {
		if (user == null) {
			log.error("同步会话用户为空");
			return RetUtils.invalidParam();
		}
		Integer uid = user.getId();
		WxSynItem synItem = null;
		boolean isAll = true;
		if (syntime != null) {
			isAll = false;
			//select * from wx_syn_item where uid = ? and devicetype = ? and syntype = ?", uid, devicetype, syntype
			synItem = getSynItem(devicetype, uid, Const.WxSynType.CHAT);
			if (synItem == null) {
				WxSynItem client = insertSynTime(devicetype, uid, Const.WxSynType.CLIENT_SYN, null, true);
				synItem = client;
			}
		} else {
			synItem = getSynItem(devicetype, uid, Const.WxSynType.CHAT);
			if (synItem != null && synItem.getSyntime() != null) {
				isAll = false;
			} else {
				WxSynItem createItem = getSynItem(devicetype, uid, Const.WxSynType.LINK_CREATE);
				if (createItem != null && createItem.getSyntime() != null) {
					isAll = false;
					synItem = createItem;
				}
			}
		}
		Map<String, Object> retmap = new HashMap<String, Object>();
		if (isAll) {
			retmap.put("all", Const.YesOrNo.YES);
			Kv params = Kv.by("uid", user.getId()).set("limit", 5000);
			SqlPara sqlPara = User.dao.getSqlPara("chat.list", params);
			List<Record> records = Db.use(Const.Db.TIO_SITE_MAIN).find(sqlPara);
			retmap.put("chatlist", records);
			if (synItem == null) {
				WxSynItem all = insertSynTime(devicetype, uid, Const.WxSynType.CHAT, null, true);
				retmap.put("synitem", all);
			} else {
				retmap.put("synitem", synItem);
			}
			return RetUtils.okData(retmap);
		} else {
			retmap.put("all", Const.YesOrNo.NO);
			if (synItem != null) {
				retmap.put("synitem", synItem);
			}
			if (syntime != null) {
				retmap.put("clienttime", syntime);
			} else {
				syntime = synItem.getSyntime();
			}
			Kv params = Kv.by("uid", user.getId()).set("syntime", syntime);
			SqlPara sqlPara = User.dao.getSqlPara("syn.synchatlist", params);
			List<Record> records = Db.use(Const.Db.TIO_SITE_MAIN).find(sqlPara);
			retmap.put("chatlist", records);
			Kv delParams = Kv.by("uid", user.getId()).set("syntime", syntime);
			SqlPara delSqlPara = User.dao.getSqlPara("syn.syndelchatlist", delParams);
			List<Record> delList = Db.use(Const.Db.TIO_SITE_MAIN).find(delSqlPara);
			//			Kv hideparams = Kv.by("uid", user.getId()).set("syntime",syntime);
			//			SqlPara hidesqlPara = User.dao.getSqlPara("syn.synchatlist", hideparams);
			//			List<Record> hideRecords = Db.use(Const.Db.TIO_SITE_MAIN).find(hidesqlPara);
			//			delList.addAll(hideRecords);
			retmap.put("dellist", delList);
			return RetUtils.okData(retmap);
		}
	}

	/**
	 * ack
	 * @param user
	 * @param synid
	 * @return
	 * @author lixinji
	 * 2020年8月27日 下午2:54:09
	 */
	public Ret ack(User user, Integer synid) {
		if (user == null || synid == null) {
			log.error("同步会话用户为空");
			return RetUtils.invalidParam();
		}
		WxSynItem wxSynItem = synItemDao.findById(synid);
		if (wxSynItem == null) {
			log.error("同步记录已清除，uid:{},synid:{}", user.getId(), synid);
			return RetUtils.okMsg("已清除同步记录");
		}
		AbsTxAtom atom = new AbsTxAtom() {

			@Override
			public boolean noTxRun() {
				SynService.me.insertSynTime(wxSynItem.getDevicetype(), user.getId(), Const.WxSynType.LINK_CREATE, new Date(), true);
				synItemDao.deleteById(synid);
				return true;
			}
		};
		boolean tx = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
		if (!tx) {
			return RetUtils.failMsg("ack失败");
		}
		return RetUtils.okOper();
	}

	/**
	 * 同步焦点
	 * @param user
	 * @return
	 * @author lixinji
	 * 2020年8月27日 下午1:46:27
	 */
	public Map<String, Short> focus(Integer uid) {
		if (uid == null) {
			log.error("同步焦点用户为空");
			return null;
		}
		Map<String, Short> focusMap = new HashMap<String, Short>();
		HashMap<String, Long> pc = ChatMsgService.me.getFocusDeviceMap(uid, Devicetype.WEB.getValue());
		if (pc != null) {
			for (String key : pc.keySet()) {
				Long existChatLinkid = pc.get(key);
				focusMap.put(existChatLinkid + "", Const.YesOrNo.YES);
			}
		}
		FocusVo app = ChatMsgService.me.getFocus(uid, Devicetype.APP.getValue(), "");
		if (app != null) {
			focusMap.put(app.getRchatlinkid() + "", Const.YesOrNo.YES);
		}
		HashMap<String, Long> h5 = ChatMsgService.me.getFocusDeviceMap(uid, Devicetype.H5.getValue());
		if (h5 != null) {
			for (String key : h5.keySet()) {
				Long existChatLinkid = h5.get(key);
				focusMap.put(existChatLinkid + "", Const.YesOrNo.YES);
			}
		}
		return focusMap;
	}

	/**
	 * @param uid
	 * @param devicetype
	 * @return
	 * @author lixinji
	 * 2020年9月29日 下午11:33:10
	 */
	public Map<String, Short> focusDevice(Integer uid, Short devicetype) {
		if (uid == null || devicetype == null) {
			log.error("同步焦点用户或者设备为空");
			return null;
		}
		if (Objects.equals(devicetype, Devicetype.IOS.getValue()) || Objects.equals(devicetype, Devicetype.ANDROID.getValue())) {
			devicetype = Devicetype.APP.getValue();
		}
		Map<String, Short> focusMap = new HashMap<String, Short>();
		if (Objects.equals(devicetype, Devicetype.WEB.getValue())) {
			HashMap<String, Long> pc = ChatMsgService.me.getFocusDeviceMap(uid, Devicetype.WEB.getValue());
			if (pc != null) {
				for (String key : pc.keySet()) {
					Long existChatLinkid = pc.get(key);
					focusMap.put(existChatLinkid + "", Const.YesOrNo.YES);
				}
			}
		}
		if (Objects.equals(devicetype, Devicetype.APP.getValue())) {
			FocusVo app = ChatMsgService.me.getFocus(uid, Devicetype.APP.getValue(), "");
			if (app != null) {
				focusMap.put(app.getRchatlinkid() + "", Const.YesOrNo.YES);
			}
		}
		if (Objects.equals(devicetype, Devicetype.H5.getValue())) {
			HashMap<String, Long> h5 = ChatMsgService.me.getFocusDeviceMap(uid, Devicetype.H5.getValue());
			if (h5 != null) {
				for (String key : h5.keySet()) {
					Long existChatLinkid = h5.get(key);
					focusMap.put(existChatLinkid + "", Const.YesOrNo.YES);
				}
			}
		}
		return focusMap;
	}

	//	/**
	//	 * 在线处理
	//	 * @param uid
	//	 * @param devicetype
	//	 * @param ipid
	//	 * @author lixinji
	//	 * 2020年9月14日 下午2:19:57
	//	 */
	//	@SuppressWarnings("unchecked")
	//	public void  online(Integer uid,Short devicetype,Integer ipid) {
	//		short devicekey = devicetype;
	//		if(Objects.equals(devicetype, Devicetype.ANDROID.getValue()) || Objects.equals(devicetype, Devicetype.IOS.getValue()) ) {
	//			devicekey = Devicetype.APP.getValue();
	//		}
	//		String key = uid + "";
	//		HashMap<String, OnlineVo> onlineMap = null;
	//		Serializable object =  Caches.getCache(CacheConfig.USER_ONLINE_STAT_2).get(key);
	//		ReentrantReadWriteLock lock =  LockUtils.getReentrantReadWriteLock(Caches.getCache(CacheConfig.USER_ONLINE_STAT_2).getCacheName() + "_" + key, null);
	//		ReadLock readLock = lock.readLock();
	//		readLock.lock();
	//		try {
	//			if(object != null) {
	//				Caches.getCache(CacheConfig.USER_ONLINE_STAT_2).remove(key);
	//				onlineMap = (HashMap<String, OnlineVo>) object;
	//			} else {
	//				onlineMap = new HashMap<String, OnlineVo>();
	//			}
	//			OnlineVo onlineVo = onlineMap.get(devicekey + "");
	//			if(onlineVo == null) {
	//				onlineVo = new OnlineVo();
	//				onlineVo.setUid(uid);
	//				onlineVo.setDevicetype(devicekey);
	//				onlineVo.setIpid(ipid);
	//				onlineMap.put(devicekey + "", onlineVo);
	//			} else {
	//				onlineVo.count.incrementAndGet();
	//			}
	//			onlineMap.put(devicekey + "", onlineVo);
	//			Caches.getCache(CacheConfig.USER_ONLINE_STAT_2).put(key, onlineMap);
	//		} catch (Throwable e) {
	//			log.error(e.getMessage(), e);
	//		} finally {
	//			readLock.unlock();
	//		}
	//	}

	//	/**
	//	 * 离线
	//	 * @param uid
	//	 * @param devicetype
	//	 * @param ipid
	//	 * @author lixinji
	//	 * 2020年9月14日 下午2:22:28
	//	 */
	//	@SuppressWarnings("unchecked")
	//	public void  outline(Integer uid,Short devicetype) {
	//		short devicekey = devicetype;
	//		if(Objects.equals(devicetype, Devicetype.ANDROID.getValue()) || Objects.equals(devicetype, Devicetype.IOS.getValue()) ) {
	//			devicekey = Devicetype.APP.getValue();
	//		}
	//		String key = uid + "";
	//		HashMap<String, OnlineVo> onlineMap = null;
	//		Serializable object =  Caches.getCache(CacheConfig.USER_ONLINE_STAT_2).get(key);
	//		ReentrantReadWriteLock lock =  LockUtils.getReentrantReadWriteLock(Caches.getCache(CacheConfig.USER_ONLINE_STAT_2).getCacheName() + "_" + key, null);
	//		ReadLock readLock = lock.readLock();
	//		readLock.lock();
	//		try {
	//			if(object != null) {
	//				onlineMap = (HashMap<String, OnlineVo>) object;
	//				if(onlineMap.isEmpty()) {
	//					//空去除在线缓存
	//					Caches.getCache(CacheConfig.USER_ONLINE_STAT_2).remove(key);
	//				} else {
	//					Caches.getCache(CacheConfig.USER_ONLINE_STAT_2).remove(key);
	//					OnlineVo onlineVo = onlineMap.get(devicekey + "");
	//					if(onlineVo != null) {
	//						//有在线缓存
	//						int count = onlineVo.count.decrementAndGet();
	//						if(count <= 0) {
	//							//只有1个链接
	//							onlineMap.remove(devicekey + "");
	//						}
	//					}
	//					if(!onlineMap.isEmpty()) {
	//						Caches.getCache(CacheConfig.USER_ONLINE_STAT_2).put(key, onlineMap);
	//					}
	//				}
	//			} 
	//		} catch (Throwable e) {
	//			log.error(e.getMessage(), e);
	//		} finally {
	//			readLock.unlock();
	//		}
	//	}

	//	/**
	//	 * 获取在线列表
	//	 * @param uid
	//	 * @return
	//	 * @author lixinji
	//	 * 2020年9月14日 下午2:23:51
	//	 */
	//	@SuppressWarnings("unchecked")
	//	public HashMap<String, OnlineVo>  onlineMap(Integer uid) {
	//		String key = uid + "";
	//		HashMap<String, OnlineVo> onlineMap = null;
	//		Serializable object =  Caches.getCache(CacheConfig.USER_ONLINE_STAT_2).get(key);
	//		if(object != null) {
	//			onlineMap = (HashMap<String, OnlineVo>) object;
	//		} 
	//		return onlineMap;
	//	}

	/**
	 * 记录用户被集群影响的缓存会话集合
	 * @param uid
	 * @param chatlinkid
	 * @author lixinji
	 * 2020年9月14日 下午2:37:40
	 */
	@SuppressWarnings("unchecked")
	public void recordCluChatCache(Integer uid, Long chatlinkid) {
		String key = uid + "";
		HashMap<String, Long> chatcache = null;
		Serializable object = Caches.getCache(CacheConfig.WX_USER_CLU_CHATCACHE_1).get(key);
		ReentrantReadWriteLock lock = LockUtils.getReentrantReadWriteLock(Caches.getCache(CacheConfig.WX_USER_CLU_CHATCACHE_1).getCacheName() + "_" + key, null);
		ReadLock readLock = lock.readLock();
		readLock.lock();
		try {
			if (object != null) {
				chatcache = (HashMap<String, Long>) object;
				chatcache.put(chatlinkid + "", chatlinkid);
			} else {
				chatcache = new HashMap<String, Long>();
				chatcache.put(chatlinkid + "", chatlinkid);
				Caches.getCache(CacheConfig.WX_USER_CLU_CHATCACHE_1).put(key, chatcache);
			}
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * 清除因集群影响的会话缓存
	 * @param uid
	 * @author lixinji
	 * 2020年9月14日 下午2:43:26
	 */
	@SuppressWarnings("unchecked")
	public void clearCluChatCache(Integer uid) {
		String key = uid + "";
		HashMap<String, Long> chatcache = null;
		Serializable object = Caches.getCache(CacheConfig.WX_USER_CLU_CHATCACHE_1).get(key);
		ReentrantReadWriteLock lock = LockUtils.getReentrantReadWriteLock(Caches.getCache(CacheConfig.WX_USER_CLU_CHATCACHE_1).getCacheName() + "_" + key, null);
		ReadLock readLock = lock.readLock();
		readLock.lock();
		try {
			if (object != null) {
				Caches.getCache(CacheConfig.WX_USER_CLU_CHATCACHE_1).remove(key);
				chatcache = (HashMap<String, Long>) object;
				for (String chatStr : chatcache.keySet()) {
					Long chatlinkid = chatcache.get(chatStr);
					Caches.getCache(CacheConfig.WX_FRIEND_MSG_CHAT_6).remove(chatlinkid + "");
				}
			}
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * 插入更新时间
	 * @param synType
	 * @param syntime
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2020年8月27日 下午2:26:03
	 */
	public WxSynItem insertSynTime(Short devicetype, Integer uid, Short syntype, Date syntime, boolean isreplace) {
		WxSynItem item = new WxSynItem();
		item.setUid(uid);
		item.setSyntype(syntype);
		item.setSyntime(syntime);
		item.setDevicetype(devicetype);
		if (isreplace) {
			WxSynItem synItem = getSynItem(devicetype, uid, syntype);
			if (synItem == null) {
				item.replaceSave();
			} else {
				item.setId(synItem.getId());
				item.update();
			}
		} else {
			item.ignoreSave();
		}
		return item;
	}

	/**
	 * 获取同步数据
	 * @param devicetype
	 * @param uid
	 * @param syntype
	 * @return
	 * @author lixinji
	 * 2020年8月27日 下午3:06:18
	 */
	public WxSynItem getSynItem(Short devicetype, Integer uid, Short syntype) {
		return synItemDao.findFirst("select * from wx_syn_item where uid = ? and devicetype = ? and syntype = ?", uid, devicetype, syntype);
	}

	/**
	 * 删除同步记录:此处删除一个用户的所有同步信息，后续添加类型请注意该方法的扩展
	 * @param devicetype
	 * @param uid
	 * @param syntype
	 * @author lixinji
	 * 2020年8月27日 下午2:30:11
	 */
	public void delSynTime(Short devicetype, Integer uid) {
		Db.use(Const.Db.TIO_SITE_MAIN).update("delete from wx_syn_item where uid = ? and devicetype = ?", uid, devicetype);
	}
}
