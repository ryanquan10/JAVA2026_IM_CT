
package org.tio.mg.service.service.tioim;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Page;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.mg.service.init.RedisInit;
import org.tio.mg.service.model.main.WxGroup;
import org.tio.mg.service.model.main.WxGroupInblack;
import org.tio.mg.service.model.main.WxUserReport;
import org.tio.mg.service.model.mg.MgUser;
import org.tio.mg.service.service.atom.AbsTxAtom;
import org.tio.mg.service.utils.RetUtils;
import org.tio.mg.service.vo.MgConst;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.topic.ImManagerTopicVo;
import org.tio.utils.jfinal.P;

import cn.hutool.core.util.StrUtil;
import org.tio.utils.resp.Resp;

/**
 * im群管理
 * @author xufei
 * 2020年5月16日 下午2:21:06
 */
public class TioGroupService {
	
	@SuppressWarnings("unused")
	private static Logger			log	= LoggerFactory.getLogger(TioGroupService.class);
	
	public static final TioGroupService	me	= new TioGroupService();
	
	
	/**
	 * 群查询
	 * @param pageNumber
	 * @param pageSize
	 * @param searchkey
	 * @param groupkey
	 * @return
	 * @author xufei
	 * 2020年6月28日 下午3:35:45
	 */
	public Ret groupList(Integer pageNumber, Integer pageSize, String searchkey,String groupkey,String starttime,String endtime) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageSize <= 0) {
			pageSize = 16;
		}
		Kv params = Kv.by("managerrole", Const.GroupRole.MANAGER).set("starttime", starttime).set("endtime", endtime);
		if(StrUtil.isNotBlank(searchkey)) {
			params.set("searchkey", "%" + searchkey + "%");
		}
		if(StrUtil.isNotBlank(groupkey)) {
			params.set("groupkey", "%" + groupkey + "%");
			params.set("gid", groupkey);
		}
		boolean allowOper = P.getBoolean("oper.open.flag",true);
		if(!allowOper) {
			params.set("noemail", Const.YesOrNo.YES);
		} else {
			params.set("email", Const.YesOrNo.YES);
		}
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("group.grouplist", params);
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(records);
	}
	
	
	/**
	 * 群管理
	 * @param pageNumber
	 * @param pageSize
	 * @param searchkey
	 * @param groupkey
	 * @param status
	 * @return
	 * @author xufei
	 * 2021年2月18日 上午11:40:06
	 */
	public Ret managerGroupList(Integer pageNumber, Integer pageSize, String searchkey,String groupkey,Short status) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageSize <= 0) {
			pageSize = 16;
		}
		Kv params = Kv.create();
		if(StrUtil.isNotBlank(searchkey)) {
			params.set("searchkey", "%" + searchkey + "%");
			params.set("phone", searchkey);
		}
		if(StrUtil.isNotBlank(groupkey)) {
			params.set("groupkey", "%" + groupkey + "%");
			params.set("gid", groupkey);
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
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("group.mangergrouplist", params);
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(records);
	}
	
	
	/**
	 * @param pageNumber
	 * @param pageSize
	 * @param groupid
	 * @return
	 * @author xufei
	 * 2021年2月24日 下午5:40:30
	 */
	public Ret forbiddenUserList(Integer pageNumber, Integer pageSize, Long groupid) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageSize <= 0) {
			pageSize = 16;
		}
		Kv params = Kv.by("groupid", groupid).set("noflag",Const.Forbiddenflag.NO);
		boolean allowOper = P.getBoolean("oper.open.flag",true);
		if(!allowOper) {
			params.set("noemail", Const.YesOrNo.YES);
		} else {
			params.set("email", Const.YesOrNo.YES);
		}
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("group.forbiddenUserList", params);
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(records);
	}
	
	/**
	 * 举报列表
	 * @param pageNumber
	 * @param pageSize
	 * @param groupid
	 * @param status
	 * @return
	 * @author xufei
	 * 2021年2月24日 下午5:48:32
	 */
	public Ret reportList(Integer pageNumber, Integer pageSize, Long groupid,Short status) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageSize <= 0) {
			pageSize = 16;
		}
		Kv params = Kv.by("groupid", groupid).set("type",Const.WxReport.GROUP);
		if(status != null) {
			params.set("status",status);
		}
		boolean allowOper = P.getBoolean("oper.open.flag",true);
		if(!allowOper) {
			params.set("noemail", Const.YesOrNo.YES);
		} else {
			params.set("email", Const.YesOrNo.YES);
		}
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("group.reportList", params);
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(records);
	}

	/**
	 * 举报列表
	 * @param pageNumber
	 * @param pageSize
	 * @param searchkey
	 * @param status
	 * @return
	 * @author xufei
	 * 2021年2月24日 下午5:48:32
	 */
	public Ret reportListNew(Integer pageNumber, Integer pageSize, String searchkey,Short status) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageSize <= 0) {
			pageSize = 16;
		}
		Kv params = Kv.by("type",Const.WxReport.GROUP);
		if (searchkey != null && !searchkey.isEmpty()) {
			params.set("searchkey", "%"+searchkey+"%");
		}
		if(status != null) {
			params.set("status",status);
		}
//		boolean allowOper = P.getBoolean("oper.open.flag",true);
//		if(!allowOper) {
//			params.set("noemail", Const.YesOrNo.YES);
//		} else {
//			params.set("email", Const.YesOrNo.YES);
//		}
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("group.reportListNew", params);
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(records);
	}
	
	
	/**
	 * 封停操作记录表
	 * @param pageNumber
	 * @param pageSize
	 * @param groupid
	 * @return
	 * @author xufei
	 * 2021年2月25日 下午3:50:29
	 */
	public Ret inblackOperlist(Integer pageNumber, Integer pageSize, Long groupid) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageSize <= 0) {
			pageSize = 16;
		}
		Kv params = Kv.by("groupid", groupid);
		boolean allowOper = P.getBoolean("oper.open.flag",true);
		if(!allowOper) {
			params.set("noemail", Const.YesOrNo.YES);
		} else {
			params.set("email", Const.YesOrNo.YES);
		}
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("group.inblackOperList", params);
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(records);
	}
	
	/**
	 * @param pageNumber
	 * @param pageSize
	 * @param groupid
	 * @return
	 * @author xufei
	 * 2021年2月24日 下午5:45:50
	 */
	public Ret managerUserList(Integer pageNumber, Integer pageSize, Long groupid) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageSize <= 0) {
			pageSize = 16;
		}
		Kv params = Kv.by("groupid", groupid).set("managerrole",Const.GroupRole.MANAGER);
		boolean allowOper = P.getBoolean("oper.open.flag",true);
		if(!allowOper) {
			params.set("noemail", Const.YesOrNo.YES);
		} else {
			params.set("email", Const.YesOrNo.YES);
		}
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("group.managerUserList", params);
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(records);
	}

	/**
	 * @param pageNumber
	 * @param pageSize
	 * @param searchkey
	 * @param groupkey
	 * @return
	 * @author xufei
	 * 2020年6月28日 下午5:29:56
	 */
	public Ret delgroupList(Integer pageNumber, Integer pageSize, String searchkey,String groupkey,String starttime,String endtime) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageSize <= 0) {
			pageSize = 16;
		}
		Kv params = Kv.by("managerrole", Const.GroupRole.MANAGER).set("starttime", starttime).set("endtime", endtime);
		if(StrUtil.isNotBlank(searchkey)) {
			params.set("searchkey", "%" + searchkey + "%");
		}
		if(StrUtil.isNotBlank(groupkey)) {
			params.set("groupkey", "%" + groupkey + "%");
			params.set("gid", groupkey);
		}
		boolean allowOper = P.getBoolean("oper.open.flag",true);
		if(!allowOper) {
			params.set("noemail", Const.YesOrNo.YES);
		} else {
			params.set("email", Const.YesOrNo.YES);
		}
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("group.delgrouplist", params);
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(records);
	}
	
	/**
	 * 查询消息模型下的群列表
	 * @param pageNumber
	 * @param pageSize
	 * @param groupkey
	 * @param starttime
	 * @param endtime
	 * @param type
	 * @return
	 * @author xufei
	 * 2020年7月14日 下午2:21:11
	 */
	public Ret modeGroupAboutMsgList(Integer pageNumber, Integer pageSize,String groupkey,String starttime,String endtime,Short type,String searchkey) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageSize <= 0) {
			pageSize = 16;
		}
		String sqlMode = "modeGroupStatList";
		if(Objects.equals(type, Const.Status.DISABLED)) {
			sqlMode = "bakModeGroupStatList";
		} else if(Objects.equals(type, Const.Status.DELETE)) {
			sqlMode = "delModeGroupStatList";
		}
		Kv params = Kv.create();
		if(StrUtil.isNotBlank(starttime)) {
			params.set("starttime", starttime);
		}
		if(StrUtil.isNotBlank(endtime)) {
			params.set("endtime", endtime);
		}
		if(StrUtil.isNotBlank(groupkey)) {
			params.set("groupkey", "%" + groupkey + "%");
			params.set("gid", groupkey);
		}
		boolean allowOper = P.getBoolean("oper.open.flag",true);
		if(!allowOper) {
			params.set("noemail", Const.YesOrNo.YES);
		} else {
			params.set("email", Const.YesOrNo.YES);
		}
		if (StrUtil.isNotBlank(searchkey)) {
			params.set("searchkey","%"+searchkey+"%");
		}
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("group." + sqlMode, params);
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(records);
	}
	
	/**
	 * 封停操作
	 * @param groupid
	 * @param status
	 * @param reason
	 * @return
	 * @author xufei
	 * 2021年2月24日 下午2:22:37
	 */
	public Ret inblack(Long groupid,Short status,String reason,MgUser mgUser) {
		if(groupid == null || status == null) {
			return RetUtils.invalidParam();
		}
		WxGroup group = WxGroup.dao.findById(groupid);
		if(group == null) {
			return RetUtils.failMsg("群存在");
		}
		if(Objects.equals(group.getStatus(), status)) {
			return RetUtils.failMsg("重复操作");
		}
		AbsTxAtom atom = new AbsTxAtom() {
			
			@Override
			public boolean noTxRun() {
				WxGroup update = new WxGroup();
				update.setId(groupid);
				update.setStatus(status);
				update.setRemark(reason);
				boolean updateflag = update.update();
				if(!updateflag) {
					return failRet("修改失败");
				}
				WxGroupInblack inblack = new WxGroupInblack();
				inblack.setGroupid(groupid);
				inblack.setOper(status);
				inblack.setReason(reason);
				inblack.setAdminnick(mgUser.getNick());
				inblack.setAdminuid(mgUser.getId());
				boolean init = inblack.save();
				if(!init) {
					return failRet("保存失败");
				}
				return true;
			}
		};
		boolean commit = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
		if(!commit) {
			return atom.getRetObj();
		}
		return RetUtils.okOper();
	}
	
	/**
	 * @param ids
	 * @param mgUser
	 * @return
	 * @author xufei
	 * 2021年2月25日 下午4:45:52
	 */
	public Ret reportDeal(String ids,MgUser mgUser) {
		if(StrUtil.isBlank(ids)) {
			return RetUtils.invalidParam();
		}
		Db.use(Const.Db.TIO_SITE_MAIN).update("update wx_user_report set `status` = ?,adminnick = ?,adminuid = ? where id = ?",Const.YesOrNo.YES,mgUser.getNick(),mgUser.getId(),"(" + ids +")");
		return RetUtils.okOper();
	}

	public Ret handlingReport(String ids, Integer status, MgUser mgUser, String reason) {
		if(StrUtil.isBlank(ids)) {
			return RetUtils.invalidParam();
		}
		String[] idList = ids.split(",");
		for (String id : idList) {
			WxUserReport wxUserReport = WxUserReport.dao.findById(id);
			if (wxUserReport == null) {
				continue;
			}
			Db.use(Const.Db.TIO_SITE_MAIN).update("update wx_user_report set `status` = ?,adminnick = ?,adminuid = ? where id = ?",3,mgUser.getNick(),mgUser.getId(),id);
			if (status.equals(1)) {
				Ret ret = inblack(wxUserReport.getGroupid(), Short.valueOf("2"), reason,mgUser);
				if(ret.isFail()) {
					log.error("封停操作失败：{}",RetUtils.getRetMsg(ret));
					return ret;
				}
				RedissonClient redisson = RedisInit.get();
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("groupid", wxUserReport.getGroupid());
				params.put("status", 2);
				RTopic topic = redisson.getTopic(Const.Topic.IM_MANAGER_OPER);
				ImManagerTopicVo topicVo = new ImManagerTopicVo();
				topicVo.setType(ImManagerTopicVo.Type.GROUP_INBLACK_OPER);
				topicVo.setParams(params);
				topic.publish(topicVo);
			}
		}
		return RetUtils.okOper();
	}
}
