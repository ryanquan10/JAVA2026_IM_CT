
/**
 * 
 */
package org.tio.sitexxx.all.test;

import org.tio.sitexxx.all.Starter;
import org.tio.sitexxx.service.service.base.SensitiveWordsService;

/**
 * @author tanyaowu
 *
 */
public class SensitiveWordsServiceTest {

	/**
	 * 
	 */
	public SensitiveWordsServiceTest() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		Starter.initBase();

		String x = "tionet";
		String xx = SensitiveWordsService.findAndReplace(x);
		System.out.println(xx);

	}

}
