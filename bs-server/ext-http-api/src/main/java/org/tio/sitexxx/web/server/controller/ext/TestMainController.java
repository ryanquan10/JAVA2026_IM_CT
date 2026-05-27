
package org.tio.sitexxx.web.server.controller.ext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.lionsoul.ip2region.DataBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.Node;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.annotation.RequestPath;
import org.tio.http.server.mvc.Routes;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.server.ServerChannelContext;
import org.tio.sitexxx.im.common.Command;
import org.tio.sitexxx.im.common.ImPacket;
import org.tio.sitexxx.im.common.ImSessionContext;
import org.tio.sitexxx.im.common.bs.HandshakeReq;
import org.tio.sitexxx.im.common.bs.wx.friend.WxFriendChatReq;
import org.tio.sitexxx.im.common.bs.wx.group.WxGroupChatReq;
import org.tio.sitexxx.im.server.TioSiteImServerStarter;
import org.tio.sitexxx.im.server.handler.wx.friend.WxFriendChatReqHandler;
import org.tio.sitexxx.im.server.handler.wx.group.WxGroupChatReqHandler;
import org.tio.sitexxx.im.server.utils.ImUtils;
import org.tio.sitexxx.service.ip2region.Ip2Region;
import org.tio.sitexxx.service.model.main.IpInfo;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.WxChatGroupItem;
import org.tio.sitexxx.service.model.main.WxChatUserItem;
import org.tio.sitexxx.service.model.main.WxFriendMsg;
import org.tio.sitexxx.service.model.main.WxGroup;
import org.tio.sitexxx.service.model.main.WxGroupMsg;
import org.tio.sitexxx.service.service.atom.RegisterAtom;
import org.tio.sitexxx.service.service.base.IpInfoService;
import org.tio.sitexxx.service.service.base.UserService;
import org.tio.sitexxx.service.service.chat.ChatIndexService;
import org.tio.sitexxx.service.service.chat.ChatMsgService;
import org.tio.sitexxx.service.service.chat.ChatService;
import org.tio.sitexxx.service.service.chat.FriendService;
import org.tio.sitexxx.service.service.chat.GroupService;
import org.tio.sitexxx.service.utils.AvatarUtils;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.Devicetype;
import org.tio.sitexxx.service.vo.MobileInfo;
import org.tio.sitexxx.service.vo.SimpleUser;
import org.tio.sitexxx.web.server.controller.wx.ChatController;
import org.tio.sitexxx.web.server.utils.WebUtils;
import org.tio.utils.json.Json;
import org.tio.utils.resp.Resp;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 
 * @author lixinji
 * 2021年8月25日 上午9:48:21
 */
@RequestPath(value = "/test/jmeter")
public class TestMainController {
	
	private static Logger log = LoggerFactory.getLogger(TestMainController.class);
	
	private static List<User> randoms = null;

	private static final Map<String, Long> groupChatMap = new HashMap<String, Long>();
	
	private static final Map<String, Long> fdChatMap = new HashMap<String, Long>();
	
	/**
	 * 用户手机起始数据：8位数
	 */
	private static AtomicInteger  USER_PHONE_START;
	
	private static final AtomicInteger  index = new AtomicInteger(0);
	
	private static Integer randsize = 0;
	
	private static Long groupid;
	
	public static void main(String[] args) {

	}
	
	/**
	 * 初始化注册用户
	 * @param request
	 * @param phonestart 用户手机起始数据初始化：8位手机号，默认已190+phonestart生成手机号，最高99999999
	 * @param adminuid 初始化用户关联的管理员uid,所有用户都与该用户默认加好友
	 * @param size 初始化用户的数据量
	 * @return 最后一个初始化的用户信息
	 * @throws Exception
	 * @author lixinji
	 * 2022年1月27日 下午6:24:22
	 */
	@RequestPath(value = "/initRegUser")
	public Resp initRegisterUser(HttpRequest request,Integer phonestart,Integer adminuid,Integer size) throws Exception {
		User adminuser = UserService.ME.getById(adminuid);
		if(adminuser == null) {
			return Resp.fail("指定用户不存在");
		}
		if(phonestart == null) {
			USER_PHONE_START = new AtomicInteger(Const.AUTO_USER_INDEX);
		} else {
			USER_PHONE_START = new AtomicInteger(phonestart);
		}
		if(size <= 0 || size > 10000) {
			size = 100;
		}
		if(size > 200) {
			request.channelContext.setHeartbeatTimeout(10 * 60 * 1000L);
		}
		User last = null;
		String error = "";
		int errorcount = 0;
		for(int i = 0; i < size; i++) {
			try {
				Ret ret = registerUser(adminuser);
				if(ret.isFail()) {
					error += RetUtils.getRetMsg(ret);
					errorcount++;
					continue;
				}
				last = RetUtils.getOkTData(ret);
			} catch (Exception e) {
				error = "初始化异常，中断初始化,并且" + error;
				errorcount++;
				continue;
			}
		}
		if(errorcount != 0) {
			log.error("初始化用户失败：{}",error);
			return Resp.fail("失败了：" + errorcount + "个,具体原因见日志");
		}
		log.error("初始化用户成功：{}人",size);
		return Resp.ok(last);
	}
	
	/**
	 * 创建最大size的群
	 * @param request
	 * @param name 群名称
	 * @param size 群人数
	 * @param adminuid
	 * @return 创建的群信息
	 * @throws Exception
	 * @author lixinji
	 * 2022年1月27日 下午6:28:28
	 */
	@RequestPath(value = "/biggroup")
	public Resp biggroup(HttpRequest request,String name,Integer size,String phonepre) throws Exception {
		User curr = WebUtils.currUser(request);
		if(size == null || size <=0) {
			size = 100;
		}
		if(size > 10000) {
			size = 10000;
		} 
		request.channelContext.setHeartbeatTimeout(10 * 60 * 1000L);
		if(StrUtil.isBlank(phonepre)) {
			phonepre = "190";
		}
		WxGroup group = new WxGroup();
		if(StrUtil.isBlank(name)) {
			group.setName("自动创建"+ size +"人群");
		} else {
			group.setName(name);
		}
		String uids = Db.use(Const.Db.TIO_SITE_MAIN).queryStr("select GROUP_CONCAT(limittable.id) uids from (select id from `user` where `status` = 1 and phone like '" + phonepre + "%' and id <> ? limit 0," + size + ") as limittable",curr.getId());
		ChatController chatController = Routes.getController(ChatController.class);
		Resp res = chatController.createGroup(request, group, uids);
		if(res.isOk()) {
			WxGroup retgroup = (WxGroup) res.getData();
			groupid = retgroup.getId();
			log.error("初始化群成功：{}",Json.toJson(group));
		}
		return res;
	}
	
	/**
	 * 
	 * 初始化测试使用的随机用户和群
	 * @param request
	 * @param adminuid 关联的管理员uid
	 * @param groupid 创建群返回的信息中的id
	 * @param size 参与测试的最大随机用户数
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2022年1月27日 下午6:40:53
	 */
	@RequestPath(value = "/initrandom")
	public Resp initrandom(HttpRequest request,Integer adminuid,Long groupid) throws Exception {
		if(adminuid == null || groupid == null) {
			return Resp.fail("adminuid 和 groupid 不能为空");
		}
		request.channelContext.setHeartbeatTimeout(10 * 60 * 1000L);
		initRadom(adminuid, groupid);
		return Resp.ok();
		
	}
	
	
	/**
	 * 获取测试群信息
	 * @param request
	 * @return
	 * @throws Exception
	 * @author lixinji
	 */
	@RequestPath(value = "/groupinfo")
	public Resp groupinfo(HttpRequest request) throws Exception {
		if(groupid == null) {
			return Resp.fail("群未初始化");
		}
		WxGroup group = GroupService.me.getByGroupid(groupid);
		if(group == null) {
			return Resp.fail("群不存在");
		}
		return Resp.ok(group);
		
	}
	
	/**
	 * 随机一个用户
	 * @param request
	 * @param order 是否顺序获取用户： 1：顺序获取用户；2：随机用户；
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2022年1月27日 下午4:13:38
	 */
	@RequestPath(value = "/randomuser")
	public Resp randomuser(HttpRequest request,Short order) throws Exception {
		User romdom = getUser(order);
		romdom.setPassword(romdom.getPwd());
		return Resp.ok(romdom);
		
	}

	/**
	 * 获取某个测试用户的群消息记录列表
	 * @param request
	 * @param groupid 群id
	 * @param order 是否顺序获取用户： 1：顺序获取用户；2：随机用户；
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2022年1月27日 下午4:14:04
	 */
	@RequestPath(value = "/groupmsg")
	public Resp groupmsg(HttpRequest request,Long groupid,Short order) throws Exception {
		User curr = testUser(request, order);
		Long chatlinkid = groupChatMap.get(curr.getId() + "");
		if(chatlinkid == null) {
			return Resp.fail("会话不存在");
		}
		Ret ret = ChatMsgService.me.groupMsgList(chatlinkid, curr.getId(), null, null);
		if (ret.isFail()) {
			return RetUtils.getFailResp(ret);
		}
		List<WxGroupMsg> list = RetUtils.getOkTData(ret);
		return Resp.ok(list);
	}
	
	/**
	 * 获取私聊消息
	 * @param request
	 * @param order 是否顺序获取用户： 1：顺序获取用户；2：随机用户；
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2022年1月27日 下午4:14:08
	 */
	@RequestPath(value = "/fdmsg")
	public Resp fdmsg(HttpRequest request,Short order) throws Exception {
		User curr = testUser(request, order);
		Integer uid = curr.getId();
		Long chatlinkid = fdChatMap.get(uid + "");
		if(chatlinkid == null) {
			return Resp.fail("会话不存在");
		}
		Ret ret = ChatMsgService.me.p2pMsgList(chatlinkid, curr.getId(), null, null);
		if (ret.isFail()) {
			return RetUtils.getFailResp(ret);
		}
		List<WxFriendMsg> data = RetUtils.getOkTData(ret);
		return Resp.ok(data);
	}
	
	/**
	 * 发送私聊
	 * @param request
	 * @param adminuid
	 * @param msg
	 * @param order 是否顺序获取用户： 1：顺序获取用户；2：随机用户；
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2022年1月27日 下午4:14:50
	 */
	@RequestPath(value = "/sendfd")
	public Resp sendfd(HttpRequest request,Integer adminuid,String msg,Short order) throws Exception {
		User curr = testUser(request, order);
		Integer uid = curr.getId();
		Long chatlinkid = fdChatMap.get(uid + "");
		if(chatlinkid == null) {
			return Resp.fail("会话不存在");
		}
		ServerChannelContext channelContext = initImContext(curr);
		WxFriendChatReq  req = new WxFriendChatReq();
		req.setC(msg + "-私聊自增：" + curr.getNick() + "发送的压测消息");
		req.setChatlinkid(chatlinkid);
		req.setTo(adminuid);
		String jsonStr = Json.toJson(req);
		ImPacket imPacket = new ImPacket();
		imPacket.setCommand(Command.WxFriendChatReq);
		imPacket.setBodyStr(jsonStr);
		WxFriendChatReqHandler.me.handler(imPacket, channelContext, false, curr, getSimpleUser(channelContext));
		return Resp.ok();
	}
	
	
	/**
	 * 发送群聊消息
	 * @param request
	 * @param groupid 发送群id
	 * @param msg 消息
	 * @param order 是否顺序获取用户： 1：顺序获取用户；2：随机用户；
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2022年1月27日 下午4:15:06
	 */
	@RequestPath(value = "/sendgroup")
	public Resp sendgroup(HttpRequest request,Long groupid,String msg,Short order) throws Exception {
		User curr = testUser(request, order);
		Long chatlinkid = groupChatMap.get(curr.getId() + "");
		if(chatlinkid == null) {
			return Resp.fail("会话不存在");
		}
		ServerChannelContext channelContext = initImContext(curr);
		WxGroupChatReq wxGroupChatReq = new WxGroupChatReq();
		wxGroupChatReq.setC(msg + "-群聊自增:" + curr.getNick() + "发送的压测消息");
		wxGroupChatReq.setG(groupid);
		wxGroupChatReq.setChatlinkid(-groupid);
		String jsonStr = Json.toJson(wxGroupChatReq);
		ImPacket imPacket = new ImPacket();
		imPacket.setCommand(Command.WxGroupChatReq);
		imPacket.setBodyStr(jsonStr);
		WxGroupChatReqHandler.me.handler(imPacket, channelContext, false, curr, getSimpleUser(channelContext));
		return Resp.ok();
	}
	
	/**
	 * 模拟长链接
	 * @param user
	 * @return
	 * @author lixinji
	 * 2022年1月27日 下午4:15:26
	 */
	public static ServerChannelContext initImContext(User user) {
		//分配token
		String uuid = RandomUtil.randomInt(1, 1000) + TioSiteImServerStarter.tioServerConfigApp.getTioUuid().uuid();
		ServerChannelContext channelContext = new ServerChannelContext(TioSiteImServerStarter.tioServerConfigApp, uuid);
		String ip = org.tio.sitexxx.service.utils.IpUtils.randomIp();
		channelContext.setClientNode(new Node(ip, RandomUtil.randomInt(1030, 65530)));
		ImSessionContext imSessionContext = new ImSessionContext();
		channelContext.setAttribute(Const.IM_SESSION_KEY, imSessionContext);

		Devicetype devicetype = Devicetype.ANDROID;
		MobileInfo mobileInfo = new MobileInfo();
		mobileInfo.setAppversion("1.99");
		mobileInfo.setCid("9999");
		mobileInfo.setDeviceinfo("huawei p40");
		mobileInfo.setDevicetype(devicetype.getValue());
		mobileInfo.setImei(RandomUtil.randomString(16));
		mobileInfo.setOperator("电信");
		mobileInfo.setResolution("1200,345");
		mobileInfo.setSize("6.6");

		String token = org.tio.sitexxx.im.common.utils.ImUtils.IMTEST_UID_PRE + user.getId();
		HandshakeReq handshakeReq = new HandshakeReq();
		handshakeReq.setToken(token);
		handshakeReq.setDevicetype(devicetype.getValue());
		handshakeReq.setMobileInfo(mobileInfo);

		imSessionContext.setHandshakeReq(handshakeReq);
		imSessionContext.setHandshaked(true);

		imSessionContext.setAndroid(true);
		DataBlock dataBlock = Ip2Region.getDataBlock(ip);
		imSessionContext.setDataBlock(dataBlock);
		imSessionContext.setIos(false);
		imSessionContext.setUid(user.getId());
		imSessionContext.setWebsocket(false);
		imSessionContext.setWx(true);
		ImUtils.setHandshakeUser(channelContext, user);
		SimpleUser simpleUser = ImUtils.getHandshakeSimpleUser(channelContext);
		simpleUser.setMobileInfo(mobileInfo);
		return channelContext;
	}
	
	/**
	 * 获取测试用户
	 * @param request
	 * @param order 是否顺序获取用户： 1：顺序获取用户；2：随机用户；
	 * @return
	 * @author lixinji
	 * 2021年8月26日 下午3:18:34
	 */
	private User testUser(HttpRequest request,Short order) {
		if(Objects.equals(order, Const.YesOrNo.YES)) {
			return getUser(order);
		}
		User curr = WebUtils.currUser(request);
		if(curr == null) {
			return getUser(Const.YesOrNo.NO);
		}
		return curr;
	}
	
	
	
	/**
	 * 获取用户
	 * @param order 是否顺序获取用户： 1：顺序获取用户；2：随机用户；
	 * @return
	 * @author lixinji
	 * 2022年1月27日 下午4:17:44
	 */
	private User getUser(Short order) {
		if(!Objects.equals(order, Const.YesOrNo.YES)) {
			User romdom = randoms.get(RandomUtil.randomInt(0, randoms.size()));
			return romdom;
		} else {
			Integer getindex = index.getAndIncrement();
			if(getindex >= randsize) {
				synchronized (randoms) {
					getindex = index.getAndIncrement();
					if(getindex >= randsize) {
						index.set(0);
						getindex = index.getAndIncrement();
					}
				}
			}
			User romdom = randoms.get(getindex);
			return romdom;
		}
		
		
	}
	
	/**
	 * 初始化测试内存数据
	 * @param adminuid
	 * @param groupid
	 * @author lixinji
	 * 2022年1月27日 下午4:17:14
	 */
	private static void initRadom(Integer adminuid,Long groupid) {
		randoms = User.dao.find("select * from `user` where id in (select uid from wx_group_user where groupid = ? and `status` = ?) and id <> ?",groupid,Const.Status.NORMAL,adminuid);
		randsize = randoms.size();
		for(User user : randoms) {
			WxChatUserItem userItem = ChatIndexService.fdUserIndex(user.getId(), adminuid);
			if (!ChatService.existTwoFriend(userItem)) {
				continue;
			}
			Long chatlinkid = userItem.getChatlinkid();
			if (chatlinkid == null) {
				Ret ret = ChatService.me.actFdChatItems(user.getId(), adminuid);
				if (ret.isFail()) {
					continue;
				}
				chatlinkid = RetUtils.getOkTData(ret, "chatlinkid");
			}
			ChatService.me.getBaseChatItems(chatlinkid);
			fdChatMap.put(user.getId() + "", chatlinkid);
			ChatMsgService.me.p2pMsgList(chatlinkid, user.getId(), null, null);
			
			WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(user.getId(), groupid);
			if (groupItem == null) {
				continue;
			}
			Long groupchatlinkid = groupItem.getChatlinkid();
			if(groupchatlinkid == null) {
				Ret actRet = ChatService.me.actGroupChatItems(groupid, user.getId());
				if (actRet.isFail()) {
					continue;
				} else {
					groupchatlinkid = RetUtils.getOkTData(actRet, "chatlinkid");
				}
			}
			ChatService.me.getBaseChatItems(groupchatlinkid);
			groupChatMap.put(user.getId() + "", groupchatlinkid);
			
			ChatMsgService.me.groupMsgList(groupchatlinkid, user.getId(), null, null);
		}
		log.error("初始化测试数据成功：用户-size:{}",randsize);
	}
	
	/**
	 * 初始化注册用户
	 * @param adminuser
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2022年1月27日 下午4:16:57
	 */
	private Ret registerUser(User adminuser ) throws Exception {
		int noindex = USER_PHONE_START.incrementAndGet();
		if(noindex > 99999999)  {
			return RetUtils.failMsg("已超生成上限"); 
		}
		String phoneStart = "00000000" + noindex;
		String phone = "190" + phoneStart.substring(phoneStart.length()-8);
		String nick = (char)(Math.random()*26+'A') + phone;
		User user = new User();
		user.setLoginname(phone);
		IpInfo ipInfo = IpInfoService.ME.save(Const.MY_IP);
		user.setIpInfo(ipInfo);
		user.setPhone(phone);
		//无法登录
		user.setPhonepwd(UserService.getMd5Pwd(phone, nick));
		user.setPwd(user.getPhonepwd());
		user.setNick(nick);
		user.setPhonebindflag(Const.YesOrNo.YES);
		user.setStatus(User.Status.NORMAL);
		if (StrUtil.isBlank(user.getAvatar())) {
			String path = AvatarUtils.pressUserAvatar(nick);
			if (StrUtil.isNotBlank(path)) {
				user.setAvatar(path);
				user.setAvatarbig(path);
			}
		}
		RegisterAtom registerUserAtom = new RegisterAtom(user);
		boolean relsut = Db.tx(registerUserAtom);
		if (!relsut) {
			return RetUtils.failMsg(registerUserAtom.getMsg());
		}
		Ret slef = ChatService.me.actFdChatItems(user.getId(), user.getId());
		if (slef.isFail()) {
			log.warn("初始化用户时，发现自己加自己好友失败");
			return RetUtils.failMsg("自己加自己好友失败");
		}
		Ret ret = FriendService.me.addFriend(adminuser, user.getId());
		if (ret.isFail()) {
			log.warn("初始化用户时，发现添加管理员好友失败");
			return RetUtils.failMsg("初始化用户时，发现添加管理员好友失败");
		}
		return RetUtils.okData(user);
	}
	
	/**
	 * 
	 * @param channelContext
	 * @return
	 * @author lixinji
	 * 2021年8月25日 上午9:59:19
	 */
	public static SimpleUser getSimpleUser(ServerChannelContext channelContext) {
		return ImUtils.getHandshakeSimpleUser(channelContext);
	}
	

}
