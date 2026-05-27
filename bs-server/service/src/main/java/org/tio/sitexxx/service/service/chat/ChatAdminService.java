
package org.tio.sitexxx.service.service.chat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.WxChatGroupItem;
import org.tio.sitexxx.service.model.main.WxChatItems;
import org.tio.sitexxx.service.model.main.WxChatUserItem;
import org.tio.sitexxx.service.model.main.WxFriend;
import org.tio.sitexxx.service.model.main.WxGroup;
import org.tio.sitexxx.service.service.base.UserService;
import org.tio.sitexxx.service.utils.PyUtils;
import org.tio.sitexxx.service.vo.Const;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 聊天服务
 * @author lixinji
 * 2019年12月31日 下午5:57:32
 */
public class ChatAdminService {
	private static Logger					log	= LoggerFactory.getLogger(ChatAdminService.class);
	public static final ChatAdminService	me	= new ChatAdminService();

	final WxChatUserItem userItemDao = new WxChatUserItem().dao();

	final WxChatGroupItem groupItemDao = new WxChatGroupItem().dao();

	final WxChatItems itemsDao = new WxChatItems().dao();

	/**
	 * 处理线上历史数据-超管使用
	 * @author lixinji
	 * 2020年3月10日 上午10:43:11
	 */
	@Deprecated
	public void resetHistory() {
		Const.getBsExecutor().execute(new Runnable() {
			@Override
			public void run() {
				try {
					System.out.println("开始重置好友历史数据");
					long start = System.currentTimeMillis();
					int count = Db.use(Const.Db.TIO_SITE_MAIN).queryInt("select count(1) from wx_friend");
					int index = (int) Math.ceil(new Double(count) / 10000);
					int limit = 0;
					Map<String, Long> dealMap = new HashMap<String, Long>();
					for (int i = 0; i < index; i++) {
						List<WxFriend> friends = WxFriend.dao.find("select * from wx_friend order by id limit " + limit + ",10000");
						for (WxFriend friend : friends) {
							if (dealMap.get(friend.getId() + "") != null) {
								continue;
							}
							String sql = "select id,uid, frienduid, remarkname, createtime from wx_friend where uid = ? and frienduid = ? limit 0, 1";
							WxFriend to = WxFriend.dao.findFirst(sql, friend.getFrienduid(), friend.getUid());
							WxChatUserItem wxChatUserItem = new WxChatUserItem();
							wxChatUserItem.setUid(friend.getUid());
							wxChatUserItem.setChatmode(Const.ChatMode.P2P);
							wxChatUserItem.setBizid(new Long(friend.getFrienduid()));
							wxChatUserItem.setLinkid(friend.getId());
							//							wxChatUserItem.setViewflag(Const.YesOrNo.NO);
							//							wxChatUserItem.setActflag(Const.YesOrNo.NO);
							wxChatUserItem.setFidkey(UserService.twoUid(friend.getUid(), friend.getFrienduid()));
							if (to == null) {
								wxChatUserItem.setLinkflag(Const.YesOrNo.NO);
								wxChatUserItem.replaceSave();
							} else {
								wxChatUserItem.setLinkflag(Const.YesOrNo.YES);
								wxChatUserItem.replaceSave();
								WxChatUserItem toitem = new WxChatUserItem();
								toitem.setUid(friend.getFrienduid());
								toitem.setChatmode(Const.ChatMode.P2P);
								toitem.setBizid(new Long(friend.getUid()));
								toitem.setLinkid(to.getId());
								//								toitem.setViewflag(Const.YesOrNo.NO);
								//								toitem.setActflag(Const.YesOrNo.NO);
								toitem.setFidkey(UserService.twoUid(friend.getUid(), friend.getFrienduid()));
								toitem.setLinkflag(Const.YesOrNo.YES);
								toitem.replaceSave();
								dealMap.put(to.getId() + "", to.getId());
							}
						}
						limit = (i + 1) * 10000;
					}
					Long end = System.currentTimeMillis();
					System.out.println("重置好友历史数据处理时间：" + (end - start) / 1000);
					Caches.getCache(CacheConfig.CHAT_USER_INDEX_2).clear();
					Caches.getCache(CacheConfig.CHAT_ITEMS_6).clear();
				} catch (Exception e) {
					log.error("", e);
				}
			}
		});
	}

	/**
	 * 
	 * @author lixinji 2020年2月21日 上午12:12:15
	 */
	public void resetFdChatIndex() {
		List<Record> records = Db.use(Const.Db.TIO_SITE_MAIN).find("select id,uid,frienduid from wx_friend");
		if (CollectionUtil.isNotEmpty(records)) {
			for (Record record : records) {
				Long id = record.getLong("id");
				WxFriend friend = new WxFriend();
				friend.setId(id);
				String remarkname = record.getStr("remarkname");
				if (StrUtil.isBlank(remarkname)) {
					Integer touid = record.getInt("frienduid");
					User user = UserService.ME.getById(touid);
					friend.setChatindex(PyUtils.getFristChat(user.getNick()));
				} else {
					friend.setChatindex(PyUtils.getFristChat(remarkname));
				}
				try {
					friend.update();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * 超管使用-已调整
	 * @return
	 * @author lixinji
	 * 2020年12月24日 上午12:33:40
	 */
	public List<WxGroup> getAllGroup() {
		return WxGroup.dao.findAll();
	}
}
