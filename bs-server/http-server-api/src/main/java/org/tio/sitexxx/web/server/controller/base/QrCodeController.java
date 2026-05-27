
package org.tio.sitexxx.web.server.controller.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.http.server.annotation.RequestPath;
import org.tio.http.server.util.Resps;

import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;

/**
 * 生成二维码
 * @author 
 * 2016年6月29日 下午7:53:59
 */
@RequestPath(value = "/qrcode")
public class QrCodeController {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(QrCodeController.class);

	/**
	 * 
	 * @param width
	 * @param height
	 * @param color 0xFFFF0000
	 * @param bgColor 0xFFFFFFAA
	 * @param str
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	@RequestPath(value = "/{width}/{height}")
	public HttpResponse index(Integer width, Integer height, Integer color, Integer bgColor, String str, HttpRequest request) throws Exception {
		HttpResponse ret = null;
		QrConfig qrConfig = new QrConfig();
		qrConfig.setHeight(height);
		qrConfig.setWidth(width);
		qrConfig.setBackColor(bgColor);
		qrConfig.setForeColor(color);
		qrConfig.setMargin(5);
		byte[] bs = QrCodeUtil.generatePng(str, qrConfig);
		ret = Resps.bytes(request, bs, "png");
		return ret;
	}

	public static void main(String[] args) {

	}

}
