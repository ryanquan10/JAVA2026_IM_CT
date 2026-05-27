<template>
  <div>
    <div class="circle" @click="bodyClick">
      <div
        class="submit_btn"
        :class="{
          op5: !(
            content.length > 0 ||
            imgList.length > 0 ||
            videoList.length > 0
          ),
        }"
        @click="onSubmit"
      >
      <i v-show="isSubmit" class="el-icon-loading size-max"></i>
      <span v-show="!isSubmit" >发布</span>
      </div>
      <div
        id="circle-publish-editor"
        name="content"
        class="chat-editor"
        contenteditable="true"
        @keydown.enter.prevent="wxTextKey"
        @paste="pasteSend"
        @input="listenRemind"
        @blur="getSelecRange"
      ></div>
      <!-- 图片预览器 -->
      <div class="uploads row_c wrap mg-t2">
        <el-upload
          class="upload"
          ref="upload1"
          name="file"
          action="/mytio/user/uploadFile.tio_x"
          :data="{ type: 1 }"
          :file-list="imgList"
          :limit="9"
          multiple
          list-type="picture-card"
          :on-success="handleSuccess"
          :on-preview="handlePictureCardPreview"
          :on-remove="handleRemove"
        >
          <i class="el-icon-plus"></i>
        </el-upload>
      </div>
      <!-- 视频预览器 -->
      <div class="videos" v-if="videoList.length > 0">
        <video
          id="video"
          class="video"
          :src="videoList[0].url"
          @click="seeVideo"
        ></video>
      </div>
      <!-- <div class="videos">
        <video class="video" :src="videoUrl"></video>
      </div> -->

      <div class="line"></div>
      <div class="emoji">
        <i
          class="li-icon-emoji"
          title="选择表情"
          @click.stop="showEmojiList"
        ></i>

        <el-upload
          class="_upload"
          accept=".jpg,.jpeg,.png,.gif,.bmp,.pdf,.JPG,.JPEG,.PBG,.GIF,.BMP,.PDF"
          action="/mytio/user/uploadFile.tio_x"
          :file-list="imgList"
          name="file"
          :show-file-list="false"
          :on-success="handleSuccess"
          @on-error="fileError"
          :data="{ type: 1 }"
          :limit="9"
          multiple
        >
          <i title="选择图片" class="el-icon-picture"></i>
        </el-upload>
        <el-upload
          class="_upload"
          accept="video/*"
          action="/mytio/user/uploadFile.tio_x"
          name="file"
          :file-list="videoList"
          :show-file-list="showVideoP"
          :on-success="handleVideoSuccess"
          @on-error="fileError"
          :data="{ type: 2 }"
          :before-upload="beforeUpload"
        >
          <i title="选择视频" class="el-icon-camera-solid"></i>
        </el-upload>

        <!-- 表情弹框 -->
        <div class="tm-emoji-container" v-show="showEmoji">
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
              <img
                :src="staticUrl + 'static/emoji/emoji/' + emojiList[0].url"
              />
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>
  
  <script>
import CircleResources from "../common/CircleResources.vue";
import { emojData } from "@public/static/emoji/emojUtil"; //处理表情包方法
import { circle, msgTips } from "@/axios/path";
import { resUrl, messageEmoji } from "@/assets/js/common";
export default {
  props: {
    circleType: {
      type: String | Number,
    }
  },
  components: {
    CircleResources,
  },
  data() {
    return {
      count: 0,
      visible: false,
      circle_list: [ ],
      comment: {
        text: "",
      },
      $chatEditor: null, //发送消息输入框dom
      // emoj
      emojiList: emojData.emojiList,
      staticUrl: "", //表情包拼接的绝对路径
      showEmoji: false, //表情包布局是否显示
      emojiLocation: {
        top: 0,
      },

      dialogImageUrl: "",
      dialogVisible: false,
      imgList: [],
      videoList: [],
      videoUrl:
        "http://192.168.1.19:82/comment/video/2023112333/1541001727593073988476928.mp4",
      showVideoP: false,

      content: "",
      isSubmit: false,
    };
  },
  watch: {
    circleType(val){
      val == 1 && this.getFocus();
    }
  },
  mounted() {
    console.log('publish circle load');
    this.$chatEditor = $("#circle-publish-editor");
    this.getFocus();
  },
  methods: {
    load(e) {
      this.count += 2;
    },
    getFocus(){
      this.$chatEditor.focus();
    },
    /* 发送消息 */
    onSubmit() {
      if (this.isSubmit) {
        return;
      }
      this.isSubmit = true;
      let videoUrl = "",
        imgUrl = "";
      if (this.imgList.length > 0) {
        imgUrl = this.imgList.map((item) => item._url).join(",");
      }
      if (this.videoList.length > 0) {
        videoUrl = this.videoList[0]._url;
      }
      // let sendVal = this.sendMsg;
      let sendVal = this.$chatEditor.text();
      if (
        !(this.content.length || this.videoList.length || this.imgList.length)
      ) {
        msgTips("请输入正文内容");
        this.isSubmit = false;
        return;
      }
      this.$chatEditor.html(""); //清空输入内容
      //消息三方共用-将消息中的html标签处理
      sendVal = sendVal.replace(/<br>/gm, "\n");

      var regx = /<[^>]*>|<\/[^>]*>/gm;
      sendVal = sendVal.replace(regx, "");
      sendVal = sendVal.replace(/&nbsp;/gm, "  ");
      sendVal = sendVal.substring(0, 2000); //控制发送字数
      let params = {
        content: sendVal,
        videoUrl,
        imgUrl,
      };
      circle
        .add(params)
        .then((res) => {
          if (res.ok) {
            this.resetForm();
            this.$emit("published");
            msgTips("发布成功");
          } else {
            msgTips(res.msg);
          }
          this.isSubmit = false;
        })
        .catch((err) => {
          msgTips(err);
          this.isSubmit = false;
        });
    },
    resetForm() {
      this.imgList = [];
      this.videoList = [];
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
        this.onSubmit();
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
      this.content = this.$chatEditor.text();
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
      range.insertNode(frag); //设置选择范围的内容为插入的内容
      let contentRange = range.cloneRange(); //克隆选区
      contentRange.setStartAfter(lastNode); //设置光标位置为插入内容的末尾
      contentRange.collapse(true); //移动光标位置到末尾
      selection.removeAllRanges(); //移出所有选区
      selection.addRange(contentRange); //添加修改后的选区
      this.content = this.$chatEditor.text();
      $(".iconIMweb_expression").removeClass("icon_select"); // 移除点击后的样式
    },
    showEmojiList(e) {
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
    // 上传相关
    handleSuccess(res, file, fileList) {
      if (res.ok) {
        file.url = resUrl(file.response.data);
        file._url = file.response.data;
        console.log(file);
        this.imgList.push(file);
        if (this.videoList.length > 0) {
          this.videoList = [];
        }
      } else {
        msgTips("上传失败，请重试");
      }
    },
    handleVideoSuccess(res, file, fileList) {
      if (res.ok) {
        file.url = resUrl(file.response.data);
        file._url = file.response.data;
        this.imgList = [];
        this.videoList = [file];
      } else {
        msgTips("上传失败，请重试");
      }
      this.showVideoP = false;
    },
    fileError() {
      msgTips("上传失败，请重试");
      this.showVideoP = false;
    },
    handlePictureCardPreview(file) {
      let imgs = this.imgList.map((item) => item.url);
      let ix = imgs.findIndex((item) => file.url == item);
      let data = {
        type: 2,
        data: {
          index: ix,
          imgs: imgs,
        },
      };
      this.$emit("seeResource", data);
      console.log(this.imgList, this.videoList);
    },
    seeVideo() {
      console.log("seeVideo");
      let data = {
        type: 3,
      };
      let dom_info = document.querySelector("#video").getBoundingClientRect();
      data.data = {
        title: "详情",
        videoUrl: this.videoList[0].url,
        width: dom_info.width,
        height: dom_info.height,
      };
      this.$emit("seeResource", data);
    },
    handleRemove(file, fileList) {
      this.imgList = fileList;
      console.log(file, fileList);
    },
    beforeUpload(file) {
      this.showVideoP = true;
      console.log({
        file,
      });

      // alert(66);
      // return false;
    },
    // 页面函数
    pageScroll() {
      this.showEmoji && this.closeEmojiList();
    },

    bodyClick() {
      this.showEmoji = false;
    },
  },
};
</script>
  
  <style lang="less" scoped>
/* ------------- */
.circle {
  position: relative;
  height: 80vh;
  overflow-y: auto;
  text-align: left;
  padding: 30px 20px 20px;
  box-sizing: border-box;
  .submit_btn {
    position: absolute;
    top: 0;
    right: 0;
    color: #30d2b5;
    padding: 10px;
  }
  .chat-editor {
    color: #000;
    border: none;
    min-height: 300px;
    outline: none;
    overflow: auto;
    width: 100%;
    word-break: break-all;
    font-size: 16px;
    line-height: 1.2em;
  }

  .videos {
    width: 200px;
    .video {
      width: 100%;
    }
  }

  .line {
    height: 1px;
    background-color: #f1f1f1;
    margin: 10px 0;
  }
  .upload {
    display: flex;
    /deep/.el-upload--picture-card {
      display: none;
      justify-content: center;
      align-items: center;
      width: 100px;
      height: 100px;
    }
    /deep/.el-upload-list--picture-card .el-upload-list__item {
      width: 100px;
      height: 100px;
    }
  }
  ._upload {
    display: inline-block;
  }
  .emoji {
    position: relative;
    i {
      font-size: 26px;
      color: #333;
      margin-right: 10px;
      transition: background 0.2s;
      &:hover {
        color: #30d2b5;
        background: #fff;
      }
    }
    /* 表情布局 */
    .tm-emoji-container {
      height: 300px;
      position: absolute;
      bottom: -160px;
      left: 30px;
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
}
</style>
 