
package org.tio.mg.service.service.tioim;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Page;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.mg.service.model.stat.UserRegisterStat;
import org.tio.mg.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.mg.service.vo.MgConst;
import org.tio.utils.jfinal.P;

import cn.hutool.core.util.StrUtil;

/**
 * 统计管理
 * @author xufei
 * 2020年5月16日 下午2:21:06
 */
public class TioStatService {
	
	@SuppressWarnings("unused")
	private static Logger			log	= LoggerFactory.getLogger(TioStatService.class);
	
	public static final TioStatService	me	= new TioStatService();

	/**
	 * 用户统计数据
	 * TODO:XUFEI-未添加在线时长
	 * @param pageNumber
	 * @param pageSize
	 * @param searchkey
	 * @return
	 * @author xufei
	 * 2020年6月28日 下午3:33:25
	 */
	public Ret userStatList(Integer pageNumber, Integer pageSize, String searchkey) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageNumber <= 0) {
			pageSize = 16;
		}
		Kv params = Kv.create();
		if(StrUtil.isNotBlank(searchkey)) {
			params.set("searchkey", "%" + searchkey + "%");
		}
		boolean allowOper = P.getBoolean("oper.open.flag",true);
		if(!allowOper) {
			params.set("noemail", Const.YesOrNo.YES);
		} else {
			params.set("email", Const.YesOrNo.YES);
		}
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_STAT).getSqlPara("stat.userstatlist", params);
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_STAT).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(records);
	}
	
	/**
	 * 区域字典列表
	 * @return
	 * @author xufei
	 * 2020年7月23日 下午3:44:04
	 */
	public Ret areadict() {
		List<Record> records = Db.use(MgConst.Db.TIO_SITE_STAT).find("SELECT province,GROUP_CONCAT(city ORDER BY citychatindex SEPARATOR ',') citys FROM area_stat where type = 1 GROUP BY charindex,province");
		return RetUtils.okList(records);
	}
	
	
	/**
	 * 用户总登录次数
	 * @param uid
	 * @return
	 * @author xufei
	 * 2020年7月23日 下午4:20:28
	 */
	public Ret userLoginCount(Integer uid) {
		Integer count = Db.use(MgConst.Db.TIO_SITE_STAT).queryInt("select totalcount from user_time_login_stat where uid = ? and dayperiod = '-1'",uid);
		if(count == null || count < 0) {
			count = 0;
		}
		return RetUtils.okData(count);
	}
	
	/**
	 * ip小的注册人数
	 * @param ip
	 * @return
	 * @author xufei
	 * 2020年7月23日 下午4:39:39
	 */
	public Ret ipRegisterCount(String ip) {
		Integer count = Db.use(MgConst.Db.TIO_SITE_STAT).queryInt("select regcount from user_register_stat where type = 2 and statbizstr = ?",ip);
		if(count == null || count < 0) {
			count = 0;
		}
		return RetUtils.okData(count);
	}
	
	/**
	 * 区域注册人数
	 * @param province
	 * @param city
	 * @return
	 * @author xufei
	 * 2020年7月23日 下午4:40:55
	 */
	public Ret areaRegisterCount(String province,String city) {
		Integer count = Db.use(MgConst.Db.TIO_SITE_STAT).queryInt("select bizcount from area_stat where province = ? and city = ?",province,city);
		if(count == null || count < 0) {
			count = 0;
		}
		return RetUtils.okData(count);
	}
	
	/**
	 * 时间注册人数
	 * @param period
	 * @return
	 * @author xufei
	 * 2020年7月23日 下午4:45:52
	 */
	public Ret timeRegisterCount(String period) {
		Integer count = Db.use(MgConst.Db.TIO_SITE_STAT).queryInt("select regcount from user_register_stat where type = 1 and statbizstr = ?",period);
		if(count == null || count < 0) {
			count = 0;
		}
		return RetUtils.okData(count);
	}
	
	/**
	 * @param pageNumber
	 * @param pageSize
	 * @param searchkey
	 * @param status
	 * @return
	 * @author xufei
	 * 2020年7月13日 下午3:01:53
	 */
	public Ret userRegisterStat(Integer pageNumber, Integer pageSize, String start,String end,String searchip, Short type,String province,String city,String order) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageNumber <= 0) {
			pageSize = 16;
		}
		if(type == null) {
			return RetUtils.invalidParam();
		}
		Kv params = Kv.by("type", type);
		if(Objects.equals(type,UserRegisterStat.Type.TIME )) {
			params.set("time", Const.YesOrNo.YES);
		} else {
			params.set("ip", Const.YesOrNo.YES);
			if(StrUtil.isBlank(order)) {
				order = "regcount";
			}
			params.set("order", order);
			if(StrUtil.isNotBlank(province)) {
				params.set("province", province);
			}
			if(StrUtil.isNotBlank(city)) {
				params.set("city", city);
			}
		}
		if(StrUtil.isNotBlank(start)) {
			params.set("starttime", start);
		}
		if(StrUtil.isNotBlank(start)) {
			params.set("endtime", end);
		}
		if(StrUtil.isNotBlank(searchip)) {
			params.set("searchip", searchip);
		}
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_STAT).getSqlPara("stat.userRegistList", params);
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_STAT).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(records);
	}
	
	
	/**
	 * @param pageNumber
	 * @param pageSize
	 * @param start
	 * @param end
	 * @param searchip
	 * @param type
	 * @param province
	 * @param city
	 * @param order
	 * @return
	 * @author xufei
	 * 2020年7月27日 下午2:23:13
	 */
	public Ret groupStat(String starttime,String endtime,Integer pageNumber,Integer pageSize,Short type) {
		if(type == null) {
			return RetUtils.invalidParam();
		}
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageSize <= 0) {
			pageSize = 16;
		}
		Kv params = Kv.by("type", type);
		if(StrUtil.isNotBlank(starttime)) {
			params.set("starttime", starttime);
		}
		if(StrUtil.isNotBlank(endtime)) {
			params.set("endtime", endtime);
		}
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_STAT).getSqlPara("stat.groupstat", params);
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_STAT).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(records);
	}
	
	/**
	 * @param pageNumber
	 * @param pageSize
	 * @param ipid
	 * @param order
	 * @return
	 * @author xufei
	 * 2020年7月23日 下午5:00:28
	 */
	public Ret userIpTimeRegisterStat(Integer pageNumber, Integer pageSize,Integer ipid,String order) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageNumber <= 0) {
			pageSize = 16;
		}
		if(StrUtil.isBlank(order)) {
			order = "statbizstr";
		}
		Kv params = Kv.by("ipid", ipid).set("type", UserRegisterStat.Type.IP_TIME);
		if(StrUtil.isNotBlank(order)) {
			params.set("order", order);
		}
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_STAT).getSqlPara("stat.userIpTimeRegisterStat", params);
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_STAT).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(records);
	}
	
}
