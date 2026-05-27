
package org.tio.sitexxx.service.service.base;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.sitexxx.service.model.main.UserToken;

/**
 * @author tanyaowu 
 * 2016年9月15日 下午1:42:50
 */
public class UserTokenService {
	@SuppressWarnings("unused")
	private static Logger					log	= LoggerFactory.getLogger(UserTokenService.class);
	public static final UserTokenService	me	= new UserTokenService();
	public static final UserToken			dao	= new UserToken().dao();

	/**
	 * 
	 * @author: tanyaowu
	 */
	public UserTokenService() {
	}

	/**
	 * 
	 * @param devicetype
	 * @param uid
	 * @return
	 * @author: tanyaowu
	 */
	public UserToken find(int devicetype, int uid) {
		UserToken userToken = dao.findFirst("select * from user_token where devicetype=? and uid=? limit 1", devicetype, uid);
		return userToken;
	}

	/**
	 * 
	 * @param uid
	 * @return
	 */
	public List<UserToken> find(int uid) {
		List<UserToken> list = dao.find("select * from user_token where uid=?", uid);
		return list;
	}

	/**
	 * 
	 * @param devicetype
	 * @param uid
	 * @return
	 * @author: tanyaowu
	 */
	public int delete(int devicetype, int uid, String token) {
		return Db.update("delete from user_token where uid=? and devicetype=? and token=?", uid, devicetype, token);
	}

	public int delete(int uid) {
		return Db.update("delete from user_token where uid=?", uid);
	}

	/**
	 * 
	 * @param userToken
	 * @author: tanyaowu
	 */
	public void add(UserToken userToken) {
		userToken.save();
	}

	/**
	 * 
	 * @param userToken
	 * @author: tanyaowu
	 */
	public void update(UserToken userToken) {
		userToken.update();
	}

	/**
	 * @param args
	 * @author: tanyaowu
	 */
	public static void main(String[] args) {

	}
}
