
package org.tio.mg.web.server.auth;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.mg.service.model.main.User;
import org.tio.mg.service.service.base.UserRoleService;
import org.tio.mg.service.service.base.UserService;
import org.tio.sitexxx.service.vo.Const;

import cn.hutool.core.util.StrUtil;

/**
 * 
 * @author tanyaowu
 */
public class AccessCtrlService {
	private static Logger log = LoggerFactory.getLogger(AccessCtrlService.class);

	public static final String PATH_CURR_USER = "/user/curr";

	/**
	 * 
	 */
	public AccessCtrlService() {
	}

	/**
	 * 指定的用户是否可以访问 指定的servletpath
	 * @param accessCtrlConfig
	 * @param userid
	 * @param servletPath1
	 * @return
	 * @throws IOException
	 */
	public static boolean canAccess(AccessCtrlConfig accessCtrlConfig, Integer userid, String servletPath1) throws IOException {
		if (accessCtrlConfig == null) {
			log.info("没有配置权限控制，说明不需要进行权限控制");
			return true;
		}

		String servletPath = servletPath1;
		//		if (StrUtil.isBlank(servletPath))
		//		{
		//			servletPath = servletPath1;
		//		}

		if (Const.USE_ANONYMOUS && PATH_CURR_USER.equals(servletPath)) {
			return true;
		}

		Object neededRolecodes = accessCtrlConfig.getNeededRolecodes(servletPath);//AccessCtrlConfig.getInstance().getProperty(servletPath);
		if (neededRolecodes != null) {
			//			if (StrUtil.isBlank(userid))
			//			{
			//				return false; //没有登录，则直接返回false
			//			}

			boolean b = false;
			if (neededRolecodes instanceof List) {
				@SuppressWarnings("unchecked")
				List<String> _neededRolecodes = (List<String>) neededRolecodes;

				for (String neededRolecode : _neededRolecodes) {
					b = isMeetRoleExp(neededRolecode, userid);
					if (b) {
						break;
					}
				}
			} else {
				b = isMeetRoleExp((String) neededRolecodes, userid);
			}

			return b;
		} else {
			return true;
		}

	}

	/**
	 * 
	 * 是否有指定角色
	 * @param userid
	 * @param code
	 * @return
	 * @throws Exception
	 */
	public static boolean hasRole(Integer userid, Short code) throws Exception {
		User user = UserService.ME.getById(userid);
		return hasRole(user, code);
	}

	public static boolean hasRole(User user, Short code) throws Exception {
		if (user == null) {
			return false;
		}
		if (!UserRoleService.checkUserStatus(user)) {
			return false;
		}
		List<Short> roles = user.getRoles();
		if (roles == null || roles.isEmpty()) {
			return false;
		}
		return roles.contains(code);
		//		return Objects.equals(code, _code);
	}

	/**
	 * 用户是否符合角色编码表达式<br>
	 * 如果neededRolecode是true或""，则直接返回true<br>
	 * 如果neededRolecode是false，则直接返回false<br>
	 * @param roleCodeExp 角色编码表达式， 可能是""、"true"、"false"、"1|3"、"1&2"、"1"、"*"、"(3&2)|1"、"!2"
	 * @param userid 可能是null
	 * @return true: 表示有指定的角色
	 * @throws IOException
	 * @throws ServletException
	 * @author: tanyaowu
	 * @创建时间:　2013年5月30日 上午10:59:33
	 */
	private static Boolean isMeetRoleExp(String roleCodeExp, Integer userid) throws IOException {
		if ("true".equals(roleCodeExp) || "".equals(roleCodeExp)) {
			return true;
		} else if ("false".equals(roleCodeExp)) {
			return false;
		} else if (roleCodeExp.contains("(") && roleCodeExp.contains(")")) {
			String rolecode = getBracketValue(roleCodeExp); //3|6
			Boolean ret = isMeetRoleExp(rolecode, userid); //
			roleCodeExp = replaceBracketValue(roleCodeExp, ret.toString()); //4|5|(3|6)  --> 4|5|true
			return isMeetRoleExp(roleCodeExp, userid);
		} else if (roleCodeExp.contains("&")) {
			String[] codes = StrUtil.splitToArray(roleCodeExp, "&");
			boolean ret = true;
			for (String code : codes) {
				ret = ret && isMeetRoleExp(code, userid);
				if (ret == false) {
					return false;
				}
			}
			return true;
		} else if (roleCodeExp.contains("|")) {
			String[] codes = StrUtil.splitToArray(roleCodeExp, "|");
			boolean ret = false;
			for (String code : codes) {
				ret = ret || isMeetRoleExp(code, userid);
				if (ret == true) {
					return true;
				}
			}
			return false;
		} else if (roleCodeExp.startsWith("!")) {
			String excludedRolecode = roleCodeExp.substring(1);
			try {
				if (userid == null) {
					return true;
				}
				if (hasRole(userid, Short.valueOf(excludedRolecode))) {
					return false;
				}
			} catch (Throwable e) {
				log.error(e.toString(), e);
				return false;
			}
		} else {
			if ("*".equals(roleCodeExp)) {
				if (userid != null) {
					return true;
				} else {
					return false;
				}
			} else {
				try {
					if (userid == null || !hasRole(userid, Short.valueOf(roleCodeExp))) {
						return false;
					}
				} catch (Throwable e) {
					log.error(e.toString(), e);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 获取一个小括号里的值，譬如"4|(5&(3|6))"，就会返回3|6
	 * @param input
	 * @return
	 */
	private static String getBracketValue(String input) {
		String regex = "\\({1}[^\\(\\)]+\\){1}";

		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(input);

		String reserved = "";
		if (matcher.find()) {
			reserved = matcher.group(0);
		}

		String ret = replaceAll(reserved, "\\(|\\)", ""); //替换)和(
		//		reserved = reserved.replace("(", "");
		//		reserved = reserved.replace(")", "");

		return ret;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//		String pkg = "com.talent.app";
		//		try
		//		{
		//			long start = SystemTimer.currTime;
		//
		//			Map<String, List<String>> dd = com.talent.web.utils.SpringMappingUtils.getMapOfControllerclsAndRequestmappings(pkg);
		//			long end = SystemTimer.currTime;
		//			System.out.println("耗时:" + (end - start) + "ms");
		//
		//			start = SystemTimer.currTime;
		//			dd = com.talent.web.utils.SpringMappingUtils.getMapOfControllerclsAndRequestmappings(pkg);
		//			end = SystemTimer.currTime;
		//			System.out.println("耗时:" + (end - start) + "ms");
		//
		//			System.out.println(Json.toJson(dd));
		//
		//			Map<String, String> ss = SpringMappingUtils.getMapOfRequestmappingAndMethod(pkg);
		//			System.out.println(Json.toJson(ss));
		//
		//		} catch (IOException e)
		//		{
		//			log.error(e.toString(), e);
		//		}

		String input = "4|(5&(3|6))";
		String ret = replaceBracketValue(input, "false");
		System.out.println(ret);

		String xx = getBracketValue(input);
		System.out.println(xx);

		//		String servletPath = "dddddd";
		//		String suffix = FilenameUtils.getExtension(servletPath);
		//
		//		boolean b11 = "1&2".contains("&");
		//		String[] sb = StrUtil.splitToArray("1&2", "&");
		//		///usercenter/buyprops/*
		//
		//		//		String likeType = "/usercenter/buycoin/batchDelete.talent";
		//		String pattern = "/usercenter/buyprops/\\w*";
		//		String sourceStr = "/usercenter/buyprops/d";
		//
		//		Pattern p = Pattern.compile(pattern); // 正则表达式
		//		Matcher m = p.matcher(sourceStr); // 操作的字符串 
		//		boolean b = m.matches(); //返回是否匹配的结果 
		//		log.info(b + "");
		//
		//		Pattern p1 = Pattern.compile("a*b");
		//		Matcher m1 = p1.matcher("aaaaab");
		//		boolean b1 = m1.matches();
		//		log.info(b1 + "");
	}

	public static String replaceAll(String input, String regex, String replacement) {
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(input);
		String result = m.replaceAll(replacement);
		return result;
	}

	/**
	 * 用指定值替换括号里的值，譬如replaceBracketValue("4|(5&(3|6))", "false")就会返回4|(5&false)
	 * @param str
	 * @return
	 */
	private static String replaceBracketValue(String input, String value) {
		String regex = "\\({1}[^\\(\\)]+\\){1}";
		return replaceAll(input, regex, value);
	}

}
