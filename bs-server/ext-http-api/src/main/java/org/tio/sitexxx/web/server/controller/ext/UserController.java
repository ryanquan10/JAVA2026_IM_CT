
package org.tio.sitexxx.web.server.controller.ext;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.annotation.RequestPath;
import org.tio.sitexxx.im.server.handler.wx.WxChatApi;
import org.tio.sitexxx.service.ext.ExtUserService;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.service.base.UserService;
import org.tio.sitexxx.service.utils.AvatarUtils;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.web.server.utils.WebUtils;
import org.tio.utils.hutool.StrUtil;
import org.tio.utils.resp.Resp;

/**
 * 扩展用户服务
 *
 * @author lixinji
 */
@RequestPath(value = "/extuser")
public class UserController {

    private static Logger log = LoggerFactory.getLogger(UserController.class);

    private ExtUserService userService = ExtUserService.ME;

    /**
     * 修改密码
     *
     * @param request
     * @param initPwd 初始密码-明文
     * @param newPwd  新密码-md5加密
     * @return
     * @author lixinji
     */
    @RequestPath(value = "/updatePwd")
    public Resp updatePwd(HttpRequest request, String initPwd, String newPwd) {
        if (StrUtil.isBlank(initPwd)) {

            return Resp.fail("原密码不允许为空");
        }
        if (StrUtil.isBlank(newPwd)) {
            return Resp.fail("新密码不允许为空");
        }
        User curr = WebUtils.currUser(request);
        return userService.updatePwd(curr, initPwd, newPwd);
    }


    /**
     * @param request
     * @param nick
     * @return
     * @throws Exception
     * @author lixinji
     */
    @RequestPath(value = "/updateNick")
    public Resp updateNick(HttpRequest request, String nick) throws Exception {
        User curr = WebUtils.currUser(request);
        if (StrUtil.isBlank(nick)) {
            return RetUtils.getInvalidResp();
        }
        if (curr == null) {
            return Resp.fail().msg("用户未登录");
        }
        //重置头像
        String path = "";
        if (Const.USE_AUTO_AVATAR) {
            if (curr.getAvatar().trim().indexOf("/user/base/avatar/") == 0 && !Objects.equals(nick.substring(0, 1), curr.getNick().substring(0, 1))) {
                path = AvatarUtils.pressUserAvatar(nick);
            }
        }
        Resp resp = userService.updateNick(curr, nick, path);
        User user = UserService.ME.getById(curr.getId());
        String avavar = path;
        //清空缓存
        Const.getBsExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (StrUtil.isNotBlank(avavar)) {
                        WxChatApi.synUserInfoToSelfAllInfo(curr.getId(), Const.UserToImSynType.USER_ALL, user);
                    } else {
                        WxChatApi.synUserInfoToSelfAllInfo(curr.getId(), Const.UserToImSynType.NICK, user);
                    }
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        });
        return resp;
    }

    /**
     * 校验密码
     * @param request
     * @param pd5
     * @return
     */
    @RequestPath(value = "/checkPwd")
    public Resp checkPwd(HttpRequest request, String pd5) {
        if (StrUtil.isBlank(pd5)) {
            return Resp.fail("密码不允许为空");
        }
        User curr = WebUtils.currUser(request);
        return userService.checkPwd(curr, pd5);
    }

}
