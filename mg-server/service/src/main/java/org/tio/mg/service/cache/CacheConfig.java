
package org.tio.mg.service.cache;

import java.util.Objects;

import org.tio.utils.time.Time;

/**
 * @author tanyaowu
 * 2016年8月16日 上午10:22:25
 */
public enum CacheConfig {
    // [[----------------------  两级缓存 start ----------------------

	/**
	 * key: sessionId
	 * value: 验证码
	 */
	CAPTCHA(CacheType.CAFFEINE_REDIS, (Long) null, Time.MINUTE_1 * 5),

	/**
	 * 群目前在哪台服务器上
	 * key: groupid + ""
	 * value: imserver'ip 就是my.ip里面配的值
	 */
	MG_GROUPID_IMSERVER(CacheType.CAFFEINE_REDIS, (Long) null, Time.HOUR_1 * 1),

	/**
	 * 默认的IM服务器
	 * key: 固定的xx
	 * value: imserver'ip 就是my.ip里面配的值
	 */
	IMSERVER(CacheType.CAFFEINE_REDIS, Time.MINUTE_1 * 2, (Long) null),

	/**
	 * key:   ip
	 * value: ipinfo对象
	 */
	IP_IPINFO(CacheType.CAFFEINE_REDIS, (Long) null, Time.HOUR_1 * 6),

	/**
	 * key:   ipinfo的id
	 * value: ipinfo对象
	 */
	ID_IPINFO(CacheType.CAFFEINE_REDIS, (Long) null, Time.HOUR_1 * 6),

	/**
	 * 
	 * key:   client_id
	 * value: Oauth2Client对象
	 */
	MG_CLIENTID_OAUTH2CLIENT(CacheType.CAFFEINE_REDIS, (Long) null, Time.HOUR_1 * 6),

	/**
	 * key:   imei
	 * value: imeistat对象
	 */
	IMEI_IMEISTAT(CacheType.CAFFEINE_REDIS, (Long) null, Time.HOUR_1 * 6),

	/**
	 * session里的一些属性需要的存活期比较短，就用这个
	 * key: sessionId + key
	 * value: Serializable
	 */
	MG_SESSION_5_MINUTES(CacheType.CAFFEINE_REDIS, Time.MINUTE_1 * 5, (Long) null),

	/**
	 * tio临时访问令牌
	 * key: 用户的session cookie值
	 * value: 比较复杂，不在此描述
	 */
	MG_TIO_ACCESS_TOKEN_TEMP(CacheType.CAFFEINE_REDIS, Time.MINUTE_1 * 1, (Long) null),

	/**
	 * tio访问令牌
	 * key: 用户的session cookie值
	 * value: 比较复杂，不在此描述
	 */
	MG_TIO_ACCESS_TOKEN(CacheType.CAFFEINE_REDIS, Time.MINUTE_1 * 120, (Long) null),

	/**
	 * 在给客户端（含浏览器和APP）生成access_token时，对方的useragent信息
	 * key: access_token
	 * value: useragent
	 */
	MG_TIO_ACCESSTOKEN_USERAGENT(CacheType.CAFFEINE_REDIS, Time.MINUTE_1 * 125, (Long) null),

	/**
	 * 群组的数据统计
	 * key: 群主的userid
	 * value: GroupStat
	 */
	GROUP_STAT(CacheType.CAFFEINE_REDIS, (Long) null, Time.HOUR_1 * 10),

	/**
	 * 用户的openid和uid
	 * key: openidtype + "_" + openid
	 * value: UserThird
	 */
	OPENID_USERTHIRD(CacheType.CAFFEINE_REDIS, Time.HOUR_1 * 2, (Long) null),

	/**
	 * 用户的uid
	 * key: uid
	 * value: UserThird
	 */
	UID_USERTHIRD(CacheType.CAFFEINE_REDIS, Time.HOUR_1 * 2, (Long) null),

	/**
	 * 存活5秒的两级缓存，key值由业务控制
	 */
	MG_TIME_TO_LIVE_SECONDS_5(CacheType.CAFFEINE_REDIS, Time.SECOND_1 * 5, (Long) null),

	/**
	 * 存活5分钟的两级缓存，key值由业务控制
	 */
	MG_TIME_TO_LIVE_MINUTE_5(CacheType.CAFFEINE_REDIS, Time.MINUTE_1 * 5, (Long) null),

	/**
	 * 闲置5秒的两级缓存，key值由业务控制
	 */
	MG_TIME_TO_IDLE_SECONDS_5(CacheType.CAFFEINE_REDIS, (Long) null, Time.SECOND_1 * 5),

    /**************************************xufei-add-or-rewrite-begin**********************************************************/

	/**
	 * 聊天会话焦点缓存
	 * key:uid_devicetype
	 * value:FocusVo
	 */
	CHAT_ON_FOCUS_1(CacheType.CAFFEINE_REDIS, Time.MINUTE_1 * 3, (Long) null),

	/**
	 * 聊天会话焦点缓存-群组
	 * key:group
	 * value:map<key:uid,uid>
	 */
	CHAT_ON_FOCUS_GROUP_1(CacheType.CAFFEINE_REDIS, Time.MINUTE_1 * 3, (Long) null),

	/**
	 * 用户聊天列表群组索引
	 * key:groupid_uid
	 * value:WxChatGroupItem
	 */
	CHAT_GROUP_INDEX_2(CacheType.CAFFEINE, Time.HOUR_1 * 12, (Long) null),
	
	
	/**
	 * 群用户列表
	 * key:groupid/groupid_limitparams
	 * value:Arraylist<record>
	 */
	CHAT_GROUP_USER_LIST_2(CacheType.CAFFEINE_REDIS, Time.SECOND_1 * 30, (Long) null),
	
	
	/**
	 * 用户聊天列表用户索引
	 * key:uid_chatmode_bizid
	 * value:WxChatUserItem
	 */
	CHAT_USER_INDEX_1(CacheType.CAFFEINE, Time.HOUR_1 * 12,  (Long) null),

	/**
	 * 用户聊天会话
	 * key:id
	 * value:WxChatItems
	 */
	CHAT_ITEMS_3(CacheType.CAFFEINE_REDIS, Time.SECOND_1 * 30, (Long) null),
	
	/**
	 * 用户的被申请记录
	 * key:id
	 * value:Record
	 */
	WX_FRIEND_APPLY_INFO_1(CacheType.CAFFEINE_REDIS, Time.HOUR_1 * 12, Time.DAY_1 * 1),

	/**
	 * 拉黑记录
	 * key:uid_touid
	 * value:WxUserBlackItems
	 */
	CHAT_USER_BLOCK_1(CacheType.CAFFEINE_REDIS, Time.HOUR_1 * 12, Time.DAY_1 * 1),

    /**************************************xufei-add-or-rewrite--end-**********************************************************/

    // ----------------------  两级缓存 end ----------------------]]

    // [[----------------------  redis缓存 start ----------------------

	/**
	 *  邮箱注册，邮箱激活码
	 * key: 激活码
	 * value: User
	 */
	EMAIL_AUTHCODE(CacheType.REDIS, Time.MINUTE_1 * 15, (Long) null),

	/**
	 * 每天每个IP邮件发送上限
	 * key：   ip
	 * value:最近24小时发送email的次数
	 */
	EMAIL_SENT_COUNT_PERIP_PERDAY(CacheType.REDIS, Time.HOUR_1 * 24, (Long) null),

	/**
	 * 每天邮件发送上限
	 * key：   ip
	 * value:最近24小时发送email的次数
	 */
	EMAIL_SENT_COUNT_PERDAY(CacheType.REDIS, Time.HOUR_1 * 24, (Long) null),

	/**
	 * 访问令牌第一步请求的返回值对象
	 * key: 比较复杂，不在此描述
	 * value: AccessTokenResp1
	 */
	MG_ACCESS_TOKEN_RESP_1(CacheType.REDIS, Time.SECOND_1 * 5, (Long) null),

	/**
	 *  博客访问次数(因为此值修改频率很高，所以不建议用两级缓存)
	 * key: blogid
	 * value: 博客访问次数
	 */
	BLOG_ACCESSCOUNT(CacheType.REDIS, (Long) null, Time.MINUTE_1 * 60),

	/**
	 * 短信ip统计
	 */
	SMS_IP_COUNT(CacheType.REDIS, Time.HOUR_1 * 24, (Long) null),

	/**
	 * 短信次数统计
	 */
	SMS_MOBILE_COUNT(CacheType.REDIS, Time.HOUR_1 * 24, (Long) null),

	/**
	 * 设备短信次数
	 */
	SMS_DEVICE_COUNT(CacheType.REDIS, Time.HOUR_1 * 2, (Long) null),

	/**
	 * 短信验证码缓存
	 */
	SMS_MOBILE_CODE(CacheType.REDIS, Time.MINUTE_1 * 10, (Long) null),

	/**
	 * 手机分步用户id验证令牌
	 */
	CHECK_PHONE_USERID_TOKEN(CacheType.REDIS, Time.MINUTE_1 * 15, (Long) null),

	/**
	 * 手机分步手机号验证令牌
	 */
	CHECK_PHONE_TOKEN(CacheType.REDIS, Time.MINUTE_1 * 15, (Long) null),

	/**
	 * 微信登录token
	 */
	WECHAT_LOGIN_TOKEN(CacheType.REDIS, Time.MINUTE_1 * 60, (Long) null),

	/**
	 * 二维码支付
	 * key: 
	 * value: 
	 */
	RECHARGE_QR(CacheType.REDIS, Time.MINUTE_1 * 3, (Long) null),

	/**
	 * 检查最新版本号
	 * key:一个随机串
	 * value:tio版本号
	 */
	MG_LATEST_VERSION(CacheType.REDIS, Time.SECOND_1 * 10, (Long) null),

	/**
	 * 用户是否正在通话
	 * key: uid
	 * value: 对方的uid
	 */
	WX_IS_CALLING(CacheType.REDIS, (Long) null, Time.MINUTE_1 * 60 * 12),

    // ----------------------  redis缓存 end ----------------------]]

    // ----------------------  caffeine 缓存 start ----------------------]]

	/**
	 * key: pageNumber_pageSize 譬如1_10
	 * value: Page<Demo>
	 */
	DEMO_CACHE(CacheType.CAFFEINE, (Long) null, Time.MINUTE_1 * 10),

	/**
	 * 搜索用户
	 * key: 可能是nick，也可能是其它
	 * value: Page<Record>
	 */
	SEARCH_USER(CacheType.CAFFEINE, Time.MINUTE_1 * 1, (Long) null),

	/**
	 * 搜索用户
	 * key: 可能是uid
	 * value: Page<Record>
	 */
	USER_INFO(CacheType.CAFFEINE, (Long) null, Time.MINUTE_1 * 60),

	/**
	 * key: userid + ""
	 * value: user
	 */
	USERID_USER_2(CacheType.CAFFEINE, (Long) null, Time.HOUR_1),

	/**
	 * 自动生成的头像缓存
	 * key:头像字符
	 * value:头像路径
	 */
	AUTO_AVATAR(CacheType.CAFFEINE, Time.DAY_1 * 1, Time.DAY_1 * 3),

	/**
	 * 用户基础信息缓存信息
	 * key: userid + ""
	 * value: <userBase>
	 */
	USERID_BASE(CacheType.CAFFEINE, (Long) null, Time.HOUR_1 * 1),

	/**
	 * key: loginname
	 * value: user
	 */
	LOGINNAME_USER(CacheType.CAFFEINE, (Long) null, Time.HOUR_1),

	/**
	 * 存活5分钟的本地缓存，key值由业务控制
	 */
	MG_TIME_TO_LIVE_MINUTE_5_LOCAL(CacheType.CAFFEINE, Time.MINUTE_1 * 5, (Long) null),

	/**
	 * 闲置5分钟的本地缓存，key值由业务控制
	 */
	MG_TIME_TO_IDLE_MINUTE_5_LOCAL(CacheType.CAFFEINE, (Long) null, Time.MINUTE_1 * 5),

	/**
	 * key:   user_agent串
	 * value: UserAgent对象
	 */
	USER_AGENT(CacheType.CAFFEINE, (Long) null, Time.HOUR_1 * 1),

	/**
	 * blog缓存，带title, html等
	 * key: id
	 * value: Blog
	 */
	BLOG_WITH_HTML(CacheType.CAFFEINE, (Long) null, Time.MINUTE_1 * 5),

	/**
	 * blog缓存，带title, md等
	 * key: id
	 * value: Blog
	 */
	BLOG_WITH_MD(CacheType.CAFFEINE, (Long) null, Time.MINUTE_1 * 5),

	/**
	 * blog缓存，带md, title
	 * key: id
	 * value: Blog
	 */
	BLOG_MD(CacheType.CAFFEINE, (Long) null, Time.MINUTE_1 * 5),

	/**
	 * blog缓存，带html, title
	 * key: id
	 * value: Blog
	 */
	BLOG_HTML(CacheType.CAFFEINE, (Long) null, Time.MINUTE_1 * 5),

	/**
	 * blog缓存，不带博客数据
	 * key: id
	 * value: Blog
	 */
	BLOG(CacheType.CAFFEINE, (Long) null, Time.MINUTE_1 * 5),
	/**
	 * view工程，html的缓存
	 * key: path
	 * value: HttpResponse
	 */
	MG_VIEW_HTML(CacheType.CAFFEINE, (Long) null, Time.HOUR_1 * 19),

	/**
	 * 注意，本缓存只能用本地缓存，不允许用分布式缓存
	 * 群聊记录
	 * key: groupid 就是群主的uid
	 * value: ListWithLock<ChatroomMsg>
	 */
	GROUP_CHAT_LOG(CacheType.CAFFEINE, Time.MINUTE_1 * 5, (Long) null),

	/**
	 *俩人的私聊记录
	 * key: getP2PKey(fromuid, touid)
	 * value: ListWithLock<ChatroomMsg>
	 */
	P2P_CHAT_LOG(CacheType.CAFFEINE, Time.MINUTE_1 * 5, (Long) null),

	/**
	 * 某人未读消息的用户数（有多少用户的消息未读）
	 * key: touid
	 * value: List<Record>
	 */
	N2P_NOT_READ_COUNT(CacheType.CAFFEINE, Time.MINUTE_1 * 5, (Long) null),

	/**
	 * 注意，本缓存只能用本地缓存，不允许用分布式缓存
	 * Wx群聊记录
	 * key: groupid
	 * value: ListWithLock<>
	 */
	WX_GROUP_MSG(CacheType.CAFFEINE, Time.MINUTE_1 * 5, (Long) null),

	/**
	 * 群基础信息
	 * key: groupid
	 * value: wxGroup 
	 */
	WX_GROUP_1(CacheType.CAFFEINE, Time.MINUTE_1 * 5, (Long) null),
	
	/**
	 * 通讯录列表
	 * key:用户id
	 * value:HashMap<string,object>
	 */
	WX_MAILLIST_1(CacheType.CAFFEINE, Time.MINUTE_1 * 30, (Long) null),

    //	/**
    //	 * 群组信息
    //	 * key: groupid
    //	 * value: WxGroup对象
    //	 */
    //	WX_GROUP_INFO(CacheType.CAFFEINE, (Long) null, Time.HOUR_1 * 5),

	/**
	 * 某人的最近的私聊信息
	 * key: touid
	 * value: List<Record>
	 */
	WX_RECENT_MSG(CacheType.CAFFEINE, Time.MINUTE_1 * 5, (Long) null),

	/**
	 *俩人的私聊记录
	 * key: getP2PKey(fromuid, touid)
	 * value: ListWithLock<ChatroomMsg>
	 */
	WX_FRIEND_MSG(CacheType.CAFFEINE, Time.MINUTE_1 * 5, (Long) null),

	/**
	 *	新版本私聊记录
	 * key: chatlinkid
	 * value: ListWithLock<WxFriendMsg>
	 */
	WX_FRIEND_MSG_CHAT_2(CacheType.CAFFEINE, Time.MINUTE_1 * 5, (Long) null),

	/**
	 *	新版本群聊记录
	 * key: groupid
	 * value: ListWithLock<WxGroupMsg>
	 */
	WX_GROUP_CHAT_3(CacheType.CAFFEINE, Time.MINUTE_1 * 5, (Long) null),

	/**
	 * 某人未读消息的用户数（有多少用户的消息未读）
	 * key: touid
	 * value: List<Record>
	 */
	WX_RECENT_MSG_NOT_READ(CacheType.CAFFEINE, Time.MINUTE_1 * 5, (Long) null),

	/**
	 * 我的好友，主要是有一个备注名是因人而异的，所以要特别处理一下
	 * key: uid + "_" + frienduid
	 * value: WxFriend对象
	 */
	WX_MY_FRIEND(CacheType.CAFFEINE, (Long) null, Time.MINUTE_1 * 60),

	/**
	 * 俩人是不是好友
	 * key: twouid
	 * value: boolean
	 */
	WX_IS_FRIEND(CacheType.CAFFEINE, (Long) null, Time.MINUTE_1 * 60),

	/**
	 * 某人的最近的私聊信息
	 * key: touid
	 * value: List<Record>
	 */
	N2P_LATELY_COUNT(CacheType.CAFFEINE, Time.MINUTE_1 * 5, (Long) null),

	/**
	 * key: 版本号，譬如1.0.1
	 * value: 配置的值
	 */
	IosCheckConf(CacheType.CAFFEINE, Time.MINUTE_1 * 10, (Long) null),

	/**
	 * key: groupid（userid）
	 * value: ImPacket
	 */
	MG_PAGE_ONLINE(CacheType.CAFFEINE, (Long) null, Time.MINUTE_1 * 10),

	/**
	 * 当前用户数
	 * 单值
	 */
	USER_COUNT(CacheType.CAFFEINE, (Long) null, Time.HOUR_1 * 1),

	/**
	 * 某人的好友分组列表
	 * key: userid
	 * value: List<ImFriendgroup>
	 */
	MY_FRIEND_GROUPS(CacheType.CAFFEINE, Time.HOUR_1 * 1, (Long) null),

	/**
	 * 某人的好友列表
	 * key: userid
	 * value: List<Record>
	 */
	MY_FRIEND_IDS(CacheType.CAFFEINE, Time.HOUR_1 * 1, (Long) null),

	/**
	 * WxCallItem对象
	 * key: id
	 * value: WxCallItem
	 */
	WxCallItem(CacheType.CAFFEINE, (Long) null, Time.MINUTE_1 * 30),
	
	
	/**************************************xufei-add-or-rewrite-mg-begin**********************************************************/
	
	
	/**
	 * 用户权限表
	 * key:mguid
	 * value:AuthVo
	 */
	MG_USER_MENU_AUTH(CacheType.CAFFEINE, Time.HOUR_1 * 12, Time.DAY_1 * 1),
	
	/**************************************xufei-add-or-rewrite-mg--end-**********************************************************/


	///占用的
	XXXXXXXXXX(CacheType.CAFFEINE, Time.HOUR_1 * 1, (Long) null);

	// ----------------------  caffeine 缓存 end ----------------------]]

	//	EMAIL_AUTHCODE1(CacheType.REDIS, (Long) null, P.get);

	CacheType	cacheType;
	String		cacheName;
	Long		timeToLiveSeconds;	//单位：秒

	Long timeToIdleSeconds; //单位：秒

	private CacheConfig(CacheType cacheType, Long timeToLiveSeconds, Long timeToIdleSeconds) {
		this.cacheType = cacheType;
		this.timeToLiveSeconds = timeToLiveSeconds;
		this.timeToIdleSeconds = timeToIdleSeconds;
		this.cacheName = this.name();//.toString();
	}

	public static CacheConfig from(String cacheName) {
		CacheConfig[] values = CacheConfig.values();
		for (CacheConfig v : values) {
			if (Objects.equals(v.cacheName, cacheName)) {
				return v;
			}
		}
		return null;
	}

	public String getCacheName() {
		return cacheName;
	}

	public CacheType getCacheType() {
		return cacheType;
	}

	public Long getTimeToIdleSeconds() {
		return timeToIdleSeconds;
	}

	public Long getTimeToLiveSeconds() {
		return timeToLiveSeconds;
	}
}
