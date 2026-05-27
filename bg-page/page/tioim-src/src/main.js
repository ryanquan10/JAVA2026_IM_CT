import Vue from 'vue';
import App from './App.vue';
import router from './router';
import store from './store';

// 引入插件和样式
import '@/assets/js/plugin/elementui.js'; // 引入 ElementUI 插件
import '@/assets/style/reset.css'; // 引入全局样式重置
import '@/assets/iconfont/iconfont.css'; // 引入图标字体
import '@/assets/js/plugin/protofun.js'; // 引入全局函数

// WebSocket 相关
import { tiows, initWs } from '@/assets/js/ws/ws'; // 连接 IM
import tioCookie from '@/assets/js/ws/tiocookie'; // Cookie 工具
import { user, msgTips } from '@/axios/path'; // API 路径
import { changeURLArgs, getQueryString, getCookie } from '@/assets/js/common.js'; // 公共工具函数

Vue.config.productionTip = false;

// 注册全局组件
import Dialog from '@/components/Dialog'; // 弹框组件
Vue.component('Dialog', Dialog);

// 初始化页面监听
store.commit('currentPage'); // 监听页面激活

// 百度统计初始化
(function () {
  // 挂载 _hmt 到全局对象 window 上
  window._hmt = window._hmt || [];

  // 动态加载百度统计脚本
  const hm = document.createElement('script');
  hm.src = 'https://hm.baidu.com/hm.js?33826f1f45b98aa96af3a5ce4ff2e1f8';
  const s = document.getElementsByTagName('script')[0];
  s.parentNode.insertBefore(hm, s);
})();

// 全局变量
var sessionid = '';
let isInitWs = true; // WebSocket 初始化连接标志
let firstInTx = true; // 首次进入项目标志

// 路由守卫：前置钩子
router.beforeEach(async (to, from, next) => {
  // 百度统计：记录页面访问
  if (window._hmt) {
    if (to.path) {
      window._hmt.push(['_trackPageview', to.fullPath]);
    }
  }

  let toPath = to.path;

  // 如果路径是根路径，跳转到首页
  if (toPath === '/') {
    next({ path: '/home' });
    return;
  }

  // 如果路径是登录页且不是首次进入，停止 WebSocket 并刷新页面
  if (toPath === '/login' && !firstInTx) {
    tiows.stop();
    location.reload();
    return;
  }

  // 首次进入项目时的配置
  if (firstInTx) {
    let tioConfig = sessionStorage.getItem('tiocomconfig');
    if (tioConfig) {
      tioConfig = JSON.parse(tioConfig);
    } else {
      tioConfig = await store.dispatch('getComView');
    }

    // 设置页面标题和 meta 信息
    $('#tiotitle').html(tioConfig.tioim_title); // 标题
    $('#tiodescription').html(tioConfig.tioim_description); // 描述
    $('#tiokeywords').html(tioConfig.tioim_keywords); // 关键词

    // 获取 session 名称和值
    const sessionName = tioConfig.session_cookie_name;
    const bs_tio_session = getQueryString('bs_tio_session');

    if (bs_tio_session) {
      tioCookie.set(sessionName, bs_tio_session, { expires: 15 });
      sessionid = bs_tio_session;
      changeURLArgs([['bs_tio_session', '']]);
    }
  }

  // 获取当前用户信息
  await store.dispatch('getCurrUser');

  // 更新 Cookie
  tioCookie.init();

  // 如果用户已登录
  if (store.state.User.isLogined) {
    console.log('已登录');
    if (sessionid) {
      await user.ndapiAutologin({ sessionid }).then((res) => {
        if (res.ok) {
          location.reload();
        }
      });
      return;
    }

    // 如果路径是登录页，跳转到首页
    if (toPath === '/login') {
      next({ path: '/home' });
      return;
    }

    // 只创建一次 WebSocket 连接
    if (isInitWs) {
      console.log('只创建一次连接');
      initWs(); // 初始化 WebSocket
      isInitWs = false;

      // 如果不在首页，请求会话列表接口计算未读消息数
      if (toPath !== '/home') {
        store.dispatch('getChatRecent');
      }

      // 请求未通过的好友申请数量
      store.dispatch('getApplyData');
    }

    next();
  } else if (toPath !== '/login') {
    console.log('未登录');

    isInitWs = true;
    if (sessionid) {
      user.ndapiAutologin({ sessionid }).then((res) => {
        if (res.ok) {
          next({ path: '/home' });
        } else {
          next({ path: '/login' });
        }
      });
      return;
    }

    next({ path: '/login' });
  } else {
    next();
  }
});

// 路由守卫：后置钩子
router.afterEach((to, from) => {
  if (firstInTx) {
    // 获取私聊和群聊音效 DOM
    const personaudio = document.getElementById('nofity_audio');
    const groupaudio = document.getElementById('group_audio');

    // 存储音效 DOM 到 Vuex
    store.commit('setAudioDom', { person: personaudio, group: groupaudio });

    firstInTx = false;
  }
});

// 初始化 Vue 应用
new Vue({
  router,
  store,
  render: (h) => h(App),
}).$mount('#app');