
package org.tio.mg.web.server.controller.tioim;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.kit.StrKit;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Page;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.mg.service.init.RedisInit;
import org.tio.mg.service.model.main.IpInfo;
import org.tio.mg.service.model.main.User;
import org.tio.mg.service.model.mg.MgUser;
import org.tio.mg.service.service.base.IpInfoService;
import org.tio.mg.service.service.tioim.TioUserService;
import org.tio.mg.service.utils.CommonUtils;
import org.tio.mg.service.utils.RetUtils;
import org.tio.mg.service.vo.AddUserVo;
import org.tio.mg.service.vo.RequestExt;
import org.tio.mg.web.server.utils.WebUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.topic.TopicVo;
import org.tio.utils.resp.Resp;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 钛信用户管理
 * @author xufei
 * 2020年5月25日 下午4:45:27
 */
@RequestPath(value = "/tiouser")
public class TioUserController {
	private static Logger log = LoggerFactory.getLogger(TioUserController.class);

	/**
	 * @param args
	 * @author xufei
	 * 2020年6月18日 上午10:04:21
	 */
	public static void main(String[] args) {
		List<Integer> segment = Arrays.asList(144, 148, 165, 172, 140, 145, 171, 167, 141, 149, 153, 162);
		System.out.println(segment.get(0));
	}

	private TioUserService userService = TioUserService.me;


	/**
	 * 获取用户列表
	 * @param request
	 * @param searchkey
	 * @param status
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月28日 下午4:30:22
	 */
	@RequestPath(value = "/download")
	public Resp download(HttpRequest request, String searchkey, Short status, Integer pageNumber, Integer pageSize, Integer orderby, Integer sort,
						 Integer city, Integer invitecode, Integer sign, Integer nick, Integer realnameflag, Integer province,
						 Integer id, Integer email, Integer createtime, Integer loginname, Integer sex, Integer ip, Integer parentinvitecode, Integer phone,
						 Integer _status, Integer source) throws Exception {
		Ret ret = userService.list(pageNumber, pageSize, searchkey, status,orderby, sort);
		if(ret.isFail()) {
			log.error("获取用户列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		Page<Record> records = (Page<Record>) RetUtils.getOkPage(ret);
		if (city == null)
			city = 1;
		if (invitecode == null)
			invitecode = 1;
		if (sign == null)
			sign = 1;
		if (nick == null)
			nick = 1;
		if (realnameflag == null)
			realnameflag = 1;
		if (province == null)
			province = 1;
		if (id == null)
			id = 1;
		if (email == null)
			email = 1;
		if (createtime == null)
			createtime = 1;
		if (loginname == null)
			loginname = 1;
		if (sex == null)
			sex = 1;
		if (ip == null)
			ip = 1;
		if (parentinvitecode == null)
			parentinvitecode = 1;
		if (phone == null)
			phone = 1;
		if (_status == null)
			_status = 1;
		if (source == null)
			source = 1;
		String download = userService.download(records, city, invitecode, sign, nick, realnameflag, province, id, email,
				createtime, loginname, sex, ip, parentinvitecode, phone, _status, source);
		Map<String, String> data = new HashMap<>();
		data.put("download", download);
		return Resp.ok(data);
	}
	
	/**
	 * 获取用户列表
	 * @param request
	 * @param searchkey
	 * @param status
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月28日 下午4:30:22
	 */
	@RequestPath(value = "/list")
	public Resp list(HttpRequest request,String searchkey,Short status,Integer pageNumber,Integer pageSize, Integer orderby, Integer sort) throws Exception {
		Ret ret = userService.list(pageNumber, pageSize, searchkey, status,orderby, sort);
		if(ret.isFail()) {
			log.error("获取用户列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}


	/**
	 * 获取下级信息
	 * @param request
	 * @param uid
	 * @return
	 * @throws Exception
	 * @author xinji
	 * 2020年3月3日 下午6:35:05
	 */
	@RequestPath(value = "/getUnderUserInfo")
	public Resp getUnderUserInfo(HttpRequest request, Integer uid) throws Exception {
		List<Record> underUserInfo = userService.getUnderUserInfo(uid);
//		WxChatApi
		return Resp.ok(underUserInfo);
	}

	/**
	 * 修改个人邀请码
	 * @param request
	 * @param invitecode
	 * @return
	 * @throws Exception
	 * @author xinji
	 * 2020年3月3日 下午6:35:05
	 */
	@RequestPath(value = "/updateInvitecode")
	public Resp updateInvitecode(HttpRequest request, Integer uid, String invitecode) throws Exception {

		if (invitecode == null || invitecode.isEmpty()) {
			return Resp.fail("邀请码不能为空");
		}

		if (!invitecode.matches("[a-zA-Z0-9]+")) {
			return Resp.fail("邀请码由数字加字母组成");
		}
		List<User> users = User.dao.find("select * from user where parentinvitecode = ?", invitecode);
		if (users != null && users.size() > 0) {
			return Resp.fail("该邀请码已存在");
		}
		userService.updateInvitecode(uid, invitecode);
		return Resp.ok();
	}
	
	/**
	 * @param request
	 * @param uid
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月29日 下午2:33:33
	 */
	@RequestPath(value = "/info")
	public Resp info(HttpRequest request,Integer uid) throws Exception {
		Ret ret = userService.info(uid);
		if(ret.isFail()) {
			log.error("获取用户信息失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	/**
	 * @param request
	 * @param searchkey
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月13日 下午3:47:14
	 */
	@RequestPath(value = "/statlist")
	public Resp statlist(HttpRequest request,String searchkey,Integer pageNumber,Integer ipid,Integer pageSize) throws Exception {
		Ret ret = userService.statlist(pageNumber, pageSize, searchkey,ipid);
		if(ret.isFail()) {
			log.error("获取用户列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
	/**
	 * 重置密码
	 * @param request
	 * @param uid
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月28日 下午4:30:44
	 */
	@RequestPath(value = "/resetPwd")
	public Resp resetPwd(HttpRequest request,Integer uid) throws Exception {
		Ret ret = userService.resetPwd(uid);
		if(ret.isFail()) {
			log.error("重置密码失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		RedissonClient redisson = RedisInit.get();
		RTopic topic = redisson.getTopic(Const.Topic.COMMON_TOPIC);
		TopicVo topicVo = new TopicVo();
		topicVo.setType(TopicVo.Type.CLEAR_USER);
		topicVo.setValue(uid);
		topic.publish(topicVo);
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	/**
	 * 禁用/启用
	 * @param request
	 * @param uid
	 * @param status
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月28日 下午4:31:50
	 */
	@RequestPath(value = "/disable")
	public Resp disable(HttpRequest request,Integer uid,Short status) throws Exception {
		Ret ret = userService.disable(uid, status);
		if(ret.isFail()) {
			log.error("禁用/启用失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		RedissonClient redisson = RedisInit.get();
		RTopic topic = redisson.getTopic(Const.Topic.COMMON_TOPIC);
		TopicVo topicVo = new TopicVo();
		topicVo.setType(TopicVo.Type.CLEAR_USER);
		topicVo.setValue(uid);
		topic.publish(topicVo);
		return Resp.ok(RetUtils.getOkData(ret));
	}

	/**
	 * 批量禁用/启用
	 * @param request
	 * @param uids
	 * @param status
	 * @return
	 * @throws Exception
	 */
	@RequestPath(value = "/batchDisable")
	public Resp batchDisable(HttpRequest request,String uids,Short status) throws Exception {
		Ret ret = userService.batchDisable(uids, status);
		if(ret.isFail()) {
			log.error("禁用/启用失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}


	/**
	 * 添加前端用户
	 * @param request
	 * @param user
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月28日 下午4:31:50
	 */
	@RequestPath(value = "/addUser")
	public Resp addUser(HttpRequest request, User user) throws Exception {
		completeUser(user, request);
		RequestExt requestExt = WebUtils.getRequestExt(request);
		user.setReghref(request.getReferer());
		if (StrUtil.isBlank(user.getAvatar())) {
			String sql = "select path from auto_avatar where chatindex = ? limit 0,1";
			String path = Db.use(Const.Db.TIO_SITE_CONF).queryStr(sql, "#");
			if (StrUtil.isNotBlank(path)) {
				user.setAvatar(path);
				user.setAvatarbig(path);
			}
		}


		return userService.addUser(user, request.getClientIp(), request.getHttpSession().getId(), requestExt);

	}
	/**
	 * MD5加密方法
	 * @param input 待加密字符串
	 * @return 加密后的十六进制字符串
	 */
	private String md5Encrypt(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] bytes = md.digest(input.getBytes(StandardCharsets.ISO_8859_1));

			// 转换为十六进制字符串
			StringBuilder sb = new StringBuilder();
			for (byte b : bytes) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("MD5加密失败", e);
		}
	}

	/**
	 * 加密密码，对应JavaScript中的加密逻辑
	 * @param loginName 登录名
	 * @param passWord 密码
	 * @return 加密后的密码
	 */
	public String encryptPassword(String loginName, String passWord) {
		// 构造加密字符串，与JavaScript中的 palinstr 变量对应
		String palinstr = "$" + "{" + loginName + "}" + passWord;

		// 使用MD5加密，对应JavaScript中的 CryptoJS.MD5(CryptoJS.enc.Latin1.parse(palinstr)).toString()
		return md5Encrypt(palinstr);
	}

	/**
	 * 批量添加用户
	 * @param request
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年6月28日 下午4:31:50
	 */
	@RequestPath(value = "/addUserBatch")
	public Resp addUser(HttpRequest request, Integer num) throws Exception {

		if (num == null || num < 1){
			return Resp.fail("num不能为空");
		}

		// 虚拟号码段
		List<Integer> segment = Arrays.asList(144, 148, 165, 172, 140, 145, 171, 167, 141, 149, 153, 162);

		RequestExt requestExt = WebUtils.getRequestExt(request);

		String sql = "select path from auto_avatar where chatindex = ? limit 0,1";
		String path = Db.use(Const.Db.TIO_SITE_CONF).queryStr(sql, "#");

		Integer start = segment.get(ThreadLocalRandom.current().nextInt(0, segment.size()));
		Map<String, String> lose = new HashMap<>();
		List<String> succeed = new ArrayList<>();
		for (int i = 0; i < num; i++) {
			User user = new User();
			user.setAgreement("on");
			user.setAvatar(path);
			user.setAvatarbig(path);
			user.setReghref(request.getReferer());
			user.setUserType(2);
			Integer end = ThreadLocalRandom.current().nextInt(10000000, 100000000);
			String logname = start+ "" + end;
			user.setNick(logname);
			// 对密码进行加密，与前端JavaScript逻辑保持一致
			String encryptedPwd = encryptPassword(logname, "123456");
			user.setPwd(encryptedPwd);
			user.setLoginname(logname);
			try {
				completeUser(user, request);
				Resp resp = userService.addUser(user, request.getClientIp(), request.getHttpSession().getId(), requestExt);
				if (resp.isOk()){
					succeed.add(logname);
				} else {
					lose.put(logname, resp.getMsg());
				}
			} catch (Exception e) {
				String errorMsg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
				lose.put(logname, errorMsg != null ? errorMsg : "系统错误");
			}
		}
		Map<String, Object> map = new HashMap<>();
		map.put("succeed", succeed);
		map.put("lose", lose);
		return Resp.ok(map);

	}


	/**
	 * 生成临时用户
	 * @param request
	 * @param num 数量
	 * @return
	 * @throws Exception
	 * @author xinji
	 * 2023年12月13日 上午11:10:50
	 */
	@RequestPath(value = "/genTempUser")
	public Resp genTempUser(HttpRequest request, Integer num) throws Exception {

		for (int i = 1; i <= num; i++) {
			User user = new User();
			user.setAgreement("on");
			user.setNick("临时用户_" + i);
			user.setLoginname(genPhone(i));
			user.setPwd("123456");
			user.setUserType(2);
			completeUser(user, request);
			RequestExt requestExt = WebUtils.getRequestExt(request);
			user.setReghref(request.getReferer());
			if (StrUtil.isBlank(user.getAvatar())) {
				String sql = "select path from auto_avatar where chatindex = ? limit 0,1";
				String path = Db.use(Const.Db.TIO_SITE_CONF).queryStr(sql, "#");
				if (StrUtil.isNotBlank(path)) {
					user.setAvatar(path);
					user.setAvatarbig(path);
				}
			}
			userService.addUser(user, request.getClientIp(), request.getHttpSession().getId(), requestExt);
		}

		return Resp.ok();

	}


	/**
	 * 设置靓号
	 * @param request
	 * @param id 用户id
	 * @param beautifulId 设置的靓号id
	 * @param expireTime 过期时间 格式 yyyy-MM-dd
	 * @return
	 * @throws Exception
	 * @author xinji
	 * 2023年12月13日 上午11:10:50
	 */
	@RequestPath(value = "/setUserBeautifulId")
	public Resp setUserBeautifulId(HttpRequest request, Integer id, Integer beautifulId, String expireTime) throws Exception {
		User user = User.dao.findById(id);
		if (user == null) {
			return Resp.fail().msg("用户不存在");
		}
		if (user.getUserType().equals(2)) {
			return Resp.fail().msg("临时用户不可以设置靓号");
		}
		if (user.getIsBeautifulId().equals(1) && !user.getBeautifulId().equals(beautifulId)) {
			User checkUid = User.dao.findFirst("select * from user where (id = ? or beautiful_id = ?) and now() < beautiful_id_expire_time", beautifulId, beautifulId);
			if (checkUid != null) {
				return Resp.fail().msg("该靓号已被使用");
			}
		}

		user.setBeautifulId(beautifulId);
		user.setBeautifulIdCreateTime(new Date());
		user.setIsBeautifulId(1);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // 定义日期格式
		Date date = sdf.parse(expireTime); // 进行转换
		user.setBeautifulIdExpireTime(date);
		boolean update = user.update();
		if (!update) {
			return Resp.fail().msg("操作失败，请重试");
		}
		return Resp.ok();
	}

	/**
	 * @param request
	 * @param request
	 * @param pageNumber
	 * @param pageSize
	 * @param searchkey
	 * @param status
	 * @return
	 * @throws Exception
	 * @author xinjili
	 * 2024年4月11日 上午10:33:38
	 */
	@RequestPath(value = "/reportlistnew")
	public Resp reportlistnew(HttpRequest request, Integer pageNumber,Integer pageSize,String searchkey,Short status) throws Exception {
		Ret ret = userService.reportListNew(pageNumber, pageSize, searchkey,status);
		if(ret.isFail()) {
			log.error("获取群举报列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}

	/**
	 * @param request
	 * @param request
	 * @param ids
	 * @param status 0 不对用户操作 1 禁用用户
	 * @return
	 * @throws Exception
	 * @author xinjili
	 * 2024年4月11日 上午11:13:38
	 */
	@RequestPath(value = "/handlingReport")
	public Resp handlingReport(HttpRequest request, String ids, Integer status) throws Exception {
		MgUser user = WebUtils.currUser(request);
		if (!status.equals(0) && !status.equals(1)) {
			return Resp.fail().msg("参数异常");
		}

		Ret ret = userService.handlingReport(ids, status, user);
		if(ret.isFail()) {
			log.error("操作失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok();
	}

	private void completeUser(User user, HttpRequest request) {
		IpInfo ipInfo = IpInfoService.ME.save(request.getClientIp());
		user.setIpInfo(ipInfo);
		RequestExt requestExt = WebUtils.getRequestExt(request);
		short deviceType = requestExt.getDeviceType();
		user.setRegistertype(deviceType);
	}

	public static String genPhone(Integer endNum) {
		if ((endNum+"").length() == 1) {
			return "1890000000" + endNum;
		}
		if ((endNum+"").length() == 2) {
			return "189000000" + endNum;
		}
		if ((endNum+"").length() == 3) {
			return "18900000" + endNum;
		}
		if ((endNum+"").length() == 4) {
			return "1890000" + endNum;
		}
		if ((endNum+"").length() == 5) {
			return "189000" + endNum;
		}
		return null;
	}
}
