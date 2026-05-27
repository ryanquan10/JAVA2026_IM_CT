
package org.tio.mg.web.server.controller;

import cn.hutool.core.util.StrUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.kit.Ret;
import org.tio.mg.service.model.mg.MgUser;
import org.tio.mg.service.service.mg.MgUserService;
import org.tio.mg.service.utils.OkHttpUtils;
import org.tio.mg.service.utils.RetUtils;
import org.tio.mg.web.server.utils.GoogleAuthUtils;
import org.tio.mg.web.server.utils.WebUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.utils.resp.Resp;

import okhttp3.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xufei
 * 2020年5月25日 下午4:45:27
 */
@RequestPath(value = "/mguser")
public class MgUserController {
    private static Logger log = LoggerFactory.getLogger(MgUserController.class);

    /**
     * @param args
     * @author tanyaowu
     */
    public static void main(String[] args) {

    }

    private MgUserService userService = MgUserService.ME;

    /**
     * @author tanyaowu
     */
    public MgUserController() {
    }

    /**
     * 当前用户
     *
     * @param request
     * @return
     * @throws Exception
     * @author xufei
     * 2020年5月27日 上午9:48:36
     */
    @RequestPath(value = "/curr")
    public Resp curr(HttpRequest request) throws Exception {
        MgUser user = WebUtils.currUser(request);
        if (user != null) {
            Resp resp = Resp.ok(user);
            return resp;
        } else {
            Resp resp = Resp.fail();
            return resp;
        }
    }

    /**
     * 获取菜单
     *
     * @param request
     * @return
     * @throws Exception
     * @author xufei
     * 2020年5月27日 下午2:15:44
     */
    @RequestPath(value = "/menu")
    public Resp menu(HttpRequest request) throws Exception {
        MgUser user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail("会话失效");
        }
        Ret ret = userService.getMenu(user.getId());
        if (ret.isFail()) {
            log.error("获取菜单失败：{}", RetUtils.getRetMsg(ret));
            return Resp.fail(RetUtils.getRetMsg(ret));
        }
        return Resp.ok(RetUtils.getOkData(ret));
    }

    /**
     * @param request
     * @return
     * @throws Exception
     * @author xufei
     * 2020年6月1日 下午4:06:17
     */
    @RequestPath(value = "/list")
    public Resp list(HttpRequest request, String searchkey, Integer rid, Short status, Integer pageNumber, Integer pageSize) throws Exception {
        Ret ret = userService.userList(searchkey, rid, status, pageNumber, pageSize);
        if (ret.isFail()) {
            log.error("获取用户列表失败：{}", RetUtils.getRetMsg(ret));
            return Resp.fail(RetUtils.getRetMsg(ret));
        }
        return Resp.ok(RetUtils.getOkPage(ret));
    }


    /**
     * @param request
     * @return
     * @throws Exception
     * @author xufei
     * 2020年6月1日 下午4:06:17
     */
    @RequestPath(value = "/bindIp")
    public Resp bindIp(HttpRequest request, Integer id, String ip) throws Exception {
        Ret ret = userService.bindIp(id, ip);
        if (ret.isFail()) {
            log.error("新增用户：{}", RetUtils.getRetMsg(ret));
            return Resp.fail(RetUtils.getRetMsg(ret));
        }
        return Resp.ok(RetUtils.getOkData(ret));
    }


    /**
     * 同步后台用户
     *
     * @param request
     * @return
     * @throws Exception
     * @author xufei
     * 2021年4月23日 上午11:31:16
     */
    @RequestPath(value = "/synAdminUser")
    public Resp synAdminUser(HttpRequest request) throws Exception {
        try {
            Response resp = OkHttpUtils.get(Const.IM_SERVER + "/mytio/ndapi/init.tio_x");
            if (resp == null) {
                return Resp.fail().msg("用户同步响应失败");
            }
            if (resp.isSuccessful()) {
                if (resp.code() != 200) {
                    return Resp.fail().msg("用户同步,状态码：" + resp.code());
                }
            } else {
                return Resp.fail().msg("用户同步,响应失败");
            }
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        return Resp.ok();
    }

    /**
     * @param request
     * @param mguid
     * @return
     * @throws Exception
     * @author xufei
     * 2020年6月2日 下午3:14:59
     */
    @RequestPath(value = "/resetPwd")
    public Resp resetPwd(HttpRequest request, Integer mguid) throws Exception {
        Ret ret = userService.resetPwd(mguid);
        if (ret.isFail()) {
            log.error("重置密码失败：{}", RetUtils.getRetMsg(ret));
            return Resp.fail(RetUtils.getRetMsg(ret));
        }
        return Resp.ok(RetUtils.getOkData(ret));
    }

    /**
     * 修改密码
     *
     * @param request
     * @param pwd
     * @param newpwd
     * @return
     * @throws Exception
     * @author xufei
     * 2020年6月8日 下午4:01:41
     */
    @RequestPath(value = "/updatePwd")
    public Resp resetPwd(HttpRequest request, String pwd, String newpwd) throws Exception {
        MgUser user = WebUtils.currUser(request);
        Ret ret = userService.updatePwd(user.getId(), pwd, newpwd);
        if (ret.isFail()) {
            log.error("修改密码失败：{}", RetUtils.getRetMsg(ret));
            return Resp.fail(RetUtils.getRetMsg(ret));
        }
        return Resp.ok(RetUtils.getOkData(ret));
    }

    /**
     * @param request
     * @param user
     * @return
     * @throws Exception
     * @author xufei
     * 2020年6月2日 下午3:24:35
     */
    @RequestPath(value = "/add")
    public Resp add(HttpRequest request, MgUser user) throws Exception {
        Ret ret = userService.add(user);
        if (ret.isFail()) {
            log.error("新增用户：{}", RetUtils.getRetMsg(ret));
            return Resp.fail(RetUtils.getRetMsg(ret));
        }
        return Resp.ok(RetUtils.getOkData(ret));
    }

    /**
     * @param request
     * @param user
     * @return
     * @throws Exception
     * @author xufei
     * 2020年6月2日 下午3:24:33
     */
    @RequestPath(value = "/update")
    public Resp update(HttpRequest request, MgUser user) throws Exception {
        Ret ret = userService.update(user);
        if (ret.isFail()) {
            log.error("新增用户：{}", RetUtils.getRetMsg(ret));
            return Resp.fail(RetUtils.getRetMsg(ret));
        }
        return Resp.ok(RetUtils.getOkData(ret));
    }

    /**
     * @param request
     * @param mguid
     * @return
     * @throws Exception
     * @author xufei
     * 2020年6月2日 下午3:24:31
     */
    @RequestPath(value = "/del")
    public Resp del(HttpRequest request, Integer mguid) throws Exception {
        Ret ret = userService.del(mguid);
        if (ret.isFail()) {
            log.error("新增用户：{}", RetUtils.getRetMsg(ret));
            return Resp.fail(RetUtils.getRetMsg(ret));
        }
        return Resp.ok(RetUtils.getOkData(ret));
    }


    /**
     * 生成二维码图片并转换为 Base64
     */
    /**
     * 生成二维码图片并转换为 Base64
     */
    private String generateQRCodeBase64(String content) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 200, 200);
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            return Base64.getEncoder().encodeToString(pngOutputStream.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("生成二维码失败", e);
        }
    }

    /**
     * 谷歌绑定数据获取
     */
    @RequestPath("/bindgoogledata")
    public Resp bindGoogle(HttpRequest request) throws IOException, WriterException {
        MgUser user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail("用户未登录");
        }
        String secretKey = null;

        // 如果用户未设置密钥，则生成随机密钥
        if (StrUtil.isEmpty(user.getSecretKey())) {
            secretKey = GoogleAuthUtils.generateSecretKey();
        } else {
            secretKey = user.getSecretKey();
        }

        // 生成二维码 URL
        String qrCodeUrl = GoogleAuthUtils.generateQrCodeUrl(
                secretKey,
                user.getId().toString(),
                "成通巢讯" + user.getLoginname()
        );

        // 生成 Base64 编码的二维码图片
        String qrCodeBase64 = generateQRCodeBase64(qrCodeUrl);

        // 返回数据
        Map<String, String> response = new HashMap<>();
        response.put("qrCodeBase64", qrCodeBase64);
        response.put("manualEntryKey", secretKey);
        return Resp.ok(response);
    }


    /**
     * 绑定谷歌身份验证器
     */
    @RequestPath("/bindgoogle")
    public Resp bindGoogle(HttpRequest request, String manualEntryKey, String code) {
        MgUser user = WebUtils.currUser(request);
        if (user == null) {
            return Resp.fail("用户未登录");
        }
        // 验证动态验证码
        boolean isValid = GoogleAuthUtils.validateCode(manualEntryKey, code);
        if (isValid) {
            user.setSecretKey(manualEntryKey);
            user.setIsBound(Const.YesOrNo.YES);
            Ret ret = userService.update(user);
            if (ret.isFail()) {
                log.error("绑定谷歌身份验证器失败：{}", RetUtils.getRetMsg(ret));
                return Resp.fail("绑定失败：" + RetUtils.getRetMsg(ret));
            }
            return Resp.ok("绑定成功");
        } else {
            return Resp.fail("验证码错误");
        }
    }
}
