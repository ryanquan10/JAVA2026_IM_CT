
package org.tio.mg.service.topic;

import java.util.Objects;

import org.redisson.api.listener.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.mg.service.service.base.UserService;
import org.tio.mg.service.service.conf.IpWhiteListService;
import org.tio.mg.service.service.conf.MgConfService;
import org.tio.mg.service.service.conf.MgDictService;
import org.tio.sitexxx.service.vo.topic.TopicVo;

import cn.hutool.core.util.StrUtil;

/**
 * @author tanyaowu 
 * 2016年11月8日 下午4:05:52
 */
public class TopicCommonListener implements MessageListener<TopicVo> {
	private static Logger log = LoggerFactory.getLogger(TopicCommonListener.class);

	public static TopicCommonListener ME = new TopicCommonListener();

	/**
	 * 
	 * @author tanyaowu
	 */
	private TopicCommonListener() {
	}

	/**
	 * @param args
	 * @author tanyaowu
	 */
	public static void main(String[] args) {

	}

	@Override
	public void onMessage(CharSequence channel, TopicVo msg) {
		System.out.println();
		String clientid = msg.getClientId();
		if (StrUtil.isBlank(clientid)) {
			log.error("clientid is null");
			return;
		}
		if (!msg.isForceRun() &&  Objects.equals(TopicVo.CLIENTID, clientid)) {
			log.info("自己发布的消息,{}", clientid);
			return;
		}
		Short type = msg.getType();
		if (Objects.equals(type, TopicVo.Type.CLEAR_DICT)) {
			//清空字典信息
			MgDictService.clearDict();
		} else if (Objects.equals(type, TopicVo.Type.CLEAR_CONF)) {
			//清空系统参数
			MgConfService.clearCache();
		} else if (Objects.equals(type, TopicVo.Type.CLEAR_USER)) {
			//清空用户缓存，后台修改用户相关信息
			Integer uid = (Integer) msg.getValue();
			UserService.ME._clearCache(uid);
		}  else if (Objects.equals(type, TopicVo.Type.CLEAR_IP_WHITE_LIST)) {
			//ip白名单缓存清空
			IpWhiteListService.clearCache();
		}
	}
}
