<template>
  <div id="app">
    <!-- 单聊或@消息提示声音 -->
    <audio id="nofity_audio">
      <source src="@/assets/audio/person.mp3" type="audio/mp3" />
    </audio>
    <!-- 群聊提示音 -->
    <audio id="group_audio">
      <source src="@/assets/audio/group.mp3" type="audio/mp3" />
    </audio>
    <router-view />
  </div>
</template>

<script>
// import {initWs} from '@/js/ws/ws';//连接im
import {
  wscommand,
  wscommand_len,
  commandReverse,
} from "@/assets/js/ws/command.js";
import iconv from "iconv-lite";
// import { TextDecoder, TextEncoder} from "text-decoding";
// import * as wsBs from '@/js/ws/bs';

function info(msg) {
  console.log(msg);
}
let ws = null;
let heartHandle = {
  timeout: 7500,
  timeoutObj: null,
  serverTimeoutObj: null,

  start: function () {
    this.timeoutObj = setInterval(() => {
      let str = "00001";
      let uint8array = new TextEncoder().encode(str);
      ws.send(uint8array.buffer);
      console.log("ping6666");
    }, this.timeout);
    // this.timeoutObj = setTimeout(()=>{
    // 	let str = '00001';
    // 	let uint8array = new TextEncoder().encode(str);
    // 	ws.send(uint8array.buffer);
    // 	console.log('ping');
    // 	this.serverTimeoutObj = setTimeout(()=>{
    // 		uni.closeSocket();
    // 		console.log('closeSocket');
    // 	}, this.timeout)
    // }, this.timeout)
  },
  reset: function () {
    clearInterval(this.timeoutObj);
    // clearTimeout(this.serverTimeoutObj);
    return this;
  },
};
export default {
  data() {
    return {
      title: "im",
    };
  },
  created() {
    // this.onInitWs();
  },
  methods: {
    // 测试使用
    onInitWs() {
      var that = this;

      let _url =
        "wss://tlys.ybsrwl.cn:9325?wx=1&tio_session=1651505546399981568&frompath=%2Fhome";
      var wsObj = new WebSocket(_url);

      // var wsObj = uni.connectSocket({
      // 	url: _url,
      // 	success() {
      // 		// console.log('success');
      // 	}
      // 	// data() {
      // 	// 	return {
      // 	// 		x: '',
      // 	// 		y: ''
      // 	// 	};
      // 	// },
      // 	// header: {
      // 	// 	'content-type': 'application/json'
      // 	// },
      // 	// protocols: ['protocol1'],
      // 	// method: 'GET'
      // });

      // uni.onSocketOpen(function (res) {
      //   console.log('WebSocket open');
      //   heartHandle.reset().start();
      // });
      // // new Uint8Array(arrayBuffer, 2);

      // uni.onSocketMessage(function(res) {
      // 	console.log('onSocketMessage');
      // 	that.handleBuf(res);
      // 	// console.log(String.fromCharCode.apply(null, new Uint8Array(res.data, 2)));
      // });

      // uni.onSocketClose((resp)=>{
      // 	console.log('close', resp);
      // 	this.onInitWs();
      // })

      // uni.onSocketError(()=>{
      // 	console.log('error');
      // 	this.onInitWs();
      // })

      wsObj.onopen = function () {
        console.log("onopen");
        heartHandle.reset().start();
      };

      wsObj.onmessage = function (res) {
        console.log("onmessage=====res========",res);
        that.handleBuf(res);
      };

      wsObj.onclose = function () {
        console.log("close");
        that.onInitWs();
      };

      wsObj.onerror = function () {
        console.log("onerror");
        that.onInitWs();
      };

      ws = wsObj;

      // ws.binaryType = this.binaryType; // 'arraybuffer'; // 'blob' or 'arraybuffer';//arraybuffer是字节

      // ws.onopen = function(event) {
      // 	console.log('onopen', event);
      // 	return;
      // 	self.handler.onopen.call(self.handler, event, ws);
      // 	self.lastInteractionTime(new Date().getTime());
      // 	// clearInterval(intervalNum);

      // 	self.pingIntervalId = setInterval(function() {
      // 		self.ping(self);
      // 	}, self.heartbeatSendInterval);
      // 	intervalNum = self.pingIntervalId;
      // };
      // ws.onmessage = function(event) {
      // 	console.log('onmessage', event);
      // 	return;
      // 	self.handler.onmessage.call(self.handler, event, ws);
      // 	self.lastInteractionTime(new Date().getTime());
      // };
      // ws.onclose = function(event) {
      // 	console.log('onclose', event);
      // 	return;
      // 	log('websocket----onclose');
      // 	clearInterval(self.pingIntervalId); // clear send heartbeat task
      // 	store.commit("setIsConnect", false);
      // 	try {
      // 		self.handler.onclose.call(self.handler, event, ws);
      // 	} catch (error) {}
      // 	self.reconn(event);
      // };
      // ws.onerror = function(event) {
      // 	console.log('onerror', event);
      // 	return;
      // 	log('websocket----onerror');
      // 	self.handler.onerror.call(self.handler, event, ws);
      // };
      console.log("webSocket已初始化完成", ws);
    },
    ab2str(buf) {
      return new Uint8Array(buf);
      return String.fromCharCode.apply(null, new Uint8Array(buf));
    },

    handleBuf(event) {
      //  console.log("---------------///"+this.ab2str(event));

      var arrayBuffer = event.data;

      // log('receive data: ', arrayBuffer, ws)

      var uint8array = null;
      var firstbyte = new Uint8Array(arrayBuffer, 0, 2);
      // console.log(firstbyte);
      // var firstchar = iconv.decode(firstbyte, 'utf-8');

      var firstchar = new TextDecoder("utf-8").decode(firstbyte);
      var isZipped = false;
      var isZippedStr = "";
      if (firstchar.indexOf("x") != -1) {
        // 压缩过的
        isZipped = true;
        isZippedStr = "(zipped)";
        var zipedUint8array = new Uint8Array(arrayBuffer, 2);
        uint8array = pako.ungzip(zipedUint8array);
      } else {
        uint8array = new Uint8Array(arrayBuffer);
      }

      var data = new TextDecoder("utf-8").decode(uint8array);
      // var data = iconv.decode(uint8array, 'utf-8');
      // console.log(data);
       log('receive data' + isZippedStr + ': ' + data)

      if (!data || data.length < wscommand_len) {
        error(
          "data wrong" +
            isZippedStr +
            ", the data length must be >= " +
            wscommand_len,
          data
        );
        return;
      }

      var commandstr = data.substr(0, wscommand_len);

      var commandName = commandReverse[commandstr];
      // console.log(commandReverse, commandstr);
      if (!commandName) {
        info(
          "commandstr is " +
            commandstr +
            isZippedStr +
            ", but con not find commandName"
        );
        return;
      }

      // var bshandler = bs[commandName];
      // var bshandler = wsBs[commandName] //window[commandName];
      // if (!bshandler) {
      // 	info('can not found wx_handler, command is ' + commandName + isZippedStr);
      // 	return;
      // }

      var bodyStr = null;
      var bodyObj = null;
      if (data.length > wscommand_len) {
        bodyStr = data.substr(wscommand_len);
        console.log(["收到服务端消息", commandstr, JSON.parse(bodyStr)]);
        // console.log('received:' + commandName + isZippedStr + '\r\n, body string is :' + bodyStr);
        try {
          bodyObj = JSON.parse(bodyStr);
        } catch (err) {
          error(
            "can not parse to object, commandName is " +
              commandName +
              isZippedStr +
              ", body string is " +
              bodyStr
          );
          return;
        }
      }
    },
  },
};
</script>

<style>
#app {
  height: 100%;
  width: 100%;
}
</style>
