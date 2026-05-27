
package org.tio.sitexxx.service.utils;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.kit.StrKit;
import org.tio.sitexxx.service.tool.druid.ConfigTools;

/**
 * 邮件发送工具类
 */
public class EmailKit {

	public static final Logger log = LoggerFactory.getLogger(EmailKit.class);

	public static void main(String[] args) {
		String ret = sendEmail("abc.com", // 邮件发送服务器地址
		        "no-reply@abc.com", // 发件邮箱
		        null, // 发件邮箱密码
		        "test@test.com", // 收件地址
		        "邮件标题", // 邮件标题
		        "content"); // 邮件内容
		System.out.println("发送返回值: " + ret);
	}

	public static String sendEmail(String fromEmail, String toEmail, String title, String content) {
		return sendEmail(null, fromEmail, null, toEmail, title, content);
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public static String getEmailUserPwd(String encodedpwd) throws Exception {
		String emailPass = ConfigTools.decrypt(encodedpwd);
		//tanyaowutywo13141015
		emailPass = emailPass.substring(8, emailPass.length() - 4);
		return emailPass;
	}

	/**
	 *
	 * @param emailServer
	 * @param fromEmail
	 * @param password
	 * @param toEmail
	 * @param title
	 * @param content 支持html
	 * @return
	 * @author tanyaowu
	 */
	public static String sendEmail(String emailServer, String fromEmail, String password, String toEmail, String title, String content) {

		HtmlEmail email = new HtmlEmail();
		if (StrKit.notBlank(emailServer)) {
			email.setHostName(emailServer);
		} else {
			// 默认使用本地 postfix 发送，这样就可以将postfix 的 mynetworks 配置为 127.0.0.1 或 127.0.0.0/8 了
			email.setHostName("127.0.0.1");
		}

		email.setSSLOnConnect(true);

		// 如果密码为空，则不进行认证
		if (StrKit.notBlank(password)) {
			email.setAuthentication(fromEmail, password);
		}

		email.setCharset("utf-8");
		try {
			email.addTo(toEmail);
			email.setFrom(fromEmail);
			email.setSubject(title);
			//			email.setMsg(content);
			email.setHtmlMsg(content);
			return email.send();
		} catch (EmailException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
}
