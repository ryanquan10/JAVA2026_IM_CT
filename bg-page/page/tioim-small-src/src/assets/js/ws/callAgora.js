import AgoraRTC from '@/assets/js/plugin/AgoraRTC_N-4.21.0.js'
import { agora } from '@/axios/path';
import store from '../../../store';
import wsSend from '@/assets/js/ws/send';//发送消息方法
import { wscommand } from '@/assets/js/ws/command';//消息码


// import {callSetSomeValue} from '@/assets/js/call'
class AgoraWebRTC {
    appid // appid
    channel // 频道
    uid // uid
    touid // 订阅谁、uid
    client  // RTC实例
    proxyMode = "0";  // 连接类型
    token   // 当前连接token
    type // 通话类型语音还是视频
    remoteUsers = {} // 远程user列表，有人加入了这个频道就会有一个user
    localTracks = {
        audioTrack: null, // 音频
        videoTrack: null,  // 视频
    };
    // 视频参数
    currentMic = null
    currentCam = null
    mics = []
    cams = []
    constructor(options) {
        this.appid = store.state.Call.agoraConfig.agora_app_id;
        this.channel = options.channel;
        this.uid = options.uid;
        this.touid = options.touid;
        this.type = options.type;
    }
    async init() {
        this.client = AgoraRTC.createClient({
            mode: "rtc",
            codec: "vp8"
        });
        this.token = await agora.getToken({
            channelName: this.channel
        }).then((resp) => {
            return resp?.data?.token || null
        })
        await this.join()
    }
    // 发布
    async publish() {
        await this.init();
        await this.createTrackAndPublish()
        await this.initDevices()
        // agora content inspect start  
        // agoraContentInspect(localTracks.videoTrack)
        // agora content inspect end ;
    }
    // 订阅
    // 释放媒体资源
    async leave() {
        let localTracks = this.localTracks;
        for (let trackName in localTracks) {
            var track = localTracks[trackName];
            if (track) {
                track.stop();
                track.close();
                localTracks[trackName] = undefined;
            }
        }
        await this.client.leave();
    }
    async join() {
        let that = this;
        // 监听user订阅
        this.client.on("user-published", async (user, mediaType) => {
            const id = user.uid;
            that.remoteUsers[id] = user;
            // 当目标user加入频道后订阅他
            if (true) {
                let remoteUser = that.remoteUsers[that.touid]
                if (remoteUser) {
                    await that.client.subscribe(remoteUser, mediaType);
                    if (mediaType === 'audio') {
                        remoteUser.audioTrack.play();
                    }
                    if (mediaType === "video") {
                        // if ($(`#player-${uid}`).length) {
                        //   return
                        // }
                        // const player = $(`
                        //  <div id="player-wrapper-${uid}">
                        //         <div id="player-${uid}" class="player">
                        //              <div class="player-name">uid: ${uid}</div>
                        //         </div>
                        //  </div>
                        // `);
                        // $("#remote-playerlist").append(player);
                        
                        remoteUser.videoTrack.play('wxCallRemoteVideo');
                        // remoteUser.videoTrack.play(`player-${uid}`);
                      }
                    let isFormUser = that.uid == wxCallMeta.fromuid;
                    if(isFormUser){
                        let req = { 
                            id: wxCallMeta.id,
                            sdp: {type: 'answer', sdp: 'agora' }
                            //    "sdp":{"type":"offer","sdp":"v=0\r\no=- 8988257017992107781 2 IN IP4 127.0.0.1\r\ns=-\r\nt=0 0\r\na=group:BUNDLE 0\r\na=extmap-allow-mixed\r\na=msid-semantic: WMS 4b5f62aa-4d10-4274-b025-220ff62839e8\r\nm=audio 9 UDP/TLS/RTP/SAVPF 111 63 9 0 8 13 110 126\r\nc=IN IP4 0.0.0.0\r\na=rtcp:9 IN IP4 0.0.0.0\r\na=ice-ufrag:2OrW\r\na=ice-pwd:WD6K9hNgovWjQAbLp2Xecabn\r\na=ice-options:trickle\r\na=fingerprint:sha-256 C6:AD:62:D0:FF:52:C4:BB:49:77:49:90:E9:45:7D:93:D0:B5:CB:5B:C2:CB:AE:79:66:9D:3D:98:C8:ED:12:C2\r\na=setup:actpass\r\na=mid:0\r\na=extmap:1 urn:ietf:params:rtp-hdrext:ssrc-audio-level\r\na=extmap:2 http://www.webrtc.org/experiments/rtp-hdrext/abs-send-time\r\na=extmap:3 http://www.ietf.org/id/draft-holmer-rmcat-transport-wide-cc-extensions-01\r\na=extmap:4 urn:ietf:params:rtp-hdrext:sdes:mid\r\na=sendrecv\r\na=msid:4b5f62aa-4d10-4274-b025-220ff62839e8 06485903-2882-4554-9913-798f2876e292\r\na=rtcp-mux\r\na=rtcp-rsize\r\na=rtpmap:111 opus/48000/2\r\na=rtcp-fb:111 transport-cc\r\na=fmtp:111 minptime=10;useinbandfec=1\r\na=rtpmap:63 red/48000/2\r\na=fmtp:63 111/111\r\na=rtpmap:9 G722/8000\r\na=rtpmap:0 PCMU/8000\r\na=rtpmap:8 PCMA/8000\r\na=rtpmap:13 CN/8000\r\na=rtpmap:110 telephone-event/48000\r\na=rtpmap:126 telephone-event/8000\r\na=ssrc:1152656327 cname:s4mZCa65H7rSuF34\r\na=ssrc:1152656327 msid:4b5f62aa-4d10-4274-b025-220ff62839e8 06485903-2882-4554-9913-798f2876e292\r\n"}
                        };
                        wsSend(wscommand.WxCall05OfferSdpReq, req);
                        console.log(`agora uid:${that.uid}订阅成功，订阅目标touid：${that.touid}`);
                    }
                }

            }
        });
        this.client.on("user-unpublished", this.handleUserUnpublished);
        // start Proxy if needed
        const mode = Number(this.proxyMode)
        if (mode != 0 && !isNaN(mode)) {
            this.client.startProxyServer(mode);
        }
        this.uid = await this.client.join(this.appid, this.channel, this.token || null, this.uid || null)

    }
    // 监听user退订
    handleUserUnpublished(user, mediaType) {
        if (mediaType === 'audio') {
            const id = user.uid;
            delete remoteUsers[id];
        }
    }
    // 创建发布
    async createTrackAndPublish() {
        console.log('callAgora 准备发布');
        let localTracks = this.localTracks;
        let type = this.type;
        localTracks.audioTrack = await AgoraRTC.createMicrophoneAudioTrack({
            encoderConfig: "music_standard"
        })
        
        if (type == 2) {
            localTracks.videoTrack = await AgoraRTC.createCameraVideoTrack();
            // play local video track
            localTracks.videoTrack.play("wxCallLocalVideo", {
                mirror: $("#mirror-check").prop("checked")
            });
        }
        // publish local tracks to channel
        let ls = {};
        for(let prop in localTracks){
            if(localTracks[prop]){
                ls[prop] = localTracks[prop]
            }
        }
        await this.client.publish(Object.values(ls));
    }
    // 初始化设备 
    async initDevices() {
        let type = this.type;
        let mics = await AgoraRTC.getMicrophones();
        if (type == 2) {
            // $(".mic-list").empty();
            // mics.forEach(mic => {
            //   const value = mic.label.split(" ").join("")
            //   $(".mic-list").append(`<option value=${value}>${mic.label}</option>`);
            // });

            // const audioTrackLabel = localTracks.audioTrack.getTrackLabel();
            // currentMic = mics.find(item => item.label === audioTrackLabel);
            // $(".mic-list").val(audioTrackLabel.split(" ").join(""));

            // get cameras
            this.cams = await AgoraRTC.getCameras();
            // $(".cam-list").empty();
            // cams.forEach(cam => {
            //   const value = cam.label.split(" ").join("")
            //   $(".cam-list").append(`<option value=${value}>${cam.label}</option>`);
            // });

            // const videoTrackLabel = localTracks.videoTrack.getTrackLabel();
            // currentCam = cams.find(item => item.label === videoTrackLabel);
            // $(".cam-list").val(videoTrackLabel.split(" ").join(""));
        }

        // $(".mic-list").empty();
        // mics.forEach(mic => {
        //     const value = mic.label.split(" ").join("")
        //     $(".mic-list").append(`<option value=${value}>${mic.label}</option>`);
        // });

        // const audioTrackLabel = localTracks.audioTrack.getTrackLabel();
        // currentMic = mics.find(item => item.label === audioTrackLabel);
        // $(".mic-list").val(currentMic.label.split(" ").join(""));
    }

}

export {
    AgoraWebRTC
} 