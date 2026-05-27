
package org.tio.sitexxx.service.service.base;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import org.apache.commons.text.StringEscapeUtils;
import org.redisson.api.RTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.IAtom;
import org.tio.jfinal.plugin.activerecord.Page;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.jfinal.plugin.activerecord.cache.EhCache;
import org.tio.jfinal.template.TemplateException;
import org.tio.sitexxx.service.api.sms.BaseSmsVo;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.init.RedisInit;
import org.tio.sitexxx.service.model.conf.ClientConf;
import org.tio.sitexxx.service.model.main.*;
import org.tio.sitexxx.service.model.stat.UserIpLoginStat;
import org.tio.sitexxx.service.model.stat.UserTimeLoginStat;
import org.tio.sitexxx.service.service.UserBaseService;
import org.tio.sitexxx.service.service.atom.AbsAtom;
import org.tio.sitexxx.service.service.atom.AbsTxAtom;
import org.tio.sitexxx.service.service.base.sms.SmsService;
import org.tio.sitexxx.service.service.chat.FriendService;
import org.tio.sitexxx.service.service.chat.GroupService;
import org.tio.sitexxx.service.service.conf.AvatarService;
import org.tio.sitexxx.service.service.conf.ConfService;
import org.tio.sitexxx.service.service.stat.StatService;
import org.tio.sitexxx.service.utils.CommonUtils;
import org.tio.sitexxx.service.utils.PeriodUtils;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.*;
import org.tio.sitexxx.service.vo.topic.TopicVo;
import org.tio.utils.cache.CacheUtils;
import org.tio.utils.cache.FirsthandCreater;
import org.tio.utils.cache.ICache;
import org.tio.utils.cache.redis.RedisCache;
import org.tio.utils.hutool.CollUtil;
import org.tio.utils.lock.LockUtils;
import org.tio.utils.resp.Resp;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;

/**
 * @author tanyaowu
 * 2016年8月10日 上午11:09:59
 */
public class UserService {
    private static Logger log = LoggerFactory.getLogger(UserService.class);

    public static final UserService ME = new UserService();

    public static final RoleService roleService = RoleService.me;

    public static final User nullUser = new User();

    //	ICache loginnameAndUserCache;

    //	ICache useridAndUserCache;

    /**
     * @author tanyaowu
     */
    private UserService() {
        //		loginnameAndUserCache = Caches.getCache(CacheConfig.LOGINNAME_USER_1);
        //		useridAndUserCache = Caches.getCache(CacheConfig.USERID_USER_7);
    }

    public static int unreadCount(User curr) {
        List<WxFriendMsg> wxFriendMsgs = WxFriendMsg.dao.find("select * from wx_friend_msg where touid = ? and (contenttype = 16 or contenttype = 17) and readflag = 2", curr.getId());
        if (wxFriendMsgs != null) {
            return wxFriendMsgs.size();
        }
        return 0;
    }

    /**
     * 禁用/启用
     *
     * @param uid
     * @param status
     * @return
     * @author lixinji
     * 2020年6月28日 下午2:36:46
     */
    public Ret disable(Integer uid, Short status) {
        if (uid == null) {
            return RetUtils.invalidParam();
        }
        User user = User.dao.findById(uid);
        if (user == null) {
            return RetUtils.failMsg("用户不存在");
        }
        if (Objects.equals(user.getStatus(), status)) {
            return RetUtils.okOper();
        }
        User update = new User();
        update.setId(user.getId());
        update.setStatus(status);
        boolean ret = update.update();
        if (!ret) {
            return RetUtils.failOper();
        }
        return RetUtils.okOper();
    }


    /**
     * 查询用户信息，主要用于展示给其它人看，所以有的信息是不允许查询出来的
     * 会区分超管和非超管
     *
     * @param currUser
     * @param uid
     * @return
     * @throws Exception
     * @author tanyaowu
     */
    public Record info1(User currUser, Integer uid) throws Exception {
        boolean isSuper = UserService.isSuper(currUser);
        ICache cache = Caches.getCache(CacheConfig.USER_INFO_3);

        String key = String.valueOf(isSuper) + uid;
        boolean putTempToCacheIfNull = true;
        Record record = CacheUtils.get(cache, key, putTempToCacheIfNull, () -> {
            User user = UserService.ME.getById(uid);
            if (user == null) {
                return null;
            }
            Record record1 = new Record();
            record1.set("nick", user.getNick());
            record1.set("id", user.getId());
            record1.set("avatar", user.getAvatar());
            record1.set("avatarbig", user.getAvatarbig());
            record1.set("status", user.getStatus());
            record1.set("beautifulId", user.getBeautifulId());

            //			Kv params = Kv.by("uid", uid);
            //			SqlPara sqlPara = User.dao.getSqlPara("user.searchByUid", params);
            //			Record record = Db.findFirst(sqlPara);

            perfectUserInfo(isSuper, record1);
            UserBase userBase = UserBaseService.me.getUserBaseByUid(uid);
            if (userBase != null) {
                record1.set("sex", userBase.getSex());
                record1.set("sign", userBase.getSign());
            }
            return record1;

        });
        return record;
    }

    /**
     * 获取用户信息，所有人得到的值是一样的，不分超管和普通用户
     *
     * @param uid
     * @return
     * @throws Exception
     * @author tanyaowu
     */
    public Record info(Integer uid) throws Exception {
        return info1(null, uid);
    }

    /**
     * 搜索用户
     *
     * @param currUser
     * @param nick
     * @param uid
     * @param loginname
     * @param pageNumber
     * @param pageSize
     * @return
     * @throws Exception
     * @author tanyaowu
     */
    public Page<Record> search(User currUser, String nick, Integer uid, String loginname, Integer pageNumber, Integer pageSize) throws Exception {
        if (pageNumber == null) {
            pageNumber = 1;
        }
        if (pageSize == null) {
            pageSize = 20;
        }
        Integer pageNumber1 = pageNumber;
        Integer pageSize1 = pageSize;

        boolean isSuper = UserService.isSuper(currUser);
        if (!isSuper) { //非超管不能用登录名查询
            loginname = null;
        }
        Short searchNickFlag = ConfService.getShort("nick.search.flag", Const.YesOrNo.YES);
        Short searchNickLike = ConfService.getShort("nick.search.like.flag", Const.YesOrNo.YES);
        ICache cache = Caches.getCache(CacheConfig.SEARCH_USER);
        Page<Record> page = null;
        Kv params = Kv.by("isSuper", isSuper);
        if (uid != null) { // 根据uid查询
            params.set("uid", uid);
            SqlPara sqlPara = User.dao.getSqlPara("user.searchByUid", params);
            page = Db.paginate(pageNumber, pageSize, sqlPara);
            perfectUserInfo(isSuper, page.getList());
        } else if (StrUtil.isNotBlank(loginname)) {
            params.set("loginname", loginname);
            SqlPara sqlPara = User.dao.getSqlPara("user.searchByLoginname", params);
            page = Db.paginate(pageNumber, pageSize, sqlPara);
            perfectUserInfo(isSuper, page.getList());
        } else if (StrUtil.isNotBlank(nick)) {
            nick = nick.trim();
            if (!isSuper) {
                if (StrUtil.containsAny(nick, "%")) {
                    return null;
                }
                loginname = null; //非超管，不允许使用loginname进行查询
            }

            //多个空格，换成%
            Pattern p = Pattern.compile("\\s+");
            Matcher m = p.matcher(nick);
            String searchNick = m.replaceAll("%");

            String key = isSuper + "_" + pageNumber + "_" + pageSize + "_" + searchNick;
            boolean putTempToCacheIfNull = true;
            page = CacheUtils.get(cache, key, putTempToCacheIfNull, new FirsthandCreater<Page<Record>>() {
                @Override
                public Page<Record> create() throws Exception {
                    params.set("nick", "%" + searchNick + "%").set("search", searchNick).set("uid", searchNick);
                    if (searchNickFlag.equals(Const.YesOrNo.YES)) {
                        if (searchNickLike.equals(Const.YesOrNo.YES)) {
                            params.set("nicklike", searchNickFlag);
                        } else {
                            params.set("nickflag", searchNickFlag);
                        }
                    }
                    Integer type = ClientConf.dao.findFirst("select * from client_conf where name = 'searchType'").getValue();
                    String sql = "user.searchByNick";
                    if (type.equals(2)) {
                        sql = "user.searchByUid";
                    } else if (type.equals(3)) {
                        sql = "user.searchByPhone";
                    }
                    SqlPara sqlPara = User.dao.getSqlPara(sql, params);
                    Page<Record> page = Db.paginate(pageNumber1, pageSize1, sqlPara);

                    perfectUserInfo(isSuper, page.getList());
                    return page;
                }
            });
        } else {
            return null;
        }
        return page;

    }


    /**
     * 完善用户信息
     *
     * @param isSuper
     * @param list
     * @throws Exception
     * @author tanyaowu
     */
    public void perfectUserInfo(boolean isSuper, List<Record> list) throws Exception {
        if (list != null) {
            for (Record record : list) {
                perfectUserInfo(isSuper, record);
            }
        }
    }

    /**
     * 完善用户信息
     *
     * @param isSuper
     * @param record
     * @throws Exception
     * @author tanyaowu
     */
    public void perfectUserInfo(boolean isSuper, Record record) throws Exception {
        if (record == null) {
            return;
        }
        Integer uid = record.getInt("id");
        if (uid != null) {
            User user = UserService.ME.getById(uid);
            record.set("roles", user.getRoles());
            if (user.getIpid() != null) {
                IpInfo ipInfo = IpInfoService.ME.getById(user.getIpid());
                if (ipInfo != null) {
                    record.set("country", ipInfo.getCountry());
                    record.set("province", ipInfo.getProvince());
                    record.set("city", ipInfo.getCity());
                }

                UserThirdOsc userThirdOsc = null;
                if ("5".equals(user.getPwd()) && user.getLoginname().startsWith("__osc_")) {
                    UserThird userThird = UserThirdService.me.getByUid(uid, UserThird.Type.OSC);
                    if (userThird != null) {
                        userThirdOsc = UserThirdOscService.me.getByUserThirdId(userThird.getId());
                        if (userThirdOsc != null) {
                            record.set("osc_url", userThirdOsc.getUrl());
                        }
                    }
                }

                if (isSuper) {
                    if (ipInfo != null) {
                        record.set("ip", ipInfo.getIp());
                    }
                    record.set("createtime", user.getCreatetime());
                    record.set("loginname", user.getLoginname());
                    if (userThirdOsc != null) {
                        record.set("osc_email", userThirdOsc.getEmail());
                    }
                }
            }
        }
    }

    /**
     * 清空用户所有的相关缓存
     *
     * @param uid 如果为null，则清除所有用户的缓存
     * @date 2016年11月20日 下午1:45:19
     */
    public void _clearCache(Integer uid) {
        if (uid == null) {
            Caches.getCache(CacheConfig.USERID_USER_7).clear();
            Caches.getCache(CacheConfig.LOGINNAME_USER_1).clear();
            Caches.getCache(CacheConfig.USER_INFO_3).clear();
            Caches.getCache(CacheConfig.USERID_BASE).clear();
        } else {
            User user = UserService.ME.getById(uid);
            String key = uid + "";
            Caches.getCache(CacheConfig.USERID_USER_7).remove(key);
            if (user != null) {
                if (StrUtil.isNotBlank(user.getPhone())) {
                    String loginkey = "phone*_" + user.getPhone();
                    Caches.getCache(CacheConfig.LOGINNAME_USER_1).remove(loginkey);
                }
                if (StrUtil.isNotBlank(user.getEmail())) {
                    String loginkey = "email*_" + user.getEmail();
                    Caches.getCache(CacheConfig.LOGINNAME_USER_1).remove(loginkey);
                }
                Caches.getCache(CacheConfig.LOGINNAME_USER_1).remove(user.getLoginname());
                if (StrUtil.isNotBlank(user.getTiono())) {
                    Caches.getCache(CacheConfig.TIONO_USER_1).remove(user.getTiono());
                }
            }
            ICache cache = Caches.getCache(CacheConfig.USER_INFO_3);
            cache.remove(String.valueOf(false) + key);
            cache.remove(String.valueOf(true) + key);
            Caches.getCache(CacheConfig.USERID_BASE).remove(key);
        }
    }

    /**
     * 发topic通知清除用户缓存
     *
     * @param uid
     */
    public void notifyClearCache(Integer uid) {
        _clearCache(uid);

        TopicVo topicVo = new TopicVo();
        topicVo.setType(TopicVo.Type.CLEAR_USER);
        topicVo.setValue(uid);
        RTopic topic = RedisInit.get().getTopic(Const.Topic.COMMON_TOPIC);

        //这里设置clientid，避免过滤掉自己
        //有的消息是要过滤自己的，就不能设置此值
        //		topicVo.setClientId("3");
        topic.publish(topicVo);
    }

    /**
     * 所有用户，包括各种状态，调用后根据业务进行状态处理
     *
     * @param id
     * @return
     * @author tanyaowu
     */
    public User getById(Integer id) {
        if (id == null) {
            return null;
        }
        String key = id + "";

        User user1 = CacheUtils.get(Caches.getCache(CacheConfig.USERID_USER_7), key, true, new FirsthandCreater<User>() {
            @Override
            public User create() {
                User user = User.dao.findById(id);

                if (user == null) {
                    return null;
                }

                if (Const.USE_ANONYMOUS) {
                    if (user.getXx() == (short) 1) {
                        user.setAvatar(AVATARS[id % AVATARS.length]);
                    }
                }
                roleService.setRoles(user);

                IpInfo ipInfo = IpInfoService.ME.getById(user.getIpid());
                user.setIpInfo(ipInfo);

                UserThird userThird = UserThirdService.me.getByUid(user.getId(), user.getThirdtype());
                user.setUserThird(userThird);

                if (StrUtil.isBlank(user.getAvatar())) {
                    user.setAvatar(User.DEFAULT_AVATAR);
                }

                if (StrUtil.isBlank(user.getAvatarbig())) {
                    user.setAvatarbig(user.getAvatar());
                }
                UserBase userBase = UserBaseService.me.getUserBaseByUid(user.getId());
                if (userBase != null) {
                    user.setSign(userBase.getSign());
                    user.setSex(userBase.getSex());
                }
                if (Objects.equals(user.getOpenflag(), Const.YesOrNo.YES) && user.getOpenid() != null) {
                    if (Const.PAY_TYPE.equals(PayConst.PayVersionType.PAY_5U)) {
                        WxUserWallet wallet = WxUserWallet.dao.findById(user.getOpenid());
                        if (wallet != null) {
                            user.setWalletid(wallet.getWalletid());
                        }
                    } else {
                        WxWallet wallet = WxWallet.dao.findById(user.getOpenid());
                        if (wallet != null) {
                            user.setWalletid(wallet.getWalletid());
                        }
                        WxWallet subwallet = WxWallet.dao.findById(user.getSubopenid());
                        if (subwallet != null) {
                            user.setSubwalletid(subwallet.getWalletid());
                        }
                    }
                }
                return user;
            }
        });
        return user1;
    }

    /**
     * 根据夜猫号获取用户信息
     *
     * @param tiono
     * @return
     * @author lixinji
     * 2021年4月20日 下午3:55:49
     */
    public User getUserByTiono(String tiono) {
        if (StrUtil.isBlank(tiono)) {
            return null;
        }
        String key = tiono;
        User user1 = CacheUtils.get(Caches.getCache(CacheConfig.TIONO_USER_1), key, true, new FirsthandCreater<User>() {
            @Override
            public User create() {
                User user = User.dao.findFirst("select * from `user` where tiono = ? and `status` = ?", tiono, User.Status.NORMAL);
                return user;
            }
        });
        return user1;
    }

    /**
     * 所有状态下的用户，调用后，根据业务进行处理
     *
     * @param id
     * @return
     * @author: tanyaowu
     */
    public User getById(String id) {
        if (StrUtil.isBlank(id)) {
            return null;
        }

        return getById(Integer.parseInt(id));
    }

    /**
     * 保存用户，会连着保存ip等信息
     *
     * @param user
     * @return
     * @author tanyaowu
     */
    public User save(User user) {
        IpInfo ip = user.getIpInfo();
        ip = IpInfoService.ME.save(ip);

        if (ip != null) {
            user.setIpid(ip.getId());
            boolean ff = user.save();
            if (ff) {
                user.setIpInfo(ip);
                return user;
            }
        }
        return null;
    }

    /**
     * 保存唯一用户
     *
     * @param user
     * @return
     */
    public User userUnionSave(User user) {
        return null;
    }

    /**
     * 获取用户数
     *
     * @return
     */
    public int getUserCount() {
        ICache cache = Caches.getCache(CacheConfig.USER_COUNT);
        String key = org.tio.sitexxx.service.cache.Caches.SINGLE_KEY;

        Integer count = CacheUtils.get(cache, key, true, new FirsthandCreater<Integer>() {
            @Override
            public Integer create() {
                String sql = "select count(*) from user";
                return Db.use(Const.Db.TIO_SITE_MAIN).queryInt(sql);
            }
        });

        return count;
    }

    /**
     * @param user
     * @param paypwd
     * @return
     * @author lixinji
     * 2021年3月12日 上午9:55:48
     */
    public Ret setPayPwd(User user, String paypwd) {
        String sql = "update user set paypwd = ?,paypwdflag = ? where id = ?";
        int c = Db.update(sql, paypwd, Const.YesOrNo.YES, user.getId());
        if (c < 1) {
            log.error("修改无变动：" + "用户设置支付密码失败");
        }
        notifyClearCache(user.getId());
        return RetUtils.okOper();
    }

    /**
     * @param user
     * @param newpaypwd
     * @param initPwd
     * @return
     * @author lixinji
     * 2021年3月12日 上午10:04:59
     */
    public Ret updatePayPwd(User user, String newpaypwd, String initPwd) {
        String md5pwd = getMd5Pwd(user.getPhone(), initPwd);
        if (!Objects.equals(md5pwd, user.getPaypwd())) {
            return RetUtils.failMsg("原密码不正确");
        }
        String sql = "update user set paypwd = ? where id = ?";
        int c = Db.update(sql, newpaypwd, user.getId());
        if (c < 1) {
            log.error("修改无变动：" + "用户修改支付密码失败");
        }
        notifyClearCache(user.getId());
        return RetUtils.okOper();
    }

    /**
     * @param user
     * @param paypwd
     * @return
     * @author lixinji
     * 2021年3月12日 上午10:07:23
     */
    public Resp resetPayPwd(User user, String paypwd) {
        String sql = "update user set paypwd = ?,paypwdflag = ? where id = ?";
        int c = Db.update(sql, paypwd, Const.YesOrNo.YES, user.getId());
        if (c <= 0) {
            log.error("修改无变动：" + "找回密码");
        }
        notifyClearCache(user.getId());
        return Resp.ok();
    }

    /**
     * 根据loginname获取用户
     *
     * @param loginname
     * @param status    如果为null则不限状态
     * @return
     * @author tanyaowu
     */
    public User getByLoginname(String loginname, Short status) {
        ICache loginnameAndUserCache = Caches.getCache(CacheConfig.LOGINNAME_USER_1);
        User user = (User) loginnameAndUserCache.get(loginname);
        if (user == null) {
            user = User.dao.findFirst("select * from user where loginname = ? and `status` <> ?", loginname, org.tio.sitexxx.service.model.main.User.Status.LOGOUT);
            if (user != null) {
                user = getById(user.getId());
            } else {
                user = User.dao.findFirst("select * from user where phone = ? and `status` <> ?", loginname, org.tio.sitexxx.service.model.main.User.Status.LOGOUT);
                if (user != null) {
                    user = getById(user.getId());
                }
            }
            if (user != null) {
                loginnameAndUserCache.put(loginname, user);
            } else {
                loginnameAndUserCache.putTemporary(loginname, nullUser);
            }
        } else {
            //可能是 nullUser
            if (user.getId() == null) {
                return null;
            }
            if (status == null) {
                return user;
            }
            if (!Objects.equals(status, user.getStatus())) {
                return null;
            }
        }
        return user;
    }

    /**
     * @param email
     * @param status
     * @return
     * @author lixinji
     * 2020年12月16日 下午3:45:47
     */
    public User getByEmail(String email, Short status) {
        ICache loginnameAndUserCache = Caches.getCache(CacheConfig.LOGINNAME_USER_1);
        String key = "email*_" + email;
        User user = (User) loginnameAndUserCache.get(key);
        if (user == null) {
            user = User.dao.findFirst("select * from user where email = ? and `status` <> ?", email, org.tio.sitexxx.service.model.main.User.Status.LOGOUT);
            if (user != null) {
                user = getById(user.getId());
            }
            if (user != null) {
                loginnameAndUserCache.put(key, user);
            } else {
                loginnameAndUserCache.putTemporary(key, nullUser);
            }
        } else {
            //可能是 nullUser
            if (user.getId() == null) {
                return null;
            }
            if (status == null) {
                return user;
            }
            if (!Objects.equals(status, user.getStatus())) {
                return null;
            }
        }
        return user;
    }

    /**
     * @param phone
     * @param status
     * @return
     * @author lixinji
     * 2020年12月16日 下午3:45:44
     */
    public User getByPhone(String phone, Short status) {
        ICache loginnameAndUserCache = Caches.getCache(CacheConfig.LOGINNAME_USER_1);
        String key = "phone*_" + phone;
        User user = (User) loginnameAndUserCache.get(key);
        if (user == null) {
            user = User.dao.findFirst("select * from user where phone = ? and `status` <> ?", phone, org.tio.sitexxx.service.model.main.User.Status.LOGOUT);
            if (user != null) {
                user = getById(user.getId());
            }
            if (user != null) {
                loginnameAndUserCache.put(key, user);
            } else {
                loginnameAndUserCache.putTemporary(key, nullUser);
            }
        } else {
            //可能是 nullUser
            if (user.getId() == null) {
                return null;
            }
            if (status == null) {
                return user;
            }
            if (!Objects.equals(status, user.getStatus())) {
                return null;
            }
        }
        return user;
    }

    /**
     * 原始登录
     * 1、loginname不存在
     * 2、密码不正确
     * 3、换aes登录吧
     *
     * @param loginname
     * @param pd5          md5加密的密码
     * @param isThirdLogin
     * @return
     */
    public Ret login(String loginname, String pd5, boolean isThirdLogin) {
        String code = "code";
        if (StrUtil.isBlank(loginname)/** || ( StrUtil.isBlank(pwd) &&  !isThirdLogin) */
        ) {
            return Ret.fail(code, 1);
        }
        User user = getByLoginname(loginname, null);
        if (user == null) {
            log.info("can find user by loginname:【{}】", loginname);
            return Ret.fail(code, 1); //loginname不存在
        }

        if (!isThirdLogin) {
            if (!Objects.equals(pd5, user.getPwd()) && !Objects.equals(pd5, user.getPhonepwd())) {
                log.info("password is invalid, loginname:[{}], md5pwd:[{}], need md5pwd:[{}]", loginname, pd5, user.getPwd());
                return Ret.fail(code, 2); //密码不正确
            }
        }

        return Ret.ok("user", user);
    }

    /**
     * @param loginname
     * @param pd5
     * @param authcode
     * @param isThirdLogin
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年12月16日 下午3:59:32
     */
    public Ret login(String loginname, String pd5, String authcode, boolean isThirdLogin) throws Exception {
        ClientConf clientConf = ClientConf.dao.findFirst("select * from client_conf where name = 'registerType'");
        String code = "code";
        if (StrUtil.isBlank(loginname)) {
            return Ret.fail(code, 1);
        }
        Integer loginType = null;
        if (Validator.isEmail(loginname) && clientConf.getValue().equals(1)) {
            loginType = Const.RegisterType.EMAIL;
        }
        if (Validator.isMobile(loginname) && clientConf.getValue().equals(2)) {
            loginType = Const.RegisterType.PHONE;
        }
        if (loginType == null) {
            return login(loginname, pd5, isThirdLogin);
        }
        User user = null;
        if (Objects.equals(loginType, Const.RegisterType.EMAIL)) {
            user = getByEmail(loginname, null);
            if (user == null) {
                user = getByLoginname(loginname, null);
            }
            if (user == null) {
                log.warn("can find user by loginname:【{}】", loginname);
                return Ret.fail(code, 1); //loginname不存在
            }
            if (!isThirdLogin) {
                if (!Objects.equals(pd5, user.getEmailpwd()) && !Objects.equals(pd5, user.getPwd())) {
                    log.warn("password is invalid, loginname:[{}], md5pwd:[{}], need md5pwd:[{}]", loginname, pd5, user.getEmailpwd());
                    return Ret.fail(code, 2); //密码不正确
                }
            }
        } else {
            user = getByPhone(loginname, null);
            if (user == null) {
                user = getByLoginname(loginname, null);
            }
            if (user == null) {
                log.warn("can find user by loginname:【{}】", loginname);
                return Ret.fail(code, 1); //loginname不存在
            }
            if (user.getUserType().equals(2)) {
                ICache cache = Caches.getCache(CacheConfig.TEMP_IM);
                String key = "tempIM" + "_" + user.getId();
                Map<String, Object> result = CacheUtils.get(cache, key, true, new FirsthandCreater<HashMap<String, Object>>() {
                    @Override
                    public HashMap<String, Object> create() {
                        return null;
                    }
                });
                if (result != null) {
                    return Ret.ok("user", user);
                }
            }

            if (StrUtil.isNotBlank(authcode)) {
                Ret ret = SmsService.me.checkCode(loginname, BaseSmsVo.BaseSmsBizType.LOGIN, authcode, null, true);
                if (ret.isFail()) {
                    return ret.set(code, 2);
                }
            } else {
                if (!isThirdLogin) {
                    if (!Objects.equals(pd5, user.getPhonepwd()) && !Objects.equals(pd5, user.getPwd())) {
                        log.warn("password is invalid, loginname:[{}], md5pwd:[{}], need md5pwd:[{}]", loginname, pd5, user.getPhonepwd());
                        return Ret.fail(code, 2); //密码不正确
                    }
                }
            }
        }
        return Ret.ok("user", user);
    }

    /**
     * 修改用户昵称
     *
     * @param user
     * @param newNick
     * @return
     * @author tanyaowu
     */
    public Resp updateNick(User user, String newNick, String avatarPath) {
        if (newNick.equals(user.getNick())) {
            return Resp.fail("原来的昵称就是这个哦！");
        }
        Resp resp = CommonUtils.checkGroupName(newNick, "昵称");
        if (!resp.isOk()) {
            return resp;
        }

        UserlogModifyNick userlogModifyNick = new UserlogModifyNick();
        userlogModifyNick.setNewnick(newNick);
        userlogModifyNick.setOldnick(user.getNick());
        userlogModifyNick.setUid(user.getId());

        if (StrUtil.isNotBlank(newNick)) {
            Short checkNick = ConfService.getShort("nick.check.flag", Const.YesOrNo.YES);
            if (checkNick.equals(Const.YesOrNo.YES)) {
                Integer check = Db.use(Const.Db.TIO_SITE_MAIN).queryInt("select id from user where nick = ? and id != ? and `status` != ? limit 1", newNick, user.getId(),
                        org.tio.sitexxx.service.model.main.User.Status.LOGOUT);
//				if (check != null) {
//					return Resp.fail("昵称已存在");
//				}
            }
            //			user.setSrcnick(newNick);
            String filterContent = newNick;
            filterContent = SensitiveWordsService.findAndReplace(filterContent);
            filterContent = StringEscapeUtils.escapeHtml4(filterContent);
            user.setNick(filterContent);
        }
        //		String loginname = user.getLoginname();
        int c = -1;
        if (StrUtil.isBlank(avatarPath)) {
            String sql = "update user set nick = ? where id = ?";
            c = Db.update(sql, newNick, user.getId());
        } else {
            String sql = "update user set nick = ?,avatar = ?,avatarbig = ? where id = ?";
            c = Db.update(sql, newNick, avatarPath, avatarPath, user.getId());
        }
        if (c < 1) {
            log.error("修改无变动：" + "用户昵称修改失败");
        }
        notifyClearCache(user.getId());
        userlogModifyNick.save();
        //		initSynInfo(user.getId(), Const.UserToImSynType.NICK, newNick, null);
        //		if (StrUtil.isNotBlank(avatarPath)) {
        //			initSynInfo(user.getId(), Const.UserToImSynType.AVATAR, avatarPath, null);
        //		}
        return Resp.ok().msg("用户昵称修改成功");
    }

    /**
     * 更新头像
     *
     * @param user
     * @param newavatar
     * @param newavatarbig
     * @return
     * @author tanyaowu
     */
    public Resp updateAvatar(User user, String newavatar, String newavatarbig) {
        UserlogModifyAvatar userlogModifyAvatar = new UserlogModifyAvatar();
        userlogModifyAvatar.setNewavatar(newavatar);
        userlogModifyAvatar.setNewavatarbig(newavatarbig);
        userlogModifyAvatar.setOldavatar(user.getAvatar());
        userlogModifyAvatar.setOldavatarbig(user.getAvatarbig());
        userlogModifyAvatar.setUid(user.getId());

        //		String loginname = user.getLoginname();
        String sql = "update user set avatar = ?, avatarbig = ? where id = ?";
        int c = Db.update(sql, newavatar, newavatarbig, user.getId());

        if (c < 1) {
            log.error("修改无变动：" + "用户头像修改失败");
        }
        notifyClearCache(user.getId());

        userlogModifyAvatar.save();
//		initSynInfo(user.getId(), Const.UserToImSynType.AVATAR, newavatar, null);
        return Resp.ok().msg("用户头像修改成功");
    }

    /**
     * 修改验证方式
     *
     * @param user
     * @param fdvalidtype
     * @return
     * @author lixinji
     * 2020年3月3日 下午5:24:48
     */
    public Resp updateFdvalidtype(User user, Short fdvalidtype) {
        String sql = "update user set fdvalidtype = ? where id = ?";
        int c = Db.update(sql, fdvalidtype, user.getId());

        if (c < 1) {
            log.error("修改无变动：" + "用户验证方式失败");
        }
        notifyClearCache(user.getId());
        return Resp.ok().msg("用户验证方式成功");
    }

    /**
     * @param user
     * @param emailpwd
     * @param phonepwd
     * @return
     * @author lixinji
     * 2020年12月16日 下午4:50:31
     */
    public Resp bindPhone(User user, String phone, String phonepwd, String emailpwd, Integer type) {

        String sql = "update user set phone = ?,phonepwd = ?,emailpwd = ?,phonebindflag = ? where id = ?";
        int c = Db.update(sql, phone, phonepwd, emailpwd, Const.YesOrNo.YES, user.getId());
        if (c < 1) {
            log.error("修改无变动：" + "用户绑定失败");
        }
        notifyClearCache(user.getId());
        if (!user.getType().equals(type)) {
            user.setType(type);
            boolean update = user.update();
            if (!update) {
                log.error("/bindPhone 更新用户类型失败 uid: {}", user.getId());
            }
        }
        return Resp.ok().msg("用户绑定成功");
    }

    /**
     * 手机注册绑定邮箱
     *
     * @param user
     * @param phone
     * @param phonepwd
     * @param emailpwd
     * @return
     * @author lixinji
     * 2020年12月24日 下午6:18:27
     */
    public Resp regbindemail(User user, String phone, String phonepwd, String emailpwd) {
        String sql = "update user set phone = ?,phonepwd = ?,phonebindflag = ? where id = ?";
        int c = Db.update(sql, phone, phonepwd, Const.YesOrNo.YES, user.getId());
        if (c < 1) {
            log.error("修改无变动：" + "用户绑定失败");
        }
        notifyClearCache(user.getId());
        return Resp.ok().msg("注册成功");
    }

    /**
     * 三方绑定手机号
     *
     * @param user
     * @param phonepwd
     * @param emailpwd
     * @return
     * @author lixinji
     * 2020年12月17日 下午2:45:15
     */
    public Ret thridBindPhone(User user, User exist, String phone, String typeSplit) {
        if (exist != null) {
            List<UserThird> userThirds = UserThirdService.me.getBindThirds(user.getId(), typeSplit);
            if (CollectionUtil.isEmpty(userThirds)) {
                return RetUtils.failMsg("三方信息不存在");
            }
            for (UserThird old : userThirds) {
                UserThird updateThrid = new UserThird();
                updateThrid.setUid(exist.getId());
                updateThrid.setId(old.getId());
                boolean update = updateThrid.update();
                if (!update) {
                    return RetUtils.failMsg("绑定失败");
                }
                notifyClearCache(old.getId());
            }
            //			Db.use(Const.Db.TIO_SITE_MAIN).update("delete from user where id = ?",user.getId());
            notifyClearCache(exist.getId());
            return RetUtils.okOper().set("login", Const.YesOrNo.YES).set("third", userThirds);
        } else {
            String sql = "update user set phone = ?,phonebindflag = ?,thirdstatus = ? where id = ?";
            int c = Db.update(sql, phone, Const.YesOrNo.YES, Const.YesOrNo.YES, user.getId());
            if (c < 1) {
                log.error("修改无变动：" + "三方用户绑定手机失败");
            }
            notifyClearCache(user.getId());
            return RetUtils.okOper().set("login", Const.YesOrNo.NO);
        }
    }

    /**
     * @param user
     * @param phonepwd
     * @return
     * @author lixinji
     * 2020年12月16日 下午6:18:41
     */
    public Resp bindNewPhone(User user, String phone, String phonepwd, String emailpwd, Integer type) {
        String sql = "update user set phone = ?,phonepwd = ?,emailpwd = ? where id = ?";
        int c = Db.update(sql, phone, phonepwd, emailpwd, user.getId());
        if (c < 1) {
            log.error("修改无变动：" + "用户重新绑定失败");
        }
        notifyClearCache(user.getId());
        if (!user.getType().equals(type)) {
            user.setType(type);
            boolean update = user.update();
            if (!update) {
                log.error("/bindNewPhone 更新用户类型失败 uid: {}", user.getId());
            }
        }
        return Resp.ok().msg("用户重新绑定成功");
    }

    /**
     * 消息提醒设置
     *
     * @param user
     * @param remindflag
     * @return
     * @author lixinji
     * 2020年3月3日 下午6:21:30
     */
    public Resp updateRemind(User user, Short remindflag) {
        String sql = "update user set msgremindflag = ? where id = ?";
        int c = Db.update(sql, remindflag, user.getId());

        if (c < 1) {
            log.error("修改无变动：" + "用户消息提醒设置失败");
        }
        notifyClearCache(user.getId());
        return Resp.ok().msg("用户消息提醒设置成功");
    }

    /**
     * 修改电话
     *
     * @param user
     * @param searchflag
     * @return
     * @author lixinji
     * 2020年3月3日 下午6:24:15
     */
    public Resp updatePhone(User user, String phone) {
        if (phone == null) {
            phone = "";
        }
        String sql = "update user_base set phone = ? where uid = ?";
        int c = Db.update(sql, phone, user.getId());
        if (c < 1) {
            log.error("修改无变动：" + "用户修改电话失败");
        }
        notifyClearCache(user.getId());
        return Resp.ok().msg("用户修改电话成功");
    }

    /**
     * 修改签名
     *
     * @param user
     * @param sign
     * @return
     * @author lixinji
     * 2020年3月3日 下午6:30:39
     */
    public Resp updateSign(User user, String sign) {
        if (sign == null) {
            sign = "";
        }
        String sql = "update user_base set sign = ? where uid = ?";
        int c = Db.update(sql, sign, user.getId());
        if (c < 1) {
            log.error("修改无变动：" + "用户修改签名失败");
        }
        notifyClearCache(user.getId());
        return Resp.ok().msg("用户修改签名成功");
    }

    /**
     * 修改用户
     *
     * @param curr
     * @param update
     * @return
     * @author lixinji
     * 2020年4月23日 上午11:06:07
     */
    public Resp updateUser(User curr, User update) {
        if (update == null) {
            return RetUtils.getInvalidResp();
        }
        if (StrUtil.isNotBlank(update.getNick())) {

            Short checkNick = ConfService.getShort("nick.check.flag", Const.YesOrNo.YES);
            if (checkNick.equals(Const.YesOrNo.YES)) {
                Integer check = Db.use(Const.Db.TIO_SITE_MAIN).queryInt("select id from user where nick = ? and id != ? and `status` != ? limit 1", update.getNick(), curr.getId(),
                        org.tio.sitexxx.service.model.main.User.Status.LOGOUT);
//				if (check != null) {
//					return Resp.fail("昵称已存在");
//				}
            }
            //			user.setSrcnick(newNick);
            String filterContent = update.getNick();
            filterContent = SensitiveWordsService.findAndReplace(filterContent);
            filterContent = StringEscapeUtils.escapeHtml4(filterContent);
            update.setNick(filterContent);
        }
        AbsTxAtom absTxAtom = new AbsTxAtom() {

            @Override
            public boolean noTxRun() {
                Kv param = Kv.by("uid", curr.getId());
                if (StrUtil.isBlank(update.getSign())) {
                    param.set("sign", "");
                } else {
                    param.set("sign", update.getSign());
                }
                if (update.getSex() != null) {
                    param.set("sex", update.getSex());
                }
                SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("user.updateBase", param);
                Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
                update.setId(curr.getId());
                return update.update();
            }
        };
        boolean atom = Db.use(Const.Db.TIO_SITE_MAIN).tx(absTxAtom);
        if (!atom) {
            return Resp.fail("用户修改失败");
        }
        notifyClearCache(curr.getId());
        //		if(StrUtil.isNotBlank(update.getNick()) && !curr.getNick().equals(update.getNick())) {
        //			initSynInfo(update.getId(), Const.UserToImSynType.NICK, update.getNick(), null);
        //		}
        //		if (StrUtil.isNotBlank(update.getAvatar()) && !curr.getAvatar().equals(update.getAvatar())) {
        //			initSynInfo(update.getId(), Const.UserToImSynType.AVATAR, update.getAvatar(), null);
        //		}
        return Resp.ok().msg("用户修改成功");
    }

    /**
     * 性别修改
     *
     * @param user
     * @param sex
     * @return
     * @author lixinji
     * 2020年3月3日 下午6:33:38
     */
    public Resp updateSex(User user, Short sex) {
        if (sex == null) {
            sex = Const.UserSex.MALE;
        }
        if (!Objects.equals(user.getSex(), sex)) {
            try {
                if (user.getAvatar().trim().indexOf("/avatar/tio/") == 0) {
                    String avatar = AvatarService.nextAvatar(sex + "");
                    String sql = "update user set avatar = ?, avatarbig = ? where id = ?";
                    Db.update(sql, avatar, avatar, user.getId());
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        String sql = "update user_base set sex = ? where uid = ?";
        int c = Db.update(sql, sex, user.getId());
        if (c < 1) {
            log.error("修改无变动：" + "用户修改性别失败");
        }
        notifyClearCache(user.getId());
        return Resp.ok().msg("用户修改性别成功");
    }

    /**
     * @param user
     * @param searchflag
     * @return
     * @author lixinji
     * 2020年3月3日 下午6:25:59
     */
    public Resp updateSearchFlag(User user, Short searchflag) {
        String sql = "update user set searchflag = ? where id = ?";
        int c = Db.update(sql, searchflag, user.getId());
        if (c < 1) {
            log.error("修改无变动：" + "用户设置别人搜索开关失败");
        }
        notifyClearCache(user.getId());
        return Resp.ok().msg("用户设置别人搜索开关成功");
    }

    /**
     * 修改密码
     *
     * @param user
     * @param initPwd 用户输入的原密码
     * @param newPwd
     * @return
     * @author tanyaowu
     */
    public Resp updatePwd(User user, String initPwd, String newPwd, String emailpwd) {
        String md5LoginnamePwd = getMd5Pwd(user.getLoginname(), initPwd);
        String md5PhonePwd = getMd5Pwd(user.getPhone(), initPwd);
        if (StrUtil.isNotBlank(emailpwd)) {
            String md5pwd = getMd5Pwd(user.getEmail(), initPwd);
            if (!Objects.equals(md5pwd, user.getEmailpwd())) {
                return Resp.fail("原密码不正确");
            }
        } else {
            if (!Objects.equals(md5PhonePwd, user.getPhonepwd())) {
                return Resp.fail("原密码不正确");
            }
            if (!Objects.equals(md5LoginnamePwd, user.getPwd())) {
                return Resp.fail("原密码不正确");
            }
        }
        //		String newmd5pwd = newPwd;//getMd5Pwd(loginname, newPwd);
        String sql = "update user set phonepwd = ?,emailpwd = ?,pwd = ? where id = ?";
        int c = Db.update(sql, newPwd, emailpwd, newPwd, user.getId());
        if (c <= 0) {
            log.error("修改无变动：" + "修改密码");
        }
        ICache loginnameAndUserCache = Caches.getCache(CacheConfig.LOGINNAME_USER_1);
        String key = "phone*_" + user.getPhone();
        loginnameAndUserCache.remove(key);
        notifyClearCache(user.getId());
        return Resp.ok();
    }

    public static void main(String[] args) {
        System.out.println(getMd5Pwd("15937338681", "123456"));
        System.out.println(getMd5Pwd("15937338681", "1234567"));

    }

    /**
     * @param user
     * @param phonepwd
     * @param emailpwd
     * @return
     * @author lixinji
     * 2020年12月17日 上午10:31:28
     */
    public Resp resetPwd(User user, String phonepwd, String emailpwd) {
        String sql = "update user set phonepwd = ?,emailpwd = ?,pwd = ? where id = ?";
        int c = Db.update(sql, phonepwd, emailpwd, phonepwd, user.getId());
        if (c <= 0) {
            log.error("修改无变动：" + "找回密码");
        }
        notifyClearCache(user.getId());
        return Resp.ok();
    }

    /**
     * @param loginname
     * @param roleid
     * @return
     * @author lixinji
     * 2020年12月17日 上午10:50:12
     */
    public Resp addRoleByLoginname(String loginname, short roleid) {
        User user = getByLoginname(loginname, org.tio.sitexxx.service.model.main.User.Status.NORMAL);
        if (user == null) {
            return Resp.fail("用户不存在或状态不正常");
        }
        return addRole(user, roleid);
    }

    public Resp addRoleByNick(String nick, short roleid) {
        User user = getByNick(nick);
        if (user == null) {
            return Resp.fail("昵称不存在");
        }

        if (!Objects.equals(user.getStatus(), org.tio.sitexxx.service.model.main.User.Status.NORMAL)) {
            return Resp.fail("用户状态不对");
        }

        return addRole(user, roleid);
    }

    /**
     * 给某用户添加角色
     *
     * @param loginname
     * @param roleid
     * @return
     * @author tanyaowu
     */
    public Resp addRole(User user, short roleid) {
        if (user == null) {
            return Resp.fail("用户为空");
        }

        boolean f = Db.use(Const.Db.TIO_SITE_MAIN).tx(new IAtom() {
            @Override
            public boolean run() throws SQLException {
                //先删除
                removeRole(user, roleid, false);

                //再添加
                String sql = "insert into user_role (uid, rid, status) values (?,?,?)";
                Db.use(Const.Db.TIO_SITE_MAIN).update(sql, user.getId(), roleid, 1);

                return true;
            }

        });

        if (f) {
            notifyClearCache(user.getId());
            return Resp.ok();
        } else {
            return Resp.fail("操作失败");
        }
    }

    /**
     * 注销
     * 1、修改用户的状态为无效
     * 2、我-删除好友
     * 3、我-退出群聊
     * 4、修改所有关联的好友会话为注销会话-此处需要修改-判断是否为好友的所有逻辑-判断是否进行弹窗提醒
     * 5、好友通讯录-要判断对方的状态
     *
     * @param user
     * @return
     * @author lixinji
     * 2021年2月26日 下午4:52:33
     */
    public Ret logout(User user, Integer ipid) {
        if (user.getId() == null) {
            return RetUtils.failMsg("用户id为空");
        }
        User old = getById(user.getId());
        if (old == null || Objects.equals(old.getStatus(), User.Status.LOGOUT)) {
            return RetUtils.failMsg("用户已注销或不存在");
        }
        Integer uid = old.getId();
        if (Objects.equals(old.getFdvalidtype(), Const.YesOrNo.NO)) {
            User update = new User();
            update.setId(uid);
            update.setFdvalidtype(Const.YesOrNo.YES);
            update.update();
            notifyClearCache(old.getId());
        }
        try {
            List<Ret> friendList = new ArrayList<Ret>();
            List<Ret> groupList = new ArrayList<Ret>();
            Ret friendRet = FriendService.me.fdList(old, "");
            List<Record> myFriends = RetUtils.getOkTData(friendRet);
            Ret groupRet = GroupService.me.groupList(uid, "");
            List<Record> myGroups = RetUtils.getOkTData(groupRet);
            if (CollectionUtil.isNotEmpty(myFriends)) {
                for (Record record : myFriends) {
                    try {
                        //好友
                        Integer touid = record.getInt("frienduid");
                        Ret ret = FriendService.me.delFriend(old, touid, ipid);
                        if (ret.isOk()) {
                            ret.set("touid", touid);
                            if (!Objects.equals(touid, uid)) {
                                friendList.add(ret);
                            }
                        }
                    } catch (Exception e) {
                        log.error("注销删除好友异常-已忽略");
                        log.error("", e);
                        continue;
                    }
                }
            }
            if (CollectionUtil.isNotEmpty(myGroups)) {
                for (Record record : myGroups) {
                    try {
                        //退群
                        Long groupid = record.getLong("groupid");
                        Ret ret = GroupService.me.leaveGroup(old, groupid);
                        if (ret.isOk()) {
                            ret.set("groupid", groupid);
                            groupList.add(ret);
                        }
                    } catch (Exception e) {
                        log.error("注销删除群异常-已忽略");
                        log.error("", e);
                        continue;
                    }
                }
            }
            Map<String, List<Ret>> retMap = new HashMap<String, List<Ret>>();
            retMap.put("friend", friendList);
            retMap.put("group", groupList);
            User update = new User();
            update.setId(uid);
            update.setStatus(User.Status.LOGOUT);
            update.setDelsign(uid);
            update.update();
            notifyClearCache(old.getId());
            return RetUtils.okData(retMap);
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        return RetUtils.failOper();
    }

    /**
     * 删除角色
     *
     * @param user
     * @param roleid
     * @return
     */
    public Resp removeRole(User user, short roleid) {
        return removeRole(user, roleid, true);
    }

    public Resp removeRole(User user, short roleid, boolean clearCache) {
        String sql = "delete from user_role where uid = ? and rid = ?";
        Db.use(Const.Db.TIO_SITE_MAIN).update(sql, user.getId(), roleid);
        if (clearCache) {
            notifyClearCache(user.getId());
        }

        return Resp.ok();
    }

    /**
     * 获取所有有可能侵权的头像
     *
     * @return
     * @author lixinji
     * 2020年1月15日 下午1:48:42
     */
    public List<Record> getTortAvatarUser() {
        String sql = "select u.id,nick,avatar,b.sex from `user` u INNER JOIN user_base b on u.id = b.uid where avatar not like '/user/avatar/%' and avatar not like 'http%'";
        List<Record> users = Db.use(Const.Db.TIO_SITE_MAIN).find(sql);
        return users;
    }

    /**
     * 获取md5加密密码
     *
     * @param loginname 登录名
     * @param plainpwd  明文密码
     * @return
     */
    public static String getMd5Pwd(String loginname, String plainpwd) {
        String pwd = SecureUtil.md5("${" + StrUtil.trim(loginname) + "}" + StrUtil.trim(plainpwd));
        return pwd;
    }

    /**
     * 修改用户地址
     *
     * @param uid
     * @param userAddress
     * @return
     */
    public Ret updateUserAddress(Integer uid, UserAddress userAddress) {
        if (userAddress == null || uid == null) {
            return Ret.fail().set("msg", "无效参数");
        }
        userAddress.setUid(uid);
        userAddress.update();
        return Ret.ok().set("data", userAddress);
    }

    public static final String[] AVATARS = new String[]{"/img/avatar/1.png", "/img/avatar/10171119181614.jpg", "/img/avatar/1106070_jc1127.png", "/img/avatar/1168934_100.jpeg",
            "/img/avatar/1485256_wooxz.png", "/img/avatar/2.jpg", "/img/avatar/20171118121406.png", "/img/avatar/20171118122407.jpg", "/img/avatar/20171118124108.jpg",
            "/img/avatar/20171118124247.png", "/img/avatar/20171118124927.jpg", "/img/avatar/20171118125112.jpg", "/img/avatar/20171118125630.jpg",
            "/img/avatar/20171118181652.jpg", "/img/avatar/20171119182050.jpg", "/img/avatar/20180416154203.jpg", "/img/avatar/20180416155040.jpg",
            "/img/avatar/20180429093933.jpg", "/img/avatar/2232696_talent-iofan.png", "/img/avatar/302580_wu1g119.png", "/img/avatar/3196787_100.jpg",
            "/img/avatar/3440734_100.jpeg", "/img/avatar/3802362_50.jpeg", "/img/avatar/463940_hehui082452239.jpg", "/img/avatar/512121_SJRSB.png", "/img/avatar/556878_100.gif",
            "/img/avatar/636232_meallon.png", "/img/avatar/87d66e45edd0274fe9c29b8cb54f9258_1.jpg", "/img/avatar/a.jpg", "/img/avatar/beimi.jpeg", "/img/avatar/cc.png",
            "/img/avatar/eee.png", "/img/avatar/fds.png", "/img/avatar/fdsse.png", "/img/avatar/gopush.jpeg", "/img/avatar/l.png", "/img/avatar/liyus.jpg",
            "/img/avatar/luxiaolei.jpg", "/img/avatar/orpherus.jpg", "/img/avatar/qbug.png", "/img/avatar/shts.jpg", "/img/avatar/springForAll.png", "/img/avatar/xianxin.jpg",
            "/img/avatar/zhishu.png"};

    /**
     * 是否是超管，上帝视角
     *
     * @param user
     * @return
     */
    public static final boolean isSuper(User user) {
        if (user == null) {
            return false;
        }
        return UserRoleService.hasRole(user, Role.ADMIN_SUPER);
    }

    /**
     * 是否是超管，上帝视角
     *
     * @param user
     * @return
     */
    public static final boolean isSuper(Integer uid) {
        User user = UserService.ME.getById(uid);
        return isSuper(user);
    }

    /**
     * 根据用户昵称修改用户状态 (status)
     *
     * @param nick
     * @param newStatus
     * @return
     */
    public int updateUserStatus(String nick, short newStatus) {
        if (StrUtil.isBlank(nick)) {
            return 0;
        }

        Integer uid = getUidByNick(nick);
        return updateUserStatus(uid, newStatus);
    }

    /**
     * 根据uid修改用户状态 (status)
     *
     * @param uid
     * @param newStatus
     * @return
     */
    public int updateUserStatus(Integer uid, short newStatus) {
        if (uid == null) {
            return 0;
        }
        String sql = "update user set status = ? where id = ?";
        int c = Db.update(sql, newStatus, uid);
        if (c <= 0) {
            log.error("修改无变动：" + "修改用户状态");
        }
        notifyClearCache(uid);
        return c;
    }

    /**
     * 根据用户昵称拉黑用户
     *
     * @param nick
     * @return
     * @throws SQLException
     */
    public int pullBlackUserByNick(String nick) throws SQLException {
        Integer uid = getUidByNick(nick);
        return pullBlackUserByUid(uid);
    }

    /**
     * 根据uid把用户拉黑
     *
     * @param uid
     * @return
     * @throws SQLException
     */
    public int pullBlackUserByUid(Integer uid) throws SQLException {
        if (uid != null) {
            AbsAtom atom = new AbsAtom() {
                @Override
                public boolean run() throws SQLException {
                    updateUserStatus(uid, User.Status.INBLACK);
                    return true;
                }
            };

            boolean flag = atom.run();

            if (flag) {
                return 1;
            }
        }
        return 0;
    }

    /**
     * 根据用户昵称将用户的状态改为正常
     *
     * @param nick
     * @return
     */
    public int normalUserByNick(String nick) {
        Integer uid = getUidByNick(nick);
        return normalUserByUid(uid);
    }

    /**
     * 根据uid将用户的状态改为正常
     *
     * @param uid
     * @return
     */
    public int normalUserByUid(Integer uid) {
        return updateUserStatus(uid, org.tio.sitexxx.service.model.main.User.Status.NORMAL);
    }

    /**
     * 根据昵称找到uid
     *
     * @param nick
     * @return
     */
    public Integer getUidByNick(String nick) {
        if (StrUtil.isBlank(nick)) {
            return null;
        }
        ICache cache = Caches.getCache(CacheConfig.TIME_TO_LIVE_SECONDS_5);
        String key = "user_nick_id" + RedisCache.SPLIT_FOR_CACHENAME + nick;
        Integer uid = CacheUtils.get(cache, key, true, new FirsthandCreater<Integer>() {
            @Override
            public Integer create() {
                String sql = "select id from user where nick = ?";
                return Db.use(Const.Db.TIO_SITE_MAIN).queryInt(sql, nick);
            }
        });
        return uid;
    }

    public User getByNick(String nick) {
        Integer uid = getUidByNick(nick);
        if (uid != null) {
            return getById(uid);
        }
        return null;
    }

    /**
     * 根据ipid获取这个用户可能是谁
     *
     * @param ipid
     * @return
     */
    public Integer getUidByIpid(Integer ipid) {
        if (ipid == null) {
            return null;
        }
        String sql = "SELECT uid FROM `tio_token_path_access_stat` where ipid=? and uid is not null order by firstAccessTime desc LIMIT 0, 1";
        return Db.use(Const.Db.TIO_SITE_STAT).queryInt(sql, ipid);
    }

    /**
     * 完善一下评论列表/博客列表等中的个人信息
     *
     * @param record
     * @param ext
     * @param uidKey
     */
    public static void completeUser(Record record, Map<String, Object> ext, String uidKey) {
        Integer uid = record.getInt(uidKey);
        if (uid != null) {
            String uidStr = org.tio.utils.hutool.StrUtil.int2Str(uid);
            if (ext.get(uidStr) != null) {
                return;
            }
            User user = UserService.ME.getById(uid);
            if (user != null) {
                Kv kv = Kv.by("nick", user.getNick()).set("avatar", user.getAvatar()).set("roles", user.getRoles());
                ext.put(uidStr, kv);
            }
        }
    }

    /**
     * 完善一下评论列表/博客列表等中的个人信息
     *
     * @param list
     * @param ext
     * @param uidKey
     */
    public static void completeUser(List<Record> list, Map<String, Object> ext, String uidKey) {
        if (list != null) {
            for (Record record : list) {
                UserService.completeUser(record, ext, uidKey);
            }
        }
    }

    /**
     * 完善一下评论列表/博客列表等中的个人信息
     *
     * @param page
     * @param uidKey
     */
    public static void completeUser(Page<Record> page, String uidKey) {
        List<Record> list = page.getList();
        if (list != null) {
            Map<String, Object> ext = new HashMap<>();
            page.setExt(ext);
            for (Record record : list) {
                UserService.completeUser(record, ext, uidKey);
            }
        }
    }

    /**
     * 用户是否包含某种角色
     *
     * @param user
     * @param code
     * @return
     */
    public static boolean hasRole(User user, Short code) {
        return UserRoleService.hasRole(user, code);
    }

    /**
     * 根据登录名或昵称获取用户
     *
     * @param loginname
     * @param nick
     * @return
     */
    public static User getByLoginnameOrNick(String loginname, String nick) {
        User user = null;
        if (StrUtil.isNotBlank(loginname)) {
            user = UserService.ME.getByLoginname(loginname, Const.Status.NORMAL);
        } else {
            user = UserService.ME.getByNick(nick);
        }
        return user;
    }

    /**
     * 获取两个userid的key
     *
     * @param uid1
     * @param uid2
     * @return
     */
    public static String twoUid(Integer uid1, Integer uid2) {
        return Math.max(uid1, uid2) + "_" + Math.min(uid1, uid2);
    }

    public static List<Integer> robots = null;

    public void initRobot() {
        if (robots != null) {
            return;
        }

        try {
            LockUtils.runWriteOrWaitRead(UserService.class.getName() + ".initRobot", this, () -> {
                if (robots != null) {
                    return;
                }
                int maxId = 23356;
                int count = 2000;
                int start = 0;
                List<Integer> list = Db.use(Const.Db.TIO_SITE_MAIN).query("select id from user where id <= ? limit ?,?", maxId, start, count);
                robots = list;

            });
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * 用户信息同步数据
     *
     * @param uid
     * @param type
     * @param bizStr
     * @author lixinji
     * 2020年3月16日 上午10:28:08
     */
    public void initSynInfo(Integer uid, Short type, String bizStr, Long bizbigint) {
        Const.getBsExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    UserInfoSyn infoSyn = new UserInfoSyn();
                    switch (type) {
                        case Const.UserToImSynType.NICK:
                            infoSyn.setBizstr(bizStr);
                            break;
                        case Const.UserToImSynType.AVATAR:
                            infoSyn.setBizstr(bizStr);
                            break;
                        case Const.UserToImSynType.GROUP_NICK:
                            infoSyn.setBizstr(bizStr);
                            infoSyn.setBizbigint(bizbigint);
                            break;
                        default:
                            break;
                    }
                    infoSyn.setType(type);
                    infoSyn.setUid(uid);
                    infoSyn.replaceSave();
                } catch (Exception e) {
                    log.error(e.toString(), e);
                }
            }
        });
    }

    /**
     * 用户登录统计-存在内存溢出风险-后续如果登录人数很多，可进行优化
     *
     * @param date
     * @author lixinji
     * 2020年7月16日 下午2:56:47
     */
    public void loginTimeStat(Date date) {
        DateTime dateTime = DateUtil.offsetDay(date, -1);
        String dayPeriod = PeriodUtils.dateToPeriodByType(dateTime, Const.PeriodType.DAY);
        List<Record> records = Db.use(Const.Db.TIO_SITE_MAIN).find("select dayperiod,count(DISTINCT uid) uidcount,count(1) logincount from login_log where dayperiod =?",
                dayPeriod);
        if (CollectionUtil.isEmpty(records)) {
            return;
        }
        int usercount = records.get(0).getInt("uidcount");
        int totalcount = records.get(0).getInt("logincount");
        if (usercount == 0 || totalcount == 0) {
            return;
        }
        UserTimeLoginStat daystat = new UserTimeLoginStat();
        daystat.setUid(-1);
        daystat.setDayperiod(dayPeriod);
        daystat.setUsercount(usercount);
        daystat.setTotalcount(totalcount);
        daystat.setRemark("天统计");
        daystat.replaceSave();
        List<Record> userLogins = Db.use(Const.Db.TIO_SITE_MAIN).find("select  uid,count(1) logincount from login_log where dayperiod = ? group by uid", dayPeriod);
        if (CollectionUtil.isEmpty(userLogins)) {
            log.error("登录统计异常：用户登录记录不存在");
            return;
        }
        for (Record userLogin : userLogins) {
            Integer uid = userLogin.getInt("uid");
            int count = userLogin.getInt("logincount");
            List<Record> hourLogins = Db.use(Const.Db.TIO_SITE_MAIN)
                    .find("select hourperiod,count(1) hourcount from login_log where dayperiod = ?  and uid = ? group by hourperiod", dayPeriod, uid);
            if (CollectionUtil.isEmpty(hourLogins)) {
                log.error("登录统计异常：用户登录记录不存在-小时");
                continue;
            }
            UserTimeLoginStat userStat = new UserTimeLoginStat();
            userStat.setUid(uid);
            userStat.setDayperiod(dayPeriod);
            userStat.setTotalcount(count);
            for (Record hourLogin : hourLogins) {
                dateToLoginStat(userStat, hourLogin.getStr("hourperiod"), hourLogin.getInt("hourcount"));
            }
            userStat.replaceSave();
            //用户总统计
            UserTimeLoginStat record = UserTimeLoginStat.dao.findFirst("select  * from user_time_login_stat where uid = ? and dayperiod = -1", uid);
            if (record == null) {
                record = new UserTimeLoginStat();
                record.setUid(uid);
                record.setDayperiod("-1");
                record.setUsercount(1);
                record.setTotalcount(count);
                record.setRemark("用户总统计");
                record.replaceSave();
            } else {
                Db.use(Const.Db.TIO_SITE_STAT).update("update user_time_login_stat set totalcount = totalcount + ? where id = ? ", count, record.getId());
            }
        }
    }

    /**
     * 单词登录时间统计
     *
     * @param loginLog
     * @author lixinji
     * 2020年7月30日 下午3:15:46
     */
    public void singleLoginTimeStat(LoginLog loginLog) {
        String dayPeriod = PeriodUtils.dateToPeriodByType(loginLog.getTime(), Const.PeriodType.DAY);
        //用户天统计
        Integer uid = loginLog.getUid();
        boolean userInit = false;
        UserTimeLoginStat userStat = UserTimeLoginStat.dao.findFirst("select  * from user_time_login_stat where uid = ? and dayperiod = ?", uid, dayPeriod);
        if (userStat == null) {
            userStat = new UserTimeLoginStat();
            userStat.setUid(uid);
            userStat.setDayperiod(dayPeriod);
            userStat.setTotalcount(0);
            userStat.setUsercount(1);
            int save = userStat.ignoreSave();
            if (save <= 0) {
                userInit = true;
            }
        } else {
            userInit = true;
        }
        String hourPeriod = loginLog.getHourperiod();
        Db.use(Const.Db.TIO_SITE_STAT).update("update user_time_login_stat set totalcount = totalcount + 1,hour" + Integer.parseInt(hourPeriod) + " = hour"
                + Integer.parseInt(hourPeriod) + " + 1 where uid = ? and dayperiod = ?", uid, dayPeriod);
        //用户总统计
        UserTimeLoginStat record = UserTimeLoginStat.dao.findFirst("select  * from user_time_login_stat where uid = ? and dayperiod = -1", uid);
        if (record == null) {
            record = new UserTimeLoginStat();
            record.setUid(uid);
            record.setDayperiod("-1");
            record.setUsercount(1);
            record.setTotalcount(1);
            record.setRemark("用户总统计");
            if (record.ignoreSave() <= 0) {
                Db.use(Const.Db.TIO_SITE_STAT).update("update user_time_login_stat set totalcount = totalcount + 1 where uid = ? and dayperiod = -1", uid);
            }
        } else {
            Db.use(Const.Db.TIO_SITE_STAT).update("update user_time_login_stat set totalcount = totalcount + 1 where id = ? ", record.getId());
        }
        //天总统计
        UserTimeLoginStat daystat = UserTimeLoginStat.dao.findFirst("select  * from user_time_login_stat where uid = -1 and dayperiod = ?", dayPeriod);
        if (daystat == null) {
            daystat = new UserTimeLoginStat();
            daystat.setUid(-1);
            daystat.setDayperiod(dayPeriod);
            daystat.setUsercount(1);
            daystat.setTotalcount(1);
            daystat.setRemark("天统计");
            if (daystat.ignoreSave() <= 0) {
                if (userInit) {
                    Db.use(Const.Db.TIO_SITE_STAT).update("update user_time_login_stat set totalcount = totalcount + 1 where uid = -1 and dayperiod = ?", dayPeriod);
                } else {
                    Db.use(Const.Db.TIO_SITE_STAT).update("update user_time_login_stat set totalcount = totalcount + 1,usercount = usercount + 1 where uid = -1 and dayperiod = ?",
                            dayPeriod);
                }
            }
        } else {
            if (userInit) {
                Db.use(Const.Db.TIO_SITE_STAT).update("update user_time_login_stat set totalcount = totalcount + 1 where id = ?", daystat.getId());
            } else {
                Db.use(Const.Db.TIO_SITE_STAT).update("update user_time_login_stat set totalcount = totalcount + 1,usercount = usercount + 1 where id = ?", daystat.getId());
            }
        }

    }

    /**
     * @param date
     * @author lixinji
     * 2020年7月16日 下午3:44:56
     */
    public void loginIpStat(Date date) {
        DateTime dateTime = DateUtil.offsetDay(date, -1);
        String dayPeriod = PeriodUtils.dateToPeriodByType(dateTime, Const.PeriodType.DAY);
        //总统计数
        List<Record> records = Db.use(Const.Db.TIO_SITE_MAIN)
                .find("select ip,ipid,count(1) totalcount,count(DISTINCT uid) uidcount from login_log where dayperiod = ? GROUP BY ip,ipid", dayPeriod);
        if (CollectionUtil.isEmpty(records)) {
            return;
        }
        for (Record ipRecord : records) {
            String ip = ipRecord.getStr("ip");
            Integer ipid = ipRecord.getInt("ipid");
            int uidcount = ipRecord.getInt("uidcount");
            int totalcount = ipRecord.getInt("totalcount");
            //ip统计数是否存在
            UserIpLoginStat ipstat = UserIpLoginStat.dao.findFirst("select * from user_ip_login_stat where ip = ? and uid = -1 and dayperiod = '-1'", ip);
            boolean isInit = false;
            if (ipstat == null) {
                isInit = true;
                ipstat = new UserIpLoginStat();
                ipstat.setIp(ip);
                ipstat.setIpid(ipid);
                ipstat.setUsercount(uidcount);
                ipstat.setTotalcount(totalcount);
                ipstat.setDayperiod("-1");
                ipstat.setUid(-1);
                ipstat.replaceSave();
            }
            //ip下的用户列表
            List<Record> useRecords = Db.use(Const.Db.TIO_SITE_MAIN).find("select uid,count(1) logincount from login_log where dayperiod = ? and ip = ? GROUP BY uid", dayPeriod,
                    ip);
            if (CollectionUtil.isEmpty(useRecords)) {
                log.error("登录统计异常：用户登录记录不存在-ip");
                return;
            }
            //莫一天的统计
            UserIpLoginStat daystat = new UserIpLoginStat();
            daystat.setUid(-1);
            daystat.setDayperiod(dayPeriod);
            daystat.setIp(ip);
            daystat.setIpid(ipid);
            daystat.setUsercount(uidcount);
            daystat.setTotalcount(totalcount);
            daystat.setRemark("天统计");
            daystat.replaceSave();
            for (Record userLogin : useRecords) {
                Integer uid = userLogin.getInt("uid");
                int userLoginCount = userLogin.getInt("logincount");
                //一个用户的小时统计
                List<Record> hourLogins = Db.use(Const.Db.TIO_SITE_MAIN)
                        .find("select hourperiod,count(1) hourcount from login_log  where dayperiod = ? and ip = ? and uid = ? group by hourperiod", dayPeriod, ip, uid);
                if (CollectionUtil.isEmpty(hourLogins)) {
                    log.error("登录统计异常：用户登录记录不存在-小时-ip");
                    continue;
                }
                UserIpLoginStat userStat = new UserIpLoginStat();
                userStat.setUid(uid);
                userStat.setTotalcount(userLoginCount);
                userStat.setDayperiod(dayPeriod);
                userStat.setIp(ip);
                userStat.setIpid(ipid);
                for (Record hourLogin : hourLogins) {
                    dateToLoginStat(userStat, hourLogin.getStr("hourperiod"), hourLogin.getInt("hourcount"));
                }
                userStat.replaceSave();
            }
            if (!isInit) {
                //更新总统计
                Record record = Db.use(Const.Db.TIO_SITE_STAT).findFirst("select count(DISTINCT uid) ipuidcount from user_ip_login_stat where ip = ? and uid != -1", ip);
                if (record == null) {
                    log.error("登录统计异常：总记录更新异常");
                    continue;
                }
                int totaluid = record.getInt("ipuidcount");
                if (totaluid == 0) {
                    log.error("登录统计异常：总记录更新异常,统计总数为0");
                    continue;
                }
                UserIpLoginStat ipStatUpdate = new UserIpLoginStat();
                ipStatUpdate.setId(ipstat.getId());
                ipStatUpdate.setUsercount(totaluid);
                ipStatUpdate.setTotalcount(ipstat.getTotalcount() + totalcount);
                ipStatUpdate.update();
            }
        }
    }

    /**
     * 单次登录ip统计
     *
     * @param loginLog
     * @author lixinji
     * 2020年7月30日 下午3:45:20
     */
    public void singleLoginIpStat(LoginLog loginLog) {
        String dayPeriod = PeriodUtils.dateToPeriodByType(loginLog.getTime(), Const.PeriodType.DAY);
        String ip = loginLog.getIp();
        Integer ipid = loginLog.getIpid();
        //用户统计
        Integer uid = loginLog.getUid();
        boolean userInit = false;
        UserIpLoginStat userStat = UserIpLoginStat.dao.findFirst("select * from user_ip_login_stat where ip = ? and uid = ? and dayperiod = ?", ip, uid, dayPeriod);
        if (userStat == null) {
            userStat = new UserIpLoginStat();
            userStat.setUid(uid);
            userStat.setTotalcount(0);
            userStat.setDayperiod(dayPeriod);
            userStat.setIp(ip);
            userStat.setIpid(ipid);
            int save = userStat.ignoreSave();
            if (save <= 0) {
                userInit = true;
            }
        } else {
            userInit = true;
        }
        String hourPeriod = loginLog.getHourperiod();
        hourPeriod = "01";
        Db.use(Const.Db.TIO_SITE_STAT).update("update user_ip_login_stat set totalcount = totalcount + 1,hour" + Integer.parseInt(hourPeriod) + " = hour"
                + Integer.parseInt(hourPeriod) + " + 1 where ip = ? and uid = ? and dayperiod = ?", ip, uid, dayPeriod);

        //ip天统计
        UserIpLoginStat daystat = UserIpLoginStat.dao.findFirst("select * from user_ip_login_stat where ip = ? and uid = -1 and dayperiod = ?", ip, dayPeriod);
        if (daystat == null) {
            daystat = new UserIpLoginStat();
            daystat.setUid(-1);
            daystat.setDayperiod(dayPeriod);
            daystat.setIp(ip);
            daystat.setIpid(ipid);
            daystat.setUsercount(1);
            daystat.setTotalcount(1);
            daystat.setRemark("天统计");
            if (daystat.ignoreSave() <= 0) {
                if (userInit) {
                    Db.use(Const.Db.TIO_SITE_STAT).update("update user_ip_login_stat set totalcount = totalcount + 1 where ip = ? and uid = -1 and dayperiod = ?", ip, dayPeriod);
                } else {
                    Db.use(Const.Db.TIO_SITE_STAT).update(
                            "update user_ip_login_stat set totalcount = totalcount + 1,usercount = usercount + 1 where ip = ? and uid = -1 and dayperiod = ?", ip, dayPeriod);
                }
            }
        } else {
            if (userInit) {
                Db.use(Const.Db.TIO_SITE_STAT).update("update user_ip_login_stat set totalcount = totalcount + 1 where id = ?", daystat.getId());
            } else {
                Db.use(Const.Db.TIO_SITE_STAT).update("update user_ip_login_stat set totalcount = totalcount + 1,usercount = usercount + 1 where id = ?", daystat.getId());
            }
        }
        //ip总统计
        UserIpLoginStat ipstat = UserIpLoginStat.dao.findFirst("select * from user_ip_login_stat where ip = ? and uid = -1 and dayperiod = '-1'", ip);
        if (ipstat == null) {
            ipstat = new UserIpLoginStat();
            ipstat.setIp(ip);
            ipstat.setIpid(ipid);
            ipstat.setUsercount(1);
            ipstat.setTotalcount(1);
            ipstat.setDayperiod("-1");
            ipstat.setUid(-1);
            if (ipstat.ignoreSave() <= 0) {
                if (userInit) {
                    Db.use(Const.Db.TIO_SITE_STAT).update("update user_ip_login_stat set totalcount = totalcount + 1 where ip = ? and uid = -1 and dayperiod = '-1'", ip);
                } else {
                    Db.use(Const.Db.TIO_SITE_STAT)
                            .update("update user_ip_login_stat set totalcount = totalcount + 1,usercount = usercount + 1 where ip = ? and uid = -1 and dayperiod = '-1'", ip);
                }
            }
        } else {
            if (userInit) {
                Db.use(Const.Db.TIO_SITE_STAT).update("update user_ip_login_stat set totalcount = totalcount + 1 where id = ?", ipstat.getId());
            } else {
                Db.use(Const.Db.TIO_SITE_STAT).update("update user_ip_login_stat set totalcount = totalcount + 1,usercount = usercount + 1 where id = ?", ipstat.getId());
            }
        }
    }

    /**
     * 单独登录统计处理
     *
     * @param loginLog
     * @author lixinji
     * 2020年7月30日 下午3:50:39
     */
    public void singleLoginInit(LoginLog loginLog) {
        Integer threadid = StatService.me.threadInit(loginLog.getId(), "", Const.ThreadLogType.LOGIN_STAT);
        Const.getBsExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    AbsTxAtom absTxAtom = new AbsTxAtom() {

                        @Override
                        public boolean noTxRun() {
                            singleLoginIpStat(loginLog);
                            singleLoginTimeStat(loginLog);
                            return true;
                        }
                    };
                    boolean save = Db.use(Const.Db.TIO_SITE_STAT).tx(absTxAtom);
                    if (!save) {
                        return;
                    }
                    StatService.me.threadDeal(threadid);
                } catch (Exception e) {
                    log.error(e.toString(), e);
                }
            }
        });
    }

    /**
     * @author lixinji
     * 2020年7月16日 下午6:06:13
     */
    public void loginInit() {
        Db.use(Const.Db.TIO_SITE_STAT).update("truncate table user_ip_login_stat");
        Db.use(Const.Db.TIO_SITE_STAT).update("truncate table user_time_login_stat");
        Record maxRecord = Db.use(Const.Db.TIO_SITE_MAIN).findFirst("select min(time) mintime from login_log");
        if (maxRecord == null) {
            return;
        }
        Date date = maxRecord.getDate("mintime");
        DateTime begiTime = DateUtil.beginOfDay(date);
        DateTime startime = DateUtil.offsetDay(begiTime, 1);
        DateTime curTime = DateUtil.beginOfDay(new Date());
        int count = 0;
        long start = System.currentTimeMillis();
        log.error("开始登录日志计算：{}", begiTime);
        while (curTime.getTime() >= startime.getTime()) {
            loginTimeStat(startime);
            loginIpStat(startime);
            startime = DateUtil.offsetDay(startime, 1);
            count++;
        }
        long end = System.currentTimeMillis();
        long exe = (end - start) / (60 * 1000);
        log.error("登录日志计算，次数：" + count + ",总时间：" + exe + ",开始日期 (+1)：" + begiTime + ",结束日期：" + startime);
    }

    /**
     * @param stat
     * @param field
     * @param value
     * @author lixinji
     * 2020年7月16日 下午3:08:11
     */
    public static void dateToLoginStat(UserTimeLoginStat stat, String field, int value) {
        switch (field) {
            case "00":
                stat.setHour0(value);
                break;

            case "01":
                stat.setHour1(value);
                break;
            case "02":
                stat.setHour2(value);
                break;
            case "03":
                stat.setHour3(value);
                break;
            case "04":
                stat.setHour4(value);
                break;
            case "05":
                stat.setHour5(value);
                break;
            case "06":
                stat.setHour6(value);
                break;
            case "07":
                stat.setHour7(value);
                break;
            case "08":
                stat.setHour8(value);
                break;
            case "09":
                stat.setHour9(value);
                break;
            case "10":
                stat.setHour10(value);
                break;
            case "11":
                stat.setHour11(value);
                break;
            case "12":
                stat.setHour12(value);
                break;
            case "13":
                stat.setHour13(value);
                break;
            case "14":
                stat.setHour14(value);
                break;
            case "15":
                stat.setHour15(value);
                break;
            case "16":
                stat.setHour16(value);
                break;
            case "17":
                stat.setHour17(value);
                break;
            case "18":
                stat.setHour18(value);
                break;
            case "19":
                stat.setHour19(value);
                break;
            case "20":
                stat.setHour20(value);
                break;
            case "21":
                stat.setHour21(value);
                break;
            case "22":
                stat.setHour22(value);
                break;
            case "23":
                stat.setHour23(value);
                break;
            default:
                break;
        }
    }

    /**
     * @param stat
     * @param field
     * @param value
     * @author lixinji
     * 2020年7月16日 下午4:00:30
     */
    public static void dateToLoginStat(UserIpLoginStat stat, String field, int value) {
        switch (field) {
            case "00":
                stat.setHour0(value);
                break;

            case "01":
                stat.setHour1(value);
                break;
            case "02":
                stat.setHour2(value);
                break;
            case "03":
                stat.setHour3(value);
                break;
            case "04":
                stat.setHour4(value);
                break;
            case "05":
                stat.setHour5(value);
                break;
            case "06":
                stat.setHour6(value);
                break;
            case "07":
                stat.setHour7(value);
                break;
            case "08":
                stat.setHour8(value);
                break;
            case "09":
                stat.setHour9(value);
                break;
            case "10":
                stat.setHour10(value);
                break;
            case "11":
                stat.setHour11(value);
                break;
            case "12":
                stat.setHour12(value);
                break;
            case "13":
                stat.setHour13(value);
                break;
            case "14":
                stat.setHour14(value);
                break;
            case "15":
                stat.setHour15(value);
                break;
            case "16":
                stat.setHour16(value);
                break;
            case "17":
                stat.setHour17(value);
                break;
            case "18":
                stat.setHour18(value);
                break;
            case "19":
                stat.setHour19(value);
                break;
            case "20":
                stat.setHour20(value);
                break;
            case "21":
                stat.setHour21(value);
                break;
            case "22":
                stat.setHour22(value);
                break;
            case "23":
                stat.setHour23(value);
                break;
            default:
                break;
        }
    }

    /**
     * @return
     */
    public User nextRobot() {
        if (CollUtil.isNotEmpty(robots)) {
            Integer uid = RandomUtil.randomEle(robots);
            return getById(uid);
        }
        return null;
    }


    /**
     * 获取直接下级
     *
     * @param user
     * @param parentInviteCode
     * @return
     * @author lixinji
     * 2020年3月3日 下午6:30:39
     */
    public Resp getDirectUnderList(User user, String parentInviteCode) {
        if (parentInviteCode == null) {
            parentInviteCode = "";
        }
        String sql = "select  " +
                "a.id as uid, a.avatar, a.nick as nick, a.invitecode as invitecode, a.parentinvitecode as parentinvitecode, a.createtime as createtime, IFNULL(b.cny, 0) as cny, c.number as number " +
                "from tio_site_main.user a  " +
                "left join  " +
                "wx_user_coin_local b " +
                "on a.id = b.uid  " +
                "left join " +
                "(WITH RECURSIVE subset_cte (id, invitecode, parentinvitecode, depth) AS ( " +
                "SELECT d.id, d.invitecode, d.parentinvitecode, 1 " +
                "FROM tio_site_main.user d " +
                "WHERE invitecode = ? " +
                "UNION ALL " +
                "SELECT t.id, t.invitecode, t.parentinvitecode, cte.depth + 1 " +
                "FROM tio_site_main.user t " +
                "INNER JOIN subset_cte cte ON t.parentinvitecode = cte.invitecode " +
                ") " +
                "SELECT subset_cte.id, subset_cte.invitecode, subset_cte.parentinvitecode, depth, count(1) as number " +
                "FROM subset_cte) c " +
                "on a.id = c.id " +
                "where " +
                "a.invitecode = ? or a.parentInviteCode = ?";
        List<Record> dataList = Db.find(sql, parentInviteCode, parentInviteCode, parentInviteCode);
        return Resp.ok().data(dataList);
    }

    /**
     * 获取下级信息
     *
     * @param uid
     * @return
     * @author lixinji
     * 2023年3月3日 下午6:30:39
     */
    public Record getUnderUserInfo(Integer uid) {
        String parentInviteCode = User.dao.findById(uid).getInvitecode();
        String sql = "select  " +
                "a.id as uid, a.avatar as avatar, a.nick, IFNULL(b.cny, 0) as cny,a.createtime as createTime, c.number  as teamNumber  " +
                "from tio_site_main.user a  " +
                "left join  " +
                "wx_user_coin_local b " +
                "on a.id = b.uid  " +
                "left join " +
                "(WITH RECURSIVE subset_cte (id, invitecode, parentinvitecode, depth) AS ( " +
                "SELECT d.id, d.invitecode, d.parentinvitecode, 1 " +
                "FROM tio_site_main.user d " +
                "WHERE invitecode = ? " +
                "UNION ALL " +
                "SELECT t.id, t.invitecode, t.parentinvitecode, cte.depth + 1 " +
                "FROM tio_site_main.user t " +
                "INNER JOIN subset_cte cte ON t.parentinvitecode = cte.invitecode " +
                ") " +
                "SELECT subset_cte.id, subset_cte.invitecode, subset_cte.parentinvitecode, depth, count(1) as number " +
                "FROM subset_cte) c " +
                "on a.id = c.id " +
                "where " +
                "a.id = ?";
        Record record = Db.findFirst(sql, parentInviteCode, uid);


        return record;
    }

    public Boolean uploadRealCertification(User user, String number, String realName, String idCardFront, String idCardBehind) {
        RealNameCertification realNameCertification = new RealNameCertification();
        if (RealNameCertification.dao.findFirst("select * from real_name_certification where uid = ?", user.getId()) != null) {
            realNameCertification = RealNameCertification.dao.findFirst("select * from real_name_certification where uid = ?", user.getId());
        }
        realNameCertification.setUid(user.getId());
        realNameCertification.setIdCardNumber(number);
        realNameCertification.setRealName(realName);
        realNameCertification.setIdCardFront(idCardFront);
        realNameCertification.setIdCardBehind(idCardBehind);
        if (realNameCertification.getStatus() != null && realNameCertification.getStatus().equals(-1)) {
            realNameCertification.setStatus(0);
            realNameCertification.setUpdatetime(new Date());
            boolean update = realNameCertification.update();
            if (!update) {
                log.error("uid: {} 实名认证上传异常。", user.getId());
            }
            return true;
        }
        realNameCertification.setCreatetime(new Date());
        boolean save = realNameCertification.save();
        if (!save) {
            log.error("uid: {} 实名认证上传异常。", user.getId());
        }
        return true;
    }

    /**
     * @param curr
     * @author xinji
     * 2023.09.05
     */
    public SignItem sign(User curr) {
        // 检查当天是否已签到
        if (!checkSignDay(curr.getId())) {
            return null;
        }
        // 获取签到任务列表
//		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("sign.signtasklist");
//		List<SignTask> signTasks = SignTask.dao.find(sqlPara);
        List<SignTask> signTasks = SignTask.dao.find("select * from sign_task order by sign_day");
        // 获取普通签到可获取的积分
        Kv params = Kv.by("sign_day", 0);
        // 总奖励
        Long resIntegral = SignTask.dao.findFirst("select * from sign_task where sign_day = ? order by sign_day", 0).getRewardIntegral();

        // 计算当前连续签到天数
        Integer signDay = 0;
        if (curr.getSignDay().equals(0)) {
            signDay = 1;
        } else if (curr.getSignDay().equals(30)) {
            signDay = 1;
        } else {
//			params.set("uid", curr.getId());
            SignItem signItemLast = SignItem.dao.findFirst("select * from sign_item where uid = ? order by create_time desc", curr.getId());
            if (isYesterday(signItemLast.getCreateTime())) {
                signDay = curr.getSignDay() + 1;
            } else {
                signDay = 1;
            }
        }

        // 额外签到任务奖励
        Long temp = 0L;

        for (SignTask signTask : signTasks) {
            if (signTask.getSignDay().equals(0)) {
                continue;
            }
            if (signDay.equals(signTask.getSignDay())) {
                temp = signTask.getRewardIntegral();
                break;
            }
        }

        // 总奖励
        resIntegral += temp;

        // 获取用户钱包信息
        WxUserCoinLocal userCoin = WxUserCoinLocal.dao.findFirst("select * from wx_user_coin_local where uid = ?", curr.getId());

        // 添加签到记录
        SignItem signItem = new SignItem();
        signItem.setUid(curr.getId());
        signItem.setIntegral(resIntegral);
        signItem.setBeforeIntegral(userCoin.getCny());
        signItem.setNowIntegral(userCoin.getCny() + resIntegral);
        signItem.setCreateTime(new Date());
        // 修改用户余额
        userCoin.setCny(userCoin.getCny() + resIntegral);
        // 修改用户连续签到天数
        curr.setSignDay(signDay);

        signItem.save();
        userCoin.update();
        curr.update();
        return signItem;
    }

    /**
     * 获取收藏列表
     *
     * @param uid
     * @param pageNumber
     * @param pageSize
     * @return
     * @throws Exception
     */
    public Page<Record> collectList(Integer uid, Integer category, Integer pageNumber, Integer pageSize) throws Exception {
        if (pageNumber == null) {
            pageNumber = 1;
        }
        if (pageSize == null) {
            pageSize = 10;
        }
        Kv params = Kv.by("uid", uid);
        if (category != null && !category.equals(0)) {
            params.set("category", category);
        }
        SqlPara sqlPara = Collect.dao.getSqlPara("collect.collectList", params);
        Page<Record> page = Db.paginate(pageNumber, pageSize, sqlPara);
        List<Record> list = page.getList();
        for (Record record : list) {
            if (record.get("category").equals(11)) {
                record.set("content", "id:" + record.get("id"));
            }
        }
        return page;
    }

    /**
     * 删除收藏
     *
     * @param user
     * @param cid
     * @return
     * @throws Exception
     */
    public Boolean delCollect(User user, Integer cid) throws Exception {
        Collect collect = Collect.dao.findById(cid);
        if (collect == null) {
            log.error("收藏id不存在");
            return false;
        }
        if (!collect.getUid().equals(user.getId())) {
            log.error("非本人不可删除收藏内容");
            return false;
        }

        boolean delete = collect.delete();

        return delete;
    }

    /**
     * 当日是否签到
     *
     * @param uid
     * @return true 未签到 false 已签到
     * @author xinji
     * 2023.09.05
     */
    private boolean checkSignDay(Integer uid) {
//		Kv params = Kv.by("uid", uid);
//		SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("sign.signitemlist", params);
        SignItem signItemLast = SignItem.dao.findFirst(
                "select * from sign_item where uid = ? order by create_time desc", uid
        );
        if (signItemLast == null) {
            return true;
        }
        if (isToday(signItemLast.getCreateTime())) {
            return false;
        }
        return true;
    }

    /**
     * 判断日期是不是今天
     *
     * @param date
     * @return 是返回true，不是返回false
     * @author xinji
     * 2023.09.05
     */
    public static boolean isToday(Date date) {
        boolean flag = false;
        // 先获取年份
        int year = Integer.parseInt(new SimpleDateFormat("yyyy").format(date));

        // 获取当前年份 和 一年中的第几天
        int day = getDayNumForYear(date);
        int currentYear = Integer.parseInt(new SimpleDateFormat("yyyy").format(new Date()));
        int currentDay = getDayNumForYear(new Date());
        // 计算 如果是去年的
        if (currentYear - year == 1) {
            // 如果当前正好是 1月1日 计算去年有多少天，指定时间是否是一年中的最后一天
            if (currentDay == 1) {
                int yearDay;
                if (year % 400 == 0) {
                    // 世纪闰年
                    yearDay = 366;
                } else if (year % 4 == 0 && year % 100 != 0) {
                    // 普通闰年
                    yearDay = 366;
                } else {
                    // 平年
                    yearDay = 365;
                }
                if (day == yearDay) {
                    flag = true;
                }
            }
        } else {
            if (currentDay - day == 0) {
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 判断日期是否为当月头一天
     *
     * @param date
     * @return
     * @author xinji
     * 2023.09.05
     */
    public boolean isFirstDayOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + 1);
        return calendar.get(Calendar.DAY_OF_MONTH) == 2;
    }

    /**
     * @param date
     * @return
     * @author xinji
     * 2023.09.05
     */
    public static boolean isYesterday(Date date) {
        boolean flag = false;
        // 先获取年份
        int year = Integer.parseInt(new SimpleDateFormat("yyyy").format(date));

        // 获取当前年份 和 一年中的第几天
        int day = getDayNumForYear(date);
        int currentYear = Integer.parseInt(new SimpleDateFormat("yyyy").format(new Date()));
        int currentDay = getDayNumForYear(new Date());
        // 计算 如果是去年的
        if (currentYear - year == 1) {
            // 如果当前正好是 1月1日 计算去年有多少天，指定时间是否是一年中的最后一天
            if (currentDay == 1) {
                int yearDay;
                if (year % 400 == 0) {
                    // 世纪闰年
                    yearDay = 366;
                } else if (year % 4 == 0 && year % 100 != 0) {
                    // 普通闰年
                    yearDay = 366;
                } else {
                    // 平年
                    yearDay = 365;
                }
                if (day == yearDay) {
                    flag = true;
                }
            }
        } else {
            if (currentDay - day == 1) {
                flag = true;
            }
        }
        return flag;
    }

    public static Integer getDayNumForYear(Date date) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        return ca.get(Calendar.DAY_OF_YEAR);
    }

    public Boolean addMoments(User user, String content, Short authFlag, String videoUrl, String imgUrl) {
        Moments moments = new Moments();
        moments.setUid(user.getId());
        if (content != null) {
            moments.setContent(content);
        } else {
            moments.setContent("");
        }
        if (videoUrl != null) {
            moments.setVideoUrl(videoUrl);
        }
        if (imgUrl != null) {
            moments.setImgUrl(imgUrl);
        }
        moments.setCreateTime(new Date());
        if (authFlag != null) {
            moments.setAuthFlag(authFlag);
        }
        return moments.save();
    }

    public Boolean delMoments(User curr, Integer mid) {
        Moments moments = Moments.dao.findById(mid);
        if (moments == null) {
            log.error("该条朋友圈不存在");
            return false;
        }
        if (!moments.getUid().equals(curr.getId())) {
            log.error("非本人不可删除朋友圈内容");
            return false;
        }

        return moments.delete();
    }

    public MomentsComments addComments(User user, Integer mid, Integer pid, String content) {
        MomentsComments momentsComments = new MomentsComments();
        momentsComments.setPid(pid);
        momentsComments.setUid(user.getId());
        momentsComments.setAvatar(user.getAvatar());
        momentsComments.setMid(mid);
        momentsComments.setContent(content);
        momentsComments.setCreateTime(new Date());
        boolean save = momentsComments.save();
        if (!save) {
            return null;
        }
        return momentsComments;
//		return momentsComments.save();
    }

    public Boolean delComments(Integer cid) {
        MomentsComments comments = MomentsComments.dao.findById(cid);
        return comments.delete();
    }

    public MomentsLikes likes(User user, Integer mid) {
        MomentsLikes momentsLikes = new MomentsLikes();
        momentsLikes.setUid(user.getId());
        momentsLikes.setAvatar(user.getAvatar());
        momentsLikes.setMid(mid);
        momentsLikes.setLikeTime(new Date());
        boolean save = momentsLikes.save();
        if (!save) {
            return null;
        }
        return momentsLikes;
    }

    public Boolean cancelLikes(Integer likesId) {
        MomentsLikes likes = MomentsLikes.dao.findById(likesId);
        return likes.delete();
    }

    public static List<WxMomentMsgVo> getMomentsMsgList(User curr, Integer pageNumber, Integer pageSize) {

        ICache cache = Caches.getCache(CacheConfig.SEARCH_MOMENTMSGS);
        String key = "momentmsgs" + "_" + curr.getId();
        if (pageNumber.equals(1)) {
            cache.remove(key);
        }
        List<WxFriendMsg> wxFriendMsgs = CacheUtils.get(cache, key, true, new FirsthandCreater<ArrayList<WxFriendMsg>>() {
            @Override
            public ArrayList<WxFriendMsg> create() throws Exception {
                ArrayList<WxFriendMsg> list = (ArrayList<WxFriendMsg>) WxFriendMsg.dao.find(
                        "select * from wx_friend_msg where touid = ? and (contenttype = 16 or contenttype = 17)  " +
                                "order by createtime desc",
                        curr.getId());
                return list;
            }
        });


//		List<WxFriendMsg> wxFriendMsgs = WxFriendMsg.dao.find(
//				"select * from wx_friend_msg where touid = ? and (contenttype = 16 or contenttype = 17)  " +
//						"order by createtime desc limit ? offset ?",
//				 curr.getId(), pageSize, (pageNumber - 1) * pageSize);

        if (wxFriendMsgs == null) {
            return new ArrayList<WxMomentMsgVo>();
        }

        int maxIndex = pageSize * (pageNumber - 1) + pageSize;
        int endIndex = Math.min(wxFriendMsgs.size(), maxIndex);
        int startIndex = pageSize * (pageNumber - 1);
        List<WxMomentMsgVo> result = new ArrayList<WxMomentMsgVo>();
        for (int i = startIndex; i < endIndex; i++) {
            WxMomentMsgVo wxMomentMsgVo = new WxMomentMsgVo();
            wxMomentMsgVo.setFromNickname(User.dao.findById(wxFriendMsgs.get(i).getUid()).getNick());
            wxMomentMsgVo.setTime(wxFriendMsgs.get(i).getCreatetime());
            wxMomentMsgVo.setWxFriendMsg(wxFriendMsgs.get(i));
            User fromUser = User.dao.findById(wxFriendMsgs.get(i).getUid());
            wxMomentMsgVo.setAvatar(fromUser.getAvatar());
            JSONObject jsonObject = JSONObject.parseObject(wxFriendMsgs.get(i).getText());
            Object text = jsonObject.get("text").toString();
            String id = jsonObject.get("id").toString();
            if (wxFriendMsgs.get(i).getContenttype().equals(Const.ContentType.LIKES)) {
                MomentsLikes momentsLikes = MomentsLikes.dao.findById(id);
                if (momentsLikes == null) {
                    wxMomentMsgVo.setLikeCancel(Const.YesOrNo.YES);
                    wxMomentMsgVo.setContent("点赞已取消");
                } else {
                    Moments moment = Moments.dao.findById(momentsLikes.getMid());
                    wxMomentMsgVo.setLikeCancel(Const.YesOrNo.NO);
                    wxMomentMsgVo.setMoments(moment);
                    wxMomentMsgVo.setMomentsLikes(momentsLikes);
                    wxMomentMsgVo.setContent("您有一条新的点赞消息");
                }
            } else {
                MomentsComments comment = MomentsComments.dao.findById(id);
                if (comment == null) {
                    Moments moment = Moments.dao.findById(jsonObject.get("mid"));
                    wxMomentMsgVo.setMoments(moment);
                    wxMomentMsgVo.setCommentCancel(Const.YesOrNo.YES);
                    wxMomentMsgVo.setContent("评论已删除");
                } else {
                    Moments moment = Moments.dao.findById(comment.getMid());
                    wxMomentMsgVo.setCommentCancel(Const.YesOrNo.NO);
                    wxMomentMsgVo.setMoments(moment);
                    wxMomentMsgVo.setMomentsComments(comment);
                    wxMomentMsgVo.setContent(text.toString());
                }
            }
            result.add(wxMomentMsgVo);
        }
        result.sort(Comparator.comparing((WxMomentMsgVo e) -> e.getTime()).reversed());
        return result;
    }

    public List<Moments> momentsList(Integer id, Integer pageNumber, Integer pageSize) {

        ICache cache = Caches.getCache(CacheConfig.SEARCH_MOMENTS);
        String key = "moments" + "_" + id;
        if (pageNumber.equals(1)) {
            cache.remove(key);
        }
        List<Moments> momentsList = CacheUtils.get(cache, key, true, new FirsthandCreater<ArrayList<Moments>>() {
            @Override
            public ArrayList<Moments> create() throws Exception {
                ArrayList<Moments> moments = (ArrayList<Moments>) Moments.dao.find("select " +
                        "a.remarkname, u.nick, u.avatar, b.* " +
                        "from " +
                        "wx_friend a, " +
                        "moments b, " +
                        "user u " +
                        "where " +
                        "a.uid = ? and a.frienduid = b.uid and u.id = b.uid " +
                        "order by b.create_time desc" /*+
						" desc limit ? offset ?"*/, id/*, pageSize, (pageNumber - 1) * pageSize*/);

//		EhCache.
                return moments;
            }
        });
        List<Moments> result = new ArrayList<Moments>();
        if (momentsList != null && momentsList.size() > 0) {
            int maxIndex = pageSize * (pageNumber - 1) + pageSize;
            int endIndex = Math.min(momentsList.size(), maxIndex);
            int startIndex = pageSize * (pageNumber - 1);
            if (momentsList.size() < startIndex) {
                return result;
            }

            for (int i = startIndex; i < endIndex; i++) {
                Moments moment = momentsList.get(i);
                List<MomentsComments> comments = MomentsComments.dao.find(
                        "select " +
                                "u.nick, a.remarkname, b.* " +
                                "from " +
                                "wx_friend a, " +
                                "moments_comments b, " +
                                "user u " +
                                "where  " +
                                "a.uid = ? and a.frienduid = b.uid and b.mid = ? and u.id = b.uid order by pid,create_time ",
                        id, moment.getId());

                List<MomentsLikes> likes = MomentsLikes.dao.find("select " +
                        "u.nick, a.remarkname, b.* " +
                        "from " +
                        "wx_friend a, " +
                        "moments_likes b, " +
                        "user u " +
                        "where  " +
                        "a.uid = ? and a.frienduid = b.uid and b.mid = ? and u.id = b.uid order by like_time", id, moment.getId());
                Set<Map.Entry<String, Object>> entries = moment._getAttrsEntrySet();
                for (Map.Entry<String, Object> entry : entries) {
                    if (entry.getKey().equals("remarkname")) {
                        moment.setRemarkName(entry.getValue().toString());
                    }
                    if (entry.getKey().equals("nick")) {
                        moment.setNick(entry.getValue().toString());
                    }
                    if (entry.getKey().equals("avatar")) {
                        moment.setAvatar(entry.getValue().toString());
                    }
                }
                for (MomentsComments comment : comments) {
                    Set<Map.Entry<String, Object>> commentEntry = comment._getAttrsEntrySet();
                    for (Map.Entry<String, Object> entry : commentEntry) {
                        if (entry.getKey().equals("remarkname")) {
                            comment.setRemarkName(entry.getValue().toString());
                        }
                        if (entry.getKey().equals("nick")) {
                            comment.setNick(entry.getValue().toString());
                        }
                    }
                }

                for (MomentsLikes like : likes) {
                    Set<Map.Entry<String, Object>> likeEntry = like._getAttrsEntrySet();
                    for (Map.Entry<String, Object> entry : likeEntry) {
                        if (entry.getKey().equals("remarkname")) {
                            like.setRemarkName(entry.getValue().toString());
                        }
                        if (entry.getKey().equals("nick")) {
                            like.setNick(entry.getValue().toString());
                        }
                    }
                }
                moment.setComments(comments);
                moment.setLikes(likes);
                if (moment.getImgUrl() == null) {
                    moment.setImgUrl("");
                }
                if (moment.getVideoUrl() == null) {
                    moment.setVideoUrl("");
                }
//				moment.setNick(moment.getNick());
//				moment.setRemarkName(moment.getRemarkName());
//				moment.setAvatar(moment.getAvatar());
                if (likes.size() > 0) {
                    moment.setLikesCount(likes.size());
                } else {
                    moment.setLikesCount(0);
                }
                result.add(moment);
            }
        }

        return result;

    }


    public List<Moments> momentsListByUid(Integer id, Integer friendUid, Integer pageNumber, Integer pageSize) {

        ICache cache = Caches.getCache(CacheConfig.SEARCH_MOMENTS);
        String key = "moments_friend" + "_" + id + "_" + friendUid;
        if (pageNumber.equals(1)) {
            cache.remove(key);
        }
        List<Moments> momentsList = CacheUtils.get(cache, key, true, new FirsthandCreater<ArrayList<Moments>>() {
            @Override
            public ArrayList<Moments> create() throws Exception {
                ArrayList<Moments> moments = (ArrayList<Moments>) Moments.dao.find("select " +
                        "a.remarkname, u.nick, u.avatar, b.* " +
                        "from " +
                        "wx_friend a, " +
                        "moments b, " +
                        "user u " +
                        "where " +
                        "a.uid = ? and b.uid = ? and a.frienduid = b.uid and u.id = b.uid " +
                        "order by b.create_time desc" /*+
						" desc limit ? offset ?"*/, id, friendUid/*, pageSize, (pageNumber - 1) * pageSize*/);

//		EhCache.
                return moments;
            }
        });
        List<Moments> result = new ArrayList<Moments>();
        if (momentsList != null && momentsList.size() > 0) {
            int maxIndex = pageSize * (pageNumber - 1) + pageSize;
            int endIndex = Math.min(momentsList.size(), maxIndex);
            int startIndex = pageSize * (pageNumber - 1);
            if (momentsList.size() < startIndex) {
                return result;
            }

            for (int i = startIndex; i < endIndex; i++) {
                Moments moment = momentsList.get(i);
                List<MomentsComments> comments = MomentsComments.dao.find(
                        "select " +
                                "u.nick, a.remarkname, b.* " +
                                "from " +
                                "wx_friend a, " +
                                "moments_comments b, " +
                                "user u " +
                                "where  " +
                                "a.uid = ? and a.frienduid = b.uid and b.mid = ? and u.id = b.uid order by pid,create_time ",
                        id, moment.getId());

                List<MomentsLikes> likes = MomentsLikes.dao.find("select " +
                        "u.nick, a.remarkname, b.* " +
                        "from " +
                        "wx_friend a, " +
                        "moments_likes b, " +
                        "user u " +
                        "where  " +
                        "a.uid = ? and a.frienduid = b.uid and b.mid = ? and u.id = b.uid order by like_time", id, moment.getId());
                Set<Map.Entry<String, Object>> entries = moment._getAttrsEntrySet();
                for (Map.Entry<String, Object> entry : entries) {
                    if (entry.getKey().equals("remarkname")) {
                        moment.setRemarkName(entry.getValue().toString());
                    }
                    if (entry.getKey().equals("nick")) {
                        moment.setNick(entry.getValue().toString());
                    }
                    if (entry.getKey().equals("avatar")) {
                        moment.setAvatar(entry.getValue().toString());
                    }
                }
                for (MomentsComments comment : comments) {
                    Set<Map.Entry<String, Object>> commentEntry = comment._getAttrsEntrySet();
                    for (Map.Entry<String, Object> entry : commentEntry) {
                        if (entry.getKey().equals("remarkname")) {
                            comment.setRemarkName(entry.getValue().toString());
                        }
                        if (entry.getKey().equals("nick")) {
                            comment.setNick(entry.getValue().toString());
                        }
                    }
                }

                for (MomentsLikes like : likes) {
                    Set<Map.Entry<String, Object>> likeEntry = like._getAttrsEntrySet();
                    for (Map.Entry<String, Object> entry : likeEntry) {
                        if (entry.getKey().equals("remarkname")) {
                            like.setRemarkName(entry.getValue().toString());
                        }
                        if (entry.getKey().equals("nick")) {
                            like.setNick(entry.getValue().toString());
                        }
                    }
                }
                moment.setComments(comments);
                moment.setLikes(likes);
                if (moment.getImgUrl() == null) {
                    moment.setImgUrl("");
                }
                if (moment.getVideoUrl() == null) {
                    moment.setVideoUrl("");
                }
//				moment.setNick(moment.getNick());
//				moment.setRemarkName(moment.getRemarkName());
//				moment.setAvatar(moment.getAvatar());
                if (likes.size() > 0) {
                    moment.setLikesCount(likes.size());
                } else {
                    moment.setLikesCount(0);
                }
                result.add(moment);
            }
        }

        return result;

    }

    public boolean checkLike(Integer mid, Integer id) {
        MomentsLikes likes = MomentsLikes.dao.findFirst("select * from moments_likes where mid = ? and uid = ?", mid, id);
        if (likes != null) {
            return false;
        }
        return true;
    }

    public void readFlagUpdate(Integer toUid, Integer type) {
        if (type.equals(1)) {
            List<WxFriendMsg> wxFriendMsgs = WxFriendMsg.dao.find("select * from wx_friend_msg where touid = ? and contenttype = 15 and readflag = 2", toUid);
            if (wxFriendMsgs != null) {
                for (WxFriendMsg wxFriendMsg : wxFriendMsgs) {
                    wxFriendMsg.setReadflag(Const.YesOrNo.YES);
                    wxFriendMsg.setReadtime(new Date());
                    wxFriendMsg.update();
                }
            }
        } else {
            Kv params = Kv.by("touid", toUid).set("nowDate", new Date());
            SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatmsg.updateMomentMsgs", params);
            Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
        }

    }

    public static void clearMomentsMsgs(User curr) {
        Kv params = Kv.by("touid", curr.getId());
        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatmsg.clearMomentMsgs", params);
        Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
    }

    public Moments getMomentById(User curr, Integer mid) {
        Moments moment = Moments.dao.findById(mid);
        User user = User.dao.findById(moment.getUid());
        WxFriend friend = WxFriend.dao.findFirst("select * from wx_friend where uid = ? and frienduid=?", curr.getId(), user.getId());
        List<MomentsLikes> momentsLikes = MomentsLikes.dao.find("select * from moments_likes where mid = ?", mid);
        List<MomentsComments> comments = MomentsComments.dao.find("select * from moments_comments where mid = ?", mid);
        if (momentsLikes != null) {
            moment.setLikes(momentsLikes);
            moment.setLikesCount(momentsLikes.size());
        } else {
            moment.setLikesCount(0);
        }
        if (friend.getRemarkname() != null && !friend.getRemarkname().isEmpty()) {
            moment.setRemarkName(friend.getRemarkname());
        }
        for (MomentsComments comment : comments) {
            User friend2 = User.dao.findById(comment.getUid());
            comment.setNick(friend2.getNick());
            WxFriend wxFriend = WxFriend.dao.findFirst("select * from wx_friend where uid = ? and frienduid = ?", curr.getId(), friend2.getId());
            if (wxFriend.getRemarkname() != null && !wxFriend.getRemarkname().isEmpty()) {
                comment.setRemarkName(wxFriend.getRemarkname());
            }
        }
        moment.setComments(comments);
        moment.setNick(user.getNick());
        moment.setAvatar(user.getAvatar());
        return moment;
    }


    public void updateInvitecode(User curr, String invitecode) {
        String sql1 = "update user set parentinvitecode = ? where invitecode = ?";
        String sql2 = "update user set invitecode = ? where invitecode = ?";
        int c1 = Db.update(sql1, invitecode, curr.getInvitecode());
        int c2 = Db.update(sql2, invitecode, curr.getInvitecode());

    }
}
