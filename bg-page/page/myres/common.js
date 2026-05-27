<#include "/myres/util.js" >


//百度统计
var _hmt = _hmt || [];
(function() {
  var hm = document.createElement("script");
  hm.src = "https://hm.baidu.com/hm.js?33826f1f45b98aa96af3a5ce4ff2e1f8";
  var s = document.getElementsByTagName("script")[0]; 
  s.parentNode.insertBefore(hm, s);
})();

//删除全部重名的cookie
// tio.cookie.removeIfRepeat();
if (tio.cookie.removeIfRepeat()) {
    //location.reload();
}



//上传资源服务器，形如: http://192.168.1.168
var res_server = "${res_server}";   //上传资源服务器
var default_avatar = "/img/tio.jpg?83";  //默认头像
var myFriends = null;  //我的好友（Wx不要用）
var curruser = null;   //当前用户对象，没登录则为null
var curruid = null;     //当前用户的userid，没登录则为null
var curruserid = null;  //同curruid，此处仅仅是为了兼容一下老用户
var isSuper = false;    //是否是超管
var ctx = "${api_ctx}"; //  web-api的contextpath，目前为"/mytio"
var suffix = "${api_suf}";//  web-api的后缀，目前为".tio_x"

var sessionName = '${session_cookie_name}';//session的cookie name
var sessionValue = tio.cookie.getLast(sessionName);  //session的cookie value
tio_autologin();

log("sessionValue", sessionValue);

var tesft_tio_fdfdse = tio.cookie.getLast(tesoegEgac);
var sitename = '${sitename}';  //网站的名字，目前为"t-io社交IM平台"

var pwd_key_login_aes = 'xOezYlYsPebzEolO'; //登录时的密码key
var pwd_key_register_aes = 'iOezXlTsOebzEolU'; //注册时的密码key

/**
 * 自动登录
 */
function tio_autologin() {
    var bs_tio_session = getQueryString("bs_tio_session");
    if (bs_tio_session) {
        // tio.cookie.remove(sessionName);
        tio.cookie.set(sessionName, bs_tio_session, { expires: 150 });
        //赋值后，要在url上把bs_tio_session值清空，否则有严重的安全风险
        changeURLArgs([
            ['bs_tio_session', ""]
        ]);
        var sessionid = bs_tio_session
        ajax.post('/ndapi/autologin', {
            data: {
              sessionid
            },
            async:false,
            success: function (resp) {
                if (resp.ok) {
                    location.reload();
                } else {
                    layer.alert(resp.msg);
                }
            }
        });
    }
}


/**
 * 角色常量
 */
var roles = {
    adminNormal: 1,
    adminSuper: 99,
    normal: 2,
    uploadvideo: 6,
    allow_read_doc: 7,
    paid_doc: 8,
    doc_taixin_doc_role :15,
    paid_sitecode_qijian: 94, //购买了官网源代码旗舰版的角色
    paid_sitecode_baijin: 95, //购买了官网源代码白金版的角色
    paid_tiochat_base: 96     //购买了tio-chat普及版的角色
};




var isAdminNormal = false; //是否是普通管理员，false: 不是普通管理员, true: 是普通管理员
var isNormal = false; //是否是普通注册用户，false: 不是, true: 是
var isLogined = false; //是否登录过了，false: 没有登录, true: 登录过了，匿名用户此值为false

var wsCache = new WebStorageCache();  //本地存储Cache
var tioCache = {
    splitStr : "_&_:",
    /**
     * @param {*} cacheName 
     * @param {*} key 
     * @param {*} value 
     * @param {*} options 譬如缓存一小时: {exp: 3600 * 1}
     */
    set: function (cacheName, key, value, options){
        wsCache.set(cacheName + tioCache.splitStr + key, value, options);
    },
    get: function (cacheName, key){
        var ret = wsCache.get(cacheName + tioCache.splitStr + key);
        return ret;
    }
};

/**
 * 经过缓存获取数据：先从缓存中取，如果缓存没有，则通过fun获取
 * @param {*} cacheName 
 * @param {*} key 
 * @param {*} exp 
 * @param {*} fun 缓存没有数据时，通过该方法获取数据，本方法需要返回数据
 */
function getWithCache(cacheName, key, exp, fun) {
    var data = null;
    data = tioCache.get(cacheName, key);
    if (data) {
        return data;
    }

    data = fun.call(fun);
    if (data) {
        tioCache.set(cacheName, key, data, {exp: exp});
    }
    return data;
}

/**
 * 获取真实的api请求路径
 * @param {*} url 
 */
function path(url) {
    var ret = "";
    if (url.indexOf("?") != -1) {
        var u = url.split("?");
        ret = ctx + u[0] + suffix + "?" + u[1];
    } else {
        ret = ctx + url + suffix;
    }
    return ret;
}

function layui_page_parseData(res) { //res 即为原始返回的数据
    if (res.ok) {
        return {
            "code": 0, //解析接口状态
            "msg": "", //解析提示文本
            "count": res.data.totalRow, //解析数据长度
            "data": res.data.list, //解析数据列表
            "pageSize": res.data.pageSize,
            "pageNumber": res.data.pageNumber
        };
    } else {
        alert(res.msg || "查询失败");
    }
}

/**
 * 给页面url增加pageNumber=1&pageSize=10
 * @param {*} pageNumber 
 * @param {*} pageSize 
 */
function layui_page_done(pageNumber, pageSize) {
    if (pageNumber) {
        changeURLArgs([
            ['pageNumber', pageNumber]
        ]);
    }
    if (pageSize) {
        changeURLArgs([
            ['pageSize', pageSize]
        ]);
    }
}

/**
 * 
 * @param {*} pagePath 形如："/doc/index.html"
 * @param {*} contentSelectStr 内容放置的 形如："#spanid", ".classid"
 *  @param {*} cleanSelectStr 要清空内容的 形如："#spanid", ".classid"
 */
function loadPage(pagePath, contentSelectStr, cleanSelectStr) {
    if (cleanSelectStr) {
        $(cleanSelectStr).html("");
    }
    $.ajax(pagePath, {
        success: function (resp) {
            $(contentSelectStr).html(resp);
        }
    });
}

function show404($container) {
    if ($container) {
        $.ajax("/p400/400-without-header.html", {
            success: function (resp) {
                $container.html(resp);
            }
        });
    } else {
        location.href = "/p400/index.html";
    }
}

var layui_page_limits = [5, 10, 20, 40, 60, 100, 200, 300, 400, 500];


/**
 * 使用方法：
 * 1、res_url("/user/xxx.jpg");
 * 2、res_url(path);
 * @param {*} path 
 */
function res_url(path) {
    if (path) {
        var isAbPath = path.indexOf("http://");
        var isAbsPath = path.indexOf("https://");
        if (isAbPath >= 0 || isAbsPath >= 0) {
            return path;
        } else {
            if (path.indexOf("/") == 0) {
                return res_server + path;
            } else {
                return res_server + "/" + path;
            }
        }
    } else {
        // return res_server;
        return null;
    }
}

/**
 * 获取用户信息
 * @param {*} uid 
 */
function getUserInfo(uid) {
    if (!uid) {
        return null;
    }

    if (uid == curruid) {
        var ret = {};
        $.extend(true, ret, curruser);
        return ret;
    }

    var cacheName = "/user/info";
    if (isSuper) {
        cacheName = "/user/info1";
    }
    
    var key = uid;
    var exp = 10;
    return getWithCache(cacheName, key, exp, function () {
        var data = null;
        ajax.get(cacheName, {
            data: { uid: uid },
            async: false,
            success: function (res) {
                if (res.ok) {
                    data = res.data;
                }
            }
        });
        return data;
    });
}
/**
 * 是否有指定的角色
 * @param {*} user 
 * @param {*} roleid 
 */
function hasRole(user, roleid) {
    if (!user) {
        return false;
    }
    return containRole(user.roles, roleid);
}

/**
 * 
 * @param {*} rolelist 
 * @param {*} roleid 
 */
function containRole(rolelist, roleid) {
    if (!rolelist || rolelist.length == 0) {
        return false;
    }
    for (var i = 0; i < rolelist.length; i++) {
        if (rolelist[i] == roleid) {
            return true;
        }
    }
    return false;
}

/**
 * 根据角色列表得出vip level
 * 0-9级，超管是99
 * @param {*} rolelist 
 */
function vipLevel(rolelist) {
    if (containRole(rolelist, roles.adminSuper)) {
        return 99;
    } else if (containRole(rolelist, roles.paid_sitecode_baijin)) {
        return 6;
    } else if(containRole(rolelist, roles.paid_tiochat_base)) {
        return 5;
    } else if(containRole(rolelist, roles.paid_sitecode_qijian)) {
        return 3;
    } else if(containRole(rolelist, roles.paid_doc)) {
        return 2;
    } else if(containRole(rolelist, roles.allow_read_doc)) {
        return 1;
    } else {
        return 0;
    }
}

/**
 * 创建VIP用户标识
 * @param {*} rolelist 
 */
function createVipFlag(rolelist) {
    return $(createVipFlagStr(rolelist));
}
/**
 * 创建VIP用户标识
 * @param {*} rolelist 
 * @param {*} cls 
 */
function createVipFlagStr(rolelist, cls) {  //cls: vip_to_avatar_common
    var _cls = cls || "";
    var vipStr = "";
    if (true) {//isSuper
        let levelNum=vipLevel(rolelist);
        let levelName=levelNum!=99?'V'+levelNum:'超';
        vipStr=`<i class="tio-level tio-level${levelNum}">${levelName}</i>`
    }
   return vipStr;
}

/**
 * 高亮显示一部分内容
 * @param {*} text 
 * @param {*} searchText 
 * @param {*} style 
 */
function highlightHtml(text, searchText, style) {
    var values = text.split(searchText);
    var ret = values.join('<span style="' + style + '">' + searchText + '</span>');
    return ret;
}
/**
 * 获取剪切板上的图片，注意，剪切板上可能有多个文件，有多少个文件，就会回调多少次
 * @param {*} e 
 * @param {*} cb 回调函数，举例如下
 *  function uploadImg(file) {
 *      var fd = new FormData();
 *      fd.append('uploadFile', file);
 *  }
 */
function getClipboardImg(event, cb, data) {
    var e = event;

    var clipboardData = window.clipboardData; //for IE
    if (!clipboardData) { // for chrome
        clipboardData = e.originalEvent.clipboardData;
    }

    if (clipboardData && clipboardData.items) {
        var items = clipboardData.items;
        if (items) {
            items = Array.prototype.filter.call(items, function (element) {
                return element.type.indexOf("image") >= 0;
            });

            Array.prototype.forEach.call(items, function (item) {
                var blob = item.getAsFile();
                var reader = new FileReader();
                reader.onloadend = function (event) {
                    var imgBase64 = event.target.result;  //    event.target.result.split(",")  [0]=data:image/png;base64  [1]=data
                    // console.log(imgBase64);  // base64
                    var dataURI = imgBase64;
                    var blob = dataURItoBlob(dataURI); // blob
                    // console.log(blob);
                    // uploadImg(blob);

                    cb.call(item, blob, data);
                };
                reader.readAsDataURL(blob);
            });
        }
    }
}

function dataURItoBlob(dataURI)
{
    var byteString = atob(dataURI.split(',')[1]);
    var mimeString = dataURI.split(',')[0].split(':')[1].split(';')[0]
    var ab = new ArrayBuffer(byteString.length);
    var ia = new Uint8Array(ab);
    for (var i = 0; i < byteString.length; i++)
    {
        ia[i] = byteString.charCodeAt(i);
    }
    var bb = new Blob([ab], { "type": mimeString });
    return bb;
}




/**
 * 判断某用户是不是普通管理员
 */
function _isAdminNormal(user) {
    return hasRole(user, roles.adminNormal);
}

//获取当前用户
function curr() {
    ajax.get("/user/curr", {
        async: false, //同步发送
        success: function (resp) {
            // log(resp);
            if (resp.code == appcode.KICKTED) {
                notLogin(); //异常登录提示
            } else {
                if (resp.ok) {
                    var data = resp.data;
                    curruser = data;
                    $.extend(true, curruser, curruser["ipInfo"]);  //把ipInfo的属性赋到一级，保持getUserInfo的兼容性

                    traced_curruser = curruser;
                    curruid = curruser.id;
                    curruserid = curruid;
                    isAdminNormal = _isAdminNormal(curruser);
                    isNormal = hasRole(curruser, roles.normal);
                    isSuper = hasRole(curruser, roles.adminSuper);
                    
                    if(curruser.phonebindflag==2){//未绑定手机号
                      window.location.href="/tioim/home";
                    }
                    
                    log("curruser:", curruser);
                    if (curruser.xx && curruser.xx == 1) {
                        isLogined = false;
                    } else {
                        isLogined = true;
                    }
                } else {
                    return;
                }
            }
        }
    });
}

/**
 * 获取数据字典数据，使用 getDict('case_product_type')
 * @param {*} dict_code 如case_product_type
 */
function getDict(dict_code) {
    var dickey = 'dict_code_' + dict_code;

    var retStr = sessionStorage.getItem(dickey);
    if (retStr) {
        return JSON.parse(retStr);
    }

    var ret = null;
    ajax.get("/dict/child", {
        data: {
            "code": dict_code,
        },
        async: false, //同步发送
        success: function (resp) {
            if (resp.ok) {
                ret = resp.data;
                sessionStorage.setItem(dickey, JSON.stringify(resp.data))
            }
        }
      })
      return ret;
}


//获取我的好友，这个是老官网的代码，不要动也不要用
function getMyFriends() {
    if (!curruser) {
        return;
    }

    ajax.get("/im/getMyFriendIds", {
        async: false, //同步发送
        success: function (resp) {
            if (resp.ok) {
                var data = resp.data;
                myFriends = data;
                if (myFriends == null) {
                    myFriends = [];
                }
            } else {
                myFriends = [];
            }
        }
    });
}

/**
 * 是否是我的好友
 * @param {} otheruid 
 */
function isMyFriend(otheruid) {
    if (!curruser || !otheruid) {
        return false;
    }

    if (!myFriends) {
        getMyFriends();
    }

    for (var index = 0; index < myFriends.length; index++) {
        var e = myFriends[index];
        if (e.uid == otheruid) {
            return true;
        }
    }
}

if (sessionValue) {
    curr();
} else {
    log("没有session cookie");
}
// 以上 added by tanyaowu

//已成登录提示框
function notLogin() {
    // var html = '<div id="notLogin">' +
    //     '<h4>安全提示</h4>' +
    //     '<h6>您的账号在别处登录，当前账号已自动退出！</h6>' +
    //     '<button class="Beginlive againLogin">重新登录</button>' +
    //     '<button class="Beginlive cancel">关闭</button>' +
    //     '</div>';
    // $("body").html(html);
}

/**
 * 添加好友
 * @param {*} uid 
 * @param {*} nick 
 * @param {*} avatar 
 * @param {*} onSuccess 
 * @param {*} onFail 
 */
function tiosite_addFriend(uid, nick, avatar, onSuccess, onFail) {
    $("#tiosite_addFriend_otherAvatar").attr("src", res_url(avatar));
    $("#tiosite_addFriend_otherNick").html(nick);
    document.forms["tiosite_addFriend_form"].frienduid.value = uid;
    layer.open({
        title: "添加好友", //other_nick,
        type: 1,
        offset: "auto",
        closeBtn: 1,
        // scrollbar : false,
        area: ["auto", "290px"], //'450px',//['450px', '450px'],  249+604=853px
        isOutAnim: false, //禁用关闭动画，否则前面关闭再打开时会有问题
        content: $("#tiosite_addFriend"),
        btn: ["加为好友", "取 消"],
        // maxmin: true,

        yes: function (layero, index) {
            var data = $(document.forms["tiosite_addFriend_form"]).serialize();
            ajax.post('/im/addFriend', {
                data: data,
                success: function (resp) {
                    if (resp.ok) {
                        myFriends = null;  //清空我的好友，以便后续再从服务器中获取最新的好友数据
                        layer.close(layero);
                        if (resp.msg) {
                            layer.msg(resp.msg);
                        } else {
                            layer.msg("添加成功");
                        }

                        var addname = "tiosite_addFriend_" + uid;
                        $('[name="' + addname + '"]').hide();

                        var deletename = "tiosite_deleteFriend_" + uid;
                        $('[name="' + deletename + '"]').show();

                        if (onSuccess) {
                            onSuccess.call(onSuccess, resp);
                        }
                    } else {
                        if (resp.msg) {
                            layer.alert(resp.msg);
                        } else {
                            layer.alert("添加失败");
                        }

                        if (onFail) {
                            onFail.call(onFail, resp);
                        }

                    }
                }
            });
        },
        btn2: function (layero, index) {
            //alert(2);
        },
        cancel: function (index, layero) {
            // alert("取消");
        }
    });
}

/**
 * 删除好友
 * @param {*} uid 
 * @param {*} onSuccess 
 * @param {*} onFail 
 */
function tiosite_deleteFriend(uid, onSuccess, onFail) {
    layer.confirm("确认删除好友？", {
        btn: ["确认删除", "取消"] //按钮
    }, function () {
        alert(3);
        ajax.post('/im/deleteFriend?uid=' + uid, {
            // data: data,
            success: function (resp) {
                if (resp.ok) {
                    myFriends = null;  //清空我的好友，以便后续再从服务器中获取最新的好友数据
                    if (resp.msg) {
                        layer.msg(resp.msg);
                    } else {
                        layer.msg("删除成功");
                    }

                    var deletename = "tiosite_deleteFriend_" + uid;
                    $('[name="' + deletename + '"]').hide();

                    var addname = "tiosite_addFriend_" + uid;
                    $('[name="' + addname + '"]').show();

                    if (onSuccess) {
                        onSuccess.call(onSuccess, resp);
                    }
                } else {
                    if (resp.msg) {
                        layer.alert(resp.msg);
                    } else {
                        layer.alert("删除失败");
                    }

                    if (onFail) {
                        onFail.call(onFail, resp);
                    }

                }
            }
        });
    }, function () {

    });
}

/**
 * 渲染私聊框
 * @param {*} addE 
 * @param {*} uid 
 * @param {*} nick 
 * @param {*} avatar 
 * @param {*} roles 
 */
function tiosite_renderP2pChat(addE, uid, nick, avatar, roles) {
    var aid = "search_p2p_chat" + uid;
    var html = "";
    html += '<button title="私聊" type="button" class="layui-btn layui-btn-primary layui-btn-xs " id="' + aid + '">';
    html += '<i class="layui-icon layui-icon-dialogue"></i>';
    html += '</button>';
    if (addE) {
        $(document).on('click', '#' + aid, function () {
            siteim.show_p2p_layer(uid, nick, avatar, roles);
        });
    }
    return html;
}

/**
 * 渲染添加好友、删除好友
 * @param {*} displayAdd 
 * @param {*} displayDelete 
 * @param {*} addE 
 * @param {*} uid 
 * @param {*} nick 
 * @param {*} avatar 
 */
function tiosite_renderAddAndDeleteFriend(displayAdd, displayDelete, addE, uid, nick, avatar) {
    var html = "";

    var addname = "tiosite_addFriend_" + uid;
    html += '<button title="加为好友" style="margin-left:10px;display:' + displayAdd + '" type="button" class="layui-btn layui-btn-primary layui-btn-xs " name="' + addname + '">';
    html += '<i class="layui-icon"></i>';
    html += '</button>';
    if (addE) {
        $(document).on('click', '[name="' + addname + '"]', function () {
            tiosite_addFriend(uid, nick, avatar);
        });
    }

    var deletename = "tiosite_deleteFriend_" + uid;
    html += '<button title="删除好友" style="display:' + displayDelete + '" type="button" class="layui-btn layui-btn-primary layui-btn-xs " name="' + deletename + '">';
    html += '<i class="layui-icon">&#xe640;</i>';
    html += '</button>';
    if (addE) {
        $(document).on('click', '[name="' + deletename + '"]', function () {
            tiosite_deleteFriend(uid);
        });
    }

    return html;
}

function laypage_render(id, data, pageCallback) {
    var laypage = layui.laypage;
    laypage.render({
        elem: id,
        count: data.totalRow //数据总数，从服务端得到
        ,
        curr: data.pageNumber,
        limit: data.pageSize
        , theme: '#9400D3'
        ,
        jump: function (obj, first) {
            //obj包含了当前分页的所有参数，比如：pageNumber = obj.curr;  pageSize = obj.limit;
            // log(obj);
            //首次不执行
            if (!first) {
                pageCallback.call(pageCallback, obj.curr, obj.limit);
            }
        }
    });
}


$(function () {
    if (curruser) {
        $("a[name='tiosite_header_to_usercenter']").attr("href", "/u/" + curruid);   //个人主页
        $("a[name='tiosite_header_to_myblog']").attr("href", "/u/" + curruid + "?qm=1");  //我的博客
        $("a[name='tiosite_header_to_mycollect']").attr("href", "/u/" + curruid + "?qm=1111");  //我的收藏
        $("a[name='tiosite_header_to_mylatest']").attr("href", "/u/" + curruid + "?qm=2222");  //最近访问

        $("img[name='curruser_avatar']").attr("src", res_url(curruser.avatar));
        $("[name='curruser_nick']").html(curruser.nick);
        
    } else {
        $("a[name='tiosite_header_to_usercenter']").hide();   //个人主页
        $("a[name='tiosite_header_to_myblog']").hide();  //我的博客
        $("a[name='tiosite_header_to_mycollect']").hide();  //我的收藏
        $("a[name='tiosite_header_to_mylatest']").hide();  //最近访问
    }

    //处理备案号和公安备案号
    // <span name="gongan_beian_text">浙公网安备 33011802002129号</span>
    //name="gongan_beian_href" href="http://www.beian.gov.cn/portal/registerSystemInfo?recordcode=33011802002129"
    if (window.location.host.indexOf('tiocloud.com') >= 0) {
        $("[name='icp_beian']").html("浙ICP备20001142号-1");
        $("[name='gongan_beian_text']").html("浙公网安备 33011802002134号");
        $("[name='gongan_beian_href']").attr("href", "http://www.beian.gov.cn/portal/registerSystemInfo?recordcode=33011802002134");
    } else if (window.location.host.indexOf('t-io.org') >= 0) {
        $("[name='icp_beian']").html("浙ICP备17032976号-1");
        $("[name='gongan_beian_text']").html("浙公网安备 33011802002129号");
        $("[name='gongan_beian_href']").attr("href", "http://www.beian.gov.cn/portal/registerSystemInfo?recordcode=33011802002129");
	} else {

    }
    
});

//数值转换
function parseNum(num) {
    if (num <= 9999) {
        return num = num;
    } else {
        var num = (num / 10000).toFixed(1);
        return num + "万";
    }
}

/**
 * beautify Date 美化时间
 * 返回：刚刚、几分钟前、几小时前、几年前等
 * @param {*} dateStr 
 */
function btDate(dateStr) {
    if (isBlank(dateStr)) {
        return "时间丢失";
    }

    var s1 = Date.parse(dateStr.replace(/-/g, "/"));
    var s2 = new Date().getTime();
    var iv = s2 - s1;

    var m = Math.round(iv / (1000 * 60));  //经历了多少分钟
    var hour;
    var day;
    var year;
    // 一年525600分钟
    if (m < 1) {
        return "刚刚";
    } else if (m >= 1 && m < 59) {
        return m + "分钟前";
    } else if (m >= 60) {
        hour = Math.round(m / 60);  //经历了多少小时
        if (m < 1440) {
            return hour + "小时前";
        } else {
            day = Math.round(hour / 24);  //经历了多少天
            if (day <= 365) {
                return day + "天前";
            } else {
                year = Math.round(day / 365);  //经历了多少年
                if (year <= 10) {
                    return year + "年前";
                } else {
                    return "很久以前";
                }
            }
        }
    }

    return "时间丢失";
};

/**
 * beautify Number 美化数字
 * 把数字显示成几K、几W、几亿等
 * @param {*} num 
 */
function btNum(num) {
    var k = Math.round(num / 1000);
    if (k < 1) {
        return num;
    } else {
        if (k < 10) {
            return k + "K";
        } else {
            var w = Math.round(num / 10000);
            if (w < 10000) {
                return w + "W";
            } else {
                var b = Math.round(num / 100000000);
                return b + "亿";
            }
        }
    }
}

/**
 * 格式化文件大小, 输出成带单位的字符串
 * @param {Number} size 文件大小
 * @param {Number} [pointLength=2] 精确到的小数点数。
 * @param {Array} [units=[ 'B', 'K', 'M', 'G', 'TB' ]] 单位数组。从字节，到千字节，一直往上指定。
 *    如果单位数组里面只指定了到了K(千字节)，同时文件大小大于M, 此方法的输出将还是显示成多少K.
 */
function formatSize(size, pointLength, units) {
    if (!size) {
        return "0";
    }

    var initsize = size;

    var unit;
    units = units.concat() || ['B', 'K', 'M', 'G', 'TB'];
    while ((unit = units.shift()) && size > 1024) {
        size = size / 1024;
    }
    return /*initsize + "_" + */(unit === 'B' ? size : size.toFixed(pointLength === undefined ? 2 : pointLength)) + unit;
}

/**
 * 格式化文件大小, 输出成带单位的字符串，譬如2K、3M等
 */
function formatSize1(size) {
    return formatSize(size, 2, ['B', 'K', 'M', 'G', 'TB']);
}

/**
 * 时间格式的处理
 * @param {*} second_time 时间戳（秒）
 */
function timeStamp(second_time) {
    var time = parseInt(second_time) + "秒";
    if (parseInt(second_time) > 60) {
        var second = parseInt(second_time) % 60;
        var min = parseInt(second_time / 60);
        time = min + "分" + second + "秒";
        if (min > 60) {
            min = parseInt(second_time / 60) % 60;
            var hour = parseInt(parseInt(second_time / 60) / 60);
            time = hour + "小时" + min + "分" + second + "秒";
            if (hour > 24) {
                hour = parseInt(parseInt(second_time / 60) / 60) % 24;
                var day = parseInt(parseInt(parseInt(second_time / 60) / 60) / 24);
                time = day + "天" + hour + "小时" + min + "分" + second + "秒";
            }
        }
    }
    if (parseInt(second_time) <= 0) {
        time = "0秒";
    }
    return time;
}


/**
 * 日期格式化
 * @param {*} time  时间戳
 * @param {*} fmt  格式：yyyy-MM-dd HH:mm:ss
 */
function formatDateByTime(time, fmt) {
    var date = new Date();
    date.setTime(time);
    var o = {
        "M+": date.getMonth() + 1, //月份 
        "d+": date.getDate(), //日 
        "H+": date.getHours(), //小时 
        "m+": date.getMinutes(), //分 
        "s+": date.getSeconds(), //秒 
        "q+": Math.floor((date.getMonth() + 3) / 3), //季度 
        "S": date.getMilliseconds() //毫秒 
    };
    if (/(y+)/.test(fmt)) {
        fmt = fmt.replace(RegExp.$1, (date.getFullYear() + "").substr(4 - RegExp.$1.length));
    }
    for (var k in o) {
        if (new RegExp("(" + k + ")").test(fmt)) {
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
        }
    }
    return fmt;
}

/**
 * 秒格式化为时分秒
 * @param {*} s  秒
 */
function formatDateByMilliseconds(s) {
    var t;
    if (s > -1) {
        var hour = Math.floor(s / 3600);
        var min = Math.floor(s / 60) % 60;
        var sec = s % 60;
        if (hour < 10) {
            t = '0' + hour + ":";
        } else {
            t = hour + ":";
        }

        if (min < 10) {
            t += "0";
        }
        t += min + ":";
        if (sec < 10) {
            t += "0";
        }
        t += sec;
    }
    return t;
}

/**
 * layer自定义弹窗
 * @param title 自定义弹窗标题
 * @param contentr 自定义弹窗的dom :例$("#content")
 * @param arear  自定义弹窗的大小
 * @param skinr 自定义弹窗名称
 * @param callback 自定义弹窗取消事件的回调函数
 * @param closeBtn 关闭按钮格式
 */

function layerContent(contentr,arear,skinr,title,callback,closeBtn){
    var skinr=skinr?skinr:'layeralert';
    return layer.open({
        title: title||false,
        skin: skinr,
        type: 1,
        shadeClose: false,
        content: contentr,
        area: arear,
        closeBtn:closeBtn,
        cancel: function(index, layero){ 
            callback?callback(index,layero):'';
        }  
    })
}

$(function () {
    // $(".tio_for_showhtml").hide();

    //需要有指定角色才能显示的元素
    $("[tio-roles]").each(function (i, e) {
        var rs = $(e).attr("tio-roles");
        if (rs) {
            var rs_arr = JSON.parse(rs);
            if (rs_arr && rs_arr) {
                $(e).hide();
                for (let index = 0; index < rs_arr.length; index++) {
                    const roleid = rs_arr[index];
                    if (hasRole(curruser, roleid)) {
                        $(e).show();
                        break;
                    }
                }
                
            }
        }
    });
    
});

