
/**
 * 
 */
package org.tio.mg.web.server.http.stat.token;

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
import org.tio.http.server.stat.token.TokenAccessStat;
import org.tio.http.server.stat.token.TokenPathAccessStat;
import org.tio.mg.service.model.stat.TioIpPullblackLog;
import org.tio.mg.service.model.stat.TioTokenPathAccessStat;
import org.tio.mg.service.service.base.IpInfoService;
import org.tio.mg.service.service.base.TioTokenPathAccessStatService;
import org.tio.mg.service.service.conf.MgConfService;
import org.tio.mg.service.service.conf.IpWhiteListService;
import org.tio.sitexxx.service.vo.Const;
import org.tio.mg.web.server.http.WebApiHttpSessionListener;
import org.tio.mg.web.server.utils.TioIpPullblackUtils;
import org.tio.utils.json.Json;
import org.tio.utils.lock.MapWithLock;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @author tanyaowu
 *
 */
public class TioSiteTokenPathAccessStatListener implements org.tio.http.server.stat.token.TokenPathAccessStatListener {
	private static Logger log = LoggerFactory.getLogger(TioSiteTokenPathAccessStatListener.class);

	public static final TioSiteTokenPathAccessStatListener ME_SITE_VIEW = new TioSiteTokenPathAccessStatListener((short) 9);

	public static final TioSiteTokenPathAccessStatListener ME_SITE_API = new TioSiteTokenPathAccessStatListener((short) 8);

	private short appType = 9;

	/**
	 * 
	 * @param tokenAccessStat
	 * @return
	 * @author tanyaowu
	 */
	public TioTokenPathAccessStat toDbObj(TokenAccessStat tokenAccessStat) {
		TioTokenPathAccessStat ret = new TioTokenPathAccessStat();

		int requestCount = tokenAccessStat.count.get();
		long timeCost = tokenAccessStat.timeCost.get();
		long duration = tokenAccessStat.getDuration();

		String uidStr = tokenAccessStat.getUid();
		if (StrUtil.isNotBlank(uidStr)) {
			ret.setUid(Integer.parseInt(uidStr));
		}

		ret.setToken(tokenAccessStat.getToken());
		ret.setAppType(this.appType); //9：网站
		ret.setRequestCount(requestCount);
		ret.setTimeCost(timeCost);
		ret.setTimeCostPerRequest(((double) timeCost) / ((double) requestCount));
		ret.setDuration(duration);
		ret.setDurationType(tokenAccessStat.getDurationType());
		ret.setFirstAccessTime(new Date(tokenAccessStat.getFirstAccessTime()));
		ret.setFormatedDuration(tokenAccessStat.getFormatedDuration());
		ret.setIp(tokenAccessStat.getIp());
		ret.setIpid(IpInfoService.ME.save(tokenAccessStat.getIp()).getId());
		ret.setPath(null);
		ret.setRequestCountPerSecond(tokenAccessStat.getPerSecond());
		ret.setServer(Const.MY_IP);
		return ret;
	}

	/**
	 * 
	 * @param tokenPathAccessStat
	 * @return
	 * @author tanyaowu
	 */
	public TioTokenPathAccessStat toDbObj(TokenPathAccessStat tokenPathAccessStat) {
		TioTokenPathAccessStat ret = new TioTokenPathAccessStat();

		int requestCount = tokenPathAccessStat.count.get();
		long timeCost = tokenPathAccessStat.timeCost.get();

		long duration = tokenPathAccessStat.getDuration();

		String uidStr = tokenPathAccessStat.getUid();
		if (StrUtil.isNotBlank(uidStr)) {
			ret.setUid(Integer.parseInt(uidStr));
		}

		ret.setToken(tokenPathAccessStat.getToken());
		ret.setAppType(this.appType); //9：网站，此
		ret.setRequestCount(requestCount);
		ret.setTimeCost(timeCost);
		ret.setTimeCostPerRequest(((double) timeCost) / ((double) requestCount));
		ret.setDuration(duration);
		ret.setDurationType(tokenPathAccessStat.getDurationType());
		ret.setFirstAccessTime(new Date(tokenPathAccessStat.getFirstAccessTime()));
		ret.setFormatedDuration(tokenPathAccessStat.getFormatedDuration());
		ret.setIp(tokenPathAccessStat.getIp());
		ret.setIpid(IpInfoService.ME.save(tokenPathAccessStat.getIp()).getId());
		ret.setPath(tokenPathAccessStat.getPath());

		if (Objects.equals(appType, (short) 8)) {
			ret.setRestype("api");
		} else {
			String ext = FileUtil.extName(tokenPathAccessStat.getPath());
			if (StrUtil.isNotBlank(ext)) {
				ret.setRestype(ext);
			}
		}

		ret.setRequestCountPerSecond(tokenPathAccessStat.getPerSecond());
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
	 * 
	 */
	public TioSiteTokenPathAccessStatListener(short appType) {
		this.appType = appType;
	}

	@Override
	public void onExpired(TioConfig tioConfig, String token, TokenAccessStat tokenAccessStat) {
		if (Objects.equals(tokenAccessStat.getDurationType(), Const.TokenPathAccessStatDuration.DURATION_2)) {
			TioTokenPathAccessStat tioTokenAccessStat = toDbObj(tokenAccessStat);
			TioTokenPathAccessStatService.ME.save(tioTokenAccessStat);

			MapWithLock<String, TokenPathAccessStat> tokenPathAccessStatMap = tokenAccessStat.getTokenPathAccessStatMap();
			if (tokenPathAccessStatMap != null) {
				ReadLock readLock = tokenPathAccessStatMap.readLock();
				readLock.lock();
				try {
					Map<String, TokenPathAccessStat> map = tokenPathAccessStatMap.getObj();
					if (map != null) {
						Set<Entry<String, TokenPathAccessStat>> set = map.entrySet();
						if (set.size() > 0) {
							List<TioTokenPathAccessStat> modelList = new ArrayList<>();
							for (Entry<String, TokenPathAccessStat> entry : set) {
								//						String path = entry.getKey();
								TioTokenPathAccessStat tioTokenPathAccessStat = null;
								try {
									TokenPathAccessStat tokenPathAccessStat = entry.getValue();
									String path = tokenPathAccessStat.getPath();
									String ext = FileUtil.extName(path);
									if (skipExtSet.contains(ext)) {
										continue;
									}

									tioTokenPathAccessStat = toDbObj(tokenPathAccessStat);
									modelList.add(tioTokenPathAccessStat);
									//									TioTokenPathAccessStatService.ME.save(tioTokenPathAccessStat);
								} catch (Exception e) {
									log.error(Json.toFormatedJson(tioTokenPathAccessStat), e);
								}
							}
							TioTokenPathAccessStatService.ME.batchSave(modelList);
						}

					}
				} catch (Throwable e) {
					log.error(e.toString(), e);
				} finally {
					readLock.unlock();
				}
			}

			if (log.isDebugEnabled()) {
				log.debug("token:{}, \r\ntokenAccessStat:{} ", token, Json.toFormatedJson(tokenAccessStat));
			}
		}

	}

	/**
	 * 
	 */
	//	private static long allcount = 1;

	private static final String CONF_PREFIX = "token.access.";

	private static final int STEP = MgConfService.getInt(CONF_PREFIX + "STEP", 200);

	private static final int	G1_MAX_COUNT		= MgConfService.getInt(CONF_PREFIX + "G1_MAX_COUNT", 200);
	private static final int	G1_MAX_PER_SECOND	= MgConfService.getInt(CONF_PREFIX + "G1_MAX_PER_SECOND", 5);

	private static final int	G2_MAX_COUNT		= MgConfService.getInt(CONF_PREFIX + "G2_MAX_COUNT", 100);
	private static final int	G2_MAX_PER_SECOND	= MgConfService.getInt(CONF_PREFIX + "G2_MAX_PER_SECOND", 10);

	@Override
	public boolean onChanged(HttpRequest httpRequest, String token, String path, TokenAccessStat tokenAccessStat, TokenPathAccessStat tokenPathAccessStat) {
		if (IpWhiteListService.isWhiteIp(httpRequest.getClientIp())) {
			return true;
		}

		int allcount = tokenAccessStat.count.get();

		if (allcount % STEP != 0) {
			return true;
		}

		log.debug("token:{}, path:{}", token, path);
		String remark = null;
		String ip1 = tokenAccessStat.getIp();
		String ip = httpRequest.getClientIp();
		if (!StrUtil.equals(ip1, ip)) {
			//			remark = "token访问违反条件组1：总访问次数" + allcount + "( > " + G1_MAX_COUNT + "), 平均每秒访问" + preSecond + "次 (>" + G1_MAX_PER_SECOND + ")";
			//			TioSiteIpPathAccessStatListener.pullBlack(httpRequest, ip, path, remark);
			//			return false;
		}

		double preSecond = tokenAccessStat.getPerSecond();

		//		int noSessionCount = tokenAccessStat.noSessionCount.get();

		//		//某IP不带session的最多次，默认1000
		//		int MAX_NO_SESSION_COUNT = ConfService.getInt(CONF_PREFIX + "MAX_NO_SESSION_COUNT", 1000);
		//
		//		//某IP带不同值sessionid的最多次，默认1000
		//		int MAX_SESSION_COUNT = ConfService.getInt(CONF_PREFIX + "MAX_SESSION_COUNT", 1000);

		//		if (noSessionCount > MAX_NO_SESSION_COUNT) {
		//			remark = "不带cookie的请求次数达到上限[" + MAX_NO_SESSION_COUNT + "]";
		//			pullBlack(httpRequest, token, path, remark);
		//			return false;
		//		}
		//
		//		if (tokenAccessStat.sessionIds.size() > MAX_SESSION_COUNT) {
		//			remark = "不同sessionid的数量达到上限[" + MAX_SESSION_COUNT + "]";
		//			pullBlack(httpRequest, token, path, remark);
		//			return false;
		//		}

		boolean g1 = false;
		boolean g2 = false;

		boolean g1_c1 = allcount > G1_MAX_COUNT; //总访问次数大于该值
		boolean g1_c2 = preSecond > G1_MAX_PER_SECOND; //平均每秒访问次数大于该值
		g1 = g1_c1 && g1_c2;
		if (g1) {
			remark = "token【" + token + "】访问违反条件组1：总访问次数" + allcount + "( > " + G1_MAX_COUNT + "), 平均每秒访问" + preSecond + "次 (>" + G1_MAX_PER_SECOND + ")，拉黑时访问的地址是：【" + path + "】";
			pullBlack(httpRequest, tokenAccessStat, tokenPathAccessStat, remark);
			return false;
		}

		boolean g2_c1 = allcount > G2_MAX_COUNT; //总访问次数大于该值
		boolean g2_c2 = preSecond > G2_MAX_PER_SECOND; //平均每秒访问次数大于该值
		g2 = g2_c1 && g2_c2;
		if (g2) {
			remark = "token【" + token + "】访问违反条件组2：总访问次数" + allcount + "( > " + G2_MAX_COUNT + "), 平均每秒访问" + preSecond + "次 (>" + G2_MAX_PER_SECOND + ")，拉黑时访问的地址是：【" + path + "】";
			pullBlack(httpRequest, tokenAccessStat, tokenPathAccessStat, remark);
			return false;
		}

		String sessionId = DefaultHttpRequestHandler.getSessionId(httpRequest);
		if (StrUtil.isNotBlank(sessionId)) {
			boolean isOk = WebApiHttpSessionListener.isValidSessionId(sessionId);
			if (!isOk) {
				//				remark = "token访问违反条件：非法cookie【" + cookie.getValue() + "】";
				//				TioSiteIpPathAccessStatListener.pullBlack(httpRequest, ip, path, remark);
				//				return false;
			}
		}

		return true;
		//		log.info("token:{}, path:{}, \r\ntokenAccessStat:{}, \r\ntokenPathAccessStat:{}", token, path, Json.toFormatedJson(tokenAccessStat), Json.toFormatedJson(tokenPathAccessStat));
	}

	/**
	 * 拉黑ip
	 * @param request
	 * @param tokenAccessStat
	 * @param tokenPathAccessStat
	 * @param remark
	 */
	public void pullBlack(HttpRequest request, TokenAccessStat tokenAccessStat, TokenPathAccessStat tokenPathAccessStat, String remark) {
		TioTokenPathAccessStat tioTokenAccessStat = toDbObj(tokenAccessStat);
		TioTokenPathAccessStatService.ME.save(tioTokenAccessStat);

		//		Integer currId = WebUtils.currUserId(request);
		//		
		//		String ip = request.getClientIp();
		//		
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
		//		TioIpPullblackLogService.ME.addToBlack(tioIpPullblackLog);

		TioIpPullblackUtils.addToBlack(request, request.getClientIp(), remark, TioIpPullblackLog.Type.HTTP_REQUEST_TOO_FREQUENTLY);

		request.close();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	public short getAppType() {
		return appType;
	}

	public void setAppType(short appType) {
		this.appType = appType;
	}

}
