
package org.tio.sitexxx.service.service.captcha;

import java.util.Properties;

import com.anji.captcha.model.common.Const;
import com.anji.captcha.service.CaptchaService;
import com.anji.captcha.service.impl.CaptchaServiceFactory;

/**
 * 图形验证码配置文件
 * @author lixinji
 * 2020年12月12日 下午1:49:44
 */
public class CaptchaConfig {

	/**
	 * 本地图片服务
	 * @return
	 * @author lixinji
	 * 2020年12月18日 下午1:50:12
	 */
	public static CaptchaService captchaService() {
		Properties config = new Properties();
		//        try {
		//            try (InputStream input = CaptchaConfig.class.getClassLoader()
		//                    .getResourceAsStream("app-env.properties")) {
		//                config.load(input);
		//            }
		//        }catch (Exception ex){
		//            ex.printStackTrace();
		//        }
		//各种参数设置....
		//缓存类型redis/local/....
		config.put(Const.CAPTCHA_CACHETYPE, "redis");
		config.put(Const.CAPTCHA_WATER_MARK, "");//水印
		config.put(Const.CAPTCHA_FONT_TYPE, "宋体");
		config.put(Const.CAPTCHA_TYPE, "default");
		config.put(Const.CAPTCHA_INTERFERENCE_OPTIONS, "0");
		config.put(Const.ORIGINAL_PATH_JIGSAW, "");
		config.put(Const.ORIGINAL_PATH_PIC_CLICK, "");
		config.put(Const.CAPTCHA_SLIP_OFFSET, "5");
		config.put(Const.CAPTCHA_AES_STATUS, "true");
		config.put(Const.CAPTCHA_WATER_FONT, "宋体");
		config.put(Const.CAPTCHA_CACAHE_MAX_NUMBER, "1000");
		config.put(Const.CAPTCHA_TIMING_CLEAR_SECOND, "180");
		//TODO:扩展业务底图
		//        if ((StrUtils.isNotBlank(config.getProperty(Const.ORIGINAL_PATH_JIGSAW))
		//                && config.getProperty(Const.ORIGINAL_PATH_JIGSAW).startsWith("classpath:"))
		//                || (StrUtils.isNotBlank(config.getProperty(Const.ORIGINAL_PATH_PIC_CLICK))
		//                && config.getProperty(Const.ORIGINAL_PATH_PIC_CLICK).startsWith("classpath:"))) {
		//            //自定义resources目录下初始化底图
		//            config.put(Const.CAPTCHA_INIT_ORIGINAL, "true");
		////            initializeBaseMap(config.getProperty(Const.ORIGINAL_PATH_JIGSAW),
		////                    config.getProperty(Const.ORIGINAL_PATH_PIC_CLICK));
		//        }
		CaptchaService s = CaptchaServiceFactory.getInstance(config);
		return s;
	}

	//    private static void initializeBaseMap(String jigsaw, String picClick) {
	//        ImageUtils.cacheBootImage(getResourcesImagesFile(jigsaw + "/original/*.png"),
	//                getResourcesImagesFile(jigsaw + "/slidingBlock/*.png"),
	//                getResourcesImagesFile(picClick + "/*.png"));
	//    }

}
