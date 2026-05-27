
package org.tio.mg.service.service.tioim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Page;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.mg.service.model.main.WxApp;
import org.tio.mg.service.utils.RetUtils;
import org.tio.mg.service.vo.MgConst;

import com.google.common.base.Objects;

import cn.hutool.core.util.StrUtil;

/**
 * app管理
 * @author xufei
 * 2020年5月16日 下午2:21:06
 */
public class TioAppService {
	
	private static Logger			log	= LoggerFactory.getLogger(TioAppService.class);
	
	public static final TioAppService	me	= new TioAppService();

	public Ret appList(Integer pageNumber, Integer pageSize, String version,Short mode,Short type,Short status) {
		if(pageNumber == null || pageNumber <= 0) {
			pageNumber = 1;
		}
		if(pageSize == null || pageNumber <= 0) {
			pageSize = 16;
		}
		Kv params = Kv.create();
		if(StrUtil.isNotBlank(version)) {
			params.set("version", "%" + version + "%");
		}
		if(mode != null) {
			params.set("updatemode", mode);
		}
		if(mode != null) {
			params.set("type", type);
		}
		if(mode != null) {
			params.set("status", status);
		}
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("app.list", params);
		Page<Record> records = Db.use(MgConst.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okPage(records);
	}
	
	/**
	 * 保存app
	 * @param app
	 * @return
	 * @author xufei
	 * 2020年6月28日 下午4:49:03
	 */
	public Ret add(WxApp app) {
		if(StrUtil.isBlank(app.getName())) {
			return RetUtils.failMsg("名称为空");
		}
		if(StrUtil.isBlank(app.getVersion())) {
			return RetUtils.failMsg("版本号为空");
		}
		if(app.getUpdatemode() == null) {
			return RetUtils.failMsg("更新模式为空");
		}
		if(app.getType() == null) {
			return RetUtils.failMsg("类型为空");
		}
		if(Objects.equal(app.getType(), MgConst.AppType.ANDROID) && StrUtil.isBlank(app.getFileurl())) {
			return RetUtils.failMsg("安卓必须上传安装包");
		}
		return RetUtils.saveRet(app);
	}
	
	/**
	 * @param app
	 * @return
	 * @author xufei
	 * 2020年6月28日 下午5:37:27
	 */
	public Ret update(WxApp app) {
		if(app.getId() == null) {
			log.error("修改id为空");
			return RetUtils.failMsg("id为空");
		}
		if(StrUtil.isBlank(app.getName())) {
			return RetUtils.failMsg("名称为空");
		}
		if(StrUtil.isBlank(app.getVersion())) {
			return RetUtils.failMsg("版本号为空");
		}
		if(app.getUpdatemode() == null) {
			return RetUtils.failMsg("更新模式为空");
		}
		if(app.getType() == null) {
			return RetUtils.failMsg("类型为空");
		}
		if(Objects.equal(app.getType(), MgConst.AppType.ANDROID) && StrUtil.isBlank(app.getFileurl())) {
			return RetUtils.failMsg("安卓必须上传安装包");
		}
		return RetUtils.upateRet(app);
	}
	
	/**
	 * app记录删除-真删除
	 * @param id
	 * @return
	 * @author xufei
	 * 2020年6月28日 下午5:39:06
	 */
	public Ret del(Integer id) {
		WxApp app = WxApp.dao.findById(id);
		if(app == null) {
			return RetUtils.failMsg("数据不存在");
		}
		boolean update =  WxApp.dao.deleteById(id);
		if(!update) {
			return RetUtils.failOper();
		}
		return RetUtils.okOper();
	}
}
