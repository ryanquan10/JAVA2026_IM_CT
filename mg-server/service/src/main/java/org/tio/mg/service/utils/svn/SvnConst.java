
package org.tio.mg.service.utils.svn;

import org.tio.utils.jfinal.P;

/**
 * @author tanyaowu
 */
public class SvnConst {
	/**
	 * SVN路径
	 */
	public static final String	SVN_URL			= P.get("qj.svn.url");
	/**
	 * SVN导出哪个目录
	 */
	public static final String	SVN_LOCAL_DIR	= P.get("qj.svn.local.dir");
	/**
	 * SVN用户名
	 */
	public static final String	SVN_USERNAME	= P.get("qj.svn.username");
	/**
	 * SVN用户密码
	 */
	public static final String	SVN_PWD			= P.get("qj.svn.pwd");

}
