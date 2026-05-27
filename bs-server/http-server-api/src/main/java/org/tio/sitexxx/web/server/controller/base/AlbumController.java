package org.tio.sitexxx.web.server.controller.base;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.UploadFile;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.kit.Ret;
import org.tio.sitexxx.service.model.conf.Conf;
import org.tio.sitexxx.service.model.main.Album;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.service.base.AlbumService;
import org.tio.sitexxx.service.service.base.UserService;
import org.tio.sitexxx.service.utils.CloudflareR2Utils;
import org.tio.sitexxx.service.utils.ImgUtils;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.utils.UploadUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.web.server.utils.VideoUtils;
import org.tio.sitexxx.web.server.utils.WebUtils;
import org.tio.utils.resp.Resp;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


@RequestPath(value = "/album")
public class AlbumController {
    private static Logger log = LoggerFactory.getLogger(AlbumController.class);
    private UserService userService = UserService.ME;
    private AlbumService albumService = AlbumService.ME;


    /**
     * 添加相册
     * @param request 请求request
     * @param name 相册名称
     * @param cover 封面图地址
     * @param permission 权限 1:开放，2:私密，3:密码
     * @param password 密码
     * @return 无返回值
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/addAlbum")
    public Resp addAlbum(HttpRequest request, String name, String cover, Integer permission, String password, Integer isTop) throws Exception {
        if (name == null || name.isEmpty()) {
            return Resp.fail().msg("相册名称不能为空");
        }
        if (cover == null || cover.isEmpty()) {
            return Resp.fail().msg("相册封面不能为空");
        }
        if (permission == null || (!permission.equals(1) && !permission.equals(2) && !permission.equals(3))) {
            return Resp.fail().msg("请设置正确的权限");
        }
        if (permission.equals(3)) {
            if (password == null || password.isEmpty()) {
                return Resp.fail().msg("访问密码不能为空");
            }
            if (password.length() < 6 || password.length() > 16) {
                return Resp.fail().msg("密码长度为6-16位的字母加数字组合");
            }
            if (!isAlphaNumeric(password)) {
                return Resp.fail().msg("密码长度为6-16位的字母加数字组合");
            }
        }
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail().msg("请登录");
        }
        Album album = Album.dao.findFirst("select * from album where uid = ? and name = ?", user.getId(), name);
        if (album != null) {
            return Resp.fail().msg("名称为 " + name + " 的相册已存在");
        }
        Ret ret = albumService.addAlbum(user, name, cover, permission, password, isTop);
        if (ret.isFail()) {
            return Resp.fail().msg(ret.get("errorMsg").toString());
        }
        return Resp.ok();
    }

    /**
     * 相册列表
     * @param request 请求request
     * @param pageNumber 页码
     * @param pageSize 条数
     * @return Page<Records>
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/albumList")
    public Resp albumList(HttpRequest request, String searchkey, Integer pageNumber, Integer pageSize) throws Exception {
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail().msg("请登录");
        }
        Ret ret = albumService.albumList(user, searchkey, pageNumber, pageSize);
        if (ret.isFail()) {
            return Resp.fail().msg(ret.get("errorMsg").toString());
        }
        return Resp.ok(RetUtils.getOkPage(ret));
    }


    /**
     * 相册列表
     * @param request 请求request
     * @param ids 查询id 使用 , 隔开
     * @return Page<Records>
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/albumListById")
    public Resp albumListById(HttpRequest request, String ids) throws Exception {
        if (ids == null || ids.isEmpty()) {
            return Resp.fail().msg("请传入查询id");
        }
        Ret ret = albumService.albumListById(ids);
        if (ret.isFail()) {
            return Resp.fail().msg(ret.get("errorMsg").toString());
        }
        return Resp.ok(ret.get("records"));
    }


    /**
     * 相册详情
     * @param request 请求request
     * @param albumId 相册id
     * @return Album
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/albumInfo")
    public Resp albumList(HttpRequest request, Integer albumId) throws Exception {
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail().msg("请登录");
        }
        Album album = Album.dao.findById(albumId);
        if (album == null) {
            return Resp.fail().msg("相册不存在");
        }
        if (!user.getId().equals(album.getUid())) {
            return Resp.fail().msg("无权查看该信息");
        }
        if (album.getPassword() != null) {
            album.setPassword(AlbumService.ME.decryptor(album.getPassword()));
        }
        return Resp.ok(album);
    }


    /**
     * 修改相册信息
     * @param request 请求request
     * @param albumId 相册id
     * @return Album
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/albumUpdate")
    public Resp albumUpdate(HttpRequest request, Integer albumId, String name, String cover, Integer permission, String password, Integer isTop) throws Exception {
        if (name == null || name.isEmpty()) {
            return Resp.fail().msg("相册名称不能为空");
        }
        if (cover == null || cover.isEmpty()) {
            return Resp.fail().msg("相册封面不能为空");
        }
        if (permission == null || (!permission.equals(1) && !permission.equals(2) && !permission.equals(3))) {
            return Resp.fail().msg("请设置正确的权限");
        }
        if (permission.equals(3)) {
            if (password == null || password.isEmpty()) {
                return Resp.fail().msg("访问密码不能为空");
            }
            if (password.length() < 6 || password.length() > 16) {
                return Resp.fail().msg("密码长度为6-16位的字母加数字组合");
            }
            if (!isAlphaNumeric(password)) {
                return Resp.fail().msg("密码长度为6-16位的字母加数字组合");
            }
        }
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail().msg("请登录");
        }
        Ret ret = albumService.albumUpdate(user, albumId, name, cover, permission, password, isTop);
        if (ret.isFail()) {
            return Resp.fail().msg(ret.get("errorMsg").toString());
        }
        return Resp.ok();
    }

    /**
     * 相册置顶
     * @param request 请求request
     * @param albumId 相册id
     * @return Album
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/albumTop")
    public Resp albumTop(HttpRequest request, Integer albumId) throws Exception {
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail().msg("请登录");
        }
        Ret ret = albumService.albumTop(user, albumId);
        if (ret.isFail()) {
            return Resp.fail().msg(ret.get("errorMsg").toString());
        }
        return Resp.ok();
    }

    /**
     * 取消相册置顶
     * @param request 请求request
     * @param albumId 相册id
     * @return Album
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/albumCancelTop")
    public Resp albumCancelTop(HttpRequest request, Integer albumId) throws Exception {
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail().msg("请登录");
        }
        Ret ret = albumService.albumCancelTop(user, albumId);
        if (ret.isFail()) {
            return Resp.fail().msg(ret.get("errorMsg").toString());
        }
        return Resp.ok();
    }

    /**
     * 删除相册
     * @param request 请求request
     * @param albumId 相册id
     * @return 无
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/albumDel")
    public Resp albumDel(HttpRequest request, Integer albumId) throws Exception {
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail().msg("请登录");
        }
        Album album = Album.dao.findById(albumId);
        if (album == null) {
            return Resp.fail().msg("相册不存在");
        }
        if (!user.getId().equals(album.getUid())) {
            return Resp.fail().msg("没有权限删除该相册");
        }
        boolean delete = album.delete();
        if (!delete) {
            return Resp.fail().msg("删除失败，请重试");
        }
        return Resp.ok(album);
    }

    /**
     * 添加相册图片
     * @param request 请求request
     * @param albumId 相册id
     * @param imgs 图片地址，使用 逗号 分隔开
     * @return Album
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/addPhoto")
    public Resp addPhoto(HttpRequest request, Integer albumId, String imgs, String videos) throws Exception {
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail().msg("请登录");
        }
        if (albumId == null) {
            return Resp.fail().msg("请选择相册");
        }
        if ((imgs == null || imgs.isEmpty()) && (videos ==null || videos.isEmpty())) {
            return Resp.fail().msg("请上传图片或视频");
        }

        Ret ret = albumService.addPhoto(user, albumId, imgs, videos);
        if (ret.isFail()) {
            return Resp.fail().msg(ret.get("errorMsg").toString());
        }
        return Resp.ok();
    }


    /**
     * 删除相册图片
     * @param request 请求request
     * @param albumId 相册id
     * @param ids 图片id 使用逗号分隔开
     * @return Album
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/delPhoto")
    public Resp delPhoto(HttpRequest request, Integer albumId, String ids) throws Exception {
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail().msg("请登录");
        }
        if (albumId == null) {
            return Resp.fail().msg("请选择相册");
        }
        if (ids == null || ids.isEmpty()) {
            return Resp.fail().msg("请选择图片");
        }

        Ret ret = albumService.delPhoto(user, albumId, ids);
        if (ret.isFail()) {
            return Resp.fail().msg(ret.get("errorMsg").toString());
        }
        return Resp.ok();
    }


    /**
     * 查看相册图片
     * @param request 请求request
     * @param albumId 相册id
     * @return Map<String, Object>
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/photoList")
    public Resp photoList(HttpRequest request, Integer albumId, String password,  Integer pageNumber, Integer pageSize) throws Exception {
        User user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail().msg("请登录");
        }
        if (albumId == null) {
            return Resp.fail().msg("请选择相册");
        }

        Ret ret = albumService.photoList(user, albumId, password, pageNumber, pageSize);
        if (ret.isFail()) {
            return Resp.fail().msg(ret.get("errorMsg").toString());
        }
        Album album = Album.dao.findById(albumId);
        Map<String, Object> result = new HashMap<>();
        result.put("album", album);
        result.put("photoList", RetUtils.getOkPage(ret));
        return Resp.ok(result);
    }


    /**
     * 查看相册图片
     * @param request 请求request
     * @param albumId 相册id
     * @return Map<String, Object>
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/getPhotoListNoLogin")
    public Resp noLoginCheckPhotoList(HttpRequest request, Integer albumId, String password,  Integer pageNumber, Integer pageSize) throws Exception {

        if (albumId == null) {
            return Resp.fail().msg("请选择相册");
        }

        Ret ret = albumService.getPhotoListNoLogin(albumId, password, pageNumber, pageSize);
        if (ret.isFail()) {
            return Resp.fail().msg(ret.get("errorMsg").toString());
        }
        Album album = Album.dao.findById(albumId);
        Map<String, Object> result = new HashMap<>();
        result.put("album", album);
        result.put("photoList", RetUtils.getOkPage(ret));
        return Resp.ok(result);
    }


    /**
     * 查看相册图片前验证
     * @param request 请求request
     * @param albumId 相册id
     * @return Map<String, Object>
     * @author xinji
     * @throws Exception
     */
    @RequestPath(value = "/photoListBeforeCheck")
    public Resp photoListBeforeCheck(HttpRequest request, Integer albumId) throws Exception {
        Album album = Album.dao.findById(albumId);
        if (album == null) {
            return Resp.fail().msg("相册不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("needPassword", album.getPermission().equals(3));
        return Resp.ok(result);
    }


    public static boolean isAlphaNumeric(String input) {
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9]*$");
        return pattern.matcher(input).matches();

    }

    /**
     * 上传文件
     * @param request
     * @param file
     * @param type 1：封面 2：相册图片 3：视频
     * @return
     */
    @RequestPath("/uploadFile")
    public Resp uploadFile(HttpRequest request, UploadFile file, Integer type) {
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
        if (StrUtil.isBlank(extName)) {
            extName = "bin"; // 默认格式
        }

        try {
            String objectKeyPrefix;
            if (type.equals(1)) {
                objectKeyPrefix = "album/cover";
            } else if (type.equals(2)) {
                objectKeyPrefix = "album/photo";
            } else {
                objectKeyPrefix = "album/video";
            }

            String objectKey = UploadUtils.dateFile(objectKeyPrefix) + "." + extName;

            // 构建 Content-Type
            String contentType;
            if ("jpg jpeg".contains(extName)) {
                contentType = "image/jpeg";
            } else if ("png".equals(extName)) {
                contentType = "image/png";
            } else if ("mp4".equals(extName)) {
                contentType = "video/mp4";
            } else {
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

            // 如果是视频类型，还要生成封面图并上传
            if (type.equals(3)) {
                String coverExt = "jpg";
                String coverObjectKey = objectKey + "_cover." + coverExt;

                // 生成封面图
                BufferedImage coverImage = VideoUtils.generateCoverFromVideo(new ByteArrayInputStream(bs));

                // 压缩封面图
                byte[] coverBytes = ImgUtils.compressImage(coverImage, 1f, 0.6d, coverExt);
                UploadUtils.unificationUpload( coverObjectKey, new ByteArrayInputStream(coverBytes), coverBytes.length, "image/" + coverExt);
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
     * 获取相册系统配置
     * @param request 请求 request
     * @return
     */
    @RequestPath(value = "/getConf")
    public Resp getConf(HttpRequest request) {
        List<Conf> confList = Conf.dao.find("select * from conf where name like '%album%'");
        return Resp.ok(confList);
    }
}
