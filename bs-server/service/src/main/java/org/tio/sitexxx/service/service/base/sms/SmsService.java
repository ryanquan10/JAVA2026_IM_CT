
package org.tio.sitexxx.service.service.base.sms;

import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.kit.Ret;
import org.tio.sitexxx.service.api.sms.BaseSmsIntf;
import org.tio.sitexxx.service.api.sms.BaseSmsResultVo;
import org.tio.sitexxx.service.api.sms.BaseSmsVo;
//import org.tio.sitexxx.service.api.sms.impl.AliyunSmsImpl;
import org.tio.sitexxx.service.api.sms.impl.AliyunSmsImpl;
import org.tio.sitexxx.service.api.sms.impl.SmsbaoSmsImpl;
import org.tio.sitexxx.service.api.sms.impl.YunSmsImpl;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.model.conf.ClientConf;
import org.tio.sitexxx.service.model.main.SmsLog;
import org.tio.sitexxx.service.service.base.UserService;
import org.tio.sitexxx.service.service.conf.ConfService;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.PayConst;
import org.tio.sitexxx.service.vo.SentSmsResultVo;
import org.tio.utils.jfinal.P;

import cn.hutool.core.util.StrUtil;

/**
 * 发送短信
 */
public class SmsService {

    private static Logger log = LoggerFactory.getLogger(SmsService.class);
    private static BaseSmsIntf<BaseSmsVo, BaseSmsResultVo> baseSmsService;

    static {
        if (Objects.equals(Const.SMS_TYPE, Const.SmsVersion.SMS_BAO)) {
            baseSmsService = new SmsbaoSmsImpl();
        } else if (Objects.equals(Const.SMS_TYPE, Const.SmsVersion.SMS_ALI)) {
            baseSmsService = new AliyunSmsImpl();
        } else {
            baseSmsService = new YunSmsImpl();
        }
    }


    public static final SmsService me = new SmsService();

    /**
     * 是不是一个手机号码
     *
     * @param mobile
     * @return
     */
    public static boolean isMobileNo(String mobile) {
        if (mobile == null || mobile.length() != 11 || !mobile.startsWith("1")) {
            return false;
        }
        return NumberUtils.isDigits(mobile);
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

    }

    /**
     * 发送短信
     *
     * @param mobile
     * @param biztype
     * @param ip
     * @param sessionid
     * @param referer
     * @param cookieKey
     * @param extParams
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年12月10日 下午4:50:22
     */
    public Ret send(String mobile, Short biztype, String ip, String sessionid, String referer, String cookieKey, Map<String, String> extParams) throws Exception {
//		if (!isMobileNo(mobile)) {
//			log.error("phone:{}, is invalid", mobile);
//			saveSmsInfo(ip, mobile, "", "", biztype, "手机号码格式不正确", 0, 0, null, Const.Status.DISABLED, sessionid, referer, cookieKey);
//			return RetUtils.failMsg("手机号格式有误");
//		}
        Integer ipcount = null;
        int maxcountperdayip = ConfService.getInt("sms.maxcount.ip.perday", P.getInt("sms.maxcount.ip.perday", 100));
        ipcount = (Integer) Caches.getCache(CacheConfig.SMS_IP_COUNT).get(ip);
        if (ipcount != null && ipcount > maxcountperdayip) {
            log.error("ip:{}, has sent {}", mobile, ipcount);
            saveSmsInfo(ip, mobile, "", "", biztype, "24小时内ip获取验证码次数超上限", 0, ipcount, null, Const.Status.DISABLED, sessionid, referer, cookieKey);
            return RetUtils.failMsg("24小时内获取验证码次数超上限！");
        } else if (ipcount == null) {
            ipcount = 0;
        }
        Integer mobilecount = (Integer) Caches.getCache(CacheConfig.SMS_MOBILE_COUNT).get(mobile);
        if (mobilecount == null) {
            mobilecount = 0;
        } else if (mobilecount >= P.getInt("sms.maxcount.mobile.perday", 10)) {
            saveSmsInfo(ip, mobile, "", "", biztype, "24小时内手机获取验证码次数超上限", mobilecount, ipcount, null, Const.Status.DISABLED, sessionid, referer, cookieKey);
            log.error("mobile:{}, has sent {}", mobile, mobilecount);
            return RetUtils.failMsg("24小时内获取验证码次数超上限！");
        }
        Integer deviceCount = null;
        if (StrUtil.isNotBlank(cookieKey)) {
            deviceCount = (Integer) Caches.getCache(CacheConfig.SMS_DEVICE_COUNT).get(cookieKey);
            if (deviceCount == null) {
                deviceCount = 0;
            }
            if (deviceCount >= P.getInt("sms.maxcount.device.count", 5)) {
                saveSmsInfo(ip, mobile, "", "", biztype, "1个设备1个小时只能发送5次", mobilecount, ipcount, deviceCount, Const.Status.DISABLED, sessionid, referer, cookieKey);
                log.error("mobile:{}, has sent {}", mobile, mobilecount);
                return RetUtils.failMsg("设备发送次数过多，请稍后再试");
            }
        }
        BaseSmsVo smsVo = baseSmsService.initSmsVo(mobile, biztype, "", extParams);
        String code = baseSmsService.getCode(smsVo, true);
        BaseSmsResultVo resultVo = baseSmsService.sendCode(smsVo, code);
        if (resultVo.getThirdCode().equals(SentSmsResultVo.ThreeCode.SUCCESS) || resultVo.getThirdCode().equals("OK")) {
            mobilecount++;
            ipcount++;
            Caches.getCache(CacheConfig.SMS_MOBILE_COUNT).put(mobile, mobilecount);
            Caches.getCache(CacheConfig.SMS_IP_COUNT).put(ip, ipcount);
            if (StrUtil.isNotBlank(cookieKey)) {
                deviceCount++;
                Caches.getCache(CacheConfig.SMS_DEVICE_COUNT).put(cookieKey, deviceCount);
            }
            resultVo.setCode(SentSmsResultVo.Code.SUCCESS);
            if (smsVo.isCodeAdd()) {
                baseSmsService.updateCode(smsVo);
            }
            saveSmsInfo(ip, mobile, code, "", biztype, "", mobilecount, ipcount, deviceCount, Const.Status.NORMAL, sessionid, referer, cookieKey);
            return RetUtils.okData(code);
        } else {
            log.error("短信发送失败，短信接口:{}, 手机号码:{}, remoteip:{},msg:{}", baseSmsService.getClass().getName(), mobile, ip, resultVo.getThirdMsg());
            saveSmsInfo(ip, mobile, code, "", biztype, resultVo.getThirdMsg(), mobilecount, ipcount, deviceCount, Const.Status.DISABLED, sessionid, referer, cookieKey);
            return RetUtils.failMsg(resultVo.getThirdMsg());
        }
    }

    /**
     * 校验验证码
     *
     * @param mobile
     * @param biztype
     * @param code
     * @param extParams
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年12月10日 下午4:56:04
     */
    public Ret checkCode(String mobile, Short biztype, String code, Map<String, String> extParams, boolean isDel) throws Exception {
        ClientConf clientConf = ClientConf.dao.findFirst("select * from client_conf where name = 'isOpenVerifyCode'");
        if (!Const.SMS_OPENFLAG || clientConf.getValue().equals(0)) {
            return RetUtils.okMsg("校验成功");
        }
        //验证码
        ClientConf messageCodeConf = ClientConf.dao.findFirst("select * from client_conf where name = 'messageCode'");
        if (messageCodeConf != null) {
            Integer messageCodeVal = messageCodeConf.getValue();
            if (messageCodeVal != null) {
                if (code.equals(messageCodeVal + "")) {
                    return RetUtils.okMsg("校验成功");
                }
            }
        }

        if (StrUtil.isBlank(mobile) || biztype == null || StrUtil.isBlank(code)) {
            return RetUtils.invalidParam();
        }
        if (Objects.equals(Const.SMS_TYPE, Const.SmsVersion.SMS_YUN)) {
            Boolean b = baseSmsService.verifyCode(mobile, code);
            if (b) {
                return RetUtils.okMsg("校验成功");
            } else {
                return RetUtils.failMsg("验证码不正确");
            }
        }
        BaseSmsVo smsVo = baseSmsService.initSmsVo(mobile, biztype, "", extParams);
        String cacheCode = baseSmsService.getCode(smsVo, false);
        if (StrUtil.isBlank(cacheCode)) {
            return RetUtils.failMsg("验证码已失效");
        }
        boolean check = code.equals(cacheCode);
        if (check) {
            if (isDel) {
                baseSmsService.delCode(smsVo);
            }
            return RetUtils.okMsg("校验成功");
        } else {
            return RetUtils.failMsg("验证码不正确");
        }

    }

    /**
     * @param mobile
     * @param biztype
     * @param code
     * @return
     * @author lixinji
     * 2020年12月16日 上午10:07:57
     */
    public Ret delCode(String mobile, Short biztype) throws Exception {
        if (StrUtil.isBlank(mobile) || biztype == null) {
            return RetUtils.invalidParam();
        }
        BaseSmsVo smsVo = baseSmsService.initSmsVo(mobile, biztype, "", null);
        baseSmsService.delCode(smsVo);
        return RetUtils.okOper();
    }

    /**
     * 保存短信发送记录日志
     *
     * @param remoteip
     * @param mobile
     * @param code
     * @param templateCode
     * @param smsCheckType
     * @param errorMsg
     * @param mobilecount
     * @param ipcount
     */
    private void saveSmsInfo(String remoteip, String phone, String code, String templateCode, Short biztype, String errorMsg, Integer mobilecount, Integer ipcount,
                             Integer devicecount, Short status, String sessionid, String referer, String cookiekey) {
        SmsLog smsLog = new SmsLog();
        smsLog.setIp(remoteip);
        smsLog.setPhone(phone);
        smsLog.setSmscode(code);
        smsLog.setTempcode(templateCode);
        smsLog.setType(biztype + "");
        smsLog.setStatus(status);
        smsLog.setErrormsg(errorMsg);
        smsLog.setPhone24count(mobilecount);
        smsLog.setIp24count(ipcount);
        if (devicecount != null) {
            smsLog.setDevicecount(devicecount);
        }
        if (StrUtil.isNotBlank(cookiekey)) {
            smsLog.setCookiekey(cookiekey);
        }
        smsLog.setSessionid(sessionid);
        smsLog.setReferer(referer);
        smsLog.save();
    }

}
