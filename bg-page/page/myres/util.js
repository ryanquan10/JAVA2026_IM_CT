<#if console.log == true >
var log = console.log.bind(console);
<#else>
var log = function () { }; 
</#if>

<#if console.info == true >
var info = console.info.bind(console); 
<#else>
var info = function () { };
</#if>

<#if console.error == true >
var error = console.error.bind(console); 
<#else>
var error = function () { }; 
</#if>

// var initAlert = window.alert;//保修原来的alert函数
// window.alert = function (msg) {//覆盖原来的alert函数
//     try {
//         if (msg.indexOf("百度未授权使用地图API") != -1) {
//             return;
//         } else {
//             layer.alert(msg);
//         }
//     } catch (error) {
//         layer.alert(msg);
//     }
// }

if (typeof(tt) == 'undefined') {
    tt = {};
}

tt.rmNull = function(arr) {
	var temp = [];
	for (var i = 0; i < arr.length; i++) {
		if (arr[i]) {
			temp.push(arr[i]);
		}
	}
	return temp;
};

Array.prototype.ttCons = function(e) {
	i = 0;
	for (; i < this.length && this[i] != e; i++);
	return !(i == this.length);
};

Array.prototype.ttIndexOf = function(obj) {
	for (var i = 0; i < this.length; i++) {
		if (this[i] == obj) {
			return i;
		}
	}
	return -1;
};
/**
 * 
 * @param {}
 *            e
 * @return {} true:包含
 */

/**
 * 删除指定序号的元素
 * 
 * @param {}
 *            index
 */
Array.prototype.ttRmAt = function(index) {
	this.splice(index, 1);
	return this;
};
/**
 * 从数组中删除指定元素
 * 
 * @param {}
 *            obj
 */
Array.prototype.ttRm = function(obj) {
	var index = this.ttIndexOf(obj);
	if (index >= 0) {
		this.ttRmAt(index);
	}
	tt.rmNull(this);
	return this;
};


var _appcode_start = 1000;
var appcode = {
    NOTLOGIN : 1 + _appcode_start,  //没有登录 1001
    TIMEOUT : 2 + _appcode_start,  //登录超时
    KICKTED : 3 + _appcode_start,  // app帐号在其它地方登录
    NOTPERMISSION : 4 + _appcode_start,//登录了，但是没有权限操作
    REFUSE : 5 + _appcode_start,//拒绝访问
    NEED_ACCESS_TOKEN : 6 + _appcode_start,//需要提供正确的access_token
    CAPTCHA_ERROR : 7 + _appcode_start, // 图形验证码错误code
    KICKTED_PC:9 + _appcode_start // PC 帐号在其它地方登录
}; 


var tEifosafjieo = "oxjyj/8aklao/uijkkjujkdfla/34g787doperjglnsafhxikjy--";
// var tesoegEgac1 = "t9P8i6J9o";
var tsvtesfvcomparedcode = 996;//1006

/**
 * @param {*} source 
 * @param {*} destination
 */
var copy = function (destination, source) {
    tEifosafjieo += "";
    tesoegEgac += "";
    //destination, source
    if (source && destination) {
        for (var propertyName in source) {
            destination[propertyName] = source[propertyName];
        }
    }
};

// 获取浏览器窗口的可视区域的宽度
function getViewPortWidth() {
    return document.documentElement.clientWidth || document.body.clientWidth;
}
 
// 获取浏览器窗口的可视区域的高度
function getViewPortHeight() {
    return document.documentElement.clientHeight || document.body.clientHeight;
}
 
// 获取浏览器窗口水平滚动条的位置
function getScrollLeft() {
    return document.documentElement.scrollLeft || document.body.scrollLeft;
}
 
// 获取浏览器窗口垂直滚动条的位置
function getScrollTop() {
    return document.documentElement.scrollTop || document.body.scrollTop;
}

/**
 * 深度拷贝，
 * @param {*} obj 
 */
function deepCopy(obj) {
    var c = obj instanceof Array ? [] : {};
    for (var i in obj) if (obj.hasOwnProperty(i)) {
        var prop = obj[i];
        if (typeof prop == 'object') {
            if (prop instanceof Array) {
                c[i] = [];
                for (var j = 0; j < prop.length; j++) {
                    if (typeof prop[j] != 'object') {
                        c[i].push(prop[j]);
                    } else {
                        c[i].push(deepCopy(prop[j]));
                    }
                }
            } else {
                c[i] = deepCopy(prop);
            }
        } else {
            c[i] = prop;
        }
    }
    return c;
}

var inittesoegEgac = function() {
    //取tPiJo
    // t9P8i6J9o
    tesoegEgac = 'tio_access_token';//tesoegEgac1.substr(0, 1) + tesoegEgac1.substr(2, 1) + tesoegEgac1.substr(4, 1) + tesoegEgac1.substr(6, 1) + tesoegEgac1.substr(8, 1);
    tsvtesfvcomparedcode += 10;
}
inittesoegEgac();
/**
 * 获取url的参数值
 * @param {*} name 
 */
function getQueryString(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
    var r = window.location.search.substr(1).match(reg);
    if (r != null) {
        var v = r[2];
        return decodeURIComponent(v);
    }
    return null;
}

/**
 * 获取请求路径
 * https://local.t-io.org/2/case/index.html --> "/2/case/index.html"
 * https://local.t-io.org/2/case/ --> "/2/case/"
 * https://local.t-io.org/2/case/565465465465 --> "/2/case/565465465465"
 */
function getRequestPath() {
    return location.pathname;
    // var url = document.location.toString();
    // var arrUrl = url.split("//");
    // var start = arrUrl[1].indexOf("/");
    // var relUrl = arrUrl[1].substring(start); //stop省略，截取从start开始到结尾的所有字符
    // if (relUrl.indexOf("?") != -1) {
    //     relUrl = relUrl.split("?")[0];
    // }
    // return relUrl;
}
/**
 * 获取请求路径的最后一部分
 * https://local.t-io.org/2/case/index.html --> "index.html"
 * https://local.t-io.org/2/case/ --> ""
 * https://local.t-io.org/2/case/565465465465 --> "565465465465"
 */
function getLastSubPath() {
    var fullpath = getRequestPath();
    var patharr = fullpath.split("/");
    return patharr[patharr.length - 1];
}

/**
 * 获取url的参数值，如果获取不到则取路径的最后一个子部分
 * https://local.t-io.org/2/case/565465465465 --> "565465465465"
 * https://local.t-io.org/2/case/caseInfo.html?id=565465465465 --> "565465465465"
 * @param {*} name 参数名，譬如id,name
 */
function getQueryStringOrLastSubPath(name) {
    var ret = getQueryString(name);
    if (!ret) {
        ret = getLastSubPath();
    }
    return ret;
}

/**
 * 随机生成字符串（当前主要用于生成校验码）
 * @param {*} len 
 */
function randomString(len) {
    len = len || 32;
    var $chars = 'ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678';    /****默认去掉了容易混淆的字符oOLl,9gq,Vv,Uu,I1****/
    var maxPos = $chars.length;
    var pwd = '';
    for (i = 0; i < len; i++) {
        pwd += $chars.charAt(Math.floor(Math.random() * maxPos));
    }
    return pwd;
}


// var key = CryptoJS.enc.Utf8.parse("uPezilSoTLyzkMop");
/**
 * [encrypt 加密]
 * @return {[type]} [description]
 */
function aes_encrypt(content, key) {
    var encryptResult = CryptoJS.AES.encrypt(content, key, {
        iv: key,
        mode: CryptoJS.mode.CBC,
        padding: CryptoJS.pad.Pkcs7
    });
    return encryptResult;
}

/**
 * [decrypt 解密]
 * @return {[type]} [description]
 */
function aes_decrypt(content, key) {
    var bytes = CryptoJS.AES.decrypt(content.toString(), key, {
        iv: key,
        mode: CryptoJS.mode.CBC,
        padding: CryptoJS.pad.Pkcs7
    });
    var decryptResult = bytes.toString(CryptoJS.enc.Utf8);
    return decryptResult;
}


function tio_close_ajax_layer_load(conf) {
    var loadindex = conf['tio_ajax_layer_load_index'];
    if (loadindex) {
        try {
            conf['tio_ajax_layer_load_index'] = null;
            // layer.close(loadindex);
            setTimeout(function(){layer.close(loadindex);}, 10);
        } catch (error) {

        }
    }
}

/**
 * 跳转到登录页
 * @param {*} fromPath 可以为null
 */
 function toLoginPage(fromPath) {
    window.location.href='/tioim/login?redirect_uri_after_login=' + encodeURIComponent(location.href) + "&from_ajax=" + encodeURIComponent(fromPath); 
}

/**
 * ajax.get("/user/curr", {
 *      success:function(resp){
 *          
 *      },
 *      showload:1  //请求时，显示正在加载
 * });
 * 
 * ajax.post("/xx/create", {
 *      success:function(resp){
 *          
 *      }
 * });
 */
var ajax = {
    mycomplete: function (conf, complete) {
        this.h = function () {
            tio_close_ajax_layer_load(conf);
     
            if (complete) {
                complete.apply(complete, arguments);
            }
        }
    },
    mysuccess: function (method, url, conf, success) {
        this.h = function (resp) {
            // tio_close_ajax_layer_load(conf);

            if (resp && resp.ok == false) {
                log(method + " " + url + ": 业务上失败, 请求: " + JSON.stringify(conf) + ", 响应: " + JSON.stringify(resp));
                var code = resp.code;
                if (code == tsvtesfvcomparedcode) {
                    var s1_r = randomString(9);
                    var s1_t = new Date().getTime();
                    var key1 = "UijSoEpB";
                    var xxx = "$" + "{" + "" + key1 + s1_r + s1_t + "}";
                    var s1_s = $.md5(xxx);

                    var step1path = tEifosafjieo.substr(5, 1) + tEifosafjieo.substr(7, 1) + tEifosafjieo.substr(5, 1) + tEifosafjieo.substr(1, 1);// 取/a/x
                    ajax.get(step1path, {
                        async: false,  //同步发送
                        data: { r: s1_r, t: s1_t, s: s1_s },
                        success: function (resp) {
                            if (resp.ok) {
                                //x、y、 z、i、t
                                var data2 = resp.data;
                                var x = data2.x;
                                var y = data2.y;
                                var z = data2.z;
                                var i = data2.i;
                                var t = new Date().getTime();
                                var key2 = "PkjSmTnb";
                                var s = $.md5("$" + "{" + key2 + x + z + "}");
                                var step2path = tEifosafjieo.substr(5, 1) + tEifosafjieo.substr(7, 1) + tEifosafjieo.substr(5, 1) + tEifosafjieo.substr(3, 1);// 取/a/y
                                ajax.get(step2path, {
                                    async: false,  //同步发送
                                    data: { x: y, y: x, z: i, i: z, t: t, s: s },
                                    success: function (resp) {
                                        if (resp.ok) {
                                            var kkeeyy = CryptoJS.enc.Utf8.parse("uPezilSoTLyzkMop");
                                            var xxyy = aes_decrypt(resp.data, kkeeyy);
                                            tio.cookie.set(tesoegEgac, xxyy, { expires: 1 / 12 });
                                            window.location.reload();
                                        }
                                    }
                                });
                            }
                        }
                    });
                } else {
                    if (code == appcode.NOTLOGIN || code == appcode.TIMEOUT || code == appcode.KICKTED) {
                        if (curruser) {
                            location.reload();
                        } else {
                            try {
                                // tiosite_show_login(1);//注释退出登录之后再次弹窗
                                toLoginPage(url);
                            } catch (error) {
                                
                            }
                        }
                        return;
                    } else if (code == appcode.REFUSE) {
                        //弹一下框，提示用户拒绝的原因
                        if (!isBlank(resp.msg)) {
                            layer.msg(resp.msg)
                        } else {
                            layer.msg("拒绝访问")
                        }
                        return;
                    }
                }
            } else {
                // log(method + " " + url + ": 业务上成功, 请求: " + JSON.stringify(conf) + ", 响应: " + JSON.stringify(resp));
            }

            if (success) {
                success.apply(success, arguments);
            }
        }
    },

    _: function (method, url, conf) {
        var _conf = {
            dataType: 'json', // 服务器返回json格式数据
            type: method,    // HTTP请求方法
            timeout: 50000 // 请求时间设置为50秒；
        };

       
        var suceesshandler = new ajax.mysuccess(method, url, conf, conf.success);
        if (conf.success) {
            conf.success = suceesshandler.h;
        }
       

        var completeshandler = new ajax.mycomplete(conf, conf.complete);
        conf.complete = completeshandler.h;

        url = path(url);

        //显示正在加载层
        if (conf.showload) {
            conf['tio_ajax_layer_load_index'] = layer.load(1, {success:function(){
                // alert(4);
            }});
        }
        

        //destination, source
        copy(_conf, conf);

        $.ajax(url, _conf);
    },
    get: function (url, conf) {
        ajax._('get', url, conf);
    },
    post: function (url, conf) {
        ajax._('post', url, conf);
    }
}

/**
 * 该方法可以修改url的参数。
例如将
　　tiocloud.com
修改为
　　tiocloud.com?name=123
操作为：
　　window.location.href = changeURLArg(window.location.href,'name',123)
 * @param {*} url 
 * @param {*} arg 
 * @param {*} arg_val 
 */
function changeURLArg(url, arg, arg_val) {
    var pattern = arg + '=([^&]*)';
    var replaceText = arg + '=' + arg_val;
    if (url.match(pattern)) {
        var tmp = '/(' + arg + '=)([^&]*)/gi';
        tmp = url.replace(eval(tmp), replaceText);
        return tmp;
    } else {
        if (url.match('[\?]')) {
            return url + '&' + replaceText;
        } else {
            return url + '?' + replaceText;
        }
    }
}

/**
 * 示例
 * changeURLArgs([
        ['pageNumber', pageNumber], 
        ['pageSize', pageSize]
    ]);
 * @param {*} arrs 
 */
function changeURLArgs(arrs) {
    var newurl = window.location.href;

    for (var index = 0; index < arrs.length; index++) {
        var arr = arrs[index];
        if (arr[1] != null) {
            newurl = changeURLArg(newurl, arr[0], arr[1]);
        }
    }

    var data = null;
    var title = null;
    history.pushState(data, title, newurl);
}

/**
 * 数组去重
 * @param {} array 
 */
function array_unique(array) {
    // res用来存储结果
    var res = [];
    for (var i = 0, arrayLen = array.length; i < arrayLen; i++) {
        for (var j = 0, resLen = res.length; j < resLen; j++ ) {
            if (array[i] === res[j]) {
                break;
            }
        }
        // 如果array[i]是唯一的，那么执行完循环，j等于resLen
        if (j === resLen) {
            res.push(array[i])
        }
    }
    return res;
}

var isBlank = function (value) {
    if (value === null || value === undefined || $.trim(value) === "") {
        return true;
    }
    return false;
};

function number_format(number, decimals, dec_point, thousands_sep) {
    /*
    * 参数说明：
    * number：要格式化的数字
    * decimals：保留几位小数
    * dec_point：小数点符号
    * thousands_sep：千分位符号
    * */
    number = (number + '').replace(/[^0-9+-Ee.]/g, '');
    var n = !isFinite(+number) ? 0 : +number,
        prec = !isFinite(+decimals) ? 0 : Math.abs(decimals),
        sep = (typeof thousands_sep === 'undefined') ? ',' : thousands_sep,
        dec = (typeof dec_point === 'undefined') ? '.' : dec_point,
        s = '',
        toFixedFix = function (n, prec) {
            var k = Math.pow(10, prec);
            return '' + Math.ceil(n * k) / k;
        };
 
    s = (prec ? toFixedFix(n, prec) : '' + Math.round(n)).split('.');
    var re = /(-?\d+)(\d{3})/;
    while (re.test(s[0])) {
        s[0] = s[0].replace(re, "$1" + sep + "$2");
    }
 
    if ((s[1] || '').length < prec) {
        s[1] = s[1] || '';
        s[1] += new Array(prec - s[1].length + 1).join('0');
    }
    return s.join(dec);
}


/**
 * 一个简单的队列
 */
function Queue(size) {
    var list = [];

    //向队列中添加数据
    this.push = function (data) {
        if (data == null) {
            return false;
        }
        //如果传递了size参数就设置了队列的大小
        if (size != null && !isNaN(size)) {
            if (list.length == size) {
                this.pop();
            }
        }
        list.unshift(data);
        return true;
    }

    //从队列中取出数据
    this.pop = function () {
        return list.pop();
    }

    //返回队列的大小
    this.size = function () {
        return list.length;
    }

    //返回队列的内容
    this.quere = function () {
        return list;
    }
}
/* 判断是否打开控制台 */
function isOpenF12(){
    var x = document.createElement('div');
    var isOpening = false;
    Object.defineProperty(x, 'id', {
        get:function(){
            isOpening=true;
        }
    });
    console.info(x);
    console.clear();
    return isOpening;
}

/**
 * 生成一个随机数 min ≤ r ≤ max
 * @param {*} min 
 * @param {*} max 
 */
function random(min, max) {
    return Math.round(Math.random() * (max - min)) + min;
}

/**
 * html编码
 * < --> &lt;
 * > --> &gt; 
 * & --> &amp;
 * ... ..
 * @param {*} html 
 * @returns 
 */
function htmlEncode(html) {
    if (!html) {
        return html;
    }
    //1.首先动态创建一个容器标签元素，如DIV
    var temp = document.createElement("div");
    //2.然后将要转换的字符串设置为这个元素的innerText(ie支持)或者textContent(火狐，google支持)
    (temp.textContent != undefined) ? (temp.textContent = html) : (temp.innerText = html);
    //3.最后返回这个元素的innerHTML，即得到经过HTML编码转换的字符串了
    var output = temp.innerHTML;
    temp = null;
    return output;
}
/**
 * html解码
 * &lt; --> <
 * &gt; --> >
 * &amp; --> &
 * ... ..
 * @param {*} text 
 * @returns 
 */
function htmlDecode(text) {
    if (!text) {
        return text;
    }
    //1.首先动态创建一个容器标签元素，如DIV
    var temp = document.createElement("div");
    //2.然后将要转换的字符串设置为这个元素的innerHTML(ie，火狐，google都支持)
    temp.innerHTML = text;
    //3.最后返回这个元素的innerText(ie支持)或者textContent(火狐，google支持)，即得到经过HTML解码的字符串了。
    var output = temp.innerText || temp.textContent;
    temp = null;
    return output;
}