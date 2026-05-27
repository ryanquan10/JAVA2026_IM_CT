
/**
 * 
 */
package org.tio.sitexxx.service.service.stat;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Page;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.model.main.IpInfo;
import org.tio.sitexxx.service.model.main.TioThreadLogs;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.WxFriend;
import org.tio.sitexxx.service.model.main.WxFriendMeta;
import org.tio.sitexxx.service.model.stat.AreaStat;
import org.tio.sitexxx.service.model.stat.TioIpPathAccessStat;
import org.tio.sitexxx.service.model.stat.UserRegisterStat;
import org.tio.sitexxx.service.service.base.IpInfoService;
import org.tio.sitexxx.service.service.base.UserService;
import org.tio.sitexxx.service.utils.PyUtils;
import org.tio.sitexxx.service.utils.QueryUtils;
import org.tio.sitexxx.service.vo.Const;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @author tanyaowu
 *
 */
public class StatService {

	/**
	 * 
	 */
	public StatService() {
	}

	private static Logger log = LoggerFactory.getLogger(StatService.class);

	public static final StatService me = new StatService();

	final TioIpPathAccessStat dao = new TioIpPathAccessStat().dao();

	private static final int MAX_STAT_DAY = 60;

	/**
	 *  -- 按天统计页面的访问量
		---------------------------
		--     d              path        count
		-- 2018-04-16   /case/index.html	 24
		-- 2018-04-16   /case/index.html	 109
		-- 2018-04-16   /doc/index.html	     18
		-- 2018-04-17   /doc/index.html	     97
		---------------------------
	 * @param path 访问路径，譬如/index.html
	 * @param days 统计多少天的
	 * @return
	 */
	public List<Record> requestCountByDay(Integer days) {
		if (days == null) {
			days = 7;
		} else {
			if (days > MAX_STAT_DAY) {
				days = MAX_STAT_DAY;
			}
			if (days < 0) {
				days = 7;
			}
		}

		String cacheKey = "stat.requestCountByDay_" + days;
		return QueryUtils.findWithCache(CacheConfig.TIME_TO_LIVE_MINUTE_5, cacheKey, Const.Db.TIO_SITE_STAT, "stat.requestCountByDay", true, days);

	}

	/**
	 *  -- 按天统计不同的ip数
		---------------------------
		--    d         |  count
		-- 2018-04-16	|  35
		-- 2018-04-17	|  222
		---------------------------
	 * @param days
	 * @return
	 */
	public List<Record> ipCountByDay(Integer days) {
		if (days == null) {
			days = 7;
		} else {
			if (days > MAX_STAT_DAY) {
				days = MAX_STAT_DAY;
			}
			if (days < 0) {
				days = 7;
			}
		}

		String cacheKey = "stat.ipCountByDay_" + days;
		return QueryUtils.findWithCache(CacheConfig.TIME_TO_LIVE_MINUTE_5, cacheKey, Const.Db.TIO_SITE_STAT, "stat.ipCountByDay", true, days);

		//		ICache cache = Caches.getCache(CacheConfig.TIME_TO_LIVE_MINUTE_5);
		//		Object obj = cache.get(cacheKey);
		//		if (obj != null) {
		//			return (List<Record>) obj;
		//		}
		//
		//		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_STAT).getSqlPara("stat.ipCountByDay");
		//		sqlPara.addPara(days);
		//		List<Record> list = Db.use(Const.Db.TIO_SITE_STAT).find(sqlPara);
		//
		//		if (list == null) {
		//			list = new ArrayList<>(0);
		//		}
		//
		//		cache.put(cacheKey, (Serializable) list);
		//		return list;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	/**
	 * -- 按省统计点击量
		---------------------------------------------
		--  hitcount | ipcount      |  province
		--  6565     |   545	    |  浙江省
		--  5443     |   434	    |  湖南省
		---------------------------------------------
	 * @param days
	 * @return
	 */
	public List<Record> statIpAndHitsByProvince(Integer days) {
		if (days == null) {
			days = 7;
		} else {
			if (days > MAX_STAT_DAY) {
				days = MAX_STAT_DAY;
			}
			if (days < 0) {
				days = 7;
			}
		}

		String cacheKey = "stat.statIpAndHitsByProvince_" + days;
		return QueryUtils.findWithCache(CacheConfig.TIME_TO_LIVE_MINUTE_5, cacheKey, Const.Db.TIO_SITE_STAT, "stat.statIpAndHitsByProvince", true, days);
	}

	/**
	 * 
	 * @param mergeRequest true:合并请求类型，即appType不分组，只按ip分组
	 * @param starttime
	 * @param endtime
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 * @author tanyaowu
	 */
	public Page<Record> ip(Boolean mergeRequest, String starttime, String endtime, Integer pageNumber, Integer pageSize) {
		Kv kv = Kv.by("starttime", starttime).set("endtime", endtime).set("mergeRequest", mergeRequest);
		Page<Record> page = Db.use(Const.Db.TIO_SITE_STAT).template("stat.ip", kv).paginate(pageNumber, pageSize);
		return page;
	}

	/**
	 * 注册统计
	 * @param user
	 * @return
	 * @author lixinji
	 * 2020年7月13日 下午2:10:54
	 */
	public void userRegisterStat(User user) {
		Integer threadid = StatService.me.threadInit(user.getId(), "", Const.ThreadLogType.REGISTER_STAT);
		Const.getBsExecutor().execute(new Runnable() {
			@Override
			public void run() {
				try {
					Date createtime = user.getCreatetime();
					if (createtime == null) {
						createtime = new Date();
					}
					String period = DateUtil.format(createtime, DatePattern.PURE_DATE_PATTERN);
					IpInfo ipInfo = user.getIpInfo();
					String ip = ipInfo.getIp();
					if (StrUtil.isBlank(ip)) {
						ip = "00000000";
					}
					UserRegisterStat timeStat = UserRegisterStat.dao.findFirst("SELECT * from user_register_stat where type = ? and statbizstr = ?", UserRegisterStat.Type.TIME,
					        period);
					if (timeStat == null) {
						timeStat = new UserRegisterStat();
						timeStat.setStatbizstr(period);
						timeStat.setType(UserRegisterStat.Type.TIME);
						timeStat.ignoreSave();
					}
					UserRegisterStat ipStat = UserRegisterStat.dao.findFirst("SELECT * from user_register_stat where type = ? and statbizstr = ?", UserRegisterStat.Type.IP, ip);
					if (ipStat == null) {
						ipStat = new UserRegisterStat();
						ipStat.setStatbizstr(ip);
						ipStat.setType(UserRegisterStat.Type.IP);
						ipStat.setStatbizid(user.getIpid());
						ipStat.ignoreSave();
					}
					UserRegisterStat ipTimeStat = UserRegisterStat.dao.findFirst("SELECT * from user_register_stat where type = ? and statbizstr = ? and statbizid = ? ",
					        UserRegisterStat.Type.IP_TIME, period, user.getIpid());
					if (ipTimeStat == null) {
						ipTimeStat = new UserRegisterStat();
						ipTimeStat.setStatbizstr(period);
						ipTimeStat.setType(UserRegisterStat.Type.IP_TIME);
						ipTimeStat.setStatbizid(user.getIpid());
						ipTimeStat.ignoreSave();
					}
					Db.use(Const.Db.TIO_SITE_STAT).update(
					        "update user_register_stat set regcount = regcount + 1 where (type = ? and statbizstr = ?) or (type = ? and statbizstr = ?) or (type = ? and statbizstr = ? and statbizid = ?) ",
					        UserRegisterStat.Type.TIME, period, UserRegisterStat.Type.IP, ip, UserRegisterStat.Type.IP_TIME, period, user.getIpid());
					String city = ipInfo.getCity();
					String province = ipInfo.getProvince();
					if (StrUtil.isBlank(city) || StrUtil.isBlank(province)) {
						return;
					}
					AreaStat stat = AreaStat.dao.findFirst("select * from area_stat where city = ? and province = ? and type = ?", city, province, Const.AreaStatType.REGISTER);
					if (stat == null) {
						stat = new AreaStat();
						stat.setCity(city);
						stat.setProvince(province);
						stat.setType(Const.AreaStatType.REGISTER);
						stat.setBizcount(1);
						stat.setCharindex(PyUtils.getAllChat(province));
						stat.setCitychatindex(PyUtils.getAllChat(city));
						stat.ignoreSave();//此处存在同步漏洞
					} else {
						Db.use(Const.Db.TIO_SITE_STAT).update("update area_stat set bizcount = bizcount + 1 where city = ? and province = ? and type = ?", city, province,
						        Const.AreaStatType.REGISTER);
					}
					StatService.me.threadDeal(threadid);
				} catch (Exception e) {
					log.error("", e);
				}
			}
		});

	}

	/**
	 * 人员统计初始化
	 * @author lixinji
	 * 2020年7月13日 下午2:55:18
	 */
	public void userRegisterStatInit() {
		Db.use(Const.Db.TIO_SITE_STAT).update("truncate table user_register_stat");
		Db.use(Const.Db.TIO_SITE_STAT).update("truncate table area_stat");
		List<User> users = User.dao.find("select * from `user` order by id");
		long start = System.currentTimeMillis();
		log.error(DateUtil.format(new Date(), DatePattern.NORM_DATETIME_PATTERN) + ",开始人员注册统计初始化：" + users.size());
		for (User user1 : users) {
			Date createtime = user1.getCreatetime();
			if (createtime == null) {
				log.error("用户的创建时间为空");
				continue;
			}
			String period = DateUtil.format(createtime, DatePattern.PURE_DATE_PATTERN);
			String ip = "";
			IpInfo ipInfo = IpInfoService.ME.getById(user1.getIpid());
			if (ipInfo == null || StrUtil.isBlank(ipInfo.getIp())) {
				ip = "00000000";
			} else {
				ip = ipInfo.getIp();
			}
			UserRegisterStat timeStat = UserRegisterStat.dao.findFirst("SELECT * from user_register_stat where type = ? and statbizstr = ?", UserRegisterStat.Type.TIME, period);
			if (timeStat == null) {
				timeStat = new UserRegisterStat();
				timeStat.setStatbizstr(period);
				timeStat.setType(UserRegisterStat.Type.TIME);
				timeStat.save();
			}
			UserRegisterStat ipStat = UserRegisterStat.dao.findFirst("SELECT * from user_register_stat where type = ? and statbizstr = ?", UserRegisterStat.Type.IP, ip);
			if (ipStat == null) {
				ipStat = new UserRegisterStat();
				ipStat.setStatbizstr(ip);
				ipStat.setType(UserRegisterStat.Type.IP);
				ipStat.setStatbizid(user1.getIpid());
				ipStat.save();
			}
			UserRegisterStat ipTimeStat = UserRegisterStat.dao.findFirst("SELECT * from user_register_stat where type = ? and statbizstr = ? and statbizid = ? ",
			        UserRegisterStat.Type.IP_TIME, period, user1.getIpid());
			if (ipTimeStat == null) {
				ipTimeStat = new UserRegisterStat();
				ipTimeStat.setStatbizstr(period);
				ipTimeStat.setType(UserRegisterStat.Type.IP_TIME);
				ipTimeStat.setStatbizid(user1.getIpid());
				ipTimeStat.ignoreSave();
			}
			Db.use(Const.Db.TIO_SITE_STAT).update(
			        "update user_register_stat set regcount = regcount + 1 where (type = ? and statbizstr = ?) or (type = ? and statbizstr = ?) or (type = ? and statbizstr = ? and statbizid = ?) ",
			        UserRegisterStat.Type.TIME, period, UserRegisterStat.Type.IP, ip, UserRegisterStat.Type.IP_TIME, period, user1.getIpid());
			if (ipInfo == null) {
				continue;
			}
			String city = ipInfo.getCity();
			String province = ipInfo.getProvince();
			if (StrUtil.isBlank(city) || StrUtil.isBlank(province)) {
				continue;
			}
			AreaStat stat = AreaStat.dao.findFirst("select * from area_stat where city = ? and province = ? and type = ?", city, province, Const.AreaStatType.REGISTER);
			if (stat == null) {
				stat = new AreaStat();
				stat.setCity(city);
				stat.setProvince(province);
				stat.setType(Const.AreaStatType.REGISTER);
				stat.setBizcount(1);
				stat.setCharindex(PyUtils.getAllChat(province));
				stat.setCitychatindex(PyUtils.getAllChat(city));
				stat.ignoreSave();//此处存在同步漏洞
			} else {
				Db.use(Const.Db.TIO_SITE_STAT).update("update area_stat set bizcount = bizcount + 1 where city = ? and province = ? and type = ?", city, province,
				        Const.AreaStatType.REGISTER);
			}
		}
		long end = System.currentTimeMillis();
		long exe = (end - start) / (60 * 1000);
		log.error(DateUtil.format(new Date(), DatePattern.NORM_DATETIME_PATTERN) + "结束人员注册统计初始化，总时间：{}", exe);
	}

	/**
	 * 初始化好友统计数据
	 * @author lixinji
	 * 2020年7月14日 下午3:55:32
	 */
	public void fdKeyInit() {
		Db.use(Const.Db.TIO_SITE_MAIN).update("truncate table wx_friend_meta");
		Db.use(Const.Db.TIO_SITE_MAIN).update("insert into wx_friend_meta(uid,touid,fidkey) select max(uid),min(touid),twouid from wx_friend_msg group by twouid ");
		Db.use(Const.Db.TIO_SITE_MAIN).update("insert IGNORE into wx_friend_meta(uid,touid,fidkey) select max(uid),min(touid),twouid from wx_friend_msg_bak group by twouid ");
		List<WxFriend> friends = WxFriend.dao.findAll();
		Map<String, String> existMap = new HashMap<String, String>();
		for (WxFriend friend : friends) {
			String fdkey = UserService.twoUid(friend.getUid(), friend.getFrienduid());
			if (StrUtil.isNotBlank(existMap.get(fdkey))) {
				continue;
			}
			WxFriendMeta meta = new WxFriendMeta();
			meta.setUid(friend.getUid());
			meta.setTouid(friend.getFrienduid());
			meta.setFidkey(fdkey);
			meta.ignoreSave();
		}
		Db.use(Const.Db.TIO_SITE_MAIN).update(
		        "update wx_friend_meta outtable set msgcount = (select count(1) from wx_friend_msg_bak intable where intable.twouid = outtable.fidkey),lastmsgid = (select max(intable.id) from wx_friend_msg_bak intable where intable.twouid = outtable.fidkey)");
		Db.use(Const.Db.TIO_SITE_MAIN).update(
		        "update wx_friend_meta outtable set msgcount = msgcount + (select count(1) from wx_friend_msg intable where intable.twouid = outtable.fidkey),lastmsgid = (select max(intable.id) from wx_friend_msg intable where intable.twouid = outtable.fidkey)");
	}

	/**
	 * 初始化线程执行日志表
	 * @param bizint
	 * @param bizstr
	 * @param type
	 * @return
	 * @author lixinji
	 * 2020年7月28日 下午1:54:52
	 */
	public Integer threadInit(Integer bizint, String bizstr, Short type) {
		if (bizint == null && StrUtil.isBlank(bizstr)) {
			log.error("线程执行统计时，主键信息都为空，type:{}", type);
			return null;
		}
		TioThreadLogs threadLogs = new TioThreadLogs();
		threadLogs.setBizint(bizint);
		threadLogs.setBizstr(bizstr);
		threadLogs.setStatus(Const.YesOrNo.NO);
		threadLogs.setType(type);
		threadLogs.save();
		return threadLogs.getId();
	}

	/**
	 * 线程执行完成
	 * @param id
	 * @author lixinji
	 * 2020年7月28日 下午1:56:12
	 */
	public void threadDeal(Integer id) {
		if (id == null) {
			log.error("重要提醒------------------------------>线程执行统计时，日志id为空");
			return;
		}
		TioThreadLogs threadLogs = new TioThreadLogs();
		threadLogs.setId(id);
		threadLogs.setStatus(Const.YesOrNo.YES);
		boolean update = threadLogs.update();
		if (!update) {
			log.error("重要提醒------------------------------>线程执行统计时，修改失败，id:{}", id);
		}
	}

}
