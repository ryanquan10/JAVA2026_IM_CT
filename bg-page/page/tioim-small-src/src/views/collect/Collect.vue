<template>
  <div class="mainbody collect">
    <el-tabs
      class="menus"
      tab-position="left"
      :value="'' + c_index"
      @tab-click="changeTab"
    >
      <el-tab-pane
        class="menu"
        v-for="(item, index) in tabs"
        :key="index"
        :label="item.name"
        :name="'' + index"
      >
        <!-- // 1、普通文本消息，2、超链接卡片消息，3、文件，4、音频，5、视频 ,6.图片 ,9.群名片 ,12.红包 ,88.链接 -->
        <div
          class="msg_body"
          :infinite-scroll-immediate="false"
          v-infinite-scroll="load"
        >
          <div v-for="(itm, ix) in item.list" :key="ix">
            <!-- item.type == 1 && item.ct !== 12 -->
            <div
              class="item"
              @contextmenu.prevent="chatContextMenu($event, itm.data, itm.id)"
            >
              <div
                class="text size-b"
                v-if="itm.category == 1"
                :id="'copy' + itm.data.mid"
                v-html="itm.data.html"
              ></div>
              <div class="file row-b-c" v-if="itm.category == 3">
                <div class="file_main">
                  <div class="size-b">{{ itm.data.fc.filename }}</div>
                  <div class="row_c size-c c-gray mg-t1">
                    <div class="text">{{ itm.data.fc.ext }}</div>
                    <div class="mg-l1">{{ itm.data.fc.size }}K</div>
                  </div>
                </div>
                <img
                  v-if="itm.data.fc.ext == 'docx'"
                  src="~@/assets/imgs/msglist/world.png"
                  alt=""
                />
                <img
                  v-else-if="
                    itm.data.fc.ext == 'jpg' ||
                    itm.data.fc.ext == 'png' ||
                    itm.data.fc.ext == 'gif'
                  "
                  src="~@/assets/imgs/msglist/jpg.png"
                  alt=""
                />
                <img
                  v-else-if="
                    itm.data.fc.ext == 'xls' || itm.data.fc.ext == 'xlsx'
                  "
                  src="~@/assets/imgs/msglist/xls.png"
                  alt=""
                />
                <img
                  v-else-if="itm.data.fc.ext == 'mp4'"
                  src="~@/assets/imgs/msglist/mp4.png"
                  alt=""
                />
                <img
                  v-else-if="itm.data.fc.ext == 'mp3'"
                  src="~@/assets/imgs/msglist/mp3.png"
                  alt=""
                />
                <img
                  v-else-if="itm.data.fc.ext == 'pdf'"
                  src="~@/assets/imgs/msglist/pdf.png"
                  alt=""
                />
                <img
                  v-else-if="
                    itm.data.fc.ext == 'ppt' || itm.data.fc.ext == 'pptx'
                  "
                  src="~@/assets/imgs/msglist/ppt.png"
                  alt=""
                />
                <img
                  v-else-if="itm.data.fc.ext == 'zip'"
                  src="~@/assets/imgs/msglist/zip.png"
                  alt=""
                />
                <img
                  v-else-if="itm.data.fc.ext == 'apk'"
                  src="~@/assets/imgs/msglist/apk.png"
                  alt=""
                />
                <img
                  v-else-if="itm.data.fc.ext == 'txt'"
                  src="~@/assets/imgs/msglist/txt.png"
                  alt=""
                />
                <img v-else src="~@/assets/imgs/msglist/notRecogn.png" alt="" />
              </div>
              <!-- 图片 -->
              <div
                v-if="itm.category == 6"
                class="imgcol cursor"
                :id="'copy' + itm.data.mid"
              >
                <img
                  :src="itm.data.bodyData.coverurl"
                  fit="cover"
                  :id="'copyImg' + itm.data.mid"
                  class="el-image"
                  @click="imgClick(itm.data.bodyData)"
                  :style="{
                    width: itm.data.bodyData.showWidth,
                    height: itm.data.bodyData.showHeight,
                  }"
                />
              </div>
              <!-- 音频 -->
              <div
                v-if="itm.category == 4"
                class="audiomsg"
                @click="playAudio(itm)"
                :style="{ width: itm.data.bodyData.width + 'px' }"
              >
                <span>{{ itm.data.bodyData.seconds }}″</span>
                <span v-html="itm.data.html"></span>
                <img
                  src="~@/assets/imgs/home/ownvoice_stop.png"
                  v-show="
                    itm.data.bodyData.type == 1 && !itm.data.bodyData.play
                  "
                />
                <img
                  src="~@/assets/imgs/home/voice_stop.png"
                  v-show="
                    itm.data.bodyData.type == 2 && !itm.data.bodyData.play
                  "
                />
                <img
                  src="~@/assets/imgs/home/ownvoice.gif"
                  v-show="itm.data.bodyData.type == 1 && itm.data.bodyData.play"
                />
                <img
                  src="~@/assets/imgs/home/voice.gif"
                  v-show="itm.data.bodyData.type == 2 && itm.data.bodyData.play"
                />
              </div>
              <!-- 视频 -->
              <div
                v-if="itm.category == 5"
                class="videocol"
                @click="videoClick(itm.data.bodyData)"
              >
                <el-image
                  :src="itm.data.bodyData.vcoverurl"
                  fit="cover"
                  class="el-image"
                  :style="{
                    width: itm.data.bodyData.sWidth,
                    height: itm.data.bodyData.sHeight,
                  }"
                >
                  <div slot="error" class="image-slot">
                    <img
                      src="~@/assets/imgs/common/avatar.jpg"
                      class="error-img"
                    />
                  </div>
                </el-image>
              </div>

              <div class="row-b-c mg-t2">
                <span class="size-s c-gray">{{ itm.title }}</span>
                <span class="size-s c-gray">{{ itm.create_time }}</span>
              </div>
            </div>
          </div>
          <div class="loadState row-c-c size-s c-gray">
            {{ loadStates[item.loadState] }}
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 消息右键操作框 -->
    <ContextMenu
      :show="contextmenushow"
      :contextmenu="contextmenu"
      :cmenutype="cmenutype"
      :isforbidden="false"
      @deleteCollect="delCollect($event.collect_id)"
    ></ContextMenu>
    <!-- @rightRemind="rightRemind"
      @userApply="userApply" -->
    <!-- 放大图片容器弹框 -->
    <div id="view-container" style="display: none" class="view-container">
      <img :src="item.url" v-for="item in imgList" :key="item.id" />
    </div>
    <!-- 视频播放容器 -->
    <Dialog v-show="video.show">
      <div class="modelbody videomodel">
        <p class="title">
          <span class="videoname" id="videoname">{{ video.title }}</span>
          <i
            class="iconfont iconIMweb_cancel_cancel closeicon"
            @click="closeVideo"
          ></i>
        </p>
        <video
          :src="video.url"
          id="tm-video"
          controls
          loop="loop"
          autoplay="autoplay"
        ></video>
      </div>
    </Dialog>
  </div>
</template>
    
<script>
import { mapMutations } from "vuex";
import { collect, msgTips } from "@/axios/path";
import ContextMenu from "@/components/home/ContextMenu"; //右键操作框
import {
  getShowTime,
  resUrl,
  setContextmenu,
  messageEmoji,
} from "@/assets/js/common";
import Viewer from "viewerjs"; //放大图片插件
import "viewerjs/dist/viewer.min.css"; //放大图片插件css
export default {
  data() {
    return {
      // 1、普通文本消息，2、超链接卡片消息，3、文件，4、音频，5、视频 ,6.图片 ,9.群名片 ,12.红包 ,88.链接
      tabs: [
        {
          name: "全部",
          type: 0,
          list: [],
          page: 1,
          load: false,
          loadState: 1,
        },
        {
          name: "文本/链接",
          type: 1,
          list: [],
          page: 1,
          load: false,
          loadState: 1,
        },
        {
          name: "图片",
          type: 6,
          list: [],
          page: 1,
          load: false,
          loadState: 1,
        },
        {
          name: "音频",
          type: 4,
          list: [],
          page: 1,
          load: false,
          loadState: 1,
        },
        {
          name: "视频",
          type: 5,
          list: [],
          page: 1,
          load: false,
          loadState: 1,
        },
        {
          name: "文件",
          type: 3,
          list: [],
          page: 1,
          load: false,
          loadState: 1,
        },
      ],
      c_index: 0,
      loadStates: ["没有更多了", "加载更多", "加载中..."],
      contextmenushow: false,
      contextmenu: {
        //会话列表右键
        top: 0,
        left: 0,
        data: {},
      },
      cmenutype: "collect", //右键类型
      imgList: [], //放大图片容器弹框
      video: {
        url: "",
        show: false,
        title: "",
      },
      count: 0,
    };
  },
  components: {
    ContextMenu,
  },
  mounted() {
    this.setApplyThis(this); //设置本页面的this对象
    this.loadList("refresh");
  },
  methods: {
    ...mapMutations(["setApplyThis", "setChatOn"]),
    loadList(type) {
      let tabs = this.tabs;
      let c_index = this.c_index;
      let tab = tabs[c_index];
      let list = tab.list;
      if (type == "refresh") {
        tab.page = 1;
      } else {
        if (tab.loadState == 0) {
          return;
        }
        tab.page++;
      }
      let params = {
        category: tab.type,
        pageNumber: tab.page,
        pageSize: 10,
      };
      tab.loadState = 2;
      collect.list(params).then((e) => {
        let _list = this.handleList(e.data.list);
        if (type == "refresh") {
          tab.list = _list;
        } else {
          tab.list = list.concat(_list);
        }
        tab.loadState = e.data.list.length < params.pageSize ? 0 : 1;
        tab.load = true;
        tabs[c_index] = tab;
        this.tabs = tabs;
      });
    },
    // 1、普通文本消息，2、超链接卡片消息，3、文件，4、音频，5、视频 ,6.图片 ,9.群名片 ,12.红包 ,88.链接
    handleList(list) {
      list
      .filter(item=>item.category != 11)
      .map((item) => {
        item.data = JSON.parse(item.content);
        if (item.category == 1) {
          let bodyc = item.data.c;
          //内容换行
          let bodyhtml = bodyc.replace(/\n/g, "<br>");
          //表情编译-begin
          bodyhtml = messageEmoji(bodyhtml);
          //网址处理
          let reg = /(http:\/\/|https:\/\/)((\w|=|\?|\.|\/|&|-|:)+)/g;
          bodyhtml = bodyhtml.replace(
            reg,
            "<a href='$1$2' target='_blank' class='texthttp'>$1$2</a>"
          );
          item.data.html = bodyhtml;
        } else if (item.category == 3) {
          item.data.fc = JSON.parse(item.data.c);
          item.data.fc.url = resUrl(item.data.fc.url);
        } else if (item.category == 4) {
          // item.data.bodyData = JSON.parse(item.data.c);

          let bodycparse = JSON.parse(item.data.c);
          bodycparse.play = false;
          if (bodycparse.seconds) {
            bodycparse.width = bodycparse.seconds * 5.8;
          }
          let bodyhtml = `<audio src="${resUrl(
            bodycparse.url
          )}" class="audio" id="audio${bodycparse.id}" mid='${
            item.mid
          }'></audio>`;
          let bodyData = {
            type: 2, // 没啥用，默认为2，标识谁发送的
            ...bodycparse,
          };
          bodyData.url = resUrl(bodyData.url);
          item.data.bodyData = bodyData;
          item.data.html = bodyhtml;
        } else if (item.category == 5) {
          item.data.bodyData = JSON.parse(item.data.c);
          item.data.bodyData.vcoverurl = resUrl(item.data.bodyData.coverurl);
          item.data.bodyData.videourl = resUrl(item.data.bodyData.url);
          item.data.vc = item.data.bodyData;
        } else if (item.category == 6) {
          // console.log(item.data.c);
          item.data.bodyData = JSON.parse(item.data.c);
          item.data.bodyData.coverurl = resUrl(item.data.bodyData.coverurl);
          item.data.bodyData.url = resUrl(item.data.bodyData.url);
        }
      });
      return list;
    },
    changeTab(e) {
      this.c_index = e.index;
      if (!this.tabs[e.index].load) {
        this.loadList("refresh");
      }
    },
    delCollect(id) {
      let tabs = this.tabs;
      let params = {
        cid: id,
      };
      collect.delete(params).then((res) => {
        if (res.ok) {
          msgTips("已删除");
          tabs.map((item) => {
            item.load = false;
          });
          this.tabs = tabs;
          this.loadList("refresh");
        } else {
          msgTips(res.msg);
        }
      });
    },
    /* 消息中的图片点击事件 */
    imgClick(item) {
      this.imgList = [item];
      var timer = setTimeout(() => {
        var viewer = new Viewer(document.getElementById("view-container"), {
          hidden: function () {
            viewer.destroy();
          },
          button: true,
          toolbar: {
            zoomIn: 4,
            zoomOut: 4,
            prev: function () {
              viewer.prev(false); //当前是第一个时是不转向查看最后一个
            },
            next: function () {
              viewer.next(false); //当前是最后一个时是不转向查看第一个
            },
            loop: false,
          },
          loop: false,
          title: false,
          navbar: false,
        });
        viewer.view(1);
        viewer.show();
        clearTimeout(timer);
      }, 100);
    },
    /* 消息中的视频点击事件 */
    videoClick(item) {
      let title = item.title;
      this.video.url = item.videourl;
      this.video.title = title;
      let $videoDom = $("#tm-video");
      let realw = item.width,
        realh = item.height,
        lw = $(window).width(),
        lh = $(window).height(),
        area = "";
      if (realw / realh > lw / lh) {
        $videoDom.css({
          width: 0.8 * lw + "px",
          height: (0.8 * lw * realh) / realw + "px",
        });
        $("#videoname").css({ width: 0.8 * lw - 100 + "px" });
      } else {
        $videoDom.css({
          width: (0.8 * lh * realw) / realh + "px",
          height: 0.8 * lh + "px",
        });
        $("#videoname").css({ width: (0.8 * lh * realw) / realh - 100 + "px" });
      }
      this.video.show = true;
    },
    /* 消息播放音频 */
    playAudio(item) {
      let _this = this;
      let audio = document.getElementById("audio" + item.data.bodyData.id);
      audio.currentTime = 0;
      // audio.volume = 1;
      let allaudio = document.getElementsByClassName("audio");
      // $.each(allaudio, (i, v) => {
      //   if (audio != v) {
      //     let mid = v.getAttribute("mid");
      //     _this.MessageList.map((val) => {
      //       if (val.mid == mid && val.ct == 4) {
      //         val.data.play = false;
      //       }
      //     });
      //     v.pause();
      //   }
      // });
      if (audio.paused) {
        item.data.bodyData.play = true;
        audio.play();
        $("#audio" + item.data.bodyData.id).unbind("ended");
        $("#audio" + item.data.bodyData.id).on("ended", function () {
          item.data.bodyData.play = false;
          audio.pause();
        });
      } else {
        item.data.bodyData.play = false;
        audio.pause();
      }
    },
    /* 关闭视频弹框 */
    closeVideo() {
      this.video.show = false;
      this.video.url = "";
    },
    /* 右键 */
    async chatContextMenu(e, v, collect_id) {
      let pos = setContextmenu(e, 130, 100);
      v.collect_id = collect_id;
      this.contextmenu = {
        top: pos.otop,
        left: pos.oleft,
        data: v,
      };
      console.log("msgdata collect", v);
      this.contextmenushow = true;
      this.$setAddEventListener("contextmenushow");
    },
    load() {
      if (this.tabs[this.c_index].load) {
        this.loadList("add");
      }
    },
  },
};
</script>
<style lang="less" scoped>
@import "~@/assets/style/less/collect/collect.less";
@import "~@/assets/style/less/home/home.less";
</style>
    