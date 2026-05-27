
package org.tio.sitexxx.im.server;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.server.TioServerConfig;
import org.tio.sitexxx.im.common.Command;
import org.tio.sitexxx.im.common.ImPacket;
import org.tio.sitexxx.im.common.ImSessionContext;
import org.tio.sitexxx.im.common.bs.wx.WxHandshakeResp;
import org.tio.sitexxx.im.server.utils.ImUtils;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.Devicetype;
import org.tio.sitexxx.service.vo.GroupStat;
import org.tio.sitexxx.service.vo.LoadData;
import org.tio.utils.cache.CacheUtils;
import org.tio.utils.cache.FirsthandCreater;
import org.tio.utils.cache.ICache;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;

/**
 * @author tanyaowu 
 * 2016年9月8日 下午1:35:02
 */
public class Ims {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(Ims.class);

	/**
	 * 
	 * @author: tanyaowu
	 */
	public Ims() {
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Date date = new Date();

		int hour = DateUtil.hour(date, true);
		int step = ROBOT_HOUR_COUNT_MAP.get(hour);
		System.out.println(step);
	}

	/**
	 * 
	 * @param channelContext
	 * @param imPacket
	 */
	public static void send(ChannelContext channelContext, ImPacket imPacket) {
		Tio.send(channelContext, imPacket);
	}

	/**
	 * 发送握手包
	 * @param channelContext
	 * @author tanyaowu
	 */
	public static void sendHandshake(ChannelContext channelContext) {
		ImSessionContext imSessionContext = ImUtils.getImSessionContext(channelContext);
		ImPacket handshakeRespPacket = null;
		if (imSessionContext.isWx()) {
			WxHandshakeResp handshakeResp = new WxHandshakeResp();
			handshakeResp.setCid(channelContext.getId());
			handshakeResp.setIp(channelContext.getClientNode().getIp());
			handshakeRespPacket = new ImPacket(Command.WxHandshakeResp, handshakeResp);
		} else {
			Tio.remove(channelContext, "不支持非wx握手");
			return;
		}
		
		Ims.send(channelContext, handshakeRespPacket);
	}

	/**
	 * 
	 * @param groupid
	 * @param imPacket
	 */
	public static void sendToGroup(String groupid, ImPacket imPacket) {
		Tio.sendToGroup(TioSiteImServerStarter.tioServerConfigWs, groupid, imPacket);
	}
	
	/**
	 * 
	 * @param groupid
	 * @param imPacket
	 */
	public static void sendToGroup(Long groupid, ImPacket imPacket) {
		Tio.sendToGroup(TioSiteImServerStarter.tioServerConfigWs, groupid.toString(), imPacket);
	}

	/**
	 * 
	 * @param userid
	 * @param imPacket
	 */
	public static void sendToUser(String userid, ImPacket imPacket) {
		Tio.sendToUser(TioSiteImServerStarter.tioServerConfigWs, userid, imPacket);
	}

	/**
	 * 
	 * @param userid
	 * @param imPacket
	 */
	public static void sendToUser(Integer userid, ImPacket imPacket) {
		sendToUser("" + userid, imPacket);
	}

	/**
	 * 
	 * @param group
	 * @param channelContext
	 * @return
	 */
	public static boolean isInGroup(String group, ChannelContext channelContext) {
		return Tio.isInGroup(group, channelContext);
	}

	/**
	 * 
	 * @param token
	 * @param imPacket
	 */
	public static void sendToToken(String token, ImPacket imPacket) {
		Tio.sendToToken(TioSiteImServerStarter.tioServerConfigWs, token, imPacket);
	}

	/**
	 * 获取当前有多少ip数，包含机器
	 * @return
	 */
	public static int getIpCount() {
		return TioSiteImServerStarter.tioServerConfigWs.ips.getIpmap().size();
	}
	
	/**
	 * 
	 * @param ip
	 * @param imPacket
	 */
	public static void sendToIp(String ip, ImPacket imPacket) {
		Tio.sendToIp(TioSiteImServerStarter.tioServerConfigWs, ip, imPacket);
	}

	/**
	 * 发送到指定的channelContextId
	 * @param channelContextId
	 * @param imPacket
	 */
	public static void sendToId(String channelContextId, ImPacket imPacket) {
		Tio.sendToId(TioSiteImServerStarter.tioServerConfigWs, channelContextId, imPacket);
	}

	/**
	 * 某群组当前有多少人（真实）
	 * @param group 对应用户的id
	 * @return
	 */
	public static int groupCount(String group) {
		int c1 = Tio.groupCount(TioSiteImServerStarter.tioServerConfigWs, group);
		//		int c2 = Tio.groupCount(TioSiteImServerStarter.tioServerConfigIos, group);
		//		int c3 = Tio.groupCount(TioSiteImServerStarter.tioServerConfigWs, group);
		return c1;// + c3;
	}

	public static ChannelContext getByCid(String cid) {
		return Tio.getByChannelContextId(TioSiteImServerStarter.tioServerConfigWs, cid);
	}

	/**
	 * 某群组有多少个设备为指定设备的用户
	 * @param devicetype
	 * @param group
	 * @return
	 */
	public static int groupCount(Devicetype devicetype, String group) {
		int c1 = Tio.groupCount(TioSiteImServerStarter.tioServerConfigWs, group);
		return c1;
	}

	/**
	 * 根据设备类型获取TioServerConfig
	 * 因为share了，所以实际已经不用分别获取TioServerConfig了
	 * @param devicetype
	 * @return
	 */
	public static TioServerConfig getTioServerConfig(Devicetype devicetype) {
		if (devicetype == Devicetype.WEB) {
			return TioSiteImServerStarter.tioServerConfigWs;
		} else if (devicetype == Devicetype.ANDROID || devicetype == Devicetype.IOS) {
			return TioSiteImServerStarter.tioServerConfigApp;
		}
		//		else if (devicetype == Devicetype.IOS) {
		//			return TioSiteImServerStarter.tioServerConfigIos;
		//		}

		return null;
	}

	/**
	 * 
	 * @param imPacket
	 * @author: tanyaowu
	 */
	public static void sendToAll(ImPacket imPacket) {
		Tio.sendToAll(TioSiteImServerStarter.tioServerConfigWs, imPacket);
	}

	/**
	 * 
	 * @return
	 * @author: tanyaowu
	 */
	public static LoadData createLoadData() {
		LoadData loadData = new LoadData();
		loadData.setPcCount(TioSiteImServerStarter.tioServerConfigWs.connections.size());
		//		loadData.setAppCount(TioSiteImServerStarter.tioServerConfigApp.connections.size());
		//		loadData.setIosCount(TioSiteImServerStarter.tioServerConfigIos.connections.size());
		return loadData;
	}

	/**
	 * 
	 * @param group
	 * @return
	 */
	public static GroupStat createGroupStat(String group) {
		int maxRealOnlineInCache = 0; //在缓存中的在线峰值（真实的）
		int maxCalcOnlineInCache = 0; //在缓存中的在线峰值（计算出来的）

		ICache groupStatCache = Caches.getCache(CacheConfig.GROUP_STAT);
		GroupStat groupStat = groupStatCache.get(group, GroupStat.class);
		if (groupStat != null) {
			maxRealOnlineInCache = groupStat.getMaxRealOnline();
			maxCalcOnlineInCache = groupStat.getMaxCalcOnline();
		}
		groupStat = Ims.createGroupStat1(group);

		//		int realCount = Ims.groupCount(group); //真实人数
		//		int calcCount = RobotUtils.calcGroupCount(realCount); //计算出来的人数
		groupStat.setMaxRealOnline(Math.max(maxRealOnlineInCache, groupStat.getMaxRealOnline()));
		groupStat.setMaxCalcOnline(Math.max(maxCalcOnlineInCache, groupStat.getMaxCalcOnline()));

		groupStatCache.put(group, groupStat);

		return groupStat;
	}

	static final int	multiple4Real	= 1;//200;          //真实用户的倍数
	static final int	multiple4Robot	= 1;//100;         //机器人的倍数

	static final Map<Integer, Integer> ROBOT_HOUR_COUNT_MAP = new HashMap<>();
	static {
		ROBOT_HOUR_COUNT_MAP.put(0, 10);
		ROBOT_HOUR_COUNT_MAP.put(1, 10);
		ROBOT_HOUR_COUNT_MAP.put(2, 10);
		ROBOT_HOUR_COUNT_MAP.put(3, 10);
		ROBOT_HOUR_COUNT_MAP.put(4, 10);
		ROBOT_HOUR_COUNT_MAP.put(5, 10);
		ROBOT_HOUR_COUNT_MAP.put(6, 10);
		ROBOT_HOUR_COUNT_MAP.put(7, 30);
		ROBOT_HOUR_COUNT_MAP.put(8, 30);
		ROBOT_HOUR_COUNT_MAP.put(9, 40);
		ROBOT_HOUR_COUNT_MAP.put(10, 40);
		ROBOT_HOUR_COUNT_MAP.put(11, 40);
		ROBOT_HOUR_COUNT_MAP.put(12, 40);
		ROBOT_HOUR_COUNT_MAP.put(13, 40);
		ROBOT_HOUR_COUNT_MAP.put(14, 40);
		ROBOT_HOUR_COUNT_MAP.put(15, 40);
		ROBOT_HOUR_COUNT_MAP.put(16, 40);
		ROBOT_HOUR_COUNT_MAP.put(17, 40);
		ROBOT_HOUR_COUNT_MAP.put(18, 40);
		ROBOT_HOUR_COUNT_MAP.put(19, 40);
		ROBOT_HOUR_COUNT_MAP.put(20, 40);
		ROBOT_HOUR_COUNT_MAP.put(21, 40);
		ROBOT_HOUR_COUNT_MAP.put(22, 30);
		ROBOT_HOUR_COUNT_MAP.put(23, 30);
		ROBOT_HOUR_COUNT_MAP.put(24, 10);
	}

	/**
	 * 
	 * @param group
	 * @return
	 */
	private static GroupStat createGroupStat1(String group) {
		GroupStat groupStat = new GroupStat();

		//		RobotJoinRoomRunnable roomRobotRunnable = RobotJoinRoomRunnable.getInstance(Integer.parseInt(group), null);
		//		int robotCount = roomRobotRunnable.uidChannelMap.getObj().size();

		//		Tio.bindGroup(channelContext, group);
		//		Tio.bindGroup(channelContext, Const.ImGroupType.REAL + group);
		//		Tio.bindGroup(channelContext, Const.ImGroupType.ALL + devicetype + group);
		//		Tio.bindGroup(channelContext, Const.ImGroupType.REAL + devicetype + group);

		//PC在线人数，含机器人
		int pcOnlineWithRobot = Ims.groupCount(Devicetype.WEB, Const.ImGroupType.ALL + Devicetype.WEB + group);
		//PC在线人数，不含机器人
		int realPcOnline = Ims.groupCount(Devicetype.WEB, Const.ImGroupType.REAL + Devicetype.WEB + group);

		//安卓在线人数，含机器人
		int androidOnlineWithRobot = Ims.groupCount(Devicetype.ANDROID, Const.ImGroupType.ALL + Devicetype.ANDROID + group);
		//安卓在线人数，不含机器人
		int realAndroidOnline = Ims.groupCount(Devicetype.ANDROID, Const.ImGroupType.REAL + Devicetype.ANDROID + group);

		//IOS在线人数，含机器人
		int iosOnlineWithRobot = Ims.groupCount(Devicetype.IOS, Const.ImGroupType.ALL + Devicetype.IOS + group);
		//IOS在线人数，不含机器人
		int realIosOnline = Ims.groupCount(Devicetype.IOS, Const.ImGroupType.REAL + Devicetype.IOS + group);

		//总在线人数，含机器人
		int countWithRobot = pcOnlineWithRobot + androidOnlineWithRobot + iosOnlineWithRobot;
		//总在线人数，不含机器人
		int realCount = realPcOnline + realAndroidOnline + realIosOnline;

		int online = Ims.groupCount(Devicetype.WEB, group);

		//计算出来的人数
		int calcOnline = realPcOnline * multiple4Real + realAndroidOnline * multiple4Real + realIosOnline * multiple4Real + (countWithRobot - realCount) * multiple4Robot;
		//		calcOnline += RandomUtils.nextInt(1, 100);

		calcOnline = online; //现在全部是真实的，所以就这样写

		ICache cache = Caches.getCache(CacheConfig.TIME_TO_LIVE_MINUTE_5_LOCAL);
		String key = "LIVESTAT.XX";
		Integer step = CacheUtils.get(cache, key, true, new FirsthandCreater<Integer>() {
			@Override
			public Integer create() {
				Date date = new Date();

				int hour = DateUtil.hour(date, true);
				int step = ROBOT_HOUR_COUNT_MAP.get(hour);
				//				step = 0;

				Week week = DateUtil.dayOfWeekEnum(date);
				if (week == Week.SUNDAY || week == Week.SATURDAY) {
					step = step - 5;
				}
				if (step < 0) {
					step = 0;
				}
				return step;
			}
		});

		calcOnline += step;

		int maxRealOnline = realCount;
		int maxCalcOnline = calcOnline;

		groupStat.setPcOnline(realPcOnline);
		groupStat.setAndroidOnline(realAndroidOnline);
		groupStat.setCalcOnline(calcOnline);
		groupStat.setIosOnline(realIosOnline);
		groupStat.setMaxCalcOnline(maxCalcOnline);
		groupStat.setMaxRealOnline(maxRealOnline);

		return groupStat;
	}

}
