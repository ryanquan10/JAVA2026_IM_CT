
package org.tio.sitexxx.web.server.controller.base;

import cn.hutool.core.io.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.UploadFile;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Page;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.sitexxx.im.common.Command;
import org.tio.sitexxx.im.common.ImPacket;
import org.tio.sitexxx.im.common.bs.wx.friend.WxFriendChatNtf;
import org.tio.sitexxx.im.common.bs.wx.friend.WxFriendErrorNtf;
import org.tio.sitexxx.im.server.Ims;
import org.tio.sitexxx.im.server.handler.wx.PushBizService;
import org.tio.sitexxx.im.server.handler.wx.WxChatApi;
import org.tio.sitexxx.im.server.handler.wx.WxSynApi;
import org.tio.sitexxx.service.api.sms.BaseSmsVo;
import org.tio.sitexxx.service.model.conf.ClientConf;
import org.tio.sitexxx.service.model.conf.Conf;
import org.tio.sitexxx.service.model.main.*;
import org.tio.sitexxx.service.service.atom.AbsTxAtom;
import org.tio.sitexxx.service.service.atom.RegisterAtom;
import org.tio.sitexxx.service.service.base.IpInfoService;
import org.tio.sitexxx.service.service.base.RegisterService;
import org.tio.sitexxx.service.service.base.UserService;
import org.tio.sitexxx.service.service.base.sms.SmsService;
import org.tio.sitexxx.service.service.chat.*;
import org.tio.sitexxx.service.service.conf.ConfService;
import org.tio.sitexxx.service.utils.*;
import org.tio.sitexxx.service.vo.*;
import org.tio.sitexxx.service.vo.wx.SynRecordVo;
import org.tio.sitexxx.web.server.controller.base.sms.SmsController;
import org.tio.sitexxx.web.server.utils.WebUtils;
import org.tio.utils.json.Json;
import org.tio.utils.resp.Resp;

import cn.hutool.core.util.StrUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

/**
 *
 */
@RequestPath(value = "/register")
public class RegisterController {

    private static Logger log = LoggerFactory.getLogger(RegisterController.class);

    private static final RegisterService registerService = RegisterService.me;
    private final static GroupService groupService = GroupService.me;

    /**
     * 激活，发送给用户注册邮箱中的带有 authCode 的激活链接指向该 action
     *
     * @throws Exception
     */
    @RequestPath(value = "/activate")
    public Resp activate(String authCode, HttpRequest request) throws Exception {
        //		User user = (User)RegisterService.me.emailAuthCodeCache.get(authCode);
        Resp resp = registerService.activate(authCode);

        if (resp.isOk()) {
            Kv kv = (Kv) resp.getData();
            User user = (User) kv.get("user");
            p2pAfterRegister(user, request);
        }
        return resp;
    }

    /**
     * 邮箱注册
     *
     * @param user
     * @param request
     * @return
     * @author tanyaowu
     */
    @RequestPath(value = "/emailRegister")
    public Resp emailRegister(User user, HttpRequest request) throws Exception {

        RequestExt requestExt = WebUtils.getRequestExt(request);
        user.setReghref(request.getReferer());
        if (StrUtil.isBlank(user.getAvatar())) {
            String path = AvatarUtils.pressUserAvatar(user.getNick());
            if (StrUtil.isNotBlank(path)) {
                user.setAvatar(path);
                user.setAvatarbig(path);
            }
        }
        Resp resp = registerService.emailRegister(user, request.getClientIp(), request.getHttpSession().getId(), requestExt);
        return resp;
    }


    /**
     * api注册逻辑
     * <p>
     * avatar:头像-非必传<br>
     * nick:昵称-必传<br>
     * loginname:登录名(手机号)-必传<br>
     * pwd:密码-必传<br>
     *
     * @param user
     * @param request
     * @return
     * @throws Exception
     * @author lixinji 2021年1月26日 下午6:35:56
     */
    @RequestPath(value = "/apiRegister")
    public Resp apiRegister(User user, HttpRequest request) throws Exception {
        completeUser(user, request);
        RequestExt requestExt = WebUtils.getRequestExt(request);
        user.setReghref(request.getReferer());
        if (StrUtil.isBlank(user.getAvatar())) {
            String path = AvatarUtils.pressUserAvatar(user.getNick());
            if (StrUtil.isNotBlank(path)) {
                user.setAvatar(path);
                user.setAvatarbig(path);
            }
        }
        //默认同意
        user.setAgreement("1");
        Resp resp = registerService.phoneRegister(user, request.getClientIp(), request.getHttpSession().getId(), requestExt);
        if (resp.isOk()) {
            Kv kv = (Kv) resp.getData();
            User reguser = (User) kv.get("user");
            p2pAfterRegister(reguser, request);
        }
        return resp;
    }

    /**
     * @param user
     * @param request
     * @return
     * @throws Exception
     */
    @RequestPath(value = "/xx")
    public Resp xx(User user, HttpRequest request) throws Exception {
        User currUser = WebUtils.currUser(request);
        if (currUser == null || !UserService.isSuper(currUser)) {
            return Resp.fail("你没资格操作");
        }

        completeUser(user, request);

        String sql = "select min(id) from user";

        Integer uid = Db.use(Const.Db.TIO_SITE_MAIN).queryInt(sql);
        user.setId(uid - 1);

        Resp resp = registerService.innerEmailRegister(user, true);
        if (resp.isOk()) {
            p2pAfterRegister(user, request);
        }
        return resp;
    }

    @RequestPath(value = "/bxx")
    public Resp bxx(String pwd, String nicks, HttpRequest request) throws Exception {
        String sql = "select * from user order by id limit 0,1";

        User user1 = new User().dao().findFirst(sql);
        Integer uid = user1.getId();
        String loginname = user1.getLoginname();
        String xx = StrUtil.splitToArray(loginname, "@")[0];
        Long loginnameStartIndex;
        try {
            loginnameStartIndex = Long.parseLong(xx);
        } catch (Exception e) {
            throw e;
        }
        loginnameStartIndex++;

        Resp resp = null;
        String[] nickarray = nicks.split(",");
        for (String nick : nickarray) {
            String loginname1 = loginnameStartIndex + "@qq.com";
            User user = new User();
            user.setNick(nick);
            user.setPwd(UserService.getMd5Pwd(loginname1, pwd));
            user.setLoginname(loginname1);
            user.setId(--uid);

            String phone = loginnameStartIndex.toString();
            user.setPhone(phone);
            user.setPhonepwd(UserService.getMd5Pwd(phone, pwd));
            user.setPhonebindflag(Const.YesOrNo.YES);

            String path = AvatarUtils.pressUserAvatar(user.getNick());
            if (StrUtil.isNotBlank(path)) {
                user.setAvatar(path);
                user.setAvatarbig(path);
            }
            completeUser(user, request);
            resp = registerService.innerEmailRegister(user, true);
            if (resp.isOk()) {
                p2pAfterRegister(user, request);
            }

            loginnameStartIndex++;
        }
        return resp;
    }

    /**
     * 默认注册路径是走的邮箱注册
     *
     * @param user
     * @param regType 注册类型：1、邮箱，2、短信
     * @param request
     * @return
     * @throws Exception
     */
    @RequestPath(value = "/{regType}", forward = "/register/submit")
    public Resp register(User user, Short regType, HttpRequest request) throws Exception {
        ClientConf clientConf = ClientConf.dao.findFirst("select * from client_conf where name = 'IPRegisterAccountNum'");
        if (!clientConf.getValue().equals(0)) {
            List<User> users = User.dao.find("select u.* from user u, ip_info i where u.ipid = i.id and i.ip = ?", request.getClientIp());
            if (users != null && users.size() >= clientConf.getValue()) {
                return Resp.fail("同一IP只能注册" + clientConf.getValue() + "个用户");
            }
        }

        //校验是否开启了验证码
        ClientConf isOpenInviteCode = ClientConf.dao.findFirst("select * from client_conf where name = 'isOpenInviteCode'");
        if (isOpenInviteCode!=null && isOpenInviteCode.getValue().equals(1)) {
            //改查询 mg 数据库
            Integer admincount = Db.use(Const.Db.TIO_SITE_MAIN).queryInt("SELECT count(id) FROM mg_invite_org where invitecode = ? ", user.getParentinvitecode());
            Integer count = Db.use(Const.Db.TIO_SITE_MAIN).queryInt("SELECT count(id) FROM user where invitecode = ? ", user.getParentinvitecode());
            if (admincount+count < 1) {
                log.error("用户邀请码code不存在");
                return Resp.fail().msg("邀请码不存在");
            }
        }

        if (regType == 1 || regType == 2 || regType == 3) {
            completeUser(user, request);
            if (regType == 1) {
                return emailRegister(user, request);
            } else if (regType == 3) {
                return loginnameRegister(user, request);
            } else {
                return phoneRegister(user, request);
            }
        }

        return RetUtils.getInvalidResp();
    }


    /**
     * 注册配置
     *
     * @param request
     * @return
     * @throws Exception
     * @author tanyaowu
     */
    @RequestPath(value = "/properties")
    public Resp registerProperties(HttpRequest request) throws Exception {
        Integer registerType = ClientConf.dao.findFirst("select * from client_conf where name = 'registerType'").getValue();
        Integer isInternationalRegister = ClientConf.dao.findFirst("select * from client_conf where name = 'isInternationalRegister'").getValue();
        Map<String, Object> data = new HashMap<>();
        data.put("registerType", registerType);
        data.put("isInternationalRegister", isInternationalRegister);
        return Resp.ok(data);
    }


    /**
     * 用邮箱找回密码（第一步：发地址到邮箱）
     *
     * @param loginname
     * @param request
     * @return
     * @throws Exception
     * @author tanyaowu
     */
    @RequestPath(value = "/retrievePwd")
    public Resp retrievePwd(String loginname, HttpRequest request) throws Exception {
        RequestExt requestExt = WebUtils.getRequestExt(request);
        return registerService.retrievePwd(loginname, request.getClientIp(), request.getHttpSession().getId(), requestExt);
    }

    /**
     * 设置新密码（第二步：设置新密码）
     *
     * @param authCode
     * @param request
     * @return
     * @throws Exception
     */
    @RequestPath(value = "/setNewPwd")
    public Resp setNewPwd(String authCode, String newpwd, HttpRequest request) throws Exception {
        return registerService.setNewPwd(authCode, newpwd);
    }

    private void completeUser(User user, HttpRequest request) {
        IpInfo ipInfo = IpInfoService.ME.save(request.getClientIp());
        user.setIpInfo(ipInfo);
        RequestExt requestExt = WebUtils.getRequestExt(request);
        short deviceType = requestExt.getDeviceType();
        user.setRegistertype(deviceType);
    }

    public static boolean register(HttpRequest request, User user, RegisterAtom registerUserAtom) {
        boolean result = Db.tx(registerUserAtom);
        if (result) {
            p2pAfterRegister(user, request);
        }

        return result;
    }

    @SuppressWarnings("deprecation")
    public static void p2pAfterRegister(User user, HttpRequest request) {
        Ret slef = ChatService.me.actFdChatItems(user.getId(), user.getId());
        if (slef.isFail()) {
            return;
        }
        WxChatItems selfChatItems = RetUtils.getOkTData(slef, "chat");
        if (WxSynApi.isSynVersion()) {
            WxSynApi.synChatSession(user.getId(), selfChatItems, SynRecordVo.SynType.ADD);
        } else {
            WxChatApi.userActOper(request, user.getId(), selfChatItems);
        }
    }


    @RequestPath(value = "/loginnameRegister")
    private Resp loginnameRegister(User user, HttpRequest request) throws Exception {
        if (!user.getLoginname().matches("[a-zA-Z0-9]+")) {
            return Resp.fail().msg("用户名由数字和字母组成");
        }
        if (user.getLoginname().length() > 21 || user.getLoginname().length() < 6) {
            return Resp.fail().msg("用户名长度为6-21位");
        }
//		if (User.dao.findFirst("select * from user where loginname = ? or phone = ? or email = ?", user.getLoginname()) != null) {
//			return Resp.fail().msg("用户名已存在");
//		}
        ClientConf clientConf = ClientConf.dao.findFirst("select * from client_conf where name = 'isOpenInviteCode'");


        while (true) {
            Random random = new Random();
            Integer uid = random.nextInt(100000) + 100000;
            User u = User.dao.findById(uid);
            if (u == null) {
                user.setId(uid);
                break;
            }
        }

        RequestExt requestExt = WebUtils.getRequestExt(request);
//		Resp beforeCheck = SmsController.bizPhoneCheck(BaseSmsVo.BaseSmsBizType.REGISTER, user.getLoginname(), request);
//		if (!beforeCheck.isOk()) {
//			return beforeCheck;
//		}
//		Ret ret = SmsService.me.checkCode(user.getLoginname(), BaseSmsVo.BaseSmsBizType.REGISTER, user.getCode(), null, false);
//		if (ret.isFail()) {
//			return Resp.fail(RetUtils.getRetMsg(ret));
//		}
        user.setReghref(request.getReferer());
        if (StrUtil.isBlank(user.getAvatar())) {
            String path = AvatarUtils.pressUserAvatar(user.getNick());
            if (StrUtil.isNotBlank(path)) {
                user.setAvatar(path);
                user.setAvatarbig(path);
            }
        }
        Resp resp = registerService.loginnameRegister(user, request.getClientIp(), request.getHttpSession().getId(), requestExt);
        if (resp.isOk()) {
            SmsService.me.delCode(user.getLoginname(), BaseSmsVo.BaseSmsBizType.REGISTER);
            Kv kv = (Kv) resp.getData();
            User reguser = (User) kv.get("user");
            p2pAfterRegister(reguser, request);
        } else {
            return resp;
        }

        //添加到组织
        addUserOrg(user);

        if (false) {
            if (clientConf.getValue().equals(1)) {
                boolean b = addDefaultFriends(request, user, 1);
                if (b) {
                    log.info("成功添加上级用户");
                }
                if (!b) {
                    log.info("添加上级用户失败");
                }
            }
            boolean b = addDefaultFriends(request, user);
            if (b) {
                log.info("成功添加上级用户");
            }
            if (!b) {
                log.info("添加上级用户失败");
            }
        }
        //默认群组
        List<DefaultGroup> groups = DefaultGroup.dao.find("select * from default_group where isopen=1");
        if (groups != null) {
            for (DefaultGroup group : groups) {
                boolean dealapplyfinal = false;
                //异步处理触发
                Const.getBsExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Short devicetype = WebUtils.getRequestExt(request).getDeviceType();
                            Ret ret = groupService.joinGroupByRegister(user, group.getGroupid(), user.getId() + "", null, dealapplyfinal);
                            log.error("else ret isFail : {}", ret.isFail());
                            if (ret.isFail()) {
                                WxChatApi.sendFriendErrorMsg(request, user.getId(), user.getId(), user.getId(), -group.getGroupid(), AppCode.GroupErrorCode.SYS_ERROR, RetUtils.getRetMsg(ret));
                                return;
                            }
                            Short joinnum = RetUtils.getOkTData(ret, "joinnum");
                            Short nameUpdate = RetUtils.getOkTData(ret, "nameupdate");
                            if (joinnum != null && joinnum != 0) {
                                User msgUser = user;
                                boolean auot = auotUpdateGroupInfo(request, devicetype, group.getGroupid(), nameUpdate, joinnum, msgUser);
                                WxChatApi.joinGroup(request, msgUser, group.getGroupid(), RetUtils.getOkTData(ret), RetUtils.getOkTData(ret, "rebind"), auot);
                            }
                        } catch (Exception e) {
                            log.error("", e);
                        }
                    }
                });
            }

        }
        return resp;

    }

    /**
     * 自动修改群头像和群名称-已调整
     * 1、群名称如果指定修改后，该方法无效
     * 2、群头像生成条件：A、人员减员；B、人员新增时，未满；C、群权限操作
     * 3、群昵称生成条，排除1后，A、2触发；B、上层逻辑触发
     *
     * @param request
     * @param devicetype
     * @param groupid
     * @param nameUpdate 名称修改标识：1：修改；2：不修改
     * @param groupname  1、修改后的群名称
     * @param joinnum    人数变动数据
     * @param curr
     * @throws Exception
     * @author lixinji
     * 2020年3月13日 下午5:20:25
     */
    public static boolean auotUpdateGroupInfo(HttpRequest request, Short devicetype, Long groupid, Short nameUpdate, Short joinnum, User curr) throws Exception {
        WxGroup group = groupService.getByGroupid(groupid);
        if (group == null || group.getJoinnum() == 0) {
            log.error("群自动修改头像，本群已解散：groupid-{}", groupid);
            return false;
        }
        //群名称是否重新生成
        boolean nameInit = false;
        String name = "";
        String oldName = group.getName();
        if (Objects.equals(group.getAutoflag(), Const.YesOrNo.YES) && Objects.equals(nameUpdate, Const.YesOrNo.YES)) { //群名称修改标识
            nameInit = true;
        }
        String avatar = ""; //头像是否生成
        //1、joinnum不为空；2：joinnum > 0时，新增用户判断；3:joinnum < 0时，退出或者被踢
        if (Objects.equals(group.getAvatarautoflag(), Const.YesOrNo.YES) && joinnum != null && ((joinnum > 0 && group.getJoinnum() - joinnum < 9) //新增用户判断,新增前是否已满头像
                || joinnum <= 0 //删除人或者操作，重新更新下头像
        )) { // 头像重新生成
            List<String> avatarList = new ArrayList<>();
            Ret ret = groupService.groupUserList(groupid, null, "");
            if (ret.isFail()) {

                return false;
            }
            Page<Record> page = RetUtils.getOkTPage(ret);
            if (page == null) {
                log.error("获取用户列表为空");
                return false;
            }
            List<Record> groupUserList = page.getList();
            int avatarCount = 0;
            for (Record record : groupUserList) {
                if (avatarCount > 8) {
                    break;
                }
                Integer uid = record.getInt("uid");
                User user = UserService.ME.getById(uid);
                if (user != null) {
                    avatarList.add(user.getAvatar());
                    avatarCount++;
                }
                if (nameInit) {
                    String newName = name + "、" + user.getNick();
                    if (newName.length() >= 31) {
                        continue;
                    }
                    name = newName;
                }
            }
            try {
                Img img = AvatarUtils.generateGroupAvatar(avatarList, groupUserList.get(0).getInt("uid"));
                avatar = img.getCoverurl();
                if (!avatar.equals(group.getAvatar())) {
                    groupService.modifyAvatar(groupid, avatar, true);
                } else {
                    avatar = "";
                }
            } catch (Exception e) {
                log.error("", e);
            }
        } else if (nameInit) { //头像不生成，名称需要重新生成
            Ret ret = groupService.groupUserList(groupid, null, "");
            if (ret.isFail()) {

                return false;
            }
            Page<Record> page = RetUtils.getOkTPage(ret);
            if (page == null) {
                log.error("获取用户列表为空");
                return false;
            }
            List<Record> groupUserList = page.getList();
            for (Record record : groupUserList) {
                Integer uid = record.getInt("uid");
                User user = UserService.ME.getById(uid);
                String newName = name + "、" + user.getNick();
                if (newName.length() >= 31) {
                    continue;
                }
                name = newName;
            }
        }
        if (StrUtil.isNotBlank(name)) {
            name = name.substring(1);
        }
        if (StrUtil.isNotBlank(name) && !oldName.equals(name)) {
            groupService.modifyName(curr.getId(), groupid, name, true);
        } else {
            name = "";
        }
        if (StrUtil.isNotBlank(name) || StrUtil.isNotBlank(avatar)) {
            return true;
        }
        return false;
    }

    /**
     * @param mobile
     * @param pwd
     * @param code
     * @param request
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年12月16日 上午11:13:22
     */
    @RequestPath(value = "/phoneRegister")
    public Resp phoneRegister(User user, HttpRequest request) throws Exception {
        if (user.getType() == null) {
            user.setType(1);
        }
        if (StrUtil.isBlank(user.getCode())) {
            log.error("用户验证码code不存在");
            return Resp.fail().msg("用户验证码code不存在");
        }
        if (User.dao.findFirst("select * from user where loginname = ? or phone = ? or email = ?", user.getLoginname(), user.getLoginname(), user.getLoginname()) != null) {
            return Resp.fail().msg("手机号已存在");
        }

        ClientConf clientConf = ClientConf.dao.findFirst("select * from client_conf where name = 'isOpenInviteCode'");
//        if (clientConf.getValue().equals(1)) {
//            //改查询 mg 数据库
//            Integer count = Db.use(Const.Db.TIO_MG).queryInt("SELECT count(id) FROM mg_invite_org where invitecode = ? ", user.getParentinvitecode());
//            if (count == null || count < 1) {
//                log.error("用户邀请码code不存在");
//                return Resp.fail().msg("邀请码不存在");
//            }
//        }

        while (true) {
            Random random = new Random();
            Integer uid = random.nextInt(100000) + 100000;
            User u = User.dao.findById(uid);
            if (u == null) {
                user.setId(uid);
                break;
            }
        }

        RequestExt requestExt = WebUtils.getRequestExt(request);
        Resp beforeCheck = Resp.ok();
        if (Integer.valueOf(1).equals(user.getType())) {
            beforeCheck = SmsController.bizPhoneCheck(BaseSmsVo.BaseSmsBizType.REGISTER, user.getLoginname(), request);
        } else {
            beforeCheck = SmsController.bizPhoneCheckForAbroad(BaseSmsVo.BaseSmsBizType.REGISTER, user.getLoginname(), request);
        }
        if (!beforeCheck.isOk()) {
            return beforeCheck;
        }
        Ret ret = SmsService.me.checkCode(user.getLoginname(), BaseSmsVo.BaseSmsBizType.REGISTER, user.getCode(), null, false);
        if (ret.isFail()) {
            return Resp.fail(RetUtils.getRetMsg(ret));
        }
        user.setReghref(request.getReferer());
        if (StrUtil.isBlank(user.getAvatar())) {
            String path = AvatarUtils.pressUserAvatar(user.getNick());
            if (StrUtil.isNotBlank(path)) {
                user.setAvatar(path);
                user.setAvatarbig(path);
            }
        }
        Resp resp = registerService.phoneRegister(user, request.getClientIp(), request.getHttpSession().getId(), requestExt);
        if (resp.isOk()) {
            SmsService.me.delCode(user.getLoginname(), BaseSmsVo.BaseSmsBizType.REGISTER);
            Kv kv = (Kv) resp.getData();
            User reguser = (User) kv.get("user");
            p2pAfterRegister(reguser, request);
        }

        //添加到组织
        addUserOrg(user);

        if (false) {
            if (clientConf.getValue().equals(1)) {
                boolean b = addDefaultFriends(request, user, 1);
                if (b) {
                    log.error("成功添加上级用户");
                }
                if (!b) {
                    log.error("添加上级用户失败");
                }
            }
            boolean b = addDefaultFriends(request, user);
            if (b) {
                log.error("成功添加上级用户");
            }
            if (!b) {
                log.error("添加上级用户失败");
            }
        }
        List<DefaultGroup> groups = DefaultGroup.dao.find("select * from default_group where isopen=1");
        if (groups != null) {
            for (DefaultGroup group : groups) {
                boolean dealapplyfinal = false;
                //异步处理触发
                Const.getBsExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Short devicetype = WebUtils.getRequestExt(request).getDeviceType();
                            Ret ret = groupService.joinGroupByRegister(user, group.getGroupid(), user.getId() + "", null, dealapplyfinal);
                            log.error("else ret isFail : {}", ret.isFail());
                            if (ret.isFail()) {
                                WxChatApi.sendFriendErrorMsg(request, user.getId(), user.getId(), user.getId(), -group.getGroupid(), AppCode.GroupErrorCode.SYS_ERROR, RetUtils.getRetMsg(ret));
                                return;
                            }
                            Short joinnum = RetUtils.getOkTData(ret, "joinnum");
                            Short nameUpdate = RetUtils.getOkTData(ret, "nameupdate");
                            if (joinnum != null && joinnum != 0) {
                                User msgUser = user;
                                boolean auot = auotUpdateGroupInfo(request, devicetype, group.getGroupid(), nameUpdate, joinnum, msgUser);
                                WxChatApi.joinGroup(request, msgUser, group.getGroupid(), RetUtils.getOkTData(ret), RetUtils.getOkTData(ret, "rebind"), auot);
                            }
                        } catch (Exception e) {
                            log.error("", e);
                        }
                    }
                });
            }
        }

        return resp;
    }


    /**
     * 上传文件
     *
     * @param request
     * @param file
     * @return
     */
    @RequestPath("/upload")
    public Resp upload(HttpRequest request, UploadFile file) {
        if (file == null) {
            return Resp.fail("参数异常");
        }

        byte[] bs = file.getData();
        String filename = file.getName();
        String extName = FileUtil.extName(filename).toLowerCase(); // 统一转小写处理

        // 只允许特定格式的图片
        if (!"jpg jpeg png bmp".contains(extName)) {
            return Resp.fail("仅支持 jpg/jpeg/png/bmp 格式的图片上传");
        }

        try {
            String objectKey = UploadUtils.dateFile("avatar/img/") + "." + extName;

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
                case "bmp":
                    contentType = "image/bmp";
                    break;
                default:
                    contentType = "application/octet-stream";
            }

            // 上传文件到 R2
            InputStream inputStream = new ByteArrayInputStream(bs);
            UploadUtils.unificationUpload( objectKey, inputStream, bs.length, contentType);
//            CloudflareR2Utils.uploadFilePublic(
//                    Const.CloudflareR2.R2_BUCKET_NAME,
//                    objectKey,
//                    inputStream,
//                    bs.length,
//                    contentType
//            );

            // 返回相对路径，前端拼接 base_url 获取完整 URL
            return Resp.ok(objectKey);

        } catch (Exception e) {
            log.error("文件上传到 R2 异常", e);
            return Resp.fail().code(500).msg("文件上传失败");
        }
    }


    public static boolean addDefaultFriends(HttpRequest request, User user, Integer type) throws Exception {
        User pUser = User.dao.findFirst("select * from user where invitecode = ?", user.getParentinvitecode());
        Resp resp = addFriend(request, "", user.getId(), pUser.getId(), type);
        return true;
    }


    /**
     * 添加用户组织
     */
    public static void addUserOrg(User user) throws Exception {
        Integer wxNoInvitationDefault = ConfService.getInt(Const.ConfMapping.WX_NO_INVITATION_DEFAULT, -1);
        Integer orgId = Db.use(Const.Db.TIO_SITE_MAIN).queryInt("SELECT id FROM mg_invite_org where invitecode = ? ", user.getParentinvitecode());
        Record record = new Record();
        record.set("uid", user.getId());
        record.set("createtime", new Date());
        if (orgId != null) {
            record.set("inviteorgid", orgId);
        } else if (wxNoInvitationDefault != null && wxNoInvitationDefault.intValue() != -1) {
            record.set("inviteorgid", wxNoInvitationDefault);
        }
        Db.use(Const.Db.TIO_SITE_MAIN).save("mg_invite_user", record);
    }

    public static boolean addDefaultFriends(HttpRequest request, User user) throws Exception {

        // 是否轮询
        DefaultFriendsConfig defaultFriendsConfig = DefaultFriendsConfig.dao.findById(1);
        if (defaultFriendsConfig == null) {
            return true;
        }
        Integer isRotation = defaultFriendsConfig.getIsRotation();

        // 获取客服们的uid
        List<Integer> defaultUserIds = getDefaultUserIds();

        Integer uid = user.getId();

        if (defaultUserIds != null && !defaultUserIds.isEmpty()) {
            User pUser = User.dao.findFirst("select * from user where invitecode = ?", user.getParentinvitecode());
            if (pUser != null) {
                defaultUserIds.remove(pUser.getId());
            }

            // 如果不轮询添加，则添加所有客服
            if (isRotation.equals(0)) {
                for (Integer touid : defaultUserIds) {
                    Resp resp = addFriend(request, "", uid, touid, 2);
                }
                return true;
            }
            // 如果轮询添加，找到需要添加的uid
            if (isRotation.equals(1)) {
                Integer touid = getAddUserId();
                Resp resp = addFriend(request, "", uid, touid, 2);
                if (!resp.isOk()) {
                    return true;
                }
                // 添加客服后，更新客服的权值
                DefaultFriends defaultFriend = DefaultFriends.dao.findFirst("select * from default_friends where uid = ?", touid.toString());
                defaultFriend.setId(defaultFriend.getId());
                defaultFriend.setUid(defaultFriend.getUid());
                defaultFriend.setDefaultMsg(defaultFriend.getDefaultMsg());
                defaultFriend.setWeight(defaultFriend.getWeight() + 1);
                defaultFriend.update();
                return true;
            }
        }
        return false;
    }

    public static List<Integer> getDefaultUserIds() {
        List<DefaultFriends> defaultFriends = DefaultFriends.dao.findAll();
        if (defaultFriends != null && !defaultFriends.isEmpty()) {
            List<Integer> uidList = new ArrayList<Integer>();
            for (DefaultFriends defaultFriend : defaultFriends) {
                uidList.add(Integer.valueOf(defaultFriend.getUid()));
            }
            return uidList;
        }
        return null;
    }

    public static Integer getAddUserId() {
        List<DefaultFriends> defaultFriends = DefaultFriends.dao.findAll();
        int weightMin = 999999999;
        if (defaultFriends != null && !defaultFriends.isEmpty()) {

            for (DefaultFriends defaultFriend : defaultFriends) {
                Integer weight = defaultFriend.getWeight();
                Integer point = DefaultFriendsConfig.dao.findFirst("select * from default_friends_config").getPoint();
                if (point.equals(1)) {
                    weightMin = weightMin > weight ? weight : weightMin;
                } else if (weight % point != 0) {
                    return Integer.valueOf(defaultFriend.getUid());
                } else if (weightMin > weight) {
                    weightMin = weight;
                }
            }

            String uid = DefaultFriends.dao.findFirst("select * from default_friends where weight = ?", weightMin).getUid();
            return Integer.valueOf(uid);
        }
        return 0;
    }


    public static Resp addFriend(HttpRequest request, String remarkname, Integer uid, Integer touid, Integer type) throws Exception {
        User tempUser = User.dao.findById(touid);
        if (tempUser == null) {
            return Resp.fail("客服不存在");
        }
        RequestExt ext = (RequestExt) request.getAttribute(RequestKey.REQUEST_EXT);
        String appversion = ext.getAppVersion();
        Short devicetype = ext.getDeviceType();
        String sessionid = request.getHttpSession().getId();
        String ip = request.getClientIp();

        User curr = User.dao.findById(touid);

        Ret ret = dealApply(curr, remarkname, uid, touid);
        if (ret.isFail()) {

            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        String underToUpMsg = Conf.dao.findFirst("select * from conf where name = ?", "register.defaultMsg.underToUp").getValue();
        String upToUnderMsg = Conf.dao.findFirst("select * from conf where name = ?", "register.defaultMsg.upToUnder").getValue();
        //消息处理
        if (type.equals(1)) {
            Const.getBsExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        String send = RetUtils.retKey(ret, "send");
                        if (StrUtil.isNotBlank(send)) {
                            String greet = RetUtils.retKey(ret, "greet");
                            //操作者的聊天列表
                            Long tochatlinkid = RetUtils.retLongKey(ret, "tochatlinkid");
                            if (send.equals("double")) {
                                boolean applysigle = RetUtils.getOkTData(ret, "applysigle");
                                //申请者的聊天列表
                                Long chatlinkid = RetUtils.retLongKey(ret, "chatlinkid");
                                addFriendEachOfPassApply(request, uid, touid, chatlinkid, tochatlinkid, greet, applysigle);
                            } else {
                                addFriendSigleOfPassApply(request, uid, touid, tochatlinkid, greet);
                            }
                        }
                        WxFriend friend = RetUtils.getOkTData(ret, "friend");
                        WxFriend toFriend = RetUtils.getOkTData(ret, "tofriend");
                        WxChatApi.friendChangeAddNtf(request, friend, toFriend);
                        if (underToUpMsg != null && !underToUpMsg.isEmpty()) {
                            Ret ret1 = WxChatApi.sendFdMsgEach(devicetype, sessionid, ip, underToUpMsg, Const.ContentType.TEXT, uid, touid, Const.YesOrNo.NO, "", null, Const.ChatMode.P2P,
                                    appversion, null);
                        }
                        if (upToUnderMsg != null && !upToUnderMsg.isEmpty()) {
                            Ret ret1 = WxChatApi.sendFdMsgEach(devicetype, sessionid, ip, upToUnderMsg, Const.ContentType.TEXT, touid, uid, Const.YesOrNo.NO, "", null, Const.ChatMode.P2P,
                                    appversion, null);
                        }
                    } catch (Exception e) {
                        log.error("", e);
                    }
                }
            });
        } else {
            //消息处理
            Const.getBsExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        String send = RetUtils.retKey(ret, "send");
                        if (StrUtil.isNotBlank(send)) {
                            String greet = RetUtils.retKey(ret, "greet");
                            //操作者的聊天列表
                            Long tochatlinkid = RetUtils.retLongKey(ret, "tochatlinkid");
                            if (send.equals("double")) {
                                boolean applysigle = RetUtils.getOkTData(ret, "applysigle");
                                //申请者的聊天列表
                                Long chatlinkid = RetUtils.retLongKey(ret, "chatlinkid");
                                addFriendEachOfPassApply(request, uid, touid, chatlinkid, tochatlinkid, greet, applysigle);
                            } else {
                                addFriendSigleOfPassApply(request, uid, touid, tochatlinkid, greet);
                            }
                        }
                        WxFriend friend = RetUtils.getOkTData(ret, "friend");
                        WxFriend toFriend = RetUtils.getOkTData(ret, "tofriend");
                        WxChatApi.friendChangeAddNtf(request, friend, toFriend);
                    } catch (Exception e) {
                        log.error("", e);
                    }
                }
            });
        }

        return Resp.ok(RetUtils.getOkData(ret));
    }


    public static Ret dealApply(User curr, String remarkname, Integer uid, Integer touid) throws Exception {
        if (uid == null || touid == null) {
            return RetUtils.invalidParam();
        }
        if (!Objects.equals(touid, curr.getId())) {
            return RetUtils.grantError();
        }
        User user = null;
        if (Objects.equals(curr.getId(), uid)) {
            user = curr;
        } else {
            user = UserService.ME.getById(uid);
            if (user == null) {
                return RetUtils.failMsg("操作的用户不存在");
            }
        }
        User toUser = UserService.ME.getById(touid);
        if (toUser == null) {
            return RetUtils.failMsg("用户不存在");
        }
        if (Objects.equals(toUser.getStatus(), User.Status.LOGOUT)) {
            return RetUtils.failMsg("用户已注销");
        }
        WxChatUserItem userItem = ChatIndexService.fdUserIndex(uid, touid);
        if (ChatService.existFriend(userItem)) {
            return selfAddFriendExistToFriendByApply(touid, uid, userItem, remarkname).set("greet", "").set("applysigle", true);
        }
        boolean addTo = true;
        if (!ChatService.checkBlack(curr.getId(), uid, touid)) {
            log.warn("好友申请处理警告：申请人把好友加入了黑名单，uid:{},touid:{}", uid, touid);
            addTo = false;
        }
        if (addTo) {
            return eachAddFriendByApply(user, toUser, toUser.getId(), remarkname).set("greet", "").set("applysigle", false);
        } else {
            // 此处为审核方加邀请方好友，但主动加好友的人把被邀请人拉黑，无权限家人
            return selfAddFriendBlockToFriend(user, toUser, toUser.getId(), remarkname).set("greet", "");
        }
    }

    public static Ret selfAddFriendBlockToFriend(User user, User toUser, Integer touid, String remarkName) {
        Integer uid = user.getId();
        AbsTxAtom atom = new AbsTxAtom() {
            @Override
            public boolean noTxRun() {
                // 保存好友信息
                WxFriend friend = friendInit(touid, uid, null, remarkName, false);
                if (friend == null) {
                    return failRet("重复加好友");
                }
                WxChatItems chatItems = new WxChatItems();
                chatItems.setUid(touid);
                chatItems.setChatmode(Const.ChatMode.P2P);
                chatItems.setBizid(new Long(uid));
                chatItems.setAvatar(user.getAvatar());
                chatItems.setLinkflag(Const.YesOrNo.NO);
                chatItems.setName(StrUtil.isNotBlank(remarkName) ? remarkName : user.getNick());
                chatItems.setLinkid(friend.getId());
                chatItems.setFidkey(UserService.twoUid(uid, touid));
                boolean chatinit = chatItems.save();
                if (!chatinit) {
                    return failRet("聊天记录保存失败");
                }
                WxChatItemsMeta meta = new WxChatItemsMeta();
                meta.setViewflag(Const.YesOrNo.YES);
                meta.setChatuptime(new Date());
                boolean metainit = meta.save();
                if (!metainit) {
                    return failRet("聊天记录保存失败");
                }
                chatItems.setMeta(meta);
                chatItems.setChatlinkmetaid(meta.getId());
                // 创建用户索引
                int count = ChatIndexService.me.chatUserInit(touid, Const.ChatMode.P2P, new Long(uid), chatItems.getId(), chatItems.getChatlinkmetaid(), null, null, friend.getId(),
                        Const.YesOrNo.NO, null);
                if (count <= 0) {
                    return failRet("创建索引冲突");
                }
                // 此处返回的是操作者方向的数据，所有chatlinkid的方向是反的
                retObj = RetUtils.okData(friend).set("send", "one").set("tochatlinkid", chatItems.getId()).set("friend", friend);
                return true;
            }
        };
        Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
        // 不需要做缓存处理
        return atom.getRetObj();
    }

    public static Ret eachAddFriendByApply(User user, User toUser, Integer touid, String remarkName) {
        Integer uid = user.getId();
        WxFriendApplyItems toItems = FriendApplyService.me.getApply(touid, uid);
        // 创建双方聊天会话
        WxChatItems chatItems = new WxChatItems();
        chatItems.setUid(uid);
        chatItems.setChatmode(Const.ChatMode.P2P);
        chatItems.setBizid(new Long(touid));
        chatItems.setAvatar(toUser.getAvatar());
        chatItems.setLinkflag(Const.YesOrNo.YES);
        chatItems.setName(toUser.getNick());
        chatItems.setFidkey(UserService.twoUid(uid, touid));
        WxChatItemsMeta meta = new WxChatItemsMeta();
        meta.setViewflag(Const.YesOrNo.YES);
        AbsTxAtom atom = new AbsTxAtom() {
            @Override
            public boolean noTxRun() {
                WxFriend friend = friendInit(uid, touid, null, "", true);
                if (friend == null) {
                    return failRet("好友记录保存失败");
                }
                WxFriend tofriend = friendInit(touid, uid, null, remarkName);
                if (tofriend == null) {
                    return failRet("to好友记录保存失败");
                }
                chatItems.setLinkid(friend.getId());
                meta.setChatuptime(new Date());
                boolean chatinit = chatItems.save();
                if (!chatinit) {
                    return failRet("聊天会话保存失败");
                }
                meta.setChatlinkid(chatItems.getId());
                boolean metainit = meta.save();
                if (!metainit) {
                    return failRet("聊天动态会话保存失败");
                }
                chatItems.setChatlinkmetaid(meta.getId());
                chatItems.setMeta(meta);
                WxChatItems tochatItems = ChatService.me.chatitemsInitToItem(chatItems, tofriend, user);
                boolean toChatinit = tochatItems.save();
                if (!toChatinit) {
                    return failRet("to聊天会话保存失败");
                }
                tochatItems.getMeta().setChatlinkid(tochatItems.getId());
                boolean tometainit = tochatItems.getMeta().save();
                if (!tometainit) {
                    return failRet("to聊天动态会话保存失败");
                }
                tochatItems.setChatlinkmetaid(tochatItems.getMeta().getId());
                // 关联用户索引
                int indexinit = ChatIndexService.me.chatUserInit(friend.getId(), uid, Const.ChatMode.P2P, new Long(touid), chatItems.getId(), chatItems.getChatlinkmetaid(),
                        tochatItems.getId(), tochatItems.getChatlinkmetaid());
                if (indexinit <= 0) {
                    return failRet("好友索引保存失败");
                }
                int toindexinit = ChatIndexService.me.chatUserInit(tofriend.getId(), touid, Const.ChatMode.P2P, new Long(uid), tochatItems.getId(), tochatItems.getChatlinkmetaid(),
                        chatItems.getId(), chatItems.getChatlinkmetaid());
                if (toindexinit <= 0) {
                    return failRet("to好友索引保存失败");
                }
                if (toItems != null && !Objects.equals(toItems.getStatus(), Const.ApplyStatus.PASS)) {
                    boolean isupdate = FriendApplyService.me.update(toItems.getId(), Const.ApplyStatus.PASS);
                    if (!isupdate) {
                        return failRet("to更新申请记录失败");
                    }
                }
                retObj = RetUtils.okData(tofriend).set("send", "double").set("chatlinkid", chatItems.getId()).set("tochatlinkid", tochatItems.getId()).set("friend", friend)
                        .set("tofriend", tofriend);
                return true;
            }
        };
        Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
        // 不做缓存处理
        return atom.getRetObj();
    }


    public static Ret selfAddFriendExistToFriendByApply(Integer uid, Integer touid, WxChatUserItem touserItem, String remarkname) {
        User user = UserService.ME.getById(touid);
        AbsTxAtom atom = new AbsTxAtom() {
            @Override
            public boolean noTxRun() {
                // 保存好友信息
                WxFriend friend = friendInit(uid, touid, null, remarkname, false);
                if (friend == null) {
                    return failRet("重复加好友");
                }
                // 初始化会话
                WxChatItems chatItems = new WxChatItems();
                chatItems.setUid(uid);
                chatItems.setChatmode(Const.ChatMode.P2P);
                chatItems.setBizid(new Long(touid));
                chatItems.setAvatar(user.getAvatar());
                chatItems.setLinkflag(Const.YesOrNo.YES);
                chatItems.setName(StrUtil.isNotBlank(remarkname) ? remarkname : user.getNick());
                chatItems.setLinkid(friend.getId());
                chatItems.setFidkey(UserService.twoUid(uid, touid));
                boolean chatinit = chatItems.save();
                if (!chatinit) {
                    return failRet("聊天会话保存失败");
                }
                WxChatItemsMeta meta = new WxChatItemsMeta();
                meta.setChatlinkid(chatItems.getId());
                meta.setViewflag(Const.YesOrNo.YES);
                meta.setChatuptime(new Date());
                boolean metainit = meta.save();
                if (!metainit) {
                    return failRet("聊天动态会话保存失败");
                }
                chatItems.setChatlinkmetaid(meta.getId());
                chatItems.setMeta(meta);
                boolean toInit = false;
                // 初始化自己的会话
                if (!ChatService.friendExistChat(touserItem)) {
                    User touser = UserService.ME.getById(touid);
                    WxFriend myfriend = FriendService.me.getFriendInfo(touserItem.getLinkid());
                    WxChatItems toChatItems = ChatService.me.chatitemsInit(touserItem.getUid(), touserItem.getChatmode(), touserItem.getBizid(), touserItem.getLinkid(),
                            touser.getAvatar(), StrUtil.isNotBlank(myfriend.getRemarkname()) ? myfriend.getRemarkname() : touser.getNick(), Const.YesOrNo.YES, Const.YesOrNo.YES, 0,
                            Const.YesOrNo.YES, null, "", "", Const.YesOrNo.NO, null, null, null, null, myfriend.getMsgfreeflag());
                    if (toChatItems == null) {
                        return failRet("初始化对方聊天会话失败");
                    }
                    touserItem.setChatlinkid(toChatItems.getId());
                    touserItem.setChatlinkmetaid(toChatItems.getChatlinkmetaid());
                    toInit = true;
                }
                // 初始化索引
                WxChatUserItem oldItem = ChatIndexService.chatUserIndex(uid, touid, Const.ChatMode.P2P);
                if (oldItem != null) {
                    WxChatUserItem update = new WxChatUserItem();
                    update.setUid(uid);
                    update.setChatmode(Const.ChatMode.P2P);
                    update.setBizid(new Long(touid));
                    update.setLinkflag(Const.YesOrNo.YES);
                    update.setLinkid(friend.getId());
                    update.setChatlinkid(chatItems.getId());
                    update.setChatlinkmetaid(chatItems.getMeta().getId());
                    if (touserItem.getChatlinkid() != null) {// 如果对方的会话存在
                        update.setTochatlinkid(touserItem.getChatlinkid());
                        update.setTochatlinkmetaid(touserItem.getChatlinkmetaid());
                    }
                    boolean updateflag = update.update();
                    if (!updateflag) {
                        return failRet("已存在的索引修改失败");
                    }
                } else {
                    // 创建用户索引
                    int count = ChatIndexService.me.chatUserInit(uid, Const.ChatMode.P2P, new Long(touid), chatItems.getId(), chatItems.getChatlinkmetaid(),
                            touserItem.getChatlinkid(), touserItem.getChatlinkmetaid(), friend.getId(), Const.YesOrNo.YES, null);
                    if (count <= 0) {
                        return failRet("创建索引冲突");
                    }
                }
                if (toInit) {
                    // 修改索引状态
                    boolean chatindex = ChatIndexService.me.chatUserIndexUpdate(touid, new Long(uid), Const.ChatMode.P2P, touserItem.getChatlinkid(),
                            touserItem.getChatlinkmetaid(), chatItems.getId(), chatItems.getChatlinkmetaid(), null, Const.YesOrNo.YES);
                    if (!chatindex) {
                        return failRet("修改索引状态异常");
                    }
                } else {
                    // 更新对方用户索引
                    boolean update = ChatIndexService.me.chatUserIndexUpdate(touid, new Long(uid), Const.YesOrNo.YES, Const.YesOrNo.YES, null, touserItem.getChatlinkid(),
                            touserItem.getChatlinkmetaid(), chatItems.getId(), chatItems.getChatlinkmetaid());
                    if (!update) {
                        failRet("更新索引失败");
                        return false;
                    }
                    boolean toUpdateChat = ChatService.me.updateChatLink(Const.YesOrNo.YES, touserItem.getChatlinkid(), touserItem.getChatlinkmetaid(), null);
                    if (!toUpdateChat) {
                        failRet("更新好友会话失败");
                        return false;
                    }
                }
                retObj = RetUtils.okData(friend).set("send", "double").set("tochatlinkid", chatItems.getId()).set("chatlinkid", touserItem.getChatlinkid()).set("friend", friend);
                return true;
            }
        };
        boolean add = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
        if (add) {
            ChatIndexService.clearChatP2pIndex(uid, touid);
            ChatIndexService.clearChatP2pIndex(touid, uid);
            if (touserItem.getChatlinkid() != null) {
                ChatIndexService.removeChatItemsCache(touserItem.getChatlinkid());
            }
        }
        return atom.getRetObj();
    }


    public static WxFriend friendInit(Integer uid, Integer touid, Long msgid, String remark, boolean isEach) {
        WxFriend friend = new WxFriend();
        friend.setUid(uid);
        friend.setStartmsgid(msgid);
        friend.setFrienduid(touid);
        if (StrUtil.isBlank(remark)) {
            User user = UserService.ME.getById(touid);
            friend.setChatindex(PyUtils.getFristChat(user.getNick()));
        } else {
            //放在这么，避免remarkname存在两个默认值：null 和空字符串
            friend.setRemarkname(remark);
            friend.setChatindex(PyUtils.getFristChat(remark));
        }
        int count = friend.ignoreSave();
        if (count < 1) {
            return null;
        }
        WxFriendMeta meta = new WxFriendMeta();
        meta.setUid(uid);
        meta.setTouid(touid);
        meta.setFidkey(UserService.twoUid(uid, touid));
        if (isEach) {
            meta.replaceSave();
        } else {
            meta.ignoreSave();
        }
        ChatIndexService.clearMailListCache(uid);
        return friend;
    }

    public static WxFriend friendInit(Integer uid, Integer touid, Long msgid, String remark) {
        WxFriend friend = new WxFriend();
        friend.setUid(uid);
        friend.setStartmsgid(msgid);
        friend.setFrienduid(touid);
        if (StrUtil.isBlank(remark)) {
            User user = UserService.ME.getById(touid);
            friend.setChatindex(PyUtils.getFristChat(user.getNick()));
        } else {
            friend.setChatindex(PyUtils.getFristChat(remark));
            friend.setRemarkname(remark);
        }

        int count = friend.ignoreSave();
        if (count < 1) {
            return null;
        }
        ChatIndexService.clearMailListCache(uid);
        return friend;
    }

    public static boolean addFriendEachOfPassApply(HttpRequest request, Integer uid, Integer touid, Long chatlinkid, Long toChatLinkid, String greet, boolean isExist)
            throws Exception {
        User touser = UserService.ME.getById(touid);
        String sessionid = request.getHttpSession().getId();
        User user = UserService.ME.getById(uid);
        String ip = request.getClientIp();
        RequestExt ext = (RequestExt) request.getAttribute(RequestKey.REQUEST_EXT);
        String appversion = ext.getAppVersion();
        Short devicetype = ext.getDeviceType();
        WxChatUserItem userItem = ChatIndexService.chatUserIndex(chatlinkid);
        AbsTxAtom atom = new AbsTxAtom() {

            @Override
            public boolean noTxRun() {
                //1、发送验证通过单通道消息
                String text = "成功添加好友";
                DefaultFriends defaultFriend = DefaultFriends.dao.findFirst("select * from default_friends where uid = ?", touid);
                if (defaultFriend != null) {
                    text = defaultFriend.getDefaultMsg();
                }
                WxFriendMsg passMsg = FriendService.me.addChatMsg(devicetype, sessionid, ip, Const.YesOrNo.NO, text, touid, uid, uid, null, appversion);
                if (passMsg == null) {
                    failRet("保存验证消息失败");
                    return false;
                }
                passMsg.setNick(touser.getNick());
                passMsg.setNick(touser.getAvatar());
                retObj = Ret.ok();
                WxFriendChatNtf passNtf = WxFriendChatNtf.from(passMsg);
                passNtf.setChatlinkid(chatlinkid);
                if (!isExist) {
                    passNtf.setActflag(Const.YesOrNo.YES);
                }
                passNtf.setNick(touser.getNick());
                passNtf.setAvatar(touser.getAvatar());
                passNtf.setActavatar(touser.getAvatar());
                passNtf.setActname(touser.getNick());
                retObj.set("pass", passNtf);
                Ret itemRet = ChatMsgService.me.afterSendFriendChatMsg(passMsg, touser, userItem.getChatlinkmetaid(), Const.YesOrNo.NO, Const.YesOrNo.YES, 1);
                if (itemRet.isFail()) {
                    retObj = itemRet;
                    msg = RetUtils.getRetMsg(retObj);
                    return false;
                }
                //2、发送申请的打招呼的聊天内容
                WxFriendMsg applyMsg = FriendService.me.addChatMsg(devicetype, sessionid, ip, Const.YesOrNo.YES, StrUtil.isBlank(greet) ? "你好,我是" + user.getNick() : greet, uid,
                        touid, touid, null, appversion);
                if (applyMsg == null) {
                    return failRet("保存打招呼消息失败");
                }
                applyMsg.setNick(user.getNick());
                applyMsg.setNick(user.getAvatar());
                WxFriendChatNtf applyNtf = WxFriendChatNtf.from(applyMsg);
                applyNtf.setChatlinkid(toChatLinkid);
                applyNtf.setActflag(Const.YesOrNo.YES);
                applyNtf.setNick(touser.getNick());
                applyNtf.setAvatar(touser.getAvatar());
                applyNtf.setActavatar(user.getAvatar());
                applyNtf.setActname(user.getNick());
                retObj.set("apply", applyNtf);

                text = "你已添加了" + user.getNick() + "，现在可以开始聊天了";
                //3、发送自己的系统内容
                WxFriendMsg sysMsg = FriendService.me.addChatMsg(devicetype, sessionid, ip, touid, text, uid, null, null, appversion);
                if (sysMsg == null) {
                    return failRet("保存系统消息失败");
                }
                sysMsg.setNick(user.getNick());
                sysMsg.setNick(user.getAvatar());
                WxFriendChatNtf sysNtf = WxFriendChatNtf.from(sysMsg);
                sysNtf.setChatlinkid(toChatLinkid);
                retObj.set("sys", sysNtf);

                Ret toItemRet = ChatMsgService.me.afterSendFriendChatMsg(sysMsg, touser, userItem.getTochatlinkmetaid(), Const.YesOrNo.NO, Const.YesOrNo.YES, 1);
                if (toItemRet.isFail()) {
                    retObj = toItemRet;
                    msg = RetUtils.getRetMsg(retObj);
                    return false;
                }
                if (!isExist) {
                    boolean startTx = ChatIndexService.me.chatuserStartMsg(uid, new Long(touid), Const.ChatMode.P2P, passMsg.getId());
                    if (!startTx) {
                        return failRet("修改起始消息异常");
                    }
                }
                boolean toStartTx = ChatIndexService.me.chatuserStartMsg(touid, new Long(uid), Const.ChatMode.P2P, passMsg.getId());
                if (!toStartTx) {
                    return failRet("修改好友起始消息异常");
                }
                if (isExist) {
                    //互相新增好友，不存在缓存
                    FriendService.me.putToP2pCache(passMsg, chatlinkid, toChatLinkid);
                    FriendService.me.putToP2pCache(applyMsg, chatlinkid, toChatLinkid);
                    FriendService.me.putToP2pCache(sysMsg, chatlinkid, toChatLinkid);
                }
                return true;
            }
        };
        boolean atomFlag = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
        if (!atomFlag) {
            sendFriendErrorMsg(devicetype, sessionid, ip, touid, uid, touid, chatlinkid, AppCode.FriendErrorCode.SYS_ERROR, atom.getMsg());
            return false;
        }
        Ret atomRet = atom.getRetObj();
        WxFriendChatNtf passNtf = RetUtils.getOkTData(atomRet, "pass");
        ImPacket imPacket = new ImPacket(Command.WxFriendChatNtf, Json.toJson(passNtf));
        Ims.sendToUser(uid, imPacket);
        WxFriendChatNtf applyNtf = RetUtils.getOkTData(atomRet, "apply");
        imPacket = new ImPacket(Command.WxFriendChatNtf, Json.toJson(applyNtf));
        Ims.sendToUser(touid, imPacket);
        WxFriendChatNtf sysNtf = RetUtils.getOkTData(atomRet, "sys");
        imPacket = new ImPacket(Command.WxFriendChatNtf, Json.toJson(sysNtf));
        Ims.sendToUser(touid, imPacket);
        //清除缓存
        ChatIndexService.clearChatUserIndex(uid, new Long(touid), Const.ChatMode.P2P);
        ChatIndexService.clearChatUserIndex(touid, new Long(uid), Const.ChatMode.P2P);
        ChatIndexService.removeChatItemsCache(chatlinkid);
        ChatIndexService.removeChatItemsCache(toChatLinkid);
        if (Const.JPushConfig.OPENFLAG && Objects.equals(passNtf.getSendbysys(), Const.YesOrNo.NO) && !Objects.equals(uid, touid)) {
            Const.getBsExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        PushBizService.me.sendFdMsg(uid, passNtf.getChatlinkid(), user, passNtf.getC());
                    } catch (Exception e) {
                        log.error("", e);
                    }
                }
            });
        }
        return true;
    }


    public static boolean addFriendSigleOfPassApply(HttpRequest request, Integer uid, Integer touid, Long chatlinkid, String greet) throws Exception {
        RequestExt ext = (RequestExt) request.getAttribute(RequestKey.REQUEST_EXT);
        String appversion = ext.getAppVersion();
        Short devicetype = ext.getDeviceType();
        User touser = UserService.ME.getById(touid);
        User user = UserService.ME.getById(uid);
        String sessionid = request.getHttpSession().getId();
        String ip = request.getClientIp();
        WxChatUserItem userItem = ChatIndexService.chatUserIndex(chatlinkid);
        AbsTxAtom atom = new AbsTxAtom() {

            @Override
            public boolean noTxRun() {
                String defaultMsg = DefaultFriends.dao.findFirst("select * from default_friends where uid = ?", touid).getDefaultMsg();
                //1、发送申请的验证的聊天内容
                WxFriendMsg applyMsg = FriendService.me.addChatMsg(devicetype, sessionid, ip, Const.YesOrNo.YES, defaultMsg, uid, touid, touid, null, appversion);
                if (applyMsg == null) {
                    failRet("保存打招呼消息失败");
                    return false;
                }
                retObj = Ret.ok();
                WxFriendChatNtf applyNtf = WxFriendChatNtf.from(applyMsg);
                applyNtf.setChatlinkid(chatlinkid);
                applyNtf.setActflag(Const.YesOrNo.YES);
                applyNtf.setNick(touser.getNick());
                applyNtf.setAvatar(touser.getAvatar());
                applyNtf.setActavatar(user.getAvatar());
                applyNtf.setActname(user.getNick());
                retObj.set("apply", applyNtf);

                //2、发送自己的系统内容
                String text = "你已添加了" + user.getNick() + "，现在可以开始聊天了";
                WxFriendMsg sysMsg = FriendService.me.addChatMsg(devicetype, sessionid, ip, touid, text, uid, null, null, appversion);
                if (sysMsg == null) {
                    failRet("保存系统消息失败");
                    return false;
                }
                WxFriendChatNtf sysNtf = WxFriendChatNtf.from(sysMsg);
                sysNtf.setChatlinkid(chatlinkid);
                retObj.set("sys", sysNtf);
                //更新touid的消息会话
                Ret toItemRet = ChatMsgService.me.afterSendFriendChatMsg(sysMsg, touser, userItem.getChatlinkmetaid(), Const.YesOrNo.NO, Const.YesOrNo.YES, 1);
                if (toItemRet.isFail()) {
                    retObj = toItemRet;
                    msg = RetUtils.getRetMsg(retObj);
                    return false;
                }
                boolean startTx = ChatIndexService.me.chatuserStartMsg(touid, new Long(uid), Const.ChatMode.P2P, applyMsg.getId());
                if (!startTx) {
                    return failRet("修改起始消息异常");
                }
                //自己新增好友也不需要
                //				FriendService.me.putToP2pCache(applyMsg, chatlinkid, null);
                //				FriendService.me.putToP2pCache(sysMsg, chatlinkid, null);
                return true;
            }
        };
        boolean atomFlag = Db.use(Const.Db.TIO_SITE_MAIN).tx(atom);
        if (!atomFlag) {
            sendFriendErrorMsg(devicetype, sessionid, ip, touid, uid, touid, chatlinkid, AppCode.FriendErrorCode.SYS_ERROR, atom.getMsg());
            return false;
        }
        Ret atomRet = atom.getRetObj();
        WxFriendChatNtf applyNtf = RetUtils.getOkTData(atomRet, "apply");
        ImPacket imPacket = new ImPacket(Command.WxFriendChatNtf, Json.toJson(applyNtf));
        Ims.sendToUser(touid, imPacket);
        WxFriendChatNtf sysNtf = RetUtils.getOkTData(atomRet, "sys");
        imPacket = new ImPacket(Command.WxFriendChatNtf, Json.toJson(sysNtf));
        Ims.sendToUser(touid, imPacket);
        //清除缓存
        ChatIndexService.clearChatUserIndex(touid, new Long(uid), Const.ChatMode.P2P);
        ChatIndexService.removeChatItemsCache(chatlinkid);
        return true;
    }


    public static boolean sendFriendErrorMsg(Short devicetype, String sessionid, String ip, Integer senduid, Integer uid, Integer touid, Long chatlinkid, Integer code, String msg) {
        WxFriendErrorNtf errorNtf = new WxFriendErrorNtf();
        errorNtf.setChatlinkid(chatlinkid);
        errorNtf.setCode(code);
        errorNtf.setMsg(msg);
        errorNtf.setTouid(touid);
        errorNtf.setUid(uid);
        WxFriendMsg sysOperMsg = FriendService.me.addChatMsg(devicetype, sessionid, ip, uid, msg, touid, Const.WxSysCode.ERROR_CODE, null, "0.0.0");
        errorNtf.setMid(sysOperMsg.getId());
        errorNtf.setT(sysOperMsg.getTime().getTime());
        ImPacket imPacket = new ImPacket(Command.WxFriendErrorNtf, Json.toJson(errorNtf));
        Ims.sendToUser(senduid, imPacket);
        return true;
    }
}





