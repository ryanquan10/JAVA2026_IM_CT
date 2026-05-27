
    if (curruser) {
      $('.notLogin').hide()
      $('.comment-left').show()
      $(".comment-box #curravatar").attr("src", res_url(curruser.avatar))
    } else {
      $('.notLogin').show()
      $('.comment-left').hide()
      $('.comment-box #curravatar').attr("src", "/2/imgs/case/icon-user.png")
    }
    $(".comment-box #curravatar").error(function () {
      $(this).attr("src", "/2/imgs/case/avatar.jpg")
    })
    layui.flow.lazyimg();//图片懒加载

    //初始化界面，传入接受表情的div编辑框
    initEmoji('#comment-textarea', '.emojibox', 'emoji_container_1', '.publishBtn')
    /**重置按钮 */
    $(".resetBtn").click(function () {
        $("#comment-textarea").empty()
        // 解决重置其内容后无法光标正确定位
        var _div = document.querySelector("#comment-textarea")
        var range = document.createRange()
        range.selectNodeContents(_div)
        range.collapse(false)
        var sel = window.getSelection()
        sel.removeAllRanges()
        sel.addRange(range)
    })
    $("#comment-textarea").on("focus", function () {
      htm = this.innerHTML
      if ((htm.length != 0 && htm != "<br>")) {
        $(".comment-btns .publishBtn").addClass("publishBtnAvailable")
      } else {
        $(".comment-btns .publishBtn").removeClass("publishBtnAvailable")
      }
    })
    $("#comment-textarea").on("input", function () {
      htm = this.innerHTML
      count = 0
      $('.comment-reply').hide()
      $('.reply-input').empty()
      if (htm.length != 0 && htm != "<br>") {
        $(".comment-btns .publishBtn").addClass("publishBtnAvailable")
      } else {
        $(".comment-btns .publishBtn").removeClass("publishBtnAvailable")
      }
    })
    /**点击除弹窗外的div隐藏弹窗 */
    window.onclick = function (e) {
      if ($(e.target).attr('class') == undefined) {
        $('.' + emojipopClass).fadeOut();
        $('.emoji_container_1').fadeOut();
        $('.editor-box-mask').hide()
        return
      }
      let moreClassFlag = $(e.target).attr('class').indexOf("emoji_li") != -1
      let screenIconClassFlag = $(e.target).attr('class').indexOf("emoji_item") != -1
      let bindEmojiImg = $(e.target).attr('class').indexOf("bindEmojiImg") != -1
      let replyemojiBtn = $(e.target).attr('class').indexOf("reply-emojiBtn") != -1
      if (!moreClassFlag && !screenIconClassFlag && !bindEmojiImg && !replyemojiBtn) {
        $('.' + emojipopClass).fadeOut();
        $('.emoji_container_1').fadeOut();
        $('.editor-box-mask').hide()
        $('#bindEmoji').attr('src','/2/imgs/case/icon-emoji.png')
        $('.reply-emojiBtn').attr('src','/2/imgs/case/icon-emoji.png')
      }
    }
    //初始化表情
    function initEmoji(editObj, emojibox, emojicontainer, replybtn) {
      log('9999999')
      var emojiList = emojData.emojiList
      $(emojibox).append(`<div class='${emojicontainer} emoji_container'><ul class='emoji_wrap'></ul></div>`);
      emojiList.forEach(res => {
        $('.emoji_wrap').append("<li class='emoji_li'><a data-emoji_code='#em1_5#' data-index='4' title=''><img src='/res/emoji/emoji/" + res.url + "' class='emoji_item'></a></li>");
      })
      //居中处理
      var obj = $(`.${emojicontainer}`);
      var docHei = $(window).height();
      var docWid = $(window).width();
      var hei = $(obj).height();
      var wid = $(obj).width();
      //用nicesroll插件美化div滚动条的效果	
      $(`.${emojicontainer}`).niceScroll({
        cursorcolor: "#ccc",//#CC0071 光标颜色
        cursoropacitymax: 1, //改变不透明度非常光标处于活动状态（scrollabar“可见”状态），范围从1到0
        touchbehavior: false, //使光标拖动滚动像在台式电脑触摸设备
        cursorwidth: "5px", //像素光标的宽度
        cursorborder: "0", // 游标边框css定义
        cursorborderradius: "5px",//以像素为光标边界半径
        autohidemode: true //是否隐藏滚动条
      });
      emojipopClass = emojicontainer
      //选择表情后，关闭，这里实现的不太友好，有兴趣可以实现点击外部，div关掉
      $('.emoji_item').on('click', function () {
        console.log(editObj)
        $(editObj).append("<img  class='emoji_item' src='" + $(this).attr('src') + "'>");
        $(replybtn).addClass("publishBtnAvailable")
        htm = $('#comment-textarea').html()
        $(replybtn).addClass(
          "reply-button-select"
        )
      });
      // 表情弹窗一直保存，点击其他地方关闭表情弹窗
    }
      $('#bindEmoji').hover(
          function (e) {
            log('wer')
            $(this).attr('src','/2/imgs/quill/icon-emjclick.svg')
          },
          function (e) {
      //       
          }
        )
      
    //定义emoji层的现实和关闭，这里用来jqeury的动画
    $('#bindEmoji').click(function () {
      count = 0
      $('.comment-reply').hide()
      $('.reply-input').empty()
      $('#bindEmoji').attr('src','/2/imgs/quill/icon-emjclick.svg')
      toggleEmoji();
    })
    function toggleEmoji() {
      $('.emoji_container_1').slideToggle();
      $('.editor-box-mask').show()
    }