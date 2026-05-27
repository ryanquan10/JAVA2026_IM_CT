
package org.tio.sitexxx.service.service.base;

import org.redisson.api.RTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.sitexxx.service.init.RedisInit;
import org.tio.sitexxx.service.model.stat.TioIpPullblackLog;
import org.tio.sitexxx.service.service.conf.IpBlackListService;
import org.tio.sitexxx.service.service.conf.IpWhiteListService;
import org.tio.sitexxx.service.vo.Const.Topic;
import org.tio.sitexxx.service.vo.topic.PullIpToBlackVo;

/**
 * @author tanyaowu 
 * 2016年10月30日 下午5:00:28
 */
public class TioIpPullblackLogService {
	private static Logger							log	= LoggerFactory.getLogger(TioIpPullblackLogService.class);
	public static final TioIpPullblackLogService	ME	= new TioIpPullblackLogService();

	/**
	 * 
	 * @author tanyaowu
	 */
	public TioIpPullblackLogService() {
	}

	/**
	 * 
	 * @param tioIpPullblackLog
	 * @return
	 * @author tanyaowu
	 */
	private boolean save(TioIpPullblackLog tioIpPullblackLog) {
		if (tioIpPullblackLog != null) {
			boolean ret = tioIpPullblackLog.save();
			return ret;
		}
		return false;
	}

	/**
	 * 把ip拉黑
	 * @param ip 要被拉黑的客户端ip
	 * @param serverport 服务器port
	 * @param remark
	 */
	public void addToBlack(TioIpPullblackLog tioIpPullblackLog) {
		String ip = tioIpPullblackLog.getIp();
		int serverport = tioIpPullblackLog.getServerport();
		String remark = tioIpPullblackLog.getRemark();

		boolean isWhiteIp = IpWhiteListService.isWhiteIp(ip);
		if (isWhiteIp) {
			log.warn("ip:[{}]是白名单，不允许拉黑。本次拉黑serverport:{}, 原因:{}", ip, serverport, remark);
			return;
		}

		log.warn("ip:{}将被拉黑", ip);

		PullIpToBlackVo pullIpToBlackVo = new PullIpToBlackVo();
		pullIpToBlackVo.setIp(ip);
		pullIpToBlackVo.setRemark(remark);
		RTopic pullIpToBlackTopic = RedisInit.get().getTopic(Topic.PULL_IP_TO_BLACK);
		pullIpToBlackTopic.publish(pullIpToBlackVo);

		TioIpPullblackLogService.ME.save(tioIpPullblackLog);
	}

	public void deleteFromBlack(String ip, int serverport, String remark) {
		log.warn("ip:{}从黑名单中删除", ip);

		PullIpToBlackVo pullIpToBlackVo = new PullIpToBlackVo();
		pullIpToBlackVo.setIp(ip);
		pullIpToBlackVo.setRemark(remark);
		pullIpToBlackVo.setType(PullIpToBlackVo.Type.DELETE_IP_FROM_BLACK);
		RTopic pullIpToBlackTopic = RedisInit.get().getTopic(Topic.PULL_IP_TO_BLACK);
		pullIpToBlackTopic.publish(pullIpToBlackVo);

		//		TioIpPullblackLog tioIpPullblackLog = new TioIpPullblackLog();
		//		tioIpPullblackLog.setIp(ip);
		//		tioIpPullblackLog.setIpid(IpInfoService.ME.save(ip).getId());
		//		tioIpPullblackLog.setRemark(remark);
		//		tioIpPullblackLog.setServer(org.tio.sitexxx.service.Const.SERVICE_HOST);
		//		tioIpPullblackLog.setServerport(serverport);
		//		tioIpPullblackLog.setTime(new Date());
		//		tioIpPullblackLog.setType(TioIpPullblackLog.Type.HTTP_REQUEST_TOO_FREQUENTLY);
		//
		//		TioIpPullblackLogService.ME.save(tioIpPullblackLog);

		IpBlackListService.me.delete(ip, remark);
	}

}
