// main.js
import Vue from 'vue';
import App from './App.vue';
import router from './router';
import store from './store';
import "@/assets/js/plugin/elementui.js"; // 引入 Element UI
import Element from 'element-ui';
import '@/assets/style/reset.css'; // 重置样式
import '@/assets/style/li.css'; // 自定义样式
import 'animate.css'; // 动画库
import '@/assets/iconfont/iconfont.css'; // 图标字体
import dragModalVue from "drag-modal-vue";
import "drag-modal-vue/lib/main.css"; // 拖拽模态框样式

// 应用启动时初始化
store.dispatch('initAgora');

Vue.use(dragModalVue);
Vue.use(Element, { size: 'small' }); // 设置 Element UI 默认尺寸

// 引入全局函数和工具
import "@/assets/js/plugin/protofun.js"; // 全局函数
import { initWs } from '@/assets/js/ws/ws'; // WebSocket 初始化
import tioCookie from '@/assets/js/ws/tiocookie'; // Cookie 工具
import { user, circle, msgTips, getCommonConfList } from "@/axios/path"; // API 路径
import { changeURLArgs, getQueryString, getCookie } from '@/assets/js/common.js'; // 常用工具函数

// 全局组件注册
import Dialog from "@/components/Dialog"; // 弹框组件
Vue.component('Dialog', Dialog);

// 百度统计初始化
window._hmt = window._hmt || [];
(function () {
    const hm = document.createElement('script');
    hm.src = 'https://hm.baidu.com/hm.js?33826f1f45b98aa96af3a5ce4ff2e1f8';
    const s = document.getElementsByTagName('script')[0];
    s.parentNode.insertBefore(hm, s);
}());

// 状态变量
let sessionid = '';
let isInitWs = true; // WebSocket 是否已初始化
let firstInTx = true; // 是否首次进入项目

// 路由钩子：全局前置守卫
router.beforeEach(async (to, from, next) => {
    // 百度统计
    if (window._hmt && to.path) {
        window._hmt.push(['_trackPageview', to.fullPath]);
    }

    // 首页跳转
    if (to.path === "/") {
        return next({ path: '/home' });
    }

    // 用户登录状态检查
    if (firstInTx) {
        let tioConfig = sessionStorage.getItem("tiocomconfig");
        if (tioConfig) {
            tioConfig = JSON.parse(tioConfig);
        } else {
            tioConfig = await store.dispatch("getComView");
        }

        const currUser = store.state.User.currUser;
        document.title = currUser ? currUser.nick : tioConfig.tioim_title;

        const sessionName = tioConfig.session_cookie_name;
        const bs_tio_session = getQueryString("bs_tio_session");
        if (bs_tio_session) {
            tioCookie.set(sessionName, bs_tio_session, { expires: 15 });
            sessionid = bs_tio_session;
            changeURLArgs([['bs_tio_session', '']]);
        }

        // 获取公共配置
        const switchConfig = await getCommonConfList().then((res) => {
            return res.data.reduce((acc, item) => {
                acc[item.name] = item;
                return acc;
            }, {});
        }).catch(() => ({}));
        store.commit("setSwitchConfig", switchConfig);
    }

    // 自动登录
    await store.dispatch("getCurrUser");
    if (!store.state.User.isLogined) {
        if (sessionid) {
            await user.ndapiAutologin({ sessionid }).then(res => {
                if (res.ok) {
                    return location.reload();
                }
            });
        }
        if (to.path !== '/login') {
            return next({ path: '/login' });
        }
    }

    // WebSocket 初始化
    if (isInitWs) {
        initWs();
        isInitWs = false;
        if (to.path !== "/home") {
            store.dispatch("getChatRecent");
        }
        store.dispatch("getApplyData");
        store.dispatch("getRemarkList");
    }

    // 获取最后一条朋友圈消息
    circle.lastCircle().then((res) => {
        if (res.data && res.data.msg) {
            store.commit("setLastCircle", res.data.msg);
        }
    });

    next();
});

// 路由钩子：全局后置钩卫
router.afterEach((to, from) => {
    if (firstInTx) {
        const personaudio = document.getElementById("nofity_audio");
        const groupaudio = document.getElementById("group_audio");
        store.commit("setAudioDom", { person: personaudio, group: groupaudio });
        firstInTx = false;
    }
});

// 自定义指令：拖拽功能
Vue.directive('popover', {
    bind(el, binding, vnode) {
        const dialogHeaderEl = el.querySelector('.el-pagination');
        const dragDom = el.querySelector('.el-popover');
        if (!dialogHeaderEl || !dragDom) return;

        const getStyle = (dom, attr) => {
            const style = dom.currentStyle || window.getComputedStyle(dom, null);
            return +style[attr].replace(/\px/g, '');
        };

        const moveDown = (e) => {
            const disX = e.clientX - dialogHeaderEl.offsetLeft;
            const disY = e.clientY - dialogHeaderEl.offsetTop;

            document.onmousemove = (e) => {
                let l = e.clientX - disX + getStyle(dragDom, 'left');
                let t = e.clientY - disY + getStyle(dragDom, 'top');

                // 边界限制
                l = Math.max(0, Math.min(l, document.documentElement.clientWidth - dragDom.offsetWidth));
                t = Math.max(0, Math.min(t, document.documentElement.clientHeight - dragDom.offsetHeight));

                dragDom.style.left = `${l}px`;
                dragDom.style.top = `${t}px`;
            };

            document.onmouseup = () => {
                document.onmousemove = null;
                document.onmouseup = null;
            };
        };

        dialogHeaderEl.onmousedown = moveDown;
    },
});

// Vue 实例化
new Vue({
    router,
    store,
    render: h => h(App),
}).$mount('#app');