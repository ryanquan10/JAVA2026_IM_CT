
package org.tio.mg.service.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.mg.service.cache.CacheConfig;
import org.tio.mg.service.cache.Caches;
import org.tio.mg.service.model.conf.Area;
import org.tio.mg.service.model.conf.Dict;
import org.tio.mg.service.model.main.UserBase;
import org.tio.mg.service.service.conf.AreaService;
import org.tio.mg.service.service.conf.MgDictService;
import org.tio.sitexxx.service.vo.Const;
import org.tio.utils.cache.CacheUtils;
import org.tio.utils.cache.FirsthandCreater;
import org.tio.utils.cache.ICache;
import org.tio.utils.resp.Resp;

import cn.hutool.core.util.StrUtil;

/**
 * 用户基础信息业务类
 * 
 *
 */
public class UserBaseService {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(UserBaseService.class);

	public static final UserBaseService me = new UserBaseService();

	final UserBase userBaseDao = new UserBase().dao();

	/**
	 * 用户基础信息-缓存
	 * 包含但不仅限于：
	 * 1、用户进本信息
	 * 2、用实名信息
	 * 3、用户nick和头像
	 * @param uid
	 * @return
	 * 
	 */
	public UserBase getUserBaseByUid(Integer uid) {
		ICache userbaseCache = Caches.getCache(CacheConfig.USERID_BASE);
		String key = uid + "";
		UserBase userBase1 = CacheUtils.get(userbaseCache, key, new FirsthandCreater<UserBase>() {
			@Override
			public UserBase create() {
				UserBase userBase = userBaseDao.findFirst("select * from user_base where uid = ?",uid);
				if (userBase != null) {
					String areaCode = userBase.getArea();
					if (StrUtil.isNotBlank(areaCode)) {
						Area area = AreaService.getParent(areaCode);
						if (area != null) {
							List<String> areaNameList = new ArrayList<>();
							List<String> areaCodeList = new ArrayList<>();
							areaNameList.add(area.getName());
							areaCodeList.add(area.getCode());
							while (area.getParentArea() != null) {
								area = area.getParentArea();
								areaNameList.add(area.getName());
								areaCodeList.add(area.getCode());
							}
							userBase.setAreas(areaNameList);
							userBase.setAreacodes(areaCodeList);
						}
					}
					String income = userBase.getIncome();
					if (StrUtil.isNotBlank(income)) {
						Dict dict = MgDictService.getDictByCode(income);
						if (dict != null && Objects.equals(dict.getStatus(), Const.Status.NORMAL)) {
							userBase.setIncomeName(dict.getName());
						}
					}
					String industry = userBase.getIndustry();
					if (StrUtil.isNotBlank(industry)) {
						Dict dict = MgDictService.getDictByCode(industry);
						if (dict != null && Objects.equals(dict.getStatus(), Const.Status.NORMAL)) {
							userBase.setIndustryName(dict.getName());
						}
					}
					String position = userBase.getPosition();
					if (StrUtil.isNotBlank(position)) {
						Dict dict = MgDictService.getDictByCode(position);
						if (dict != null && Objects.equals(dict.getStatus(), Const.Status.NORMAL)) {
							userBase.setPositionName(dict.getName());
						}
					}
				}
				return userBase;
			}
		});
		return userBase1;
	}

	/**
	 * 修改用户基础数据-清空缓存
	 * @param userBase
	 * @return
	 * 
	 */
	public Resp updateBase(UserBase userBase) {
		boolean isUpdate = userBase.update();
		if (!isUpdate) {
			return Resp.fail("用户基础信息修改失败");
		}
		Caches.getCache(CacheConfig.USERID_BASE).remove(userBase.getUid() + "");
		return Resp.ok("用户修改成功.");
	}

	public static void main(String[] args) throws Exception {
	}

}
