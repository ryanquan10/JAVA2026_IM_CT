
package org.tio.mg.im.common.utils;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.core.TioConfig;
import org.tio.http.common.HttpConfig;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.session.HttpSession;
import org.tio.mg.im.common.ImSessionContext;
import org.tio.mg.im.common.bs.HandshakeReq;
import org.tio.mg.im.common.converter.SimpleUserConverter;
import org.tio.mg.service.init.RedisInit;
import org.tio.mg.service.model.main.IpInfo;
import org.tio.mg.service.model.main.User;
import org.tio.mg.service.model.main.UserAgent;
import org.tio.mg.service.service.base.IpInfoService;
import org.tio.mg.service.service.base.UserAgentService;
import org.tio.mg.service.service.base.UserRoleService;
import org.tio.mg.service.service.base.UserService;
import org.tio.mg.service.vo.SessionExt;
import org.tio.mg.service.vo.SimpleUser;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.Devicetype;
import org.tio.sitexxx.service.vo.MobileInfo;
import org.tio.sitexxx.service.vo.SessionKey;
import org.tio.utils.cache.caffeineredis.CaffeineRedisCache;
import org.tio.utils.jfinal.P;
import org.tio.utils.lock.SetWithLock;
import org.tio.utils.page.Page;

import cn.hutool.core.util.StrUtil;

/**
 * @author tanyaowu 
 * 2016年9月13日 上午11:41:13
 */
public class ImUtils {
	private static Logger log = LoggerFactory.getLogger(ImUtils.class);

	/**
	 * 握手时该ChannelContext的User对象<br>
	 * value: User对象<br>
	 */
	private static final String CHANNEL_KEY_HANDSHAKE_USER = "TIO_SITE_HANDSHAKE_USER";

	/**
	 * 握手时该ChannelContext的SimpleUser对象<br>
	 * value: SimpleUser对象<br>
	 */
	private static final String TIO_SITE_HANDSHAKE_SIMPLEUSERVO = "TIO_SITE_HANDSHAKE_SIMPLEUSERVO";

	/**
	 * 设置握手时的User
	 * @param channelContext
	 * @param user
	 */
	public static SimpleUser setHandshakeUser(ChannelContext channelContext, User user) {
		return setHandshakeUser(channelContext, user, null);
	}

	/**
	 * 设置握手时的User
	 * @param channelContext
	 * @param user
	 * @param may
	 * @return
	 */
	public static SimpleUser setHandshakeUser(ChannelContext channelContext, User user, SimpleUser may) {
		if (user != null) {

			//			if (UserService.isSuper(user.getLoginname())) {
			//				user = null;
			//			}
		}
		channelContext.setAttribute(CHANNEL_KEY_HANDSHAKE_USER, user);
		SimpleUser simpleUser = setHandshakeSimpleUser(channelContext, user, may);
		return simpleUser;
	}

	/**
	 * 获取握手时的User
	 * @param channelContext
	 */
	public static User getHandshakeUser(ChannelContext channelContext) {
		return (User) channelContext.getAttribute(CHANNEL_KEY_HANDSHAKE_USER);
	}

	/**
	 * 设置握手时的SimpleUser
	 * @param channelContext
	 * @param simpleUser
	 */
	public static SimpleUser setHandshakeSimpleUser(ChannelContext channelContext, User user) {
		return setHandshakeSimpleUser(channelContext, user, null);
	}

	/**
	 * 设置握手时的SimpleUser
	 * @param channelContext
	 * @param user
	 * @param may
	 * @return
	 */
	public static SimpleUser setHandshakeSimpleUser(ChannelContext channelContext, User user, SimpleUser may) {
		SimpleUser simpleUser = ImUtils.getSimpleUser(user, channelContext, true);
		simpleUser.setMay(may);
		//		simpleUser.setIsLogout(isLogout);
		channelContext.setAttribute(TIO_SITE_HANDSHAKE_SIMPLEUSERVO, simpleUser);
		return simpleUser;
	}

	/**
	 * 获取握手时的SimpleUser
	 * @param channelContext
	 */
	public static SimpleUser getHandshakeSimpleUser(ChannelContext channelContext) {
		return (SimpleUser) channelContext.getAttribute(TIO_SITE_HANDSHAKE_SIMPLEUSERVO);
	}

	/**
	 * 
	 * @author: tanyaowu
	 */
	protected ImUtils() {
	}

	private static final long httpSessionTimeout = P.getLong("http.session.timeout", HttpConfig.DEFAULT_SESSION_TIMEOUT);

	private static final CaffeineRedisCache httpSessionCache = CaffeineRedisCache.register(RedisInit.get(), P.get("http.session.cache.name", HttpConfig.SESSION_CACHE_NAME), null,
	        httpSessionTimeout);

	private static UserService userService = UserService.ME;

	/**
	 * 根据token获取userid
	 * @param token
	 * @return
	 * @author: tanyaowu
	 */
	public static Integer getUseridByToken(String token) {
		if (StrUtil.isBlank(token)) {
			return null;
		}

		//快速算法 start
		HttpSession session = (HttpSession) httpSessionCache.get(token);
		if (session != null) {
			SessionExt sessionExt = session.getAttribute(SessionKey.SESSION_EXT, SessionExt.class);
			Integer userid = sessionExt.getUid();
			if (userid != null) {
				return userid;
			} else {
				log.info("session中并未绑定userid, token:{}", token);
				return null;
			}
		} else {
			log.info("不能根据token[{}]找到session对象", token);
			return null;
		}
		//快速算法 end

		//传统算法 start
		//		String site = site();
		//		try {
		//			String url = site + "/user/byToken?token=" + URLEncoder.encode(token, Const.CHARSET);
		//			OkHttpClient client = new OkHttpClient();
		//			Request request = new Request.Builder().url(url).build();
		//			Response response = client.newCall(request).execute();
		//			String useridStr = response.body().string();
		//			if (StrUtil.isBlank(useridStr)) {
		//				return null;
		//			} else {
		//				Integer userid = Integer.parseInt(useridStr);
		//				return userid;
		//			}
		//		} catch (Throwable e) {
		//			log.error(e.toString(), e);
		//			return null;
		//		}
		//传统算法 end
	}

	/**
	 * 获取token
	 * @param channelContext
	 * @return
	 * @author tanyaowu
	 */
	public static String getToken(ChannelContext channelContext) {
		ImSessionContext imSessionContext = ImUtils.getImSessionContext(channelContext);
		if (imSessionContext == null) {
			return null;
		}

		HandshakeReq handshakeReq = imSessionContext.getHandshakeReq();
		if (handshakeReq == null) {
			return null;
		}

		String token = handshakeReq.getToken();

		return token;
	}

	/**
	 * 获取channelContext对应的用户
	 * @param channelContext
	 * @return
	 * @author: tanyaowu
	 */
	public static User getUser(ChannelContext channelContext) {
		if (channelContext.isVirtual) {
			return getHandshakeUser(channelContext);
		}

		String token = getToken(channelContext);

		User user = getUser(token);

		//检查token是不是过期了
		if (user == null) {
			if (StrUtil.isNotBlank(channelContext.userid)) {
				Tio.unbindUser(channelContext);
			}
		}

		return user;
	}

	public static Integer getUid(ChannelContext channelContext) {
		User user = getUser(channelContext);
		if (user == null) {
			return null;
		}
		
		return user.getId();
	}

	/**
	 * 预警：所有状态下的用户，未处理用户状态，调用方法处，进行状态业务处理
	 * @param token
	 * @return
	 * @author tanyaowu
	 */
	public static User getUser(String token) {
		if (StrUtil.isBlank(token)) {
			return null;
		}

		Integer userid = ImUtils.getUseridByToken(token);
		/**
		 * TO-此处未处理用户状态，调用方法处，进行状态业务处理
		 */
		User user = userService.getById(userid);
		return user;
	}

	//	/**
	//	 * 建议不使用，用户的状态未处理，建议分步使用
	//	 * @param token
	//	 * @return
	//	 * @author tanyaowu
	//	 */
	//	@Deprecated
	//	public static SimpleUser getSimpleUser(String token) {
	//		User user = getUser(token);
	//		return SimpleUser.fromUser(user);
	//	}

	/**
	 * 获取SimpleUser，只在登录状态下返回SimpleUser对象，否则返回null
	 * @param channelContext
	 * @return
	 * @author tanyaowu
	 */
	public static SimpleUser getSimpleUser(ChannelContext channelContext) {
		return getSimpleUser(channelContext, false);
	}

	/**
	 * 获取SimpleUser
	 * @param channelContext
	 * @param force 
	 * 			true: 不管登录与否，都返回SimpleUser对象；
	 * 			false：只在登录状态下返回SimpleUser对象
	 * @return
	 */
	public static SimpleUser getSimpleUser(ChannelContext channelContext, boolean force) {
		User user = getUser(channelContext);
		return getSimpleUser(user, channelContext, force);
	}

	/**
	 * 根据ChannelContext获取User对象
	 * @param user
	 * @param channelContext
	 * @param force 是否强制创建User
	 * @return
	 */
	public static SimpleUser getSimpleUser(User user, ChannelContext channelContext, boolean force) {
		if (!UserRoleService.checkUserStatus(user)) {
			if (!force) {
				return null;
			}
		}
		SimpleUser simpleUser = null;
		if (user != null) {
			simpleUser = SimpleUser.fromUser(user);
			completeSimpleUser(simpleUser, channelContext);
		} else {
			simpleUser = createTourist(channelContext);
		}
		return simpleUser;
	}

	/**
	 * 创建一个游客
	 * @param channelContext
	 * @return
	 * @author tanyaowu
	 */
	public static SimpleUser createTourist(ChannelContext channelContext) {
		SimpleUser simpleUser = new SimpleUser();
		simpleUser.setN("游客" + channelContext.getId());
		completeSimpleUser(simpleUser, channelContext);
		return simpleUser;
	}

	/**
	 * 用ChannelContext完善SimpleUser对象，主要用于IM前端显示
	 * @param simpleUser
	 * @param channelContext
	 */
	public static void completeSimpleUser(SimpleUser simpleUser, ChannelContext channelContext) {
		ImSessionContext imSessionContext = ImUtils.getImSessionContext(channelContext);
		if (imSessionContext.isWebsocket()) {
			HttpRequest httpRequest = ImUtils.getHandshakeRequest(channelContext);
			String userAgentStr = httpRequest.getUserAgent();
			UserAgent userAgent = UserAgentService.ME.save(userAgentStr);
			simpleUser.setUserAgent(userAgent);
		} else {
			simpleUser.setMobileInfo(ImUtils.getMobileInfo(channelContext));
		}

		String ip = channelContext.getClientNode().getIp();
		IpInfo ipInfo = IpInfoService.ME.save(ip);

		simpleUser.setCid(channelContext.getId());
		simpleUser.setTimeCreated(channelContext.stat.timeCreated);
		simpleUser.setIpInfo(ipInfo);
		simpleUser.setGroupid(imSessionContext.getGroupid());
	}

	/**
	 * 获取ImSessionContext
	 * @param channelContext
	 * @return
	 * @author tanyaowu
	 */
	public static ImSessionContext getImSessionContext(ChannelContext channelContext) {
		ImSessionContext imSessionContext = (ImSessionContext) channelContext.getAttribute(Const.IM_SESSION_KEY);
		return imSessionContext;
	}

	/**
	 * 获取HandshakeReq
	 * @param channelContext
	 * @return
	 * @author tanyaowu
	 */
	public static HandshakeReq getHandshakeReq(ChannelContext channelContext) {
		ImSessionContext imSessionContext = ImUtils.getImSessionContext(channelContext);
		HandshakeReq handshakeReq = imSessionContext.getHandshakeReq();

		return handshakeReq;
	}

	/**
	 * 获取设备类型
	 * @param channelContext
	 * @return
	 * @author tanyaowu
	 */
	public static Devicetype getDevicetype(ChannelContext channelContext) {
		ImSessionContext imSessionContext = ImUtils.getImSessionContext(channelContext);

		if (imSessionContext.isWebsocket()) {
			return Devicetype.WEB;
		}

		HandshakeReq handshakeReq = getHandshakeReq(channelContext);
		return Devicetype.from(handshakeReq.getMobileInfo().getDevicetype());
	}

	/**
	 * 
	 * @param channelContext
	 * @return
	 * @author tanyaowu
	 */
	public static boolean isIos(ChannelContext channelContext) {
		ImSessionContext imSessionContext = ImUtils.getImSessionContext(channelContext);
		return imSessionContext.isIos();
	}

	/**
	 * 
	 * @param channelContext
	 * @return
	 * @author tanyaowu
	 */
	public static boolean isAndroid(ChannelContext channelContext) {
		ImSessionContext imSessionContext = ImUtils.getImSessionContext(channelContext);
		return imSessionContext.isAndroid();
	}

	/**
	 * 手机过来的连接才有，PC端过来的都会返回null
	 * @param channelContext
	 * @return
	 * @author tanyaowu
	 */
	public static MobileInfo getMobileInfo(ChannelContext channelContext) {
		ImSessionContext imSessionContext = ImUtils.getImSessionContext(channelContext);

		if (imSessionContext.isWebsocket()) {
			return null;
		}

		HandshakeReq handshakeReq = getHandshakeReq(channelContext);
		return handshakeReq.getMobileInfo();
	}

	/**
	 * 获取ws握手请求包
	 * @param channelContext
	 * @return
	 * @author tanyaowu
	 */
	public static HttpRequest getHandshakeRequest(ChannelContext channelContext) {
		ImSessionContext imSessionContext = ImUtils.getImSessionContext(channelContext);

		if (imSessionContext.isWebsocket()) {
			return imSessionContext.getWsSessionContext().getHandshakeRequest();
		}

		return null;
	}

	/**
	 * 分页获取某组的用户
	 * @param tioConfig
	 * @param channelContext
	 * @param group
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 * @author tanyaowu
	 */
	public static Page<SimpleUser> getPageOfGroup(TioConfig tioConfig, ChannelContext channelContext, String group, Integer pageNumber, Integer pageSize) {
		SimpleUserConverter simpleUserConverter = null;
		ImSessionContext imSessionContext = ImUtils.getImSessionContext(channelContext);
		if (imSessionContext.isSuper()) {
			simpleUserConverter = SimpleUserConverter.supper;
		} else {
			simpleUserConverter = SimpleUserConverter.me;
		}
		Page<SimpleUser> x = Tio.getPageOfGroup(tioConfig, group, pageNumber, pageSize, simpleUserConverter);
		return x;
	}

	/**
	 * 重新绑定群组，主要用于重新排序
	 * @param channelContext
	 */
	public static void rebindGroups(ChannelContext channelContext) {
		//		ImSessionContext imSessionContext = ImUtils.getImSessionContext(channelContext);
		boolean isModifiedUser = false; //是否设置了握手用户信息，即是否调用了：ImUtils.setHandshakeUser(channelContext, user);
		TioConfig tioConfig = channelContext.tioConfig;
		User user = ImUtils.getUser(channelContext);

		//如果之前已经握过手，则先将该ChannelContext从所有群组中解绑Group，这个主要是为了TreeSet排序的正确性
		SetWithLock<String> setWithLock = channelContext.getGroups();
		if (setWithLock != null) {
			try {
				Set<String> groups = setWithLock.getObj();
				if (groups != null && groups.size() > 0) {
					//1、先备份一下已经所属群组
					Set<String> groupsbak = new HashSet<>(groups.size());
					groupsbak.addAll(groups);

					//2、解绑群组
					//tioConfig.groups.unbind(channelContext, false);
					for (String groupid : groupsbak) {
						try {
							tioConfig.groups.unbind(groupid, channelContext, true, false);
						} catch (Exception e) {
							log.error(e.toString(), e);
						}
					}

					//3、修改握手User
					ImUtils.setHandshakeUser(channelContext, user);
					isModifiedUser = true;

					//4、重新绑定群组
					for (String groupid : groupsbak) {
						try {
							tioConfig.groups.bind(groupid, channelContext, false);
						} catch (Exception e) {
							log.error(e.toString(), e);
						}
					}
				}
			} catch (Throwable e) {
				throw e;
			}
		}

		if (!isModifiedUser) {
			ImUtils.setHandshakeUser(channelContext, user);
		}
	}
}
