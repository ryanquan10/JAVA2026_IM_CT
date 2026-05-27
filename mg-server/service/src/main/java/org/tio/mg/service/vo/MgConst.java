
package org.tio.mg.service.vo;

import org.tio.utils.jfinal.P;

import cn.hutool.core.io.FileUtil;

/**
 * 
 * @author xufei
 * 2020年5月26日 上午10:35:25
 */
public class MgConst {
	
	
	public static final String RESOURCE_PATH 				= FileUtil.getAbsolutePath("classpath:");
	
	/**
	 * 上传资源服务器存放上传文件的根目录
	 */
	public static final String RES_ROOT 					= P.get("res.root");
	
	
	public static final String	VIEW_PAGE_ROOT				= P.get("http.view.page");
	
	/**
	 * 服务器的IP地址或域名
	 */
	public static final String	MY_IP						= P.get("my.ip");

	public static final String	BS_IP						= P.get("bs.ip");
	
	/**
	 * 发票报销excel模板
	 */
	public static final String	INVOICE_EXCEL_TEMPLATE		= P.get("invoice.excel.template");
	
	public static String MG_USER_DEFAULT_PWD = "888888";
	
	public static interface AccessToken {
		/**
		 * 存令牌(access_token)的cookie name
		 */
		public static final String COOKIENAME_FOR_ACCESSTOKEN = "tio_mg_access_token";
	}
	
	public static interface Http {
		String SESSION_COOKIE_NAME = P.get("http.session.cookie.name", "tio_mg_session");
	}
	
	public static interface Db {
		/**
		 * tio_site_main数据库
		 */
		String	TIO_SITE_MAIN	= "tio_site_main";
		/**
		 * tio_site_stat数据库
		 */
		String	TIO_SITE_STAT	= "tio_site_stat";
		/**
		 * tio_site_conf数据库
		 */
		String	TIO_SITE_CONF	= "tio_site_conf";
		/**
		 * tio_site_mg数据库
		 */
		String	TIO_MG		= "tio_mg";
	}
	
	/**
	 * 
	 * @author xufei
	 * 2020年5月28日 下午3:01:10
	 */
	public static interface ConfMapping {
		/**
		 * 管理后台vue根路由
		 */
		public String MG_VUE_ROOT_PATH = "mg.vue.root.path";

		/**
		 * 管理后台vue根组件页面
		 */
		public String MG_VUE_ROOT_COMPONENT = "mg.vue.root.component";
		
		/**
		 * 管理后台菜单同步网站
		 */
		public String MG_MENU_SYSN_SITE = "mg.menu.sysn.site";
	}
	
	/**
	 * 操作日志标识
	 * 
	 *
	 */
	public static interface OperLogType {
		/**
		 * 菜单
		 */
		short MENU = 1;

		/**
		 * 操作
		 */
		short OPER = 2;
		
		/**
		 * 系统
		 */
		Short SYS = 3;
	}
	
	
	public static interface AuthIndexType {
		/**
		 * 菜单
		 */
		short change = 1;

		/**
		 * 操作
		 */
		short index = 2;
		
		/**
		 * 系统
		 */
		Short SYS = 3;
	}
	
	/**
	 * 权限类型
	 * @author xufei
	 * 2020年5月27日 上午11:16:15
	 */
	public static interface AuthType {
		/**
		 * 菜单
		 */
		short MENU = 1;
		
		/**
		 * 页面
		 */
		short PAGE = 2;

		/**
		 * 操作
		 */
		short OPER = 3;
		
	}
	
	/**
	 * 后台系统配置映射
	 * @author xufei
	 * 2020年7月7日 下午3:42:34
	 */
	public static interface MgConfMapping {
		/**
		 * 最近打开页面大小
		 */
		public String RECENT_PAGE_SIZE = "recent.page.size";

		
		/**
		 * 收藏页面大小
		 */
		public String FAVORITE_PAGE_SIZE = "favorite.page.size";
	}
	
	/**
	 * 
	 * @author xufei
	 * 2020年5月26日 下午3:11:20
	 */
	public static interface MgUserStatus {
		/**
		 * 正常
		 */
		short NORMAL = 1;

		/**
		 * 封停
		 */
		short INBLACK = 5;

		/**注销
		 */
		short LOGOUT = 6;
	}
	
	/**
	 * APP类型
	 * @author xufei
	 * 2020年6月28日 下午5:33:36
	 */
	public static interface AppType {
		/**
		 * 安卓
		 */
		short ANDROID = 1;

		/**
		 *	ios
		 */
		short IOS = 2;

	}
	
	
	/**
	 * 后台上传路径
	 * @author xufei
	 * 2020年6月18日 上午10:11:06
	 */
	public static interface MgUploadDir {
		
		String DEFAULT_DIR = "mg/default";
		
		/**
		 * 招聘企业log
		 */
		short RECRUIT_CMP_LOGO = 1;
		
		String RECRUIT_CMP_LOGO_DIR = "recruit/cmp/logo";
		
		/**
		 * 订单电子合同
		 */
		short ORDER_CONTRACT = 2;
		
		String ORDER_CONTRACT_DIR = "order/contract";
		
		/**
		 * app文件包
		 */
		short APP_FILE = 3;
		
		String APP_FILE_DIR = "app/file";
		
		
		/**
		 * 后台发票路径
		 */
		String MG_INVOICE_DIR = "mg/reimbursement";
	}

	/**
	 * Cloudflare R2 配置
	 */
	public static interface CloudflareR2 {

		/**
		 * access-key
		 */
		String R2_ACCESS_KEY = P.get("cloudflare.r2-access-key", "88cbe45580fc5725986f0c1ffd1db6e3");

		/**
		 * secret-key
		 */
		String R2_SECRET_KEY = P.get("cloudflare.r2-secret-key", "2f96fe4757294d3be402fc73fafa168231975d0f3d6365c03f8f509ecd8a1a76");

		/**
		 * bucket-name
		 */
		String R2_BUCKET_NAME = P.get("cloudflare.r2-bucket-name", "ctcx");

		/**
		 * endpoint
		 */
		String R2_ENDPOINT = P.get("cloudflare.r2-endpoint", "https://0fd4c030de5520eae6227e1c590a64f4.r2.cloudflarestorage.com");
	}
	
}
