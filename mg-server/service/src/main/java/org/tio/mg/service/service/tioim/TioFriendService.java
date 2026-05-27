
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
 * im好友管理
 * @author xufei
 * 2020年5月16日 下午2:21:06
 */
public class TioFriendService {
	
	@SuppressWarnings("unused")
	private static Logger			log	= LoggerFactory.getLogger(TioFriendService.class);
	
	public static final TioFriendService	me	= new TioFriendService();
	
	
	
	/**
	 * 查询消息模型下的好友列表
	 * @param pageNumber
	 * @param pageSize
	 * @param searchkey
	 * @param starttime
	 * @param endtime
	 * @param type
	 * @return
	 * @author xufei
	 * 2020年7月14日 下午4:52:43
	 */
	public Ret fdlist(Integer pageNumber, Integer pageSize,String searchkey,String starttime,String endtime,Short type, String content) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageSize <= 0) {
			pageSize = 16;
		}
		String sqlMode = "fdStatList";
		if(!Objects.equals(type, Const.Status.NORMAL)) {
			sqlMode = "fdBakStatList";
		} 
		Kv params = Kv.create();
		if(StrUtil.isNotBlank(starttime)) {
			params.set("starttime", starttime);
		}
		if(StrUtil.isNotBlank(endtime)) {
			params.set("endtime", endtime);
		}
		if(StrUtil.isNotBlank(searchkey)) {
			params.set("searchkey", "%" + searchkey + "%");
			params.set("searchid", searchkey);
		}

		if(StrUtil.isNotBlank(content)) {
			params.set("content", "%" +content+"%");
		}
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("friend." + sqlMode, params);
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(records);
	}
}
