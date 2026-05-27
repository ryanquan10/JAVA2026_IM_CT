<template>
  <div>
    <div class="msg_title row-c-c">
      <span class="size-n">消息</span>
      <el-popover placement="top" width="160" v-model="visible_clear">
        <p>确认清空？</p>
        <div style="text-align: right; margin: 0">
          <el-button size="mini" type="text" @click="visible_clear = false"
            >取消</el-button
          >
          <el-button type="primary" size="mini" @click="clearNoticeList"
            >确定</el-button
          >
        </div>
        <div class="clear_btn pointer" slot="reference">清空</div>
      </el-popover>
    </div>
    <div
      class="circle"
      v-infinite-scroll="load"
      @scroll="pageScroll"
      @click="bodyClick"
    >
      <template v-if="tabs[0].list.length > 0">
        <div
          class="circle_item _row"
          v-for="(item, index) in tabs[0].notReadList"
          :key="index"
          @click="seeCircle(item)"
        >
          <div class="circle_avatar">
            <el-image :src="item.avatar" class="img">
              <div slot="error" class="image-slot">
                <img src="~@/assets/imgs/common/avatar.jpg" class="error-img" />
              </div>
            </el-image>
          </div>
          <div class="circle_main auto">
            <div class="row-b-c">
              <span class="circle_username size-n">{{
                item.fromNickname
              }}</span>
              <span class="size-n c-gray">{{ getShowTime(item.time) }}</span>
            </div>

            <div v-if="item.ct == 16" class="circle_content">
              <i class="li-icon-zan c-major"></i>
            </div>
            <div
              v-else-if="item.ct == 17"
              class="circle_content"
              v-html="item.html"
            ></div>
          </div>
          <div class="circle_cover row-c-c">
            <div v-if="item.fileType == 1" class="text">{{ item.content }}</div>
            <el-image
              v-else-if="item.fileType == 2"
              :src="item.imgs[0]"
              class="img"
            >
              <div slot="error" class="image-slot">
                <img src="~@/assets/imgs/common/avatar.jpg" class="error-img" />
              </div>
            </el-image>
            <div class="video row-c-c" v-else-if="item.fileType == 3">
              <video :src="item.videoUrl"></video>
              <i class="li-icon-play c-white"></i>
            </div>
          </div>
        </div>

        <div class="real-on row-c-c">以下是已读消息</div>
        <!-- 16点赞 17评论 -->
        <div
          class="circle_item _row"
          v-for="(item, index) in tabs[0].readList"
          :key="index"
          @click="seeCircle(item)"
        >
          <div class="circle_avatar">
            <el-image :src="item.avatar" class="img">
              <div slot="error" class="image-slot">
                <img src="~@/assets/imgs/common/avatar.jpg" class="error-img" />
              </div>
            </el-image>
          </div>
          <div class="circle_main auto">
            <div class="row-b-c">
              <span class="circle_username size-n">{{
                item.fromNickname
              }}</span>
              <span class="size-n c-gray">{{ getShowTime(item.time) }}</span>
            </div>

            <div v-if="item.ct == 16" class="circle_content">
              <i class="li-icon-zan c-major"></i>
            </div>
            <div
              v-else-if="item.ct == 17"
              class="circle_content"
              v-html="item.html"
            ></div>
          </div>
          <div class="circle_cover row-c-c">
            <div v-if="item.fileType == 1" class="text">{{ item.content }}</div>
            <el-image
              v-else-if="item.fileType == 2"
              :src="item.imgs[0]"
              class="img"
            >
              <div slot="error" class="image-slot">
                <img src="~@/assets/imgs/common/avatar.jpg" class="error-img" />
              </div>
            </el-image>
            <div class="video row-c-c" v-else-if="item.fileType == 3">
              <video :src="item.videoUrl"></video>
              <i class="li-icon-play c-white"></i>
            </div>
          </div>
        </div>
      </template>
      <div v-else class="row-c-c pd-all2 size-n c-gray">空空如也</div>
    </div>
  </div>
</template>
  
  <script>
import { mapState, mapMutations, mapActions } from "vuex";
import { getShowTime, resUrl, messageEmoji } from "@/assets/js/common";
import { circle, msgTips } from "@/axios/path";
export default {
  data() {
    return {
      c_index: 0,
      visible_clear: false,
    };
  },
  computed: {
    ...mapState({
      curruid: (state) => state.User.currUid,
      curruser: (state) => state.User.currUser,
      tabs: (state) => {
        let circleNotice = state.Ws.circleNotice;
        let list = circleNotice.list;
        let readList = [];
        let notReadList = [];
        for (let i = 0; i < list.length; i++) {
          if (list[i].wxFriendMsg.readflag == 1) {
            readList.push(list[i]);
          } else {
            notReadList.push(list[i]);
          }
        }
        circleNotice.readList = readList;
        circleNotice.notReadList = notReadList;
        return [circleNotice];
      },
    }),
  },
  mounted() {
    // this.$nextTick(() => {
    //   this.$msgcontainer = $("#msgcontainer");
    //   this.$chatEditor = $("#chat-editor");
    //   this.setChatEditor(this.$chatEditor);
    //   this.setChatSofftop($("#chat-bottom").offset().top - 88);
    // });
    // this.loadList("refresh");
  },
  methods: {
    ...mapMutations(["setNotReadCircleNoticeNum", "setCircleNotice"]),
    getShowTime,
    loadList(type, isTip) {
      console.log(type, isTip);
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
        // category: tab.type,
        pageNumber: tab.page,
        pageSize: 10,
      };
      tab.loadState = 2;
      circle.noticeList(params).then((e) => {
        let _list = this.handleList(e.data);
        if (type == "refresh") {
          isTip && msgTips("刷新成功");
          tab.list = _list;
        } else {
          tab.list = list.concat(_list);
        }
        tab.loadState = e.data.length < params.pageSize ? 0 : 1;
        tab.load = true;
        tabs[c_index] = tab;
        this.setCircleNotice(tab);
        // this.tabs = tabs;
        console.log(this.tabs);
      });
    },
    handleList(list) {
      console.log(list);
      let _list = [];
      list.map((item) => {
        item.ct = item.wxFriendMsg.ct;
        console.log(item.ct, item.content);
        if (item.likeCancel == 1) {
          return;
        }
        if (item.ct == 16) {
          // 处理点赞
          item.avatar = resUrl(item.avatar);
        } else if (item.ct == 17) {
          // 处理评论
          item.avatar = resUrl(item.avatar);
          item.html = item.momentsComments?this.handleToHtml(item.momentsComments.content) : item.content;
        } else {
          return;
        }

        // 处理内容
        // item.html = this.handleToHtml(item.content);
        let moments = item.moments;
        if (!moments) {
          return;
        }
        // 处理文件资源
        if (moments.imgUrl) {
          item.imgs = moments.imgUrl.split(",");
          item.imgs = item.imgs.map(img=>{
            return resUrl(img)
          })
          item.fileType = 2;
        } else if (moments.videoUrl) {
          item.videoUrl = resUrl(moments.videoUrl);
          item.fileType = 3;
        } else {
          item.content = moments.content;
          item.fileType = 1;
        }
        _list.push(item);
      });
      console.log(_list);
      return _list;
    },
    // 处理内容拼接emoji、处理链接等
    handleToHtml(content) {
      if (!content) {
        return "";
      }
      let html = "";
      // let bodyc = "[微笑]";
      html = content.replace(/\n/g, "<br>");
      //表情编译-begin
      html = messageEmoji(html);
      //网址处理
      let reg = /(http:\/\/|https:\/\/)((\w|=|\?|\.|\/|&|-|:)+)/g;
      html = html.replace(
        reg,
        "<a href='$1$2' target='_blank' class='texthttp'>$1$2</a>"
      );
      return html;
    },
    load(e) {
      if (this.tabs[0].load) {
        this.loadList("add");
      }
    },
    // 获取某一条朋友圈是否点赞
    isZan(item) {
      return item.likes.findIndex((item) => item.uid == this.curruid);
    },
    userZan(item, index) {
      let ix = this.isZan(item);
      if (ix == -1) {
        let params = {
          mid: item.id,
        };
        circle.addZan(params).then((res) => {
          if (res.ok) {
            let curruser = this.curruser;
            let like_obj = {
              nick: curruser.nick,
              remarkName: curruser.remark,
              ...res.data,
            };
            item.likes.push(like_obj);
          } else {
            msgTips(res.msg);
          }
        });
      } else {
        let params = {
          likesId: item.likes[ix].id,
        };
        circle.delZan(params).then((res) => {
          if (res.ok) {
            item.likes.splice(ix, 1);
          } else {
            msgTips(res.msg);
          }
        });
      }
      this.hideCircleTools(index);
    },

    showEdit(index, comment_user) {
      // let comment = {};
      this.comment = {
        text: "",
        index: index,
        comment_user,
      };
      this.$nextTick(() => {
        this.$chatEditor = $("#comment-editor");
        this.$chatEditor.focus();
        this.hideCircleTools(index);
      });
    },
    /* 发送消息 */
    sendComment() {
      // let sendVal = this.$chatEditor.html()
      let sendVal = $("#comment-editor").text();
      if (sendVal == "") {
        msgTips("请输入正文内容");
        return;
      }
      //取出输入内容中的艾特
      var atarr = [];
      if (this.isGroup) {
        let atlist = $("#comment-editor .setatbutton");
        if (atlist.length > 0) {
          $.each(atlist, function (v, item) {
            atarr.push($(item).attr("atuid"));
          });
        }
        atarr = Array.from(new Set(atarr));
      }
      this.$chatEditor.html(""); //清空输入内容
      //消息三方共用-将消息中的html标签处理
      sendVal = sendVal.replace(/<br>/gm, "\n");

      var regx = /<[^>]*>|<\/[^>]*>/gm;
      sendVal = sendVal.replace(regx, "");
      sendVal = sendVal.replace(/&nbsp;/gm, "  ");
      sendVal = sendVal.substring(0, 2000); //控制发送字数
      let comment = this.comment;
      let comment_user = comment.comment_user;
      let circle_list = this.tabs[0].list;
      let circle_item = circle_list[comment.index];
      let params = {
        content: sendVal,
        mid: circle_item.id,
        pid: comment_user ? comment_user.id : 0,
      };
      circle.addComment(params).then((res) => {
        console.log(res.ok);
        if (res.ok) {
          let curruser = this.curruser;
          let create_obj = {
            nick: curruser.nick,
            remarkName: curruser.remark,
            parent: comment_user ? comment_user : null,
            ...res.data,
          };
          circle_item.comments.push(create_obj);
          this.resetEditor();
        } else {
          msgTips(res.msg);
        }
      });
    },
    resetEditor() {
      this.comment = {
        index: -1,
        comment_user: null,
      };
    },
    delCircle(mid, index) {
      let circle_list = this.tabs[0].list;
      let params = {
        mid,
      };
      circle.delete(params).then((res) => {
        console.log(res.ok);
        if (res.ok) {
          circle_list.splice(index, 1);
        } else {
          msgTips(res.msg);
        }
      });
      // let likes = item.likes.find(like=>like.id == item.id);
    },
    showCircleTools(index) {
      this.circle_tools.show = index;
      console.log(this.circle_tools);
    },
    hideCircleTools(index) {
      let circle_list = this.tabs[0].list;
      circle_list[index].show_tools = false;
    },

    wxTextKey(e) {
      if (!this.chatOn) return;
      let keyCode = e.keyCode;
      if (keyCode == 13 && e.ctrlKey) {
        //ctrl+enter换行
        let selection = getSelection();
        let range = selection.getRangeAt(0);
        let el = document.createElement("div"); //创建一个空的div外壳
        el.innerHTML = "<br/>"; //设置span内容为我们想要插入的内容。
        // el.innerHTML='\n';
        //加入空格是为了防止在光标定位位置不准确
        let spanNode = document.createElement("span");
        spanNode.innerHTML = "&nbsp;";
        let frag = document.createDocumentFragment(); //创建一个空白的文档片段，便于之后插入dom树
        let node = el.firstChild;
        frag.appendChild(node);
        let lastNode = frag.appendChild(spanNode.firstChild);
        range.insertNode(frag); //将内容插入光标处
        selection.extend(lastNode, 0); //重新定位光标位置
        selection.collapseToEnd();
        var selection2 = getSelection();
        var range2 = selection2.getRangeAt(0);
        var textNode = range2.startContainer;
        //如果焦点在内容中换行-删除新增加的空格
        if (textNode.nextSibling && textNode.nextSibling.length > 0) {
          range2.setStart(textNode, range2.endOffset);
          range2.setEnd(textNode, range2.endOffset + 1);
          range2.deleteContents();
        }
      } else if (keyCode == 13) {
        //enter发送消息
        this.sendComment();
      }
    },
    /* 右键粘贴发送图片消息 */
    pasteSend(ev) {
      let _this = this;
      ev.preventDefault();
      this.getSelecRange();
      var dataItem = ev.clipboardData.items;
      $.each(dataItem, (i, item) => {
        if (i == 0 && item.kind == "file" && item.type.match("^image/")) {
          var file = item.getAsFile();
          if (file) {
            var reader = new FileReader();
            reader.readAsDataURL(file);
            reader.onload = function () {
              let blob = dataURLtoBlob(this.result);
              let fd = new FormData();
              fd.append("uploadFile", blob, "wxchatimg.jpg");
              fd.append("chatlinkid", _this.chatOn);
              _this.upLoadAjax("/chat/img", fd);
            };
          }
        } else {
          if (item.kind == "string" && item.type.match("^text/plain")) {
            let { selection, range } = _this.lastSelection;
            item.getAsString(function (s) {
              // $(ev.target).append(`<pre>${s}</pre>`);
              // log(s);
              var pre = document.createElement("pre");
              pre.innerHTML = s;
              var frag = document.createDocumentFragment();
              let lastNode = frag.appendChild(pre);
              //填入内容并且重新设置焦点位置
              range.insertNode(frag);
              let contentRange = range.cloneRange(); //克隆选区
              contentRange.setStartAfter(lastNode); //设置光标位置为插入内容的末尾
              contentRange.collapse(true); //移动光标位置到末尾
              selection.removeAllRanges(); //移出所有选区
              selection.addRange(contentRange); //添加修改后的选区
            });
          }
        }
      });
    },
    /* 信息输入框监听@事件 */
    listenRemind(e) {
      let sendVal = $("#comment-editor").text();
      this.comment.text = sendVal;
      if (this.isGroup && e.data == "@") {
        this.showAtContent();
      }
    },
    /* 获取输入框失去焦点时的光标位置 */
    getSelecRange() {
      let selection = getSelection(),
        range = selection.getRangeAt(0);
      this.lastSelection = {
        range: range,
        selection: selection,
      };
    },
    /* 点击表情 */
    appendMessage(e) {
      document.documentElement.click();
      let facealt = $(e.currentTarget).attr("alt");
      if (!navigator.userAgent.indexOf("Firefox") >= 0) {
        this.$chatEditor.focus();
      }
      if (!this.lastSelection.range) {
        this.getSelecRange();
      }
      let { range, selection } = this.lastSelection;

      let el = document.createElement("span"); //创建一个空的div外壳
      el.innerHTML = facealt; //设置div内容为我们想要插入的内容。
      let frag = document.createDocumentFragment(); //创建一个空白的文档片段，便于之后插入dom树

      let node = el.firstChild;
      let lastNode = frag.appendChild(node);
      console.log(lastNode);
      range.insertNode(frag); //设置选择范围的内容为插入的内容
      let contentRange = range.cloneRange(); //克隆选区
      contentRange.setStartAfter(lastNode); //设置光标位置为插入内容的末尾
      contentRange.collapse(true); //移动光标位置到末尾
      selection.removeAllRanges(); //移出所有选区
      selection.addRange(contentRange); //添加修改后的选区

      $(".iconIMweb_expression").removeClass("icon_select"); // 移除点击后的样式
      let sendVal = $("#comment-editor").text();
      this.comment.text = sendVal;
    },
    showEmojiList(e) {
      console.log(e);
      let y = e.y;
      let top = 0;
      if (y > 450) {
        top = y - 450;
      } else {
        top = y + 30;
      }
      this.emojiLocation = {
        top: top,
      };
      console.log(this.emojiLocation);
      this.showEmoji = !this.showEmoji;
    },
    closeEmojiList() {
      this.showEmoji = false;
    },

    // 页面函数 主要处理一些关闭函数
    // 页面点击
    pageScroll() {
      this.showEmoji && this.closeEmojiList();
      // console.log('scroll');
    },
    // 页面点击
    bodyClick() {
      console.log("bodyClick");
      // this.showEmoji && this.closeEmojiList();
      // if (this.circle_tools.show > -1) {
      //   this.hideCircleTools(this.circle_tools.show);
      // }
      // this.comment.index > -1 && this.resetEditor();
    },
    seeCircle(item) {
      this.$emit("seeCircle", item);
    },
    clearNoticeList() {
      this.visible_clear = false;
      circle.clearNoticeList().then((res) => {
        if (res.ok) {
          let tab = {
            name: "朋友圈通知",
            list: [],
            page: 1,
            load: false,
            loadState: 1,
          };
          this.setCircleNotice(tab);
        } else {
          msgTips(res.msg);
        }
      });
    },
  },
};
</script>
  
  <style lang="less" scoped>
/* ------------- */
.msg_title {
  position: relative;
  padding: 10px 0;
  color: #000;
  .clear_btn {
    position: absolute;
    top: 0;
    right: 0;
    padding: 10px;
    color: #576b95;
  }
}
.circle {
  height: 420px;
  overflow-y: auto;
  text-align: left;
  .real-on {
    padding: 10px;
    color: #999;
    font-size: 14px;
  }
  // margin-left: 15px;
  .circle_item {
    padding: 10px 0;
    padding-right: 10px;
    // background-color: #e1e1e1;
    &:hover {
      background-color: #efefef;
    }
    .circle_avatar {
      width: 40px;
      height: 40px;
      border-radius: 4px;
      flex-shrink: 0;
      overflow: hidden;
    }
    .circle_avatar > .img {
      width: 100%;
      height: 100%;
    }
  }
  .circle_main {
    margin-left: 6px;
    padding-right: 16px;
    .circle_username {
      color: #576b95;
    }
    .circle_content {
      margin-top: 6px;
      i {
        margin-left: 4px;
      }
    }

    .circle_footer {
      padding: 10px 0px;
      border-bottom: 2px solid #f1f1f1;

      .tag-popover {
        padding: 0;
      }
    }
    .send_box {
      position: relative;
      padding: 6px 10px;
      background-color: #f6f6f6;
      .zan_list {
        padding-bottom: 6px;
        .zan_main {
          margin-left: 8px;
          .zan_item {
            margin-right: 4px;
          }
          // .zan_item::after{
          //   content: ',';
          // }
        }
      }
      .comment_list {
        padding: 4px 0 10px;
        .comment_item {
          padding: 4px;
          line-height: 20px;

          .name {
            margin: 0 2px;
            white-space: nowrap;
          }
        }
      }
      .zan_list + .send_main,
      .comment_list + .send_main {
        margin-top: 10px;
        &::before {
          position: absolute;
          top: -10px;
          content: "";
          width: 100%;
          height: 1px;
          background-color: #e1e1e1;
        }
      }
      .send_main {
        position: relative;
        border: 1px solid #30d2b2;
        border-radius: 4px;
        margin-top: 0px;
        background-color: #fff;

        .editor-placeholder {
          position: absolute;
          top: 8px;
          left: 10px;
          font-size: 14px;
          color: #ccc;
        }
        /deep/.el-textarea__inner {
          border-width: 0px;
        }
        .el-textarea::after {
          position: absolute;
          right: 0;
          bottom: 0;
          content: "";
          width: 10px;
          height: 10px;
          background-color: #fff;
        }
        .send_footer {
          padding: 10px 10px;
          i {
            font-size: 28px;
          }
          .send_btn {
            width: 80px;
            height: 36px;
            border-radius: 4px;
            color: #666;
            background-color: #ebebeb;
            transition: all 0.2s;
          }
          .send_btn.active {
            background-color: #30d2b2;
            color: #fff;
          }
        }
      }

      .chat-editor {
        color: #000;
        border: none;
        min-height: 60px;
        outline: none;
        overflow: auto;
        width: 100%;
        padding: 10px 10px;
        word-break: break-all;
      }
    }
  }
  .circle_cover {
    width: 40px;
    height: 40px;
    border-radius: 4px;
    flex-shrink: 0;
    overflow: hidden;
    background-color: #f6f6f6;

    .img {
      width: 100%;
      height: 100%;
    }
    .video {
      position: relative;
      width: 100%;
      height: 100%;

      video {
        height: 100%;
      }
      i {
        position: absolute;
        font-size: 16px;
      }
    }
    .text {
      display: -webkit-box;
      overflow: hidden;
      text-overflow: ellipsis;
      -webkit-line-clamp: 3;
      -webkit-box-orient: vertical;
      line-height: 1.4em;
      font-size: 12px;
      padding: 6px;
      // text-align: center;
    }
  }

  /* 表情布局 */
  .tm-emoji-container {
    width: 496px;
    height: 300px;
    position: fixed;
    top: 0px;
    left: 200px;
    z-index: 22;
    background: #fff;
    box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
    #tm-emoji-body {
      height: 250px;
      overflow: auto;
    }
    .tm-emoji-body {
      flex-wrap: wrap;
      padding: 10px;
      user-select: none;
      li {
        padding: 5px;
      }
    }
    .tm-emoji-btngroup {
      height: 50px;
      background: #f2f2f2;
      border-top: 1px solid #f2f2f2;
      li {
        padding: 9px 15px;
        &.on {
          background: #fff;
        }
        img {
          width: 30px;
        }
      }
    }
  }
}
</style>
  <style lang="less">
.circle_more {
  &:hover {
    color: #30d2b2;
  }
}
.el-popover.popoverStyle {
  padding: 0px;
  font-size: 12px;
  color: #333;
  .items {
    border-radius: 4px;
    overflow: hidden;
    .item {
      height: 40px;
      padding: 0 10px;
      color: #fff;
      font-size: 15px;
      background-color: rgb(76, 76, 76);
      span {
        margin-left: 4px;
        font-size: 14px;
        line-height: 1em;
      }
    }
    .item:hover {
      background-color: rgb(68, 68, 68);
    }
  }
}
.el-popover .popper__arrow {
  border-left-color: rgb(76, 76, 76) !important;
}
.el-popover.popoverStyle .popper__arrow::after {
  border-left-color: rgb(76, 76, 76) !important;
}
</style>