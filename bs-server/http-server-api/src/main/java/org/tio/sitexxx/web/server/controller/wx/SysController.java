
package org.tio.sitexxx.web.server.controller.wx;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.UploadFile;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.kit.Ret;
import org.tio.sitexxx.service.model.main.File;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.service.chat.SysService;
import org.tio.sitexxx.service.utils.CloudflareR2Utils;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.utils.UploadUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.RequestExt;
import org.tio.sitexxx.web.server.utils.WebUtils;
import org.tio.utils.hutool.StrUtil;
import org.tio.utils.json.Json;
import org.tio.utils.resp.Resp;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;

/**
 * 系统相关
 *
 * @author lixinji
 * 2020年1月6日 下午7:41:13
 */
@RequestPath(value = "/sys")
public class SysController {
    private static Logger log = LoggerFactory.getLogger(SysController.class);

    /**
     * 版本更新
     *
     * @param request
     * @param version
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年9月18日 上午11:11:58
     */
    @RequestPath(value = "/version")
    public Resp version(HttpRequest request, String version) throws Exception {
        RequestExt requestExt = WebUtils.getRequestExt(request);
        short deviceType = requestExt.getDeviceType();
        if (requestExt.isFromAppAndroid()) {
            deviceType = 1;
        } else if (requestExt.isFromAppIos()) {
            deviceType = 2;
        }
        if (StrUtil.isBlank(version)) {
            version = requestExt.getAppVersion();
        }
        Ret ret = SysService.me.checkVersion(deviceType, version);
        if (ret.isFail()) {

            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        return Resp.ok(RetUtils.getOkData(ret));
    }

    /**
     * 上传文件
     *
     * @param request
     * @param file
     * @return
     */
    @RequestPath(value = "/upload")
    public Resp upload(HttpRequest request, UploadFile file) {
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail("请登录");
        }
        if (file == null) {
            return Resp.fail("参数异常");
        }

        byte[] bs = file.getData();
        String filename = file.getName();
        String extName = FileUtil.extName(filename).toLowerCase(); // 统一转小写处理
        if (!extName.equals("jpg") && !extName.equals("jpeg") && !extName.equals("png") && !extName.equals("bmp")) {
            return Resp.fail("仅支持图片上传");
        }
        try {
            InputStream inputStream = new ByteArrayInputStream(bs);
            long size = bs.length;
            String contentType = "image/" + extName;

            String objectKey = UploadUtils.dateFile("report/img/") + "." + extName;

            UploadUtils.unificationUpload( objectKey, inputStream, size, contentType);
//            String bucketName = Const.CloudflareR2.R2_BUCKET_NAME;
//            CloudflareR2Utils.uploadFilePublic(bucketName, objectKey, inputStream, size, contentType);
//            String r2Endpoint = Const.CloudflareR2.R2_ENDPOINT;
//            String publicUrl = String.format("https://%s.%s/%s", bucketName, r2Endpoint.split("//")[1], objectKey);
            return Resp.ok(objectKey);
        } catch (Exception e) {
            log.error("文件上传到 R2 异常", e);
            return Resp.fail().code(500).msg("文件上传失败");
        }
    }

    /**
     * 举报投诉
     *
     * @param request
     * @param groupid
     * @param touid
     * @param mid
     * @param reason
     * @return
     * @throws Exception
     * @author lixinji
     * 2021年1月27日 下午3:01:25
     */
    @RequestPath(value = "/report")
    public Resp report(HttpRequest request, Long groupid, Integer touid, Long mid, String reason, String imgs) throws Exception {
        if (groupid == null && touid == null && mid == null) {
            return RetUtils.getInvalidResp();
        }
        if (imgs == null) {
            return Resp.fail().msg("图片不能为空");
        }
        User curr = WebUtils.currUser(request);
        Short devicetype = WebUtils.getRequestExt(request).getDeviceType();
        String appversion = WebUtils.getRequestExt(request).getAppVersion();
        Ret ret = SysService.me.report(curr.getId(), touid, groupid, mid, reason, devicetype, appversion, imgs);
        if (ret.isFail()) {
            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        return Resp.ok(RetUtils.OPER_RIGHT);
    }

    /**
     * @param request
     * @param reason
     * @return
     * @throws Exception
     * @author lixinji
     * 2021年4月8日 上午10:31:40
     */
    @RequestPath(value = "/advise")
    public Resp advise(HttpRequest request, String reason) throws Exception {
        if (StrUtil.isBlank(reason)) {
            return RetUtils.getInvalidResp();
        }
        User curr = WebUtils.currUser(request);
        Short devicetype = WebUtils.getRequestExt(request).getDeviceType();
        String appversion = WebUtils.getRequestExt(request).getAppVersion();
        Ret ret = SysService.me.report(curr.getId(), null, null, null, reason, devicetype, appversion, null);
        if (ret.isFail()) {
            return Resp.fail().msg(RetUtils.getRetMsg(ret));
        }
        return Resp.ok(RetUtils.OPER_RIGHT);
    }

    /**
     * 错误日志
     *
     * @param request
     * @param uploadFile
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年11月2日 上午11:33:42
     */
    @RequestPath(value = "/errlog")
    public Resp updateAvatar(HttpRequest request, UploadFile uploadFile) throws Exception {
        if (uploadFile == null) {
            return Resp.fail("上传信息为空");
        }
        User curr = WebUtils.currUser(request);
        Integer uid = null;
        if (curr != null) {
            uid = curr.getId();
        } else {
            uid = -777;
        }
        String sessionid = request.getHttpSession().getId();
        File dbFile = innerUploadFile(uid, uploadFile, sessionid);
        if (dbFile != null) {
            log.error("app出现了未知异常，已保存记录，上传记录：{}", Json.toJson(dbFile));
        }
        return Resp.ok();
    }

    /**
     * 错误文件上传
     *
     * @param uid
     * @param uploadFile
     * @param sessionid
     * @return
     * @throws Exception
     * @author lixinji
     * 2020年11月2日 上午11:28:34
     */
    private static File innerUploadFile(Integer uid, UploadFile uploadFile, String sessionid) throws Exception {
        byte[] bs = uploadFile.getData();
        String filename = uploadFile.getName();
        String ext = FileUtil.extName(filename);
        if (StrUtil.isBlank(ext)) {
            ext = "bin"; // 默认格式
        }

        String basePath = "/" + Const.UPLOAD_DIR.APP_LOG_ERR + "/" + DateUtil.format(new Date(), "yyyyMMddHH") + "/" + uid;
        String objectKey = basePath + "/" + filename;

        // 构建 Content-Type
        String contentType = "application/octet-stream";
        if ("jpg jpeg png gif bmp".contains(ext.toLowerCase())) {
            contentType = "image/" + ext;
        } else if ("mp4 avi mov mkv".contains(ext.toLowerCase())) {
            contentType = "video/" + ext;
        } else if ("pdf".equals(ext.toLowerCase())) {
            contentType = "application/pdf";
        }

        // 上传文件到 R2
        try (InputStream inputStream = new ByteArrayInputStream(bs)) {
            UploadUtils.unificationUpload( objectKey, inputStream, bs.length, contentType);
//            CloudflareR2Utils.uploadFilePublic(
//                    Const.CloudflareR2.R2_BUCKET_NAME,
//                    objectKey,
//                    inputStream,
//                    bs.length,
//                    contentType
//            );
        }

        // 构造 File 对象并保存到数据库
        File dbFile = new File();
        dbFile.setExt(ext);
        dbFile.setFilename(filename);
        dbFile.setSession(sessionid);
        dbFile.setSize((long) bs.length);
        dbFile.setUid(uid);
        dbFile.setUrl(objectKey); // 存储的是相对路径，前端拼接 base_url 即可访问
        dbFile.save();

        return dbFile;
    }

    /**
     * @param request
     * @param bizid
     * @param chatmode
     * @return
     * @throws Exception
     * @author lixinji
     * 2021年3月2日 上午10:16:20
     */
    @RequestPath(value = "/screenshot")
    public Resp screenshot(HttpRequest request, Long bizid, Short chatmode) throws Exception {
        if (bizid == null && chatmode == null) {
            return RetUtils.getInvalidResp();
        }
        return Resp.ok(RetUtils.OPER_RIGHT);
    }

}
