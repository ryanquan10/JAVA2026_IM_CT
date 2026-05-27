
package org.tio.mg.service.service.mg;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.mg.service.model.mg.MgUserToken;
import org.tio.mg.service.vo.MgConst;

/**
 * 后台用户token表
 * @author xufei
 * 2020年5月26日 下午2:56:24
 */
public class MgUserTokenService {
	@SuppressWarnings("unused")
	private static Logger					log	= LoggerFactory.getLogger(MgUserTokenService.class);
	public static final MgUserTokenService	me	= new MgUserTokenService();
	public static final MgUserToken			dao	= new MgUserToken().dao();

	/**
	 * 
	 * @author: tanyaowu
	 */
	public MgUserTokenService() {
	}

	/**
	 * 获取后台用户的登录token
	 * @param devicetype
	 * @param uid
	 * @return
	 * @author: tanyaowu
	 */
	public MgUserToken find(int devicetype, int mguid) {
		MgUserToken MgUserToken = dao.findFirst("select * from mg_user_token where devicetype=? and mguid=? limit 1", devicetype, mguid);
		return MgUserToken;
	}

	/**
	 * 获取后台用户的登录token列表
	 * @param uid
	 * @return
	 */
	public List<MgUserToken> find(int mguid) {
		List<MgUserToken> list = dao.find("select * from mg_user_token where mguid=?", mguid);
		return list;
	}

	/**
	 * 删除已登录的token
	 * @param devicetype
	 * @param uid
	 * @return
	 * @author: tanyaowu
	 */
	public int delete(int devicetype, int mguid, String token) {
		return Db.use(MgConst.Db.TIO_MG).update("delete from mg_user_token where mguid=? and devicetype=? and token=?", mguid, devicetype, token);
	}

	/**
	 * 删除用户的所有token
	 * @param mguid
	 * @return
	 * @author xufei
	 * 2020年6月18日 下午3:26:37
	 */
	public int delete(int mguid) {
		return Db.use(MgConst.Db.TIO_MG).update("delete from mg_user_token where mguid=?", mguid);
	}

	/**
	 * 新增token
	 * @param MgUserToken
	 * @author: tanyaowu
	 */
	public void add(MgUserToken MgUserToken) {
		MgUserToken.replaceSave();
	}

	/**
	 * 修改token
	 * @param MgUserToken
	 * @author: tanyaowu
	 */
	public void update(MgUserToken MgUserToken) {
		MgUserToken.update();
	}

	/**
	 * @param args
	 * @author: tanyaowu
	 */
	public static void main(String[] args) {

	}
}
