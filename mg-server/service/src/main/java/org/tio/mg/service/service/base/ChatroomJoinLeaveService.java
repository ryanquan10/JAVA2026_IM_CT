
package org.tio.mg.service.service.base;

import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Page;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.mg.service.model.main.User;

/**
 * @author tanyaowu 
 * 2016年9月15日 下午1:42:50
 */
public class ChatroomJoinLeaveService {
	public static final ChatroomJoinLeaveService me = new ChatroomJoinLeaveService();

	/**
	 * 分页查询某用户的访问日志
	 * @param curr
	 * @param uid 查询谁的访问日志
	 * @param pageNumber
	 * @return
	 * @author tanyaowu
	 */
	public Page<Record> page(User curr, Integer uid, Integer pageNumber) {
		boolean isSuper = UserService.isSuper(curr);
		if (!isSuper) { //非超管不能查别人的登录日志
			uid = curr.getId();
		}
		Integer pageSize = 10;
		Kv params = Kv.by("uid", uid);
		SqlPara sqlPara = User.dao.getSqlPara("user.pageAccessLog", params);
		Page<Record> page = Db.paginate(pageNumber, pageSize, sqlPara);
		return page;
	}

}
