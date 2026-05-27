/**
 * 初始化授权并下载
 */
function initlic() {
    let appkey = $("#appkey").val();
    let appid = $("#appid").val();
    let fd =new FormData();
    fd.append("uploadFile",$("#formFile").get(0).files[0]);
    fd.append("appid",appid);
    fd.append("appkey",appkey);

    ajax.post("/license/filegrant", {
        data: fd,
        contentType: false, // 注意这里应设为false
        processData: false,
        timeout: 500000,
        success: function (resp) {
            if (resp.ok) {
                let data = resp.data;
                window.open(data.url);
            } else {
                layer.alert(resp.msg || "上传失败");
            }
        }
    });
}