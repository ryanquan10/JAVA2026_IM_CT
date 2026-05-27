<template>
  <div>
    <div
    id="circle"
      class="circle"
      v-infinite-scroll="load"
      @scroll="pageScroll"
      @click="bodyClick"
    >
      <template v-if="tabs[0].list.length > 0">
        <div
          class="circle_item _row"
          v-for="(item, index) in mid ? single_circle : tabs[0].list"
          :key="index"
        >
          <div class="circle_avatar" >
            <div class="circle_avatar_mask" :data-uid="item.uid" data-type="show_card"></div>
            <el-image :src="item.avatar" class="img">
              <div slot="error" class="image-slot">
                <img src="~@/assets/imgs/common/avatar.jpg" class="error-img" />
              </div>
            </el-image>
          </div>
          <div class="circle_main auto">
            <span class="circle_username size-b">{{ item.nick }}</span>
            <div
              class="circle_content"
              v-html="item.html"
              v-if="item.content"
            ></div>
            <CircleResources
              :type="item.msgType"
              :item="item"
              @seeResource="$emit('seeResource', $event)"
            />
            <div class="circle_footer row-b-c">
              <div class="row_c">
                <div class="size-n c-black">
                  {{ getShowTime(item.createTime) }}
                </div>
                <div
                  class="size-s c-major mg-l2 pointer"
                  @click="delCircle(item.id, index)"
                  v-if="item.is_me"
                >
                  删除
                </div>
              </div>
              <el-popover
                popper-class="popoverStyle"
                placement="left"
                trigger="click"
                v-model="item.show_tools"
              >
                <div class="items row-b-c">
                  <div
                    class="item row-c-c auto pointer"
                    @click.stop="userZan(item, index)"
                  >
                    <i class="li-icon-zan size-b" slot="reference"></i>
                    <span class="">{{
                      isZan(item) == -1 ? "赞" : "取消"
                    }}</span>
                  </div>
                  <div
                    class="item row-c-c auto pointer"
                    @click.stop="showEdit(index)"
                  >
                    <i
                      class="el-icon-chat-dot-square size-t"
                      slot="reference"
                    ></i>
                    <span class="">评论</span>
                  </div>
                </div>
                <i
                  class="circle_more el-icon-more size-t pointer"
                  slot="reference"
                  @click.stop="showCircleTools(index)"
                ></i>
              </el-popover>
            </div>

            <div
              class="send_box"
              v-if="
                item.comments.length > 0 ||
                item.likes.length > 0 ||
                comment.index == index
              "
            >
              <div class="zan_list row-b-c" v-if="item.likes.length > 0">
                <i class="li-icon-zan size-n c-major"></i>
                <div class="zan_main _row wrap auto">
                  <div
                    class="zan_item c-major"
                    v-for="like in item.likes"
                    :key="like.id"
                  >
                    {{ like.nick }}
                  </div>
                </div>
              </div>
              <ul
                class="comment_list"
                v-if="item.comments.length > 0"
                @click.stop="&quot;#&quot;;"
              >
                <div v-for="(cm, ix) in item.comments" :key="ix">
                  <li
                    class="comment_item _row"
                    :class="{ del: curruid == item.uid || curruid == cm.uid }"
                    @click="showEdit(index, cm)"
                  >
                    <template v-if="cm.parent">
                      <div class="name c-major">
                        {{ cm.remarkName ? cm.remarkName : cm.nick }}
                      </div>
                      <div class="name">回复</div>

                      <div class="name c-major">
                        {{
                          cm.parent.remarkName
                            ? cm.parent.remarkName
                            : cm.parent.nick
                        }}
                      </div>
                    </template>
                    <div v-else class="name c-major">
                      {{ cm.remarkName ? cm.remarkName : cm.nick }}
                    </div>

                    <div>：</div>
                    <div class="" v-html="handleToHtml(cm.content)"></div>
                    <div
                      class="delete row-c-c pointer"
                      @click.stop="delComment(item.id, cm.id)"
                    >
                      <i class="el-icon-close"></i>
                    </div>
                  </li>
                </div>
              </ul>
              <div
                class="send_main"
                @click.stop="showEmoji = false"
                v-if="comment.index == index"
              >
                <span
                  class="editor-placeholder"
                  v-show="comment.text.length == 0"
                  >{{
                    comment.comment_user
                      ? "回复" +
                        (comment.comment_user.remarkName
                          ? comment.comment_user.remarkName
                          : comment.comment_user.nick)
                      : "评论"
                  }}</span
                >
                <div
                  id="comment-editor"
                  name="content"
                  class="chat-editor"
                  contenteditable="true"
                  @keydown.enter.prevent="wxTextKey"
                  @paste="pasteSend"
                  @input="listenRemind"
                  @blur="getSelecRange"
                ></div>
                <div class="send_footer row-e-c">
                  <i
                    class="li-icon-emoji pointer"
                    @click.stop="showEmojiList"
                  ></i>
                  <div
                    class="send_btn active row-c-c mg-l1 pointer"
                    @click.stop="sendComment"
                  >
                    发送
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="row-c-c pd-all2 size-n c-gray">
          {{ loadStates[tabs[0].loadState] }}
        </div>
      </template>
      <el-empty
        v-if="tabs[0].list.length == 0 && tabs[0].load"
        description="你还没有朋友发布朋友圈哦~"
      ></el-empty>

      <!-- 表情弹框 -->
      <div
        class="tm-emoji-container"
        v-show="showEmoji"
        :style="{ top: emojiLocation.top + 'px' }"
        @click.stop="&quot;#&quot;;"
      >
        <div id="tm-emoji-body">
          <ul class="tm-emoji-body flexbox">
            <li
              v-for="(emoji, index) in emojiList"
              :key="index"
              @click="appendMessage"
              :alt="emoji.alt"
            >
              <img
                :src="staticUrl + 'static/emoji/emoji/' + emoji.url"
                class="small-emoji"
                style="width: 24px"
              />
            </li>
          </ul>
        </div>
        <ul class="tm-emoji-btngroup flexbox">
          <li class="on">
            <img :src="staticUrl + 'static/emoji/emoji/' + emojiList[0].url" />
          </li>
        </ul>
      </div>
    </div>
    <!-- 个人名片 -->
    <UserCard
      :show.sync="cardshow"
      :userCard="userCard"
      ref="usercard"
    ></UserCard>
  </div>
</template>

<script>
import { mapState, mapMutations, mapActions } from "vuex";
import CircleResources from "../common/CircleResources.vue";
import { emojData, replaceEmoji } from "@public/static/emoji/emojUtil"; //处理表情包方法
import { getShowTime, resUrl, messageEmoji } from "@/assets/js/common";
import { circle, msgTips } from "@/axios/path";
import UserCardMixins from "@/mixins/usercard.js"; //个人信息卡片
import UserCard from "@/components/UserCard"; //个人名片
export default {
  components: {
    CircleResources,
    UserCard
  },
  props: {
    // mid如果有值，那么将请求数据显示单条朋友圈，其他一切逻辑将根据mid进行判断
    mid: {
      type: String | Number,
      default: "",
    },
  },
  data() {
    return {
      count: 0,
      visible: false,
      circle_list: [],
      comment: {
        index: -1,
        comment_user: null,
      },
      $chatEditor: null, //发送消息输入框dom
      // emoj
      emojiList: emojData.emojiList,
      staticUrl: "", //表情包拼接的绝对路径
      showEmoji: false, //表情包布局是否显示
      emojiLocation: {
        top: 0,
      },
      tabs: [
        {
          name: "朋友圈",
          type: 0,
          list: [],
          page: 1,
          load: false,
          loadState: 1,
        },
      ],
      c_index: 0,
      circle_tools: {
        show: -1, // 工具栏显示 对应项的下标
      },
      single_circle: [],
      loadStates: {
        0: "END",
        1: "加载更多",
        2: "加载中...",
      },
    };
  },
  computed: {
    ...mapState({
      curruid: (state) => state.User.currUid,
      curruser: (state) => state.User.currUser,
      newCircleNotice: (state) => state.Ws.newCircleNotice, //朋友圈未读消息数量
    }),
  },
  watch: {
    mid(newVal, oldVal) {
      console.log(newVal, oldVal);
      if (newVal) {
        this.single_circle = [];
        this.onlyOne();
      }
    },
    newCircleNotice(newVal, oldVal) {
      console.log(newVal);
      if (newVal) {
        let newCircleNotice = newVal;
        let ciecle_list = this.tabs[0].list;
        let c_ix = ciecle_list.findIndex(
          (item) => item.id == newCircleNotice.data.mid
        );
        if (c_ix != -1) {
          if (newCircleNotice.type == "comment") {
            let comment = newCircleNotice.data;
            if (comment.pid) {
              let parent = ciecle_list[c_ix].comments.find(
                (cm) => cm.id == comment.pid
              );
              console.log(parent);
              comment.parent = parent ? parent : null;
            }
            ciecle_list[c_ix].comments.push(comment);
          } else if (newCircleNotice.type == "like") {
            ciecle_list[c_ix].likes.push(newCircleNotice.data);
          }
        }
      }
    },
  },
  mounted() {
    // this.$nextTick(() => {
    //   this.$msgcontainer = $("#msgcontainer");
    //   this.$chatEditor = $("#chat-editor");
    //   this.setChatEditor(this.$chatEditor);
    //   this.setChatSofftop($("#chat-bottom").offset().top - 88);
    // });
    this.loadList("refresh");
    /* 监听消息事件 */
    window.addEventListener("message", function (event) {
      // 确保消息来自预期的源
      if (event.origin !== "http://example.com") return;
      const newData = event.data;
      console.log("收到的更新数据：", newData);
    });
  },
  mixins: [UserCardMixins],
  methods: {
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
      circle.list(params).then((e) => {
        let _list = this.handleList(e.data);
        if (type == "refresh") {
          isTip && msgTips("刷新成功");
          tab.list = _list;
        } else {
          tab.list = list.concat(_list);
        }
        tab.loadState = _list.length < params.pageSize ? 0 : 1;
        tab.load = true;
        tabs[c_index] = tab;
        this.tabs = tabs;
        console.log(this.tabs);
      });
    },
    onlyOne() {
      let params = {
        mid: this.mid,
      };
      circle.onlyOne(params).then((res) => {
        if (res.ok) {
          let list = [res.data];

          this.single_circle = this.handleList(list);
        }
      });
    },
    handleList(list) {
      // console.log(this.curruid, this.curruser);
      let curruid = this.curruid;
      list.map((item) => {
        item.type = item.videoUrl ? 2 : 1;
        item.is_me = curruid == item.uid;
        item.avatar = resUrl(item.avatar);
        // 处理内容
        item.html = this.handleToHtml(item.content);
        // 处理文件资源
        if (item.imgUrl) {
          item.imgs = item.imgUrl.split(",");
          item.imgs = item.imgs.map(img=>{
            return resUrl(img)
          })
          item.msgType = 2;
        } else if (item.videoUrl) {
          item.videoUrl = resUrl(item.videoUrl);
          item.msgType = 3;
        } else {
          item.msgType = 1;
        }
        // 处理点赞
        for (let i = 0; i < item.likes.length; i++) {
          if (item.likes[i + 1]) {
            item.likes[i].nick += "，";
          }
        }
        // 处理评论
        item.comments.map((comment) => {
          comment.parent = null;
          if (comment.pid) {
            let parent = item.comments.find((cm) => cm.id == comment.pid);
            console.log(parent);
            comment.parent = parent ? parent : null;
          }
        });
        // for(let i = 0; i < item.comments.length; i++){
        //   let comment = item.comments[i];
        //   if(comment)
        // }
      });
      console.log(list);
      return list;
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
          console.log(res.ok);
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
          console.log(res.ok);
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
      console.log("showEdit", index, comment_user);
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
    /* 提交评论 */
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
      let circle_list = this.mid ? this.single_circle : this.tabs[0].list;
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
          if (this.mid) {
            let del_ix = circle_list.findIndex((item) => item.id == mid);
            del_ix != -1 && circle_list.splice(del_ix, 1);
            this.$emit("close");
          } else {
            circle_list.splice(index, 1);
          }
        } else {
          msgTips(res.msg);
        }
      });
      // let likes = item.likes.find(like=>like.id == item.id);
    },
    delComment(mid, cid) {
      this.$confirm("确认删除这条评论吗？")
        .then((_) => {
          let params = {
            cid,
          };
          circle.delComment(params).then((res) => {
            console.log(res.ok);
            if (res.ok) {
              this.delCommentFromList(mid, cid);
              this.mid &&
                this.single_circle.length > 0 &&
                this.delCommentFromList(mid, cid, this.single_circle);
            } else {
              msgTips(res.msg);
            }
          });
        })
        .catch((_) => {});
    },
    // 删除本地评论 origin_list：指定的列表
    delCommentFromList(mid, cid, origin_list) {
      if (mid && cid) {
        let circle_list = origin_list ? origin_list : this.tabs[0].list;
        let cir = circle_list.find((item) => item.id == mid);
        let del_ix = cir.comments.findIndex((item) => item.id == cid);
        del_ix != -1 && cir.comments.splice(del_ix, 1);
      } else {
        console.error("mid or cid is null", mid, cid);
      }
    },
    showCircleTools(index) {
      this.circle_tools.show = index;
      console.log(this.circle_tools);
    },
    hideCircleTools(index) {
      let circle_list = this.mid ? this.single_circle : this.tabs[0].list;
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
    /**展示用户的用户信息 */
    async showCard(e, uid, item) {
      console.log('showCard', {e, uid, item});
      this.showUserCard(e, uid);
    },

    // 页面函数 主要处理一些关闭函数
    // 页面点击
    pageScroll() {
      this.showEmoji && this.closeEmojiList();
      // console.log('scroll');
    },
    // 页面点击
    bodyClick(e) {
      let dataset = e.target.dataset; 
      if(dataset.type == 'show_card'){
        let rect = document.querySelector('#circle').getBoundingClientRect();
        let event = {
          pageX: e.pageX - rect.left + 60,
          pageY: e.pageY - rect.top,
        }
        // console.log("bodyClick",event );
        this.showUserCard(event, dataset.uid);
      }
      this.showEmoji && this.closeEmojiList();
      if (this.circle_tools.show > -1) {
        this.hideCircleTools(this.circle_tools.show);
      }
      this.comment.index > -1 && this.resetEditor();
      // this.$emit('seeResource', {
      //   type: 2,
      //   data: [
      //   'http://192.168.1.19:82/comment/img/2023112351/1653191727611272637325312.png',
      //     'http://192.168.1.19:82/comment/img/2023112322/1653141727611251145711616.jpg',

      //   ]
      // })
    },
  },
};
</script>

<style lang="less" scoped>
/* ------------- */
.circle {
  height: 75vh;
  overflow-y: auto;
  text-align: left;
  margin-left: 15px;
  .circle_item {
    padding: 10px 0;
    .circle_avatar {
      position: relative;
      width: 40px;
      height: 40px;
      border-radius: 4px;
      flex-shrink: 0;
      overflow: hidden;
      .circle_avatar_mask{
        position: absolute;
        width: 100%;
        height: 100%;
        z-index: 1;
      }
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
          position: relative;
          padding: 4px;
          line-height: 20px;
          &:hover {
            background-color: #fff;
          }
          .delete {
            position: absolute;
            bottom: 0;
            right: 0;
            display: none;
            height: 28px;
            padding: 0 10px;
            color: #30d2b2;
            &:hover {
              font-weight: bold;
            }
          }
          .name {
            margin: 0 2px;
            white-space: nowrap;
          }
        }
        .del {
          &:hover {
            .delete {
              display: flex;
            }
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
.el-message-box__headerbtn .el-message-box__close:hover {
  color: #30d2b2 !important;
}
.el-button--default:hover {
  background-color: #edfaf7 !important;
  color: #30d2b2 !important;
  border-color: #30d2b2 !important;
}
.el-button--primary {
  color: #fff;
  background-color: #30d2b2 !important;
  border-color: #30d2b2 !important;
}
.el-button--primary:hover {
  color: #fff !important;
  background-color: #30d2b2 !important;
  border-color: #30d2b2 !important;
}

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