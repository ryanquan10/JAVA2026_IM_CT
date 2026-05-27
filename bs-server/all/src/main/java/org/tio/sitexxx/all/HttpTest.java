
package org.tio.sitexxx.all;

import org.tio.utils.http.HttpUtils;

/**
 * @author tanyaowu
 */
public class HttpTest {

	/**
	 * 
	 */
	public HttpTest() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		String url = "http://127.0.0.1:10160/im/site";

		HttpUtils.get(url);

	}

}
