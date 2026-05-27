  /**
   * 添加拷贝代码动作
   */
  function addListenerForCode() {
    /**复制代码 */
    $('#editor-container').on('mouseenter', '.hljs', function (e) {
      let timestamp = new Date().getTime()
      let preid = $(e.currentTarget).attr('id')
      $(e.currentTarget).append(`<button class="copyBtn" tio-copy-btn='1' id='cory_${timestamp}' data-clipboard-action="copy" data-clipboard-target="#${preid} table">
    <img class="icon-copy" src="/2/imgs/quill/icon-copy.png"/>
    <span>复制代码</span></button>`)
    })

    $('#editor-container').on('mouseleave', '.hljs', function (e) {
      let perId = e.currentTarget.id
      var container = document.getElementById(perId);
      if (container && $(container.lastElementChild).attr('tio-copy-btn') == '1') {
        container.removeChild(container.lastElementChild);
      }
    })

    //mousemove只执行一次就可以了，所以用one
    $('#editor-container').one('mousemove', '.hljs', function (e) {
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