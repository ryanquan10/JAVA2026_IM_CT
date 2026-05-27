
package org.tio.sitexxx.service.service.chat;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.model.main.WxFriendApplyItems;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.utils.cache.CacheUtils;
import org.tio.utils.cache.FirsthandCreater;
import org.tio.utils.cache.ICache;

/**
 * 申请记录处理
 * @author lixinji
 * 2020年1月8日 下午5:01:10
 */
public class FriendApplyService {
	private static Logger					log	= LoggerFactory.getLogger(FriendApplyService.class);
	public static final FriendApplyService	me	= new FriendApplyService();

	/**
	 * 申请记录-已调整
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2020年2月4日 下午4:29:54
	 */
	public Ret applyList(Integer uid) {
		if (uid == null) {
			log.error("获取申请列表：用户id为空");
			return RetUtils.invalidParam();
		}
		Kv params = Kv.by("uid", uid);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("friendapply.applylist", params);
		List<Record> records = Db.use(Const.Db.TIO_SITE_MAIN).find(sqlPara);
		return RetUtils.okList(records);
	}

	/**
	 * 正在申请的条数-已调整
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2020年2月4日 下午4:36:47
	 */
	public Ret applyData(Integer uid) {
		if (uid == null) {
			log.error("获取申请列表：用户id为空");
			return RetUtils.invalidParam();
		}
		int count = Db.use(Const.Db.TIO_SITE_MAIN).queryInt("SELECT count(1) num FROM wx_friend_apply_items where touid = ? and `status` = ?", uid, Const.ApplyStatus.APPLY);
		return RetUtils.okData(count);
	}

	/**
	 * 
	 * TODO-lixinji-存在泄露风险-未进行用户判断-已调整
	 * @param applyid
	 * @return
	 * @author lixinji
	 * 2020年2月25日 下午9:21:55
	 */
	public Ret applyInfo(Integer applyid) {
		if (applyid == null) {
			log.error("获取申请信息：无效id");
			return RetUtils.invalidParam();
		}
		ICache cache = Caches.getCache(CacheConfig.WX_FRIEND_APPLY_INFO_1);
		String key = applyid + "";
		Record record = CacheUtils.get(cache, key, true, new FirsthandCreater<Record>() {
			@Override
			public Record create() {
				Kv params = Kv.by("applyid", applyid);
				SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("friendapply.applyInfo", params);
				Record record = Db.use(Const.Db.TIO_SITE_MAIN).findFirst(sqlPara);
				return record;
			}
		});
		return RetUtils.okData(record);
	}

	/*********************************************end-调整-*********************************************************/

	/**
	 * 清空申请换缓存-已调整
	 * @param applyid
	 * @author lixinji
	 * 2020年2月25日 下午9:26:42
	 */
	public void clearApplyCache(Integer applyid) {
		Caches.getCache(CacheConfig.WX_FRIEND_APPLY_INFO_1).remove(applyid + "");
	}

	/**
	 * 初始化申请记录-已调整
	 * @param items
	 * @return
	 * @author lixinji
	 * 2020年1月8日 下午5:16:12
	 */
	public int applyInit(Integer uid, Integer touid, String greet, Short autoFlag, Short status) {
		WxFriendApplyItems applyItems = new WxFriendApplyItems();
		applyItems.setFromuid(uid);
		applyItems.setTouid(touid);
		applyItems.setGreet(greet);
		applyItems.setAutoflag(autoFlag);
		applyItems.setStatus(status);
		applyItems.setReplytime(new Date());
		return applyItems.replaceSave();
	}

	/**
	 * 申请初始化-已调整
	 * @param uid
	 * @param touid
	 * @param greet
	 * @return
	 * @author lixinji
	 * 2020年1月9日 下午7:01:09
	 */
	public int applyInit(Integer uid, Integer touid, String greet, WxFriendApplyItems applyItems) {
		if (applyItems == null) {
			applyItems = new WxFriendApplyItems();
		}
		applyItems.setFromuid(uid);
		applyItems.setTouid(touid);
		applyItems.setGreet(greet);
		applyItems.setAutoflag(Const.YesOrNo.NO);
		applyItems.setStatus(Const.ApplyStatus.APPLY);
		applyItems.setReplytime(new Date());
		return applyItems.replaceSave();
	}

	/**
	 *  申请初始化-已调整
	 * @param uid
	 * @param touid
	 * @param greet
	 * @return
	 * @author lixinji
	 * 2020年1月10日 上午10:07:03
	 */
	public int applyInit(Integer uid, Integer touid, String greet) {
		return applyInit(uid, touid, greet, null);
	}

	/**
	 * 修改申请记录-已调整
	 * @param applyid
	 * @param status
	 * @return
	 * @author lixinji
	 * 2020年1月9日 下午7:04:25
	 */
	public boolean update(Integer applyid, Short status) {
		WxFriendApplyItems update = new WxFriendApplyItems();
		update.setId(applyid);
		update.setStatus(status);
		boolean updateFlag = update.update();
		if (updateFlag) {
			clearApplyCache(applyid);
		}
		return updateFlag;
	}

	/**
	 * 获取申请记录-已调整
	 * 未加缓存-TODO:lixinji
	 * @param id
	 * @return
	 * @author lixinji
	 * 2020年1月8日 下午5:56:58
	 */
	public WxFriendApplyItems getById(Integer id) {
		return WxFriendApplyItems.dao.findById(id);
	}

	/**
	 * 清除历史记录-已调整
	 * @param items
	 * @return
	 * @author lixinji
	 * 2020年1月8日 下午5:26:39
	 */
	public Ret removeApply(WxFriendApplyItems items) {
		WxFriendApplyItems old = getApply(items.getFromuid(), items.getTouid());
		if (old != null) {
			WxFriendApplyItems.dao.deleteById(old.getId());
			clearApplyCache(old.getId());
		}
		return RetUtils.okOper();
	}

	/**
	 * 清空历史-已调整
	 * @param uid
	 * @param touid
	 * @return
	 * @author lixinji
	 * 2020年1月13日 上午10:35:05
	 */
	public boolean removeApply(Integer uid, Integer touid) {
		WxFriendApplyItems old = getApply(uid, touid);
		if (old != null) {
			WxFriendApplyItems.dao.deleteById(old.getId());
			clearApplyCache(old.getId());
		}
		return true;
	}

	/**
	 * 申请记录-已调整
	 * 无缓存-TODO:lixinji
	 * @param uid
	 * @param touid
	 * @return
	 * @author lixinji
	 * 2020年1月10日 上午10:44:18
	 */
	public WxFriendApplyItems getApply(Integer uid, Integer touid) {
		Kv params = Kv.by("fromuid", uid).set("touid", touid);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("friendapply.findApply", params);
		WxFriendApplyItems old = WxFriendApplyItems.dao.findFirst(sqlPara);
		return old;
	}

}
