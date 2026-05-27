
package org.tio.mg.service.service.tioim;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.mg.service.cache.CacheConfig;
import org.tio.mg.service.cache.Caches;
import org.tio.mg.service.model.conf.EmailServer;
import org.tio.mg.service.utils.EmailKit;
import org.tio.mg.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.utils.cache.CacheUtils;
import org.tio.utils.cache.FirsthandCreater;
import org.tio.utils.cache.ICache;

import com.alibaba.druid.filter.config.ConfigTools;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 统计管理
 * @author xufei
 * 2020年5月16日 下午2:21:06
 */
public class TioEmailService {
	
	private static Logger			log	= LoggerFactory.getLogger(TioEmailService.class);
	
	public static final TioEmailService	me	= new TioEmailService();

	private static int index = 0;

	final static EmailServer dao = new EmailServer().dao();
	
	/**
	 * 获取邮件服务器列表
	 * @return
	 * @author xufei
	 * 2020年6月28日 下午4:42:38
	 */
	public List<EmailServer> getAll() {
		ICache cache = Caches.getCache(CacheConfig.MG_TIME_TO_LIVE_MINUTE_5_LOCAL);
		String key = "EmailServerList";
		ArrayList<EmailServer> list = CacheUtils.get(cache, key, true, new FirsthandCreater<ArrayList<EmailServer>>() {
			@Override
			public ArrayList<EmailServer> create() {
				ArrayList<EmailServer> list = (ArrayList<EmailServer>) dao.find("select * from email_server where status = 1");
				return list;
			}
		});
		return list;
	}
	
	/**
	 * 获取邮件服务器列表
	 * @param searchKey
	 * @return
	 * @author xufei
	 * 2020年6月28日 下午4:42:17
	 */
	public List<EmailServer> getAll(String searchKey) {
		if(StrUtil.isBlank(searchKey)) {
			ArrayList<EmailServer> list = (ArrayList<EmailServer>) dao.find("select * from email_server ");
			return list;
		} else {
			ArrayList<EmailServer> list = (ArrayList<EmailServer>) dao.find("select * from email_server where email like '%" + searchKey + "%'");
			return list;
		}
		
	}

	/**
	 * 获取一个EmailServer
	 * @return
	 */
	public EmailServer next() {
		List<EmailServer> list = getAll();
		if (list == null || list.size() == 0) {
			log.error("没有配置邮箱服务器");
			return null;
		}

		int i = index++;
		if (i >= list.size()) {
			index = 0;
			i = index++;
		}

		return list.get(i);
	}
	
	/**
	 * 发送邮件
	 * @param startid
	 * @param endid
	 * @param title
	 * @param content
	 * @return
	 * @author xufei
	 */
	public Ret sendEmail(Integer startid,Integer endid,String title,String content){
		if(startid == null || endid == null || StrUtil.isBlank(title) || StrUtil.isBlank(content)) {
			return RetUtils.invalidParam();
		}
		if(startid > endid) {
			return RetUtils.failMsg("起始id大于截至id");
		}
		EmailServer emailServer = next();
		String emailPass = "";
		try {
			emailPass = EmailKit.getEmailUserPwd(emailServer.getPwd());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		int sendCount = 0;
		List<Record> list =  Db.use(Const.Db.TIO_SITE_MAIN).find("select loginname from `user` where id >=? and id <=? and `status` = 1 and  loginname REGEXP '^[A-Z0-9._%-]+@[A-Z0-9.-]+.[A-Z]{2,4}$'",startid,endid);
		if(CollectionUtil.isNotEmpty(list)) {
			for(Record record : list) {
				String loginName = record.getStr("loginname");
				EmailKit.sendEmail(emailServer.getServer(),emailServer.getEmail(),emailPass, loginName, title, content);
				sendCount ++;
			}
		}
		return RetUtils.okData("成功发送邮件" + sendCount + "次");
	}
	
	/**
	 * 邮件服务器是否存在
	 * @param email
	 * @return
	 * @author xufei
	 * 2020年6月28日 下午4:54:09
	 */
	public boolean exist (String email) {
		List<EmailServer> records =  dao.find("select * from email_server where email = ?",email);
		return CollectionUtil.isNotEmpty(records);
	}
	
	/**
	 * 保存邮件服务器
	 * @param emailServer
	 * @return
	 * @author xufei
	 * 2020年6月28日 下午4:49:03
	 */
	public Ret add(EmailServer emailServer) {
		if(StrUtil.isBlank(emailServer.getEmail())) {
			return RetUtils.failMsg("账号为空");
		}
		if(StrUtil.isBlank(emailServer.getServer())) {
			return RetUtils.failMsg("服务器为空");
		}
		if(StrUtil.isBlank(emailServer.getPwd())) {
			return RetUtils.failMsg("密码为空");
		}
		if(exist(emailServer.getEmail())) {
			return RetUtils.failMsg("邮件服务已存在");
		}
		try {
			emailServer.setPwd(ConfigTools.encrypt(emailServer.getPwd()));
		} catch (Exception e) {
			log.error(e.toString(), e);
			return RetUtils.failMsg("加密失败");
		}
		boolean save = emailServer.save();
		if(!save) {
			return RetUtils.failOper();
		}
		Caches.getCache(CacheConfig.MG_TIME_TO_LIVE_MINUTE_5_LOCAL).clear();
		index = 0;
		return RetUtils.okOper();
	}
	
	/**
	 * 修改邮件服务器
	 * @param emailServer
	 * @return
	 * @author xufei
	 * 2020年6月28日 下午4:51:01
	 */
	public Ret update(EmailServer emailServer) {
		if(StrUtil.isBlank(emailServer.getEmail())) {
			return RetUtils.failMsg("账号为空");
		}
		if(StrUtil.isBlank(emailServer.getServer())) {
			return RetUtils.failMsg("服务器为空");
		}
		if(StrUtil.isNotBlank(emailServer.getPwd())) {
			try {
				emailServer.setPwd(ConfigTools.encrypt(emailServer.getPwd()));
			} catch (Exception e) {
				log.error(e.toString(), e);
				return RetUtils.failMsg("加密失败");
			}
		}
		boolean update = emailServer.update();
		if(!update) {
			return RetUtils.failOper();
		}
		Caches.getCache(CacheConfig.MG_TIME_TO_LIVE_MINUTE_5_LOCAL).clear();
		index = 0;
		return RetUtils.okOper();
	}
	
	/**
	 * 邮件服务删除-真删除，如果假删除，需要进行表结构修改
	 * @param email
	 * @return
	 * @author xufei
	 * 2020年6月28日 下午4:56:53
	 */
	public Ret del(String email) {
		if(StrUtil.isBlank(email)) {
			return RetUtils.failMsg("账号为空");
		}
		EmailServer emailServer = dao.findById(email);
		if(emailServer == null) {
			return RetUtils.failMsg("数据不存在");
		}
		boolean update = dao.deleteById(email);
		if(!update) {
			return RetUtils.failOper();
		}
		Caches.getCache(CacheConfig.MG_TIME_TO_LIVE_MINUTE_5_LOCAL).clear();
		index = 0;
		return RetUtils.okOper();
	}
}
