
package org.tio.mg.service.service.tioim;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.IdUtil;
import cn.hutool.poi.excel.BigExcelWriter;
import cn.hutool.poi.excel.ExcelUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.kit.StrKit;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Page;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.mg.service.cache.Caches;
import org.tio.mg.service.init.RedisInit;
import org.tio.mg.service.model.main.*;
import org.tio.mg.service.model.mg.MgUser;
import org.tio.mg.service.service.atom.AbsTxAtom;
import org.tio.mg.service.service.atom.RegisterAtom;
import org.tio.mg.service.service.base.UserService;
import org.tio.mg.service.utils.CommonUtils;
import org.tio.mg.service.utils.PeriodUtils;
import org.tio.mg.service.utils.RetUtils;
import org.tio.mg.service.vo.RequestExt;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.utils.PyUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.mg.service.vo.MgConst;
import org.tio.sitexxx.service.vo.topic.ImManagerTopicVo;
import org.tio.sitexxx.service.vo.topic.TopicVo;
import org.tio.utils.jfinal.P;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import org.tio.utils.resp.Resp;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * im用户管理
 * @author xufei
 * 2020年5月16日 下午2:21:06
 */
public class TioUserService {
	
	private static Logger			log	= LoggerFactory.getLogger(TioUserService.class);
	
	public static final TioUserService	me	= new TioUserService();

	/**
	 * 用户列表数据
	 * @param pageNumber
	 * @param pageSize
	 * @param searchkey
	 * @param status
	 * @return
	 * @author xufei
	 * 2020年6月29日 上午11:47:24
	 */
	public Ret list(Integer pageNumber, Integer pageSize, String searchkey, Short status, Integer orderby, Integer sort) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageSize <= 0) {
			pageSize = 16;
		}
		Kv params = Kv.create();
		if(StrUtil.isNotBlank(searchkey)) {
			params.set("searchkey", "%" + searchkey + "%");
			params.set("searchid", searchkey);
		}
		if (orderby != null) {
			params.set("orderby", orderby);
			params.set("sort", sort);
		}
		if(status != null) {
			params.set("status", status);
		} else {
			params.set("defaultstatus",User.Status.LOGOUT);
		}
		boolean allowOper = P.getBoolean("oper.open.flag",true);
		if(!allowOper) {
			params.set("noemail", Const.YesOrNo.YES);
		} else {
			params.set("email", Const.YesOrNo.YES);
		}
 		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("tiouser.list", params);
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(records);
	}
	
	/**
	 * @param uid
	 * @return
	 * @author xufei
	 * 2020年7月29日 下午2:30:18
	 */
	public Ret info(Integer uid) {
		Kv params = Kv.by("uid", uid);
		boolean allowOper = P.getBoolean("oper.open.flag",true);
		if(!allowOper) {
			params.set("noemail", Const.YesOrNo.YES);
		} else {
			params.set("email", Const.YesOrNo.YES);
		}
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("tiouser.info", params);
		Record record = Db.use(MgConst.Db.TIO_SITE_MAIN).findFirst(sqlPara);
		return RetUtils.okData(record);
	}
	
	/**
	 * 
	 * @param pageNumber
	 * @param pageSize
	 * @param searchkey
	 * @param type
	 * @return
	 * @author xufei
	 * 2020年7月13日 下午3:48:33
	 */
	public Ret statlist(Integer pageNumber, Integer pageSize,String searchkey,Integer ipid) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageNumber <= 0) {
			pageSize = 16;
		}
		Kv params = Kv.create();
		DateTime dateTime = PeriodUtils.getDateByPeriod(searchkey);
		params.set("starttime",DateUtil.beginOfDay(dateTime));
		params.set("endtime", DateUtil.endOfDay(dateTime));
		if(ipid != null) {
			params.set("ipid", ipid);
		}
		boolean allowOper = P.getBoolean("oper.open.flag",true);
		if(!allowOper) {
			params.set("noemail", Const.YesOrNo.YES);
		} else {
			params.set("email", Const.YesOrNo.YES);
		}
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("tiouser.statlist", params);
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(records);
	}
	
	/**
	 * @param uid
	 * @return
	 * @author xufei
	 * 2020年6月28日 下午2:34:17
	 */
	public Ret resetPwd(Integer uid) {
		if(uid == null) {
			log.error("用户id为空");
			return RetUtils.invalidParam();
		}
		User user = User.dao.findById(uid);
		if(user == null) {
			return RetUtils.failMsg("用户不存在");
		}
		User update = new User();
		if(StrUtil.isNotBlank(user.getPhone())) {
			String resetPwd = getMd5Pwd(user.getPhone(),MgConst.MG_USER_DEFAULT_PWD);
			update.setPhonepwd(resetPwd);
		}
		if(StrUtil.isNotBlank(user.getEmail())) {
			String resetPwd = getMd5Pwd(user.getEmail(),MgConst.MG_USER_DEFAULT_PWD);
			update.setEmailpwd(resetPwd);
		}
		if(StrUtil.isNotBlank(user.getLoginname())) {
			String resetPwd = getMd5Pwd(user.getLoginname(),MgConst.MG_USER_DEFAULT_PWD);
			update.setPwd(resetPwd);
		}
		update.setId(user.getId());
		boolean ret = update.update();
		if(!ret) {
			return RetUtils.failOper();
		}
		return RetUtils.okOper();
	}
	
	/**
	 * 禁用/启用
	 * @param uid
	 * @param status
	 * @return
	 * @author xufei
	 * 2020年6月28日 下午2:36:46
	 */
	public Ret disable(Integer uid,Short status) {
		if(uid == null) {
			return RetUtils.invalidParam();
		}
		User user = User.dao.findById(uid);
		if(user == null) {
			return RetUtils.failMsg("用户不存在");
		}
		if(Objects.equals(user.getStatus(), status)) {
			return RetUtils.okOper();
		}
		User update = new User();
		update.setId(user.getId());
		update.setStatus(status);
		boolean ret = update.update();
		if(!ret) {
			return RetUtils.failOper();
		}
		return RetUtils.okOper();
	}


	/**
	 * 禁用/启用
	 * @param uids
	 * @param status
	 * @return
	 * @author xufei
	 * 2020年6月28日 下午2:36:46
	 */
	public Ret batchDisable(String uids,Short status) {
		if (status == null || (status != 1 && status != 5) || StrUtil.isBlank(uids)) {
			return RetUtils.invalidParam();
		}
		List<String> split = Arrays.asList(uids.split(","));
		String idStr = split.stream().map(String::valueOf).collect(Collectors.joining(","));
		List<User> users = User.dao.find("select * from user where id in ("+idStr+")");
		if(users.isEmpty()) {
			return RetUtils.noExistParam();
		}
		List<Integer> a = new ArrayList<>();
		users.forEach(e ->{
			if(Objects.equals(e.getStatus(), status)) {
				return;
			}
			User update = new User();
			update.setId(e.getId());
			update.setStatus(status);
			boolean ret = update.update();
			if(ret) {
				RedissonClient redisson = RedisInit.get();
				RTopic topic = redisson.getTopic(Const.Topic.COMMON_TOPIC);
				TopicVo topicVo = new TopicVo();
				topicVo.setType(TopicVo.Type.CLEAR_USER);
				topicVo.setValue(e.getId());
				topic.publish(topicVo);
			}else {
				a.add(e.getId());
			}
		});

		if(!a.isEmpty()){
			String aStr = a.stream().map(String::valueOf).collect(Collectors.joining(","));
			return RetUtils.failMsg("更新失败用户ID:" + aStr);
		}
		return RetUtils.okOper();
	}
	
	/**
	 * 用户密码加密
	 * @param loginname
	 * @param plainpwd
	 * @return
	 * @author xufei
	 * 2020年6月18日 下午3:25:18
	 */
	public static String getMd5Pwd(String loginname, String plainpwd) {
		String pwd = SecureUtil.md5("${" + StrUtil.trim(loginname) + "}" + StrUtil.trim(plainpwd));
		return pwd;
	}

    public Resp addUser(User user, String clientIp, String id, RequestExt requestExt) {

		String loginname = StrUtil.trim(user.getLoginname());
		String pwd = StrUtil.trim(user.getPwd());
		//		String pwd2 = StrUtil.trim(user.getPwd2());
		String nick = StrUtil.trim(user.getNick());

		if (StrKit.isBlank(user.getAgreement())) {
			return Resp.fail("您必须 [同意用户服务条款] 才能注册");
		}

		if (StrKit.isBlank(pwd)) {
			return Resp.fail("密码不能为空");
		}

		//		if (!Objects.equals(pwd, pwd2)) {
		//			return Resp.fail("两次密码不一致");
		//		}

		Resp resp = CommonUtils.checkGroupName(nick, "昵称");
		if (!resp.isOk()) {
			return resp;
		}
		if (!Validator.isMobile(loginname)) {
			return Resp.fail("不是合法的手机号码");
		}
		user.setPhone(loginname);
		user.setPhonepwd(pwd);
		user.setPhonebindflag(Const.YesOrNo.YES);
		if (StrKit.isBlank(nick)) {
			return Resp.fail("昵称不能为空");
		}

		User u1 = User.dao.findFirst("select * from user where nick = ?", user.getNick());
		if (u1 != null) {
			return Resp.fail("昵称已被注册，请换一个昵称");
		}

		User u2 = User.dao.findFirst("select * from user where loginname = ?", user.getLoginname());

		if (u2 != null) {
			return Resp.fail("该账号已注册，如忘记密码，请找回");
		}

		user.setLoginname(loginname);
		user.setNick(nick);

		//		pwd = UserService.getMd5Pwd(user.getLoginname(), pwd);
		//		user.setPwd(pwd);
		user.setStatus((short)1);
		user.setCreatetime(new Date());
		user.setThirdstatus(Const.UserThirdStatus.NORMAL);
		RegisterAtom registerUserAtom = new RegisterAtom(user);
		boolean relsut = Db.tx(registerUserAtom);
		if (relsut) {
			Integer uid = user.getId();
			AbsTxAtom atom = new AbsTxAtom() {
				@Override
				public boolean noTxRun() {
					// 保存好友信息
					WxFriend friend = friendInit(uid, uid, null, "", true);
					if (friend == null) {
						return failRet("自己加自己好友失败");
					}
					WxChatUserItem item = new WxChatUserItem();
					item.setUid(uid);
					item.setChatmode(Const.ChatMode.P2P);
					item.setBizid(Long.valueOf(uid));
					item.setLinkid(friend.getId());
					item.setChatlinkid(null);
					item.setChatlinkmetaid(null);
					item.setTochatlinkid(null);
					item.setTochatlinkmetaid(null);
					item.setLinkflag(Const.YesOrNo.YES);
					item.setFidkey(UserService.twoUid(uid, uid));
					int init = item.ignoreSave();
					return okRet(friend);
				}
			};
			Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
			return Resp.ok(registerUserAtom.getMsg()).data(Kv.by("loginname", user.getLoginname()).set("id", user.getId()).set("user", user));
		} else {
			return Resp.fail(registerUserAtom.getMsg());
			//				User user1 = userService.getByLoginname(user.getLoginname(), null);
			//				if (user1 != null) {
			//					return Resp.fail("该帐号已经被激活过了，请直接登录");
			//				} else {
			//					return Resp.fail("帐号激活失败");
			//				}
		}
	}

	/**
	 * 好友初始化-有统计-已调整
	 * 此方法只用在两个好友初始化中，第一个创建者使用，请注意
	 * @param uid
	 * @param touid
	 * @return boolean isEach
	 * @author lixinji 2020年1月9日 下午4:28:30
	 */
	public WxFriend friendInit(Integer uid, Integer touid, Long msgid, String remark, boolean isEach) {
		WxFriend friend = new WxFriend();
		friend.setUid(uid);
		friend.setStartmsgid(msgid);
		friend.setFrienduid(touid);
		if (StrUtil.isBlank(remark)) {
			User user = UserService.ME.getById(touid);
			friend.setChatindex(PyUtils.getFristChat(user.getNick()));
		} else {
			//放在这么，避免remarkname存在两个默认值：null 和空字符串
			friend.setRemarkname(remark);
			friend.setChatindex(PyUtils.getFristChat(remark));
		}
		int count = friend.ignoreSave();
		if (count < 1) {
			return null;
		}
		WxFriendMeta meta = new WxFriendMeta();
		meta.setUid(uid);
		meta.setTouid(touid);
		meta.setFidkey(UserService.twoUid(uid, touid));
		if (isEach) {
			meta.replaceSave();
		} else {
			meta.ignoreSave();
		}
//		ChatIndexService.clearMailListCache(uid);
		return friend;
	}

//	/**
//	 * 通讯录缓存清理-已调整
//	 * @param uid
//	 * @author lixinji
//	 * 2020年3月10日 下午4:39:29
//	 */
//	public static void clearMailListCache(Integer uid) {
//		Caches.getCache(CacheConfig.WX_MAILLIST_2).remove(uid + "_99");
//		Caches.getCache(CacheConfig.WX_MAILLIST_2).remove(uid + "_" + Const.ChatMode.P2P);
//		Caches.getCache(CacheConfig.WX_MAILLIST_2).remove(uid + "_" + Const.ChatMode.GROUP);
//	}

    public Ret reportListNew(Integer pageNumber, Integer pageSize, String searchkey, Short status) {

		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageSize <= 0) {
			pageSize = 16;
		}
		Kv params = Kv.by("type",Const.WxReport.USER);
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
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("tiouser.reportListNew", params);
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(records);
    }

	public Ret handlingReport(String ids, Integer status, MgUser mgUser) {
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
				Ret ret = disable(wxUserReport.getTouid(), Short.valueOf("5"));
				if(ret.isFail()) {
					log.error("禁用/启用失败：{}",RetUtils.getRetMsg(ret));
					return ret;
				}
				RedissonClient redisson = RedisInit.get();
				RTopic topic = redisson.getTopic(Const.Topic.COMMON_TOPIC);
				TopicVo topicVo = new TopicVo();
				topicVo.setType(TopicVo.Type.CLEAR_USER);
				topicVo.setValue(wxUserReport.getTouid());
				topic.publish(topicVo);
			}
		}
		return RetUtils.okOper();
	}

	/**
	 * 获取下级信息
	 * @param uid
	 * @return
	 * @author lixinji
	 * 2023年3月3日 下午6:30:39
	 */
	public List<Record> getUnderUserInfo(Integer uid) {
		String parentInviteCode = User.dao.findById(uid).getInvitecode();
		String sql = "select " +
				"a.id as uid, a.avatar as avatar, a.nick, IFNULL(b.cny, 0) as cny,a.createtime as createTime  " +
				"from " +
				"tio_site_main.user a left join wx_user_coin_local b on a.id = b.uid, " +
				"(WITH RECURSIVE subset_cte (id, invitecode, parentinvitecode, depth) AS ( " +
				"SELECT d.id, d.invitecode, d.parentinvitecode, 1 " +
				"FROM tio_site_main.user d " +
				"WHERE invitecode = ? " +
				"UNION ALL " +
				"SELECT t.id, t.invitecode, t.parentinvitecode, cte.depth + 1 " +
				"FROM tio_site_main.user t " +
				"INNER JOIN subset_cte cte ON t.parentinvitecode = cte.invitecode " +
				") " +
				"SELECT subset_cte.id, subset_cte.invitecode, subset_cte.parentinvitecode, depth " +
				"FROM subset_cte) c " +
				"where " +
				"a.id = c.id " +
				"and a.id != ?";
		List<Record> records = Db.find(sql, parentInviteCode, uid);


		return records;
	}

	public void updateInvitecode(Integer uid, String invitecode) {
		User curr = User.dao.findById(uid);
		String sql1 = "update user set parentinvitecode = ? where parentinvitecode = ?";
		String sql2 = "update user set invitecode = ? where invitecode = ?";
		int c1 = Db.update(sql1,invitecode, curr.getInvitecode());
		int c2 = Db.update(sql2,invitecode, curr.getInvitecode());
	}

	public String download(Page<Record> all, Integer city, Integer invitecode, Integer sign, Integer nick, Integer realnameflag, Integer province,
						 Integer id, Integer email, Integer createtime, Integer loginname, Integer sex, Integer ip, Integer parentinvitecode, Integer phone,
						 Integer _status, Integer source) throws IOException {
		List<Map<String, Object>> list = new ArrayList<>();
		for (Record record : all.getList()) {
			Map<String, Object> map = new LinkedHashMap<>();
			if (city.equals(1)) {
				map.put("城市", record.get("city"));
			}
			if (invitecode.equals(1)) {
				map.put("邀请码", record.get("invitecode"));
			}
			if (sign.equals(1)) {
				map.put("签到", record.get("sign"));
			}
			if (nick.equals(1)) {
				map.put("昵称", record.get("nick"));
			}
			if (realnameflag.equals(1)) {
				map.put("实名状态", record.get("realnameflag"));
			}
			if (province.equals(1)) {
				map.put("省份", record.get("province"));
			}
			if (id.equals(1)) {
				map.put("id", record.get("id"));
			}
			if (email.equals(1)) {
				map.put("邮箱", record.get("email"));
			}
			if (loginname.equals(1)) {
				map.put("用户名", record.get("loginname"));
			}
			if (sex.equals(1)) {
				map.put("性别", record.get("sex"));
			}
			if (ip.equals(1)) {
				map.put("ip", record.get("ip"));
			}
			if (parentinvitecode.equals(1)) {
				map.put("上级邀请码", record.get("parentinvitecode"));
			}
			if (phone.equals(1)) {
				map.put("手机号", record.get("phone"));
			}
			if (_status.equals(1)) {
				map.put("用户状态", record.get("status"));
			}
			if (source.equals(1)) {
				map.put("用户来源", record.get("source"));
			}
			if (createtime.equals(1)) {
				map.put("注册时间", record.get("createtime"));
			}
			list.add(map);
		}
		return downloadExcel(list);
	}

	/**
	 * 导出excel
	 */
	public static String downloadExcel(List<Map<String, Object>> list) throws IOException {
		String tempPath = "/excel/" + IdUtil.fastSimpleUUID() + ".xlsx";
		String path =MgConst.RES_ROOT + tempPath;
		File file = new File(path);
		BigExcelWriter writer= ExcelUtil.getBigWriter(file);
		// 一次性写出内容，使用默认样式，强制输出标题
		writer.write(list, true);
		//response为HttpServletResponse对象
		//test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
		// 终止后删除临时文件
//		file.deleteOnExit();
		//此处记得关闭输出Servlet流
		writer.close();
		return Const.RES_SERVER + tempPath;
	}

	public static Workbook downloadExcel(String[] rowTitles, List<List<String>> rowDatas) throws IOException {
		Workbook workbook = new HSSFWorkbook();
		try {

			// 第二步，在webbook中添加一个sheet,对应Excel文件中的sheet
			Sheet sheet = workbook.createSheet("外部案件导入模板");
			// 第三步，在sheet中添加表头第0行
			Row row = sheet.createRow(0);
			// 第四步，创建单元格，并设置值表头 设置表头居中
			CellStyle style = workbook.createCellStyle();
			style.setAlignment(HorizontalAlignment.CENTER); // 创建一个居中格式
			for (int i = 0; i < rowTitles.length; i++) {
				Cell cell = row.createCell(i);
				cell.setCellValue(rowTitles[i]);
				cell.setCellStyle(style);
				sheet.setColumnWidth(i, (25 * 256));  //设置列宽，50个字符宽
			}

			// 第五步，写入实体数据 实际应用中这些数据从数据库得到
			if (rowDatas != null) {
				for (int i = 0; i < rowDatas.size(); i++) {
					row = sheet.createRow(i+1);
					List<String> strings = rowDatas.get(i);
					for (int j = 0; j < strings.size(); j++) {
						Cell cell = row.createCell(j, CellType.STRING);
						cell.setCellValue(strings.get(j));
						cell.setCellStyle(style);
						if (StringUtils.isNotBlank(strings.get(j))) {
							int columnWidth = (strings.get(j).length() * 2) * 256;
							sheet.setColumnWidth(j, Math.max((25 * 256), columnWidth));
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return workbook;

	}

}
