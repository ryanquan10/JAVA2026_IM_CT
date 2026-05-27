
let pathname=location.pathname,
    uploadImgUrl = '';
if(pathname.indexOf("/2/case/") != -1){
  uploadImgUrl = '/tiocase/uploadImg'
}else if(pathname.indexOf("/2/blog/") != -1){
  uploadImgUrl = '/tioblog/uploadImg'
}

var quill = new Quill('#editor-container', {
  modules: {
    formula: true,
    syntax: true,
    toolbar: '#toolbar-container'
  },
  placeholder: '',
  theme: 'snow'
});
// 预览
var previewquill = new Quill('#preview-editor', {
  modules: {
    formula: true,
    syntax: true,
  },
  readOnly:true,
  theme: 'snow'
});
setTimeout(function(){
  $('.custom-toolbar').show()
},3)

$(function () {

  $('#bindEmoji').click(function(){
    count = 0
    toggleEmoji();
   })
		function toggleEmoji(){
      $('.emoji_container_1').slideToggle();
      $('.editor-box-mask').show()
		}
    //初始化表情
		function initEmoji(editObj,emojibox,emojicontainer,replybtn){
			$(emojibox).append(`<div class='${emojicontainer} emoji_container'><ul class='emoji_wrap'></ul></div>`);
      emojData.emojiList.forEach(res=>{
        $('.emoji_wrap').append("<li class='emoji_li'><a data-emoji_code='#em1_5#' data-index='4' title=''><img src='/res/emoji/emoji/"+res.url+"' class='emoji_item'></a></li>");
      })
			//居中处理
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
			$('.emoji_item').on('click',function(){
        console.log($(this))
        let range = quill.getSelection(true);
        quill.insertEmbed(range.index, 'image', $(this).attr('src'), Quill.sources.USER)
        quill.setSelection(range.index + 1);
        quill.formatText(range.index,1,{"alt":"emoji","width":24 });  //https://www.xcwmoon.com/post/100 //可设置 alt width height 

      });
      // 表情弹窗一直保存，点击其他地方关闭表情弹窗
    }
    //初始化界面，传入接受表情的div编辑框
    initEmoji('.comment-textarea','.emojibox','emoji_container_1','.publishBtn')
    /**预览 */
    var customButton = document.querySelector('.preview');
    customButton.addEventListener('click', function() {
      console.log(quill.root)
      var quillContent = quill.root.innerHTML,
      length = previewquill.getLength();
      previewquill.deleteText(0, length);
    // previewquill.clipboard.dangerouslyPasteHTML(0, quillContent);    //这个很耗性能，不要随便用
      previewquill.root.innerHTML = quillContent;
      $('#preview-editor .hljs').each(function (i, block) {
        try {
          hljs.highlightBlock(block);
        } catch (error) {
          log(error);
        }
      });
      var html = $('.preview-modelbody').html();
      layer.open({
          title: ' ', 
          type: 1,
          closeBtn: 1,
          area: ["970px", "742px"],
          content: html,
          btn: null
      });
    });

    
    /**重写图片上传 */
    $('.ql-custom-imgbtn').click(function(){
      $('.quill-uploadImgBox').show()
      $('.uploadImgBox-header p').removeClass('uploadImgBox-header-select')
      $('.default1-select').addClass('uploadImgBox-header-select')
      $(`.pictures_1`).addClass('display_none')
      $(`.pictures_0`).removeClass('display_none')
      $('.picturesUrl').val('')
      $('.uploader-file').val('')

    })
    $('.uploadImgBox-header p').click(function(e){
      $('.uploadImgBox-header p').removeClass('uploadImgBox-header-select')
      $(this).addClass('uploadImgBox-header-select')
      let idx = e.currentTarget.dataset.idx,ix = idx == 1 ? 0 : 1
      $(`.pictures_${ix}`).addClass('display_none')
      $(`.pictures_${idx}`).removeClass('display_none')
    })
      /**自定义图片上传 */
    document.getElementById('upload').addEventListener('change', function (event) {
        var $file = event.currentTarget;
        var formData = new FormData();
        var file = $file.files;
        formData = new FormData();
        formData.append('uploadFile', file[0]);

        ajax.post(uploadImgUrl, {
					data: formData,
					contentType: false, // 注意这里应设为false
					processData: false,
          timeout: 500000,
          showload:1,
					success: function (res) {
						if (res.ok) {
             //你的图片上传成功后的返回值...所以格式由你来定!
              let timestamp=new Date().getTime(),
              url=res_url(res.data.coverurl),
              id = `picturesUrl_${timestamp}`,
              className='picturesImg'
              insertImg(url,id,className,'image')
              $('.quill-uploadImgBox').hide()
						} else {
							layer.alert(res.msg);
						}
					}
        });
    });
    /**网络图片插入 */
    $('.picturesUrlBtn').click(function(){
      let timestamp=new Date().getTime(),
          url=$('.picturesUrl').val(),
          id = `picturesUrl_${timestamp}`,
          className='picturesImg'
      insertImg(url,id,className,'image')
      $('.quill-uploadImgBox').hide()
    })
    function insertImg(url,id,classname,insertType,width,height){
      let range = quill.getSelection(true)
      quill.insertEmbed(range.index, insertType, url, Quill.sources.USER)
      quill.setSelection(range.index + 1),width1=width||'',height1=height||''
      
      quill.formatText(range.index,1,{"alt":'','id':`${id}`,'class':`${classname}`,'width':width1,'height':height1})
      console.log(insertType)
      if(insertType=='image'){
        bindResize($(`#${id}`));
      }
    }
    /**
     * 富文本编辑器监听黏贴
     */
  quill.clipboard.addMatcher(Node.ELEMENT_NODE, (node, delta) => {
    delta.ops = delta.ops.map(op => {
      if (op.insert.image) {
        let timestamp = new Date().getTime(),
          id = `picturesUrl_${timestamp}`;
        if (!op.attributes) {
          op.attributes = {};
        }
        //调用
        
        if(op.insert.image.indexOf('data:image/')==0){
          var blob = dataURLtoBlob(op.insert.image);
          var file = blobToFile(blob, 'imgName');
          formData = new FormData();
          formData.append('uploadFile', file);
          ajax.post(uploadImgUrl, {
            data: formData,
            contentType: false, // 注意这里应设为false
            processData: false,
            timeout: 500000,
            showload:1,
            async:false,
            success: function (res) {
              if (res.ok) {
                op.insert.image=res_url(res.data.coverurl)
              } else {
                layer.alert(res.msg);
              }
            }
          });
        }
        op.attributes["class"] = 'picturesImg';
        op.attributes.id = id;
        setTimeout(function () {
          bindResize($(`#${id}`));
        }, 15)
      }
      log(op)
      return op;
    })
    log(delta)
    return delta
  })


   function dataURLtoBlob(dataurl) { 
    var  arr = dataurl.split(',')
    var  mime = arr[0].match(/:(.*?);/)[1]
    var  bstr = atob(arr[1])
    var  n = bstr.length
    var  u8arr = new Uint8Array(n)
    while (n--) {
      u8arr[n] = bstr.charCodeAt(n);
    }
    return new Blob([u8arr], { type: mime });
}
//将blob转换为file
 function blobToFile(theBlob, fileName){
    theBlob.lastModifiedDate = new Date();
    theBlob.name = fileName;
    return theBlob;
}

    /**重写视频 */
    $('.ql-custom-videobtn').click(function(){
      $('.quill-uploadVideo').show()
      $('.uploadVideo-header p').removeClass('uploadVideo-header-select')
      $('.default-select').addClass('uploadVideo-header-select')
      $(`.video_1`).addClass('display_none')
      $(`.video_0`).removeClass('display_none')
      $('.videoUrl').val('')
      $('.uploader-file').val('')
    })
    $('.uploadVideo-header p').click(function(e){
      $('.uploadVideo-header p').removeClass('uploadVideo-header-select')
      $(this).addClass('uploadVideo-header-select')
      let idx = e.currentTarget.dataset.idx,ix = idx == 1 ? 0 : 1
      $(`.video_${ix}`).addClass('display_none')
      $(`.video_${idx}`).removeClass('display_none')
    })
    /**网络视频插入 */
    $('.videoUrlBtn').click(function(){
      let timestamp=new Date().getTime(),
          url=$('.videoUrl').val(),
          id = `videoUrl_${timestamp}`,
          className='video',
          width='100%'
          insertImg(url,id,className,'video',width)
      $('.quill-uploadVideo').hide()
    })
    document.getElementById('upload-video').addEventListener('change', function (event) {
      var $file = event.currentTarget;
      var formData = new FormData();
      var file = $file.files;
      formData = new FormData();
      formData.append('uploadFile', file[0]);
      ajax.post("/tiocase/video", {
        data: formData,
        contentType: false, // 注意这里应设为false
        processData: false,
        timeout: 500000,
        success: function (res) {
          if (res.ok) {
            let timestamp=new Date().getTime(),
            url=res_url(res.data.url),
            id = `videoUrl_${timestamp}`,
            className='video',
            width='100%'
            insertImg(url,id,className,'video',width)
            $('.quill-uploadVideo').hide()
          } else {
            layer.alert(res.msg);
          }
        }
      });
    });
    /**关闭上传弹窗 */
    $('.cencel-upload').click(function(){
      $('.quill-uploadImgBox').hide()
      $('.quill-uploadVideo').hide()
    })
    $('.picturesCancel').click(function(){
      $('.quill-uploadImgBox').hide()
      $('.quill-uploadVideo').hide()
    })

        /**点击除弹窗外的div隐藏弹窗 */
        window.onclick = function (e) {
          if($(e.target).attr('class')==undefined){
            $('.'+emojipopClass).fadeOut();
            $('.emoji_container_1').fadeOut();
            $('.editor-box-mask').hide()
            $('.imgtool').hide()
            $('.screenPopup').hide()
            return
          }
          let moreClassFlag = $(e.target).attr('class').indexOf("emoji_li") != -1
          let screenIconClassFlag = $(e.target).attr('class').indexOf("emoji_item") != -1
          let bindEmojiImg = $(e.target).attr('class').indexOf("bindEmojiImg") != -1
          let replyemojiBtn = $(e.target).attr('class').indexOf("reply-emojiBtn") != -1
          let picturesImg = $(e.target).attr('class').indexOf("picturesImg") != -1
          
          let imgWidthinput = $(e.target).attr('class').indexOf("imgWidthinput") != -1
          let imgHeightinput = $(e.target).attr('class').indexOf("imgHeightinput") != -1
          let screenstatus = $(e.target).attr('class').indexOf("screen-status") != -1,
          currentscreen = $(e.target).attr('class').indexOf("current-screen") != -1,
          iconttubiao_xiala = $(e.target).attr('class').indexOf("iconttubiao_xiala") != -1,
          screenArricon = $(e.target).attr('class').indexOf("screenArr-icon") != -1,
          popupCancel = $(e.target).attr('class').indexOf("popupCancel") != -1,
          editPopupSubmit = $(e.target).attr('class').indexOf("addEditPopup-submit") != -1,
          tipsSubmit = $(e.target).attr('class').indexOf("tips-submit") != -1,
          layuiClose = $(e.target).attr('class').indexOf("layui-layer-setwin") != -1,
          layerclose = $(e.target).attr('class').indexOf("layui-layer-close") != -1,
          categoryinput = $(e.target).attr('class').indexOf("category-input") != -1
          if (!screenstatus&&!currentscreen&&!iconttubiao_xiala&&!screenArricon&&!popupCancel&&!editPopupSubmit&&!tipsSubmit&&!layuiClose&&!layerclose&&!categoryinput  ) {
            $('.screenPopup').hide()
        }
            if(!picturesImg&&!imgHeightinput&&!imgWidthinput){
              $('.imgtool').hide()
            }
          if(!moreClassFlag&&!screenIconClassFlag&&!bindEmojiImg&&!replyemojiBtn){
            $('.'+emojipopClass).fadeOut();
            $('.emoji_container_1').fadeOut();
            $('.editor-box-mask').hide()
          }
      }
      /**
       * 图片宽高input事件
       */
      $('input[name="imgWidth"]').on('input',function(e){
        e.target.value = e.target.value>838?838:e.target.value
        let imgWidth = e.target.value
        console.log(imgtoolId)
        let imgStyle = imgtoolId.style
        imgStyle.width = imgWidth+'px'
      });
      $('input[name="imgHeight"]').on('input',function(e){
        let imgHeight = e.target.value
        let imgStyle = imgtoolId.style
        imgStyle.height = imgHeight+'px'
      });
})
//绑定需要拖拽改变大小的元素对象
function bindResize(el){
  //初始化参数
  // var els = el.style,
  el=el[0]
  var els = el.style,
  //鼠标的 X 和 Y 轴坐标
  x = y = 0;
  var img_url = el.src
  // // 创建对象
  var img = new Image();
  // // 改变图片的src
  img.src = img_url;
  $(el).mousedown(function (e){
      var e = e||window.event;
      //获得当前点击位置距离浏览器顶部的距离
      var  top = e.pageY-e.offsetY-45;
      //获得当前点击位置距离浏览器左侧的距离
      var left = e.pageX-e.offsetX-81+(el.width/2);
      $('.imgtool').show()
      let imgWidth = el.width
      let imgHeight = el.height
      $('input[name="imgWidth"]').val(imgWidth)
      $('input[name="imgHeight"]').val(imgHeight)
      imgtoolId = el
      //按下元素后，计算当前鼠标与对象计算后的坐标
      x = e.clientX - el.offsetWidth,
      y = e.clientY - el.offsetHeight;
      imgtoolLT(left,top)

      //在支持 setCapture 做些东东
      el.setCapture ? (
      //捕捉焦点
          el.setCapture(),
      //设置事件
          el.onmousemove = function (ev)
          {
              mouseMove(ev || event);
          },
          el.onmouseup = mouseUp
      ) : (
          //绑定事件
          $(document).bind("mousemove", mouseMove).bind("mouseup", mouseUp)
      );
      //防止默认事件发生
      e.preventDefault();
  });
  //移动事件
  function mouseMove(e)
  {
      //宇宙超级无敌运算中...
      els.width = e.clientX - x + 'px'
      // els.height = e.clientY - y + 'px';
      el.width = e.clientX - x
      els.height = 'auto'
      els.width = e.clientX - x>838?'838':el.width 
      $('input[name="imgWidth"]').val(el.width)
      $('input[name="imgHeight"]').val(el.height)
  }
  //停止事件
  function mouseUp(){
      //在支持 releaseCapture 做些东东
      el.releaseCapture ? (
      //释放焦点
          el.releaseCapture(),
      //移除事件
          el.onmousemove = el.onmouseup = null
      ) : (
          //卸载事件
          $(document).unbind("mousemove", mouseMove).unbind("mouseup", mouseUp)
          
      );
  }
}
function imgtoolLT(x,y){
$('.imgtool').css('top',y)
$('.imgtool').css('left',x)
}

  /**
   * 添加拷贝代码动作
   */
  function addListenerForCode() {
    /**复制代码 */
    $('#editor-container').on('mouseenter', 'pre', function (e) {
      let timestamp = new Date().getTime()
      let preid = $(e.currentTarget).attr('id')
log(e.currentTarget)
      $(e.currentTarget).append(`<button class="copyBtn" tio-copy-btn='1' id='cory_${timestamp}' data-clipboard-action="copy" data-clipboard-target="#${preid} table">
    <img class="icon-copy" src="/2/imgs/quill/icon-copy.png"/>
    <span>复制代码</span></button>`)
    })

    $('#editor-container').on('mouseleave', 'pre', function (e) {
      let perId = e.currentTarget.id
      var container = document.getElementById(perId);
      if (container && $(container.lastElementChild).attr('tio-copy-btn') == '1') {
        container.removeChild(container.lastElementChild);
      }
    })

    //mousemove只执行一次就可以了，所以用one
    $('#editor-container').one('mousemove', 'pre', function (e) {
      let perId = e.currentTarget.id
      var container = document.getElementById(perId);
      if (container && $(container.lastElementChild).attr('tio-copy-btn') != '1') {
        $(container).trigger("mouseenter");
      }
    })

    var clipboard = new ClipboardJS('.copyBtn');
    clipboard.on('success', function (e) {
      console.log(e);
      layer.msg('复制成功!', {
        time: 2000, //2s后自动关闭
      });
    });
    clipboard.on('error', function (e) {
      console.log(e);
    })
  }