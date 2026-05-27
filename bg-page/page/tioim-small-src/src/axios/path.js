import { fetchPost, fetchGet, fetchUpload } from '@/axios/http';
import { getWithCache } from '@/assets/js/common';
import Vue from 'vue';
import layer from 'vue-layer';
import 'vue-layer/lib/vue-layer.css';
let $layer = layer(Vue);
/* 弹框提示 */
export function msgTips(msg) {
    $layer.msg(msg || '网络异常');
}
export function layerLoading() {
    $layer.loading();
}
export function layerCloseAll() {
    $layer.closeAll();
}
/* 公共变量 */
function commonView() {
    console.log('before initCookie11', JSON.parse(JSON.stringify(document.cookie)));
    return fetchGet("/config/viewmodel", {}, true);
}
/* 获取后台单项参数配置 */
function getCommonConfByName(data) {
    return fetchGet("/app/getClientConfByName", data);
}
/* 获取后台参数配置列表 */
function getCommonConfList() {
    return fetchGet("/app/getClientConf");
}

/* im服务 */
function imServer() {
    return fetchGet("/im/imserver");
}
/* 用户信息 */
function currUser() {
    return fetchGet("/user/curr");
}
/* 上传文件 */
function uploadFile(url, data) {
    return fetchUpload(url, data);
}
/* 打洞服务器 */
function getTurnServer(groupid, userflag) {
    let cacheName = "/im/turnserver";
    let key = "x";
    return getWithCache(cacheName, key, 10, async function () {
        var data = null;
        await fetchGet(cacheName, { groupid, userflag }).then(res => {
            if (res.ok) {
                data = res.data;
            }
        })
        return data;
    });
};
/* 用户接口 */
const user = {
    /* 用户登录 */
    userLogin(data) {
        return fetchPost("/login", data);
    },
    /* 忘记密码 */
    retrievePwd(data) {
        return fetchPost("/register/retrievePwd", data);
    },
    /* 注册 */
    userRegister(data, registerType) {
        let url = '/register/' + registerType
        // if (data.code == '') {
        //     url = "/register/1";
        // } else {
        //     url = "/register/2";
        // }
        return fetchPost(url, data);//1邮箱，2是手机验证 3.用户名
    },
    /* 修改用户信息 */
    updatUser(data) {
        return fetchPost("/user/updatUser", data);
    },
    /* 修改用户头像 */
    updateAvatar(data) {
        return fetchUpload("/user/updateAvatar", data);
    },
    /* 修改密码 */
    updatePwd(data) {
        return fetchPost("/user/updatePwd", data);
    },
    /* 退出登录 */
    logout() {
        return fetchPost("/logout", []);
    },
    /* 获取验证图片  以及token */
    reqGet(data) {
        return fetchPost("/anjiCaptcha/get", data);
    },
    /* 滑动或者点选验证 */
    reqCheck(data) {
        return fetchPost("/anjiCaptcha/check", data);
    },
    /* 二次校验接口 */
    reqverify(data) {
        return fetchPost("/anjiCaptcha/verify", data);
    },
    /* 发送短信 */
    smsSend(data) {
        return fetchGet("/sms/send", data);
    },
    /* 发送短信 */
    smsCheck(data) {
        return fetchGet("/sms/check", data);
    },
    /* 发送短信前验证 */
    smsBeforeCheck(data) {
        return fetchGet("/sms/beforeCheck", data);
    },
    /* 忘记密码-重置手机用户密码-前置 */
    userResetPwdBefore(data) {
        return fetchGet("/user/resetPwdBefore", data);
    },
    /* 忘记密码-重置手机用户密码 */
    userResetPwd(data) {
        return fetchGet("/user/resetPwd", data);
    },
    /* 第三方绑定手机号 */
    userThirdbindphone(data) {
        return fetchGet("/user/thirdbindphone", data);
    },
    /* 手机注册绑定邮箱 */
    userRegbindemail(data) {
        return fetchPost("/user/regbindemail", data);
    },
    /* 后台账号登录 */
    ndapiLogin(data) {
        return fetchPost("/ndapi/login", data);
    },
    /* 后台链入-自动登录 */
    ndapiAutologin(data) {
        return fetchPost("/ndapi/autologin", data);
    },
};
/* 私聊群聊公用 */
const chatcom = {
    /* 获取会话列表 */
    chatRecent() {
        return fetchGet("/chat/list");
    },
    /* 用户会话操作  */
    chatOper(data) {
        return fetchPost("/chat/oper", data);
    },
    /* 消息操作 */
    msgOper(data) {
        return fetchPost("/chat/msgOper", data);
    },
    /* 合并转发消息 */
    msgMergeForward(data) {
        return fetchPost("/chat/msgMergeForward", data);
    },
    /* 转发消息 */
    msgForward(data) {
        return fetchPost("/chat/msgForward", data);
    },
    /* 通讯录 */
    chatMailList(data) {
        return fetchGet("/chat/mailList", data);
    },
    /* 聊天激活 */
    chatActChat(data) {
        return fetchGet("/chat/actChat", data);
    },
    /* 分享名片 */
    shareCard(data) {
        return fetchPost("/chat/shareCard", data);
    },
    /* 绑定手机号 */
    bindphone(data) {
        return fetchPost("/user/bindphone", data);
    },
    /* 修改手机号 */
    bindnewphone(data) {
        return fetchPost("/user/bindnewphone", data);
    },
    /* 会话已读ack */
    chatReadAck(data) {
        return fetchGet("/chat/readAck", data);
    },
    /* 会话免打扰操作：好友免打扰/群免打扰 */
    msgfreeflag(data) {
        return fetchPost("/chat/msgfreeflag", data);
    },
    /* 举报投诉 */
    sysReport(data) {
        return fetchPost("/sys/report", data);
    },
    showMergeMessage(data) {
        return fetchGet("/chat/showMergeMessage", data);
    },
};
/* 好友 */
const friend = {
    /* 申请数据 */
    getApplyData(data) {
        return fetchGet("/chat/applyData", data);
    },
    /* 申请列表 */
    getApplyFriendList(data) {
        return fetchGet("/chat/applyList", data);
    },
    /* 是否为我的好友 */
    isMyFriend(touid) {
        let cacheName = "/chat/isFriend";
        let key = touid;
        return getWithCache(cacheName, key, 10, async function () {
            var data = null;
            await fetchPost(cacheName, { touid }).then(res => {
                if (res.ok) {
                    data = res.data;
                }
            })
            return data;
        });
    },
    /* 添加好友-检测 */
    checkAddFriend(data) {
        return fetchGet("/chat/checkAddFriend", data);
    },
    /* 添加好友-非验证加好友 */
    chatAddFriend(data) {
        return fetchPost("/chat/addFriend", data);
    },
    /* 修改好友备注名 */
    modifyRemarkname(data) {
        return fetchPost("/friend/modifyRemarkname", data)
    },
    /* 删除好友 */
    delFriend(data) {
        return fetchPost("/chat/delFriend", data);
    },
    /* 同意申请 */
    dealApply(data) {
        return fetchPost("/chat/dealApply", data);
    },
    /* 申请添加好友 */
    friendApply(data) {
        return fetchPost("/chat/friendApply", data);
    },
    /* 搜索用户 */
    searchUser(data) {
        return fetchGet("/user/search", data);
    },
    /* 忽略好友申请 */
    friendIgnoreApply(data) {
        return fetchGet("/friend/ignoreApply", data);
    },
    checkOnlineStatus(data){
        return fetchGet("/friend/isOnline", data);
    }
}
/* 群聊 */
const group = {
    /* at用户列表 */
    atGroupUserList(data) {
        return fetchGet("/chat/atGroupUserList", data);
    },
    /* 更改群名称 */
    modifyName(data) {
        return fetchPost("/group/modifyName", data);
    },
    /* 群聊信息 */
    //查询自己的用户信息标识：1：是；2：否
    getWxGroupInfo(groupid, userflag) {
        let cacheName = "/chat/group";
        let key = groupid + userflag;
        return getWithCache(cacheName, key, 5, async function () {
            var data = null;
            await fetchGet(cacheName, { groupid, userflag }).then(res => {
                if (res.ok) {
                    data = res.data;
                }
            })
            return data;
        });
    },
    getchatGroupInfo(data) {
        return fetchPost("/chat/group", data);
    },
    /* 群成员检测 */
    checkGroupUser(data) {
        return fetchGet('/chat/checkGroupUser', data);
    },
    /* 删除群成员 */
    kickGroup(data) {
        return fetchPost("/chat/kickGroup", data);
    },
    /* 群名片进群检查 */
    checkCardJoinGroup(data) {
        return fetchGet('/chat/checkCardJoinGroup', data);
    },
    /* 邀请成员 */
    directInvite(data) {
        return fetchPost("/chat/joinGroup", data);
    },
    /* 群成员 */
    groupMember(data) {
        return fetchGet("/chat/groupUserList", data);
    },
    /* 修改群简介 */
    modifyIntro(data) {
        return fetchPost("/group/modifyIntro", data);
    },
    /* 添加群公告 单公告 */
    modifyNotice(data) {
        return fetchPost("/group/modifyNotice", data);
    },
    /* 修改群公告 多公告 */
    updateNotice(data) {
        return fetchPost("/group/updateNotice", data);
    },
     /* 删除历史群公告  参数 groupid, noticeid(删除多个公告，id使用逗号隔开,如:1,2,3,4,5) */
    delNotice(data) {
        return fetchPost("/group/delNotice", data);
    },
    
    /* 修改群昵称 */
    modifyGroupNick(data) {
        return fetchPost("/chat/modifyGroupNick", data);
    },
    // 修改群成员标签 参数 groupid, type(1:群主标签, 2:普通成员标签, 3:管理员标签), label
    updateLabel(data) {
        return fetchPost("/group/updateLabel", data);
    },
    /* 离开群聊 */
    leaveGroup(data) {
        return fetchPost("/chat/leaveGroup", data);
    },
    /* 检查是否可以发送卡片 */
    checkSendCard(data) {
        return fetchGet("/chat/checkSendCard", data);
    },
    /* 修改进群方式 */
    modifyApply(data) {
        return fetchPost("/chat/modifyApply", data);
    },
    /* 解散群聊 */
    delGroup(data) {
        return fetchPost("/chat/delGroup", data);
    },
    /* 转让群主 */
    changeOwner(data) {
        return fetchPost("/chat/changeOwner", data);
    },
    /* 可添加好友 */
    applyGroupFdList(data) {
        return fetchGet("/chat/applyGroupFdList", data);
    },
    /* 增加群成员 */
    joinGroup(data) {
        return fetchPost("/chat/joinGroup", data);
    },
    /* 创建群聊 */
    createGroup(data) {
        return fetchPost("/chat/createGroup", data);
    },
    /* 禁言 */
    chatForbidden(data) {
        return fetchPost("/chat/forbidden", data);
    },
    /* 禁言-禁言用户列表 */
    forbiddenUserList(data) {
        return fetchPost("/chat/forbiddenUserList", data);
    },
    /* 用户禁言状态 */
    chatForbiddenFlag(data) {
        return fetchPost("/chat/forbiddenFlag", data);
    },
    /* 修改群审核开关 */
    modifyReview(data) {
        return fetchPost("/group/modifyReview", data);
    },
    /* 群免打扰 */
    modifyGroupPush(data) {
        return fetchPost("/group/modifyGroupPush", data);
    },
    /* 修改群头像 */
    modifyAvatar(data) {
        return fetchUpload("/group/modifyAvatar", data);
    },
    /* 群管理员操作 */
    group_manager(data) {
        return fetchPost("/group/manager", data);
    },
    /* 修改群添加好友开关 */
    modifyFriendFlag(data) {
        return fetchPost("/group/modifyFriendFlag", data);
    },
    /* 群公告滚动显示 */
    modifyNoticeFlag(data) {
        return fetchPost("/group/modifyNoticeRoll", data);
    },
    modifyGroupCardFlag(data) {
        return fetchPost("/group/updateCardSwitch", data);
    },
    modifyMsgBackFlag(data) {
        return fetchPost("/group/updateMsgBack", data);
    },
    // 修改群参数 参数 groupid, isOpen(1开启 2关闭), type(inviteCode(邀请码) memberNum(成员数量))
    modifyGroupSwitch(data){
        return fetchPost("/group/modifyGroupSwitch", data);
    },
    /* 群加入申请 */
    joinGroupApply(data) {
        return fetchPost("/chat/joinGroupApply", data);
    },
    /* 群申请处理 */
    dealGroupApply(data) {
        return fetchPost("/chat/dealGroupApply", data);
    },
    groupApplyInfo(data) {
        return fetchPost("/chat/groupApplyInfo", data);
    },
    /* 设置的所有的成员备注 获取好友和陌生人备注  无参数 */
    getRemarkList(data) {
        return fetchGet("/friend/getRemarkList", data);
    },
    /* 群签到 参数 groupid */
    groupSign(data) {
        return fetchPost("/group/sign", data);
    },
    /* 用户查询自己某月在群组中的个人签到信息  参数 groupid, year, month */
    groupUserSignInfo(data) {
        return fetchPost("/group/userSignInfo", data);
    },
    

    


}

/* 群聊 */
const collect = {
    /* 收藏列表 */
    list(data) {
        return fetchGet("/user/collectList", data);
    },
    /* 添加 */
    add(data) {
        return fetchPost("/user/addCollect", data);
    },
    /* 删除 */
    delete(data) {
        return fetchPost("/user/delCollect", data);
    },
}







const circle = {
    // 查看朋友圈 参数 pageNumber(页数，从0开始)，pageSize(每页显示几条数据，建议为5)
    list(data) {
        return fetchGet("/user/momentsList", data);
    },
    //  获取指定朋友圈 /user/getMomentById 参数 mid
    onlyOne(data) {
        return fetchGet("/user/getMomentById", data);
    },
    // 新增朋友圈  参数 content，videoUrl(视频url，只允许上传一个视频)，imgUrl(图片url，上传多张图片时使用英文字符的 , 隔开)
    add(data) {
        return fetchPost("/user/addMoments", data);
    },
    // 删除朋友圈 参数 mid(朋友圈id)
    delete(data) {
        return fetchPost("/user/delMoments", data);
    },
    // 添加朋友圈评论 参数 mid(朋友圈id)，pid(所回复的评论的id，如果是直接评论朋友圈而不是评论朋友圈的评论传0)，content
    addComment(data) {
        return fetchPost("/user/addComments", data);
    },
    // 删除朋友圈评论 参数 cid(评论id)
    delComment(data) {
        return fetchPost("/user/delComments", data);
    },
    // 朋友圈点赞  参数 mid(朋友圈id)
    addZan(data) {
        return fetchPost("/user/momentLikes", data);
    },
    // 取消朋友圈点赞 /user/cancelLikes 参数 likesId(点赞id)
    delZan(data) {
        return fetchPost("/user/cancelLikes", data);
    },
    // 获取朋友圈消息列表  参数：Integer pageNumber, Integer pageSize
    noticeList(data) {
        return fetchGet("/user/getMomentsMsgList", data);
    },
    // 获取最新一条朋友圈推送消息
    lastCircle(data) {
        return fetchGet("/user/getMomentsReadFlag", data);
    },
    // 清空朋友圈消息列表
    clearNoticeList(data) {
        return fetchPost("/user/clearMomentsMsgs", data);
    },
    // 评论和点赞未读数  无参数
    notReadNoticeNum(data) {
        return fetchGet("/user/unreadCount", data);
    },
    // 修改朋友圈评论和点赞未读状态 /user/readMsg
    readCircleNotice(data) {
        return fetchGet("/user/readMsg", data);
    },


}
const agora = {
    // getAppid
    getBase(data) {
        return fetchPost("/config/base", data);
    },
    // 获取token
    getToken(data) {
        return fetchPost("/chat/getToken", data);
    },
}


export {
    commonView,
    imServer,
    getCommonConfByName,
    getCommonConfList,
    getTurnServer,
    uploadFile,
    currUser,
    user,
    chatcom,
    friend,
    group,
    collect,
    circle,
    agora
}
