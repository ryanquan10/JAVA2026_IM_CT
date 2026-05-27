
package org.tio.sitexxx.web.server.controller.base;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.EscapeUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.Tio;
import org.tio.http.common.HttpConfig;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.http.common.UploadFile;
import org.tio.http.common.session.HttpSession;
import org.tio.http.server.annotation.RequestPath;
import org.tio.http.server.mvc.Routes;
import org.tio.http.server.util.Resps;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Page;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.sitexxx.im.common.Command;
import org.tio.sitexxx.im.common.ImPacket;
import org.tio.sitexxx.im.common.bs.wx.group.WxGroupOperNtf;
import org.tio.sitexxx.im.server.Ims;
import org.tio.sitexxx.im.server.TioSiteImServerStarter;
import org.tio.sitexxx.im.server.handler.wx.WxChatApi;
import org.tio.sitexxx.im.server.handler.wx.WxChatQueueApi;
import org.tio.sitexxx.im.server.handler.wx.WxSynApi;
import org.tio.sitexxx.service.api.sms.BaseSmsVo;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.model.conf.ClientConf;
import org.tio.sitexxx.service.model.conf.Conf;
import org.tio.sitexxx.service.model.main.*;
import org.tio.sitexxx.service.model.stat.GroupStat;
import org.tio.sitexxx.service.pay.init.PayInit;
import org.tio.sitexxx.service.service.ImgService;
import org.tio.sitexxx.service.service.base.*;
import org.tio.sitexxx.service.service.base.sms.SmsService;
import org.tio.sitexxx.service.service.chat.ChatIndexService;
import org.tio.sitexxx.service.service.chat.ChatService;
import org.tio.sitexxx.service.service.chat.FriendService;
import org.tio.sitexxx.service.service.chat.GroupService;
import org.tio.sitexxx.service.service.conf.AvatarService;
import org.tio.sitexxx.service.service.conf.ConfService;
import org.tio.sitexxx.service.utils.*;
import org.tio.sitexxx.service.vo.*;
import org.tio.sitexxx.service.vo.wx.SysMsgVo;
import org.tio.sitexxx.web.server.controller.base.sms.SmsController;
import org.tio.sitexxx.web.server.controller.base.thirdlogin.ThirdLoginFactory;
import org.tio.sitexxx.web.server.controller.wx.ChatController;
import org.tio.sitexxx.web.server.utils.VideoUtils;
import org.tio.sitexxx.web.server.utils.WebUtils;
import org.tio.sitexxx.service.vo.WxMomentMsgVo;
import org.tio.utils.cache.CacheUtils;
import org.tio.utils.cache.FirsthandCreater;
import org.tio.utils.cache.ICache;
import org.tio.utils.hutool.StrUtil;
import org.tio.utils.json.Json;
import org.tio.utils.resp.Resp;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.img.ImgUtil;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import static org.tio.sitexxx.web.server.init.WebApiInit.TEMP_USER_REQUEST;

/**
 * @author tanyaowu
 * 2016年6月29日 下午7:53:59
 */
@RequestPath(value = "/user")
public class UserController {
    private static Logger log = LoggerFactory.getLogger(UserController.class);

    private UserService userService = UserService.ME;
    private final static GroupService groupService = GroupService.me;

    /**
     * 搜索用户
     *
     * @param nick
     * @param uid
     * @param loginname
     * @param pageNumber
     * @param pageSize
     * @param request
     * @return
     * @throws Exception
     * @author tanyaowu
     */
    @RequestPath(value = "/search")
    public Resp search(String nick, Integer uid, String loginname, Integer pageNumber, Integer pageSize, HttpRequest request) throws Exception {
        Integer type = ClientConf.dao.findFirst("select * from client_conf where name = 'searchType'").getValue();
        if (type.equals(0)) {
            return Resp.fail().msg("当前不允许搜索好友");
        }
        pageSize = 20; //
        User curr = WebUtils.currUser(request);
        Page<Record> page = UserService.ME.search(curr, nick, uid, loginname, pageNumber, pageSize);
        return Resp.ok(page);
    }

    /**
     * 查询用户信息，主要用于展示给其它人看，所以有的信息是不允许查询出来的
     *
     * @param uid
     * @param request
     * @return
     * @throws Exception
     * @author tanyaowu
     */
    @RequestPath(value = "/info")
    public Resp info(Integer uid, HttpRequest request) throws Exception {
        if (uid == null) {
            return Resp.fail("用户id参数为空");
        }
        User curr = WebUtils.currUser(request);
        Record record = UserService.ME.info(uid);
        if (Objects.equals(User.Status.LOGOUT, record.getShort("status"))) {
            return Resp.fail("该用户已注销");
        }
        if (curr != null) {
            WxChatUserItem fd = ChatIndexService.fdUserIndex(curr.getId(), uid);
            if (ChatService.existFriend(fd)) {
                WxFriend friend = FriendService.me.getFriendInfo(fd.getLinkid());
                if (friend != null && StrUtil.isNotBlank(friend.getRemarkname())) {
                    Record newread = new Record();
                    Map<String, Object> retMap = new HashMap<String, Object>();
                    BeanUtil.copyProperties(record.getColumns(), retMap);
                    retMap.put("remarkname", friend.getRemarkname());
                    if (friend.getLabelids() != null) {
                        String[] labelidList = friend.getLabelids().split(",");
                        ArrayList<Label> labelList = new ArrayList<>();
                        for (String labelid : labelidList) {
                            Label label = Label.dao.findById(labelid);
                            labelList.add(label);
                        }
                        retMap.put("labelList", labelList);
                    }
                    newread.setColumns(retMap);
                    return Resp.ok(newread);
                }
            } else {
                StrangerRemarkName strangerRemarkName = StrangerRemarkName.dao.findFirst("select * from stranger_remark_name where uid = ? and relation_uid = ?", curr.getId(), uid);
                if (strangerRemarkName != null) {
                    Record newread = new Record();
                    Map<String, Object> retMap = new HashMap<String, Object>();
                    BeanUtil.copyProperties(record.getColumns(), retMap);
                    retMap.put("remarkname", strangerRemarkName.getRemarkName());
                    newread.setColumns(retMap);
                    return Resp.ok(newread);
                }
            }
        }
        return Resp.ok(record);
    }

    /**
     * @param uid
     * @param request
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年10月16日 下午2:01:46
     */
    @RequestPath(value = "/block")
    public Resp block(Integer uid, HttpRequest request) throws Exception {
        if (uid == null) {
            return Resp.fail("用户id参数为空");
        }
        User curr = WebUtils.currUser(request);
        WxUserBlackItems items = ChatService.getBlockItems(curr.getId(), uid);
        return Resp.ok(items != null ? Const.YesOrNo.YES : Const.YesOrNo.NO);
    }

    /**
     * 给超管专用的，其它用户调不到这个方法
     *
     * @param uid
     * @param request
     * @return
     * @throws Exception
     * @author tanyaowu
     */
    @RequestPath(value = "/info1")
    public Resp info1(Integer uid, HttpRequest request) throws Exception {
        User curr = WebUtils.currUser(request);
        Record record = UserService.ME.info1(curr, uid);
        if (curr != null) {
            WxChatUserItem fd = ChatIndexService.fdUserIndex(curr.getId(), uid);
            if (ChatService.existFriend(fd)) {
                WxFriend friend = FriendService.me.getFriendInfo(fd.getLinkid());
                if (friend != null && StrUtil.isNotBlank(friend.getRemarkname())) {
                    Record newread = new Record();
                    Map<String, Object> retMap = new HashMap<String, Object>();
                    BeanUtil.copyProperties(record.getColumns(), retMap);
                    retMap.put("remarkname", friend.getRemarkname());
                    newread.setColumns(retMap);
                    return Resp.ok(newread);
                }
            }
        }
        return Resp.ok(record);
    }

    /**
     * 获取当前用户
     *
     * @param request
     * @return
     * @throws Exception
     * @author tanyaowu
     */
    @RequestPath(value = "/curr")
    public Resp curr(HttpRequest request) throws Exception {
        User user = WebUtils.currUser(request);
        if (user != null) {
            Resp resp = Resp.ok(user);
            //			HttpResponse ret = Resps.json(request, resp);
            return resp;
        } else {

            if (Const.USE_ANONYMOUS) { //用匿名
                request.setAttribute(org.tio.sitexxx.service.vo.RequestKey.USE_ROBOT, true);
                LoginController loginController = Routes.getController(LoginController.class);
                loginController.login(null, null, null, request);
                return curr(request);
            } else {
                Resp resp = Resp.fail("You're not logged in");
                //				HttpResponse ret = Resps.json(request, resp);
                return resp;
            }
        }
    }

    /**
     * 根据token，获取用户ID（此处的token其实就是sessionId）
     *
     * @param request
     * @param token
     * @return
     * @throws Exception
     * @author: tanyaowu
     */
    @RequestPath(value = "/byToken")
    public Resp byToken(HttpRequest request, String token) throws Exception {

        String sessionId = token;
        HttpSession httpSession = request.httpConfig.getHttpSession(sessionId);

        if (httpSession != null) {
            SessionExt sessionExt = httpSession.getAttribute(SessionKey.SESSION_EXT, SessionExt.class);
            Integer userid = sessionExt.getUid();

            if (userid != null) {
                Resp resp = Resp.ok(userid);
                return resp;//Resps.json(request, resp);
            } else {
                log.info("{} session中并未绑定userid", request.getChannelContext());
            }
        } else {
            log.info("{} 不能根据sessionId[{}]找到session对象", request.getChannelContext(), sessionId);
        }

        Resp resp = Resp.fail();
        return resp;//Resps.json(request, resp);

    }

    /**
     * @param request
     * @param x       userid
     * @return
     * @throws Exception
     * @author tanyaowu
     */
    @RequestPath(value = "/by")
    public Resp by(HttpRequest request, Integer x) throws Exception {
        if (x == null) {
            Tio.remove(request.getChannelContext(), "非法请求");
            return null;
        }

        SimpleUser simpleUser = SimpleUser.fromUid(x);
        Resp resp = null;
        if (simpleUser != null) {
            resp = Resp.ok(simpleUser);
        } else {
            resp = Resp.fail("can not found the userid");
        }
        return resp;
    }

    /**
     * @param request
     * @param initPwd
     * @param newPwd
     * @return
     * @author lixinji
     * 2021年3月12日 上午10:09:11
     */
    @RequestPath(value = "/updatepaypwd")
    public Resp updatePayPwd(HttpRequest request, String initPwd, String newPwd) {
        if (StrUtil.isBlank(initPwd)) {
            return Resp.fail("原密码不允许为空");
        }
        if (StrUtil.isBlank(newPwd)) {
            return Resp.fail("新密码不允许为空");
        }
        User curr = WebUtils.currUser(request);
        if (Objects.equals(curr.getPwd(), newPwd)) {
            return Resp.fail("新密码与原密码相同");
        }
        Ret ret = userService.updatePayPwd(curr, newPwd, initPwd);
        if (ret.isFail()) {
            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        return Resp.ok(RetUtils.getOkData(ret));
    }

    /**
     * @param request
     * @param paypwd
     * @return
     * @author lixinji
     * 2021年3月12日 上午10:13:03
     */
    @RequestPath(value = "/setpaypwd")
    public Resp setPayPwd(HttpRequest request, String paypwd) {
        if (StrUtil.isBlank(paypwd)) {
            return Resp.fail("密码不允许为空");
        }
        User curr = WebUtils.currUser(request);
        if (Objects.equals(curr.getPaypwdflag(), Const.YesOrNo.YES)) {
            return Resp.fail("用户已设置支付密码");
        }
        Ret ret = userService.setPayPwd(curr, paypwd);
        if (ret.isFail()) {
            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        return Resp.ok(RetUtils.getOkData(ret));
    }

    /**
     * 验证支付密码
     *
     * @param request
     * @param paypwd
     * @return
     * @author lixinji
     * 2021年3月15日 上午11:25:53
     */
    @RequestPath(value = "/checkpaypwd")
    public Resp checkPaypwd(HttpRequest request, String paypwd) {
        if (StrUtil.isBlank(paypwd)) {
            return Resp.fail("支付密码为空");
        }
        User curr = WebUtils.currUser(request);
        if (!Objects.equals(paypwd, curr.getPaypwd())) {
            return Resp.fail("支付密码错误");
        }
        return Resp.ok();
    }

    /**
     * @param request
     * @param code
     * @param paypwd
     * @return
     * @throws Exception
     * @author lixinji
     * 2021年3月12日 上午10:15:19
     */
    @RequestPath(value = "/resetpaypwd")
    public Resp resetPayPwd(HttpRequest request, String code, String paypwd) throws Exception {
        User curr = WebUtils.currUser(request);
        if (StrUtil.isBlank(paypwd) || StrUtil.isBlank(code)) {
            return RetUtils.getInvalidResp();
        }
        String phone = curr.getPhone();
        Resp beforeCheck = SmsController.bizPhoneCheck(BaseSmsVo.BaseSmsBizType.PWD_PAY_BACK, phone, request);
        if (!beforeCheck.isOk()) {
            return beforeCheck;
        }
        Ret ret = SmsService.me.checkCode(phone, BaseSmsVo.BaseSmsBizType.PWD_PAY_BACK, code, null, false);
        if (ret.isFail()) {
            return Resp.fail(RetUtils.getRetMsg(ret));
        }
        User user = userService.getByPhone(phone, null);
        if (user == null) {
            return Resp.fail("用户不存在");
        }
        Resp resp = userService.resetPayPwd(user, paypwd);
        if (resp.isOk()) {
            SmsService.me.delCode(phone, BaseSmsVo.BaseSmsBizType.PWD_PAY_BACK);
        }
        return resp;
    }

    /**
     * 查询登录日志
     *
     * @param request
     * @param uid
     * @param pageNumber
     * @return
     * @author tanyaowu
     */
    @RequestPath(value = "/pageLoginLog")
    public Resp pageLoginLog(HttpRequest request, Integer uid, Integer pageNumber) {
        User curr = WebUtils.currUser(request);
        boolean isSuper = UserService.isSuper(curr);
        if (!isSuper) { //非超管不能查别人的登录日志
            uid = curr.getId();
        }
        Page<Record> page = LoginLogService.me.page(curr, uid, pageNumber);
        return Resp.ok(page);
    }

    /**
     * 查询访问日志
     *
     * @param request
     * @param uid
     * @param pageNumber
     * @return
     */
    @RequestPath(value = "/pageAccessLog")
    public Resp pageAccessLog(HttpRequest request, Integer uid, Integer pageNumber) {
        User curr = WebUtils.currUser(request);
        boolean isSuper = UserService.isSuper(curr);
        if (!isSuper) { //非超管不能查别人的日志
            uid = curr.getId();
        }
        Page<Record> page = ChatroomJoinLeaveService.me.page(curr, uid, pageNumber);
        return Resp.ok(page);
    }

    /**
     * @param userAddress
     * @param request
     * @return
     * @throws Exception
     */
    @RequestPath(value = "/updateAds")
    public Resp updateAds(UserAddress userAddress, HttpRequest request) throws Exception {
        User user = WebUtils.currUser(request);
        Ret ret = userService.updateUserAddress(user.getId(), userAddress);
        if (ret.isOk()) {
            return Resp.ok().data(ret.get("data"));
        } else {
            return Resp.fail(ret.getStr("msg"));
        }
    }

    /*********************************************begin- 登录注册相关***************************************************************/

    /**
     * 修改密码
     *
     * @param request
     * @param initPwd
     * @param newPwd
     * @param emailpwd 废除字段，可忽略不传
     * @return
     * @author lixinji
     * 2022年5月10日 下午12:02:54
     */
    @RequestPath(value = "/updatePwd")
    public Resp updatePwd(HttpRequest request, String initPwd, String newPwd, String emailpwd) {
        if (StrUtil.isBlank(initPwd)) {
            return Resp.fail("原密码不允许为空");
        }
        if (StrUtil.isBlank(newPwd)) {
            return Resp.fail("新密码不允许为空");
        }
        User curr = WebUtils.currUser(request);
        if (curr == null){
            return Resp.fail("请登录");
        }
        return userService.updatePwd(curr, initPwd, newPwd, emailpwd);
    }

    /**
     * 找回密码前置
     * 没有邮箱账号后，可以废除该功能，2022年5月后正式废除
     *
     * @param request
     * @param phone
     * @param code
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年12月16日 下午5:53:11
     */
    @Deprecated
    @RequestPath(value = "/resetPwdBefore")
    public Resp resetPwdBefore(HttpRequest request, String phone, String code) throws Exception {
        if (StrUtil.isBlank(phone) || StrUtil.isBlank(code)) {
            return RetUtils.getInvalidResp();
        }
        Resp beforeCheck = SmsController.bizPhoneCheck(BaseSmsVo.BaseSmsBizType.PWD_BACK, phone, request);
        if (!beforeCheck.isOk()) {
            return beforeCheck;
        }
        Ret ret = SmsService.me.checkCode(phone, BaseSmsVo.BaseSmsBizType.PWD_BACK, code, null, false);
        if (ret.isFail()) {
            return Resp.fail(RetUtils.getRetMsg(ret));
        }
        User user = userService.getByPhone(phone, null);
        if (user == null) {
            return Resp.fail("用户不存在");
        }
        Map<String, String> login = new HashMap<String, String>();
        login.put("email", user.getEmail());
        login.put("phone", user.getPhone());
        return Resp.ok(login);
    }

    /**
     * 找回密码
     *
     * @param request
     * @param phone
     * @param code
     * @param emailpwd 没有邮箱账号时，该值可以忽略
     * @param phonepwd
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年12月16日 下午6:00:46
     */
    @RequestPath(value = "/resetPwd")
    public Resp resetPwd(HttpRequest request, String phone, String code, String phonepwd, String emailpwd) throws Exception {
        if (StrUtil.isBlank(phone) || StrUtil.isBlank(code)) {
            return RetUtils.getInvalidResp();
        }
        Resp beforeCheck = SmsController.bizPhoneCheck(BaseSmsVo.BaseSmsBizType.PWD_BACK, phone, request);
        if (!beforeCheck.isOk()) {
            return beforeCheck;
        }
        Ret ret = SmsService.me.checkCode(phone, BaseSmsVo.BaseSmsBizType.PWD_BACK, code, null, false);
        if (ret.isFail()) {
            return Resp.fail(RetUtils.getRetMsg(ret));
        }
        User user = userService.getByPhone(phone, null);
        if (user == null) {
            return Resp.fail("用户不存在");
        }
        Resp resp = userService.resetPwd(user, phonepwd, emailpwd);
        if (resp.isOk()) {
            SmsService.me.delCode(phone, BaseSmsVo.BaseSmsBizType.PWD_BACK);
        }
        return resp;
    }

    /**
     * 注销检测
     *
     * @param uid
     * @param request
     * @return
     * @throws Exception
     * @author lixinji
     * 2021年4月8日 下午2:54:04
     */
    @RequestPath(value = "/logoutcheck")
    public Resp logoutCheck(Integer uid, HttpRequest request) throws Exception {
        User user = WebUtils.currUser(request);
        if (!Objects.equals(uid, user.getId())) {
            return Resp.fail("登录已失效，请刷新");
        }
        if (!Objects.equals(User.Status.NORMAL, user.getStatus())) {
            return Resp.fail("注销申请失败，请确认账号状态");
        }
        if (Objects.equals(user.getOpenflag(), Const.YesOrNo.YES)) {
            if (user.getWalletid() == null) {
                WxUserCoinLocal userCoin = WxUserCoinLocal.dao.findFirst("select * from wx_user_coin_local where uid = ?", user.getId());
                if (userCoin != null) {
                    user.setWalletid(userCoin.getWalletid());
                }
            }
            boolean checkWallet = PayInit.payService.walletCheckLogout(user);
            if (!checkWallet) {
                return Resp.fail("注销申请失败，请注销开户状态");
            }
        }
        return Resp.ok();
    }

    /**
     * 注销
     *
     * @param uid
     * @param request
     * @return
     * @throws Exception
     * @author lixinji
     * 2021年2月26日 下午4:51:56
     */
    @RequestPath(value = "/logout")
    public Resp logout(Integer uid, String code, HttpRequest request) throws Exception {
        User user = WebUtils.currUser(request);
        if (!Objects.equals(uid, user.getId())) {
            return Resp.fail("登录已失效，请刷新");
        }
        Ret smsRet = SmsService.me.checkCode(user.getPhone(), BaseSmsVo.BaseSmsBizType.LOGOUT, code, null, false);
        if (smsRet.isFail()) {
            return Resp.fail(RetUtils.getRetMsg(smsRet));
        }
        if (!Objects.equals(User.Status.NORMAL, user.getStatus())) {
            return Resp.fail("注销申请失败，请确认账号状态");
        }
        if (Objects.equals(user.getOpenflag(), Const.YesOrNo.YES)) {
            boolean checkWallet = PayInit.payService.walletCheckLogout(user);
            if (!checkWallet) {
                return Resp.fail("注销申请失败，请确认账号状态");
            }
        }
        String ip = request.getClientIp();
        IpInfo ipinfo = IpInfoService.ME.save(ip);
        Ret ret = userService.logout(user, ipinfo.getId());
        if (ret.isFail()) {
            log.error(RetUtils.getRetMsg(ret));
            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        Map<String, List<Ret>> retMap = RetUtils.getOkTData(ret);
        List<Ret> friendList = retMap.get("friend");
        List<Ret> groupList = retMap.get("group");
        Short devicetype = WebUtils.getRequestExt(request).getDeviceType();
        //消息触发
        Const.getBsExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Integer uid = user.getId();
                    for (Ret ret : groupList) {
                        Short nameUpdate = RetUtils.getOkTData(ret, "nameupdate");
                        Long groupid = RetUtils.getOkTData(ret, "groupid");
                        Tio.unbindGroup(TioSiteImServerStarter.tioServerConfigWs, user.getId() + "", groupid + "");
                        //自己退群操作
                        WxGroupOperNtf leaveNtf = new WxGroupOperNtf();
                        leaveNtf.setC("自动退群");
                        leaveNtf.setMid(null);
                        leaveNtf.setT(System.currentTimeMillis());
                        leaveNtf.setUid(uid);
                        leaveNtf.setG(groupid);
                        leaveNtf.setChatlinkid(-groupid);
                        leaveNtf.setOper(Const.WxGroupOper.LEAVE_GROUP);
                        ImPacket imPacket = new ImPacket(Command.WxGroupOperNtf, Json.toJson(leaveNtf));
                        Ims.sendToUser(uid, imPacket);
                        WxChatGroupItem newowner = RetUtils.getOkTData(ret, "owner");
                        WxChatGroupItem leave = RetUtils.getOkTData(ret, "leave");
                        boolean auto = ChatController.auotUpdateGroupInfo(request, devicetype, groupid, nameUpdate, (short) -1, user);
                        WxChatApi.leaveGroup(request, user, leave, newowner, auto);
                    }
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        });
        Const.getBsExecutor().execute(new Runnable() {
            @SuppressWarnings("deprecation")
            @Override
            public void run() {
                try {
                    Integer uid = user.getId();
                    for (Ret ret : friendList) {
                        Long chatlinkid = RetUtils.getOkTData(ret, "chatlinkid");
                        Long tochatlinkid = RetUtils.getOkTData(ret, "tochatlinkid");
                        Integer touid = RetUtils.getOkTData(ret, "touid");
                        if (WxSynApi.isSynVersion()) {
                            WxSynApi.synFriendDel(uid, touid, chatlinkid, tochatlinkid, RetUtils.getOkTData(ret, "fid"));
                        } else {
                            if (chatlinkid != null) {
                                WxChatApi.userChatOper(request, uid, chatlinkid, Const.WxUserOper.DEL_FRIEND, "删除好友", "", null);
                            }
                            if (tochatlinkid != null) {
                                WxChatApi.userChatOper(request, touid, tochatlinkid, Const.WxUserOper.TO_DEL_FRIEND, "被删除好友", "", null);
                            }
                            WxChatApi.friendChangeDelNtf(request, uid, RetUtils.getOkTData(ret, "fid"));
                        }
                    }
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        });
        SmsService.me.delCode(user.getPhone(), BaseSmsVo.BaseSmsBizType.LOGOUT);
        return Resp.ok();
    }


    /**
     * @param request
     * @param phone
     * @param code
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年12月17日 下午3:15:40
     */
    @RequestPath(value = "/thirdbindphone")
    public HttpResponse thirdbindphone(HttpRequest request, String phone, String code) throws Exception {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resps.json(request, Resp.fail().msg("用户未登录"));
        }
        Ret ret = SmsService.me.checkCode(phone, BaseSmsVo.BaseSmsBizType.THIRD_BIND_PHONE, code, null, false);
        if (ret.isFail()) {
            return Resps.json(request, Resp.fail().msg(RetUtils.getRetMsg(ret)));
        }
        User exist = userService.getByPhone(phone, null);
        String typeSplit = ThirdLoginFactory.getSimilarTypesStr(curr.getThirdtype());
        if (exist != null) {
            UserThird check = UserThirdService.me.checkExist(exist.getId(), typeSplit);
            if (check != null) {
                String error = "当前手机号已被其它" + UserThird.getThirdLoginTitle(curr.getThirdtype()) + "绑定,请更换其它手机号";
                Resps.json(request, Resp.fail().msg(error));
            }
        }
        //三方绑定手机号
        Ret bindret = userService.thridBindPhone(curr, exist, phone, typeSplit);
        if (bindret.isFail()) {
            return Resps.json(request, Resp.fail().msg(RetUtils.getRetMsg(bindret)));
        } else {
            SmsService.me.delCode(phone, BaseSmsVo.BaseSmsBizType.THIRD_BIND_PHONE);
            Short login = RetUtils.getOkTData(bindret, "login");
            if (Objects.equals(login, Const.YesOrNo.YES)) {
                List<UserThird> userThirds = RetUtils.getOkTData(bindret, "third");
                for (UserThird userThird : userThirds) {
                    if (StrUtil.isNotBlank(userThird.getUnionid())) {
                        Caches.getCache(CacheConfig.OPENID_USERTHIRD).remove(userThird.getType() + "_" + userThird.getUnionid());
                        //清除该类型下的缓存，一般不会出现，只会在并发下极小出现
                        typeSplit = ThirdLoginFactory.getSimilarTypesStr(userThird.getType());
                        Caches.getCache(CacheConfig.OPENID_USERTHIRD).remove(typeSplit + "_" + userThird.getUnionid());
                    }
                    Caches.getCache(CacheConfig.OPENID_USERTHIRD).remove(userThird.getType() + "_" + userThird.getOpenid());
                    Caches.getCache(CacheConfig.UID_USERTHIRD).remove(userThird.getType() + "_" + curr.getId());
                }
                request.setAttribute(RequestKey.IS_THIRD_LOGIN, true);
                request.setAttribute(RequestKey.THIRD_LOGIN_USER, exist);
                LoginController loginController = Routes.getController(LoginController.class);
                return loginController.login(exist.getLoginname(), null, null, request);
            }
        }
        return Resps.json(request, Resp.ok());
    }

    /**
     * @param request
     * @param phone
     * @param code
     * @param emailpwd
     * @param phonepwd
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年12月16日 下午6:11:21
     */
    @RequestPath(value = "/bindnewphone")
    public Resp bindnewphone(HttpRequest request, String phone, String code, String phonepwd, String emailpwd, Integer type) throws Exception {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail().msg("用户未登录");
        }
        if (User.dao.findFirst("select * from user where (loginname = ? or phone = ? or email = ?) and id != ?", phone, phone, phone, curr.getId()) != null) {
            return Resp.fail().msg("手机号已存在");
        }
        String oldPhone = curr.getPhone();
        Resp beforeCheck = SmsController.bizPhoneCheck(BaseSmsVo.BaseSmsBizType.BIND_NEW_PHONE, phone, request);
        if (!beforeCheck.isOk()) {
            return beforeCheck;
        }
        Ret ret = SmsService.me.checkCode(phone, BaseSmsVo.BaseSmsBizType.BIND_NEW_PHONE, code, null, false);
        if (ret.isFail()) {
            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        if (Objects.equals(curr.getEmailbindflag(), Const.YesOrNo.YES)) {
            if (StrUtil.isBlank(emailpwd)) {
                return Resp.fail().msg("请输入密码");
            }
            if (!emailpwd.equals(curr.getEmailpwd())) {
                return Resp.fail().msg("密码不正确");
            }
        }
        if (StrUtil.isBlank(phonepwd)) {
            phonepwd = "";
        }
        //修改用戶绑定手机号
        Resp resp = userService.bindNewPhone(curr, phone, phonepwd, emailpwd, type);
        if (resp.isOk()) {
            SmsService.me.delCode(phone, BaseSmsVo.BaseSmsBizType.BIND_NEW_PHONE);
            SmsService.me.delCode(oldPhone, BaseSmsVo.BaseSmsBizType.OLD_PHONE_CHECK);
        }
        return resp;
    }

    /**
     * @param request
     * @param phone
     * @param code
     * @param emailpwd
     * @param phonepwd
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年12月16日 下午4:37:14
     */
    @RequestPath(value = "/bindphone")
    public Resp bindphone(HttpRequest request, String phone, String code, String emailpwd, String phonepwd, Integer type) throws Exception {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail().msg("用户未登录");
        }
        if (User.dao.findFirst("select * from user where (loginname = ? or phone = ? or email = ?) and id != ?", phone, phone, phone, curr.getId()) != null) {
            return Resp.fail().msg("手机号已存在");
        }
        Resp beforeCheck = SmsController.bizPhoneCheck(BaseSmsVo.BaseSmsBizType.BIND_PHONE, phone, request);
        if (!beforeCheck.isOk()) {
            return beforeCheck;
        }
        Ret ret = SmsService.me.checkCode(phone, BaseSmsVo.BaseSmsBizType.BIND_PHONE, code, null, false);
        if (ret.isFail()) {
            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        if (Objects.equals(curr.getEmailbindflag(), Const.YesOrNo.YES)) {
            if (StrUtil.isBlank(emailpwd)) {
                return Resp.fail().msg("请输入密码");
            }
            if (!emailpwd.equals(curr.getEmailpwd())) {
                return Resp.fail().msg("密码不正确");
            }
        }
        if (StrUtil.isBlank(phonepwd)) {
            phonepwd = "";
        }
        if (type == null) {
            type = 1;
        }
        //修改用戶绑定手机号
        Resp resp = userService.bindPhone(curr, phone, phonepwd, emailpwd, type);
        if (resp.isOk()) {
            SmsService.me.delCode(phone, BaseSmsVo.BaseSmsBizType.BIND_PHONE);
        }
        return resp;
    }
    /*********************************************end - 登录注册相关***************************************************************/


    /*********************************************begin - 修改用户信息相关***************************************************************/
    /**
     * 修改昵称
     *
     * @param nick
     * @param request
     * @return
     * @throws Exception
     * @author tanyaowu
     */
    @RequestPath(value = "/updateNick")
    public Resp updateNick(HttpRequest request, String nick) throws Exception {
        User curr = WebUtils.currUser(request);
        if (StrUtil.isBlank(nick)) {
            return RetUtils.getInvalidResp();
        }
        if (curr == null) {
            return Resp.fail().msg("用户未登录");
        }
        //重置头像
        String path = "";
        if (Const.USE_AUTO_AVATAR) {
            if (curr.getAvatar().trim().indexOf("/user/base/avatar/") == 0 && !Objects.equals(nick.substring(0, 1), curr.getNick().substring(0, 1))) {
                path = AvatarUtils.pressUserAvatar(nick);
            }
        }
        Resp resp = userService.updateNick(curr, nick, path);
        User user = UserService.ME.getById(curr.getId());
        String avavar = path;
        //清空缓存
        Const.getBsExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (StrUtil.isNotBlank(avavar)) {
                        WxChatApi.synUserInfoToSelfAllInfo(curr.getId(), Const.UserToImSynType.USER_ALL, user);
                    } else {
                        WxChatApi.synUserInfoToSelfAllInfo(curr.getId(), Const.UserToImSynType.NICK, user);
                    }
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        });
        return resp;
    }

    /**
     * 修改头像
     *
     * @param request
     * @param uploadFile
     * @return
     * @throws Exception
     * @author tanyaowu
     */
    @RequestPath(value = "/updateAvatar")
    public Resp updateAvatar(HttpRequest request, UploadFile uploadFile) throws Exception {
        if (uploadFile == null) {
            return Resp.fail("上传信息为空");
        }
        User curr = WebUtils.currUser(request);
        Resp ret = null;
        if (curr == null) {
            ret = Resp.fail("您尚未登录或登录超时").code(AppCode.ForbidOper.NOTLOGIN);
        }
        byte[] imageBytes = uploadFile.getData();
        //		String extName = FileUtil.extName(uploadFile.getName());

        if (UserService.isSuper(curr) || UserRoleService.hasRole(curr, Role.UPLOAD_VIDEO)) {

        } else {
            int maxsize = ConfService.getInt("user.upload.avatar.maxsize", 512);

            if (imageBytes.length > 1024 * maxsize) {
                ret = Resp.fail("文件尺寸不能大于" + maxsize + "KB");
            }
        }

        Integer uid = null;
        if (curr != null) {
            uid = curr.getId();
        } else {
            uid = 1;
        }
        BufferedImage bi = ImgUtil.toImage(imageBytes);
        float scale = ImgUtils.calcScaleWithWidth(168, bi);
        Img img = ImgUtils.processImg(Const.UPLOAD_DIR.USER_AVATAR, uid, uploadFile, scale);

        img.setComefrom(Img.ComeFrom.MODIFY_AVATAR);

        img.setStatus((short) 1);
        img.setSession(request.getHttpSession().getId());

        boolean f = ImgService.me.save(img);

        if (ret != null) {
            return ret;
        }

        if (f) {
            Resp resp = userService.updateAvatar(curr, img.getCoverurl(), img.getUrl());
            User sendUser = UserService.ME.getById(curr.getId());
            Const.getBsExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        WxChatApi.synUserInfoToSelfAllInfo(curr.getId(), Const.UserToImSynType.AVATAR, sendUser);
                    } catch (Exception e) {
                        log.error("", e);
                    }
                }
            });
            return resp;
        } else {
            return Resp.fail("服务器异常");
        }
    }

    /**
     * 修改好友验证方式
     *
     * @param request
     * @param fdvalidtype
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年3月3日 下午5:22:55
     */
    @RequestPath(value = "/updatValid")
    public Resp updatValid(HttpRequest request, Short fdvalidtype) throws Exception {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail().msg("用户未登录");
        }
        return userService.updateFdvalidtype(curr, fdvalidtype);
    }


    /**
     * 消息提醒设置
     *
     * @param request
     * @param remindflag
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年3月3日 下午6:20:57
     */
    @RequestPath(value = "/updatRemind")
    public Resp updatRemind(HttpRequest request, Short remindflag) throws Exception {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail().msg("用户未登录");
        }
        return userService.updateRemind(curr, remindflag);
    }

    /**
     * 允许别人搜索设置
     *
     * @param request
     * @param searchflag
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年3月3日 下午6:24:29
     */
    @RequestPath(value = "/updatSearchFlag")
    public Resp updatSearchFlag(HttpRequest request, Short searchflag) throws Exception {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail().msg("用户未登录");
        }
        return userService.updateSearchFlag(curr, searchflag);
    }


    /**
     * 修改签名
     *
     * @param request
     * @param sign
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年3月3日 下午6:35:05
     */
    @RequestPath(value = "/updatSign")
    public Resp updatSign(HttpRequest request, String sign) throws Exception {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail().msg("用户未登录");
        }
        return userService.updateSign(curr, sign);
    }

    /**
     * 修改用户
     *
     * @param request
     * @param user
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年4月23日 上午11:06:17
     */
    @RequestPath(value = "/updatUser")
    public Resp updatUser(HttpRequest request, User user) throws Exception {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail().msg("用户未登录");
        }
        String nick = user.getNick();
        boolean isSyn = false;
        if (StrUtil.isNotBlank(nick) && !nick.equals(curr.getNick())) {
            //重置头像
            if (Const.USE_AUTO_AVATAR) {
                String path = "";
                if (curr.getAvatar().trim().indexOf("/user/base/avatar/") == 0 && !Objects.equals(nick.substring(0, 1), curr.getNick().substring(0, 1))) {
                    path = AvatarUtils.pressUserAvatar(nick);
                }
                user.setAvatar(path);
                user.setAvatarbig(path);
            }
            isSyn = true;
        }
        if (!Const.USE_AUTO_AVATAR && !Objects.equals(user.getSex(), curr.getSex())) {
            if (curr.getAvatar().trim().indexOf("/avatar/tio/") == 0) {
                String avatar = AvatarService.nextAvatar(user.getSex() + "");
                user.setAvatar(avatar);
                user.setAvatarbig(avatar);
                isSyn = true;
            }
        }
        if (!nick.equals(curr.getNick())) {
            Resp resp = CommonUtils.checkGroupName(nick, "昵称");
            if (!resp.isOk()) {
                return resp;
            }
        }
        if (StrUtil.isBlank(user.getAvatar())) {
            user.setAvatar(curr.getAvatar());
            user.setAvatarbig(curr.getAvatar());
        }
        Resp resp = userService.updateUser(curr, user);
        if (isSyn) {
            User sendUser = UserService.ME.getById(curr.getId());
            Const.getBsExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        WxChatApi.synUserInfoToSelfAllInfo(curr.getId(), Const.UserToImSynType.USER_ALL, sendUser);
                    } catch (Exception e) {
                        log.error("", e);
                    }
                }
            });
        }
        return resp;
    }

    /**
     * 修改性别
     *
     * @param request
     * @param sex
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年3月3日 下午6:35:35
     */
    @RequestPath(value = "/updatSex")
    public Resp updatSex(HttpRequest request, Short sex) throws Exception {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail().msg("用户未登录");
        }
        if (Objects.equals(sex, Const.UserSex.FEMALE) || Objects.equals(sex, Const.UserSex.MALE) || Objects.equals(sex, Const.UserSex.SECRET)) {
            return userService.updateSex(curr, sex);
        }
        return Resp.fail("无效性别");
    }
    /********************************************* end - 修改用户信息相关***************************************************************/

    /********************************************* begin - 废弃 ***************************************************************/

    /**
     * 修改手机号-老版本的修改附属信息的手机号，废除
     *
     * @param request
     * @param phone
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年3月3日 下午6:34:22
     */
    @Deprecated
    @RequestPath(value = "/updatPhone")
    public Resp updatPhone(HttpRequest request, String phone) throws Exception {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail().msg("用户未登录");
        }
        //修改用戶好友验证方式
        return userService.updatePhone(curr, phone);
    }


    /**
     * 手机注册绑定邮箱-废弃
     *
     * @param request
     * @param phone
     * @param code
     * @param emailpwd
     * @param phonepwd
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年12月24日 下午6:12:58
     */
    @Deprecated
    @RequestPath(value = "/regbindemail")
    public Resp regbindemail(HttpRequest request, String phone, String code, String email, String emailpwd, String phonepwd) throws Exception {
        if (StrUtil.isBlank(phonepwd) || StrUtil.isBlank(emailpwd)) {
            return Resp.fail().msg("密码不能为空");
        }
        Resp beforeCheck = SmsController.bizPhoneCheck(BaseSmsVo.BaseSmsBizType.REGISTER, phone, request);
        if (!beforeCheck.isOk()) {
            return beforeCheck;
        }
        Ret ret = SmsService.me.checkCode(phone, BaseSmsVo.BaseSmsBizType.REGISTER, code, null, false);
        if (ret.isFail()) {
            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        User old = userService.getByEmail(email, null);
        if (old == null) {
            return Resp.fail().msg("邮箱未注册");
        }
        if (!old.getEmailpwd().equals(emailpwd)) {
            return Resp.fail().msg("密码不正确");
        }
        //修改用戶绑定手机号
        Resp resp = userService.regbindemail(old, phone, phonepwd, emailpwd);
        if (resp.isOk()) {
            SmsService.me.delCode(phone, BaseSmsVo.BaseSmsBizType.REGISTER);
        }
        return resp;
    }

    /**
     * 重置用户头像
     *
     * @param request
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年1月15日 下午1:53:14
     */
    @Deprecated
    @RequestPath(value = "/resetAvator")
    public Resp resetAvator(HttpRequest request) throws Exception {
        List<Record> users = UserService.ME.getTortAvatarUser();
        int count = 0;
        for (Record user : users) {
            User update = new User();
            Integer id = user.getInt("id");
            update.setId(id);
            if (!Const.USE_AUTO_AVATAR) {
                String avatar = AvatarService.nextAvatar(user.getShort("sex") + "");
                update.setAvatar(avatar);
                update.setAvatarbig(avatar);
                update.update();
                count++;
                //				userService.initSynInfo(id, Const.UserToImSynType.AVATAR, avatar,null);
                UserService.ME.notifyClearCache(id);
                User sendUser = UserService.ME.getById(id);
                //清空缓存
                Const.getBsExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            WxChatApi.synUserInfoToSelfAllInfo(id, Const.UserToImSynType.AVATAR, sendUser);
                        } catch (Exception e) {
                            log.error("", e);
                        }
                    }
                });
            }
        }
        return Resp.ok("总共有用户：" + users.size() + ",成功处理：" + count);
    }
    /*********************************************  end - 废弃 ***************************************************************/
    /**
     * 获取直接下级
     *
     * @param request
     * @param parentInviteCode
     * @return
     * @throws Exception
     * @author xinji
     * 2020年3月3日 下午6:35:05
     */
    @RequestPath(value = "/getDirectUnderList")
    public Resp getDirectUnderList(HttpRequest request, String parentInviteCode) throws Exception {
        User curr = WebUtils.currUser(request);
        if (parentInviteCode == null || parentInviteCode.isEmpty()) {
            parentInviteCode = curr.getInvitecode();
        }
        if (curr == null) {
            return Resp.fail().msg("用户未登录");
        }
        return userService.getDirectUnderList(curr, parentInviteCode);
    }


    /**
     * 获取下级信息
     *
     * @param request
     * @param uid
     * @return
     * @throws Exception
     * @author xinji
     * 2020年3月3日 下午6:35:05
     */
    @RequestPath(value = "/getUnderUserInfo")
    public Resp getUnderUserInfo(HttpRequest request, Integer uid) throws Exception {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail().msg("用户未登录");
        }
        if (uid == null) {
            uid = curr.getId();
        }

        Record underUserInfo = userService.getUnderUserInfo(uid);
        underUserInfo.set("isOnline", !WxChatApi.isOutline(uid));
//		WxChatApi
        return Resp.ok(underUserInfo);
    }


    /**
     * 修改个人邀请码
     *
     * @param request
     * @param invitecode
     * @return
     * @throws Exception
     * @author xinji
     * 2020年3月3日 下午6:35:05
     */
    @RequestPath(value = "/updateInvitecode")
    public Resp updateInvitecode(HttpRequest request, String invitecode) throws Exception {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail().msg("用户未登录");
        }
        if (invitecode == null || invitecode.isEmpty()) {
            return Resp.fail("邀请码不能为空");
        }

        if (!invitecode.matches("[a-zA-Z0-9]+")) {
            return Resp.fail("邀请码由数字加字母组成");
        }
        List<User> users = User.dao.find("select * from user where parentinvitecode = ?", invitecode);
        if (users != null && users.size() > 0) {
            return Resp.fail("该邀请码已存在");
        }
        userService.updateInvitecode(curr, invitecode);
        return Resp.ok();
    }

    /**
     * 上传实名认证信息
     *
     * @param request
     * @param number
     * @param realName
     * @param idCardFront
     * @param idCardBehind
     * @return
     * @throws Exception
     * @author xinji
     * 2020年3月3日 下午6:35:05
     */
    @RequestPath(value = "/uploadRealCertification")
    public Resp uploadRealCertification(HttpRequest request, String number, String realName, String idCardFront, String idCardBehind) throws Exception {
        User curr = WebUtils.currUser(request);
        if (StrUtil.isBlank(number) || StrUtil.isBlank(realName) || StrUtil.isBlank(idCardFront) || StrUtil.isBlank(idCardBehind)) {
            return Resp.fail().msg("参数异常");
        }
        if (!isIDCard(number)) {
            return Resp.fail().msg("身份证号码格式错误");
        }
        if (!isChineseByReg(realName)) {
            return Resp.fail().msg("真实姓名只支持中文字符");
        }

        if (curr.getRealnameflag().equals((short) 1)) {
            return Resp.fail().msg("已实名，请勿重复提交");
        }
        RealNameCertification realNameCertification = RealNameCertification.dao.findFirst("select * from real_name_certification where uid = ?", curr.getId());
        if (realNameCertification != null && realNameCertification.getStatus().equals(0)) {
            return Resp.fail().msg("实名信息已提交，正在进行后台审核。请勿重复提交");
        }
        if (realNameCertification != null && realNameCertification.getStatus().equals(1)) {
            return Resp.fail().msg("已实名，请勿重复提交");
        }
        if (curr == null) {
            return Resp.fail().msg("用户未登录");
        }
        Boolean res = userService.uploadRealCertification(curr, number, realName, idCardFront, idCardBehind);
        return res ? Resp.ok() : Resp.fail().msg("上传失败，请联系客服");
    }

    /**
     * 获取实名信息审核状态
     *
     * @param request
     * @return
     * @throws Exception
     * @author xinji
     * 2020年3月3日 下午6:35:05
     */
    @RequestPath(value = "/verifyRealNameInfoStatus")
    public Resp verifyRealNameInfoStatus(HttpRequest request) throws Exception {
        User curr = WebUtils.currUser(request);
        RealNameCertification realNameCertification = RealNameCertification.dao.findFirst("select * from real_name_certification where uid = ?", curr.getId());
        Map data = new HashMap();
        if (realNameCertification == null) {
            data.put("status", -99);
            data.put("msg", "未上传实名信息");
            return Resp.ok().data(data);
        }
        data.put("status", realNameCertification.getStatus());
        data.put("mark", realNameCertification.getMark());
        if (realNameCertification.getStatus().equals(-1)) {
            data.put("msg", "实名审核已被拒绝");
            return Resp.ok().data(data);
        }
        if (realNameCertification.getStatus().equals(0)) {
            data.put("msg", "实名审核中");
            return Resp.ok().data(data);
        }
        return Resp.ok().data(data);
    }

    /**
     * 获取实名信息
     *
     * @param request
     * @return
     * @throws Exception
     * @author xinji
     * 2023年3月3日 下午6:35:05
     */
    @RequestPath(value = "/getRealInfo")
    public Resp getRealInfo(HttpRequest request) throws Exception {
        User curr = WebUtils.currUser(request);
        RealNameCertification realNameCertification = RealNameCertification.dao.findFirst("select * from real_name_certification where uid = ?", curr.getId());
//		if (realNameCertification == null) {
//			return Resp.fail().msg("未上传实名信息");
//		}
//		if (realNameCertification.getStatus().equals(0)) {
//			return Resp.fail().msg("信息审核中");
//		}
        Map data = new HashMap();
        data.put("uid", realNameCertification.getUid());
        data.put("idCardNo", realNameCertification.getIdCardNumber());
        data.put("realName", realNameCertification.getRealName());
        data.put("idCardFront", realNameCertification.getIdCardFront());
        data.put("idCardBehind", realNameCertification.getIdCardBehind());
        data.put("status", realNameCertification.getStatus());
        data.put("mark", realNameCertification.getMark());
        return Resp.ok().data(data);
    }

    /**
     * 签到
     *
     * @param request
     * @return
     * @throws Exception
     * @author xinji
     * 2023年9月5日 下午5:40:05
     */
    @RequestPath(value = "/sign")
    public Resp sign(HttpRequest request) throws Exception {
        User curr = WebUtils.currUser(request);
        if (!Objects.equals(curr.getOpenflag(), Const.YesOrNo.YES)) {
            return Resp.fail("用户未开户");
        }
        SignItem sign = userService.sign(curr);
        if (sign == null) {
            return Resp.fail("已签到");
        }
        return Resp.ok(sign);
    }

    /**
     * 签到
     *
     * @param request
     * @return
     * @throws Exception
     * @author xinji
     * 2023年9月5日 下午5:40:05
     */
    @RequestPath(value = "/signItem")
    public Resp signItem(HttpRequest request) throws Exception {
        User curr = WebUtils.currUser(request);
        if (!Objects.equals(curr.getOpenflag(), Const.YesOrNo.YES)) {
            return Resp.fail("用户未开户");
        }
        List<SignItem> signItems = SignItem.dao.find("select * from sign_item where uid = ? order by create_time desc", curr.getId());
        return Resp.ok(signItems);
    }

    /**
     * 获取签到任务配置
     *
     * @param request
     * @return
     * @author xinji
     * 2023.09.15
     */
    @RequestPath(value = "/getSignTask")
    public Resp getSignTask(HttpRequest request) {
        List<SignTask> all = SignTask.dao.findAll();
        return Resp.ok(all);
    }

    /**
     * 公告列表
     *
     * @param request
     * @return
     * @throws Exception
     * @author xinji
     * 2023年9月11日 下午4:17:05
     */
    @RequestPath(value = "/noticeList")
    public Resp noticeList(HttpRequest request) throws Exception {
        List<Notice> notices = Notice.dao.find("select * from notice order by release_time desc");
        return Resp.ok(notices);
    }

    /**
     * 查看签到规则
     *
     * @param request
     * @return
     * @author xinji
     * 2023.09.21
     */
    @RequestPath(value = "/getSignRole")
    public Resp getSignRole(HttpRequest request) {
        SignRole signRole = SignRole.dao.findFirst("select * from sign_role");
        return Resp.ok(signRole);
    }


    /**
     *
     * 收藏API
     *
     */

    /**
     * 添加收藏
     *
     * @param request
     * @param fromId
     * @param fromType
     * @param category 内容种类 1、普通文本消息，2、超链接卡片消息，3、文件，4、音频，5、视频 ,6.图片 ,9.群名片 ,12.红包 ,88.链接
     * @param content
     * @return
     * @throws Exception
     * @author xin ji
     * 2023年10月10日 下午5:02:16
     */
    @RequestPath(value = "/addCollect")
    public Resp addCollect(HttpRequest request, Integer fromType, Integer fromId, Integer category, String content, String title, String imgUrl, String text) throws Exception {
        if (content == null) {
            return Resp.fail("内容不能为空");
        }
        if (fromType == null) {
            return Resp.fail("收藏来源类型不能为空");
        }
        if (fromId == null) {
            return Resp.fail("收藏来源id不能为空");
        }
        if (category == null) {
            return Resp.fail("收藏分类不能为空");
        }

        if (fromType.equals(1)) {
            User fromUser = User.dao.findById(fromId);
            if (fromUser == null) {
                return Resp.fail("信息来源好友不存在");
            }
            title = "来自好友 " + fromUser.getNick();
        } else if (fromType.equals(2)) {
            WxGroup fromGroup = WxGroup.dao.findById(fromId);
            if (fromGroup == null) {
                return Resp.fail("信息来源群组不存在");
            }
            title = "来自群组 " + fromGroup.getName();
        }


        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail("请登录");
        }
        Collect collect = new Collect();

        collect.setUid(curr.getId());
        collect.setTitle(title);
        collect.setFromType(fromType);
        collect.setFromId(fromId);
        collect.setCategory(category);
        collect.setContent(content);
        if (category.equals(11)) {
            collect.setText(text);
            collect.setImgUrl(imgUrl);
        }
        collect.setCreateTime(new Date());
        collect.save();
        return Resp.ok();
    }

    /**
     * 更新笔记
     *
     * @param request
     * @param collectId
     * @param content
     * @param title
     * @return
     * @throws Exception
     * @author xin ji
     * 2024年2月20日
     */
    @RequestPath(value = "/updateCollect")
    public Resp updateCollect(HttpRequest request, Integer collectId, String content, String title, String imgUrl, String text) throws Exception {
        if (content == null) {
            return Resp.fail("内容不能为空");
        }
        if (collectId == null) {
            return Resp.fail("更新内容的id不能为空");
        }

        if (title == null) {
            return Resp.fail("标题不能为空");
        }


        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail("请登录");
        }
        Collect collect = Collect.dao.findById(collectId);
        if (collect == null) {
            return Resp.fail("该笔记不存在");
        }
        if (!collect.getCategory().equals(11)) {
            return Resp.fail("非笔记内容不可修改");
        }
        if (!collect.getUid().equals(curr.getId())) {
            return Resp.fail("只有笔记发布者可修改该内容");
        }
        collect.setTitle(title);
        collect.setContent(content);
        collect.setImgUrl(imgUrl);
        collect.setText(text);
        collect.update();
        return Resp.ok();
    }

    /**
     * 查询收藏列表
     *
     * @param request
     * @param category
     * @param pageNumber
     * @param pageSize
     * @return
     * @throws Exception
     * @author xin ji
     * 2023年11月9日 下午5:02:16
     */
    @RequestPath(value = "/collectList")
    public Resp collectList(HttpRequest request, Integer category, Integer pageNumber, Integer pageSize) throws Exception {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail().msg("请登录");
        }
        Page<Record> collectList = userService.collectList(curr.getId(), category, pageNumber, pageSize);
        return Resp.ok(collectList);
    }


    /**
     * 查看收藏详情
     *
     * @param request
     * @return
     * @throws Exception
     * @author xin ji
     * 2023年11月9日 下午5:02:16
     */
    @RequestPath(value = "/collectInfo")
    public Resp collectInfo(HttpRequest request, Integer cid) throws Exception {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail().msg("请登录");
        }
        Collect collect = Collect.dao.findById(cid);
        if (collect == null) {
            return Resp.fail().msg("该记录不存在");
        }
        return Resp.ok(collect);
    }

    /**
     * 删除收藏
     *
     * @param request
     * @param cid
     * @return
     * @throws Exception
     * @author xin ji
     * 2023年11月19日 下午5:12:16
     */
    @RequestPath(value = "/delCollect")
    public Resp delCollect(HttpRequest request, Integer cid) throws Exception {
        User curr = WebUtils.currUser(request);
        Boolean success = userService.delCollect(curr, cid);
        if (!success) {
            return Resp.fail("删除失败");
        }
        return Resp.ok();
    }

/**
 *
 * 朋友圈API
 *
 */

    /**
     * 新增朋友圈
     *
     * @param videoUrl
     * @param imgUrl
     * @param content
     * @return
     * @throws Exception
     * @author xin ji
     * 2023年11月15日 下午2:02:16
     */
    @RequestPath(value = "/addMoments")
    public Resp addMoments(HttpRequest request, String content, Short authflag, String videoUrl, String imgUrl) throws Exception {
        User user = WebUtils.currUser(request);
        if ((content == null || content.isEmpty()) &&
                (videoUrl == null || videoUrl.isEmpty()) &&
                (imgUrl == null || imgUrl.isEmpty())) {
            return Resp.fail("请输入朋友圈内容");
        }
        if ((videoUrl != null && !videoUrl.isEmpty()) &&
                (imgUrl != null && !imgUrl.isEmpty())) {
            return Resp.fail("发表的朋友圈内容不能同时包含视频和图片");
        }
        if (imgUrl != null && imgUrl.split(",").length > 9) {
            return Resp.fail("最多只能上传 9 张图片");
        }
        if (videoUrl != null && videoUrl.split(",").length > 1) {
            return Resp.fail("最多只能上传 1 个视频");
        }
        if (authflag == null) {
            return Resp.fail("需要设置权限标识");
        }
        Boolean success = userService.addMoments(user, content, authflag, videoUrl, imgUrl);
        if (success) {
            WxChatApi.sendFriendMomentsMsg(request, user);
            return Resp.ok();
        }
        return Resp.fail("朋友圈上传失败");
    }

    /**
     * 查询朋友圈列表
     *
     * @param request
     * @param pageNumber
     * @param pageSize
     * @return
     * @throws Exception
     * @author xin ji
     * 2023年11月16日 上午11:02:16
     */
    @RequestPath(value = "/momentsList")
    public Resp momentsList(HttpRequest request, Integer pageNumber, Integer pageSize) throws Exception {
        User curr = WebUtils.currUser(request);
        if (pageNumber == null) {
            pageNumber = 1;
        }
        if (pageNumber < 1) {
            pageNumber = 1;
        }
        if (pageSize == null) {
            pageSize = 5;
        }
        List<Moments> momentsList = userService.momentsList(curr.getId(), pageNumber, pageSize);
        userService.readFlagUpdate(curr.getId(), 1);
        return Resp.ok(momentsList);
    }

    /**
     * 获取好友/自己的朋友圈列表
     *
     * @param request
     * @param uid
     * @param pageNumber
     * @param pageSize
     * @return
     * @throws Exception
     * @author xin ji
     * 2023年11月16日 上午11:02:16
     */
    @RequestPath(value = "/momentsListByUid")
    public Resp momentsListByUid(HttpRequest request, Integer uid, Integer pageNumber, Integer pageSize) throws Exception {
        User curr = WebUtils.currUser(request);
        Ret ret = FriendService.me.isFriend(curr, uid);
        if (ret.get("data").equals(Const.YesOrNo.NO)) {
            return Resp.fail().msg("查询对象不是好友，无法查询");
        }
        if (pageNumber == null) {
            pageNumber = 1;
        }
        if (pageNumber < 1) {
            pageNumber = 1;
        }
        if (pageSize == null) {
            pageSize = 5;
        }
        List<Moments> momentsList = userService.momentsListByUid(curr.getId(), uid, pageNumber, pageSize);
//		userService.readFlagUpdate(curr.getId(), 1);
        return Resp.ok(momentsList);
    }

    /**
     * 删除朋友圈
     *
     * @param request
     * @param mid
     * @return
     * @throws Exception
     * @author xin ji
     * 2023年11月15日 下午3:12:16
     */
    @RequestPath(value = "/delMoments")
    public Resp delMoments(HttpRequest request, Integer mid) throws Exception {
        User curr = WebUtils.currUser(request);
        Boolean success = userService.delMoments(curr, mid);
        if (!success) {
            return Resp.fail("删除失败");
        }
        return Resp.ok();
    }

    /**
     * 查询指定朋友圈
     *
     * @param request
     * @param mid
     * @return
     * @throws Exception
     * @author xin ji
     * 2023年11月15日 下午3:12:16
     */
    @RequestPath(value = "/getMomentById")
    public Resp getMomentById(HttpRequest request, Integer mid) throws Exception {
        User curr = WebUtils.currUser(request);
        Moments moment = userService.getMomentById(curr, mid);

        return Resp.ok(moment);
    }

    /**
     * 新增朋友圈评论
     *
     * @param mid
     * @param pid
     * @param content
     * @return
     * @throws Exception
     * @author xin ji
     * 2023年11月15日 下午5:02:16
     */
    @RequestPath(value = "/addComments")
    public Resp addComments(HttpRequest request, Integer mid, Integer pid, String content) throws Exception {
        User user = WebUtils.currUser(request);
        Moments moments = Moments.dao.findById(mid);
        if (moments == null) {
            return Resp.fail("该条朋友圈不存在");
        }
        if (content == null || content.isEmpty()) {
            return Resp.fail("评论内容不能为空");
        }
        Ret friend = FriendService.me.isFriend(user, moments.getUid());
        if (friend.isFail()) {
            return Resp.fail("非好友不允许评论");
        }
        if (pid == null) {
            pid = 0;
        }
        MomentsComments momentsComments = new MomentsComments();
        if (!pid.equals(0)) {
            momentsComments = MomentsComments.dao.findById(pid);
            if (momentsComments == null) {
                return Resp.fail("该条评论不存在或已删除，无法对该评论进行回复");
            }
            Ret friend1 = FriendService.me.isFriend(user, momentsComments.getUid());
            if (friend1.isFail()) {
                return Resp.fail("和该评论者不是好友，不能在该评论下发表评论");
            }
        }
        MomentsComments comments = userService.addComments(user, mid, pid, content);
        if (comments == null) {
            return Resp.fail("评论失败");
        }
        if (!pid.equals(0)) {
            WxChatApi.sendNewCommentMsg(request, user, comments, momentsComments.getUid());
        } else {
            WxChatApi.sendNewCommentMsg(request, user, comments, moments.getUid());

        }
//		MomentsComments.dao.findFirst("select * from moments_comments where uid=? and mid = ? and")
        return Resp.ok(comments);
    }

    /**
     * 删除朋友圈评论
     *
     * @param request
     * @param cid
     * @return
     * @throws Exception
     * @author xin ji
     * 2023年11月16日 上午9:12:16
     */
    @RequestPath(value = "/delComments")
    public Resp delComments(HttpRequest request, Integer cid) throws Exception {
        User curr = WebUtils.currUser(request);
        MomentsComments comments = MomentsComments.dao.findById(cid);
        Moments moments = Moments.dao.findById(comments.getMid());
        if (moments == null) {
            return Resp.fail("该朋友圈不存在");
        }
        Integer mUid = moments.getUid();
        if (!curr.getId().equals(comments.getUid()) && !curr.getId().equals(mUid)) {
            return Resp.fail("您没有权限进行该操作");
        }
        Boolean success = userService.delComments(cid);
        if (!success) {
            return Resp.fail("删除失败");
        }
        return Resp.ok();
    }

    /**
     * 朋友圈点赞
     *
     * @param mid
     * @return
     * @throws Exception
     * @author xin ji
     * 2023年11月16日 上午9:42:16
     */
    @RequestPath(value = "/momentLikes")
    public Resp momentLikes(HttpRequest request, Integer mid) throws Exception {
        User user = WebUtils.currUser(request);
        Moments moments = Moments.dao.findById(mid);
        if (moments == null) {
            return Resp.fail("该条朋友圈不存在");
        }
        Ret friend = FriendService.me.isFriend(user, moments.getUid());
        if (friend.isFail()) {
            return Resp.fail("非好友不允许点赞");
        }
        boolean isLike = userService.checkLike(mid, user.getId());
        if (!isLike) {
            return Resp.fail("不可以重复点赞");
        }
        MomentsLikes likes = userService.likes(user, mid);
        if (likes == null) {
            return Resp.fail("点赞失败");
        }
        WxChatApi.sendNewLikesMsg(request, user, moments.getUid(), likes);
        return Resp.ok(likes);
    }

    /**
     * 取消朋友圈点赞
     *
     * @param request
     * @param likesId
     * @return
     * @throws Exception
     * @author xin ji
     * 2023年11月16日 上午10:33:16
     */
    @RequestPath(value = "/cancelLikes")
    public Resp cancelLikes(HttpRequest request, Integer likesId) throws Exception {
        User curr = WebUtils.currUser(request);
        MomentsLikes likes = MomentsLikes.dao.findById(likesId);
        if (likes == null) {
            return Resp.fail("点赞信息不存在，取消失败");
        }
        if (!curr.getId().equals(likes.getUid())) {
            return Resp.fail("您没有权限进行该操作");
        }
        Boolean success = userService.cancelLikes(likesId);
        if (!success) {
            return Resp.fail("取消失败");
        }
        return Resp.ok();
    }

    /**
     * 上传朋友圈资源
     *
     * @param request
     * @param files
     * @param type
     * @return
     * @throws Exception
     * @author xin ji
     * 2023年11月22日 15:31:12
     */
    @RequestPath(value = "/uploadFiles")
    public Resp uploadFiles(HttpRequest request, UploadFile[] files, Integer type) throws Exception {
        if (files == null || files.length == 0) {
            return Resp.fail("参数异常");
        }

        StringBuilder filePaths = new StringBuilder();

        String basePath;
        if (type.equals(1)) {
            basePath = "moments/img";
        } else {
            basePath = "moments/video";
        }

        for (UploadFile file : files) {
            byte[] bs = file.getData();
            String filename = file.getName();
            String extName = FileUtil.extName(filename).toLowerCase();
            if (StrUtil.isBlank(extName)) {
                extName = "bin"; // 默认格式
            }

            try {
                // 构建 objectKey
                String objectKey = UploadUtils.dateFile(basePath) + "." + extName;

                // 构建 Content-Type
                String contentType;
                switch (extName) {
                    case "jpg":
                    case "jpeg":
                        contentType = "image/jpeg";
                        break;
                    case "png":
                        contentType = "image/png";
                        break;
                    case "mp4":
                        contentType = "video/mp4";
                        break;
                    default:
                        contentType = "application/octet-stream";
                }

                // 上传文件到 R2
                InputStream inputStream = new ByteArrayInputStream(bs);
                UploadUtils.unificationUpload( objectKey, inputStream, bs.length, contentType);
//                CloudflareR2Utils.uploadFilePublic(
//                        Const.CloudflareR2.R2_BUCKET_NAME,
//                        objectKey,
//                        inputStream,
//                        bs.length,
//                        contentType
//                );

                // 拼接返回路径
                filePaths.append("/").append(objectKey).append(",");
            } catch (Exception e) {
                log.error("文件上传失败", e);
                return Resp.fail("文件上传失败：" + e.getMessage());
            }
        }

        // 去掉末尾多余的逗号
        if (filePaths.length() > 0) {
            filePaths.deleteCharAt(filePaths.length() - 1);
        }

        return Resp.ok(filePaths.toString());
    }

    /**
     * @param request
     * @param file
     * @param type    1：图片 2：视频
     * @return
     */
    @RequestPath(value = "/uploadFile")
    public Resp uploadFile(HttpRequest request, UploadFile file, Integer type) {
        User user = WebUtils.currUser(request);
        Integer uid = 1;
        if (user != null) {
            uid = user.getId();
        }

        if (file == null) {
            return Resp.fail("参数异常");
        }

        byte[] bs = file.getData();
        String filename = file.getName();
        String extName = FileUtil.extName(filename).toLowerCase(); // 统一转小写处理

        try {
            String basePath;
            if (type.equals(1)) {
                basePath = "comment/img";
            } else if (type.equals(2)) {
                basePath = "comment/video";
            } else {
                return Resp.fail("参数异常, 类型1为图片，类型2为视频");
            }

            String objectKey = UploadUtils.dateFile(basePath) + "." + extName;

            // 构建 Content-Type
            String contentType;
            switch (extName) {
                case "jpg":
                case "jpeg":
                    contentType = "image/jpeg";
                    break;
                case "png":
                    contentType = "image/png";
                    break;
                case "mp4":
                    contentType = "video/mp4";
                    break;
                default:
                    contentType = "application/octet-stream";
            }

            // 上传主文件到 R2
            InputStream inputStream = new ByteArrayInputStream(bs);
            UploadUtils.unificationUpload( objectKey, inputStream, bs.length, contentType);
//            CloudflareR2Utils.uploadFilePublic(
//                    Const.CloudflareR2.R2_BUCKET_NAME,
//                    objectKey,
//                    inputStream,
//                    bs.length,
//                    contentType
//            );

            // 如果是视频，生成并上传封面图
            if (type.equals(2)) {
                String coverExt = "jpg";
                String coverObjectKey = objectKey + "_cover." + coverExt;

                // 提取视频封面图
                BufferedImage coverImage = VideoUtils.generateCoverFromVideo(new ByteArrayInputStream(bs));

                // 压缩封面图
                byte[] coverBytes = ImgUtils.compressImage(coverImage, 1f, 0.6d, coverExt);
                UploadUtils.unificationUpload( objectKey, inputStream, bs.length, contentType);
                // 上传封面图到 R2
//                CloudflareR2Utils.uploadFilePublic(
//                        Const.CloudflareR2.R2_BUCKET_NAME,
//                        coverObjectKey,
//                        new ByteArrayInputStream(coverBytes),
//                        coverBytes.length,
//                        "image/" + coverExt
//                );
            }

            return Resp.ok(objectKey); // 只返回相对路径，前端拼接 base_url 获取完整 URL

        } catch (Exception e) {
            log.error("文件上传到 R2 异常", e);
            return Resp.fail().code(500).msg("文件上传失败");
        }
    }

    /**
     * 获取最新一条朋友圈推送消息
     *
     * @param request
     * @return
     */
    @RequestPath(value = "/getMomentsReadFlag")
    public Resp getMomentsReadFlag(HttpRequest request) {
        User curr = WebUtils.currUser(request);
        WxFriendMsg wxFriendMsg = WxFriendMsg.dao.findFirst("select * from wx_friend_msg where touid = ? and contenttype = 15 order by createtime desc", curr.getId());
        HashMap<String, Object> result = new HashMap<>();
        if (wxFriendMsg == null) {
            return Resp.ok();
        }
        User fromUser = User.dao.findById(wxFriendMsg.getUid());
        if (fromUser != null) {
            result.put("msg", wxFriendMsg);
            result.put("fromUserUid", fromUser.getId());
            result.put("fromUserAvatar", fromUser.getAvatar());
            return Resp.ok(result);
        }
        return Resp.ok(wxFriendMsg);
    }

    /**
     * 获取朋友圈消息列表
     *
     * @param request
     * @param pageNumber
     * @param pageSize
     * @return
     */
    @RequestPath(value = "/getMomentsMsgList")
    public Resp getMomentsMsgList(HttpRequest request, Integer pageNumber, Integer pageSize) {
        User curr = WebUtils.currUser(request);
        if (pageNumber == null) {
            pageNumber = 1;
        }
        if (pageNumber < 1) {
            pageNumber = 1;
        }
        if (pageSize == null) {
            pageSize = 5;
        }

        List<WxMomentMsgVo> result = UserService.getMomentsMsgList(curr, pageNumber, pageSize);
//		WxFriendMsg wxFriendMsg = WxFriendMsg.dao.findFirst("select * from wx_friend_msg where touid = ? and contenttype = 15 order by createtime desc", curr.getId());

        return Resp.ok(result);
    }

    @RequestPath(value = "/readMsg")
    public Resp readMsg(HttpRequest request) {
        User curr = WebUtils.currUser(request);
        userService.readFlagUpdate(curr.getId(), 2);
        return Resp.ok();
    }

    /**
     * 清空朋友圈消息列表
     *
     * @param request
     * @return
     */
    @RequestPath(value = "/clearMomentsMsgs")
    public Resp clearMomentsMsgs(HttpRequest request) {
        User curr = WebUtils.currUser(request);

        UserService.clearMomentsMsgs(curr);
//		WxFriendMsg wxFriendMsg = WxFriendMsg.dao.findFirst("select * from wx_friend_msg where touid = ? and contenttype = 15 order by createtime desc", curr.getId());

        return Resp.ok();
    }

    /**
     * 评论和点赞未读数
     *
     * @param request
     * @return
     */
    @RequestPath(value = "/unreadCount")
    public Resp unreadCount(HttpRequest request) {
        User curr = WebUtils.currUser(request);

        int count = UserService.unreadCount(curr);
//		WxFriendMsg wxFriendMsg = WxFriendMsg.dao.findFirst("select * from wx_friend_msg where touid = ? and contenttype = 15 order by createtime desc", curr.getId());
        Map<String, Integer> result = new HashMap<>();
        result.put("unreadCount", count);
        return Resp.ok(result);
    }

    /**
     * 生成临时聊天
     *
     * @param request
     * @return
     */
    @RequestPath(value = "/genTempIM")
    public Resp genTempIM(HttpRequest request) throws Exception {
        Conf conf = Conf.dao.findFirst("select * from conf where name = 'temp.im.link'");
        if (conf == null) {
            return Resp.fail("后台暂未设置临时聊天登录链接，请联系管理员");
        }
        User currUser = WebUtils.currUser(request);
        User tempUser = WxChatApi.getTempUser();
        if (tempUser == null) {
            return Resp.fail("临时聊天室暂无空闲，请稍后");
        }
        WxGroup wxGroup = new WxGroup();
        wxGroup.setName("临时聊天室_" + (int) (Math.random() * 10000));
        wxGroup = createGroup(request, wxGroup, tempUser.getId().toString());
        ICache cache = Caches.getCache(CacheConfig.TEMP_IM);
        String key = "tempIM" + "_" + tempUser.getId();
        WxGroup finalWxGroup = wxGroup;
        WxChatItems wxChatItems = WxChatItems.dao.findFirst("select * from wx_chat_items where uid = ? and bizid = ?", tempUser.getId(), finalWxGroup.getId());
        Map<String, Object> result = CacheUtils.get(cache, key, true, new FirsthandCreater<HashMap<String, Object>>() {
            @Override
            public HashMap<String, Object> create() {
                HashMap<String, Object> res = new HashMap<>();
                res.put("uid", tempUser.getId());
                res.put("verify_code", key);
                res.put("group_id", finalWxGroup.getId());
                if (wxChatItems == null) {
                    WxChatItems wxChatItems1 = WxChatItems.dao.findFirst("select * from wx_chat_items where uid = ? and bizid = ?", tempUser.getId(), finalWxGroup.getId());
                    res.put("chatlinkid", "-" + wxChatItems1.getBizid());
                    res.put("bizid", wxChatItems1.getBizid());
                } else {
                    res.put("chatlinkid", "-" + wxChatItems.getBizid());
                    res.put("bizid", wxChatItems.getBizid());
                }
                return res;
            }
        });
        if (result == null) {
            return Resp.fail("临时会话生成失败");
        }


        Map<String, Object> data = new HashMap<>();
        data.put("isGroup", true);
        data.put("type", "temporary");
        data.put("verifyCode", key);
        data.put("chatOn", result.get("chatlinkid"));
        data.put("bizid", result.get("bizid"));
        Gson gson = new Gson();
        String url = gson.toJson(data);
        String encryptUrl = Encrypt(url);
        String loginLink = conf.getValue() + "data=" + encryptUrl.replaceAll("\\+", "%2B");

        TempUserKey tempUserKey = new TempUserKey();
        tempUserKey.setName(wxGroup.getName());
        tempUserKey.setLoginLink(loginLink);
        tempUserKey.setKey(key);
        tempUserKey.setCreateUid(currUser.getId());
        tempUserKey.setMemberUid(tempUser.getId());
        tempUserKey.setGroupId(finalWxGroup.getId());
        tempUserKey.setCreateTime(new Date());
        tempUserKey.save();
        TEMP_USER_REQUEST.put("create_" + key, request);

        return Resp.ok(result);
    }

    /**
     * 临时聊天列表
     *
     * @param request
     * @return
     * @throws Exception
     */
    @RequestPath(value = "/tempIMList")
    public Resp tempIMList(HttpRequest request) throws Exception {
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail("请先登录");
        }
        List<TempUserKey> tempUserKeys = TempUserKey.dao.find("select * from temp_user_key where create_uid = ?", user.getId());
        return Resp.ok(tempUserKeys);
    }

    /**
     * 临时聊天重命名
     *
     * @param request
     * @param tempKey
     * @param name
     * @return
     * @throws Exception
     */
    @RequestPath(value = "/tempIMRename")
    public Resp tempIMRename(HttpRequest request, String tempKey, String name) throws Exception {
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail("请先登录");
        }
        TempUserKey tempUserKey = TempUserKey.dao.findFirst("select * from temp_user_key where create_uid = ? and temp_key = ?", user.getId(), tempKey);
        if (tempUserKey == null) {
            return Resp.fail().msg("临时会话不存在");
        }
        tempUserKey.setName(name);
        tempUserKey.update();

        WxGroup wxGroup = WxGroup.dao.findById(tempUserKey.getGroupId());
        if (wxGroup == null) {
            return Resp.fail().msg("临时会话已解散");
        }
        modifyName(request, wxGroup.getId(), name);
        return Resp.ok(tempUserKey);
    }


    public Resp modifyName(HttpRequest request, Long groupid, String name) {
        User curr = WebUtils.currUser(request);
        name = EscapeUtil.escapeHtml4(name);
        Ret ret = GroupService.me.modifyName(curr.getId(), groupid, name);
        if (ret.isFail()) {

            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        ClientConf clientConf = ClientConf.dao.findFirst("select * from client_conf where name = ?", "isOpenUpdateGroupNameNotify");
        //消息触发
        if (clientConf.getValue().equals(1)) {
            String sendName = name;
            Const.getBsExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Ret ret = WxChatApi.updateGroupInfo(request, curr, groupid, sendName);
                        if (ret.isFail()) {
                            log.error(RetUtils.getRetMsg(ret));
                        }
                    } catch (Exception e) {
                        log.error("", e);
                    }
                }
            });
        }
        return Resp.ok(RetUtils.OPER_RIGHT);
    }

    /**
     * 解散临时聊天
     *
     * @param request
     * @return
     */
    @RequestPath(value = "/dismissTempIM")
    public Resp dismissTempIM(HttpRequest request, String verifyCode) throws Exception {
        ICache cache = Caches.getCache(CacheConfig.TEMP_IM);
        Map<String, Object> result = CacheUtils.get(cache, verifyCode, false, new FirsthandCreater<HashMap<String, Object>>() {
            @Override
            public HashMap<String, Object> create() {
                return null;
            }
        });
        if (result == null) {
            return Resp.fail("当前会话已失效");
        }
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail("请登录");
        }
        WxGroup group = WxGroup.dao.findById(result.get("group_id"));
        if (group != null) {
            if (!Objects.equals(curr.getId(), group.getUid())) {
                return Resp.fail(RetUtils.LOGIN_ERROR);
            }
            Ret ret = groupService.delGroup(curr, Long.valueOf(result.get("group_id").toString()));
            if (ret.isFail()) {
                return Resp.fail().msg(RetUtils.getRetMsg(ret));
            }
            //消息处理
            Const.getBsExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        WxChatGroupItem groupItem = RetUtils.getOkTData(ret, "groupitem");
                        WxGroup group = RetUtils.getOkTData(ret, "group");
                        WxGroupUser groupUser = RetUtils.getOkTData(ret, "groupuser");
                        WxChatApi.delGroup(request, curr, groupItem, group, groupUser);
                    } catch (Exception e) {
                        log.error("", e);
                    }
                }
            });
            //统计处理
            Const.getBsExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        WxGroup group = RetUtils.getOkTData(ret, "group");
                        String dayperiod = PeriodUtils.dateToPeriodByType(group.getCreatetime(), Const.PeriodType.DAY);
                        GroupStat groupStat = GroupStat.dao.findFirst("select * from group_stat where dayperiod = ? and type = ?", dayperiod, Const.Status.DELETE);
                        if (groupStat == null) {
                            groupStat = new GroupStat();
                            groupStat.setAddcount(1);
                            groupStat.setDayperiod(dayperiod);
                            groupStat.setType(Const.Status.DELETE);
                            groupStat.ignoreSave();
                        } else {
                            Db.use(Const.Db.TIO_SITE_STAT).update("update group_stat set addcount = addcount + 1 where dayperiod = ? and type = ?", dayperiod, Const.Status.DELETE);
                        }
                        Db.use(Const.Db.TIO_SITE_STAT).update("update group_stat set addcount = addcount - 1 where dayperiod = ? and type = ?", dayperiod, Const.Status.NORMAL);
                    } catch (Exception e) {
                        log.error("", e);
                    }
                }
            });
        }

        WxChatApi.useSysChatNtf(request, Integer.valueOf(result.get("uid").toString()), Const.WxSysCode.LOGOUT, "退出登录", null);
        if (TEMP_USER_REQUEST.get(verifyCode) != null) {
            tempUserLogout(TEMP_USER_REQUEST.get(verifyCode));
            TEMP_USER_REQUEST.remove(verifyCode);
            TEMP_USER_REQUEST.remove("create_" + verifyCode);
        }
        TempUserKey tempUserKey = TempUserKey.dao.findFirst("select * from temp_user_key where temp_key = ?", verifyCode);
        if (tempUserKey != null) {
            tempUserKey.delete();
        }
        cache.remove(verifyCode);

        return Resp.ok("解散成功");
    }

    public static Resp tempUserLogout(HttpRequest request) {
        HttpConfig httpConfig = request.getHttpConfig();
        HttpSession httpSession = request.getHttpSession();
        String sessionId = httpSession.getId();
        User user = WebUtils.currUser(request);
        if (user != null) {

            SessionExt sessionExt = httpSession.getAttribute(SessionKey.SESSION_EXT, SessionExt.class);
            sessionExt.setUid(null);
            sessionExt.setLoginTime(null);
            sessionExt.setKickedInfo(null);
            httpSession.update(httpConfig);//切记：每次修改SessionExt后，要调用一下update把数据更新到分布式缓存中
            RequestExt requestExt = WebUtils.getRequestExt(request);
            WxChatQueueApi.leaveFocusQueue(user, requestExt.getAppDevice(), "");
            //			//离线
            //			SynService.me.outline(user.getId(), requestExt.getDeviceType());
            //极光推送
            if (Const.JPushConfig.OPENFLAG && Objects.equals(requestExt.getAppDevice(), Devicetype.APP.getValue())) {
                Db.use(Const.Db.TIO_SITE_MAIN).update("delete from wx_jpush_user WHERE uid = ?", user.getId());
            }
            int c = UserTokenService.me.delete(requestExt.getAppDevice(), user.getId(), sessionId);
            if (c <= 0) {
                log.warn("can find usertoken by devicetype【{}】 and uid【{}】 and token:【{}】", requestExt.getAppDevice(), user.getId(), sessionId);

                UserToken userToken = UserTokenService.me.find(requestExt.getAppDevice(), user.getId());
                if (userToken != null) {
                    String tokenInDb = userToken.getToken();
                    HttpSession otherHttpSession = httpConfig.getHttpSession(tokenInDb);
                    if (otherHttpSession != null) {
                        Integer useridInOtherSession = WebUtils.getUserIdBySession(otherHttpSession);
                        if (useridInOtherSession != null) {
                            SessionExt sessionExtInOtherSession = otherHttpSession.getAttribute(SessionKey.SESSION_EXT, SessionExt.class);
                            if (sessionExtInOtherSession != null) {
                                sessionExtInOtherSession.setUid(null);
                                otherHttpSession.update(httpConfig);
                            }
                        }
                    }

                }
            }

            Resp resp = Resp.ok();
            return resp;
        } else {
            Resp resp = Resp.fail("你并未登录");
            return resp;
        }
    }

    public static boolean isIDCard(String idCard) {
        String regex = "^\\d{17}[\\d|xX]$";
        return idCard.matches(regex);
    }

    public static boolean isChineseByReg(String str) {
        String reg = "[\\u4e00-\\u9fa5]";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
    }

    public WxGroup createGroup(HttpRequest request, WxGroup wxGroup, String uidList) throws Exception {
        String name = "";
        if (cn.hutool.core.util.StrUtil.isNotBlank(wxGroup.getName())) {
            name = wxGroup.getName();
            name = name.length() > 30 ? name.substring(0, 30) : name;
            wxGroup.setName(name);
            wxGroup.setAutoflag(Const.YesOrNo.NO);
        }
        String[] uidArr = cn.hutool.core.util.StrUtil.splitToArray(uidList, ",");
        User curr = WebUtils.currUser(request);
//		if (Objects.equals(curr.getOpenflag(), Const.YesOrNo.NO)) {
//			return Resp.fail("用户未实名");
//		}
//		RealNameCertification realNameCertification = RealNameCertification.dao.findFirst("select * from real_name_certification where uid = ?", curr.getId());
//		if (realNameCertification == null) {
//			return Resp.fail().msg("未进行实名认证，提交实名认证，并在审核通过后，才可创建群聊");
//		}
//		if (realNameCertification.getStatus().equals(-1)) {
//			return Resp.fail().msg("实名认证失败，请重新提交实名信息");
//		}
//		if (realNameCertification.getStatus().equals(0)) {
//			return Resp.fail().msg("实名认证审核中，实名通过后才可创建群聊");
//		}
//		List<String> joinTemp = new ArrayList<>();
//		List<String> onJoinTemp = new ArrayList<>();
//
//		for (String uid : uidArr) {
//			RealNameCertification userReal = RealNameCertification.dao.findFirst("select * from real_name_certification where uid = ?", uid);
//			if (userReal != null && userReal.getStatus().equals(1)) {
//				joinTemp.add(uid);
//			} else {
//				onJoinTemp.add(uid);
//			}
//		}
//		if (joinTemp.size() == 0) {
//			return Resp.fail().msg("添加的群成员均未实名认证，创建群组失败。");
//		}
//		String[] joinUids = new String[joinTemp.size()];
//		for (int i = 0; i < joinTemp.size(); i++) {
//			joinUids[i] = joinTemp.get(i);
//		}
//		String[] onJoinUids = new String[onJoinTemp.size()];
//		for (int i = 0; i < onJoinTemp.size(); i++) {
//			onJoinUids[i] = onJoinTemp.get(i);
//		}
        Ret checkRet = WxChatApi.checkCreateGroupRegLimit(curr);
        if (checkRet.isFail()) {
            return null;
        }
//		Ret imgRet = getGroupImg(joinUids, curr, name);
        Ret imgRet = getGroupImg(uidArr, curr, name);
        Img img = RetUtils.getOkTData(imgRet, "img");
        String nicks = RetUtils.getOkTData(imgRet, "nicks");
        if (cn.hutool.core.util.StrUtil.isBlank(name)) {
            name = RetUtils.getOkTData(imgRet, "name");
            wxGroup.setName(name);
            wxGroup.setAutoflag(Const.YesOrNo.YES);
        }
        wxGroup.setAvatar(img.getCoverurl());
        Short devicetype = WebUtils.getRequestExt(request).getDeviceType();
        String sessionid = request.getHttpSession().getId();
        String ip = request.getClientIp();
//		short joinnum = joinUids != null ? (short) (joinUids.length + 1) : (short) 1;
        short joinnum = uidArr != null ? (short) (uidArr.length + 1) : (short) 1;
        wxGroup.setCreatetime(new Date());
        Ret ret = GroupService.createGroup(curr, wxGroup, nicks, devicetype, sessionid, ip, joinnum, WebUtils.getRequestExt(request).getAppVersion());
        if (ret.isFail()) {
            return null;
        }
//		ClientConf clientConf = ClientConf.dao.findFirst("select * from client_conf where name = ?", "isOpenCreateGroupNotify");

        try {
            WxGroupMsg msg = RetUtils.getOkTData(ret, "msg");
            SysMsgVo sysMsgVo = RetUtils.getOkTData(ret, "sysmsgvo");
            //发送自己的群信息
            WxChatApi.creatGroupOwner(request, devicetype, curr, wxGroup, msg, sysMsgVo);
            //其它人的消息触发
            WxChatApi.creatGroup(request, curr, uidList, nicks, wxGroup, msg, sysMsgVo);
        } catch (Exception e) {
            log.error("", e);
        }
        //消息触发
//		if (clientConf.getValue().equals(1)) {
//		Const.getBsExecutor().execute(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					WxGroupMsg msg = RetUtils.getOkTData(ret, "msg");
//					SysMsgVo sysMsgVo = RetUtils.getOkTData(ret, "sysmsgvo");
//					//发送自己的群信息
//					WxChatApi.creatGroupOwner(request, devicetype, curr, wxGroup, msg, sysMsgVo);
//					//其它人的消息触发
//					WxChatApi.creatGroup(request, curr, uidList, nicks, wxGroup, msg, sysMsgVo);
//				} catch (Exception e) {
//					log.error("", e);
//				}
//			}
//		});
//		}
        //统计处理
        Const.getBsExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String dayperiod = PeriodUtils.dateToPeriodByType(wxGroup.getCreatetime(), Const.PeriodType.DAY);
                    org.tio.sitexxx.service.model.stat.GroupStat groupStat = org.tio.sitexxx.service.model.stat.GroupStat.dao.findFirst("select * from group_stat where dayperiod = ? and type = ?", dayperiod, Const.Status.NORMAL);
                    if (groupStat == null) {
                        groupStat = new GroupStat();
                        groupStat.setAddcount(1);
                        groupStat.setDayperiod(dayperiod);
                        groupStat.setType(Const.Status.NORMAL);
                        groupStat.ignoreSave();
                    } else {
                        Db.use(Const.Db.TIO_SITE_STAT).update("update group_stat set addcount = addcount + 1 where dayperiod = ? and type = ?", dayperiod, Const.Status.NORMAL);
                    }
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        });
        return wxGroup;
    }

    /**
     * 获取群用户的的头像-已调整
     *
     * @param uidArr
     * @param curr
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年2月13日 下午4:06:50
     */
    private static Ret getGroupImg(String[] uidArr, User curr, String name) throws Exception {
        List<String> avatarList = new ArrayList<>();
        avatarList.add(curr.getAvatar());
        boolean createName = cn.hutool.core.util.StrUtil.isBlank(name) ? true : false;
        if (createName) {
            name = curr.getNick();
        }
        String nicks = "";
        int c = 0;
        for (String uidStr : uidArr) {
            if (cn.hutool.core.util.StrUtil.isNotBlank(uidStr)) {
                int uid = Integer.parseInt(uidStr);
                if (Objects.equals(uid, curr.getId())) { //前面已经把当前用户加到了该群，所以此处略过
                    continue;
                }
                User user = UserService.ME.getById(uid);
                if (user != null) {
                    if (createName) {
                        String newName = name + "、" + user.getNick();
                        if (newName.length() < 30) {
                            name = newName;
                        }
                    }
                    nicks += "、" + user.getNick();
                    if (c >= 8) {
                        continue;
                    }
                    avatarList.add(user.getAvatar());
                    c++;
                }
            }
        }
        Img img = AvatarUtils.generateGroupAvatar(avatarList, curr.getId());
//		log.error("img: {}", img);
        if (createName) {
            return Ret.ok().set("img", img).set("nicks", nicks.substring(1)).set("name", name);
        } else {
            return Ret.ok().set("img", img).set("nicks", nicks.substring(1));
        }
    }

    /**
     * 收藏转发
     *
     * @param request
     * @return
     */
    @RequestPath(value = "/sendCollectMsg")
    public Resp sendCollectMsg(HttpRequest request, String uids, String groupids, Integer collectid) throws Exception {
        if ((uids == null || uids.isEmpty()) && (groupids == null || groupids.isEmpty())) {
            return Resp.fail().msg("请选择转发对象");
        }
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail().msg("请登录");
        }
        Ret ret = WxChatApi.sendCollectMsg(request, curr, uids, groupids, collectid);
        if (ret.isFail()) {
            return Resp.fail().msg(ret.get("errorMsg").toString());
        }
        return Resp.ok();
    }

    /**
     * 笔记发送
     *
     * @param request
     * @return
     */
    @RequestPath(value = "/sendNote")
    public Resp sendNote(HttpRequest request, String sendId, Short type, Integer noteId) throws Exception {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail().msg("请登录");
        }
        Ret ret = WxChatApi.sendNote(request, curr, sendId, type, noteId);
        if (ret.isFail()) {
            return Resp.fail().msg(ret.get("errorMsg").toString());
        }
        return Resp.ok();
    }

    /**
     * 查询是否有和当前用户呼叫信息
     *
     * @param request
     * @return
     * @throws Exception
     */
    @RequestPath(value = "/getWxIsCalling")
    public Resp getWxIsCalling(HttpRequest request) throws Exception {
        User curr = WebUtils.currUser(request);
        if (curr == null) {
            return Resp.fail().msg("请登录");
        }

        Integer curUid = curr.getId();
        Map<String, Object> retMap = new HashMap<String, Object>();
        ICache cache = Caches.getCache(CacheConfig.WX_IS_CALLING);

        Integer fromUid = cache.get(curUid + "_id", Integer.class);//当前用户属于通话的目标用户
        Short targetUserType = cache.get(curUid + "_type", Short.class);//通话类型
        retMap.put("curUid", curUid);
        if (targetUserType != null) {
            retMap.put("type", targetUserType);
        }
        if (fromUid != null) {
            retMap.put("fromUid", fromUid);
        }
        return Resp.ok(retMap);
    }


    public static String sKey = "0000000000000000";

    public static String Encrypt(String sSrc) {
        byte[] raw;
        try {
            raw = sKey.getBytes("utf-8");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] encrypted = cipher.doFinal(sSrc.getBytes("utf-8"));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 解密
    public static String Decrypt(String sSrc) {
        try {
            byte[] raw = sKey.getBytes("utf-8");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] encrypted1 = Base64.getDecoder().decode(sSrc);
            try {
                byte[] original = cipher.doFinal(encrypted1);
                String originalString = new String(original, "utf-8");
                return originalString;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
