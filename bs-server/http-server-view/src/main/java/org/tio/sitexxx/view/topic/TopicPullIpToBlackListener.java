
/**
 * 
 */
package org.tio.sitexxx.view.topic;

import java.util.Objects;

import org.redisson.api.listener.MessageListener;
import org.tio.core.Tio;
import org.tio.sitexxx.service.vo.topic.PullIpToBlackVo;
import org.tio.sitexxx.view.http.WebViewInit;

import cn.hutool.core.util.StrUtil;

/**
 * @author tanyaowu
 *
 */
public class TopicPullIpToBlackListener implements MessageListener<PullIpToBlackVo> {
	public static final TopicPullIpToBlackListener me = new TopicPullIpToBlackListener();

	/**
	 * 
	 */
	private TopicPullIpToBlackListener() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

	@Override
	public void onMessage(CharSequence channel, PullIpToBlackVo pullIpToBlackVo) {
		String ip = pullIpToBlackVo.getIp();
		if (StrUtil.isNotBlank(ip)) {
			if (Objects.equals(pullIpToBlackVo.getType(), PullIpToBlackVo.Type.ADD_BLACK_IP)) {
				Tio.IpBlacklist.add(WebViewInit.tioServerConfig, ip);
			} else {
				Tio.IpBlacklist.remove(WebViewInit.tioServerConfig, ip);
			}

		}
	}

}
