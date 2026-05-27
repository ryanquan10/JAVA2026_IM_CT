
package org.tio.mg.service.vo;

import org.tio.mg.service.model.main.IpInfo;
import org.tio.mg.service.model.main.UserAgent;
import org.tio.sitexxx.service.vo.Devicetype;

/**
 * @author tanyaowu 
 * 2016年9月27日 上午10:42:10
 */
public class RequestExt implements java.io.Serializable {
	private static final long serialVersionUID = 2201875338516562322L;

	/**
	 * 是否是ios App
	 */
	private boolean		fromAppIos			= false;
	/**
	 * 是否是安卓 App
	 */
	private boolean		fromAppAndroid		= false;
	/**
	 * 是否是App
	 */
	private boolean		fromApp				= false;
	/**
	 * 是否是从浏览器访问过来的
	 */
	private boolean		fromBrowser			= true;
	/**
	 * 是否是从移动浏览器访问过来的
	 */
	private boolean		fromBrowserMobile	= false;
	/**
	 * 是否是从PC浏览器访问过来的
	 */
	private boolean		fromBrowserPc		= true;
	/**
	 * 浏览器信息，从浏览器访问过来的才有此对象
	 */
	private UserAgent	userAgent;

	private IpInfo	ipInfo;
	/**
	 * 手机信息
	 */
	private String	deviceinfo;
	/**
	 * 手机尺寸
	 */
	private String	size;
	/**
	 * app版本号
	 */
	private String	appVersion	= null;
	/**
	 * 渠道号
	 */
	private String	cid;
	/**
	 * 手机分辨率，譬如1080,1344
	 */
	private String	resolution;
	/**
	 * imei
	 */
	private String	imei;
	/**
	 * 运营商
	 */
	private String	operator;
	/**
	 * ios才有
	 */
	private String	idfa;
	/**
	 * 枚举，见：org.tio.mg.service.Const.DeviceType
	 */
	private short	deviceType	= Devicetype.WEB.getValue();
	/**
	 * 是否能做http缓存（对于一些错误的响应是不能缓存的）
	 */
	private boolean	canCache	= true;

	/**
	 * Response对象是不是从缓存中取的
	 */
	private boolean fromCache = false;

	public boolean isFromAppIos() {
		return fromAppIos;
	}

	public void setFromAppIos(boolean fromAppIos) {
		this.fromAppIos = fromAppIos;
	}

	public boolean isFromAppAndroid() {
		return fromAppAndroid;
	}

	public void setFromAppAndroid(boolean fromAppAndroid) {
		this.fromAppAndroid = fromAppAndroid;
	}

	public boolean isFromApp() {
		return fromApp;
	}

	public void setFromApp(boolean fromApp) {
		this.fromApp = fromApp;
	}

	public boolean isFromBrowser() {
		return fromBrowser;
	}

	public void setFromBrowser(boolean fromBrowser) {
		this.fromBrowser = fromBrowser;
	}

	/**
	 * 
	 * @author: tanyaowu
	 */
	public RequestExt() {
	}

	/**
	 * @param args
	 * @author: tanyaowu
	 */
	public static void main(String[] args) {

	}

	public short getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(short deviceType) {
		this.deviceType = deviceType;
	}

	public boolean isCanCache() {
		return canCache;
	}

	public void setCanCache(boolean canCache) {
		this.canCache = canCache;
	}

	public boolean isFromCache() {
		return fromCache;
	}

	public void setFromCache(boolean fromCache) {
		this.fromCache = fromCache;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	public String getResolution() {
		return resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getDeviceinfo() {
		return deviceinfo;
	}

	public void setDeviceinfo(String deviceinfo) {
		this.deviceinfo = deviceinfo;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getIdfa() {
		return idfa;
	}

	public void setIdfa(String idfa) {
		this.idfa = idfa;
	}

	public boolean isFromBrowserMobile() {
		return fromBrowserMobile;
	}

	public void setFromBrowserMobile(boolean fromBrowserMobile) {
		this.fromBrowserMobile = fromBrowserMobile;
	}

	public UserAgent getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(UserAgent userAgent) {
		this.userAgent = userAgent;
	}

	public boolean isFromBrowserPc() {
		return fromBrowserPc;
	}

	public void setFromBrowserPc(boolean fromBrowserPc) {
		this.fromBrowserPc = fromBrowserPc;
	}

	public IpInfo getIpInfo() {
		return ipInfo;
	}

	public void setIpInfo(IpInfo ipInfo) {
		this.ipInfo = ipInfo;
	}

}
