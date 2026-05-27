
package org.tio.sitexxx.all;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.sitexxx.service.service.base.UserService;

/**
 * @author tanyaowu 
 * 2016年12月16日 下午12:05:15
 */
public class UserMain {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(UserMain.class);

	/**
	 * 
	 * @author tanyaowu
	 */
	public UserMain() {
	}

	/**
	 * @param args
	 * @author tanyaowu
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		org.tio.sitexxx.all.Starter.initBase();

		//		for (int i = 0; i < 20; i++) {
		//			EmailServer emailServer = EmailServerService.me.next();
		//			System.out.println(emailServer.getEmail());
		//		}

		String pwd = UserService.getMd5Pwd("19000000706@qq.com", "666666");
		System.out.println(pwd);
	}
}
