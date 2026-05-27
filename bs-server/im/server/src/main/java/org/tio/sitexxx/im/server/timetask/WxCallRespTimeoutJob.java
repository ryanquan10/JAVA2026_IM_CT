
package org.tio.sitexxx.im.server.timetask;

import java.util.List;

import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.sitexxx.im.server.handler.wx.call.WxCallUtils;
import org.tio.sitexxx.service.model.conf.ClientConf;
import org.tio.sitexxx.service.model.main.WxCallItem;
import org.tio.utils.quartz.AbstractJobWithLog;

import cn.hutool.core.collection.CollectionUtil;

/**
 * 音视频通话，响应超时检查任务
 * @author tanyaowu
 */
public class WxCallRespTimeoutJob extends AbstractJobWithLog {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(WxCallRespTimeoutJob.class);

	@Override
	public void run(JobExecutionContext context) throws Exception {
		check();
	}

	private static final String sql = "select * from wx_call_item where (status = ? or status = ?) and calltime < date_sub(now(), interval 40 second)";
	private static final String sqlexp = "select * from wx_call_item where hanguptype = ? and calltime < date_sub(now(), interval 24 HOUR)";
	private static final String sql2 = "select * from wx_call_item where status = ? and calltime < date_sub(now(), interval 40 second)";

	public static void check() {
//		ClientConf clientConf = ClientConf.dao.findFirst("select * from client_conf where name = 'isOpenAgora'");
//		List<WxCallItem> list;
//		if (clientConf.getValue().equals(1)) {
//			list = WxCallItem.dao.find(sql2, WxCallItem.Status.BEGIN_CALLING);
//		} else {
//			list = WxCallItem.dao.find(sql, WxCallItem.Status.BEGIN_CALLING,WxCallItem.Status.TCP_CONNECTED);
//		}
		List<WxCallItem> list = WxCallItem.dao.find(sql, WxCallItem.Status.BEGIN_CALLING,WxCallItem.Status.TCP_CONNECTED);;
		if (CollectionUtil.isNotEmpty(list)) {
			for (WxCallItem wxCallItem : list) {
				WxCallUtils.endCall(null, wxCallItem.getId(), null, WxCallItem.Hanguptype.RESP_TIMEOUT);
			}
		}
		List<WxCallItem> listexp = WxCallItem.dao.find(sqlexp, WxCallItem.Hanguptype.NOT_HANGUP);
		if (CollectionUtil.isNotEmpty(listexp)) {
			for (WxCallItem wxCallItem : listexp) {
				WxCallUtils.endCall(null, wxCallItem.getId(), null, WxCallItem.Hanguptype.TCP_DROPPED);
			}
		}
	}
}
