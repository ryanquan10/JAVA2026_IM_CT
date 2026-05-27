
package org.tio.mg.service.service.tioim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Page;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.mg.service.utils.RetUtils;
import org.tio.mg.service.vo.MgConst;
import org.tio.sitexxx.service.vo.Const;
import org.tio.utils.jfinal.P;

import cn.hutool.core.util.StrUtil;

/**
 * app红包管理
 * @author xufei
 * 2020年5月16日 下午2:21:06
 */
public class TioRedService {
	
	@SuppressWarnings("unused")
	private static Logger			log	= LoggerFactory.getLogger(TioRedService.class);
	
	public static final TioRedService	me	= new TioRedService();
	
	
	/**
	 * 开户列表
	 * @param pageNumber
	 * @param pageSize
	 * @param searchkey 用户查询信息
	 * @param status
	 * @return
	 * @author xufei
	 * 2021年4月12日 下午4:48:40
	 */
	public Ret openList(Integer pageNumber, Integer pageSize, String searchkey, String walletid) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageNumber <= 0) {
			pageSize = 16;
		}
		Kv params = Kv.create();
		if(StrUtil.isNotBlank(searchkey)) {
			params.set("searchkey", "%" + searchkey + "%");
			params.set("searchid", searchkey);
		}
		if(StrUtil.isNotBlank(walletid)) {
			params.set("walletid", walletid);
		} 
		boolean allowOper = P.getBoolean("oper.open.flag",true);
		if(!allowOper) {
			params.set("noemail", Const.YesOrNo.YES);
		} else {
			params.set("email", Const.YesOrNo.YES);
		}
		SqlPara sqlPara;
		if (P.getInt("pay.mode", 1) == 2) {
			sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("tiored.openlistlocal", params);
		} else {
			sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("tiored.openlist", params);
		}
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(records);
	}
	
	/**
	 * 红包列表
	 * @param pageNumber
	 * @param pageSize
	 * @param searchkey
	 * @param orderno
	 * @return
	 * @author xufei
	 * 2021年4月12日 下午4:59:27
	 */
	public Ret redList(Integer pageNumber, Integer pageSize, String searchkey, String orderno) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageNumber <= 0) {
			pageSize = 16;
		}
		Kv params = Kv.create();
		if(StrUtil.isNotBlank(searchkey)) {
			params.set("searchkey", "%" + searchkey + "%");
			params.set("searchid", searchkey);
		}
		if(StrUtil.isNotBlank(orderno)) {
			params.set("orderno", orderno);
		} 
		boolean allowOper = P.getBoolean("oper.open.flag",true);
		if(!allowOper) {
			params.set("noemail", Const.YesOrNo.YES);
		} else {
			params.set("email", Const.YesOrNo.YES);
		}

		SqlPara sqlPara;
		if (P.getInt("pay.mode", 1) == 2) {
			sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("tiored.redlistlocal", params);
		} else {
			sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("tiored.redlist", params);
		}
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(records);
	}
	
	
	/**
	 * 提现列表
	 * @param pageNumber
	 * @param pageSize
	 * @param searchkey
	 * @param orderno
	 * @param status
	 * @return
	 * @author xufei
	 * 2021年4月12日 下午4:59:37
	 */
	public Ret withholdList(Integer pageNumber, Integer pageSize, String searchkey, String orderno,Short status) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageNumber <= 0) {
			pageSize = 16;
		}
		Kv params = Kv.create();
		if(StrUtil.isNotBlank(searchkey)) {
			params.set("searchkey", "%" + searchkey + "%");
			params.set("searchid", searchkey);
		}
		if(StrUtil.isNotBlank(orderno)) {
			params.set("orderno", orderno);
		} 
		if(status != null) {
			params.set("status", status);
		}
		boolean allowOper = P.getBoolean("oper.open.flag",true);
		if(!allowOper) {
			params.set("noemail", Const.YesOrNo.YES);
		} else {
			params.set("email", Const.YesOrNo.YES);
		}
		SqlPara sqlPara;
		if (P.getInt("pay.mode", 1) == 2) {
			sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("tiored.withholdlistlocal", params);
		} else {
			sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("tiored.withholdlist", params);
		}
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(records);
	}
	
	/**
	 * 充值列表
	 * @param pageNumber
	 * @param pageSize
	 * @param searchkey
	 * @param orderno
	 * @param status
	 * @return
	 * @author xufei
	 * 2021年4月12日 下午4:56:39
	 */
	public Ret rechargeList(Integer pageNumber, Integer pageSize, String searchkey, String orderno,Short status) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageNumber <= 0) {
			pageSize = 16;
		}
		Kv params = Kv.create();
		if(StrUtil.isNotBlank(searchkey)) {
			params.set("searchkey", "%" + searchkey + "%");
			params.set("searchid", searchkey);
		}
		if(StrUtil.isNotBlank(orderno)) {
			params.set("orderno", orderno);
		} 
		if(status != null) {
			params.set("status", status);
		}
		boolean allowOper = P.getBoolean("oper.open.flag",true);
		if(!allowOper) {
			params.set("noemail", Const.YesOrNo.YES);
		} else {
			params.set("email", Const.YesOrNo.YES);
		}

		SqlPara sqlPara;
		if (P.getInt("pay.mode", 1) == 2) {
			sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("tiored.rechargelistlocal", params);
		} else {
			sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("tiored.rechargelist", params);
		}
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(records);
	}
	
	/**
	 * 钱包明细
	 * @param pageNumber
	 * @param pageSize
	 * @param searchkey
	 * @param orderno
	 * @param status
	 * @return
	 * @author xufei
	 * 2021年4月12日 下午5:17:22
	 */
	public Ret coinList(Integer pageNumber, Integer pageSize, Integer uid) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageNumber <= 0) {
			pageSize = 16;
		}
		Kv params = Kv.create();
		params.set("uid", uid);
		boolean allowOper = P.getBoolean("oper.open.flag",true);
		if(!allowOper) {
			params.set("noemail", Const.YesOrNo.YES);
		} else {
			params.set("email", Const.YesOrNo.YES);
		}
		SqlPara sqlPara;
		if (P.getInt("pay.mode", 1) == 2) {
			sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("tiored.coinlistlocal", params);
		} else {
			sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("tiored.coinlist", params);
		}
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(records);
	}

	public Ret grabRedList(Integer pageNumber, Integer pageSize) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageNumber <= 0) {
			pageSize = 16;
		}
		SqlPara sqlPara;
		if (P.getInt("pay.mode", 1) == 2) {
			sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("tiored.redlistlocal");
		} else {
			sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("tiored.redlist");
		}
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(records);
	}
}
