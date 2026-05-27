
package org.tio.sitexxx.web.server.controller.manager;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.redisson.api.RTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.maintain.IpStats;
import org.tio.http.common.HeaderName;
import org.tio.http.common.HeaderValue;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.http.server.annotation.RequestPath;
import org.tio.http.server.util.Resps;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.sitexxx.service.init.RedisInit;
import org.tio.sitexxx.service.model.conf.Avatar;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.WxApp;
import org.tio.sitexxx.service.model.stat.TioIpPullblackLog;
import org.tio.sitexxx.service.service.base.RegisterService;
import org.tio.sitexxx.service.service.base.TioIpPullblackLogService;
import org.tio.sitexxx.service.service.base.UserService;
import org.tio.sitexxx.service.service.conf.AvatarService;
import org.tio.sitexxx.service.service.conf.IpWhiteListService;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.Const.Topic;
import org.tio.sitexxx.service.vo.Devicetype;
import org.tio.sitexxx.service.vo.topic.CleanViewCacheVo;
import org.tio.sitexxx.web.server.utils.TioIpPullblackUtils;
import org.tio.sitexxx.web.server.utils.WebUtils;
import org.tio.utils.resp.Resp;
import org.tio.utils.ui.layui.table.LayuiPage;

import cn.hutool.core.util.StrUtil;

/**
 * @author tanyaowu
 * 2016年6月29日 下午7:53:59
 */
@RequestPath(value = "/m/tio")
public class TioController {
	private static Logger log = LoggerFactory.getLogger(TioController.class);

	/**
	 * @param args
	 * @author tanyaowu
	 */
	public static void main(String[] args) {

	}
	/**
	 *
	 * @author tanyaowu
	 */
	public TioController() {
	}
	
	/**
	 * 初始化谭信信息
	 * @return
	 */
	@RequestPath(value = "/initWx")
	public Resp initWx() {
		String sql = "select * from user where id < 31329 and id > 23351";  //第一批
		sql = "select * from user where id <= 23351";  //第二批
		List<User> list = User.dao.find(sql);
		for (User user : list) {
			try {
				RegisterService.me.initWx(user);
			} catch (Exception e) {
				log.error("", e);
			}
		}
		return Resp.ok();
	}

	/**
	 * 清空静态资源数据
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestPath(value = "/clearStaticResCache")
	public Resp clearStaticResCache(HttpRequest request) throws Exception {
		CleanViewCacheVo cleanViewCacheVo = new CleanViewCacheVo();
		RTopic topic = RedisInit.get().getTopic(Topic.CLEAN_VIEW_CACHE);
		topic.publish(cleanViewCacheVo);

		return Resp.ok("缓存已经清空");
	}

	/**
	 * 获取ipStat信息
	 * @param request
	 * @param channelContext
	 * @return
	 * @throws Exception
	 * @author: tanyaowu
	 */
	@RequestPath(value = "/ipStat")
	public Resp ipStat(Long duration, HttpRequest request, ChannelContext channelContext) throws Exception {
		IpStats ipStats = channelContext.tioConfig.ipStats;
		return Resp.ok(LayuiPage.ok(ipStats.values(duration), ipStats.size(duration)));
	}

	/**
	 * 把ip列为白名单
	 * @param ip
	 * @return
	 * @throws Exception
	 */
	@RequestPath(value = "/addWhiteIp")
	public Resp addWhiteIp(HttpRequest request, String ip) throws Exception {
		ip = StrUtil.trim(ip);
		User currUser = WebUtils.currUser(request);
		String clientip = request.getClientIp();
		Integer curruserid = currUser.getId();
		String remark = curruserid + "," + clientip;
		IpWhiteListService.me.save(ip, remark);
		return Resp.ok();
	}

	/**
	 * 把自己设为白名单
	 * @param request
	 * @return
	 * @throws Exception
	 * @author tanyaowu
	 */
	@RequestPath(value = "/addSelfToWhiteIp")
	public Resp addSelfToWhiteIp(HttpRequest request) throws Exception {
		User currUser = WebUtils.currUser(request);
		String clientip = request.getClientIp();
		Integer curruserid = currUser.getId();
		String remark = "把自己设为白名单, " + curruserid + ", " + clientip;
		IpWhiteListService.me.save(clientip, remark);
		return Resp.ok();
	}

	/**
	 * 将自己从白名单中删除
	 * @param request
	 * @return
	 * @throws Exception
	 * @author tanyaowu
	 */
	@RequestPath(value = "/deleteSelfToWhiteIp")
	public Resp deleteSelfToWhiteIp(HttpRequest request) throws Exception {
		User currUser = WebUtils.currUser(request);
		String clientip = request.getClientIp();
		Integer curruserid = currUser.getId();
		String remark = "将自己从白名单中删除, " + curruserid + ", " + clientip;
		IpWhiteListService.me.delete(clientip, remark);
		return Resp.ok();
	}

	/**
	 * 删除ip白名单
	 * @param request
	 * @param ip
	 * @return
	 * @throws Exception
	 */
	@RequestPath(value = "/deleteWhiteIp")
	public Resp deleteWhiteIp(HttpRequest request, String ip) throws Exception {
		ip = StrUtil.trim(ip);
		User currUser = WebUtils.currUser(request);
		String clientip = request.getClientIp();
		Integer curruserid = currUser.getId();
		String remark = curruserid + "," + clientip;
		IpWhiteListService.me.delete(ip, remark);
		return Resp.ok();
	}

	/**
	 * 把ip列为黑名单
	 * @param ip
	 * @return
	 * @throws Exception
	 */
	@RequestPath(value = "/addBlackIp")
	public Resp addBlackIp(HttpRequest request, String ip) throws Exception {
		ip = StrUtil.trim(ip);
		User currUser = WebUtils.currUser(request);
		String clientip = request.getClientIp();
		Integer curruserid = currUser.getId();
		String remark = curruserid + "," + clientip;

		TioIpPullblackUtils.addToBlack(request, ip, remark, TioIpPullblackLog.Type.MANUAL);

		return Resp.ok();
	}

	/**
	 * 删除ip黑名单
	 * @param request
	 * @param ip
	 * @return
	 * @throws Exception
	 */
	@RequestPath(value = "/deleteBlackIp")
	public Resp deleteBlackIp(HttpRequest request, String ip) throws Exception {
		ip = StrUtil.trim(ip);
		User currUser = WebUtils.currUser(request);
		String clientip = request.getClientIp();
		Integer curruserid = currUser.getId();
		String remark = curruserid + "," + clientip;
		TioIpPullblackLogService.ME.deleteFromBlack(ip, request.getChannelContext().getServerNode().getPort(), remark);
		return Resp.ok();
	}

	/**
	 * 拉黑用户
	 * @param request
	 * @param nick
	 * @return
	 * @throws Exception
	 */
	@RequestPath(value = "/pullBlackUser")
	public Resp pullBlackUser(HttpRequest request, String nick) throws Exception {
		int r = UserService.ME.pullBlackUserByNick(nick);
		if (r > 0) {
			return Resp.ok();
		} else {
			return Resp.fail("拉黑失败，请检查昵称是否正确");
		}
	}

	/**
	 * 把用户的状态设为正常
	 * @param request
	 * @param nick
	 * @return
	 * @throws Exception
	 */
	@RequestPath(value = "/normalUser")
	public Resp normalUser(HttpRequest request, String nick) throws Exception {
		int r = UserService.ME.normalUserByNick(nick);
		if (r > 0) {
			return Resp.ok();
		} else {
			return Resp.fail("操作失败，请检查昵称是否正确");
		}
	}

	/**
	 * 清除所有用户缓存
	 * @param request
	 * @param nick
	 * @return
	 * @throws Exception
	 */
	@RequestPath(value = "/clearAllUserCache")
	public Resp clearAllUserCache(HttpRequest request) throws Exception {
		UserService.ME.notifyClearCache(null);
		return Resp.ok();
	}

	@RequestPath(value = "/clearUserCache")
	public Resp clearUserCache(HttpRequest request, String nick) throws Exception {
		User user = UserService.ME.getByNick(nick);
		if (user == null) {
			return Resp.fail("昵称不存在");
		}

		UserService.ME.notifyClearCache(user.getId());
		return Resp.ok();
	}

	/**
	 * 分页查询，只允许查前100条，防止误操作
	 * @param request
	 * @param db
	 * @param sql
	 * @return
	 * @throws Exception
	 * @author tanyaowu
	 */
	@RequestPath(value = "/q")
	public HttpResponse q(HttpRequest request, String db, String sql) throws Exception {

		if (!IpWhiteListService.isWhiteIp(request.getClientIp())) {
			return Resps.html(request, "你没资格执行该操作");
		}

		if (StrUtil.isBlank(db)) {
			db = org.tio.sitexxx.service.vo.Const.Db.TIO_SITE_MAIN;
		}
		List<Record> list = Db.use(db).find(sql);
		//		Resp ret = Resp.ok(Json.toFormatedJson(list));

		HttpResponse response = Resps.json(request, LayuiPage.ok(list, list.size()));
		response.addHeader(HeaderName.Access_Control_Allow_Origin, HeaderValue.from("*"));
		response.addHeader(HeaderName.Access_Control_Allow_Headers, HeaderValue.from("x-requested-with,content-type"));
		return response;
	}

	/**
	 * 重建头像
	 * @param request
	 * @param db
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	@RequestPath(value = "/avatar")
	public HttpResponse avatar(HttpRequest request) throws Exception {
		if (true) {
			return null;
		}

		String root = org.tio.sitexxx.service.vo.Const.RES_ROOT;
		String avatarRoot = root + "/avatar";
		int rootLen = root.length();
		//		int avatarRootLen = avatarRoot.length();
		List<File> list = cn.hutool.core.io.FileUtil.loopFiles(avatarRoot);

		int c = 0;
		int dirC = 0;
		for (File file : list) {
			if (file.isDirectory()) {
				dirC++;
				continue;
			}
			String absPath = StrUtil.replace(file.getAbsolutePath(), "\\", "/");
			String path = absPath.substring(rootLen);
			String[] pathes = StrUtil.splitToArray(path, "/");
			//			int x = path.indexOf("\\\\/");
			String type = pathes[2];

			Avatar avatar = new Avatar();
			avatar.setInitUrl(path);
			avatar.setPath(path);
			avatar.setType(type);
			try {
				boolean f = AvatarService.me.save(avatar);
				if (f) {
					c++;
				}
			} catch (Exception e) {
				log.error("", e);
			}
		}
		String msg = "文件数:" + list.size() + ", 目录数：" + dirC + ", 插入数:" + c;
		System.out.println(msg);

		return Resps.json(request, msg);
	}

	/**
	 * 根据loginname添加角色
	 * @param request
	 * @param loginname
	 * @param roleid
	 * @return
	 * @author tanyaowu
	 */
	@RequestPath(value = "/addRoleByLoginname")
	public Resp addRoleByLoginname(HttpRequest request, String loginname, short roleid) {
		return UserService.ME.addRoleByLoginname(loginname, roleid);
	}

	/**
	 * 根据nick添加
	 * @param request
	 * @param nick
	 * @param roleid
	 * @return
	 * @author tanyaowu
	 */
	@RequestPath(value = "/addRoleByLNick")
	public Resp addRoleByLNick(HttpRequest request, String nick, short roleid) {
		return UserService.ME.addRoleByNick(nick, roleid);
	}
	
	
//	@RequestPath(value = "/changeIp")
//	public Resp changeIp(HttpRequest request) {
//		int maxId = 23356;
//
//		List<IpInfo> iplist = IpInfo.dao.find("select * from ip_info where time < '2018-11-06 22:07:23' and time > '2018-10-10 22:07:23' order by id");
//
//		List<Integer> userlist = Db.use(Const.Db.TIO_SITE_MAIN).query("select id from user where id <= ?", maxId);
//		
//		int ipindex = 0;
//		for (Integer uid : userlist) {
//			
//			IpInfo ipInfo = iplist.get(ipindex);
//			
//			String sql = "update user set ipid=?, createtime=?, updatetime=? where id=?";
//			Db.use(Const.Db.TIO_SITE_MAIN).update(sql, ipInfo.getId(), ipInfo.getTime(), ipInfo.getTime(), uid);
//			
//			ipindex += 24;
//		}
//		
//		return Resp.ok();
//	}
	/**
	 * 版本信息
	 * @param request
	 * @return
	 * @throws Exception
	 * @author lixinji
	 * 2020年9月23日 下午6:58:34
	 */
	@RequestPath(value = "/tioAppConfig")
	public Resp tioAppConfig(HttpRequest request) throws Exception {
		WxApp sysVersion = WxApp.dao.findFirst(
				"select * from wx_app where type = ? and `status` = ?", Devicetype.ANDROID.getValue(),
				Const.Status.NORMAL);
		Map<String, WxApp> config = new HashMap<String, WxApp>();
		config.put("android", sysVersion);
		return Resp.ok(config);
	}

}
