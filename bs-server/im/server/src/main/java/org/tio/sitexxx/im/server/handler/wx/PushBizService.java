
package org.tio.sitexxx.im.server.handler.wx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.WxChatItems;
import org.tio.sitexxx.service.model.main.WxGroup;
import org.tio.sitexxx.service.model.main.WxGroupMsg;
import org.tio.sitexxx.service.model.main.WxGroupUser;
import org.tio.sitexxx.service.model.main.WxJpushUser;
import org.tio.sitexxx.service.service.base.UserService;
import org.tio.sitexxx.service.service.chat.ChatService;
import org.tio.sitexxx.service.service.chat.GroupService;
import org.tio.sitexxx.service.service.chat.JPushService;
import org.tio.sitexxx.service.vo.Const;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 推送业务服务
 * @author lixinji
 * 2020年09月25日 下午5:57:32
 */
public class PushBizService {
	private static Logger				log	= LoggerFactory.getLogger(PushBizService.class);
	public static final PushBizService	me	= new PushBizService();

	/**
	 * 群消息推送
	 * @param groupid
	 * @param group
	 * @param user
	 * @param msg
	 * @author lixinji
	 * 2020年12月22日 下午5:53:04
	 */
	public void sendGroupMsg(Long groupid, WxGroup group, User user, WxGroupMsg msg) {
		List<Record> pushUser = GroupService.me.groupUserToPush(groupid);
		if (CollectionUtil.isNotEmpty(pushUser)) {
			Map<String, String> extras = new HashMap<String, String>();
			extras.put("chatName", group.getName());
			extras.put("nick", user.getNick());
			extras.put("text", msg.getResume());
			extras.put("chatlinkid", -groupid + "");
			List<String> pushReg = new ArrayList<String>();
			int size = 0;
			for (Record record : pushUser) {
				Integer sendUid = record.getInt("uid");
				if (!checkGroupUser(sendUid, groupid) || Objects.equals(user.getId(), sendUid)) {
					continue;
				}
				pushReg.add(record.getStr("regid"));
				size++;
				if (size == 1000) {
					JPushService.send(JPushService.initPushReg(pushReg, user.getNick() + "：" + msg.getResume(), extras, group.getName()), Const.ChatMode.GROUP);
					pushReg.clear();
				}
			}
			if (CollectionUtil.isNotEmpty(pushReg)) {
				JPushService.send(JPushService.initPushReg(pushReg, user.getNick() + "：" + msg.getResume(), extras, group.getName()), Const.ChatMode.GROUP);
			}
		}
	}

	/**
	 * 好友消息推送
	 * @param touid
	 * @param touserItem
	 * @param user
	 * @param msg
	 * @author lixinji
	 * 2020年12月22日 下午5:58:19
	 */
	public void sendFdMsg(Integer uid, Long chatlinkid, User user, String text) {
		String regid = checkUserToRegid(uid);
		if (StrUtil.isNotBlank(regid)) {
			WxChatItems chatItems = ChatService.me.getBaseChatItems(chatlinkid);
			JPushService.send(JPushService.initPushReg(regid, user.getNick(), text, chatItems.getName(), chatItems.getId(), Const.ChatMode.P2P), Const.ChatMode.P2P);
		}
	}

	/**
	 * 群聊推送check
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2020年9月29日 上午11:19:06
	 */
	public static boolean checkGroupUser(Integer uid, Long groupid) {
		if (uid == null || groupid == null) {
			log.error("群推送监测：传入参数为空");
			return false;
		}
		//		Map<String, OnlineVo> onlineMap = SynService.me.onlineMap(uid);
		//		if(onlineMap != null && onlineMap.get(Devicetype.APP.getValue() + "") != null) {
		//			//在线
		//			log.error("群推送监测：设备在线:{}",Json.toJson(onlineMap.get(Devicetype.APP.getValue() + "")));
		//			return false;
		//		}
		if (WxChatApi.isOnline(uid)) {
			//在线
			//			log.error("群推送监测：设备在线,uid:{},groupid:{}",uid,groupid);
			return false;
		}
		User user = UserService.ME.getById(uid);
		if (user == null || Objects.equals(user.getMsgremindflag(), Const.YesOrNo.NO)) {
			//全局消息不提醒
			//			log.error("群推送监测：全局参数,uid:{},groupid:{},flag:{}",uid,groupid,user.getMsgremindflag());
			return false;
		}
		WxGroupUser wxGroupUser = GroupService.me.getGroupUser(uid, groupid);
		if (wxGroupUser != null && Objects.equals(wxGroupUser.getMsgfreeflag(), Const.YesOrNo.YES)) {
			//群消息免打扰
			//			log.error("群推送监测：群参数,uid:{},groupid:{}",uid,groupid);
			return false;
		}
		return true;
	}

	/**
	 * 检查用户是否推送-私聊
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2020年9月24日 下午2:34:15
	 */
	public static String checkUserToRegid(Integer uid) {
		if (uid == null) {
			log.error("个人推送监测：传入参数为空");
			return "";
		}
		//		Map<String, OnlineVo> onlineMap = SynService.me.onlineMap(uid);
		//		if(onlineMap != null && onlineMap.get(Devicetype.APP.getValue() + "") != null) {
		//			//在线
		//			log.error("个人推送监测：设备在线:{}",Json.toJson(onlineMap.get(Devicetype.APP.getValue() + "")));
		//			return "";
		//		}
		if (WxChatApi.isOnline(uid)) {
			//在线
						log.error("个人推送监测：设备在线,uid:{}",uid);
			return "";
		}
		//如果群聊为空，需要单用户查询，群聊上层已查询
		WxJpushUser jpushUser = WxJpushUser.dao.findFirst("select * from wx_jpush_user where uid = ?", uid);
		if (jpushUser == null) {
			//未绑定设备
						log.error("个人推送监测：未绑定设备,uid:{}",uid);
			return "";
		}
		User user = UserService.ME.getById(uid);
		if (user == null || Objects.equals(user.getMsgremindflag(), Const.YesOrNo.NO)) {
			//全局消息不提醒
						log.error("个人推送监测：全局参数,uid:{}，flag:{}",uid,user.getMsgremindflag());
			return "";
		}
		return jpushUser.getRegid();
	}
}
