
package org.tio.mg.web.server.controller.base;

import java.util.Date;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpConfig;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.http.common.session.HttpSession;
import org.tio.http.server.annotation.RequestPath;
import org.tio.http.server.util.Resps;
import org.tio.jfinal.kit.Ret;
import org.tio.mg.service.model.main.UserAgent;
import org.tio.mg.service.model.mg.MgIpInfo;
import org.tio.mg.service.model.mg.MgOperLog;
import org.tio.mg.service.model.mg.MgUser;
import org.tio.mg.service.model.mg.MgUserLoginLog;
import org.tio.mg.service.model.mg.MgUserToken;
import org.tio.mg.service.service.base.IpInfoService;
import org.tio.mg.service.service.conf.MgConfService;
import org.tio.mg.service.service.mg.MgUserService;
import org.tio.mg.service.service.mg.MgUserTokenService;
import org.tio.mg.service.utils.PeriodUtils;
import org.tio.mg.service.utils.RetUtils;
import org.tio.mg.service.vo.MgConst;
import org.tio.mg.service.vo.RequestExt;
import org.tio.mg.service.vo.SessionExt;
import org.tio.mg.web.server.init.WebApiInit;
import org.tio.mg.web.server.utils.GoogleAuthUtils;
import org.tio.mg.web.server.utils.WebUtils;
import org.tio.mg.web.server.vo.LoginResult.ErrorCode;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.SessionKey;
import org.tio.utils.SystemTimer;
import org.tio.utils.jfinal.P;
import org.tio.utils.resp.Resp;

import cn.hutool.core.util.StrUtil;

/**
 * @author xufei
 * 2020年5月25日 下午4:45:27
 */
@RequestPath(value = "/mglogin")
public class MgLoginController {
    private static Logger log = LoggerFactory.getLogger(MgLoginController.class);

    /**
     * @param args
     * @author tanyaowu
     */
    public static void main(String[] args) {

    }

    private MgUserService userService = MgUserService.ME;

    /**
     * @author tanyaowu
     */
    public MgLoginController() {
    }

    /**
     * 是否是第三方登录
     *
     * @param loginname
     * @param pd5          md5加密的密码
     * @param authcode
     * @param isThirdLogin
     * @param user
     * @param request
     * @return
     */
    private Ret _login(String loginname, String pd5, String authcode, HttpRequest request) {
        RequestExt requestExt = WebUtils.getRequestExt(request);
        Ret ret = null;
        MgUser user = null;
        ret = userService.login(loginname, pd5, requestExt.getIpInfo());
        if (ret.isFail()) {
            Resp resp = Resp.fail();
            Integer code = (Integer) ret.get("code");
            if (code == 3) {
                resp.code(3);
            } else {
                resp.code(ErrorCode.USER_OR_PWD_ERROR_PWD.code).msg(ErrorCode.USER_OR_PWD_ERROR_PWD.value);
            }

            HttpResponse httpResponse = Resps.json(request, resp);
            return Ret.fail().set("resp", httpResponse);
        } else {
            user = RetUtils.getOkTData(ret);
        }

        if (user != null) {
            Resp resp = checkStatus(user);
            if (resp.isOk()) {
                return Ret.ok().set("user", user);
            } else {
                HttpResponse httpResponse = Resps.json(request, resp);
                return Ret.fail().set("resp", httpResponse);
            }
        } else {
            Resp resp = Resp.fail();
            resp.code(ErrorCode.USER_OR_PWD_ERROR_PWD.code).msg(ErrorCode.USER_OR_PWD_ERROR_PWD.value);
            HttpResponse httpResponse = Resps.json(request, resp);
            return Ret.fail().set("resp", httpResponse);
        }
    }

    /**
     * @param user
     * @return
     * @author xufei
     * 2020年5月26日 下午3:11:48
     */
    private static Resp checkStatus(MgUser user) {
        if (Objects.equals(user.getStatus(), MgConst.MgUserStatus.NORMAL)) {
            return Resp.ok();
        } else {
            if (Objects.equals(user.getStatus(), MgConst.MgUserStatus.INBLACK)) {
                return Resp.fail().code(ErrorCode.USER_INBLACK_ERROR.code).msg(ErrorCode.USER_INBLACK_ERROR.value);
            } else {
                return Resp.fail().code(ErrorCode.USER_STATUS_ERROR.code).msg(ErrorCode.USER_STATUS_ERROR.value);
            }
        }
    }

    /**
     * http://127.0.0.1/login?loginname=tywo45@163.com&pwd=c68fd49a78b33a0199bed7b6c8953953&pd5=fidksjfdlskfjdks
     * 登录，这个是公共的，具体实现是在_login中
     *
     * @param loginname //	 * @param pwd
     * @param pd5       md5加密后的密码
     * @param authcode  图形验证码
     * @param request
     * @return
     * @throws Exception
     */
    @RequestPath(value = "")
    public HttpResponse login(String loginname, String pd5, String authcode, String code, HttpRequest request) throws Exception {
        loginname = StrUtil.trim(loginname);
        String sessionId = request.getHttpSession().getId();
        MgUser user = null;
        HttpSession httpSession = request.getHttpSession();
        HttpConfig httpConfig = request.getHttpConfig();
        Boolean aFalse = MgConfService.getBoolean("sys.login.google.auth", "false");

        Ret ret = this._login(loginname, pd5, authcode, request);
        if (ret.isOk()) {
            user = (MgUser) ret.get("user");

            if (aFalse != null && aFalse.booleanValue() && user.getIsBound().shortValue() == 1) {
                if(StrUtil.isEmpty(code)){
                    return Resps.json(request, Resp.fail("谷歌验证码不能为空"));
                }
                //验证谷歌验证码
                boolean isValid = GoogleAuthUtils.validateCode(user.getSecretKey(), code);
                if(!isValid){
                    return Resps.json(request, Resp.fail("谷歌验证码错误"));
                }
            }

            RequestExt requestExt = WebUtils.getRequestExt(request);
            short deviceType = requestExt.getDeviceType();
            //是否是从手机端过来的请求
            boolean fromApp = requestExt.isFromApp();

            //添加登录日志
            String ip = request.getClientIp();
            MgIpInfo ipinfo = IpInfoService.ME.mgSave(ip);
            MgOperLog operLog = new MgOperLog();
            operLog.setModename("登录");
            operLog.setAid(-1);
            operLog.setOperip(ip);
            operLog.setOperparam(loginname + "****" + authcode);
            operLog.setOpertype(MgConst.OperLogType.SYS);
            if (fromApp) {
                operLog.setDeviceinfo(StringUtils.substring(requestExt.getDeviceinfo(), 0, 128));
            } else {
                operLog.setDeviceinfo(StringUtils.substring(request.getUserAgent(), 0, 128));
            }
            operLog.save();
            MgUserLoginLog userLoginLog = new MgUserLoginLog();
            Date time = new Date();
            userLoginLog.setIp(ip);
            userLoginLog.setIpid(ipinfo.getId());
            userLoginLog.setSessionid(sessionId);
            userLoginLog.setMguid(user.getId());
            userLoginLog.setDevicetype(deviceType);
            userLoginLog.setDayperiod(PeriodUtils.dateToPeriodByType(time, Const.PeriodType.DAY));
            userLoginLog.setTimeperiod(PeriodUtils.dateToPeriodByType(time, Const.PeriodType.TIME));
            userLoginLog.setHourperiod(PeriodUtils.dateToPeriodByType(time, Const.PeriodType.HOUR));
            userLoginLog.setTime(time);
            if (fromApp) {
                userLoginLog.setDeviceinfo(StringUtils.substring(requestExt.getDeviceinfo(), 0, 128));
                userLoginLog.setImei(requestExt.getImei());
            } else {
                UserAgent userAgent = requestExt.getUserAgent();
                if (userAgent != null) {
                    userLoginLog.setDeviceinfo(userAgent.getOsName() + " " + userAgent.getOsVersion() + "/" + userAgent.getAgentName() + " " + userAgent.getAgentVersionMajor());
                } else {
                    userLoginLog.setDeviceinfo(StringUtils.substring(request.getUserAgent(), 0, 128));
                }
            }
            userLoginLog.save();
            HttpResponse httpResponse = Resps.json(request, Resp.ok());
            // 先更新sessionId，防止一个sessionId存留过久，减少sessionId被盗的风险
            SessionExt oldSessionExt1 = httpSession.getAttribute(SessionKey.SESSION_EXT, SessionExt.class);
            if (SystemTimer.currTime - oldSessionExt1.getCreateTime() > 1000 * 60 * 10) {
                WebApiInit.requestHandler.updateSessionId(request, httpSession, httpResponse);
            }

            String newSeesionId = request.getHttpSession().getId();
            //token
            MgUserToken userToken = MgUserTokenService.me.find(deviceType, user.getId());
            if (userToken == null) {
                userToken = new MgUserToken();
                userToken.setMguid(user.getId());
                userToken.setDevicetype(deviceType);
                userToken.setToken(newSeesionId);
                MgUserTokenService.me.add(userToken);
            } else {
                String oldToken = userToken.getToken();
                if (Objects.equals(oldToken, sessionId)) {
                    //这里是自己T自己，相当于没T
                } else {
                    boolean allowOper = P.getBoolean("oper.open.flag", true);
                    if (allowOper) {
                        //把原来别人登录的T出去
                        HttpSession oldHttpSession = (HttpSession) httpConfig.getSessionStore().get(oldToken);
                        if (oldHttpSession != null) {
                            if (Objects.equals(MgConfService.getInt("login.use.sso", 2), 1)) { //启用sso（单点登录）
//								//发送被踢的信息-待扩展
//								int kicktedCode = AppCode.ForbidOper.KICKTED;
//								if(Objects.equals(deviceType, Devicetype.PC.getValue())) {
//									kicktedCode = AppCode.ForbidOper.KICKTED_PC;
//								} else if (Objects.equals(deviceType, Devicetype.H5.getValue())) {
//									kicktedCode = AppCode.ForbidOper.KICKTED_H5;
//								}
//								WxChatApi.sendFriendErrorMsg(deviceType, sessionId, ip, user.getId(), user.getId(), user.getId(), null,
//										kicktedCode, "当前账号已在其他设备登录");
                                //此处增加长链接断开逻辑
                                //Tio.removeToken(TioSiteImServerStarter.tioServerConfigApp, oldToken, "异地登录被踢");
                                SessionExt oldSessionExt = oldHttpSession.getAttribute(SessionKey.SESSION_EXT, SessionExt.class, new SessionExt(), httpConfig);
                                oldSessionExt.setUid(null);
                                oldSessionExt.setKickedInfo(operLog);
                                oldHttpSession.update(httpConfig);//切记：每次修改SessionExt后，要调用一下update把数据更新到分布式缓存中
                            } else {
                                //不启用sso（单点登录）
                            }
                        }
                        userToken.setToken(newSeesionId);
                        MgUserTokenService.me.update(userToken);
                    }
                }
            }
            SessionExt sessionExt = httpSession.getAttribute(SessionKey.SESSION_EXT, SessionExt.class);
            sessionExt.setUid(user.getId());
            sessionExt.setLoginTime(SystemTimer.currTime);
            httpSession.update(httpConfig);//切记：每次修改SessionExt后，要调用一下update把数据更新到分布式缓存中
            return httpResponse;
        } else {
            HttpResponse httpResponse = (HttpResponse) ret.get("resp");
            if (httpResponse == null) {
                log.error("_login(loginname, pwd, authcode, request)返回值没有包含response信息");
                return Resps.json(request, Resp.fail("服务器异常"));
            }
            return httpResponse;
        }
    }
}
