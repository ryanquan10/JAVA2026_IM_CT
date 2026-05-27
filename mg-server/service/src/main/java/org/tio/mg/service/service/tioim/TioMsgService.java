
package org.tio.mg.service.service.tioim;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Page;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.mg.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.mg.service.vo.MgConst;

import cn.hutool.core.util.StrUtil;

/**
 * im消息管理
 * @author xufei
 * 2020年5月16日 下午2:21:06
 */
public class TioMsgService {
	
	@SuppressWarnings("unused")
	private static Logger			log	= LoggerFactory.getLogger(TioMsgService.class);
	
	public static final TioMsgService	me	= new TioMsgService();

	/**
	 * 近期私聊消息列表
	 * @param pageNumber
	 * @param pageSize
	 * @param searchkey
	 * @param contenttype
	 * @param starttime
	 * @param endtime
	 * @return
	 * @author xufei
	 * 2020年6月29日 上午11:50:29
	 */
	public Ret p2pList(Integer pageNumber, Integer pageSize,String fidkey, String contenttype,String starttime,String endtime,Short type,String searchkey) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageSize <= 0) {
			pageSize = 16;
		}
		String sqlMode = "modeP2PMsgList";
		if(!Objects.equals(type, Const.Status.NORMAL)) {
			sqlMode = "bakModeP2PMsgList";
		}
		Kv params = Kv.by("fidkey", fidkey);
		if(contenttype != null) {
			params.set("contenttype", "(" + contenttype + ")");
		} 
		if(StrUtil.isNotBlank(starttime)) {
			params.set("starttime", starttime);
		}
		if(StrUtil.isNotBlank(endtime)) {
			params.set("endtime", endtime);
		}
		if (StrUtil.isNotBlank(searchkey)) {
			params.set("searchkey","%" + searchkey + "%");
		}
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("wxmsg." + sqlMode, params);
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(records);
	}
	
	/**
	 * 群聊模型聊天列表
	 * @param pageNumber
	 * @param pageSize
	 * @param groupid
	 * @param type
	 * @return
	 * @author xufei
	 * 2020年7月8日 下午5:27:02
	 */
	public Ret groupList(Integer pageNumber, Integer pageSize, Long groupid,Short type, String searchkey) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageSize <= 0) {
			pageSize = 16;
		}
		String sqlMode = "groupModeMsgList";
		if(!Objects.equals(type, Const.Status.NORMAL)) {
			sqlMode = "bakGroupModeMsgList";
		}
		Kv params = Kv.by("groupid", groupid);

		if (StrUtil.isNotBlank(searchkey)) {
			params.set("searchkey", "%"+searchkey+"%");
		}
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("wxmsg." + sqlMode, params);
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(records);
	}
	
}
