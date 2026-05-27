import { group, msgTips } from '@/axios/path';
const state = {
    groupUserInfo: {},
    forbiddenInfo: {},
    switchConfig: {
        // isOpenAddressShow: false,
        // isOpenAliPay: false,
        // isOpenBackMsgNotify: false,
        // isOpenGroupApplyNotify: false,
        // isOpenGroupForbiddenNotify: false,
        // isOpenGroupReviewNotify: false,
        // isOpenGroupUpdateManagerNotify: false,
        // isOpenInviteCode: false,
        // isOpenRedPacket: false,
        // isOpenSign: false,
        // isOpenTeam: false,
        // isOpenUSDTPay: false,
        // isOpenUpdateGroupNameNotify: false,
        // isOpenUpdateGroupNoticeNotify: false,
        // isOpenVideo: false,
        // isOpenVoice: false,
        // isOpenWallet: false,
        // isOpenWxPay: false,
    }, // 前端功能开关配置
}
const actions = {
    getChatGroupInfo({ commit }, groupid) {
        group.getchatGroupInfo({ groupid, userflag: 1 }).then(res => {
            if (res.ok) {
                console.log('group.getchatGroupInfo', res.data);
                commit('setGetGroupUserInfo', res.data)
            }
        })
    },
    getchatForbiddenFlag({ commit }, postdata) {
        group.chatForbiddenFlag(postdata).then(res => {
            if (res.ok) {
                commit('setForbiddenInfo', res.data)
            }
        })
    }


}
const mutations = {
    setGetGroupUserInfo(state, val) {
        console.log('common setGroupInfo', val.group);
        state.groupUserInfo = val
    },
    setGetGroupInfo(state, val) {
        console.log('common setGroupInfo');
        state.groupUserInfo.group = val
    },
    setForbiddenInfo(state, val) {
        state.forbiddenInfo = val
    },
    setGetMsgFree(state, val) {
        state.groupUserInfo.groupuser.msgfreeflag = val
    },
    setGetGroupAvatar(state, val) {
        state.groupUserInfo.group.avatar = val
    },
    /* 存储后端开关配置参数 */
    setSwitchConfig(state, val) {
        state.switchConfig = val;
        console.log('后端开关配置参数设置成功', state.switchConfig);
    },
}
export default {
    state,
    actions,
    mutations
}