
package org.tio.sitexxx.im.server.topic;

import java.util.Map;
import java.util.Objects;

import org.redisson.api.listener.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.sitexxx.im.server.handler.wx.WxChatApi;
import org.tio.sitexxx.service.vo.topic.ImManagerTopicVo;
import org.tio.sitexxx.service.vo.topic.TopicVo;

import cn.hutool.core.util.StrUtil;

/**
 * @author tanyaowu 2016年11月8日 下午4:05:52
 */
public class TopicImManagerListener implements MessageListener<ImManagerTopicVo> {
	private static Logger log = LoggerFactory.getLogger(TopicImManagerListener.class);

	public static TopicImManagerListener ME = new TopicImManagerListener();
	

	/**
	 * 
	 * @author tanyaowu
	 */
	private TopicImManagerListener() {
	}

	/**
	 * @param args
	 * @author tanyaowu
	 */
	public static void main(String[] args) {

	}

	@Override
	public void onMessage(CharSequence channel, ImManagerTopicVo msg) {
		String clientid = msg.getClientId();
		if (StrUtil.isBlank(clientid)) {
			log.error("clientid is null");
			return;
		}
		if (Objects.equals(TopicVo.CLIENTID, clientid)) {
			log.info("自己发布的消息,{}", clientid);
			return;
		}
		Short type = msg.getType();
		if (Objects.equals(type, ImManagerTopicVo.Type.GROUP_INBLACK_OPER)) {
			//群封停操作
			Map<String, Object> params = msg.getParams();
			Object groupid = params.get("groupid");
			Object status = params.get("status");
			if (groupid == null || status == null) {
				log.error("群封停操作消息异常：群id或者状态为空,groupid:{},status:{}", groupid, status);
				return;
			}
			WxChatApi.inblackNotify((Long) groupid, (Short) status);
		} 


	}
}
