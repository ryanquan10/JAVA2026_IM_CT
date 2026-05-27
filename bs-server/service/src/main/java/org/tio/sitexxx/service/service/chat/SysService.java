
package org.tio.sitexxx.service.service.chat;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.kit.Ret;
import org.tio.sitexxx.service.model.main.WxApp;
import org.tio.sitexxx.service.model.main.WxGroupMsg;
import org.tio.sitexxx.service.model.main.WxUserReport;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.Const;

import cn.hutool.core.util.StrUtil;

/**
 * 系统服务
 * @author lixinji
 * 2020年8月25日 下午5:57:32
 */
public class SysService {
	private static Logger			log	= LoggerFactory.getLogger(SysService.class);
	public static final SysService	me	= new SysService();

	/**
	 * 版本信息
	 * @param deviceType
	 * @param version
	 * @return
	 * @author lixinji
	 * 2020年9月18日 下午1:35:03
	 */
	public Ret checkVersion(Short deviceType, String version) {
		if (StrUtil.isBlank(version)) {
			log.error("版本公司异常:版本号为空,设备类型-{}", deviceType);
			return RetUtils.failMsg("版本号为空");
		}
		WxApp sysVersion = WxApp.dao.findFirst("select * from wx_app where type = ? and `status` = ?", deviceType, Const.Status.NORMAL);
		Map<String, Object> reslut = new HashMap<>();
		if (sysVersion == null || sysVersion.getVersion().equals(version) || StrUtil.compareVersion(version, sysVersion.getVersion()) > 0) {
			reslut.put("updateflag", Const.YesOrNo.NO);
			reslut.put("url", "");
			reslut.put("version", version);
			return RetUtils.okData(reslut);
		} else {
			reslut.put("url", sysVersion.getFileurl());
			reslut.put("updateflag", Const.YesOrNo.YES);
			reslut.put("manualOperationUrl", sysVersion.getManualOperationUrl());
			if (Objects.equals(sysVersion.getUpdatemode(), Const.YesOrNo.NO)) {//不是正常更新
				reslut.put("forceflag", Const.YesOrNo.YES);
				reslut.put("version", sysVersion.getVersion());
				reslut.put("content", sysVersion.getName());
				return RetUtils.okData(reslut);
			} else {
				reslut.put("forceflag", Const.YesOrNo.NO);
				reslut.put("version", sysVersion.getVersion());
				reslut.put("content", sysVersion.getName());
				return RetUtils.okData(reslut);
			}
		}
	}

	/**
	 * 举报投诉
	 * @param uid
	 * @param touid
	 * @param groupid
	 * @param mid
	 * @param reason
	 * @return
	 * @author lixinji
	 * 2021年1月27日 下午3:14:03
	 */
	public Ret report(Integer uid, Integer touid, Long groupid, Long mid, String reason, Short devicetype, String appversion, String imgs) {
		WxUserReport report = new WxUserReport();
		report.setUid(uid);
		report.setAppversion(StrUtil.isBlank(appversion) ? "0.0.0" : appversion);
		report.setDevicetype(devicetype);
		if (touid != null) {
			report.setTouid(touid);
			report.setType(Const.WxReport.USER);
		} else if (groupid != null) {
			report.setGroupid(Math.abs(groupid));
			if (mid != null) {
				WxGroupMsg msg = WxGroupMsg.dao.findById(mid);
				if (msg != null) {
					report.setSrctext(msg.getText());
				}
				report.setMid(mid);
				report.setType(Const.WxReport.MSG);
			} else {
				report.setType(Const.WxReport.GROUP);
			}
		} else {
			report.setType(Const.WxReport.ADVISE);
		}
		report.setImgs(imgs);
		report.setReason(reason);
		report.save();
		return RetUtils.okOper();
	}
}
