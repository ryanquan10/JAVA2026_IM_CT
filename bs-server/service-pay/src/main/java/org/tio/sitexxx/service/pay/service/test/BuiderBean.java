
package org.tio.sitexxx.service.pay.service.test;

import com.alibaba.fastjson15.JSONObject;

public class BuiderBean extends BuilderTest {

	public JSONObject bothEncryptBuild() throws IllegalAccessException {
		JSONObject json = super.assembleBuild();
		return json;
	}

	public static void main(String args[]) throws IllegalAccessException {
		BuiderBean buiderBean = new BuiderBean();
		JSONObject json = buiderBean.bothEncryptBuild();
		System.out.println(json.toString());
	}
}
