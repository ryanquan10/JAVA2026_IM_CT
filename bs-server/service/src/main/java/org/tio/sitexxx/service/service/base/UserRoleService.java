
package org.tio.sitexxx.service.service.base;

import java.util.List;
import java.util.Objects;

import org.tio.sitexxx.service.model.main.User;

/**
 * 用户角色
 * 
 * @date 2016年4月18日 下午2:17:30
 */
public class UserRoleService {
	public static final UserRoleService me = new UserRoleService();

	public static final UserService userService = UserService.ME;

	public static boolean checkUserStatus(User user) {
		if (user == null) {
			return false;
		}
		return Objects.equals(user.getStatus(), User.Status.NORMAL);
	}

	public static boolean checkUserStatus(Integer uid) {
		User user = userService.getById(uid);
		return checkUserStatus(user);
	}

	/**
	 * 用户是否包含某种角色
	 * @param user
	 * @param code
	 * @return
	 * 
	 * @date 2016年4月18日 下午4:31:45
	 */
	public static boolean hasRole(User user, Short code) {
		if (user == null) {
			return false;
		}
		List<Short> roles = user.getRoles();
		if (roles == null || roles.isEmpty()) {
			return false;
		}
		return roles.contains(code);
	}

}
