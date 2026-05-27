
package org.tio.mg.service.service.tioim;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Page;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.mg.service.model.main.WxChatItems;
import org.tio.mg.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.utils.hutool.StrUtil;
import org.tio.mg.service.vo.MgConst;

/**
 * 会话管理
 * @author xufei
 * 2020年5月16日 下午2:21:06
 */
public class TioChatService {
	
	private static Logger			log	= LoggerFactory.getLogger(TioChatService.class);
	
	public static final TioChatService	me	= new TioChatService();

	/**
	 * 会话列表
	 * @param uid
	 * @return
	 * @author xufei
	 * 2020年7月29日 下午1:45:14
	 */
	public Ret chatItemList(Integer uid,Integer pageNumber, Integer pageSize) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageNumber <= 0) {
			pageSize = 16;
		}
		if(uid == null) {
			log.error("获取聊天列表：无效用户");
			return Ret.fail("msg","无效参数");
		}
		Kv params = Kv.by("uid", uid);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chat.list", params);
		Page<Record> pages = Db.use(Const.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(pages);
	}
	
	
	/**
	 * 截屏列表
	 * @param pageNumber
	 * @param pageSize
	 * @param searchkey
	 * @param chatmode
	 * @return
	 * @author xufei
	 * 2021年3月2日 上午11:18:28
	 */
	public Ret screenlist(Integer pageNumber, Integer pageSize,String searchkey,Short chatmode) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageNumber <= 0) {
			pageSize = 16;
		}
		Kv params = Kv.create();
		if(StrUtil.isNotBlank(searchkey)) {
			params.set("searchkey", "%" + searchkey + "%");
			params.set("searchid", searchkey);
		}
		if(chatmode != null) {
			params.set("chatmode", chatmode);
		}
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chat.screenlist", params);
		Page<Record> pages = Db.use(Const.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(pages);
	}
	
	
	/**
	 * @param chatlinkid
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 * @author xufei
	 * 2021年3月2日 上午11:18:32
	 */
	public Ret chatMsgList(Long chatlinkid,Integer pageNumber, Integer pageSize) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageNumber <= 0) {
			pageSize = 16;
		}
		WxChatItems chatItems = WxChatItems.dao.findById(chatlinkid);
		if(chatItems == null) {
			log.error("获取聊天列表：无效会话");
			return RetUtils.noExistParam();
		}
		Kv params = Kv.create();;
		String sqlParaName = "";
		if(Objects.equals(chatItems.getChatmode(), Const.ChatMode.P2P)) {
			sqlParaName = "p2pMsg";
			params.set("key",chatItems.getFidkey());
		} else {
			sqlParaName = "groupMsg";
			params.set("key",chatItems.getBizid());
		}
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chat." + sqlParaName, params);
		Page<Record> pages = Db.use(Const.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(pages);
	}
	
	
	/**
	 * 群列表
	 * @param pageNumber
	 * @param pageSize
	 * @param uid
	 * @return
	 * @author xufei
	 * 2020年7月29日 下午2:24:58
	 */
	public Ret groupList(Integer pageNumber, Integer pageSize,Integer uid) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageNumber <= 0) {
			pageSize = 16;
		}
		Kv params = Kv.by("uid", uid);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chat.groupList", params);
		Page<Record> pages = Db.use(Const.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(pages);
	}

	/**
	 * @param pageNumber
	 * @param pageSize
	 * @param groupid
	 * @return
	 * @author xufei
	 * 2020年7月29日 下午2:26:47
	 */
	public Ret groupMsgList(Integer pageNumber, Integer pageSize,Long groupid) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageNumber <= 0) {
			pageSize = 16;
		}
		Kv params = Kv.by("key", groupid);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chat.groupMsg", params);
		Page<Record> pages = Db.use(Const.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(pages);
	}
	
	/**
	 * @param groupid
	 * @return
	 * @author xufei
	 * 2020年7月29日 下午2:35:57
	 */
	public Ret groupInfo(Long groupid) {
		Record group = Db.use(MgConst.Db.TIO_SITE_MAIN).findFirst("select g.*,m.joinnum,u.avatar,u.loginname,u.nick from wx_group g INNER JOIN wx_group_meta m on m.groupid = g.id INNER JOIN `user` u on u.id = g.uid where g.id = ?", groupid);
		return RetUtils.okData(group);
	}
	
	/**
	 * @param groupid
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 * @author xufei
	 * 2020年7月29日 下午2:37:38
	 */
	public Ret groupUserList(Long groupid,Integer pageNumber, Integer pageSize) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageNumber <= 0) {
			pageSize = 16;
		}
		Kv params = Kv.by("groupid", groupid);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chat.groupUserlist", params);
		Page<Record> records = Db.use(Const.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(records);
		
	}
	
	/**
	 * 好友列表
	 * @param pageNumber
	 * @param pageSize
	 * @param uid
	 * @return
	 * @author xufei
	 * 2020年7月29日 下午2:25:30
	 */
	public Ret friendList(Integer pageNumber, Integer pageSize,Integer uid) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageNumber <= 0) {
			pageSize = 16;
		}
		Kv params = Kv.by("uid", uid);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chat.friendList", params);
		Page<Record> pages = Db.use(Const.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(pages);
	}
	
	/**
	 * @param pageNumber
	 * @param pageSize
	 * @param uid
	 * @return
	 * @author xufei
	 * 2020年7月30日 上午10:02:10
	 */
	public Ret applyList(Integer pageNumber, Integer pageSize,Integer uid) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageNumber <= 0) {
			pageSize = 16;
		}
		Kv params = Kv.by("uid", uid);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chat.applylist", params);
		Page<Record> pages = Db.use(Const.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(pages);
	}
	
	/**
	 * @param pageNumber
	 * @param pageSize
	 * @param fidkey
	 * @return
	 * @author xufei
	 * 2020年7月29日 下午2:27:56
	 */
	public Ret friendMsgList(Integer pageNumber,Integer pageSize,Integer uid,Integer touid) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageNumber <= 0) {
			pageSize = 16;
		}
		String fidkey = "";
		if(uid.intValue() > touid.intValue()) {
			fidkey = uid  + "_" + touid;
		} else {
			fidkey = touid  + "_" + uid;
		}
		Kv params = Kv.by("key", fidkey);
		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chat.p2pMsg", params);
		Page<Record> pages = Db.use(Const.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(pages);
	}
}
