
package org.tio.sitexxx.service.vo;

import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;

import cn.hutool.core.util.StrUtil;
import org.tio.utils.jfinal.P;
import org.tio.utils.thread.pool.DefaultThreadFactory;
import org.tio.utils.thread.pool.TioCallerRunsPolicy;
import org.tio.utils.time.Time;


/**
 * @author tanyaowu
 * 2016年9月19日 下午5:04:12
 */
public class Const {

    /**
     * 特殊字符
     */
    public static final char[] SPECIAL_CHARACTER = new char[]{'<', '>', '\"', '\'', '\\', '/', '~', '`', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '+', '[',
            ']', '{', '}', '＠', '￥', ' ', '	'};
    /**
     * 握手签名时的key
     */
    public static final String HANDSHAKE_KEY = P.get("app.handshake.key", "ufDoiDnj");
    public static final String CHARSET = "utf-8";
    /**
     * 在ChannelContext存放ImSessionContext对象的key
     */
    public static final String IM_SESSION_KEY = "im_session_key";
    /**
     * IM心跳超时时间，单位：毫秒
     */
    public static final long IM_HEARTBEAT_TIMEOUT = P.getLong("im.heartbeat.timeout", 120 * 1000L);

    /**
     *
     */
    public static final int AUTO_USER_INDEX = P.getInt("auto.user.index", 1000);

    /**
     * 焦点刷新时间-毫秒
     */
    public static final int FOCUS_REFRESH_TIMEOUT = P.getInt("focus.refresh.timeout", 20000);

    /**
     * 用来做测试的groupid，随时会变和被取消
     */
    public static final String TEST_GROUPID = "45";
    /**
     * 上传资源服务器存放上传文件的根目录
     */
    public static final String RES_ROOT = P.get("res.root");
    /**
     * 服务器的IP地址或域名
     */
    public static final String MY_IP = P.get("my.ip");

    /**
     * 支付类型：1：新生支付；2：易支付
     */
    public static final String PAY_TYPE = P.get("pay.version.type", "1");
    public static final String SMS_TYPE = P.get("sms.type", "1");

//    public static final String AGORA_OPEN = P.get("agora.open", "1");
//    public static final String AGORA_APP_ID = P.get("agora.app.id");
//    public static final String AGORA_APP_CERTIFICATE = P.get("agora.app.certificate");
//    public static final String AGORA_APP_TOKEN_EXPIRATION_SECONDS = P.get("agora.app.token.expiration.seconds", "3600");
//    public static final String AGORA_APP_PRIVILEGE_EXPIRATION_SECONDS = P.get("agora.app.privilege.expiration.seconds", "3600");

    /**
     * 是否使用tio集群
     * true : 使用集群
     * false : 不使用
     */
    public static final boolean USE_TIO_CLU = P.getBoolean("use.tio.clu", false);

    /**
     * 新版同步标识
     */
    public static final boolean SYN_NEW_VERSION = P.getBoolean("syn.newversion.flag", false);

    /**
     * 敏感词过滤开关
     */
    public static final boolean SENSITIVE_FLAG = P.getBoolean("sensitive.flag", false);

    /**
     * 短信服务端验证开关
     */
    public static final boolean SMS_OPENFLAG = P.getInt("sms.openflag", 1) == 1;

    /**
     * 默认系统用户id，系统初始化后，不能修改，建议不做修改
     */
    public static final Integer SYS_ADMIN_UID = -8;

    /**
     * 焦点高性能开关
     * false: 优先保证正确性
     * true:  优先保证性能
     */
    public static final boolean MSG_READ_FOCUS_FLAG = P.getBoolean("msg.read.focus.flag", false);

    /**
     * 上传资源服务器
     */
    public static String RES_SERVER = P.get("res.server");

    static {
        if (StrUtil.endWith(RES_SERVER, "/")) {
            RES_SERVER = StrUtil.subBefore(RES_SERVER, "/", true);
        }
    }

    /**
     * im服务地址
     */
    public static String IM_SERVER = P.get("im.server");

    static {
        if (StrUtil.endWith(IM_SERVER, "/")) {
            IM_SERVER = StrUtil.subBefore(IM_SERVER, "/", true);
        }
    }

    /**
     * 阿里惠普TTF字体路径
     */
    public static final String MEDIUM_FONT_TTF_FILE = P.get("medium.font.ttf.file");

    /**
     * 用户头像自动生成背景图片路径
     */
    public static final String USER_AVATAR_BASE_BACKGROUND_IMG = P.get("user.avatar.base.background.img");

    /**
     * 用户生成头像字体字号
     */
    public static final int USER_AVATAR_MEDIUM_FONT_SIZE = P.getInt("user.avatar.medium.font.size", 80);

    /**
     * 是否用匿名系统
     */
    public static final boolean USE_ANONYMOUS = Objects.equals(P.getInt("use.anonymous", 2), 1);

    /**
     * 是否开启自动生成头像：默认不开启
     */
    public static final boolean USE_AUTO_AVATAR = Objects.equals(P.getInt("use.auto.avatar", 2), 1);

    /**
     * 超管loginname
     */
    public static final String SUPER_MANAGER_LOGINNAME = P.get("super.loginname.1", "");
    /**
     * 超管loginname2
     */
    public static final String SUPER_MANAGER_LOGINNAME_2 = P.get("super.loginname.2", "");
    /**
     * 是否启动im服务器
     */
    public static final boolean IS_START_IM = P.getInt("start.im", 2) == 1;

    /**
     * 是否于后台redis建立通知
     */
    public static final boolean IS_REDIS_LINK_MG = P.getInt("redis.link.mg", 2) == 1;

    /**
     * 是否启动了view服务器
     */
    public static final boolean IS_START_VIEW = P.getInt("start.view", 1) == 1;

    /**
     * 是否开启钱包功能
     */
    public static final boolean IS_OPEN_WALLET = P.getBoolean("wallet.open.flag", false);

    /**
     * 钱包商户号
     */
    public static final String WALLET_MERCHANTID = P.get("wallet.merchantid", "");

    /**
     * 商户私钥位置
     */
    public static final String WALLET_MERCHANT_PEM = P.get("wallet.merchant.pem", "");

    /**
     * 分页查询时，第几页的参数名，统一用这个名字
     */
    public static final String PARAM_NAME_PAGENUMBER = "pageNumber";
    /**
     * 分页查询时，每页几条数据，统一用这个名字
     */
    public static final String PARAM_NAME_PAGESIZE = "pageSize";

    /**
     * 是否im和wx互通
     */
    public static final boolean IM_CONNECT_WX = P.getInt("im.connect.wx", 1) == 1;

    /**
     * 通道默认uid
     */
    public static final Integer MSG_DEFAULT_UID = -1;

    /**
     * im是否进行ssl认证
     */
    public static final boolean IM_SSL_FLAG = StrUtil.isNotBlank(P.get("ssl.keystore"));

    /**
     * tio监控数据统计时段
     *
     * @author tanyaowu
     * 2016年10月30日 下午1:34:46
     */
    public static interface IpStatDuration {
        public static final Long DURATION_1 = Time.MINUTE_1 * 2;
        //		public static final Long DURATION_2 = Time.MINUTE_1 * 10;
        //		public static final Long DURATION_3 = Time.HOUR_1 * 1;

        public static final Long[] IPSTAT_DURATIONS = new Long[]{DURATION_1/**, DURATION_2, DURATION_3 */
        };
    }

    /**
     * im 群组的子群
     *
     * @author tanyaowu
     */
    public static interface ImGroupType {

        String PREFIX = "_$x-_";

        /**
         * 所有用户都加入这个组
         */
        String ALL_IN_ONE = PREFIX + "_allinone";

        /**
         * 真实用户
         */
        String REAL = PREFIX + "_real_";

        /**
         * 所有用户
         */
        String ALL = PREFIX + "_";

        /**
         * 绑定省
         */
        String PROVINCE = PREFIX + "PROVINCE_";

        /**
         * 绑定市
         */
        String CITY = PREFIX + "CITY_";

        /**
         * 运营商
         */
        String OPERATOR = PREFIX + "OPERATOR_";
    }

    /**
     * 短信版本类型
     *
     * @author xinji
     * 2024年7月16日 下午3:35:25
     */
    public static interface SmsVersion {
        /**
         * 短信宝
         */
        String SMS_BAO = "1";

        /**
         * 阿里云信
         */
        String SMS_ALI = "2";

        /**
         * 网易云信
         */
        String SMS_YUN = "3";

    }

    public static interface Protocol {
        String IM_WS = "ws";
        String IM_APP = "app";
    }

    /**
     * ip访问路径统计
     *
     * @author tanyaowu
     * 2016年10月30日 下午1:34:29
     */
    public static interface IpPathAccessStatDuration {
        //		public static final long DURATION_1 = Time.SECOND_1 * 10;
        public static final long DURATION_2 = Time.MINUTE_1 * 5;
        //		public static final long DURATION_3 = Time.HOUR_1 * 1;

        //		public static final Long[] IP_PATH_ACCESS_STAT_DURATIONS = new Long[] { DURATION_1 };
        public static final Long[] IP_PATH_ACCESS_STAT_DURATIONS = new Long[]{DURATION_2};

    }

    public static interface TokenPathAccessStatDuration {
        //		public static final long DURATION_1 = Time.SECOND_1 * 10;
        public static final long DURATION_2 = Time.MINUTE_1 * 5;
        //		public static final long DURATION_3 = Time.HOUR_1 * 1;

        //		public static final Long[] IP_PATH_ACCESS_STAT_DURATIONS = new Long[] { DURATION_1 };
        public static final Long[] TOKEN_PATH_ACCESS_STAT_DURATIONS = new Long[]{DURATION_2};

    }

    public static interface ImPort {
        /**
         * pc端的端口(websocket的端口)
         */
        public Integer WS = P.getInt("im.port.ws");
        /**
         * app端的端口(安卓和ios的端口一样)
         */
        public Integer APP = P.getInt("im.port.app");
    }

    /**
     * 网站地址
     */
    public static String SITE = P.get("site");

    static {
        if (StrUtil.endWith(SITE, "/")) {
            SITE = StrUtil.subBefore(SITE, "/", true);
        }
    }

    /**
     * 外部服务
     */
    public static String OUT_SERVICE = P.get("out.service");

    static {
        if (StrUtil.endWith(OUT_SERVICE, "/")) {
            OUT_SERVICE = StrUtil.subBefore(OUT_SERVICE, "/", true);
        }
    }

    /**
     * 生产环境的网站地址
     */
    public static String PRODUCT_SITE = P.get("product.site", "https://www.tiocloud.com");

    static {
        if (StrUtil.endWith(PRODUCT_SITE, "/")) {
            PRODUCT_SITE = StrUtil.subBefore(PRODUCT_SITE, "/", true);
        }
    }

    /**
     * 极光推送参数
     *
     * @author lixinji
     * 2020年9月24日 上午10:23:14
     */
    public static interface JPushConfig {

        /**
         * 开启开关
         */
        boolean OPENFLAG = P.getBoolean("Jpush.open.flag", false);
        /**
         *
         */
        String APPKEY = P.get("JAppKey", "8c6e59990d78ad7996e1f2ae");

        /**
         *
         */
        String MASTERSECRET = P.get("JMasterSecret", "345a350e7f1905e703704165");
    }

    public static String API_CONTEXTPATH = P.get("http.api.contextpath", "/mytio");

    public static String API_SUFFIX = P.get("http.api.suffix", ".tio_x");

    public static interface Path {
        /**
         * 登录页
         */
        String LOGIN = "/tioim/login";//"/login/index.html";

        /**
         * 微信支付的path
         */
        String WECHAT_PAY = "/recharge/wechatPay.html";

        /**
         * 阅读博客
         */
        String BLOG_VIEW = "/blog/r/index.html";

        /**
         * tio文档
         */
        String DOC_TIO = "/doc/tio/index.html";

        /**
         * tio官网代码的文档
         */
        String DOC_TIO_SITE = "/doc/site/index.html";

        /**
         * 钛信的文档
         */
        String DOC_TAIXIN = "/doc/taixin/index.html";

        /**
         * 拿博客数据
         */
        String BLOG_DATA = "/blog/r/blog-view-onlyhtml.html";//"/blog/r/blog-view.html";

        /**
         * 案例列表页
         */
        String TIOCASE_INDEX = "/2/case/index.html";
        /**
         * 案例详情页
         */
        String TIOCASE_INFO = "/2/case/caseInfo.html";

        /**
         * 博客列表
         */
        String TIOBLOG_INDEX = "/2/blog/index.html";

        /**
         * 博客详情页
         */
        String TIOBLOG_INFO = "/2/blog/blogInfo.html";

    }

    /**
     * 百度资源
     *
     * @author tanyaowu
     */
    public static interface BaiduZiyuan {
        /**
         * 百度链接推送地址
         */
        String PUSH_URL = "http://data.zz.baidu.com/urls?site=https://www.tiocloud.com&token=cPTafjnh3Zld21qs";

        /**
         * 百度链接更新地址
         */
        String UPDATE_URL = "http://data.zz.baidu.com/update?site=https://www.tiocloud.com&token=cPTafjnh3Zld21qs";

        /**
         * 百度链接删除地址
         */
        String DELETE_URL = "http://data.zz.baidu.com/del?site=https://www.tiocloud.com&token=cPTafjnh3Zld21qs";

    }

    /**
     * view层，公共model的key
     *
     * @author tanyaowu
     */
    public static interface ModelKey {
        String TITLE = "title";
        String KEYWORDS = "keywords";
        String DESCRIPTION = "description";
        String RES_SERVER = "res_server";
        String JS_VERSION = "js_version";
        String API_CONTEXTPATH = "api_ctx";                // "/api"
        String API_SUFFIX = "api_suf";                // ".php"
        String HTTP_SESSION_COOKIE_NAME = "session_cookie_name";    // "TIO_SESSION"
        String SITE_NAME = "sitename";
        String SEO_HTML = "seo_html";
        String OFFICIAL_ADDRESS = "official_address";
    }

    public static interface AliOss {
        /**
         *
         */
        public static final String ENDPOINT = P.get("ali.oss.endpoint");
        /**
         * accessKeyId
         */
        public static final String ACCESS_KEY_ID = P.get("ali.oss.AccessKeyID");
        /**
         * accessKeySecret
         */
        public static final String ACCESS_KEY_SECRET = P.get("ali.oss.AccessKeySecret");

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


    /**
     * TencentOss 配置
     */
    public static interface TencentOss {

        /**
         * access-key
         */
        String SECRET_ID = P.get("tencentoss.secret-id", "88cbe45580fc5725986f0c1ffd1db6e3");

        /**
         * secret-key
         */
        String SECRET_KEY = P.get("tencentoss.secret-key", "2f96fe4757294d3be402fc73fafa168231975d0f3d6365c03f8f509ecd8a1a76");

        /**
         * bucket-name
         */
        String BUCKET_NAME = P.get("tencentoss.bucket-name", "ctcx");

        /**
         * REGION
         */
        String REGION = P.get("tencentoss.region", "ctcx");
    }

    public static String OSS_TYPE = P.get("oss-type", "tencentoss");


    /**
     * 阿里直播
     *
     * @author tanyaowu
     */
    public static interface AliLive {
        /**
         * url
         */
        public static final String URL = P.get("ali.live.url");
        /**
         * regionid
         */
        public static final String REGIONID = P.get("ali.live.regionId");
        /**
         * accesskey
         */
        public static final String ACCESSKEY = P.get("ali.live.accessKey");
        /**
         * accesssecret
         */
        public static final String ACCESSSECRET = P.get("ali.live.accessSecret");
        /**
         * domainname
         */
        public static final String DOMAINNAME = P.get("ali.live.domainName");
        /**
         * appname
         */
        public static final String APPNAME = P.get("ali.live.appName");

        /**
         *
         */
        public static final String AUTHKEY1 = P.get("ali.live.authkey.1");
    }

    /**
     * 系统参数配置key值
     */
    public static interface ConfMapping {
        /**
         * 好友会话缩略最长字符
         */
        public String WX_FRIEND_MSG_RESUME_MAXSIZE = "wx.friend.msg.resume.maxsize";

        /**
         * 好友历史消息分页条数
         */
        public String WX_FRIEND_MSG_LIMIT = "wx.friend.msg.limit";

        /**
         * 默认查询群聊的的历史记录条数
         */
        public static final String WX_GROUP_MSG_LIMIT = "wx.group.msg.limit";

        /**
         * 缩略消息默认长度
         */
        static final String WX_GROUP_MSG_RESUME_MAXSIZE = "wx.group.msg.resume.maxsize";

        /**
         * 群用户最大人数
         */
        static final String WX_GROUP_MAX_JOIN_NUM = "wx.group.max.join.num";

        /**
         * 群用户默认显示多少
         */
        static final String WX_GROUP_USER_VIEW_DEFAULT = "wx.group.user.view.limit";

        /**
         * 撤回的最大时间
         */
        static final String WX_MSG_BACK_MAX_TIME = "wx.msg.back.max.time";

        /**
         * 提现的最大金额
         */
        static final String WX_WALLET_WITHHOLD_MAX_AMOUNT = "wx.wallet.withhold.max.amount";

        /**
         * 提现的最大次数
         */
        static final String WX_WALLET_WITHHOLD_MAX_COUNT = "wx.wallet.withhold.max.count";

        /**
         * 提现的最小金额
         */
        static final String WX_WALLET_WITHHOLD_MIN_AMOUT = "wx.wallet.withhold.min.amount";

        /**
         * 提现的手续费
         */
        static final String WX_WALLET_WITHHOLD_COMMISSION = "wx.wallet.withhold.commission";

        /**
         * 提现的手续费常量
         */
        static final String WX_WALLET_WITHHOLD_COMMISSION_CONST = "wx.wallet.withhold.commission.const";

        /**
         * 提现超时时间
         */
        static final String WX_WALLET_WITHHOLD_TIMEOUT = "wx.wallet.withhold.timeout";

        /**
         * 充值的最大金额
         */
        static final String WX_WALLET_RECHARGE_MAX_AMOUNT = "wx.wallet.recharge.max.amount";

        /**
         * 充值订单超时时间
         */
        static final String WX_WALLET_RECHARGE_TIMEOUT = "wx.wallet.recharge.timeout";

        /**
         * 支付短信业务超时时间
         */
        static final String WX_WALLET_SMS_TIMEOUT = "wx.wallet.sms.timeout";

        /**
         * 发送红包的最大金额
         */
        static final String WX_WALLET_SENDREDPACKET_MAX_AMOUNT = "wx.wallet.redpacket.max.amount";

        /**
         * 发红包超时时间
         */
        static final String WX_WALLET_SENDREDPACKET_TIMEOUT = "wx.wallet.sendredpacket.timeout";

        /**
         * 发红包支付超时时间
         */
        static final String WX_WALLET_REDPACKET_PAY_TIMEOUT = "wx.wallet.redpacket.pay.timeout";

        /**
         * 发红包初始化超时时间
         */
        static final String WX_WALLET_REDPACKET_INT_TIMEOUT = "wx.wallet.redpacket.init.timeout";

        /**
         * 群发限速开关
         */
        static final String WX_GROUP_MSG_SEND_LIMIT_OPEN = "wx.group.msg.send.limit.open";

        /**
         * 创建群限制：用户注册时间天数
         */
        static final String WX_CREATE_GROUP_LIMIT_USER_REG_DAY = "wx.create.group.limit.user.reg.day";

        /**
         * 聊天限制：用户注册时间天数
         */
        static final String WX_CHAT_LIMIT_USER_REG_DAY = "wx.chat.limit.user.reg.day";

        /**
         * 外部用户默认头像
         */
        static final String OUT_USER_API_DEFAULT_AVATAR_URL = "out.user.api.default.avatar.url";

        /**
         * 默认邀请码注册用户所属组织
         */
        static final String WX_NO_INVITATION_DEFAULT = "wx.no.invitation.default";

    }

    /**
     * 数据库名字常量
     *
     * @author tanyaowu
     * 2016年10月20日 下午4:04:51
     */
    public static interface Db {
        /**
         * tio_site_main数据库
         */
        String TIO_SITE_MAIN = "tio_site_main";
        /**
         * tio_site_stat数据库
         */
        String TIO_SITE_STAT = "tio_site_stat";
        /**
         * tio_site_conf数据库
         */
        String TIO_SITE_CONF = "tio_site_conf";
        /**
         * tio_mg数据库
         */
        String TIO_MG = "tio_mg";
    }

    /**
     * 历史表
     *
     * @author tanyw
     */
    public static interface HistoryTable {
        /**
         * 历史表后缀
         */
        public static final String HISTORY_TABLE_SUFFIX = "_x_his";
    }

    /**
     * 上传目录
     *
     * @author tanyaowu
     * 2016年12月22日 下午5:03:14
     */
    public static interface UPLOAD_DIR {

        /**
         * im上传视频的目录
         */
        public static final String IM_VIDEO = "im/upload/video";
        /**
         * im上传图片的目录
         */
        public static final String IM_IMG = "im/upload/img";
        /**
         * 群组头像上传
         */
        public static final String IM_GROUP_AVATAR = "im/group/avatar";

        /**
         * wx上传语音的目录
         */
        public static final String WX_AUDIO = "wx/upload/audio";

        /**
         * wx上传视频的目录
         */
        public static final String WX_VIDEO = "wx/upload/video";

        /**
         * wx上传图片的目录
         */
        public static final String WX_IMG = "wx/upload/img";

        /**
         * wx上传文件的目录
         */
        public static final String WX_FILE = "wx/upload/file";

        /**
         * wx群组头像上传
         */
        public static final String WX_GROUP_AVATAR = "wx/group/avatar";

        /**
         * blog上传图片的目录
         */
        public static final String BLOG_IMG = "blog/upload/img";

        /**
         * 用户上传头像的目录
         */
        public static final String USER_AVATAR = "user/avatar";

        /**
         * 群上传头像的目录
         */
        public static final String GROUP_AVATAR = "group/groupavatar";

        /**
         * 自动生成头像的目录
         */
        public static final String USER_BASE_AVATAR = "user/base/avatar";

        /**
         * 案例上传图片
         */
        public static final String CASE_IMG = "case/img";

        /**
         * 案例上传视频
         */
        public static final String CASE_VIDEO = "case/video";

        /**
         * 博客上传视频
         */
        public static final String BLOG_VIDEO = "blog/video";

        /**
         * 招聘简历
         */
        public static final String RECRUIT_RESUME = "tio/resume";

        /**
         * 发票
         */
        public static final String INVOICE = "tio/invoice";

        /**
         * 授权
         */
        public static final String LICENSE = "tio/license";

        /**
         * app错误日志
         */
        public static final String APP_LOG_ERR = "app/log/err";

        /**
         * 用户上传的地图信息
         */
        public static final String POSITION = "wx/position";

    }

    public static interface ZkNode {
        /**
         * 根节点
         */
        String ROOT = "/tio-site";

        /**
         * IM服务器
         * 该节点下的数据形如:<br>
         * |--192.168.0.1<br>
         * |--192.168.0.2<br>
         * |--192.168.0.3<br>
         */
        String IM_SERVERS = ROOT + "/im-server";

    }

    /**
     * topic常量，前后台要一致
     *
     * @author tanyaowu
     * 2016年11月8日 下午4:16:25
     */
    public static interface Topic {
        /**
         * 通用topic
         */
        String COMMON_TOPIC = "COMMON_TOPIC";
        /**
         * 清除http cache
         */
        String CLEAR_HTTP_CACHE = "CLEAR_HTTP_CACHE";
        /**
         * 把ip拉黑
         */
        String PULL_IP_TO_BLACK = "PULL_IP_TO_BLACK";
        /**
         * web通知im系统发消息
         */
        String WEB_NTY_IM = "WEB_NTY_IM";
        /**
         * 清空html-view的缓存
         */
        String CLEAN_VIEW_CACHE = "CLEAN_VIEW_CACHE";

        /**
         * im管理后台操作消息
         */
        String IM_MANAGER_OPER = "IM_MANAGER_OPER";
    }

    /**
     * 机器人标识
     */
    public static interface Robot {
        /**
         * 是
         */
        short YES = 1;

        /**
         * 否
         */
        short NO = 2;
    }

    /**
     * 通用yes/no
     */
    public static interface YesOrNo {
        /**
         * 是
         */
        short YES = 1;

        /**
         * 否
         */
        short NO = 2;

        /**
         * 未知
         */
        short NO_FLAG = 3;
    }

    /**
     * 群进入类型
     *
     * @author lixinji
     * 2021年1月13日 下午3:57:54
     */
    public static interface GroupJoinMode {
        /**
         * 是
         */
        short REVIEW = 1;

        /**
         * 否
         */
        short NO_REVIEW = 2;
    }

    /**
     * 申请状态
     *
     * @author lixinji
     * 2020年1月9日 下午2:50:54
     */
    public static interface ApplyStatus {
        /**
         * 通过
         */
        short PASS = 1;

        /**
         * 申请中
         */
        short APPLY = 2;

        /**
         * 无效：过期/拒绝
         */
        short REJECT = 3;

        /**
         * 下架
         */
        short UNPUBLISH = 4;

        /**
         * 删除
         */
        short DELETE = 5;
    }

    /**
     * 消息是否是系统发出
     *
     * @author tanyaowu
     */
    public static interface Sendbysys {
        /**
         * 是
         */
        short YES = 1;

        /**
         * 否
         */
        short NO = 2;
    }

    /**
     * 标准状态常量
     */
    public static interface Status {
        /**
         * 正常
         */
        short NORMAL = 1;
        /**
         * 禁用/处理中
         */
        short DISABLED = 2;

        /**
         * 删除
         */
        short DELETE = 3;
    }

    /**
     * 标准处理状态
     *
     * @date 2016年5月24日 下午4:13:26
     */
    public static interface DealStatus {
        /**
         * 正常
         */
        short NORMAL = 1;

        /**
         * 处理中
         */
        short DEALING = 88;

        /**
         * 完成
         */
        short FINISH = 3;
    }

    /**
     * @author: tanyaowu
     */
    private Const() {
    }

    public static interface AccessToken {
        /**
         * 存令牌(access_token)的cookie name
         */
        public static final String COOKIENAME_FOR_ACCESSTOKEN = "tio_access_token";
    }

    /**
     * 区域显示状态
     */
    public static interface AreaViewStatus {
        /**
         * 显示
         */
        short VIEW = 1;

        /**
         * 隐藏
         */
        short HIDDEN = 2;
    }

    /**
     * 三方登录状态
     */
    public static interface UserThirdStatus {
        /**
         * 正式
         */
        short NORMAL = 1;

        /**
         * 初始化登录
         */
        short INIT = 2;
    }

    /**
     * 性别
     *
     * @author lixinji
     * 2020年3月3日 下午6:32:53
     */
    public static interface UserSex {
        /**
         * 男
         */
        short MALE = 1;

        /**
         * 女
         */
        short FEMALE = 2;

        /**
         * 保密
         */
        short SECRET = 3;
    }


    /**
     * 财务收支状态
     */
    public static interface CoinFlag {
        /**
         * 收入
         */
        short INCOME = 1;

        /**
         * 支出
         */
        short PAY = 2;
    }

    /**
     * 业务标准来源模型
     */
    public static interface StandardMode {
        /**
         * 充值
         */
        short RECHARGE = 1;
    }

    /**
     * 该类定义的，单位都是秒
     *
     * @author tanyaowu
     */
    public static class CacheTime {
        /**
         * 60秒
         */
        public static final long SECOND_60 = 60;
    }

    public static interface UrlParamName {
        /**
         * 登录后重定向的地址
         */
        String REDIRECT_URI_AFTER_LOGIN = "redirect_uri_after_login";
    }

    /**
     * 一些固定的聊天群组
     *
     * @author tanyaowu
     */
    public static interface GroupId {

        /**
         * 全站聊天的groupid
         */
        String ALL = "-1";
    }

    /**
     * 一些内置的ChannelContextId
     *
     * @author tanyaowu
     */
    public static interface ChannelContextId {

        /**
         * 系统内置的ChannelContextId
         */
        String SYSTEM = "1";
    }

    /**
     * 群加人方式
     *
     */
    //	public static interface GroupJoinMode {
    //		/**
    //		 *  群主邀请
    //		 */
    //		short OWENSER_INVITE = 1;
    //
    //		/**
    //		 * 群主审核
    //		 */
    //		short OWENSER_ADD = 2;
    //		
    //		/**
    //		 * 直接入群
    //		 */
    //		short ALL_ADD = 3;
    //	}

    /**
     * 聊天模型
     *
     * @author lixinji
     */
    public static interface ChatMode {
        /**
         * 私聊
         */
        short P2P = 1;

        /**
         * 群聊
         */
        short GROUP = 2;
    }

    /**
     * 群角色
     *
     * @author lixinji
     * 2020年2月13日 下午4:34:29
     */
    public static interface GroupRole {
        /**
         * 群主
         */
        short OWNER = 1;

        /**
         * 成员
         */
        short MEMBER = 2;

        /**
         * 群管理员
         */
        short MANAGER = 3;
    }

    /**
     * Im的人员操作码
     *
     * @author lixinji
     * 2020年1月17日 下午2:20:08
     */
    public static interface WxUserOper {
        /**
         * 删除聊天会话
         */
        short DEL_ITEM_REACT = 1;

        /**
         * 拉黑消息
         */
        short BLACK = 2;

        /**
         * 拉黑恢复消息
         */
        short REMOVE_BLACK = 3;

        /**
         * 激活操作
         */
        short ACT = 4;

        /**
         * 删除好友通知
         */
        short DEL_FRIEND = 5;

        /**
         * 被删除好友通知
         */
        short TO_DEL_FRIEND = 6;

        /**
         * 对方已读
         */
        short TO_READ = 7;

        /**
         * 清空聊天记录
         */
        short CLEAR_CHAT_MSG = 8;

        /**
         * 撤回消息
         */
        short BACK_MSG = 9;

        /**
         * 删除消息
         */
        short DEL_MSG = 10;

        /**
         * 隐藏会话(删除会话)：影响激活/已退群业务
         */
        short HIDE_CHAT = 11;

        /**
         * 置顶操作
         */
        short CHAT_TOP = 21;

        /**
         * 取消置顶操作
         */
        short CHAT_CANCEL_TOP = 22;

        /**
         * 设置免打扰
         */
        short CHAT_MSGFREE = 25;

        /**
         * 举报
         */
        short CHAT_REPORT = 99;
    }

    /**
     * 同步类型
     *
     * @author lixinji
     * 2020年8月27日 下午2:37:17
     */
    public static interface WxSynType {
        /**
         * 会话同步
         */
        short CHAT = 1;

        /**
         * 链接建立
         */
        short LINK_CREATE = 2;

        /**
         * 客户端同步
         */
        short CLIENT_SYN = 3;

    }

    /**
     * 举报类型
     *
     * @author lixinji
     * 2021年1月27日 下午3:09:23
     */
    public static interface WxReport {
        /**
         * 用户
         */
        short USER = 1;

        /**
         * 群
         */
        short GROUP = 2;

        /**
         * 消息
         */
        short MSG = 3;

        /**
         * 建议
         */
        short ADVISE = 4;

    }

    /**
     * 文件icon类型
     *
     * @author lixinji
     * 2020年8月26日 下午4:12:08
     */
    public static interface FileIconType {
        /**
         * pdf
         */
        short PDF = 1;

        /**
         * txt
         */
        short TXT = 2;

        /**
         * doc
         */
        short DOC = 3;

        /**
         * xls
         */
        short XLS = 4;

        /**
         * ppt
         */
        short PPT = 5;

        /**
         * apk
         */
        short APK = 6;

        /**
         * 图片
         */
        short IMG = 7;

        /**
         * 压缩包
         */
        short ZIP = 8;

        /**
         * 视频
         */
        short VIDEO = 9;

        /**
         * 音频
         */
        short AUDIO = 10;

        /**
         * 其它
         */
        short OTHER = 11;

    }

    /**
     * 聊天系统code
     *
     * @author lixinji
     * 2020年2月25日 下午9:55:52
     */
    public static interface WxSysCode {
        /**
         * 申请好友请求
         */
        short APPLY_REQUEST = 30;

        /**
         * 好友发生变更-新增
         */
        short FRIEND_CHANGE_ADD = 31;

        /**
         * 好友发生变更-减员
         */
        short FRIEND_CHANGE_DEL = 32;

        /**
         * 好友发生变更-信息修改
         */
        short FRIEND_INFO_UPDATE = 33;

        /**
         * 好友发生变更-信息修改
         */
        short LOGOUT = 34;

        /**
         * 好友发生变更-信息修改
         */
        short CLEAR_REMARK_NAME_CACHE = 35;


        /**
         * 新的圈子文章动态
         */
        short PUBLISH_CIRCLE_ARTICLE = 36;


        /**
         * 新的圈子文章评论
         */
        short PUBLISH_CIRCLE_ARTICLE_COMMENT = 37;

        /**
         * 新的圈子文章点赞
         */
        short PUBLISH_CIRCLE_ARTICLE_LIKE = 38;


        /**
         * 错误码
         */
        short ERROR_CODE = -100;
    }

    /**
     * 群操作通知
     *
     * @author lixinji
     * 2020年2月24日 下午1:04:59
     */
    public static interface WxGroupOper {
        /**
         * 群主-删除群
         */
        short DEL_GROUP = 1;

        /**
         * 转让群-转让
         */
        short CHANGE_OUT_GROUP = 2;

        /**
         * 接受群-转让
         */
        short CHANGE_IN_GROUP = 3;

        /**
         * 群加入
         */
        short UPDATE_JOINNUM = 4;

        /**
         * 退群
         */
        short LEAVE_GROUP = 5;

        /**
         * 被踢出群
         */
        short KICK_GROUP = 6;

        /**
         * 重新加入群
         */
        short RE_JOIN_GROUP = 7;

        /**
         * 撤回消息
         */
        short BACK_MSG = 9;

        /**
         * 删除消息
         */
        short DEL_MSG = 10;

        /**
         * 更新群角色
         */
        short UPDATE_GROUP_ROLE = 11;

        /**
         * 修改群头像
         */
        short UPDATE_GROUP_AVATAR = 20;

        /**
         * 修改群名称
         */
        short UPDATE_GROUP_NAME = 21;

        /**
         * 自动修改群信息
         */
        short AUTO_UPDATE_INFO = 22;

        /**
         * 修改群通知-未发送同步
         */
        short UPDATE_GROUP_NOTICE = 23;

        /**
         * 成员-同步删除群
         */
        short MEMBER_DEL_GROUP = 26;

        /**
         * 封停操作
         */
        short INBLACK_OPER = 27;
    }

    /**
     * Im的消息操作码
     *
     * @author lixinji
     * 2020年1月17日 下午2:20:08
     */
    public static interface WxMsgOper {
        /**
         * 删除消息
         */
        short DEL = 1;

        /**
         * 撤回消息
         */
        short BACK = 9;

        /**
         * 新的朋友圈消息
         */
        short MOMENT = 601;
        /**
         * 新的朋友圈点赞或评论
         */
        short COMMENTSORLIKES = 602;
        /**
         * 圈子名片发送
         */
        short CIRCLECARDSEND = 603;
        /**
         * 收藏转发
         */
        short COLLECTMSG = 604;

        /**
         * 笔记发送
         */
        short NOTE = 605;
        /**
         * 举报
         */
        short REPORT = 99;
    }

    /**
     * 用户同步类型
     *
     * @author lixinji
     * 2020年3月16日 上午10:25:43
     */
    public static interface UserToImSynType {
        /**
         * 昵称
         */
        short NICK = 1;

        /**
         * 头像
         */
        short AVATAR = 2;

        /**
         * 群昵称
         */
        short GROUP_NICK = 3;

        /**
         * 用户所有
         */
        short USER_ALL = 99;
    }

    /**
     * 会话操作码
     *
     * @author lixinji
     * 2020年3月5日 下午9:36:45
     */
    public static interface SessionOper {
        /**
         * 进入
         */
        short JOIN = 1;

        /**
         * 离开
         */
        short LEAVE = 2;
    }

    /**
     * 好友验证方式
     *
     * @author lixinji
     * 2020年1月8日 下午4:35:49
     */
    public static interface FdValidType {
        /**
         * 验证
         */
        short VALID = 1;

        /**
         * 不验证
         */
        short NO_VALID = 2;
    }

    /**
     * 周期
     *
     * @author lixinji
     */
    public static interface PeriodType {

        /**
         * 天
         */
        short DAY = 1;

        /**
         * 周
         */
        short WEEK = 2;

        /**
         * 月
         */
        short MONTH = 3;

        /**
         * 年
         */
        short YEAR = 4;

        /**
         * 季
         */
        short QUARTER = 5;

        /**
         * 小时
         */
        short HOUR = 6;

        /**
         * 时分
         */
        short TIME = 7;

        /**
         * 所有
         */
        short TOTAL = 9;

        /**
         * 所有标识
         */
        String TOTAL_PERIOD = "total";

        /**
         * 默认周期字段
         */
        String DEFAULT_PERIOD = "period";
    }

    /**
     * 内容类型
     * 内容类型，1、普通文本消息，2、博客，3、文件，4、音频，5、视频，6：图片
     *
     * @author tanyaowu
     */
    public static interface ContentType {
        /**
         *
         */
        short TEXT = 1;

        /**
         * 博客
         */
        short BLOG = 2;

        /**
         * 文件
         */
        short FILE = 3;

        /**
         * 音频
         */
        short AUDIO = 4;

        /**
         * 视频
         */
        short VIDEO = 5;

        /**
         * 图片
         */
        short IMG = 6;

        short RUN_JS = 7;

        /**
         * 邀请用户入群
         */
        short INVITE_INTO_GROUP = 8;

        /**
         * 名片
         */
        short MSG_CARD = 9;

        /**
         * 视频通话
         */
        short CALL_VIDEO = 10;

        /**
         * 语音通话
         */
        short CALL_AUDIO = 11;

        /**
         * 发红包
         */
        short REDPACKET = 12;

        /**
         * 群申请
         */
        short GROUP_APPLY = 13;

        /**
         * 地图位置
         */
        short POSITION = 14;

        /**
         * 新的朋友圈
         */
        short MOMENT = 15;
        /**
         * 新的朋友圈点赞
         */
        short LIKES = 16;
        /**
         * 新的朋友圈评论
         */
        short COMMENT = 17;
        /**
         * 圈子名片
         */
        short CIRCLE_CARD = 18;

        /**
         * 收藏转发
         */
        short COLLECT_MSG = 19;

        /**
         * 笔记发送
         */
        short NOTE_MSG = 20;
        /**
         * 引用消息
         */
        short QUOTE_MSG = 66;

        /**
         * 模板消息
         */
        short TEMPLATE = 88;

        /**
         * 合并转发
         */
        short MERGE = 77;

    }

    public static interface MergeType {

        short TYPE = 2;
    }

    /**
     * 钛信定时任务类型
     *
     * @author lixinji
     * 2020年7月3日 下午3:58:26
     */
    public static interface WxTaskType {
        /**
         * 备份消息
         */
        short BAK_MSG = 1;

    }

    /**
     * 消息名片类型-来自聊天模型
     *
     * @author lixinji
     * 2020年3月9日 下午2:32:09
     */
    public static interface MsgCardType {
        /**
         * 好友
         */
        short FRIEND = Const.ChatMode.P2P;

        /**
         * 群
         */
        short GROUP = Const.ChatMode.GROUP;

    }

    /**
     * 暂时移过来，未来可能不用了
     *
     * @author lixinji
     * 2020年2月13日 下午11:23:19
     */
    public static interface BackStatus {
        /**
         * 正常
         */
        short OK = 1;

        /**
         * 被自己撤回
         */
        short WITHDRAW_BY_SELF = 2;

        /**
         * 被管理员撤回
         */
        short WITHDRAW_BY_MANAGER = 3;
    }

    /**
     * 区域统计类型
     *
     * @author lixinji
     * 2020年7月23日 下午2:45:15
     */
    public static interface AreaStatType {
        /**
         * 注册
         */
        short REGISTER = 1;
    }

    /**
     * 现存执行日志类型
     *
     * @author lixinji
     * 2020年7月28日 下午1:58:20
     */
    public static interface ThreadLogType {
        /**
         * 注册统计
         */
        short REGISTER_STAT = 1;
        /**
         * 登录日志统计
         */
        short LOGIN_STAT = 2;
    }

    public static interface Http {
        String SESSION_COOKIE_NAME = P.get("http.session.cookie.name", "tio_session");
    }

    /**
     * 注册类型
     *
     * @author lixinji
     * 2020年12月16日 下午3:18:43
     */
    public static interface RegisterType {
        /**
         * 邮箱
         */
        int EMAIL = 1;

        /**
         * 电话
         */
        int PHONE = 2;
        int USERNAME = 3;
    }

    /**
     * 禁言标志
     *
     * @author lixinji
     * 2021年1月5日 上午11:18:43
     */
    public static interface Forbiddenflag {
        /**
         * 时长禁言
         */
        short DURATION = 1;

        /**
         * 不禁言
         */
        short NO = 2;

        /**
         * 长久
         */
        short LONGTERM = 3;
    }

    private static ThreadPoolExecutor bsExecutor = null;
    private static int MAX_POOL_SIZE_FOR_BS = 256;

    /**
     * 获取业务系统的常规线程池
     *
     * @return
     */
    public static ThreadPoolExecutor getBsExecutor() {
        if (bsExecutor != null) {
            return bsExecutor;
        }

        synchronized (Const.class) {
            if (bsExecutor != null) {
                return bsExecutor;
            }

            LinkedBlockingQueue<Runnable> runnableQueue = new LinkedBlockingQueue<>();
            String threadName = "tio-bs";
            DefaultThreadFactory threadFactory = DefaultThreadFactory.getInstance(threadName, Thread.NORM_PRIORITY);
            CallerRunsPolicy callerRunsPolicy = new TioCallerRunsPolicy();
            bsExecutor = new ThreadPoolExecutor(MAX_POOL_SIZE_FOR_BS, MAX_POOL_SIZE_FOR_BS, 60, TimeUnit.SECONDS, runnableQueue, threadFactory, callerRunsPolicy);
            bsExecutor.prestartCoreThread();
            return bsExecutor;
        }
    }

}
