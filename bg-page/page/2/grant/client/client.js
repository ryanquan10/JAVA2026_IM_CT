var ctx = "${api_ctx}"; //  web-api的contextpath，目前为"/mytio"
var suffix = "${api_suf}";

$(document).ready(function () {
  initNav();
});

function initNav() {
  $(".nav-link").on("click", function () {
    $(".nav-link").removeClass("active");
    $(this).addClass("active");
  });

  if (location.hash) {
    if ($(`.nav-link[href="${location.hash}"]`).length == 0) {
      location.hash = "";
    }
  }

  if (!location.hash) {
    location.hash = $(".tab_content_item").first().attr("id");
  }
  $(`[href='${location.hash}']`).trigger("click");
}

/**
 * 获取机器码-并下载代码
 */
function getCoder() {
  $.ajax({
		url: ctx + "/license/getcode" + suffix,
		type: "GET",
		cache: false,
		success: function (resp) {
			if (resp.ok) {
        let data = resp.data;
        download(data.url,data.filename);
      } else {
        alert(resp.msg);
      }
		}
	});
}


function remotelic() {
  $.ajax({
		url: ctx + "/license/networkact" + suffix,
		type: "GET",
		cache: false,
		success: function (resp) {
			if (resp.ok) {
        alert("授权成功");
      } else {
        alert(resp.msg);
      }
		}
	});
}

/**
 * 安装授权
 */
function installlic() {
  var formData = new FormData();
  formData.append("uploadFile",$("#thisfile")[0].files[0]);
  $.ajax({
		url: ctx + "/license/fileact" + suffix,
    type: "POST",
    data : formData,
    contentType: false,
    processData: false,
		success: function (resp) {
			if (resp.ok) {
        alert("安装完成,请验证");
      } else {
        alert(resp.msg);
      }
		}
	});
}

/**
 * 检测授权
 */
function checklic() {
  $.ajax({
		url: ctx + "/license/checklic" + suffix,
		type: "GET",
		cache: false,
		success: function (resp) {
			if (resp.ok) {
        alert("授权成功");
        var redirect = getQueryString('redirect_uri_after_grant');
        if (redirect) {
          window.location.href = redirect;
        }
      } else {
        alert(resp.msg);
      }
		}
	});
}


/**
 * 下载方法-同官网授权-单独使用
 **/
function download(url, fileName) {
  var xmlHttp = null;
  // IE7+, Firefox, Chrome, Opera, Safari 浏览器执行代码
  xmlHttp = new XMLHttpRequest();

  // 2.如果实例化成功，就调用open（）方法：
  if (xmlHttp != null) {
    xmlHttp.open('get', url, true);
    xmlHttp.send();
    xmlHttp.onreadystatechange = doResult; //设置回调函数
  }

  /**
   * 内部回调方法
   */
  function doResult() {
    if (xmlHttp.readyState == 4)  { //4表示执行完成
      if (xmlHttp.status == 200) { //200表示执行成功
        // 创建隐藏的可下载链接
        var aLink = document.createElement('a');
        aLink.download = fileName; //设置a标签的下载名字
        aLink.style.display = 'none';
        // 字符内容转变成blob地址
        var blob = new Blob([xmlHttp.responseText]);
        aLink.href = URL.createObjectURL(blob);
        // 触发点击
        document.body.appendChild(aLink);
        aLink.click();
        // 然后移除
        document.body.removeChild(aLink);
      }
    }
  }
}

 //触发隐藏的file表单
 function makeThisfile(){
  $('#thisfile').click();
}

//file表单选中文件时,让file表单的val展示到showname这个展示框
$('#thisfile').change(function(){
  $('#showname').val($(this).val())
})

const grantVertify = function(){
  alert(1);
}