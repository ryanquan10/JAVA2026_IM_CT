/**评论列表 */
var commentHtml = "",
    count = 0,
    chatFlag = "",
    commentOldList = [],
    cmtid='',
    // 获取 类型
    totalPage = 1,
    totalRow = 0,
    isNext = true,//下拉加载更多
    pageSize = 16,
    praiseflag = '',//是否已点赞
    $window = $(window),
    lw = $window.width(),//浏览器宽度
    lh = $window.height(),//浏览器高度
    emojipopClass = '',//弹窗展示class名
    replyFlag = false,//是否回复;
    typeId='',
    infoId=getQueryStringOrLastSubPath("id"),
    commentParams={
      pageNumber:1
    },
    commentListUrl='',//评论列表 
    commentUrl='',
    praisecommentUrl='',
    delcommentUrl='',
    capacityTitle='',
    pathname=location.pathname,
    caseUrl = '/2/case/',
    blogUrl = '/2/blog/';
    if(pathname.indexOf(caseUrl)!=-1){//是否为案例详情页面
      commentParams.cid=infoId
      typeId='cid'
      commentListUrl='/tiocase/commentList',//评论列表 
      commentUrl='/tiocase/comment',
      praisecommentUrl='/tiocase/praisecomment',
      delcommentUrl='/tiocase/delcomment',
      capacityTitle='发布者'
    }else if(pathname.indexOf(blogUrl)!=-1){//是否为博客详情页面
      commentParams.bid=infoId
      typeId='bid'
      commentListUrl='/tioblog/commentList'//评论列表
      commentUrl='/tioblog/comment'
      praisecommentUrl='/tioblog/praisecomment'
      delcommentUrl='/tioblog/delcomment'
      capacityTitle='博主'
    }
    $(".comment-info").empty()
  /**
   * 评论列表
   */
  function getcommentList() {
    infoId=getQueryStringOrLastSubPath("id")
    if(typeId=='bid'){
      commentParams.bid=infoId
    }else{
      commentParams.cid=infoId
    }
    ajax.get(commentListUrl, {
      data: commentParams,
      success: function (res) {
        let data = res.data
        var titleHtml = ""
        titleHtml += `<span>${blogmeta.cmts||caseInfo.commments || 0}条评论</span>`
        commentHtml = ''
        if(res.data.list.length>0){
          if (pageNumber == 1) {
            $(".comment-info").empty()
              listenScroll();//监听滚动事件
            commentOldList = data.list
          } else {
            isNext = true;
            commentOldList = commentOldList.concat(data.list)
          }
          commentNewList = toTree(commentOldList)
          $.each(commentNewList, function (index, item) {
            commentHtml += appendhtml(item, index, 0, '', '')
            setTimeout(function () {
              $(".comment-avatar").error(function () {
                $(this).attr("src", "/2/imgs/case/avatar.jpg")
              })
              commentReply(item)
              //  子评论
              var childrenHtml = ''
              $.each(item.children, function (index1, item1) {// 一级item1
                childrenHtml += appendhtml(item1, index1, 1, item.nick, item.uid)
                var childrenHtml1 = ''
                $.each(item1.children, function (index2, item2) {//二级i
                  childrenHtml1 += appendhtml(item2, index2, 2, item1.nick, item1.uid)
                  var childrenHtml2 = ''
                  $.each(item2.children, function (index3, item3) {// 三级item3
                    childrenHtml2 += appendhtml(item3, index3, 3, item2.nick, item2.uid)
                    var childrenHtml3 = ''
                    $.each(item3.children, function (index4, item4) {// 四级item4
                      childrenHtml3 += appendhtml(item4, index4, 4, item3.nick, item3.uid)
                      setTimeout(function () {
                        $(".comment-avatar").error(function () {
                          $(this).attr("src", "/2/imgs/case/avatar.jpg")
                        })
                        commentReply(item4)
                        gomyhome()
                      }, 8)
                    });
                    setTimeout(function () {
                      $(`.children_${item3.id}`).html(childrenHtml3)
                      commentReply(item3)
                      gomyhome()
                    }, 7)
                  });
                  setTimeout(function () {
                    $(`.children_${item2.id}`).html(childrenHtml2)
                    commentReply(item2)
                    gomyhome()
                  }, 5)
                });
                setTimeout(function () {
                  $(`.children_${item1.id}`).html(childrenHtml1)
                  commentReply(item1)
                  gomyhome()
                }, 2)
              });
              $(`.children_${item.id}`).html(childrenHtml)
              gomyhome()
            }, 2)
          });
          $(".comment-info").html(commentHtml)
          gomyhome()
        }else{
          $('.comment-info').html('')
        }

        $(".comment-list-title").html(titleHtml)
        //当图片加载失败时，你要进行的操作
        totalPage = data.totalPage;
        totalRow = data.totalRow;
        pageSize = data.pageSize
      },
    })
  }
  /**修改评论数据结构 */
  function toTree(data) {
    let result = []
    if (!Array.isArray(data)) {
      return result
    }
    data.forEach((item) => {
      delete item.children
    })
    let map = {}
    data.forEach((item) => {
      map[item.id] = item
    })
    data.forEach((item) => {
      let parent = map[item.pid]
      if (parent) {
        ; (parent.children || (parent.children = [])).push(item)
      } else {
        result.push(item)
      }
    })
    return result
  }

  /**追加html
   * @param {*} data 数据 
   * @param {*} index 下标
   * @param {*} idx  下一层下标
   * @param {*} replyname 回复昵称
   * @param {*} uid 评论uid
   */
  function appendhtml(data, index, idx, replyname, uid) {
    var i = 0, usertips = '', usertipsclass = "comment-user-flag-none"
    if (data.children) {
      i = 0
    } else {
      i = 4
    }
    if (caseInfo.uid === data.uid && curruid != data.uid) {
      usertips = capacityTitle
      usertipsclass = ''
    } else {
      usertips = ''
      usertipsclass = "comment-user-flag-none"
    }

    var imgclass = curruid === data.uid ? '' : 'icon-me-none'
    var replynameclass = (replyname == undefined || replyname == '') ? "replyname-none" : 'replyname-flex'
    var replayiconMeClass = uid === curruid ? '' : 'icon-me-none'
    var replayusertipsclass = (uid === caseInfo.uid && curruid != uid) ? '' : 'comment-user-flag-none'
    var replayusertips = (uid === caseInfo.uid && curruid != uid) ? capacityTitle : ''
    return `
    <div class="">
        <div class="comment-item_${data.id} comment-item comment-item_i${idx}">
          <div class="flex-column">
            <img class="comment-avatar gomyhome cursor" data-uid="${data.uid}" src="${res_url(data.avatar)}" >
            <div class="comment-hr comment-hr_${i}" ></div>
          </div>
          <div class="comment-userinfo">
              <div class="comment-nick ">
                <div class="comment-nick-left">
                  <span class="comment-nick_${data.id} gomyhome cursor" data-uid="${data.uid}">${data.nick}</span>
                  <p class="icon-me ${imgclass}">我</p>
                  <span class="comment-user-flag ${usertipsclass}">${usertips}</span>
                  <p class="${replynameclass}">
                    <span style="color:#999999;margin:0 4px">回复</span>${replyname}
                    <span class="comment-user-flag ${replayusertipsclass}">${replayusertips}</span>
                    <p  class="icon-me ${replayiconMeClass}">我</p>
                  </p>
                  <p class="comment-time"> ${btDate(data.createtime)} </p>
                </div>
                <div class=" ${data.STATUS == 1 ? 'comment-footer-right' : 'disNone'} ">
                      <p class="flex_center comment-delete_${data.id}  comment-delete cursor ${imgclass}">
                          <img class="comment-icon comment-deleteIcon_${data.id}" src="/2/imgs/case/icon-delete.png">
                          删除
                      </p>
                      <p class="flex_center cursor comment-chat_${data.id} comment-chat_${idx} " id="replay_${data.id}" data-last="reply_${data.id}">
                          <img class="comment-icon comment-chatIcon_${data.id}" src="/2/imgs/case/icon-chat.png" >
                          回复
                      </p>
                      <p class="flex_center cursor " id="commfabulous_${data.id}" >
                          <img class="comment-icon icon-commfabulous" src="${data.praiseflag==1?'/2/imgs/case/icon-fabulous-select.png':'/2/imgs/case/icon-fabulous.png'}" >
                          <span>${btNum(data.praises)||0}</span>
                      </p>
                </div>
              </div>
             <p class="comment-html_${index} comment-html ${data.STATUS == 1 ? '' : 'colorCCC'}">${data.STATUS == 1 ? data.html : '评论已删除'}</p> 
              <div class="comment-reply_${data.id} comment-reply">
                 <div style="display:flex">
                  <div class="reply-left-box">
                    <div class="reply-emojibox_${data.id}"></div>
                    <img id="reply-emojiBtn_${data.id}" class="reply-emojiBtn" src="/2/imgs/case/reply-emoji.png">
                    <div contenteditable="true" class="reply-input reply-input_${data.id} reply-input-class${idx}" placeholder="回复：${data.nick}"></div>
                  </div>
                  <button class="reply-button_${data.id
      } reply-button">回复</button>
                 </div>
              </div>
              <div class="comment-footer comment-footer_${data.id} ${data.children && idx == 0 ? 'border-bottom-none comment-footer_i0' : ''}"></div>
              <!-- 子评论  -->
              <div class="children_${data.id} children_reply children_reply_i${idx}">
            
              </div>
          </div>
        </div>
     
    </div>
    `
  }
  /**追加子评论 */
  function commentReply(data) {
    /**评论icon鼠标移动样式 */
    $(`.comment-delete_${data.id}`).hover(
      function (e) {
        $(e.target).addClass("comment-delete-select")
        $(`.comment-deleteIcon_${data.id}`).attr(
          "src",
          "/2/imgs/case/icon-delete-select.png"
        )
      },
      function (e) {
        $(e.target).removeClass("comment-delete-select")
        $(`.comment-deleteIcon_${data.id}`).attr(
          "src",
          "/2/imgs/case/icon-delete.png"
        )
      }
    )
    $(`.comment-deleteIcon_${data.id}`).hover(
      function (e) {
        $(`.comment-delete_${data.id}`).addClass("comment-delete-select")
      },
      function (e) {
        $(`.comment-delete_${data.id}`).removeClass("comment-delete-select")
      }
    )
    $(`.comment-chat_${data.id}`).hover(
      function (e) {
        $(e.target).addClass("comment-chat-select")
        $(`.comment-chatIcon_${data.id}`).attr(
          "src",
          "/2/imgs/case/icon-chat-select.png"
        )
      },
      function (e) {
        $(e.target).removeClass("comment-chat-select")
        $(`.comment-chatIcon_${data.id}`).attr(
          "src",
          "/2/imgs/case/icon-chat.png"
        )
      }
    )
    $(`.comment-chatIcon_${data.id}`).hover(
      function (e) {
        $(`.comment-chat_${data.id}`).addClass("comment-chat-select")
      },
      function (e) {
        $(`.comment-chat_${data.id}`).removeClass("comment-chat-select")
      }
    )
    // 回复显示弹窗
    $(`#replay_${data.id}`).click(function (e) {
      if(!curruid){
        log('未登录')
        toLoginPage()//跳转登录页面
        return
      }

      $(".comment-reply").hide()
      $('.reply-input').empty()
      htm = ''
      let lastValue = e.currentTarget.dataset.last
      count = count + 1
      if (chatFlag === lastValue) {
        //同一个
        if (count % 2 == 0) {
          log('同一个-隐藏')
          $(".comment-reply").hide()
          $(`.comment-footer_${data.id}`).removeClass("border-bottom-none")
        } else {
          log('同一个-显示')
          $(`.comment-reply_${data.id}`).show()
          $(`.comment-footer_${data.id}`).addClass("border-bottom-none")
        }
      } else {
        //不同
        log('不同')
        count = 1
        $(`.comment-reply_${data.id}`).show()
        $(`.comment-footer_${data.id}`).addClass("border-bottom-none")

        initEmoji('.reply-input_' + data.id, '.reply-emojibox_' + data.id, 'reply-emoji_container' + data.id, '.reply-button_' + data.id)
        ////////////////表情
      }
      chatFlag = lastValue
    })
    $(`#reply-emojiBtn_${data.id}`).click(function () {
      toggleEmoji();
      $(`#reply-emojiBtn_${data.id}`).attr('src','/2/imgs/quill/icon-emjclick.svg')
    })
    function toggleEmoji() {
      $(`.reply-emoji_container${data.id}`).slideToggle();
    }
    $(`.reply-button_${data.id}`).click(function () {
      var replyhtml = $(`.reply-input_${data.id}`).html()
      if (replyhtml == '') return
      pageNumber = 1
      commentOldList = []
      commentHtml = ""
      $(".comment-info").empty()
      replyFlag = true
      let data1 = {
        pid: data.id,
        html: replyhtml
      }
      commentParams.pageNumber =1
      if(typeId=='bid'){
        data1.bid=infoId
      }else{
        data1.cid=infoId
      }
      ajax.post(commentUrl, {
        data:data1,
        success: function (res) {
          if (res.ok) {
            layer.msg("评论成功")
            $(`.reply-input_${data.id}`).val("")
          } else {
            layer.msg('评论失败')
          }
          commentParams.pageNumber = 1;
          getcommentList()
          window.scrollTo(100, 0)
        },
      })
    })
    /**回复评论控制样式 */
    $(`.reply-input_${data.id}`).on("input", function () {
      htm = this.innerHTML
      if (htm.length != 0 && htm != "<br>") {
        $(`.comment-reply .reply-button_${data.id}`).addClass(
          "reply-button-select"
        )
      } else {
        $(`.comment-reply .reply-button_${data.id}`).removeClass(
          "reply-button-select"
        )
      }
    })
    /**删除评论 */
    $(`.comment-delete_${data.id}`).click(function () {
      $('.conformDelete').show()
      $('.conform-mask').show()
      cmtid = data.id
    })
    /**评论点赞 */
    $(`#commfabulous_${data.id}`).click(function () {
      let oper = data.praiseflag==1?2:1
      let data1={
        oper
      }
      if(typeId=='bid'){
        data1.bcid=data.id
      }else{
        data1.ccid=data.id
      }
      ajax.post(praisecommentUrl, {
        data: data1,
        success: function (res) {
          if(res.ok){
            data.praiseflag = oper
            data.praises = oper==1?data.praises+1:data.praises-1
            $(`#commfabulous_${data.id} span`).html(data.praises)
            let icon = oper==1?'/2/imgs/case/icon-fabulous-select.png':'/2/imgs/case/icon-fabulous.png'
            $(`#commfabulous_${data.id} img`)[0].src = icon
            layer.msg(oper == 1 ? '点赞成功' : '取消点赞')
          }
        },
      })
    })
    
  }
  /**
   * 删除评论
   */
  $('.cureDelete').click(function () {
    delcomment(cmtid)
  })
  $('.cancel').click(function () {
    $('.conformDelete').hide()
    $('.conform-mask').hide()
  })
  function delcomment(cmtid) {
    let data1={}
    if(typeId=='bid'){
      data1.cmtid=cmtid
    }else{
      data1.commentid=cmtid
    }
    ajax.post(delcommentUrl, {
      data: data1,
      success: function (res) {
        if (res.ok) {
          layer.msg("删除评论成功")
          commentOldList = []
          commentHtml = ""
          $(".comment-info").empty()
          pageNumber = 1
          commentParams.pageNumber =1
          getcommentList()
          chatFlag = ""
          window.scrollTo(100, 0)
          $('.conformDelete').hide()
          $('.conform-mask').hide()
        }
      },
    })
  }
  /* 监听滚动事件 */
  function listenScroll() {
    // if(typeId=='bid'){
    //   $('.info-content-main').scroll(function () {
    //       var scrollTop = $('.info-content-main').scrollTop();
    //       var scrollHeight = $('.comment-list').height();
    //       lh = $('.info-content-main').height()//浏览器高度
    //       //上一页数据加载完成&&总页数大于当前页码&&滚动到底部
    //       if (isNext && totalPage > pageNumber && scrollTop + lh + 50 > scrollHeight) {
    //         pageNumber++;
    //         isNext = false;
    //         console.log(pageNumber)
    //         commentParams.pageNumber = pageNumber;
    //         getcommentList();
    //       }
    //   })
    // }else{
        $window.scroll(function () {
          var scrollTop = $window.scrollTop();
          var scrollHeight = $(document).height();
          //上一页数据加载完成&&总页数大于当前页码&&滚动到底部
          if (isNext && totalPage > pageNumber && scrollTop + lh + 50 > scrollHeight) {
            pageNumber++;
            isNext = false;
            console.log(pageNumber)
            commentParams.pageNumber = pageNumber;
            getcommentList();
          }
      })
    // }
  }

  if(typeId=='cid'){
    submitComment()
  }

  /**
   * 提交评论
   */
  function submitComment(){
      $(".publishBtn").click(function () {
        console.log(htm)
        if (htm === '') return
        let data1 ={ pid: "",html: htm}
        if(typeId=='bid'){
          data1.bid=infoId
        }else{
          data1.cid=infoId
        }
        ajax.post(commentUrl, {
          data:data1,
          success: function (res) {
            if (res.ok) {
              layer.msg("评论成功")
              $("#comment-textarea").empty()
              pageNumber = 1
              commentParams.pageNumber = 1;
              getcommentList();
              htm = ''
            } else {
              layer.msg("暂不能发表评论")
            }
      
            $(".comment-btns .publishBtn").removeClass("publishBtnAvailable")
          },
        })
      })
  }
    function gomyhome(){
      $(".gomyhome").click(function(e){
        let uid = e.currentTarget.dataset.uid
        if(uid){
          window.location.href = '/2/my/index.html?uid='+encodeURIComponent(uid);
        }
      })
    }