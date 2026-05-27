
package org.tio.sitexxx.service.utils.ncount;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import org.tio.sitexxx.service.pay.exception.NCountException;
import org.tio.sitexxx.service.vo.Const;
import org.tio.utils.hutool.StrUtil;

import cn.hutool.core.io.resource.ResourceUtil;

/**
 * 新生支付
 * @author lixinji
 * 2021年3月2日 上午9:39:01
 */
public class NcountUtils {

	public static PrivateKey loadPrivateKey() throws NCountException {
		String privateKey = ResourceUtil.readUtf8Str(Const.WALLET_MERCHANT_PEM);
		if (StrUtil.isBlank(privateKey)) {
			throw new RuntimeException("新生支付私钥未配置");
		}
		// 去除头尾标志
		// 去除换行符
		privateKey = privateKey.replace("\r", "").replace("\n", "").replace(" ", "");
		byte[] bPriKey = Base64Util.decode(privateKey);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bPriKey);
		KeyFactory keyFactory;
		try {
			keyFactory = KeyFactory.getInstance(NcountKey.ALGORITHM);
			PrivateKey key = keyFactory.generatePrivate(keySpec);
			return key;
		} catch (Exception e) {
			e.printStackTrace();
			throw new NCountException("加载私钥异常", e);
		}
	}
}
