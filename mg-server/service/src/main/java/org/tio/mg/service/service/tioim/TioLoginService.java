
package org.tio.mg.service.service.tioim;

import java.util.List;

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
import org.tio.utils.jfinal.P;

import cn.hutool.core.util.StrUtil;

/**
 * 钛信登录日志管理
 * @author xufei
 * 2020年5月16日 下午2:21:06
 */
public class TioLoginService {
	
	@SuppressWarnings("unused")
	private static Logger			log	= LoggerFactory.getLogger(TioLoginService.class);
	
	public static final TioLoginService	me	= new TioLoginService();

	/**
	 * 登录日志
	 * @param pageNumber
	 * @param pageSize
	 * @param searchkey
	 * @param ip
	 * @param starttime
	 * @param endtime
	 * @return
	 * @author xufei
	 * 2020年7月16日 下午1:51:57
	 */
	public Ret loginList(Integer pageNumber, Integer pageSize, String searchkey,String ip,String starttime,String endtime) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageNumber <= 0) {
			pageSize = 16;
		}
		Kv params = Kv.create();
		if(StrUtil.isNotBlank(starttime)) {
			params.set("starttime", starttime);
		}
		if(StrUtil.isNotBlank(endtime)) {
			params.set("endtime", endtime);
		}
		if(StrUtil.isNotBlank(ip)) {
			params.set("searchip", ip);
		}
		if(StrUtil.isNotBlank(searchkey)) {
			params.set("searchkey", "%" + searchkey + "%");
			params.set("searchid", searchkey);
		}
		boolean allowOper = P.getBoolean("oper.open.flag",true);
		if(!allowOper) {
			params.set("noemail", Const.YesOrNo.YES);
		} else {
			params.set("email", Const.YesOrNo.YES);
		}
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("tiologin.list", params);
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(records);
	}
	
	
	/**
	 * 统计日志--天
	 * @param pageNumber
	 * @param pageSize
	 * @param starttime
	 * @param endtime
	 * @return
	 * @author xufei
	 * 2020年7月16日 下午3:23:24
	 */
	public Ret statTimeList(Integer pageNumber, Integer pageSize,String starttime,String endtime) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageNumber <= 0) {
			pageSize = 16;
		}
		Kv params = Kv.create();
		if(StrUtil.isNotBlank(starttime)) {
			params.set("starttime", starttime);
		}
		if(StrUtil.isNotBlank(endtime)) {
			params.set("endtime", endtime);
		}
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_STAT).getSqlPara("tiologin.statTimeList", params);
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_STAT).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(records);
	}
	
	
	/**
	 * 统计日志--天--用户
	 * @param period
	 * @return
	 * @author xufei
	 * 2020年7月16日 下午3:25:46
	 */
	public Ret statTimeUserList(Integer pageNumber,Integer pageSize,String period) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageNumber <= 0) {
			pageSize = 16;
		}
		if(StrUtil.isBlank(period)) {
			return RetUtils.invalidParam();
		}
		Kv params = Kv.by("dayperiod", period);
		boolean allowOper = P.getBoolean("oper.open.flag",true);
		if(!allowOper) {
			params.set("noemail", Const.YesOrNo.YES);
		} else {
			params.set("email", Const.YesOrNo.YES);
		}
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_STAT).getSqlPara("tiologin.statTimeUserList", params);
		Page<Record> list = Db.use(MgConst.Db.TIO_SITE_STAT).paginate(pageNumber, pageSize,sqlPara);
		return RetUtils.okPage(list);
	}
	
	/**
	 * 统计日志--天--日志
	 * @param period
	 * @param uid
	 * @return
	 * @author xufei
	 * 2020年7月16日 下午3:26:34
	 */
	public Ret statTimeLoginList(String period,Integer uid) {
		if(StrUtil.isBlank(period) || uid == null) {
			return RetUtils.invalidParam();
		}
		Kv params = Kv.by("dayperiod", period).set("uid",uid);
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("tiologin.statTimeLoginList", params);
		List<Record> list = Db.use(MgConst.Db.TIO_SITE_MAIN).find(sqlPara);
		return RetUtils.okList(list);
	}
	
	
	
	/**
	 * 统计日志--Ip
	 * @param pageNumber
	 * @param pageSize
	 * @param starttime
	 * @param endtime
	 * @return
	 * @author xufei
	 * 2020年7月16日 下午3:23:24
	 */
	public Ret statIpList(Integer pageNumber, Integer pageSize,String ip,String order) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageNumber <= 0) {
			pageSize = 16;
		}
		Kv params = Kv.create();
		if(StrUtil.isNotBlank(ip)) {
			params.set("ip", ip);
		}
		if(StrUtil.isBlank(order)) {
			params.set("order","usercount");
		} else { 
			params.set("order",order);
		}
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_STAT).getSqlPara("tiologin.statIpList", params);
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_STAT).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(records);
	}
	
	/**
	 * 统计日志--Ip--天
	 * @param ip
	 * @return
	 * @author xufei
	 * 2020年7月16日 下午4:27:57
	 */
	public Ret statIpDayList(Integer pageNumber,Integer pageSize,String ip,String order) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageNumber <= 0) {
			pageSize = 16;
		}
		if(StrUtil.isBlank(ip)) {
			return RetUtils.invalidParam();
		}
		Kv params = Kv.by("ip", ip);
		if(StrUtil.isBlank(order)) {
			params.set("order","usercount");
		} else { 
			params.set("order",order);
		}
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_STAT).getSqlPara("tiologin.statIpDayList", params);
		Page<Record> list = Db.use(MgConst.Db.TIO_SITE_STAT).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(list);
	}
	
	
	/**
	 * 统计日志--Ip--用户
	 * @param period
	 * @return
	 * @author xufei
	 * 2020年7月16日 下午3:25:46
	 */
	public Ret statIpUserList(Integer pageNumber,Integer pageSize,String period,String ip) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageNumber <= 0) {
			pageSize = 16;
		}
		if(StrUtil.isBlank(period)) {
			return RetUtils.invalidParam();
		}
		Kv params = Kv.by("dayperiod", period).set("ip",ip);
		boolean allowOper = P.getBoolean("oper.open.flag",true);
		if(!allowOper) {
			params.set("noemail", Const.YesOrNo.YES);
		} else {
			params.set("email", Const.YesOrNo.YES);
		}
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_STAT).getSqlPara("tiologin.statIpUserList", params);
		Page<Record> list = Db.use(MgConst.Db.TIO_SITE_STAT).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(list);
	}
	
	/**
	 * 统计日志--Ip--用户--日志
	 * @param period
	 * @param uid
	 * @return
	 * @author xufei
	 * 2020年7月16日 下午3:26:34
	 */
	public Ret statIpLoginList(String period,Integer uid,String ip) {
		if(StrUtil.isBlank(period) || uid == null) {
			return RetUtils.invalidParam();
		}
		Kv params = Kv.by("dayperiod", period).set("uid",uid).set("ip",ip);
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("tiologin.statIpLoginList", params);
		List<Record> list = Db.use(MgConst.Db.TIO_SITE_MAIN).find(sqlPara);
		return RetUtils.okList(list);
	}
	
}
