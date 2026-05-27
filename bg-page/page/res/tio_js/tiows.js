if (typeof tio == "undefined") {
  tio = {};
}

/**
 * @param {*} param 作为tio.ws对象的参数，由业务自己使用，框架不使用
 * @param {*} handler
 * @param {*} heartbeatTimeout 心跳时间 单位：毫秒
 * @param {*} reconnInterval 重连间隔时间 单位：毫秒
 * @param {*} binaryType 'blob' or 'arraybuffer';//arraybuffer是字节
 */
tio.ws = function (
  urlcreator,
  param,
  handler,
  heartbeatTimeout,
  reconnInterval,
  binaryType
) {
  this.binaryType = binaryType || "arraybuffer";
  this.urlcreator = urlcreator;
  // console.error(urlcreator);
  // this.reconnUrl += 'tiows_reconnect=true';
  this.param = param;

  this.handler = handler;
  this.heartbeatTimeout = heartbeatTimeout;
  this.reconnInterval = reconnInterval;
  this.stopped = false;

  this.lastInteractionTime = function () {
    if (arguments.length == 1) {
      this.lastInteractionTimeValue = arguments[0];
    }
    return this.lastInteractionTimeValue;
  };

  this.heartbeatSendInterval = heartbeatTimeout / 2;
  var firstThis = this;

  this.connect = function (isReconnect) {
    var self = this;

    // var _url;
    // try {
    //   _url = self.urlcreator.call(self.urlcreator, isReconnect);
    // } catch (e) {
    //   error(e);
    // }



    var _url;
    try {
      var imServerMeta = self.urlcreator.call(self.urlcreator, isReconnect);
      if (imServerMeta.url) {
        _url = imServerMeta.url;
        if (imServerMeta.timeout) {
          firstThis.heartbeatTimeout = imServerMeta.timeout;
          firstThis.heartbeatSendInterval = firstThis.heartbeatTimeout / 2;
        }
      } else {
        _url = imServerMeta;
      }
    } catch (e) {
      console.error(e);
    }

    // self.handler.onmessage.call(self.handler, event, ws);
    console.warn("获取到的url:" + _url);
    if (!_url) {
      console.warn("准备重连:" + _url);

      var self = this;
      setTimeout(function () {
        var ws = self.connect(false);
        self.ws = ws;
      }, self.reconnInterval);
      return;
    }

    var ws = new WebSocket(_url);
    this.ws = ws;
    ws.binaryType = this.binaryType; // 'arraybuffer'; // 'blob' or 'arraybuffer';//arraybuffer是字节

    ws.onopen = function (event) {
      self.handler.onopen.call(self.handler, event, ws);
      self.lastInteractionTime(new Date().getTime());

      self.pingIntervalId = setInterval(function () {
        self.ping(self);
      }, self.heartbeatSendInterval);
    };
    ws.onmessage = function (event) {
      self.handler.onmessage.call(self.handler, event, ws);
      self.lastInteractionTime(new Date().getTime());
    };
    ws.onclose = function (event) {
      clearInterval(self.pingIntervalId); // clear send heartbeat task
      try {
        self.handler.onclose.call(self.handler, event, ws);
      } catch (error) { }


      self.reconn(event);
    };
    ws.onerror = function (event) {
      self.handler.onerror.call(self.handler, event, ws);
    };

    return ws;
  };

  this.reconn = function (event) {
    var self = this;
    if (!self.stopped) {
      setTimeout(function () {
        if (!self.stopped) {
          var ws = self.connect(true);
          self.ws = ws;
        }
      }, self.reconnInterval);
    }


  };

  this.ping = function () {
    if (!this.stopped) {
      var iv = new Date().getTime() - this.lastInteractionTime(); // 已经多久没发消息了
      // 单位：秒
      if (this.heartbeatSendInterval + iv >= this.heartbeatTimeout) {
        this.handler.ping(this.ws);
      }
    }

  };

  this.send = function (data) {
    this.ws.send(data);
  };

  this.stop = function () {
    this.stopped = true;
    this.ws.close();
  };
};
