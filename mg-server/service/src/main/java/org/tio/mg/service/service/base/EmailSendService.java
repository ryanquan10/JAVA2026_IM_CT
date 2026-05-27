
/**
 * 
 */
package org.tio.mg.service.service.base;

import java.io.Serializable;

/**
 * @author tanyaowu
 *
 */
public class EmailSendService {

	public static final EmailSendService me = new EmailSendService();

	/**
	 * 
	 */
	public EmailSendService() {
	}

	public static class EmailSendVo implements Serializable {
		/**
		 * 
		 */
		private static final long	serialVersionUID	= 361963493750205411L;
		String						emailServer, fromEmail, emailPass, toEmail, title, content;

		public EmailSendVo(String emailServer, String fromEmail, String emailPass, String toEmail, String title, String content) {
			super();
			this.emailServer = emailServer;
			this.fromEmail = fromEmail;
			this.emailPass = emailPass;
			this.toEmail = toEmail;
			this.title = title;
			this.content = content;
		}

	}

}
