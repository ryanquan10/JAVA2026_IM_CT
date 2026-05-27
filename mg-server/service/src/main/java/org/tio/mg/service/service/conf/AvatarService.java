
package org.tio.mg.service.service.conf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.mg.service.model.conf.Avatar;
import org.tio.sitexxx.service.vo.Const;
import org.tio.utils.lock.LockUtils;

import cn.hutool.core.util.RandomUtil;

/**
 * 头像库
 * @author tanyaowu
 *
 */
public class AvatarService {
	private static Logger				log			= LoggerFactory.getLogger(AvatarService.class);
	public static final AvatarService	me			= new AvatarService();
	final static Avatar					dao			= new Avatar().dao();
	/**
	 * key: id,
	 * value: Avatar
	 */
	private static Map<Integer, Avatar>	cacheMap	= null;
	private static List<Avatar>			cacheList	= null;
	private static List<Avatar>			maleList	= null;
	private static List<Avatar>			femalelist	= null;
	private static List<Avatar>			secretlist	= null;

	public final static String male = Const.UserSex.MALE + "";
	
	public final static String female = Const.UserSex.FEMALE + "";
	
	public final static String secret = Const.UserSex.SECRET + "";
	
	/**
	 * 
	 */
	public static void clearCache() {
		synchronized (AvatarService.class) {
			cacheMap = null;
			cacheList = null;
			maleList = null;
			femalelist = null;
			secretlist = null;
		}
	}

	public boolean save(Avatar avatar) {
		return avatar.save();
	}

	public static void loadData() {
		clearCache();
		Map<Integer, Avatar> tempCache = new HashMap<>();
		List<Avatar> list = null;
		maleList = new ArrayList<Avatar>();
		femalelist = new ArrayList<Avatar>();
		secretlist = new ArrayList<Avatar>();
		try {
			int total = Db.use(Const.Db.TIO_SITE_CONF).queryInt("select count(*) from avatar");
			int count = 1000;
			int start = 0;

			if (total > count) {
				start = RandomUtil.randomInt(0, total - count);
			}

			list = dao.find("select * from avatar limit " + start + ", " + count);

			if (list == null) {
				list = new ArrayList<>();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		if (list == null || list.size() == 0) {
			return;
		}

		for (Avatar avatar : list) {
			tempCache.put(avatar.getId(), avatar);
			if(Objects.equals(avatar.getType(), male)) {
				maleList.add(avatar);
			}
			if(Objects.equals(avatar.getType(), female)) {
				femalelist.add(avatar);
			}
			if(Objects.equals(avatar.getType(), secret)) {
				secretlist.add(avatar);
			}
		}
		cacheList = list;
		cacheMap = tempCache;
	}

	public static String nextAvatar() {
		try {
			int index = RandomUtil.randomInt(0, cacheList.size());
			return cacheList.get(index).getPath();
		} catch (Exception e) {
			log.error(e.toString(), e);
			return org.tio.mg.service.model.main.User.DEFAULT_AVATAR;
		}
	}
	
	/**
	 * @param type
	 * @return
	 * @author xufei
	 * 2020年5月9日 下午2:44:53
	 */
	public static String nextAvatar(String type) {
		try {
			switch (type) {
			case male:
				int index1 = RandomUtil.randomInt(0, maleList.size());
				return maleList.get(index1).getPath();
			case female:
				int index2 = RandomUtil.randomInt(0, femalelist.size());
				return femalelist.get(index2).getPath();
			case secret:
				int index3 = RandomUtil.randomInt(0, secretlist.size());
				return secretlist.get(index3).getPath();
			default:
				int index4 = RandomUtil.randomInt(0, cacheList.size());
				return cacheList.get(index4).getPath();
			}
		} catch (Exception e) {
			log.error(e.toString(), e);
			return org.tio.mg.service.model.main.User.DEFAULT_AVATAR;
		}
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public static Avatar get(Integer id) {
		if (cacheMap == null) {
			try {
				LockUtils.runWriteOrWaitRead(AvatarService.class.getName(), AvatarService.class, () -> {
//					@Override
//					public void read() {
//					}

//					@Override
//					public void write() {
						if (cacheMap == null) {
							loadData();
						}
//					}
				});
			} catch (Exception e) {
				log.error(e.toString(), e);
			}
		}
		Avatar value = cacheMap.get(id);
		return value;
	}

}
