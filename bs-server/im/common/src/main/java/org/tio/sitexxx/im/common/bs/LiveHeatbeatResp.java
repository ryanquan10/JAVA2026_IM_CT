
/**
 *
 */
package org.tio.sitexxx.im.common.bs;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 直播心跳响应
 * @author tanyaowu 
 * 2016年11月3日 上午9:42:15
 */
public class LiveHeatbeatResp implements Serializable {
	private static final long	serialVersionUID	= -963061070964363738L;
	@SuppressWarnings("unused")
	private static Logger		log					= LoggerFactory.getLogger(LiveHeatbeatResp.class);

	public static interface Result {
		/**
		 * 更新心跳成功
		 */
		int	OK			= 1;
		/**
		 * 并没有在直播
		 */
		int	NOT_LIVE	= 2;

		/**
		 * 心跳已经停止了
		 */
		int NO_HEARTBEAT = 3;
	}

	private Integer result = 1;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	/**
	 *
	 */
	public LiveHeatbeatResp() {

	}

	/**
	 * @return the result
	 */
	public Integer getResult() {
		return result;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(Integer result) {
		this.result = result;
	}

}
