
package org.tio.mg.web.server.http.stat.ip;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.TioConfig;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.handler.DefaultHttpRequestHandler;
import org.tio.http.server.stat.ip.path.IpAccessStat;
import org.tio.http.server.stat.ip.path.IpPathAccessStat;
import org.tio.http.server.stat.ip.path.IpPathAccessStatListener;
import org.tio.mg.service.model.stat.TioIpPathAccessStat;
import org.tio.mg.service.model.stat.TioIpPullblackLog;
import org.tio.mg.service.service.base.IpInfoService;
import org.tio.mg.service.service.base.TioIpPathAccessStatService;
import org.tio.mg.service.service.conf.MgConfService;
import org.tio.mg.service.service.conf.IpWhiteListService;
import org.tio.sitexxx.service.vo.Const;
import org.tio.mg.web.server.http.WebApiHttpSessionListener;
import org.tio.mg.web.server.utils.TioIpPullblackUtils;
import org.tio.utils.hutool.StrUtil;
import org.tio.utils.json.Json;
import org.tio.utils.lock.MapWithLock;

import cn.hutool.core.io.FileUtil;

/**
 * 
 * @author tanyaowu 
 * 2016年10月30日 下午1:19:04
 */
public class TioSiteIpPathAccessStatListener implements IpPathAccessStatListener {
	private static Logger log = LoggerFactory.getLogger(TioSiteIpPathAccessStatListener.class);

	public static final TioSiteIpPathAccessStatListener ME_SITE_VIEW = new TioSiteIpPathAccessStatListener((short) 9);

	public static final TioSiteIpPathAccessStatListener ME_SITE_API = new TioSiteIpPathAccessStatListener((short) 8);

	private short appType = 9;

	/**
	 * 
	 * @author tanyaowu
	 */
	public TioSiteIpPathAccessStatListener(short appType) {
		this.appType = appType;
	}

	/**
	 * 
	 * @param ipAccessStat
	 * @return
	 * @author tanyaowu
	 */
	public TioIpPathAccessStat toDbObj(IpAccessStat ipAccessStat) {
		TioIpPathAccessStat ret = new TioIpPathAccessStat();

		int requestCount = ipAccessStat.count.get();
		long timeCost = ipAccessStat.timeCost.get();
		long duration = ipAccessStat.getDuration();

		ret.setAppType(this.appType); //9：网站
		ret.setRequestCount(requestCount);
		ret.setTimeCost(timeCost);
		ret.setTimeCostPerRequest(((double) timeCost) / ((double) requestCount));
		ret.setDuration(duration);
		ret.setDurationType(ipAccessStat.getDurationType());
		ret.setFirstAccessTime(new Date(ipAccessStat.getFirstAccessTime()));
		ret.setFormatedDuration(ipAccessStat.getFormatedDuration());
		ret.setIp(ipAccessStat.getIp());
		ret.setIpid(IpInfoService.ME.save(ipAccessStat.getIp()).getId());
		ret.setNoSessionCount(ipAccessStat.noSessionCount.get());
		ret.setPath(null);
		ret.setRequestCountPerSecond(ipAccessStat.getPerSecond());
		ret.setServer(Const.MY_IP);
		return ret;
	}

	/**
	 * 
	 * @param ipPathAccessStat
	 * @return
	 * @author tanyaowu
	 */
	public TioIpPathAccessStat toDbObj(IpPathAccessStat ipPathAccessStat) {
		TioIpPathAccessStat ret = new TioIpPathAccessStat();

		int requestCount = ipPathAccessStat.count.get();
		long timeCost = ipPathAccessStat.timeCost.get();

		long duration = ipPathAccessStat.getDuration();

		ret.setAppType(this.appType); //9：网站，此
		ret.setRequestCount(requestCount);
		ret.setTimeCost(timeCost);
		ret.setTimeCostPerRequest(((double) timeCost) / ((double) requestCount));
		ret.setDuration(duration);
		ret.setDurationType(ipPathAccessStat.getDurationType());
		ret.setFirstAccessTime(new Date(ipPathAccessStat.getFirstAccessTime()));
		ret.setFormatedDuration(ipPathAccessStat.getFormatedDuration());
		ret.setIp(ipPathAccessStat.getIp());
		ret.setIpid(IpInfoService.ME.save(ipPathAccessStat.getIp()).getId());
		ret.setNoSessionCount(ipPathAccessStat.noSessionCount.get());
		ret.setPath(ipPathAccessStat.getPath());

		if (Objects.equals(appType, (short) 8)) {
			ret.setRestype("api");
		} else {
			String ext = FileUtil.extName(ipPathAccessStat.getPath());
			if (StrUtil.isNotBlank(ext)) {
				ret.setRestype(ext);
			}
		}

		ret.setRequestCountPerSecond(ipPathAccessStat.getPerSecond());
		ret.setServer(Const.MY_IP);
		return ret;
	}

	private static Set<String> skipExtSet = new HashSet<>();

	static {
		skipExtSet.add("css");
		skipExtSet.add("js");
		skipExtSet.add("ico");
		skipExtSet.add("png");
		skipExtSet.add("jpg");
		skipExtSet.add("swf");
		skipExtSet.add("xml");
		skipExtSet.add("gif");
		skipExtSet.add("jpeg");
		skipExtSet.add("woff");
		skipExtSet.add("map");
		skipExtSet.add("txt");

		skipExtSet.add("mp4");
		skipExtSet.add("m3u8");
		skipExtSet.add("svg");
	}

	/** 
	 * @param tioConfig
	 * @param ip
	 * @param ipAccessStat
	 * @author tanyaowu
	 */
	@Override
	public void onExpired(TioConfig tioConfig, String ip, IpAccessStat ipAccessStat) {
		if (Objects.equals(ipAccessStat.getDurationType(), Const.IpPathAccessStatDuration.DURATION_2)) {
			TioIpPathAccessStat tioIpAccessStat = toDbObj(ipAccessStat);
			TioIpPathAccessStatService.ME.save(tioIpAccessStat);

			MapWithLock<String, IpPathAccessStat> ipPathAccessStatMap = ipAccessStat.getIpPathAccessStatMap();
			if (ipPathAccessStatMap != null) {
				ReadLock readLock = ipPathAccessStatMap.readLock();
				readLock.lock();
				try {
					Map<String, IpPathAccessStat> map = ipPathAccessStatMap.getObj();
					if (map != null) {
						Set<Entry<String, IpPathAccessStat>> set = map.entrySet();
						if (set.size() > 0) {
							List<TioIpPathAccessStat> modelList = new ArrayList<>();
							for (Entry<String, IpPathAccessStat> entry : set) {
								TioIpPathAccessStat tioIpPathAccessStat = null;
								try {
									//						String path = entry.getKey();
									IpPathAccessStat ipPathAccessStat = entry.getValue();
									String path = ipPathAccessStat.getPath();
									String ext = FileUtil.extName(path);
									if (skipExtSet.contains(ext)) {
										continue;
									}

									tioIpPathAccessStat = toDbObj(ipPathAccessStat);
									modelList.add(tioIpPathAccessStat);
									//									TioIpPathAccessStatService.ME.save(tioIpPathAccessStat);
								} catch (Exception e) {
									log.error(Json.toFormatedJson(tioIpPathAccessStat), e);
								}
							}

							TioIpPathAccessStatService.ME.batchSave(modelList);
						}

					}
				} catch (Throwable e) {
					log.error(e.toString(), e);
				} finally {
					readLock.unlock();
				}
			}

			if (log.isDebugEnabled()) {
				log.debug("ip:{}, \r\nipAccessStat:{} ", ip, Json.toFormatedJson(ipAccessStat));
			}
		}
	}

	private static final String CONF_PREFIX = "ip.access.";

	private static final int STEP = MgConfService.getInt(CONF_PREFIX + "STEP", 200);

	//某IP不带session的最多次，默认1000
	private static final int MAX_NO_SESSION_COUNT = MgConfService.getInt(CONF_PREFIX + "MAX_NO_SESSION_COUNT", 1000);

	//某IP带不同值sessionid的最多次，默认1000
	private static final int MAX_SESSION_COUNT = MgConfService.getInt(CONF_PREFIX + "MAX_SESSION_COUNT", 1000);

	private static final int	G1_MAX_COUNT		= MgConfService.getInt(CONF_PREFIX + "G1_MAX_COUNT", 3000);
	private static final int	G1_MAX_PER_SECOND	= MgConfService.getInt(CONF_PREFIX + "G1_MAX_PER_SECOND", 5);

	private static final int	G2_MAX_COUNT		= MgConfService.getInt(CONF_PREFIX + "G2_MAX_COUNT", 1000);
	private static final int	G2_MAX_PER_SECOND	= MgConfService.getInt(CONF_PREFIX + "G2_MAX_PER_SECOND", 50);

	/** 
	 * @param httpRequest
	 * @param ip
	 * @param path
	 * @param ipAccessStat
	 * @param ipPathAccessStat
	 * @author tanyaowu
	 */
	@Override
	public boolean onChanged(HttpRequest httpRequest, String ip, String path, IpAccessStat ipAccessStat, IpPathAccessStat ipPathAccessStat) {
		if (IpWhiteListService.isWhiteIp(ip)) {
			return true;
		}

		//		int pathcount = ipPathAccessStat.count.get();

		int allcount = ipAccessStat.count.get();

		if (allcount % STEP != 0) {
			return true;
		}

		log.debug("ip:{}, path:{}", ip, path);

		double preSecond = ipAccessStat.getPerSecond();

		int noSessionCount = ipAccessStat.noSessionCount.get();

		String remark = null;

		if (noSessionCount > MAX_NO_SESSION_COUNT) {
			remark = "不带cookie的请求次数达到上限[" + MAX_NO_SESSION_COUNT + "]";
			pullBlack(httpRequest, ipAccessStat, ipPathAccessStat, remark);
			return false;
		}

		if (ipAccessStat.sessionIds.size() > MAX_SESSION_COUNT) {
			remark = "ip访问违反条件：不同sessionid的数量达到上限[" + MAX_SESSION_COUNT + "]";
			pullBlack(httpRequest, ipAccessStat, ipPathAccessStat, remark);
			return false;
		}

		boolean g1 = false;
		boolean g2 = false;

		boolean g1_c1 = allcount > G1_MAX_COUNT; //总访问次数大于该值
		boolean g1_c2 = preSecond > G1_MAX_PER_SECOND; //平均每秒访问次数大于该值
		g1 = g1_c1 && g1_c2;
		if (g1) {
			remark = "ip访问违反条件组1：总访问次数" + allcount + "(> " + G1_MAX_COUNT + "), 平均每秒访问" + preSecond + "次 (>" + G1_MAX_PER_SECOND + ")";
			pullBlack(httpRequest, ipAccessStat, ipPathAccessStat, remark);
			return false;
		}

		boolean g2_c1 = allcount > G2_MAX_COUNT; //总访问次数大于该值
		boolean g2_c2 = preSecond > G2_MAX_PER_SECOND; //平均每秒访问次数大于该值
		g2 = g2_c1 && g2_c2;
		if (g2) {
			remark = "ip访问违反条件组2：总访问次数" + allcount + "(> " + G2_MAX_COUNT + "), 平均每秒访问" + preSecond + "次 (>" + G2_MAX_PER_SECOND + ")";
			//			pullBlack(httpRequest, ip, path, remark);
			pullBlack(httpRequest, ipAccessStat, ipPathAccessStat, remark);
			return false;
		}

		String sessionId = DefaultHttpRequestHandler.getSessionId(httpRequest);//.getSessionCookie(httpRequest, httpRequest.getHttpConfig());
		if (StrUtil.isNotBlank(sessionId)) {
			boolean isOk = WebApiHttpSessionListener.isValidSessionId(sessionId);
			if (!isOk) {
				//				remark = "ip访问违反条件：非法cookie【" + cookie.getValue() + "】";
				//				pullBlack(httpRequest, ip, path, remark);
				//				return false;
			}
		}

		return true;
		//		log.info("ip:{}, path:{}, \r\nipAccessStat:{}, \r\nipPathAccessStat:{}", ip, path, Json.toFormatedJson(ipAccessStat), Json.toFormatedJson(ipPathAccessStat));
	}

	/**
	 * 拉黑ip
	 * @param request
	 * @param ipAccessStat
	 * @param ipPathAccessStat
	 * @param remark
	 */
	public void pullBlack(HttpRequest request, IpAccessStat ipAccessStat, IpPathAccessStat ipPathAccessStat, String remark) {
		TioIpPathAccessStat tioIpAccessStat = toDbObj(ipAccessStat);
		TioIpPathAccessStatService.ME.save(tioIpAccessStat);

		//		Integer currId = WebUtils.currUserId(request);
		//		
		//		String ip = request.getClientIp();
		//		TioIpPullblackLog tioIpPullblackLog = new TioIpPullblackLog();
		//		tioIpPullblackLog.setIp(ip);
		//		tioIpPullblackLog.setIpid(IpInfoService.ME.save(ip).getId());
		//		tioIpPullblackLog.setRemark(remark);
		//		tioIpPullblackLog.setServer(org.tio.mg.service.Const.SERVICE_HOST);
		//		tioIpPullblackLog.setServerport(request.getChannelContext().getServerNode().getPort());
		//		tioIpPullblackLog.setTime(new Date());
		//		tioIpPullblackLog.setType(TioIpPullblackLog.Type.HTTP_REQUEST_TOO_FREQUENTLY);
		//		
		//		tioIpPullblackLog.setSessionid(request.getHttpSession().getId());
		//		tioIpPullblackLog.setCookie(request.getHeader(HttpConst.RequestHeaderKey.Cookie));
		//		tioIpPullblackLog.setInitpath(request.requestLine.getInitPath());
		//		tioIpPullblackLog.setPath(request.requestLine.getPath());
		//		tioIpPullblackLog.setRequestline(request.requestLine.toString());
		//		tioIpPullblackLog.setUid(currId);
		//		
		//		
		//		TioIpPullblackLogService.ME.addToBlack(tioIpPullblackLog);

		TioIpPullblackUtils.addToBlack(request, request.getClientIp(), remark, TioIpPullblackLog.Type.HTTP_REQUEST_TOO_FREQUENTLY);

		request.close();
	}

	/**
	 * @param args
	 * @author tanyaowu
	 */
	public static void main(String[] args) {
		System.out.println(3 % 6);
		System.out.println(6 % 3);

	}
}
