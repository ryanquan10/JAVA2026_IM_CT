
/**
 * 
 */
package org.tio.sitexxx.view.task;

import org.quartz.JobExecutionContext;
import org.tio.sitexxx.service.service.conf.ConfService;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.view.WebViewStarter;
import org.tio.utils.quartz.AbstractJobWithLog;

/**
 * @author tanyaowu
 *
 */
public class UpdateModelTask extends AbstractJobWithLog {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	@Override
	public void run(JobExecutionContext context) throws Exception {
		String title = ConfService.getString("seo.title", "t-io - 让天下没有难开发的网络通讯, 单机不仅仅是支持30万长连接");
		String keywords = ConfService.getString("seo.keywords", "t-io,tio,开源,netty,mina,rpc,jfinal,layui,hutool,osc,io,socket,tcp,nio,aio,nio2,im,游戏,java,长连接");
		String description = ConfService.getString("seo.description",
		        "t-io/tio是一个网络编程框架，也可以叫TCP长连接框架，从这一点来说是有点像netty的，但t-io为常见和网络相关的业务（如IM、消息推送、RPC、监控）提供了近乎于现成的解决方案，即丰富的编程API，极大减少业务层的编程难度");

		if (WebViewStarter.mapModel != null) {
			WebViewStarter.mapModel.put(Const.ModelKey.TITLE, title);
			WebViewStarter.mapModel.put(Const.ModelKey.KEYWORDS, keywords);
			WebViewStarter.mapModel.put(Const.ModelKey.DESCRIPTION, description);
		}
	}

}
