
package org.tio.sitexxx.service.service.chat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.hutool.core.util.StrUtil;
import cn.jpush.api.push.PushResult;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.sitexxx.service.vo.Const;

import cn.jiguang.common.ClientConfig;
import cn.jpush.api.JPushClient;
import cn.jpush.api.push.model.Options;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosAlert;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;
import org.tio.utils.json.Json;

/**
 * 推送服务
 *
 * @author lixinji
 * 2020年09月25日 下午5:57:32
 */
public class JPushService {
    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(JPushService.class);
    public static final JPushService me = new JPushService();

    private static JPushClient jpushClient = new JPushClient(Const.JPushConfig.MASTERSECRET, Const.JPushConfig.APPKEY, null, ClientConfig.getInstance());

    /**
     * 别名发送方式初始化-参数
     *
     * @param uid
     * @param fromnick
     * @param msg
     * @param chatName
     * @return
     * @author lixinji
     * 2020年9月24日 上午10:53:28
     */
    public static PushPayload initPushAlias(Integer uid, String fromnick, String msg, String chatName, Short chatmode) {
        Map<String, String> extras = new HashMap<String, String>();
        extras.put("chatName", chatName);
        extras.put("nick", fromnick);
        extras.put("text", msg);
        extras.put("chatlinkid", "");
        if (Objects.equals(chatmode, Const.ChatMode.GROUP)) {
            msg = fromnick + "：" + msg;
        }
        return initPushAlias(uid, msg, extras, chatName);
    }

    /**
     * 别名发送方式初始化-参数-群组
     *
     * @param uid
     * @param fromnick
     * @param msg
     * @param chatName
     * @return
     * @author lixinji
     * 2020年9月24日 上午11:19:41
     */
    public static PushPayload initPushAlias(List<String> uid, String fromnick, String msg, String chatName, Short chatmode) {
        Map<String, String> extras = new HashMap<String, String>();
        extras.put("chatName", chatName);
        extras.put("nick", fromnick);
        extras.put("text", msg);
        extras.put("chatlinkid", "");
        if (Objects.equals(chatmode, Const.ChatMode.GROUP)) {
            msg = fromnick + "：" + msg;
        }
        return initPushAlias(uid, msg, extras, chatName);
    }

    /**
     * 别名发送方式初始化-参数
     *
     * @param uid
     * @param fromnick
     * @param msg
     * @param chatName
     * @param chatlinkid
     * @return
     * @author lixinji
     * 2020年9月24日 上午10:53:02
     */
    public static PushPayload initPushAlias(Integer uid, String fromnick, String msg, String chatName, Long chatlinkid, Short chatmode) {
        Map<String, String> extras = new HashMap<String, String>();
        extras.put("chatName", chatName);
        extras.put("nick", fromnick);
        extras.put("text", msg);
        extras.put("chatlinkid", chatlinkid + "");
        if (Objects.equals(chatmode, Const.ChatMode.GROUP)) {
            msg = fromnick + "：" + msg;
        }
        return initPushAlias(uid, msg, extras, chatName);
    }

    /**
     * 别名发送方式初始化-单条
     *
     * @param uid
     * @param chatName
     * @param msg
     * @return
     * @author lixinji
     * 2020年9月24日 上午10:29:53
     */
    public static PushPayload initPushAlias(Integer uid, String msg, Map<String, String> extras, String title) {
        Notification notification = Notification.newBuilder().addPlatformNotification(AndroidNotification.newBuilder().setAlert(msg).setTitle(title).addExtras(extras).build())
                .addPlatformNotification(IosNotification.newBuilder().setSound("sound.caf").setAlert(IosAlert.newBuilder().setTitleAndBody(title, "", msg).build()).addExtras(extras).build()).build();
        return PushPayload.newBuilder().setPlatform(Platform.all()).setAudience(Audience.alias(uid + "")).setNotification(notification)
                .setOptions(Options.newBuilder().setApnsProduction(true).build()).build();
    }

    /**
     * 别名发送方式初始化-群组
     *
     * @param uid
     * @param msg
     * @param extras
     * @return
     * @author lixinji
     * 2020年9月24日 上午11:19:15
     */
    public static PushPayload initPushAlias(List<String> uid, String msg, Map<String, String> extras, String title) {
        Notification notification = Notification.newBuilder().addPlatformNotification(AndroidNotification.newBuilder().setAlert(msg).setTitle(title).addExtras(extras).build())
                .addPlatformNotification(IosNotification.newBuilder().setSound("sound.caf").setAlert(IosAlert.newBuilder().setTitleAndBody(title, "", msg).build()).addExtras(extras).build()).build();
        return PushPayload.newBuilder().setPlatform(Platform.all()).setAudience(Audience.alias(uid)).setNotification(notification)
                .setOptions(Options.newBuilder().setApnsProduction(true).build()).build();
    }

    /**
     * 注册id方式初始化-参数
     *
     * @param uid
     * @param fromnick
     * @param msg
     * @param chatName
     * @return
     * @author lixinji
     * 2020年9月24日 上午10:53:28
     */
    public static PushPayload initPushReg(String regid, String fromnick, String msg, String chatName, Short chatmode) {
        Map<String, String> extras = new HashMap<String, String>();
        extras.put("chatName", chatName);
        extras.put("nick", fromnick);
        extras.put("text", msg);
        extras.put("chatlinkid", "");
        if (Objects.equals(chatmode, Const.ChatMode.GROUP)) {
            msg = fromnick + "：" + msg;
        }
        return initPushReg(regid, msg, extras, chatName);
    }

    /**
     * 注册id方式初始化-参数-群组
     *
     * @param uid
     * @param fromnick
     * @param msg
     * @param chatName
     * @return
     * @author lixinji
     * 2020年9月24日 上午11:19:41
     */
    public static PushPayload initPushReg(List<String> regids, String fromnick, String msg, String chatName, Short chatmode) {
        Map<String, String> extras = new HashMap<String, String>();
        extras.put("chatName", chatName);
        extras.put("nick", fromnick);
        extras.put("text", msg);
        extras.put("chatlinkid", "");
        if (Objects.equals(chatmode, Const.ChatMode.GROUP)) {
            msg = fromnick + "：" + msg;
        }
        return initPushReg(regids, msg, extras, chatName);
    }

    /**
     * 注册id方式初始化-参数
     *
     * @param regid
     * @param fromnick
     * @param msg
     * @param chatName
     * @param chatlinkid
     * @return
     * @author lixinji
     * 2020年9月24日 下午6:30:16
     */
    public static PushPayload initPushReg(String regid, String fromnick, String msg, String chatName, Long chatlinkid, Short chatmode) {
        Map<String, String> extras = new HashMap<String, String>();
        extras.put("chatName", chatName);
        extras.put("nick", fromnick);
        extras.put("text", msg);
        extras.put("chatlinkid", chatlinkid + "");
        if (Objects.equals(chatmode, Const.ChatMode.GROUP)) {
            msg = fromnick + "：" + msg;
        }
        return initPushReg(regid, msg, extras, chatName);
    }

    /**
     * 注册id方式初始化-单条记录
     *
     * @param regid
     * @param msg
     * @param extras
     * @return
     * @author lixinji
     * 2020年9月24日 下午6:29:33
     */
    public static PushPayload initPushReg(String regid, String msg, Map<String, String> extras, String title) {
        // 构建通知内容
        Notification notification = Notification.newBuilder()
                .addPlatformNotification(
                        AndroidNotification.newBuilder()
                                .setAlert(msg)
                                .setTitle(title)
                                .addExtras(extras) // 添加额外参数（可选）
                                .setIntent(getIntentForHome()) // 设置 intent 参数
                                .build()
                )
                .addPlatformNotification(
                        IosNotification.newBuilder()
                                .setSound("sound.caf")
                                .setAlert(IosAlert.newBuilder().setTitleAndBody(title, "", msg).build())
                                .addExtras(extras)
                                .build()
                )
                .build();

        // 构建推送 Payload
        PushPayload.Builder builder = PushPayload.newBuilder()
                .setPlatform(Platform.all()) // 设置平台（Android 和 iOS）
                .setAudience(Audience.registrationId(regid)) // 设置目标用户（通过 registrationId）
                .setNotification(notification); // 设置通知内容

        // 设置 options 参数，包括第三方厂商通道的参数
        Options.Builder optionsBuilder = Options.newBuilder()
                .setApnsProduction(true); // 设置是否为生产环境（iOS 推送相关）

        // 构建第三方厂商通道参数
        Map<String, JsonObject> thirdPartyChannelOptions = new HashMap<>();

        // 小米通道
        JsonObject xiaomiOptions = new JsonObject();
        xiaomiOptions.addProperty("skip_quota", false);
        xiaomiOptions.addProperty("distribution", "secondary_push");
        xiaomiOptions.addProperty("classification", 1);//1.私信消息需要选择“系统消息”。
        xiaomiOptions.addProperty("channel_id", StrUtil.isNotBlank(msg) && "[语音通话]".equals(msg) || "[视频通话]".equals(msg) ? "136423" : "135238"); // 小米的 channel_id
        thirdPartyChannelOptions.put("xiaomi", xiaomiOptions);

        // vivo 通道
        JsonObject vivoOptions = new JsonObject();
        vivoOptions.addProperty("skip_quota", false);
        vivoOptions.addProperty("distribution", "secondary_push");
        vivoOptions.addProperty("classification", 1);
        vivoOptions.addProperty("category", "IM"); // vivo 的消息二级分类标识 IM、SOCIAL
        vivoOptions.addProperty("callback_id", ""); // vivo 的回调 ID
        thirdPartyChannelOptions.put("vivo", vivoOptions);

        // 华为通道
        JsonObject huaweiOptions = new JsonObject();
        huaweiOptions.addProperty("skip_quota", false);
        huaweiOptions.addProperty("distribution", "secondary_push");
        huaweiOptions.addProperty("channel_id", ""); // 华为的 channel_id
        huaweiOptions.addProperty("category", "IM"); // 华为的消息分类
        huaweiOptions.addProperty("importance", "NORMAL"); // 华为通知栏消息智能分类
        huaweiOptions.addProperty("receipt_id", ""); // 华为的回执 ID
        thirdPartyChannelOptions.put("huawei", huaweiOptions);

        // 荣耀通道
        JsonObject honorOptions = new JsonObject();
        honorOptions.addProperty("skip_quota", false);
        honorOptions.addProperty("distribution", "secondary_push");
        thirdPartyChannelOptions.put("honor", honorOptions);

        // OPPO 通道
        JsonObject oppoOptions = new JsonObject();
        oppoOptions.addProperty("skip_quota", false);
        oppoOptions.addProperty("distribution", "secondary_push");
        oppoOptions.addProperty("channel_id", ""); // OPPO 的 channel_id
        oppoOptions.addProperty("category", ""); // OPPO 的消息分类
        thirdPartyChannelOptions.put("oppo", oppoOptions);

        // 将第三方厂商通道参数添加到 options
        optionsBuilder.setThirdPartyChannelV2(thirdPartyChannelOptions);

        return builder.setOptions(optionsBuilder.build()).build();
    }

    /**
     * 定义点击通知后跳转到首页的 Intent
     */
    private static JsonObject getIntentForHome() {
        // 创建一个 JsonObject 来表示 Intent
        JsonObject intent = new JsonObject();
        intent.addProperty("url", "intent:#Intent;action=android.intent.action.MAIN;end");
        return intent;
    }

    /**
     * 注册id方式初始化-列表
     *
     * @param regids
     * @param msg
     * @param extras
     * @return
     * @author lixinji
     * 2020年9月24日 下午6:28:00
     */
    public static PushPayload initPushReg(List<String> regids, String msg, Map<String, String> extras, String title) {
        Notification notification = Notification.newBuilder().addPlatformNotification(AndroidNotification.newBuilder().setAlert(msg).setTitle(title).addExtras(extras).build())
                .addPlatformNotification(IosNotification.newBuilder().setSound("sound.caf").setAlert(IosAlert.newBuilder().setTitleAndBody(title, "", msg).build()).addExtras(extras).build()).build();
        return PushPayload.newBuilder().setPlatform(Platform.all()).setAudience(Audience.registrationId(regids)).setNotification(notification)
                .setOptions(Options.newBuilder().setApnsProduction(true).build()).build();
    }

    /**
     * @param payload
     * @author lixinji
     * 2020年9月24日 上午10:45:39
     */
    public static void send(PushPayload payload, Short chatmode) {
        try {
//            log.error("payload->{}", payload.toString());
            PushResult result = jpushClient.sendPush(payload);
//			jpushClient.sendPush(payload);
            log.error("推送信息：payload:{},result:{},chatmode:{}", payload.toString(), Json.toJson(result), chatmode);

            // 请求结束后，调用 NettyHttpClient 中的 close 方法，否则进程不会退出。

            if (result.getResponseCode() == 200) {
                jpushClient.close();
            } else {
                Thread.sleep(5000);
                jpushClient.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
