import { fetchPost, fetchGet, fetchUpload } from '@/_admin/axios/http';
import { Message } from 'element-ui';

/* 弹框提示 */
export function msgTips(res) {
    if (res.code == 1001) {//未登录
        return;
    } else {
        let msg;
        if (typeof res === 'object') {
            msg = res.msg
        } else {
            msg = res;
        }
        Message({
            message: msg || '程序异常',
            type: "error"
        });
    }

}
/* 接口成功信息提示 */
export function successTips(msg) {
    Message({
        message: msg,
        type: "success"
    });
}
/* 公共配置 */
function sysParams() {
    return fetchGet("/sys/params");
}
/* 上传文件 */
function commonFile(data) {
    return fetchUpload("/common/file", data);
}
/* 上传多个文件 */
function commonFiles(data) {
    return fetchUpload("/common/files", data);
}
/* 字典子节点 */
function mgdictChild(data) {
    return fetchGet("/mgdict/childdict", data);
}
/* 登录 */
function mgLogin(data) {
    return fetchPost("/mglogin", data);
}
/* 退出登录 */
function mgLogout() {
    return fetchGet("/mglogout");
}
/* 登录用户 */
async function mgCurr() {
    return await fetchGet("/mguser/curr");
}
/* 用户菜单 */
function mgMenu() {
    let res = {};
    $.ajax({
        url: process.env.VUE_APP_apiCtx + "/mguser/menu" + process.env.VUE_APP_sufFix,
        async: false,
        success: function (data) {
            res = data;
        }
    })
    return res;
}
/* 用户-修改密码 */
function mgUpdatePwd(data) {
    return fetchPost("/mguser/updatePwd", data);
}
/* 字典列表 */
function mgRoleDictList(data) {
    return fetchGet("/mgrole/dictlist", data);
}
/* 上传封面 */
function caseCoverFile(data) {
    return fetchUpload("/tiocase/caseCover", data);
}
/* 顶部管理 */
const mgheader = {
    /* 最近打开 */
    recentPage: (data) => {
        return fetchGet("/topmenu/recentpage", data);
    },
    /* 收藏列表 */
    favoriteList: (data) => {
        return fetchGet("/topmenu/favoritelist", data);
    },
    /* 添加收藏 */
    addFavorite: (data) => {
        return fetchPost("/topmenu/addfavorite", data);
    },
    /* 删除收藏 */
    delFavorite: (data) => {
        return fetchPost("/topmenu/delfavorite", data);
    },
    /* 收藏排序 */
    topmenuIndex: (data) => {
        return fetchPost("/topmenu/index", data);
    },
};
/* 用户管理 */
const mguser = {
    /* 列表 */
    mgUserList: (data) => {
        return fetchGet("/mguser/list", data);
    },
    /* 用户-删除 */
    mgUserDel: (data) => {
        return fetchPost("/mguser/del", data);
    },
    /* 用户-修改 */
    mgUserUpdate: (data) => {
        return fetchPost("/mguser/update", data);
    },
    /* 用户-新增 */
    mguserAdd: (data) => {
        return fetchPost("/mguser/add", data);
    },
    /* 重置密码 */
    mgResetPwd: (data) => {
        return fetchPost("/mguser/resetPwd", data);
    },
    /* 生成前台账号 */
    synAdminUser: (data) => {
        return fetchPost("/mguser/synAdminUser", data);
    },
    /* 生成前台账号 new 带参数*/
    addUser: (data) => {
        return fetchPost("/tiouser/addUser", data);
    },
    /* 生成前台账号 new 带参数*/
    addUserBatch: (data) => {
        return fetchPost("/tiouser/addUserBatch", data);
    },
    /* 设置靓号 参数 Integer id(用户id), Integer beautifulId(设置的靓号id), String expireTime(过期时间 格式 yyyy-MM-dd) */
    setUserBeautifulId: (data) => {
        return fetchPost("/tiouser/setUserBeautifulId", data);
    },
    // 修改用户余额 /api/updateMoney 参数 uid, type(0：扣钱，1：充钱), money(单位 分)
    changeUserPrice: (data) => {
        return fetchPost("/api/updateMoney", data);
    },
    changeInvite(data) {
        return fetchPost("/api/isOpenInviteCodeShow", data);
    },
    batchDisable(data) {
        return fetchPost("/tiouser/batchDisable", data);
    },
    // 获取所有下级信息  参数 uid
    getUnderUserInfo(data) {
        return fetchPost("/tiouser/getUnderUserInfo", data);
    },
    // 修改个人邀请码  参数 uid, invitecode
    updateInvitecode(data) {
        return fetchPost("/tiouser/updateInvitecode", data);
    },
    // user表导出excel
    exportExcal(data) {
        return fetchPost("/tiouser/download", data);
    },

    /* 谷歌绑定数据获取 */
    bindGoogleData: (data) => {
        return fetchPost("/mguser/bindgoogledata", data);
    },
    /* 绑定谷歌身份验证器 */
    bindGoogle: (data) => {
        return fetchPost("/mguser/bindgoogle", data);
    },
    /* 用户组织查询*/
    listMgInviteOrg: (data) => {
        return fetchPost("/mginviteuser/listMgInviteOrg", data);
    },
    /* 用户组织添加*/
    addMgInviteOrg: (data) => {
        return fetchPost("/mginviteuser/addMgInviteOrg", data);
    },
    /* 用户组织删除*/
    delMgInviteOrg: (data) => {
        return fetchPost("/mginviteuser/delMgInviteOrg", data);
    },
    /* 获取所有管理用户*/
    getAllAdminUser: (data) => {
        return fetchPost("/mginviteuser/getAllAdminUser", data);
    },
    /* 获取组织成员*/
    listInviteUser: (data) => {
        return fetchPost("/mginviteuser/listInviteUser", data);
    },

    


};
/* 菜单 */
const mgauth = {
    /* 同步菜单 */
    initMenu: (data) => {
        return fetchPost("/sys/initmenu", data);
    },
    /* 菜单列表 */
    mgAuthList: (data) => {
        return fetchGet("/mgauth/authlist", data);
    },
    /* 重排序 */
    mgAuthIndex: (data) => {
        return fetchGet("/mgauth/index", data);
    },
    /* 菜单-删除 */
    mgAuthDel: (data) => {
        return fetchPost("/mgauth/del", data);
    },
    /* 菜单-新增 */
    mgAuthAdd: (data) => {
        return fetchPost("/mgauth/add", data);
    },
    /* 菜单-修改 */
    mgAuthUpdate: (data) => {
        return fetchPost("/mgauth/update", data);
    },
    /* 菜单-启用|禁用 */
    mgAuthDisable: (data) => {
        return fetchPost("/mgauth/disable", data);
    },
    /* 菜单-菜单树 */
    mgMenuList: () => {
        return fetchGet("/mgauth/menulist");
    }
};
/* 配置项 */
const mgconf = {
    /* 列表 */
    mgConfList: (data) => {
        return fetchGet("/mgconf/list", data);
    },
    /* 新增 */
    mgConfAdd: (data) => {
        return fetchPost("/mgconf/add", data);
    },
    /* 修改 */
    mgConfUpdate: (data) => {
        return fetchPost("/mgconf/update", data);
    }
};
/* 字典 */
const mgdic = {
    /* 顶层列表 */
    mgDicTopList: (data) => {
        return fetchGet("/mgdict/topList", data);
    },
    /*子节点列表 */
    mgDicChildList: (data) => {
        return fetchGet("/mgdict/childList", data);
    },
    /* 新增 */
    mgDictAdd: (data) => {
        return fetchPost("/mgdict/add", data);
    },
    /* 修改 */
    mgDictUpdate: (data) => {
        return fetchPost("/mgdict/update", data);
    },
    /* 启用禁用 */
    mgDicDisable: (data) => {
        return fetchPost('/mgdict/disable', data);
    },
    /* 删除 */
    mgDicDel: (data) => {
        return fetchPost("/mgdict/del", data);
    },
    /* 重排序 */
    mgDictIndex: (data) => {
        return fetchPost("/mgdict/index", data);
    },
    /* 插入 */
    mgDictInsert: (data) => {
        return fetchPost("/mgdict/insert", data);
    }
};
/* 角色 */
const mgrole = {
    /* 列表 */
    mgRoleList: (data) => {
        return fetchGet("/mgrole/list", data);
    },
    /* 新增 */
    mgRoleAdd: (data) => {
        return fetchPost("/mgrole/add", data);
    },
    /* 修改 */
    mgRoleUpdate: (data) => {
        return fetchPost("/mgrole/update", data);
    },
    /* 删除 */
    mgRoleDel: (data) => {
        return fetchPost("/mgrole/del", data);
    },
    /* 权限树 */
    mgRoleAuthTree: (data) => {
        return fetchGet("/mgrole/roleAuthTree", data);
    },
    /* 分配权限 */
    mgRoleGrant: (data) => {
        return fetchPost("/mgrole/grant", data);
    },
};
/* 菜单操作 */
const authoper = {
    /* 菜单-操作列表 */
    mgAuthOperlist: (data) => {
        return fetchGet("/mgauth/operlist", data);
    },
    /* 菜单-操作新增 */
    mgAuthOperAdd: (data) => {
        return fetchPost("/mgauth/operadd", data);
    },
    /* 菜单-操作修改 */
    mgAuthOperUpdate: (data) => {
        return fetchPost("/mgauth/operupdate", data);
    },
    /* 菜单-操作删除 */
    mgAuthOperDel: (data) => {
        return fetchPost('/mgauth/operdel', data);
    },
    /* 菜单-操作启用禁用 */
    mgAuthOperDisable: (data) => {
        return fetchPost("/mgauth/operdisable", data);
    }
}
/* 页面操作列表 */
function mgAuthPageAuthList(data) {
    return fetchGet("/mgauth/pageAuthList", data)
}
/* 日志统计 */
const logsts = {
    /* 日志列表 */
    loginList: (data) => {
        return fetchGet('/mgloginstat/loginlist', data);
    },
    /* 时间-统计列表 */
    timeList: (data) => {
        return fetchGet("/mgloginstat/timelist", data);
    },
    /* 时间-用户列表 */
    timeUserList: (data) => {
        return fetchGet("/mgloginstat/timeuserlist", data);
    },
    /* 周期下用户日志列表 */
    loginInfoList: (data) => {
        return fetchGet("/mgloginstat/logininfolist", data);
    },
    /* 用户-统计列表 */
    userList: (data) => {
        return fetchGet("/mgloginstat/userList", data);
    },
    /* 用户-天统计列表 */
    userDayList: (data) => {
        return fetchGet("/mgloginstat/userdaylist", data);
    },
    /* 统计 */
    stat: (data) => {
        return fetchGet("/mgloginstat/stat", data);
    },
};
/* 招聘管理 */
const recruit = {
    /* 企业字典列表 */
    cmpDictList: () => {
        return fetchGet("/recruit/cmpdictlist");
    },
    /* 企业-查询列表 */
    cmpQueryList: (data) => {
        return fetchGet("/recruit/cmpuerylist", data);
    },
    /* 企业-新增 */
    cmpAdd: (data) => {
        return fetchPost("/recruit/cmpadd", data);
    },
    /* 企业-修改 */
    cmpUpdate: (data) => {
        return fetchPost('/recruit/cmpupdate', data);
    },
    /* 企业-启用-禁用 */
    cmpDisable: (data) => {
        return fetchPost("/recruit/cmpdisable", data);
    },
    /* 职位-查询列表 */
    recruitQueryList: (data) => {
        return fetchGet("/recruit/recruitquerylist", data);
    },
    /* 职位-新增 */
    recruitAdd: (data) => {
        return fetchPost('/recruit/recruitadd', data);
    },
    /* 职位-修改 */
    recruitUpdate: (data) => {
        return fetchPost("/recruit/recruitupdate", data);
    },
    /* 职位-启用|禁用 */
    recruitDisable: (data) => {
        return fetchPost("/recruit/recruitdisable", data);
    },
    /* 简历-列表 */
    resumeQueryList: (data) => {
        return fetchGet("/recruit/resumequerylist", data);
    }
}
/* 订单管理 */
const order = {
    /* 订单列表 */
    orderList: (data) => {
        return fetchGet("/order/orderlist", data);
    },
    /* 订单关联用户列表 */
    orderUserList: (data) => {
        return fetchGet("/order/orderuserlist", data);
    },
    /* 订单售后列表 */
    orderAfterSalesList: (data) => {
        return fetchGet("/order/orderaftersaleslist", data);
    },
    /* 订单参数信息 */
    orderParam: (data) => {
        return fetchGet('/order/orderparam', data);
    },
    /* 修改订单参数信息 */
    paramUpdate: (data) => {
        return fetchPost('/order/paramupdate', data);
    },
    /* 新增订单 */
    orderAdd: (data) => {
        return fetchPost("/order/orderadd", data);
    },
    /* 修改订单 */
    orderUpdate: (data) => {
        return fetchPost("/order/orderupdate", data);
    },
    /* 新增售后 */
    salesAdd: (data) => {
        return fetchPost("/order/salesadd", data);
    },
    /* 修改售后 */
    salesUpdate: (data) => {
        return fetchPost("/order/salesupdate", data);
    },
    /* 删除售后 */
    delSales: (data) => {
        return fetchPost("/order/delsales", data);
    },
    /* 删除订单 */
    delOrderUser: (data) => {
        return fetchPost("/order/delorderuser", data);
    },
    // 后台流水统计 /api/sysRecords 无参数
    sysRecords: (data) => {
        return fetchGet('/api/sysRecords', data);
    },
};
/* 数据库查询 */
function sqlQuery(data) {
    return fetchGet("/sys/query", data);
}
/* 缓存处理 */
const clear = {
    /* 清空用户缓存 */
    clearUser: (data) => {
        return fetchPost("/tio/clearuser", data);
    },
    /* 清空配置 */
    clearConf: (data) => {
        return fetchPost("/tio/clearconf", data);
    },
    /* 清空静态资源 */
    clearStaticRes: (data) => {
        return fetchPost("/tio/clearstaticres", data);
    },
};
/* 钛信管理 */
const imuser = {
    /* 用户管理列表 */
    userList: (data) => {
        return fetchGet("/tiouser/list", data);
    },
    /* 用户启用|禁用 */
    userDisable: (data) => {
        return fetchPost("/tiouser/disable", data);
    },
    /* 用户-重置密码 */
    resetPwd: (data) => {
        return fetchPost("/tiouser/resetPwd", data);
    },
    /* 用户-注册列表 */
    userRegisterStat: (data) => {
        return fetchGet("/stat/userRegisterStat", data);
    },
    /* 用户-用户列表 */
    statList: (data) => {
        return fetchGet("/tiouser/statlist", data);
    },
    /* 用户数据-ip-周期 */
    userIpTimeRegisterStat: (data) => {
        return fetchGet("/stat/userIpTimeRegisterStat", data);
    },
    /* 用户信息-详情 */
    userDetail: (data) => {
        return fetchGet("/tiouser/info", data);
    }
};
/* 消息管理 */
const imchat = {
    /* 私聊-聊天列表 */
    fdList: (data) => {
        return fetchGet("/friend/fdlist", data);
    },
    /* 私聊-消息列表 */
    p2pList: (data) => {
        return fetchGet("/tiomsg/p2plist", data);
    },
    /* 有效群列表 */
    groupReList: (data) => {
        return fetchGet("/group/list", data);
    },
    /* 无效群 */
    groupDelList: (data) => {
        return fetchGet("/group/dellist", data);
    },
    /* 群列表-消息模型 */
    modeGroupList: (data) => {
        return fetchGet("/group/modegrouplist", data);
    },
    /* 群消息列表-消息模型 */
    groupMsgModeList: (data) => {
        return fetchGet('/tiomsg/grouplist', data);
    },
    /* 群统计 */
    groupStat: (data) => {
        return fetchGet('/stat/groupstat', data);
    }
};
/* 邮件管理 */
const email = {
    /* 发邮件 */
    emailSubmit: (data) => {
        return fetchPost("/email/submit", data);
    },
    /* 邮件服务列表 */
    emailList: (data) => {
        return fetchGet("/email/list", data);
    },
    /* 新增邮件服务 */
    addEmail: (data) => {
        return fetchPost("/email/add", data);
    },
    /* 修改邮件服务 */
    updateEmail: (data) => {
        return fetchPost("/email/update", data);
    },
    /* 删除邮件服务 */
    delEmail: (data) => {
        return fetchPost("/email/del", data);
    }
};
/* app管理 */
const imapp = {
    /* 列表 */
    appList: (data) => {
        return fetchGet("/wxapp/list", data);
    },
    /* 新增 */
    addApp: (data) => {
        return fetchPost("/wxapp/add", data);
    },
    /* 修改 */
    updateApp: (data) => {
        return fetchPost("/wxapp/update", data);
    },
    /* 删除 */
    delApp: (data) => {
        return fetchPost("/wxapp/del", data);
    }
};
/* 发票管理 */
const invoice = {
    /* 发票列表 */
    invoiceList: (data) => {
        return fetchGet("/invoice/invoicelist", data);
    },
    /* 研发修改 */
    develop: (data) => {
        return fetchPost("/invoice/develop", data);
    },
    /* 关联用户 */
    invoiceUsers: (data) => {
        return fetchGet("/invoice/users", data);
    },
    /* 单标记 */
    invoiceUpdate: (data) => {
        return fetchPost("/invoice/updatestatus", data);
    },
    /* 批量标记 */
    batchUpdate: (data) => {
        return fetchPost("/invoice/batchupdate", data);
    },
    /* 查询条件下总金额 */
    invoiceTotal: (data) => {
        return fetchGet("/invoice/total", data);
    },
    /* 用户-列表 */
    userInvoiceList: (data) => {
        return fetchGet("/invoice/userinvoicelist", data);
    },
    /* 用户-总金额 */
    userTotal: (data) => {
        return fetchGet("/invoice/usertotal", data);
    },
    /* 新增发票 */
    addInvoice: (data) => {
        return fetchUpload("/invoice/add", data);
    },
    /* 修改发票 */
    updateInvoice: (data) => {
        return fetchUpload("/invoice/update", data);
    }
};
/* 报销管理 */
const reimb = {
    /* 报销单列表 */
    reimBurseList: (data) => {
        return fetchGet("/invoice/reimburselist", data);
    },
    /* 报销单基础信息 */
    reimBurseInfo: (data) => {
        return fetchGet("/invoice/reimburseinfo", data);
    },
    /* 发票列表-报销单内 */
    invoiceReimBurseList: (data) => {
        return fetchGet("/invoice/invoicereimburselist", data);
    },
    /* 发票列表-非报销单内 */
    invoiceOutReimBurseList: (data) => {
        return fetchGet("/invoice/invoiceoutreimburselist", data);
    },
    /* 生成报销单code */
    getrCode: (data) => {
        return fetchGet("/invoice/getrcode", data);
    },
    /* 新增报销单-含新增发票 */
    addReimBurse: (data) => {
        return fetchPost("/invoice/addreimburse", data);
    },
    /* 修改报销单-含新增发票 */
    updateReimBurse: (data) => {
        return fetchPost("/invoice/updatereimburse", data);
    },
    /* 修改发票备注 */
    updateremark: (data) => {
        return fetchPost("/invoice/updateremark", data);
    },
    /* 删除报销单 */
    delReimBurse: (data) => {
        return fetchPost("/invoice/delreimburse", data);
    },
    /* 删除发票 */
    delReimBurseInvoice: (data) => {
        return fetchPost("/invoice/delreimburseinvoice", data);
    },
    /* 下载 */
    upload: (data) => {
        return fetchGet("/invoice/upload", data);
    },
    /* /invoice/reimbursedict */
    reimBursedict: (data) => {
        return fetchGet("/invoice/reimbursedict", data);
    },
}
/* 日志管理 */
const journal = {
    /* 日志列表 */
    loginList: (data) => {
        return fetchGet("/tiologin/loginlist", data);
    },
    /* 时间维度-列表 */
    timeList: (data) => {
        return fetchGet("/tiologin/timelist", data);
    },
    /* 时间维度-天-用户列表 */
    timeUserList: (data) => {
        return fetchGet("/tiologin/timeuserlist", data);
    },
    /* 时间维度-用户-日志列表 */
    timeLoginList: (data) => {
        return fetchGet("/tiologin/timeloginlist", data);
    },
    /* IP维度-列表 */
    ipList: (data) => {
        return fetchGet("/tiologin/iplist", data);
    },
    /* IP维度-天-列表 */
    ipDayList: (data) => {
        return fetchGet("/tiologin/ipdaylist", data);
    },
    /* IP维度-天-用户列表 */
    ipUserList: (data) => {
        return fetchGet("/tiologin/ipuserlist", data);
    },
    /* IP维度-天-用户-日志列表 */
    ipLoginList: (data) => {
        return fetchGet("/tiologin/iploginlist", data);
    },
};
/* 统计管理 */
const stat = {
    /* 区域字典列表 */
    areadict: (data) => {
        return fetchGet("/stat/areadict", data);
    },
    /* 用户总登录次数 */
    userlogincount: (data) => {
        return fetchGet("/stat/userlogincount", data);
    },
    /* ip注册人数 */
    ipregcount: (data) => {
        return fetchGet("/stat/ipregcount", data);
    },
    /* 区域注册人数 */
    arearegcount: (data) => {
        return fetchGet("/stat/arearegcount", data);
    },
    /* 时间注册人数 */
    timeregcount: (data) => {
        return fetchGet("/stat/timeregcount", data);
    },
};
/* 会话管理 */
const chat = {
    /* 会话列表 */
    chatList: (data) => {
        return fetchGet('/wxchat/chatlist', data);
    },
    /* 会话消息列表 */
    chatMsgList: (data) => {
        return fetchGet('/wxchat/chatmsglist', data);
    },
    /* 好友列表 */
    friendList: (data) => {
        return fetchGet("/wxchat/friendlist", data);
    },
    /* 好友消息列表 */
    friendMsgList: (data) => {
        return fetchGet("/wxchat/friendmsglist", data);
    },
    /* 好友申请记录 */
    applyList: (data) => {
        return fetchGet("/wxchat/applylist", data);
    },
    /* 群列表 */
    groupList: (data) => {
        return fetchGet("/wxchat/grouplist", data);
    },
    /* 群信息 */
    groupInfo: (data) => {
        return fetchGet("/wxchat/groupinfo", data);
    },
    /* 群用户列表 */
    groupUserList: (data) => {
        return fetchGet("/wxchat/groupuserlist", data);
    },
    /* 群消息列表 */
    groupMsgList: (data) => {
        return fetchGet("/wxchat/groupmsglist", data);
    }
};
/* 案例管理 */
const caseList = {
    /* 案例列表 */
    tiocaseList: (data) => {
        return fetchPost("/tiocase/list", data);
    },
    /* 案例信息 */
    tiocaseInfo: (data) => {
        return fetchPost("/tiocase/info", data);
    },
    /* 案例-修改数据 */
    tiocaseUpdate: (data) => {
        return fetchPost("/tiocase/update", data);
    }
}
/* 群聊管理 */
const newgroupList = {
    /* 案例列表 */
    managerlist: (data) => {
        return fetchGet("/group/managerlist", data);
    },
    /**封停操作 */
    inblack: (data) => {
        return fetchPost("/group/inblack", data);
    },
    /* 封停列表 */
    inblackoperlist: (data) => {
        return fetchGet("/group/inblackoperlist", data);
    },
    /* 禁言用户列表 */
    forbiddenUserList: (data) => {
        return fetchGet("/group/forbiddenUserList", data);
    },
    /* 管理员用户列表 */
    managerUserList: (data) => {
        return fetchGet("/group/managerUserList", data);
    },
    /* 群举报列表列表 */
    reportlist: (data) => {
        return fetchGet("/group/reportlist", data);
    },
    /* 举报标记 */
    reportdeal: (data) => {
        return fetchPost("/group/reportdeal", data);
    },
    // 修改群组虚拟人数  参数 groupid, vnum
    updateVnum: (data) => {
        return fetchPost("/group/updateVnum", data);
    },
}
/* 钱包红包 */
const redPrurse = {
    /* 开户列表 */
    openlist: (data) => {
        return fetchGet("/red/openlist", data);
    },
    /* 红包列表 */
    redlist: (data) => {
        return fetchGet("/red/redlist", data);
    },
    /* 提现列表 */
    withholdlist: (data) => {
        return fetchGet("/red/withholdlist", data);
    },
    /* 充值列表 */
    rechargelist: (data) => {
        return fetchGet("/red/rechargelist", data);
    },
    /* 钱包明细 */
    coinlist: (data) => {
        return fetchGet("/red/coinlist", data);
    }
}
// 底部导航
const lowerNav = {
    /* 列表 */
    list: (data) => {
        return fetchPost("/api/getDiscoveryPageInfo", data);
    },
    add: (data) => {
        return fetchUpload("/api/setDiscoveryPageInfo", data);
    },
    edit: (data) => {
        return fetchUpload("/api/updateDiscoveryPageInfo", data);
    },
    delete: (data) => {
        return fetchPost("/api/delDiscoveryPageInfo", data);
    },
}
// File 文件相关
const file = {
    // 上传文件到服务器
    uploadFile: (data) => {
        return fetchUpload("/api/uploadFile", data)
    },
    // 将本地file对象转化为可以显示的数据
    getLocalImg: async (file) => {
        var fr = new FileReader();
        var that = this;
        fr.readAsDataURL(file.raw);
        fr.onload = function (e) {
            return e.target.result;
        };
    },
    // 上传文件到服务器,本地钱包
    uploadFileLocalPurse: (data) => {
        return fetchUpload("/api/uploadPaymentImg", data)
    },
}
const baseTools = {
    toFormData: (data) => {
        let formData = new FormData();
        for (var prop in data) {
            formData.append(prop, data[prop]);
        }
        return formData;
    }
}
// 默认好友相关
const defaultFriends = {
    /* 列表 */
    list: (data) => {
        return fetchPost("/api/getDefaultFriend", data);
    },
    add: (data) => {
        return fetchUpload("/api/addDefaultFriend", data);
    },
    edit: (data) => {
        return fetchUpload("/api/updateDefaultFriend", data);
    },
    delete: (data) => {
        return fetchPost("/api/delDefaultFriend", data);
    },
    getConfig: (data) => {
        return fetchPost("/api/getDefaultFriendConfig", data);
    },
    setConfig: (data) => {
        return fetchUpload("/api/updateDefaultFriendConfig", data);
    },
}

// 本地钱包相关
const localPurse = {
    /* 列表 */
    PayImgGet: (data) => {
        return fetchGet("/api/getPayImg", data);
    },
    PayImgUpdata: (data) => {
        return fetchUpload("/api/updatePayImg", data);
    },
    PayImgSet: (data) => {
        return fetchUpload("/api/setPayImg", data);
    },
    // 确认
    confirmLog: (data) => {
        return fetchPost("/api/confirmationOfPayment", data);
    },
    // 获取充值记录
    getLogFormRecharge: (data) => {
        return fetchPost("/api/getRechargePaymentItem", data);
    },
    // 获取提现记录
    getLogFormWithdrawal: (data) => {
        return fetchPost("/api/getWithholdPaymentItem", data);
    },
    // 获取用户提现信息
    getUserPayQrcode: (data) => {
        return fetchGet("/api/getUserPayInfo", data);
    },
}

// 用户实名
const realname = {
    // 获取实名认证列表  参数 无
    list: (data) => {
        return fetchGet("/api/getRealNameCertificationList", data);
    },
    // 审核实名信息  参数:uid, status(1: 通过，-1: 未通过)
    update: (data) => {
        return fetchPost("/api/verifyUserReal", data);
    }

}
// 积分
const score = {
    // 获取签到任务表 /api/getSignTask 参数 无
    list: (data) => {
        return fetchGet("/api/getSignTask", data);
    },
    // 添加签到任务 /api/addSignTask 参数 signDay(连续签到天数), rewardIntegral(奖励积分), taskDescribe(任务描述)
    add: (data) => {
        return fetchPost("/api/addSignTask", data);
    },
    // 修改签到任务 /api/updateSignTask 参数 id, signDay, rewardIntegral, describe
    edit: (data) => {
        return fetchPost("/api/updateSignTask", data);
    },
    // 删除签到任务 /api/delSignTask 参数 id
    delete: (data) => {
        return fetchPost("/api/delSignTask", data);
    },
    // 查看签到规则 /api/getSignRole 无参数
    getSignRole: (data) => {
        return fetchGet("/api/getSignRole", data);
    },
    // 更新签到规则(没有的时候添加，有的时候更新) /api/updateSignRole 参数 content(规则内容)
    updateSignRole: (data) => {
        return fetchPost("/api/updateSignRole", data);
    },
}
// 公告
const notice = {
    // 获取公告列表 /api/getNotice参数 无
    list: (data) => {
        return fetchGet("api/getNotice", data);
    },
    // 添加新的公告 /api/addNotice 参数 title, content
    add: (data) => {
        return fetchPost("api/addNotice", data);
    },
    // 修改公告内容 /api/updateNotice 参数 id,title, content
    edit: (data) => {
        return fetchPost("api/updateNotice", data);
    },
    // 删除公告 /api/delNotice参数 id
    delete: (data) => {
        return fetchPost("api/delNotice", data);
    },
}

// 客户端设置
const setting = {
    // 查询客户端配置
    appSwitchList: (data) => {
        return fetchGet("api/getClientConf", data);
    },
    // 添加客户端配置 /api/addClientConf 参数  name，value，describe
    appSwitchAdd: (data) => {
        return fetchPost("api/addClientConf", data);
    },
    // 修改客户端配置 /api/updateClientConf 参数 name，value，describe
    appSwitchEdit: (data) => {
        return fetchPost("api/updateClientConf", data);
    },
    // 删除
    appSwitchDelete: (data) => {
        return fetchPost("#", data);
    },
    // 查询指定客户端配置 /api/getClientConfByName 参数 name
    appSwitchByName: (data) => {
        return fetchGet("api/getClientConfByName", data);
    },
}

// 圈子中心，帮办
// 圈子管理:
// 投诉管理:
// 查看投诉列表 /circle/circleComplaintList 参数 Integer status(null: 查询所有投诉信息，0：未审核的投诉列表，1：已通过的投诉列表，2：已拒绝的投诉列表)
// 投诉详情 /circle/circleComplaintInfo 参数 Integer complaintId(投诉id)
// 审核投诉 /circle/examineComplaint 参数 Integer complaintId, Integer status(1 通过 2拒绝)
const circleCenter = {
    // 圈子申请创建列表  参数 String searchKey, Short status, Integer pageNumber, Integer pageSize
    list: (data) => {
        return fetchGet("/circle/applyList", data);
    },
    // 审核创建圈子申请  参数 Integer circleApplyId, Integer status(1:通过审核，-1:拒绝审核), String refuseReason(拒绝理由)
    update: (data) => {
        return fetchPost("/circle/updateApplyStatus", data);
    },
    // 圈子列表  参数 String searchKey, Short status(null(不传参数): 查询所有圈子。1:正常状态的圈子 2:被封的圈子), Integer pageNumber, Integer pageSize
    circleList: (data) => {
        return fetchPost("/circle/circleList", data);
    },
    // 修改圈子状态  参数 Integer circleId, Integer status(2:封圈子，1：解封)
    updateCircleStatus: (data) => {
        return fetchPost("/circle/updateCircleStatus", data);
    },
    // 查看圈子成员列表  参数 Integer circleId
    circleMemberList: (data) => {
        return fetchPost("/circle/circleMemberList", data);
    },
    // 修改圈子信息  参数 Integer circleId, String avatar, String name, String describe, Integer isOpen, Integer isExamine, Integer isInvite, Integer inviteNum
    updateCircle: (data) => {
        return fetchPost("/circle/updateCircle", data);
    },
    // 设置推荐圈子  参数 Integer circleId, Integer status
    updateCircleRecommend: (data) => {
        return fetchPost("/circle/updateCircleRecommend", data);
    },
    // 获取热门城市列表 /circle/recommendCityList 参数 无
    recommendCityList: (data) => {
        return fetchPost("/circle/recommendCityList", data);
    },
    // 设置推荐城市  参数 Integer cityId, Integer status
    setCityRecommend: (data) => {
        return fetchPost("/circle/setCityRecommend", data);
    },
    // 查询所有城市信息  参数 String searchKey
    cityList: (data) => {
        return fetchPost("/circle/cityList", data);
    },
    // 圈子文章列表 参数 String searchKey, Integer pageNumber, Integer pageSize
    circleArticleList: (data) => {
        return fetchPost("/circle/circleArticleList", data);
    },
    // 查看文章评论点赞列表  参数 articleId(文章id)
    articleDetails: (data) => {
        return fetchPost("/circle/articleDetails", data);
    },
    // 删除圈子文章评论  参数 commentId(评论id)
    articleCommentDel: (data) => {
        return fetchPost("/circle/articleCommentDel", data);
    },
    // 删除圈子文章  参数 String articleIds(文章id 使用 , 分隔开)
    circleArticleDel: (data) => {
        return fetchPost("/circle/circleArticleDel", data);
    },
    // 查看投诉列表  参数 Integer status(null: 查询所有投诉信息，0：未审核的投诉列表，1：已通过的投诉列表，2：已拒绝的投诉列表)
    circleComplaintList: (data) => {
        return fetchPost("/circle/circleComplaintList", data);
    },
    // 投诉详情  参数 Integer complaintId(投诉id)
    circleComplaintInfo: (data) => {
        return fetchPost("/circle/circleComplaintInfo", data);
    },
    // 审核投诉  参数 Integer complaintId, Integer status(1 通过 2拒绝)
    examineComplaint: (data) => {
        return fetchPost("/circle/examineComplaint", data);
    },
    // 设置用户角色  参数 Integer circleId, Integer uid, Integer role(1:圈主, 2:管理员, 3:普通成员)
    setMemberRole: (data) => {
        return fetchPost("/circle/setMemberRole", data);
    },
    // 移除圈子用户  参数 Integer circleId, String uids
    delMember: (data) => {
        return fetchPost("/circle/delMember", data);
    },
    // 删除圈子  参数 circleId
    delCircle: (data) => {
        return fetchPost("/circle/delCircle", data);
    },



}
const album = {
    // 相册列表  参数 String searchKey, Integer pageNumber, Integer pageSize
    albumList: (data) => {
        return fetchGet("/album/albumList", data);
    },
    // 删除相册  参数 String albumIds(相册id 使用 , 分隔开)
    albumDel: (data) => {
        return fetchPost("/album/circleDel", data);
    },
    // 查看相册图片  参数 Integer albumId(相册id)
    photos: (data) => {
        return fetchGet("/album/photos", data);
    },
    // 删除相册图片  参数 String photoIds(图片id 使用 , 分隔开)
    delPhotos: (data) => {
        return fetchPost("/album/delPhotos", data);
    },
}




const report = {
    // 获取群组投诉列表  参数 Integer pageNumber,Integer pageSize,String searchkey,Short status(2: 未处理，3: 已处理)
    groupList: (data) => {
        return fetchGet("/group/reportlistnew", data);
    },
    // 处理群组投诉  参数 String ids, Integer status(0 不对群组操作 1 封停群组), String reason
    groupHandle: (data) => {
        return fetchPost("/group/handlingReport", data);
    },
    // 获取用户投诉列表  参数 Integer pageNumber,Integer pageSize,String searchkey,Short status(2: 未处理，3: 已处理)
    friendList: (data) => {
        return fetchGet("/tiouser/reportlistnew", data);
    },
    // 处理用户投诉  参数 String ids, Integer status(0 不对用户操作 1 禁用用户)
    friendHandle: (data) => {
        return fetchPost("/tiouser/handlingReport", data);
    },
}
// ip黑名单
const ipBlacks = {
    // 黑名单列表  参数 pageNumber, pageSize
    list: (data) => {
        return fetchGet("/api/getIpBlackList", data);
    },
    // 添加黑名单  参数 ip, remark
    add: (data) => {
        return fetchPost("/api/addIpBlack", data);
    },
    // 删除黑名单  参数 ip, remark
    delete: (data) => {
        return fetchPost("/api/deleteIpBlack", data);
    },
}

// 默认群组
const defaultGroups = {
    // 查看默认群组 
    list: (data) => {
        return fetchGet("/api/getDefaultGroup", data);
    },
    // 添加默认群组  参数 groupid
    add: (data) => {
        return fetchPost("/api/addDefaultGroup", data);
    },
    // 修改默认群组  参数 id, groupid, isOpen(0不使用，1使用)
    edit: (data) => {
        return fetchPost("/api/updateDefaultGroup", data);
    },
    // 删除默认群组  参数 id
    delete: (data) => {
        return fetchPost("/api/delDefaultGroup", data);
    },
}



export {
    sysParams,
    commonFile,
    commonFiles,
    caseCoverFile,
    mgdictChild,
    mgLogin,
    mgLogout,
    mgCurr,
    mgMenu,
    mgUpdatePwd,
    mgRoleDictList,
    mgheader,
    mguser,
    mgauth,
    mgconf,
    mgdic,
    mgrole,
    authoper,
    mgAuthPageAuthList,
    logsts,
    recruit,
    order,
    sqlQuery,
    clear,
    imuser,
    imchat,
    email,
    imapp,
    invoice,
    reimb,
    journal,
    stat,
    chat,
    caseList,
    newgroupList,
    redPrurse,
    lowerNav,
    file,
    baseTools,
    defaultFriends,
    localPurse,
    realname,
    score,
    notice,
    setting,
    circleCenter,
    album,
    report,
    ipBlacks,
    defaultGroups
}