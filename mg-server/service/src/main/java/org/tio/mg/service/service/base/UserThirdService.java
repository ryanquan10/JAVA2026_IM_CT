
package org.tio.mg.service.service.base;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.mg.service.cache.CacheConfig;
import org.tio.mg.service.cache.Caches;
import org.tio.mg.service.model.main.UserThird;
import org.tio.mg.service.model.main.UserThird.SubTable;
import org.tio.utils.cache.CacheUtils;
import org.tio.utils.cache.FirsthandCreater;
import org.tio.utils.cache.ICache;

/**
 * @author tanyaowu
 * 2016年8月10日 上午11:09:59
 */
public class UserThirdService {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(UserThirdService.class);

	public static final UserThirdService me = new UserThirdService();

	final UserThird nullUser = new UserThird();

	ICache openidCache;

	ICache uidCache;

	/**
	 *
	 * @author tanyaowu
	 */
	private UserThirdService() {
		openidCache = Caches.getCache(CacheConfig.OPENID_USERTHIRD);
		uidCache = Caches.getCache(CacheConfig.UID_USERTHIRD);
	}

	public boolean save(UserThird userThird) throws SQLException {
		boolean flag = userThird.save();
		SubTable sub = userThird.getSubTable();
		if (sub != null) {
			sub.setUserThirdId(userThird.getId());
			sub.save();

			//			Integer type = userThird.getType();
			//			if (Objects.equals(UserThird.Type.QQ, type) || Objects.equals(UserThird.Type.QQ_MOBILE, type)) {
			//				UserThirdQq userThirdQq = (UserThirdQq) sub;
			//				userThirdQq.setUserThirdId(userThird.getId());
			//				userThirdQq.save();
			//			} else if (Objects.equals(UserThird.Type.WX, type) || Objects.equals(UserThird.Type.WX_MOBILE, type) || Objects.equals(UserThird.Type.WX_OPEN, type)) {
			//				UserThirdWx userThirdWx = (UserThirdWx) sub;
			//				userThirdWx.setUserThirdId(userThird.getId());
			//				userThirdWx.save();
			//			} else if (Objects.equals(UserThird.Type.WB, type) || Objects.equals(UserThird.Type.WB_MOBILE, type)) {
			//				UserThirdWeibo userThirdWeibo = (UserThirdWeibo) sub;
			//				userThirdWeibo.setUserThirdId(userThird.getId());
			//				userThirdWeibo.save();
			//			} else if(Objects.equals(UserThird.Type.OSC, type)) {
			//				UserThirdOsc userThirdOsc = (UserThirdOsc) sub;
			//				userThirdOsc.setUserThirdId(userThird.getId());
			//				userThirdOsc.save();
			//			}
		}
		return flag;
	}

	/**
	 * 
	 * @param type
	 * @param typeStr
	 * @param openid
	 * @return
	 * @author tanyaowu
	 */
	public UserThird getByOpenid(Integer type, String typeStr, String openid) {
		if (openid == null || typeStr == null || type == null) {
			return null;
		}
		String key = typeStr + "_" + openid;
		UserThird userThird = openidCache.get(key, UserThird.class);
		if (userThird == null) {

			userThird = UserThird.dao.findFirst("select * from user_third where type in (" + typeStr + ") and openid=?", openid);
			if (userThird != null) {
				openidCache.put(key, userThird);
			} else {
				openidCache.putTemporary(key, nullUser);
			}
		} else {
			if (userThird.getId() == null) {
				return null;
			}
		}
		return userThird;
	}

	/**
	 * 
	 * @param type
	 * @param typeStr
	 * @param unionid
	 * @return
	 * @author tanyaowu
	 */
	public UserThird getByUnionid(Integer type, String typeStr, String unionid) {
		if (unionid == null || typeStr == null || type == null) {
			return null;
		}
		String key = typeStr + "_" + unionid;
		UserThird userThird = openidCache.get(key, UserThird.class);
		if (userThird == null) {

			userThird = UserThird.dao.findFirst("select * from user_third where type in (" + typeStr + ") and unionid=?", unionid);
			if (userThird != null) {
				openidCache.put(key, userThird);
			} else {
				openidCache.putTemporary(key, nullUser);
			}
		} else {
			if (userThird.getId() == null) {
				return null;
			}
		}
		return userThird;
	}

	//	/**
	//	 * 
	//	 * @param type
	//	 * @param unionid
	//	 * @return
	//	 */
	//	public UserThird getByUnionid(Integer type, String unionid) {
	//		if (unionid == null || type == null) {
	//			return null;
	//		}
	//		String key = type + "_" + unionid;
	//		UserThird userThird = openidCache.get(key, UserThird.class);
	//		if (userThird == null) {
	//			userThird = dao.findFirst("select * from user_third where type=? and unionid=?", type, unionid);
	//			if (userThird != null) {
	//				openidCache.put(key, userThird);
	//			} else {
	//				openidCache.putTemporary(key, nullUser);
	//			}
	//		} else {
	//			if (userThird.getId() == null) {
	//				return null;
	//			}
	//		}
	//		return userThird;
	//	}
	//	
	//	

	/**
	 * 
	 * @param uid
	 * @return
	 */
	public UserThird getByUid(Integer uid) {
		if (uid == null) {
			return null;
		}

		String key = uid + "";
		boolean putTempToCacheIfNull = false;
		UserThird userThird = CacheUtils.get(uidCache, key, putTempToCacheIfNull, new FirsthandCreater<UserThird>() {
			@Override
			public UserThird create() {
				UserThird userThird = UserThird.dao.findFirst("select * from user_third where uid=?", uid);
				return userThird;
			}
		});
		return userThird;

	}
}
