
package org.tio.sitexxx.all.test;

import org.tio.sitexxx.service.init.JFInit;
import org.tio.sitexxx.service.init.PropInit;
import org.tio.sitexxx.service.timetask.MySqlBackupJob;

/**
 * @author tanyaowu
 */
public class MySqlBackupTest {

	/**
	 * 
	 */
	public MySqlBackupTest() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		PropInit.init();
		JFInit.init();
		MySqlBackupJob.me.run(null);
	}

}
