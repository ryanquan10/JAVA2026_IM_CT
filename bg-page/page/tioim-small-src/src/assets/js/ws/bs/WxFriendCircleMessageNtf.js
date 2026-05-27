/*朋友聊天通知-- Server-->Client*/
import store from '@/store/index.js';
import { formatDateByTime } from '@/assets/js/common';
import router from '@/router/index';
var WxFriendChatNtf = async function (ws, event, commandName, bodyStr, bodyObj) {
    log("收到服务器消息-------------------", commandName, bodyObj);
    let stateWs = store.state.Ws;
    let { chatOn, allNotRead, chatSofftop, personAudio, iscurrentpage, focuskeys } = stateWs;
    let curruid = store.state.User.currUid;//当前用户id
    let currpath = router.history.current.path;//当前路径
    let bodytouid = bodyObj.touid,//接收方userid
        bodyuid = bodyObj.uid;//发送方userid
    var isSendByMe = bodyuid == curruid;  //true: 是我发给别人的；fase：是别人发给我的
    var fromuser = { avatar: bodyObj.avatar, nick: bodyObj.nick };//发送人的信息
    bodyObj.t = formatDateByTime(bodyObj.t, 'yyyy-MM-dd HH:mm:ss');
    let sendbysys = bodyObj.sendbysys;//是否为系统消息 1:系统消息；2:非系统消息
    //处于home聊天室页面并且处于当前聊天对话
    let isinChat = focuskeys.find(item => item == bodyObj.chatlinkid);
    log('朋友聊天通知')
    log('chatOn')
    log(chatOn)
    log('bodyObj.chatlinkid')
    log(bodyObj.chatlinkid)
    console.log('朋友圈消息通知', bodyObj);
    if (bodyObj.readflag && !isSendByMe) {
        store.commit("addNotReadCircleNoticeNum");
        let content = JSON.parse(bodyObj.c);
        let newCircleNotice = {
            type: content.type
        }
        if (content.type == 'comment') {
            let comment_obj = {
                "avatar": bodyObj.avatar,
                "content": content.text,
                "createTime": bodyObj.t,
                "id": content.id,
                "mid": content.mid,
                "nick": bodyObj.nick,
                "pid": content.pid,
                "uid": bodyObj.uid,
                "remarkName": "",
            }
            newCircleNotice.data = comment_obj;

        } else if (content.type == 'like') {
            var like_obj = {
                avatar: bodyObj.avatar,
                id: content.id,
                likeTime: bodyObj.t,
                mid: content.mid,
                nick: bodyObj.nick,
                remarkName: '',
                uid: bodyObj.uid,
            }
            newCircleNotice.data = like_obj;
        }
        store.commit("setNewCircleNotice", newCircleNotice);

    }

};
export default WxFriendChatNtf;
