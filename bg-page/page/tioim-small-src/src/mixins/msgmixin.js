import {
  defineScroll,
  resUrl,
  dataURLtoBlob,
  setContextmenu,
} from "@/assets/js/common"
import {
  group,
  chatcom,
  friend,
  uploadFile,
  msgTips,
  layerLoading,
  layerCloseAll,
} from "@/axios/path"
import caret from "jquery.caret" //定位@框
import wsSend from "@/assets/js/ws/send"
import { wscommand } from "@/assets/js/ws/command.js"
import WxCall00Start from "@/assets/js/ws/bs/WxCall00Start.js"
import { getBase64, convertBase64ToBlob } from '@/assets/js/common';
import { getNodeType, fetchUrlAndConvertToFile, dataEncode, dataDecode, formatStringToSE } from "@/assets/js/toolsLi.js"
import { async } from "regenerator-runtime"
let fileIcons = {
  docx: require('../assets/imgs/msglist/world.png'),
  jpg: require('../assets/imgs/msglist/jpg.png'),
  png: require('../assets/imgs/msglist/jpg.png'),
  gif: require('../assets/imgs/msglist/jpg.png'),
  xls: require('../assets/imgs/msglist/xls.png'),
  xlsx: require('../assets/imgs/msglist/xls.png'),
  mp4: require('../assets/imgs/msglist/mp4.png'),
  mp3: require('../assets/imgs/msglist/mp3.png'),
  pdf: require('../assets/imgs/msglist/pdf.png'),
  ppt: require('../assets/imgs/msglist/ppt.png'),
  pptx: require('../assets/imgs/msglist/ppt.png'),
  zip: require('../assets/imgs/msglist/zip.png'),
  apk: require('../assets/imgs/msglist/apk.png'),
  txt: require('../assets/imgs/msglist/txt.png'),
  default: require('../assets/imgs/msglist/notRecogn.png'),
}
const msgMixin = {
  data() {
    return {
      lastSelection: {
        //插入内容selection
        range: null,
        selection: null,
      },
      remindshow: false, //@好友列表框是否显示
      remindsearch: "", //@框的搜索关键词
      atMemList: [], //at列表
      pcarshow: false, //分享好友名片弹框显示状态
      gcarshow: false, //分享群聊名片弹框显示状态
      isFriend: false, //当前用户是否为好友
      isforbidden: false,
      joinshow: false, //确认加入群聊弹框
      cardData: {}, //名片消息信息
      loading: false
    }
  },
  methods: {
    /* 上传图片 */
    uploadImg(e) {
      return this.uploadCom(e, "/chat/img")
    },
    /* 上传视频 */
    uploadVideo(e) {
      return this.uploadCom(e, "/chat/video")
    },
    /* 上传附件 */
    uploadDix(e) {
      return this.uploadCom(e, "/chat/file")
    },
    /* 上传投诉图片 */
    uploadReportImg(e) {
      e.fileKey = 'file';
      return this.uploadCom(e, "/sys/upload")
    },
    /* 上传处理数据 */
    uploadCom(event, url) {
      layerLoading()
      let _this = this,
        file = event.file;
      let fd = new FormData()
      if (!file) {
        msgTips("这是个空文件，无法发送，请重新选择。")
        setTimeout(() => {
          layerCloseAll()
        }, 1500)
        return
      }
      fd.append(event.fileKey ? event.fileKey : "uploadFile", file)
      fd.append("chatlinkid", _this.chatOn)
      return _this.upLoadAjaxV2(url, fd)
    },
    /* 上传处理数据 */  // old
    // uploadCom(event, url) {
    //   layerLoading()
    //   let _this = this,
    //     file = event.currentTarget.files[0],
    //     reader = new FileReader();
    //   console.log(file, 666);
    //   reader.readAsDataURL(file)
    //   reader.onload = function (e) {
    //     let blob = dataURLtoBlob(this.result)
    //     let fd = new FormData()
    //     if (blob == undefined) {
    //       msgTips(file.name + "是个空文件，无法发送，请重新选择。")
    //       setTimeout(() => {
    //         layerCloseAll()
    //       }, 1500)
    //       event.target.value = ""
    //       return
    //     }
    //     fd.append("uploadFile", blob, file.name)
    //     // fd.append("uploadFile", file)
    //     fd.append("chatlinkid", _this.chatOn)
    //     _this.upLoadAjax(url, fd)
    //     event.target.value = ""
    //   }
    // },
    /* 上传 */
    upLoadAjax(url, postdata, cb) {
      uploadFile(url, postdata).then((res) => {
        layerCloseAll()
        if (res.ok) {
          if (cb) cb(res)
        } else {
          msgTips(res.msg)
        }
      })
    },
    upLoadAjaxV2(url, postdata, cb) {
      // return uploadFile(url, postdata);
      return new Promise((resolve, reject) => {
        uploadFile(url, postdata).then((res) => {
          layerCloseAll()
          if (res.ok) {
            if (cb) cb(res)
            resolve(true)
          } else {
            msgTips(res.msg)
            resolve(false)
          }
        })
      })
    },
    /* 发起视频通话|语音邀请 */
    async wxCallInvite(type) {
      if (!this.chatOn) return
      if (this.callShow) {
        $(".iconVideocall").removeClass("icon_select"); // 移除点击后的样式
        $(".iconvoicecall").removeClass("icon_select"); // 移除点击后的样式
        msgTips("您正在通话中，请挂断后再拨")
        return
      }
      this.setCallType(type)
      WxCall00Start(type, this.bizId) // 1、语音通话；，2、视频通话
    },
    /* 选择表情 */
    chooseEmoji() {
      this.showEmoji = true
      this.$nextTick(() => {
        defineScroll($("#tm-emoji-body"))
      })

      this.$setAddEventListener("showEmoji")
      if (navigator.userAgent.indexOf('Firefox') >= 0) {
        this.$chatEditor.focus()
      }
    },
    /* 点击表情 */
    appendMessage(e) {
      document.documentElement.click()
      let facealt = $(e.currentTarget).attr("alt")
      if (!navigator.userAgent.indexOf('Firefox') >= 0) {
        this.$chatEditor.focus()
      }
      if (!this.lastSelection.range) {
        this.getSelecRange()
      }
      let { range, selection } = this.lastSelection

      let el = document.createElement("span") //创建一个空的div外壳
      el.innerHTML = facealt //设置div内容为我们想要插入的内容。
      let frag = document.createDocumentFragment() //创建一个空白的文档片段，便于之后插入dom树

      let node = el.firstChild
      let lastNode = frag.appendChild(node)
      console.log(lastNode);
      range.insertNode(frag) //设置选择范围的内容为插入的内容
      let contentRange = range.cloneRange() //克隆选区
      contentRange.setStartAfter(lastNode) //设置光标位置为插入内容的末尾
      contentRange.collapse(true) //移动光标位置到末尾
      selection.removeAllRanges() //移出所有选区
      selection.addRange(contentRange) //添加修改后的选区

      $(".iconIMweb_expression").removeClass("icon_select"); // 移除点击后的样式 
    },
    /* 输入框enter发送消息;ctrl+enter换行 ; */
    wxTextKey(e) {
      if (!this.chatOn) return
      let keyCode = e.keyCode
      if (keyCode == 13 && e.ctrlKey) {
        //ctrl+enter换行
        let selection = getSelection()
        let range = selection.getRangeAt(0)
        let el = document.createElement("div") //创建一个空的div外壳
        el.innerHTML = "<br/>" //设置span内容为我们想要插入的内容。
        // el.innerHTML='\n';
        //加入空格是为了防止在光标定位位置不准确
        let spanNode = document.createElement("span")
        spanNode.innerHTML = "&nbsp;"
        let frag = document.createDocumentFragment() //创建一个空白的文档片段，便于之后插入dom树
        let node = el.firstChild
        frag.appendChild(node)
        let lastNode = frag.appendChild(spanNode.firstChild)
        range.insertNode(frag) //将内容插入光标处
        selection.extend(lastNode, 0) //重新定位光标位置
        selection.collapseToEnd()
        var selection2 = getSelection()
        var range2 = selection2.getRangeAt(0)
        var textNode = range2.startContainer
        //如果焦点在内容中换行-删除新增加的空格
        if (textNode.nextSibling && textNode.nextSibling.length > 0) {
          range2.setStart(textNode, range2.endOffset)
          range2.setEnd(textNode, range2.endOffset + 1)
          range2.deleteContents()
        }
      } else if (keyCode == 13) {
        //enter发送消息
        this.chatSendMessageBefore()
      }
    },
    /*  drop_handler(ev) {
            let _this=this;
            ev.preventDefault();
            var dataItem = ev.clipboardData.items;
            $.each(dataItem,(i,item)=>{
                if(i==0){
                    var file= item.getAsFile();
                }else{
                    if ((item.kind == 'string') && 
                        (item.type.match('^text/plain'))) {
                        // This item is the target node
                        item.getAsString(function (s){
                            $(ev.target).append(`<pre>${s}</pre>`)
                        //   ev.target.appendChild(document.getElementById(s)); 
                            log(s);
                        });
                    } 
                    else if ((item.kind == 'string') && 
                                (item.type.match('^text/html'))) {
                        log("... Drop: HTML");
                    } else if ((item.kind == 'string') && 
                                (item.type.match('^text/uri-list'))) {
                        log("... Drop: URI");
                    } else if ((item.kind == 'file') && 
                                (item.type.match('^image/'))) {
                        var f = item.getAsFile();
                        log("... Drop: File ");
                    }
                }
            })
        }, */
    /* 右键粘贴发送图片消息 */
    pasteSend(ev) {
      let _this = this
      ev.preventDefault()
      this.getSelecRange()
      var dataItem = ev.clipboardData.items
      $.each(dataItem, (i, item) => {
        if (i == 0 && item.kind == "file" && item.type.match("^image/")) {
          var file = item.getAsFile()
          console.log('pasteSend', file, item);
          if (file) {
            var reader = new FileReader()
            reader.readAsDataURL(file)
            reader.onload = function () {
              let blob = dataURLtoBlob(this.result)
              let fd = new FormData()
              fd.append("uploadFile", blob, "wxchatimg.jpg")
              fd.append("chatlinkid", _this.chatOn)
              _this.upLoadAjax("/chat/img", fd)
            }
          }
        } else {
          if (item.kind == "string" && item.type.match("^text/plain")) {
            let { selection, range } = _this.lastSelection
            item.getAsString(function (s) {
              // $(ev.target).append(`<pre>${s}</pre>`);
              // log(s);
              var pre = document.createElement("pre")
              pre.innerHTML = s
              var frag = document.createDocumentFragment()
              let lastNode = frag.appendChild(pre)
              //填入内容并且重新设置焦点位置
              range.insertNode(frag)
              let contentRange = range.cloneRange() //克隆选区
              contentRange.setStartAfter(lastNode) //设置光标位置为插入内容的末尾
              contentRange.collapse(true) //移动光标位置到末尾
              selection.removeAllRanges() //移出所有选区
              selection.addRange(contentRange) //添加修改后的选区
            })
          }
        }
      })
    },
    pasteSendV2(ev) {
      console.log('pasteSendV2');
      let _this = this
      ev.preventDefault()
      this.getSelecRange()
      var dataItem = ev.clipboardData.items
      
      let isImg = false;
      try{
        // 不知道为什么复制的图片会出现在数组的第二项，如果第二项为图片且第一项不是图片删除第一项，否则不做处理
        let one = dataItem[0];
        let two = dataItem[1];
        if((one.kind != "file" || !one.type.match("^image/")) && two.kind == "file" && two.type.match("^image/")){
          isImg = true;
          console.log('delete one', dataItem)
        }
      }catch(e){
        console.log(e)
      }
      $.each(dataItem, (i, item) => {
        console.log(i, item);
        if(i == 0 && isImg){
          console.log('return');
          return
        }
        if ((i == 0 || i == 1) && item.kind == "file" && item.type.match("^image/")) {
          var file = item.getAsFile()
          // console.log('pasteSend', file, item);
          if (file) {
            let fileObj = {
              type: file.type,
              name: file.name,
              file: file
            }
            this.handleFileChange([fileObj])
          }
        } else {
          if (item.kind == "string" && item.type.match("^text/plain")) {
            let { selection, range } = _this.lastSelection
            item.getAsString(function (s) {
              // $(ev.target).append(`<pre>${s}</pre>`);
              // log(s);
              var pre = document.createElement("pre")
              pre.innerHTML = s
              var frag = document.createDocumentFragment()
              let lastNode = frag.appendChild(pre)
              //填入内容并且重新设置焦点位置
              range.insertNode(frag)
              let contentRange = range.cloneRange() //克隆选区
              contentRange.setStartAfter(lastNode) //设置光标位置为插入内容的末尾
              contentRange.collapse(true) //移动光标位置到末尾
              selection.removeAllRanges() //移出所有选区
              selection.addRange(contentRange) //添加修改后的选区
            })
          }
        }
      })
    },
    moveCursorToLast(){
      let { selection, range } = this.lastSelection
      var frag = document.createDocumentFragment()
      let lastNode;
      if(this.getIsCiteAnchor){
        lastNode = document.getElementById('citeAnchor').previousSibling;
      }else{
        lastNode = document.getElementById('chat-editor').lastChild;
      }
      // $("#chat-editor").children().last();
      console.log(lastNode);
      //填入内容并且重新设置焦点位置
      range.insertNode(frag)
      let contentRange = range.cloneRange() //克隆选区
      contentRange.setStartAfter(lastNode) //设置光标位置为插入内容的末尾
      contentRange.collapse(true) //移动光标位置到末尾
      selection.removeAllRanges() //移出所有选区
      selection.addRange(contentRange) //添加修改后的选区
    },
    /* 获取输入框失去焦点时的光标位置 */
    getSelecRange() {
      let selection = getSelection(),
        range = selection.getRangeAt(0)
      this.lastSelection = {
        range: range,
        selection: selection,
      }
    },
    /* 信息输入框监听@事件 */
    listenRemind(e) {
      // console.log(e);
      if (this.isGroup && e.data == "@") {
        this.showAtContent()
      }
    },
    /* 引用一条消息 */
    quoteMsg(data) {
      console.log('你引用了一条消息', data);
      // 处理选取的文件
      let citeAnchor = $('#citeAnchor')
      let isCiteAnchor = citeAnchor.length > 0 ? true : false;
      let htmlStr = this.gradleQuoteMsg(data);
      // 如果输入框内内容为空，插入一个空格
      if (this.$chatEditor.text().length == 0) {
        console.log('text null')
        $("#chat-editor").append('\u00a0');
      }

      if (isCiteAnchor) {
        citeAnchor.replaceWith(htmlStr);
      } else {
        $("#chat-editor").append(htmlStr);
      }
      this.moveCursorToLast();

    },
    // 获取当前是否存在引用
    getIsCiteAnchor(){
      return $('#citeAnchor').citeAnchor.length > 0
    },
    /* 构建消息内容 */
    gradleQuoteMsg(data) {
      let ct = data.ct;
      let contentHtml;
      console.log('datac', data);
      // 1、普通文本消息，2、超链接卡片消息，3、文件，4、音频，5、视频 ,6.图片
      switch (ct) {
        case 1:
          contentHtml = data.c;
          break;
        case 2:
          contentHtml = data.c;
          break;
        case 3:
          let fileIcon = fileIcons[data.fc.ext];
          if (!fileIcon) fileIcon = fileIcons['default'];
          contentHtml = `
          <div class="_row mg-l1">
            <span>${data.fc.filename}</span>
            <img
              class="msg-quote-img mg-l1"
              src="${fileIcon}"
              alt=""
            />
          </div>
          `;
          break;
        case 4:
          contentHtml = `
          <div class="row_c">
            <i class="li-icon-yuyin"></i>
            <span>${data.bodyData.seconds}″</span>
          </div>
          `;
          break;
        case 5:
          contentHtml = `
          <div class="mg-l1">
            <div class="msg-quote-video row-c-c">
              <img
                class="msg-quote-img"
                src="${resUrl(data.bodyData.coverurl)}"
              />
              <i class="icon-play li-icon-play"></i>
            </div>
          </div>
          `;
          break;
        case 6:
          contentHtml = `
          <div class="">
            <img
              class="msg-quote-img mg-l1"
              src="${resUrl(data.bodyData.coverurl)}"
              alt="${data.bodyData.filename}"
            />
          </div>
          `;
          break;
        case 9:
          contentHtml = `
          <div class="row_c">
            <i class="li-icon-yuyin"></i>
            <span>${data.bodyData.bizname}的${data.bodyData.cardtype == 1 ? '个人' : '群'}名片</span>
          </div>
          `;
          break;
        case 20:
          contentHtml = `
          <div class="_row">
            <i class="li-icon-yuyin"></i>
            <span>${data.nick}的云笔记</span>
            ${data.bodyData.imgUrl ?
              `
              <img
                class="msg-quote-img mg-l1"
                src="${data.bodyData.imgUrl}"
                alt=""
              />
              `:
              ''
            }
          </div>
          `;
          break;
        default:

      }
      if (!contentHtml) {
        msgTips('引用格式错误，请重试');
        return false;
      }
      let _data = dataEncode(data)
      let formatStr = `
        <div id="citeAnchor" contenteditable="false" data-data="${_data}">
          <div class="cite" onclick="previewCite()">
              <div class="cite_text _row">
                  <span>${data.nick}：</span>
                  ${contentHtml}
              </div>
              <i class="city_fork_icon li-icon-close" onclick="clearCite(event)"></i>
          </div>
        </div>
      `;
      return formatStr
    },
    /* 获取编辑区的所有有效节点，返回一个规则的对象数组，根据数组顺序发送消息 */
    getSendMsgList(contentsNode) {
      console.log('contentsNode', contentsNode);
      let arr = [];
      for (let i = 0; i < contentsNode.length; i++) {
        let node = contentsNode[i];
        let nodeType = getNodeType(node);
        let lastNode = arr.length > 0 ? arr[arr.length - 1] : null;
        let typeFlags = ['text', 'img', 'pre'];
        if (typeFlags.includes(nodeType)) {
          if (nodeType == 'img') {
            arr.push({
              type: nodeType,
              val: node.getAttribute("src")
            })
          } else {
            let textContent = formatStringToSE(node.textContent)
            if (lastNode && (['text', 'pre'].includes(lastNode.type) && ['text', 'pre'].includes(nodeType))) {
              lastNode.val += nodeType == 'pre' ? '\n' + textContent : textContent;
            } else {
              arr.push({
                type: nodeType,
                val: textContent
              })
            }
          }
        }
      }
      console.log('rcontentsNode', arr);
      return arr
    },
    /*获取引用消息信息 */
    getQuoteMsg() {
      let citeAnchor = $('#citeAnchor')
      let citeData = citeAnchor.attr('data-data');
      if (citeData) {
        let citeMsg = dataDecode(citeData);
        console.log("======================citeMsg==========================",citeMsg)
        let quoteMsg = {
          quotemsgcontent: citeMsg.c,
          quotemid: citeMsg.mid,
          quotemsgtype: citeMsg.ct,
          quotesrcnick: citeMsg.nick,
        }
        return quoteMsg;
      } else {
        return false;
      }
    },

    // 发送多条消息
    async chatSendMessageBefore() {
      let sendVal = this.$chatEditor.text()
      let contentsNode = this.$chatEditor.contents();
      let sendMsgList = this.getSendMsgList(contentsNode);
      let quoteMsg = this.getQuoteMsg();
      let quoteMsgIx = quoteMsg ? sendMsgList.findIndex((item) => (item.type == 'text' || item.type == 'pre')) : -1;  // 只有第一条文字消息需要加入引用，获取他位于数组中的位置
      // 给多条消息中的第一条文本消息添加一个识别符，发送时判断是否需要携带@的文本和参数
      let atlist = $("#chat-editor .setatbutton");
      if (this.isGroup && atlist.length > 0) {
        let firstItem = sendMsgList.find((item) => (item.type == 'text' || item.type == 'pre'));
        firstItem.isAt = true;
      }
      // console.log('getQuoteMsg', this.getQuoteMsg(), quoteMsgIx);
      console.log('sendMsgList', sendMsgList);
      if (sendMsgList.length > 0) {
        for (let i = 0; i < sendMsgList.length; i++) {
          await new Promise(async (resolve, reject) => {
            // setTimeout(async () => {
            // console.log('chatSendMessage' +  i);
            let msgItem = sendMsgList[i];
            if (['text', 'pre'].includes(msgItem.type)) {
              if (quoteMsgIx != -1 && i == quoteMsgIx) {
                this.chatSendMessage(msgItem.val, quoteMsg, msgItem.isAt);
                // debugger
              } else {
                this.chatSendMessage(msgItem.val, null, msgItem.isAt);
              }
            } else if (msgItem.type == 'img') {
              let params = {
                file: await fetchUrlAndConvertToFile(msgItem.val)
              }
              await this.uploadImg(params);
            } else {

            }
            resolve()
            // }, 1000)
          })
        }
        this.$chatEditor.html("") //清空输入内容
        console.log('清空输入内容');
      } else {
        this.chatSendMessage(sendVal);
      }
    },
    /* 发送消息 */
    chatSendMessage(sendVal, quoteMsg, isAt) {
      // let sendVal = this.sendMsg;
      // let sendVal = this.$chatEditor.html()
      // sendVal = this.$chatEditor.text()
      console.log('chatSendMessage', sendVal, quoteMsg);
      if (sendVal == "") {
        msgTips("请输入正文内容")
        return
      }
      //取出输入内容中的艾特
      var atarr = []
      if (this.isGroup && isAt) {
        let atlist = $("#chat-editor .setatbutton")
        if (atlist.length > 0) {
          $.each(atlist, function (v, item) {
            atarr.push($(item).attr("atuid"))
            sendVal = $(item).text() + sendVal;
          })
        }
        atarr = Array.from(new Set(atarr))
      }

      this.$chatEditor.html("") //清空输入内容
      //消息三方共用-将消息中的html标签处理
      // debugger
      sendVal = sendVal.replace(/<br>/gm, "\n")

      var regx = /<[^>]*>|<\/[^>]*>/gm
      sendVal = sendVal.replace(regx, "")
      sendVal = sendVal.replace(/&nbsp;/gm, "  ")
      sendVal = sendVal.substring(0, 2000) //控制发送字数
      if (this.isGroup) {
        let sendreq = {
          c: sendVal,
          chatlinkid: this.chatOn
        }
        if (quoteMsg) Object.assign(sendreq, quoteMsg);
        if (atarr.length > 0) {
          sendreq.at = atarr.join(",")
        }
        // debugger
        wsSend(wscommand.WxGroupChatReq, sendreq)
      } else {
        let sendreq = {
          c: sendVal,
          chatlinkid: this.chatOn,
        }
        if (quoteMsg) Object.assign(sendreq, quoteMsg);
        console.log('发送信息给好友', sendreq);
        wsSend(wscommand.WxFriendChatReq, sendreq)
      }
    },
    /* 隐藏@框 */
    hideRemind() {
      this.remindshow = false
    },
    /* 显示@框 */
    showAtContent() {
      document.documentElement.click()
      this.remindsearch = ""
      this.remindshow = true
      this.$setAddEventListener("remindshow")
      this.atGroupUserList()
      this.$nextTick(() => {
        defineScroll($("#remindfriends"))
      })
      //使用caret定位@框的位置
      let realHeight = $(window).height() //浏览器高度
      var offset = this.$chatEditor.caret("offset")
      $("#remindContent").offset({
        left: offset.left + 30,
        top: offset.top - realHeight / 2,
      })
      setTimeout(() => {
        $("#remindsearch").focus()
      }, 300)
    },
    /* 获取@列表 */
    atGroupUserList() {
      let postdata = {
        groupid: this.bizId,
        searchkey: this.remindsearch,
      }
      group.atGroupUserList(postdata).then((res) => {
        if (res.ok) {
          let data = res.data
          data.map((item) => {
            item.avatar = resUrl(item.avatar)
          })
          this.atMemList = data
        }
      })
    },
    /* 防抖 */
    debounce(fn, delay) {
      if (this.timer != null) {
        clearTimeout(this.timer)
      }
      this.timer = setTimeout(fn, delay)
    },
    /* @框搜索事件 */
    remindSearch() {
      let _this = this
      this.debounce(function () {
        _this.atGroupUserList()
      }, 300)
    },
    /* 快捷方式调取@列表 */
    getRemindContent() {
      document.documentElement.click()
      //如果selection没有内容-重新获取焦点获取选取信息
      if (!this.lastSelection.range) {
        this.$chatEditor.focus()
        let nselection = getSelection(),
          nrange = nselection.getRangeAt(0)
        this.lastSelection = {
          range: nrange,
          selection: nselection,
        }
      }
      let { selection, range } = this.lastSelection
      //手动添加@符号给信息框
      let spanNode1 = document.createElement("span")
      spanNode1.innerHTML = "@"
      var frag = document.createDocumentFragment()
      let lastNode = frag.appendChild(spanNode1.firstChild)

      //填入内容并且重新设置焦点位置
      range.insertNode(frag)
      let contentRange = range.cloneRange() //克隆选区
      contentRange.setStartAfter(lastNode) //设置光标位置为插入内容的末尾
      contentRange.collapse(true) //移动光标位置到末尾
      selection.removeAllRanges() //移出所有选区
      selection.addRange(contentRange) //添加修改后的选区
      //显示@列表
      this.showAtContent()
    },
    /* 头像右键@TA */
    rightRemind() {
      //如果selection没有内容-重新获取焦点获取光标信息
      if (!this.lastSelection.range) {
        this.$chatEditor.focus()
        let nselection = getSelection(),
          nrange = nselection.getRangeAt(0)
        this.lastSelection = {
          range: nrange,
          selection: nselection,
        }
      }
      let addobj = {
        uid: this.contextmenu.data.uid,
        nick: this.contextmenu.data.remindnick,
      }
      this.rangeAdd(addobj)
    },
    /* 取消@ -@列表隐藏信息框重新定位光标位置*/
    cancleRemind() {
      this.remindshow = false
      $whiteMask.hide()
      let selection = this.lastSelection.selection
      let range = this.lastSelection.range
      let contentRange = range.cloneRange() //克隆选区
      contentRange.collapse(true) //移动光标位置到末尾
      selection.removeAllRanges() //移出所有选区
      selection.addRange(contentRange) //添加修改后的选区
    },
    /* 新增@人员 */
    addReMind(val) {
      this.remindshow = false
      this.$chatEditor.focus()
      let item = val.nick
      let { selection, range } = this.lastSelection
      let textNode = range.startContainer

      //删除页面中@符号
      range.setStart(textNode, range.endOffset - 1)
      range.setEnd(textNode, range.endOffset)
      range.deleteContents()
      this.rangeAdd(val)
    },
    /* 添加@内容及重新定义光标位置 */
    rangeAdd(item) {
      let { selection, range } = this.lastSelection
      //创建button存放@内容
      var button1 = document.createElement("button")
      button1.setAttribute("contenteditable", "false")
      button1.setAttribute("atuid", item.uid)
      button1.innerHTML = "@" + item.nick + "&nbsp"
      button1.className = "setatbutton"
      var frag = document.createDocumentFragment()
      let lastNode = frag.appendChild(button1)

      //填入内容并且重新设置焦点位置
      range.insertNode(frag)
      let contentRange = range.cloneRange() //克隆选区
      contentRange.setStartAfter(lastNode) //设置光标位置为插入内容的末尾
      contentRange.collapse(true) //移动光标位置到末尾
      selection.removeAllRanges() //移出所有选区
      selection.addRange(contentRange) //添加修改后的选区
    },
    /* 右键 */
    async chatContextMenu(e, v, type, is_right) {
      console.log(e, v, type);
      this.cmenutype = type
      /* 消息右键 */
      if (type == "msg") {
        console.log('chatOn', this.chatOn, v);
        v.chatlinkid = this.chatOn;
        //普通文本消息右键添加文本选中效果
        if (v.ct == 1) {
          let paragraphs = document.getElementById("copy" + v.mid)
          let range = new Range()
          range.setStartBefore(paragraphs)
          range.setEndAfter(paragraphs)
          let selection = window.getSelection()
          selection.removeAllRanges()
          selection.addRange(range)
        }

        if (this.isGroup) {
          log(v)
          let uid = v.uid
          let postdata1 = {
            uid: uid,
            groupid: this.bizId,
          }
          let groupInfo = await group.getWxGroupInfo(this.bizId, 1)
          v.grouprole = groupInfo.groupuser.grouprole //群聊角色
          v.msgBack = groupInfo.group.msgBack //群聊角色
        }
      }
      /* 消息头像右键 */
      if (type == "avatar") {
        log('msgmixin')
        if (!this.isGroup) {
          return
        }
        log(v)
        let uid = v.uid
        let postdata1 = {
          uid: uid,
          groupid: this.bizId,
        }
        this.isFriend = await friend.isMyFriend(uid)
        let forRes = await group.chatForbiddenFlag(postdata1)
        if (forRes.ok) {
          this.isforbidden = forRes.data
          v.forbiddenInfo = forRes.data
        }

        let groupInfo = await group.getWxGroupInfo(this.bizId, 1)
        let postdata = {
          uid: uid,
          groupid: this.bizId,
        }
        let res = await group.checkGroupUser(postdata) //是否在群聊中
        if (res.ok) {
          v.isInGroup = res.data
        }
        v.remindnick = v.name
        // if(v.selfGrouprole==''||v.selfGrouprole==undefined){
        //     v.selfGrouprole = v.grouprole // 群聊中个人角色
        // }
        v.selfGrouprole = groupInfo.groupuser.grouprole
        v.friendflag = groupInfo.group.friendflag
        // v.grouprole = groupInfo.groupuser.grouprole //群聊角色
        log(v)
      }
      let pos = setContextmenu(e, 130, 100)
      this.contextmenu = {
        top: pos.otop,
        left: pos.oleft,
        data: v,
        is_right
      }
      this.contextmenushow = true
      this.$setAddEventListener("contextmenushow")
    },
    /* 分享好友名片 */
    shareFriend() {
      document.documentElement.click()
      this.pcarshow = true
      this.$setAddEventListener("pcarshow")
    },
    stopProp() { },
    /* 分享群聊 */
    shareGroup() {
      document.documentElement.click()
      this.gcarshow = true
      this.$setAddEventListener("gcarshow")
    },
    /* 个人设置 */
    chatPersonSet(e) {
      if (!this.chatOn) return
      this.showUserCard(e, this.bizId)
    },
    /* 消息中-发送好友验证 */
    async sendApply() {
      document.documentElement.click()
      let userInfo = await this.getUserInfo(this.bizId)
      userInfo.avatar = resUrl(userInfo.avatar)
      this.userCard.data = userInfo
      this.$refs.usercard.msgApply()
    },
    /* 名片的点击事件 */
    cardClick(e, item) {
      let data = item.bodyData
      let cardtype = data.cardtype
      this.cardData = data
      //个人名片
      if (cardtype == 1) {
        // this.showCard(e);
        this.showUserCard(e, data.bizid)
      }
      //群聊名片
      if (cardtype == 2) {
        let postdata = {
          groupid: data.bizid,
          applyuid: data.shareFromUid,
          sendtime: data.sendtime,
        }
        group.checkCardJoinGroup(postdata).then(async (res) => {
          if (res.ok) {
            var joinflag = res.data
            if (joinflag == 1) {
              //已进群
              let pdata = { groupid: data.bizid }
              let result = await chatcom.chatActChat(postdata)
              if (result.ok) {
                let data = result.data.chat
                this.applyThis.$refs.chatlist.chatColClick(data)
              } else {
                msgTips(res.msg)
              }
            } else {
              //未进群
              this.joinshow = true
            }
          } else {
            msgTips(res.msg)
          }
        })
      }
    },
    /* 取消加入群聊 */
    cancleJoin() {
      this.joinshow = false
    },
    /* 确定加入群聊 */
    sureJoinGroup() {
      this.loading = true
      let postdata = {
        uids: this.curruid,
        groupid: this.cardData.bizid,
        applyuid: this.cardData.shareFromUid,
      }
      group.directInvite(postdata).then(async (res) => {
        this.loading = false
        if (res.ok) {
          this.joinshow = false
          let result = await chatcom.chatActChat(postdata)
          if (result.ok) {
            let data = result.data.chat
            this.applyThis.$refs.chatlist.chatColClick(data)
          }
          msgTips("进群成功")
        } else {
          msgTips(res.msg)
        }
      })
    },
  },
}
export default msgMixin
