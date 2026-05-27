<template>
  <div class="maincontent">
    <!-- 头部 -->
    <div class="chatinfo">
      <span class="chat-name" v-if="isGroup">{{
        chatinfo.name + "(" + chatinfo.joinnum + ")"
      }}</span>
      <div class="chat-name" style="line-height: 1.1em" v-else>
        <div class="_row mg-tm">
          <span>{{ chatinfo.name }}</span>
<!--          <p-->
<!--            class="size-n c-black mg-l1"-->
<!--            v-if="switchConfig.isOpenAddressForWeb.value && chatinfo.ip"-->
<!--          >-->
<!--            {{ chatinfo.city + "：" + chatinfo.ip }}-->
<!--          </p>-->
        </div>
        <div
          v-if="chatinfo && friendOnlineInfo"
          class="online row_c size-ms"
          :class="{ on: friendOnlineInfo.online }"
        >
          {{
            friendOnlineInfo.online
              ? "在线"
              : "离线 " + friendOnlineInfo.lastLoginTime
          }}
        </div>
      </div>

      <div class="info-opera">
        <!-- 举报icon -->
        <span
          class="cursor"
          v-show="(isGroup && chatinfo.linkflag == 1) || !isGroup"
          @click="
            (reportShow = true),
              isGroup ? (reportmsg = '群聊') : (reportmsg = '用户')
          "
        ></span>
        <!-- 群聊头部右侧操作图标 -->
        <i
          class="iconfont iconttubiao_point"
          v-show="isGroup && chatinfo.linkflag == 1"
          @click.stop="chatGroupSet"
        ></i>
        <!-- 私聊头部右侧操作图标 -->
        <i
          class="iconfont iconttubiao_point"
          v-show="!isGroup"
          @click.stop="chatPersonSet"
        ></i>
      </div>
    </div>
    <!-- 消息列表 -->
    <div class="msgcontainer" id="msgcontainer">
      <ul>
        <div
          v-if="
            isGroup &&
            storeGroupInfo &&
            storeGroupInfo.noticeRoll == 1 &&
            storeGroupInfo.notice
          "
          @click="showNotice"
        >
          <div class="notice_bar_null"></div>
          <div class="notice_bar">
            <notice-bar
              :list="[{ content: storeGroupInfo ? storeGroupInfo.notice : '' }]"
            />
          </div>
        </div>
        <!-- 不是删除消息且满足单通道显示 否则隐藏-->
        <div
          v-show="nextPage && !lastPage"
          class="msg-col msg-center-col msg-moretop"
          @click="getNextMsg"
        >
          查看更多消息
        </div>
        <!-- item.type == 9001 ||  自定义类型9001用于判断时间显示类型，仿微信 -->
        <div class="_row" :class="{'bg-major-light': manySelectMids[item.mid]}" v-for="item in MessageList"
            :key="item.mid"
            > 
          <div v-if="isManySelect && [1,2].includes(Number(item.type)) && ![4,9,10,11,20].includes(Number(item.ct))" class="many-icon row-c-c" @click="setManySelectMids(item.mid,item)">
            <i v-show="manySelectMids[item.mid]" class="li-icon-select_round_fill c-major"></i>
            <i v-show="!manySelectMids[item.mid]" class="li-icon-select_round c-gray-light"></i>
          </div>
          <li
            class="auto"
            :class="[
              !item.delwhere && item.singleshow
                ? item.type == 1 && item.ct !== 12
                  ? 'msg-col msg-right-col'
                  : item.type == 2 && item.ct !== 12
                  ? 'msg-col msg-left-col'
                  : item.type == 3 || item.type == 4 || item.ct == 12
                  ? 'msg-col msg-center-col'
                  : 'msg-hide'
                : 'msg-hide',
            ]"
            :mid="item.mid"
          >
            <!-- 如果为系统消息，且满足单通道显示-->
            <!-- <div v-if="item.type == 3 && item.singleshow && curruid != bizId"> -->
            <!-- <div v-if="item.type == 2 && item.singleshow && item.ct==13">
              {{item.mid}}
              {{item.name}}想邀请5好友加入群聊，<span class="cursor">去确认</span>
          </div> -->
            <!-- {{item.apply}} -->
            <div v-if="showTimeType == 2">
              <p> {{ getShowTime(item.time) }} </p>
            </div>
            <div v-if="item.type == 3 && item.singleshow && item.ct !== 13">
              <p>{{ getShowTime(item.t) }}</p>
              <p v-html="item.html"></p>

            </div>
            <div v-if="item.type == 3 && item.ct == 13">
              <p>{{ getShowTime(item.t) }}</p>

              <p>
                {{ item.name }}想邀请好友加入群聊，<span
                  class="cursor examine"
                  @click="goExamine(item)"
                  >{{ item.apply.status == 1 ? "已确认" : "去确认" }}</span
                >
              </p>
            </div>
            <!-- 系统消息的红包提示 -->

            <div
              v-if="
                (item.type == 1 &&
                  item.ct == 12 &&
                  item.singleshow &&
                  item.touid == curruid) ||
                (item.type == 2 && item.ct == 12 && item.singleshow)
              "
            >
              <p>{{ getShowTime(item.t) }}</p>
              <p>收到红包，请在手机端查看</p>
            </div>
            <div
              v-if="
                item.type == 1 &&
                item.ct == 12 &&
                item.singleshow &&
                item.touid != curruid
              "
            >
              <p>{{ getShowTime(item.t) }}</p>
              <p>发出红包，请在手机端查看</p>
            </div>
            <!-- 系统消息的非好友验证消息 -->
            <div v-if="item.type == 4">
              <p class="systime">{{ getShowTime(item.t) }}</p>
              <span class="sysmsg"
                >{{ item.html
                }}<span class="sendapply" @click="sendApply"
                  >发送好友验证</span
                ></span
              >
            </div>
            <!-- 判断当前是自己发送的消息并且消息类型不为12（红包类型）&&当前是好友发送的消息并且消息类型不为12（红包类型） -->
            <template
              v-if="
                ((item.type == 1 && item.ct !== 12 && item.ct !== 13) ||
                  (item.type == 2 && item.ct !== 12 && item.ct !== 13)) &&
                item.singleshow &&
                !item.delwhere
              "
            >
              <el-image
                class="msg-avatar"
                :src="item.avatar"
                @contextmenu.prevent="
                  chatContextMenu(
                    $event,
                    item,
                    'avatar',
                    item.type == 1 && item.ct !== 12
                  )
                "
                @click.stop="showCard($event, item.uid, item)"
              >
                <div slot="error" class="image-slot">
                  <img
                    src="~@/assets/imgs/common/avatar.jpg"
                    class="error-img"
                    @contextmenu.prevent="
                      chatContextMenu(
                        $event,
                        item,
                        'avatar',
                        item.type == 1 && item.ct !== 12
                      )
                    "
                    @click.stop="showCard($event, item.uid, item)"
                  />
                </div>
              </el-image>
              <div
                class="msg-right"
                @contextmenu.prevent="
                  chatContextMenu(
                    $event,
                    item,
                    'msg',
                    item.type == 1 && item.ct !== 12
                  )
                "
              >
                <p class="msg-top">
                  <!--  <i v-if="item.touid!=curruid" :class="item.readflag==1?'wxc_read':'wxc_notread'">{{item.readflag==1?'已读':(item.readflag==2?'未读':'')}}</i> -->
                  
                  <span class="msg-nick">{{
                    myRemarks[item.f] ? myRemarks[item.f] : item.nick
                  }}</span>
                  <span class="msg-time">{{ getShowTime(item.t) }}</span>
                  <span v-if="switchConfig.groupShowRole.value" class="msg-label" :class="['label' + item.grouprole]">{{item.label}}</span>
                </p>
                <div
                  :class="[
                    'msg-bot',
                    item.ct == 5 ||
                    item.ct == 6 ||
                    item.ct == 9 ||
                    item.ct == 20
                      ? 'msg-bot-nbg'
                      : item.ct == 88
                      ? 'msg-bot-nbg not-max-width'
                      : '',
                  ]"
                >
                  <i
                    v-if="item.touid != curruid"
                    :class="[
                      'msgreadsta',
                      item.readflag == 1 ? 'wxc_read' : 'wxc_notread',
                    ]"
                  >
                    <template v-if="switchConfig.isReadOpen.value">
                      {{
                        item.readflag == 1
                          ? "已读"
                          : item.readflag == 2
                          ? "未读"
                          : ""
                      }}
                    </template>
                  </i>
                  <!--  1、普通文本消息，2、超链接卡片消息，3、文件，4、音频，5、视频 ,6.图片 ,9.名片 ,10.视频通话, 11.音频通话, 12.红包 20.笔记-->
                  <div
                    v-if="item.ct == 1"
                    :id="'copy' + item.mid"
                    v-html="item.html"
                  ></div>

                  <!-- <div
                    v-if="item.ct == 77"
                    :id="item.mid"
                    v-html="item.c"
                  ></div> -->
                  <div v-if="item.ct == 77" :id="item.mid" class="message">
                    <!-- <div class="message-time">{{ JSON.parse(item.c).msg  }}
                      <p>聊天记录</p>
                    </div> -->
                    <div class="message-card">
    <div class="message-header">{{ JSON.parse(item.c).msg  }}
      <div class="message-body">
        <a style="cursor: pointer;color: #01588c;" @click="viewMessageRecord(item.c)">聊天记录</a>
      </div>
      <!-- Add a space between the two elements -->
    </div>

</div>
                  </div>
          
                  <div v-else-if="item.ct == 3" class="file-flex">
                    <div>
                      <div class="filebg" v-html="strRep(item.html)"></div>
                      <div class="fileSize">
                        {{ formatFileSize(item.fc.size) }}
                      </div>
                    </div>
                    <img
                      v-if="item.fc.ext == 'docx'"
                      src="~@/assets/imgs/msglist/world.png"
                      alt=""
                    />
                    <img
                      v-else-if="
                        item.fc.ext == 'jpg' ||
                        item.fc.ext == 'png' ||
                        item.fc.ext == 'gif'
                      "
                      src="~@/assets/imgs/msglist/jpg.png"
                      alt=""
                    />
                    <img
                      v-else-if="item.fc.ext == 'xls' || item.fc.ext == 'xlsx'"
                      src="~@/assets/imgs/msglist/xls.png"
                      alt=""
                    />
                    <img
                      v-else-if="item.fc.ext == 'mp4'"
                      src="~@/assets/imgs/msglist/mp4.png"
                      alt=""
                    />
                    <img
                      v-else-if="item.fc.ext == 'mp3'"
                      src="~@/assets/imgs/msglist/mp3.png"
                      alt=""
                    />
                    <img
                      v-else-if="item.fc.ext == 'pdf'"
                      src="~@/assets/imgs/msglist/pdf.png"
                      alt=""
                    />
                    <img
                      v-else-if="item.fc.ext == 'ppt' || item.fc.ext == 'pptx'"
                      src="~@/assets/imgs/msglist/ppt.png"
                      alt=""
                    />
                    <img
                      v-else-if="item.fc.ext == 'zip'"
                      src="~@/assets/imgs/msglist/zip.png"
                      alt=""
                    />
                    <img
                      v-else-if="item.fc.ext == 'apk'"
                      src="~@/assets/imgs/msglist/apk.png"
                      alt=""
                    />
                    <img
                      v-else-if="item.fc.ext == 'txt'"
                      src="~@/assets/imgs/msglist/txt.png"
                      alt=""
                    />
                    <img
                      v-else
                      src="~@/assets/imgs/msglist/notRecogn.png"
                      alt=""
                    />
                  </div>

                  <div
                    v-else-if="item.ct == 4"
                    class="audiomsg"
                    @click="playAudio(item)"
                    :style="{ width: item.bodyData.width + 'px' }"
                  >
                    <span>{{ item.bodyData.seconds }}″</span>
                    <span v-html="item.html"></span>
                    <img
                      src="~@/assets/imgs/home/ownvoice_stop.png"
                      v-show="item.type == 1 && !item.bodyData.play"
                    />
                    <img
                      src="~@/assets/imgs/home/voice_stop.png"
                      v-show="item.type == 2 && !item.bodyData.play"
                    />
                    <img
                      src="~@/assets/imgs/home/ownvoice.gif"
                      v-show="item.type == 1 && item.bodyData.play"
                    />
                    <img
                      src="~@/assets/imgs/home/voice.gif"
                      v-show="item.type == 2 && item.bodyData.play"
                    />
                  </div>
                  <!-- 视频 -->
                  <div
                    v-else-if="item.ct == 5"
                    class="videocol"
                    @click="videoClick(item.bodyData)"
                  >
                    <el-image
                      :src="item.bodyData.vcoverurl"
                      fit="cover"
                      class="el-image"
                      :style="{
                        width: item.bodyData.sWidth,
                        height: item.bodyData.sHeight,
                      }"
                    >
                      <div slot="error" class="image-slot">
                        <img
                          src="~@/assets/imgs/common/avatar.jpg"
                          class="error-img"
                        />
                      </div>
                    </el-image>
                  </div>
                  <!-- 图片 -->
                  <div
                    v-else-if="item.ct == 6"
                    class="imgcol cursor"
                    :id="'copy' + item.mid"
                  >
                    <img
                      :src="item.bodyData.coverurl"
                      fit="cover"
                      :id="'copyImg' + item.mid"
                      class="el-image"
                      @click="imgClick(item.mid)"
                      :style="{
                        width: item.bodyData.showWidth,
                        height: item.bodyData.showHeight,
                      }"
                    />
                    <!-- <el-image :src="item.bodyData.coverurl" fit="cover" class="el-image" @click="imgClick(item.mid)" :style="{
                      width: item.bodyData.showWidth,
                      height: item.bodyData.showHeight,
                    }">
                  </el-image> -->
                  </div>
                  <!-- 名片 -->
                  <div
                    v-else-if="item.ct == 9"
                    class="cardbg"
                    @click.stop="cardClick($event, item)"
                  >
                    <div class="cardtop">
                      <el-image
                        :src="item.bodyData.bizavatar"
                        fit="cover"
                        class="el-image"
                      >
                        <div slot="error" class="image-slot">
                          <img
                            src="~@/assets/imgs/common/avatar.jpg"
                            class="error-img"
                          />
                        </div>
                      </el-image>
                      <span class="cardname">{{ item.bodyData.bizname }}</span>
                    </div>
                    <div class="cardbot">
                      <img
                        src="~@/assets/imgs/home/scard.png"
                        v-show="item.bodyData.cardtype == 1"
                      />
                      <img
                        src="~@/assets/imgs/home/gcard.png"
                        v-show="item.bodyData.cardtype == 2"
                      />
                      {{ item.bodyData.cardtype == 2 ? "群名片" : "个人名片" }}
                    </div>
                  </div>
                  <!-- 音视频通话 10:视频通话；11:音频通话-->
                  <div
                      @click="handleCallBack(item.ct)"
                    v-else-if="item.ct == 10 || item.ct == 11"
                    class="callcol"
                  >
                    <img
                      src="~@/assets/imgs/home/video.png"
                      v-if="item.ct == 10"
                      :class="[
                        'call-icon',
                        item.type == 1 ? 'right-icon rotate' : '',
                      ]"
                    />
                    <img
                      src="~@/assets/imgs/home/autio.png"
                      v-if="item.ct == 11 && item.type == 2"
                      class="call-icon"
                    />
                    <img
                      src="~@/assets/imgs/home/autiort.png"
                      v-if="item.ct == 11 && item.type == 1"
                      class="call-icon right-icon"
                    />
                    {{ item.bodyData.reason }}
                  </div>
                  <!-- 名片 -->
                  <div
                    v-else-if="item.ct == 20"
                    class="cardbg"
                    @click.stop="cardClick($event, item)"
                  >
                    <div class="cardtop">
                      <el-image
                        :src="item.bodyData.imgUrl"
                        fit="cover"
                        class="el-image"
                      >
                        <div slot="error" class="image-slot">
                          <img
                            src="~@/assets/imgs/common/avatar.jpg"
                            class="error-img"
                          />
                        </div>
                      </el-image>
                      <span class="cardname">分享了他的云笔记</span>
                    </div>
                    <div class="cardbot">
                      <img src="~@/assets/imgs/home/scard.png" />
                      <!-- <img
                      src="~@/assets/imgs/home/gcard.png"
                      v-show="item.bodyData.cardtype == 2"
                    /> -->
                      云笔记
                    </div>
                  </div>
                  <!-- 链接消息 -->
                  <div v-else-if="item.ct == 88">
                    <div
                      class="hyperlinks cursor"
                      @click="openNews(item.temp.url)"
                    >
                      <div class="hyperlinks-title">{{ item.temp.title }}</div>
                      <div class="hyperlinks-content">
                        <span class="cardname">{{ item.temp.subtitle }}</span>
                        <el-image
                          :src="item.temp.img"
                          fit="cover"
                          class="el-image"
                        >
                          <div slot="error" class="image-slot">
                            <img
                              src="~@/assets/imgs/msglist/link_default_img.png"
                              class="error-img"
                            />
                          </div>
                        </el-image>
                      </div>
                    </div>
                  </div>
                  <!-- <div v-else-if="item.ct == 66">
                  <div v-html="item.html"></div>
                </div> -->
                </div>
                <div
                  v-if="item.isQuote"
                  class="msg-quote"
                  @click="quotePreview(item)"
                  @contextmenu.stop.prevent="quoteContextmenu(item.quotemid)"
                >
                  <div class="msg-quote-main _row wrap">
                    <span class="msg-quote-nick">{{ item.quotesrcnick }}:</span>
                    <div
                      v-if="item.quotemsgtype == 1 || item.quotemsgtype == 2"
                      class="msg-quote-text many-t"
                      v-html="item.quotemsgcontent"
                    ></div>
                    <div v-else-if="item.quotemsgtype == 3" class="_row mg-l1">
                      <span>{{ strRep(item.quotemsgcontent.filename) }}</span>
                      <img
                        class="msg-quote-img mg-l1"
                        v-if="item.quotemsgcontent.ext == 'docx'"
                        src="~@/assets/imgs/msglist/world.png"
                        alt=""
                      />
                      <img
                        class="msg-quote-img mg-l1"
                        v-else-if="
                          item.quotemsgcontent.ext == 'jpg' ||
                          item.quotemsgcontent.ext == 'png' ||
                          item.quotemsgcontent.ext == 'gif'
                        "
                        src="~@/assets/imgs/msglist/jpg.png"
                        alt=""
                      />
                      <img
                        class="msg-quote-img mg-l1"
                        v-else-if="
                          item.quotemsgcontent.ext == 'xls' ||
                          item.quotemsgcontent.ext == 'xlsx'
                        "
                        src="~@/assets/imgs/msglist/xls.png"
                        alt=""
                      />
                      <img
                        class="msg-quote-img mg-l1"
                        v-else-if="item.quotemsgcontent.ext == 'mp4'"
                        src="~@/assets/imgs/msglist/mp4.png"
                        alt=""
                      />
                      <img
                        class="msg-quote-img mg-l1"
                        v-else-if="item.quotemsgcontent.ext == 'mp3'"
                        src="~@/assets/imgs/msglist/mp3.png"
                        alt=""
                      />
                      <img
                        class="msg-quote-img mg-l1"
                        v-else-if="item.quotemsgcontent.ext == 'pdf'"
                        src="~@/assets/imgs/msglist/pdf.png"
                        alt=""
                      />
                      <img
                        class="msg-quote-img mg-l1"
                        v-else-if="
                          item.quotemsgcontent.ext == 'ppt' ||
                          item.quotemsgcontent.ext == 'pptx'
                        "
                        src="~@/assets/imgs/msglist/ppt.png"
                        alt=""
                      />
                      <img
                        class="msg-quote-img mg-l1"
                        v-else-if="item.quotemsgcontent.ext == 'zip'"
                        src="~@/assets/imgs/msglist/zip.png"
                        alt=""
                      />
                      <img
                        class="msg-quote-img mg-l1"
                        v-else-if="item.quotemsgcontent.ext == 'apk'"
                        src="~@/assets/imgs/msglist/apk.png"
                        alt=""
                      />
                      <img
                        class="msg-quote-img mg-l1"
                        v-else-if="item.quotemsgcontent.ext == 'txt'"
                        src="~@/assets/imgs/msglist/txt.png"
                        alt=""
                      />
                      <img
                        class="msg-quote-img mg-l1"
                        v-else
                        src="~@/assets/imgs/msglist/notRecogn.png"
                        alt=""
                      />
                    </div>
                    <div v-else-if="item.quotemsgtype == 4" class="row_c">
                      <i class="li-icon-yuyin"></i>
                      <span>{{ item.quotemsgcontent.seconds }}″</span>
                    </div>
                    <div v-else-if="item.quotemsgtype == 5" class="mg-l1">
                      <div class="msg-quote-video row-c-c">
                        <img
                          class="msg-quote-img"
                          :src="resUrl(item.quotemsgcontent.coverurl)"
                          :alt="item.quotemsgcontent.filename"
                        />
                        <i class="icon-play li-icon-play"></i>
                      </div>
                    </div>
                    <div v-else-if="item.quotemsgtype == 6" class="">
                      <img
                        class="msg-quote-img mg-l1"
                        :src="resUrl(item.quotemsgcontent.coverurl)"
                        :alt="item.quotemsgcontent.filename"
                      />
                    </div>
                    <div v-else-if="item.quotemsgtype == 9" class="_row">
                      <i
                        v-if="item.quotemsgcontent.cardtype == 2"
                        class="li-icon-group-card icon_card size-t"
                      ></i>
                      <i
                        v-else
                        class="li-icon-person-card icon_card size-b"
                      ></i>
                      <span>{{
                        item.quotemsgcontent.bizname +
                        "的" +
                        (item.quotemsgcontent.cardtype == 1 ? "个人" : "群") +
                        "名片"
                      }}</span>
                    </div>
                    <div v-else-if="item.quotemsgtype == 20" class="_row">
                      <i class="li-icon-note"></i>
                      <span>云笔记</span>
                      <img
                        class="msg-quote-img mg-l1"
                        v-if="item.quotemsgcontent.imgUrl"
                        :src="item.quotemsgcontent.imgUrl"
                        alt=""
                      />
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </li>
        </div>
      </ul>
    </div>
    <!-- 发送信息操作区域 -->
    <div v-show="!isManySelect" class="chat-bottom" id="chat-bottom">
      <div class="chat-send-opera">
        <div class="chat-send-icon">
          <i
            title="选择表情"
            class="iconfont iconIMweb_expression"
            @click="IconColor('iconIMweb_expression')"
            @click.stop="chooseEmoji"
          ></i>
          <el-upload
            class="_upload"
            accept=".jpg,.jpeg,.png,.gif,.bmp,.pdf,.JPG,.JPEG,.PBG,.GIF,.BMP,.PDF"
            action="/mytio/chat/img.tio_x"
            :file-list="[]"
            name="uploadFile"
            :on-success="handleSuccess"
            @on-error="fileError"
            :data="{ chatlinkid: chatOn }"
            multiple
          >
            <i title="发送图片" class="iconfont iconIMweb_picture"> </i>
          </el-upload>
          <el-upload
            class="_upload"
            action="/mytio/chat/file.tio_x"
            :file-list="[]"
            name="uploadFile"
            :on-success="handleSuccess"
            @on-error="fileError"
            :data="{ chatlinkid: chatOn }"
            multiple
          >
            <i title="发送文件" class="iconfont iconIMweb_file"> </i>
          </el-upload>
          <el-upload
            class="_upload"
            accept="video/*"
            action="/mytio/chat/video.tio_x"
            :file-list="[]"
            name="uploadFile"
            :on-success="handleSuccess"
            @on-error="fileError"
            :data="{ chatlinkid: chatOn }"
            multiple
          >
            <i title="发送视频" class="iconfont iconIMweb_video"> </i>
          </el-upload>
          <!-- <i title="发送图片" class="iconfont iconIMweb_picture">
            <input type="file" accept="image/*" id="chat-send-img" @change="uploadImg" />
          </i>
          <i title="发送文件" class="iconfont iconIMweb_file">
            <input type="file" accept="*" @change="uploadDix" />
          </i>
          <i title="发送视频" class="iconfont iconIMweb_video">
            <input type="file" accept="video/*" @change="uploadVideo" />
          </i> -->

          <i
            title="@"
            class="iconfont iconIMweb_"
            v-show="isGroup"
            @click="IconColor('iconIMweb_')"
            @click.stop="getRemindContent"
          ></i>
          <i
            v-if="switchConfig.isOpenCard.value"
            title="推荐好友"
            class="iconfont iconIMweb_grcard"
            @click="IconColor('iconIMweb_grcard')"
            @click.stop="shareFriend"
          >
          </i>
          <i
            title="分享群聊"
            class="iconfont iconIMweb_qcard"
            @click="IconColor('iconIMweb_qcard')"
            @click.stop="shareGroup"
          >
          </i>
          <i
            title="语音通话"
            class="iconfont iconvoicecall"
            v-if="!isGroup && switchConfig.isOpenVoice.value"
            @click="IconColor('iconvoicecall')"
            @click.stop="wxCallInvite(1)"
          >
          </i>
          <i
            title="视频通话"
            class="iconfont iconVideocall"
            v-if="!isGroup && switchConfig.isOpenVideo.value"
            @click="IconColor('iconVideocall')"
            @click.stop="wxCallInvite(2)"
          >
          </i>
        </div>
      </div>
      <!-- 输入聊天内容文本框 -->
      <div
        id="chat-editor"
        name="content"
        class="chat-editor"
        @keydown.enter.prevent="wxTextKey"
        @paste="pasteSendV2"
        contenteditable="true"
        @input="listenRemind"
        @blur="getSelecRange"
      ></div>
      <div class="chat-send-bot">
        <div class="chat-send-tips">
          Ctrl+Enter：换行
          <span>|</span>
          Enter：发送
        </div>

        <!-- chatSendMessageBefore -->
        <button class="primarybtn" @click.stop="chatSendMessageBefore">
          发送
        </button>
        <file-upload @input="handleFileChange" :drop="true"></file-upload>
      </div>
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
            <img :src="staticUrl + 'static/emoji/emoji/' + emojiList[0].url" />
          </li>
        </ul>
      </div>
      <!-- 分享好友名片|群聊名 -->
      <ShareCard
        ref="shareCard"
        :pcarshow.sync="pcarshow"
        :gcarshow.sync="gcarshow"
        :fileShow.sync="fileShow"
        :files="dragFiles"
        @preview="dragPreview"
        @sendFiles="dragSendFiles"
        @close="clearDragFileList"
      ></ShareCard>
    </div>
    <!-- 多选消息操作区域 -->
         
    <div class="manyMsgMenu row-c-c" v-show="isManySelect">
      <div >
        <ul class="menus">
          <li class="menu clm-c-c" @click="showManyTransMessage(true)">
            <i class="menu-icon row-c-c li-icon-relay"></i>
            <span class="menu-text mg-t1">{{"合并-转发"}}</span>
          </li>
        </ul>
        <i class="close-btn li-icon-close" @click="hideManySelect"></i>
        </div>

             <!--我就加了这div 添加空格    还是请求 原来 逐条 -->
     &nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;
     <div >
        <ul class="menus">
          <li class="menu clm-c-c" @click="showManyTransMessage(false)">
            <i class="menu-icon row-c-c li-icon-relay"></i>
            <span class="menu-text mg-t1">逐条转发</span>
          </li>
        </ul>
        <i class="close-btn li-icon-close" @click="hideManySelect"></i>
    </div>
    </div>


    <!-- 放大图片容器弹框 -->
    <div id="view-container" style="display: none" class="view-container">
      <img
        :src="item.imgsrc"
        v-for="item in pImgList.length > 0 ? pImgList : imgList"
        :key="item.mid"
      />
    </div>
    <!-- 视频播放容器 -->
    <Dialog v-show="video.show">
      <div class="modelbody videomodel">
        <p class="title">
          <span class="videoname" id="videoname">{{ video.title }}</span>
          <i
            class="iconfont iconIMweb_cancel_cancel closeicon"
            @click="closeVideo"
          ></i>
        </p>
        <video
          class=""
          :src="video.url"
          id="tm-video"
          controls
          loop="loop"
          autoplay="autoplay"
        ></video>
      </div>
    </Dialog>
    <!-- 确定加入群聊-弹框 -->
    <Dialog v-show="joinshow">
      <div class="modelbody">
        <div class="maintitle">是否接受邀请加入群聊？</div>
        <div class="button-group">
          <button class="primarybtn default" @click="cancleJoin">取消</button>
          <button class="primarybtn" @click="sureJoinGroup" :disabled="loading">
            确定
          </button>
        </div>
      </div>
    </Dialog>
    <!-- @列表 -->
    <div id="remindContent" class="remindList" @click.stop="stopProp">
      <div class="remindContent" v-show="remindshow">
        <p class="remind-title">
          <span>选择需要提醒的人</span>
          <i
            class="iconfont iconIMweb_cancel_cancel closeicon"
            @click="hideRemind"
          ></i>
        </p>
        <p class="tm-search-friend">
          <i class="iconfont iconIMweb_search"></i>
          <input
            type="text"
            autocomplete="off"
            placeholder="请输入好友名称"
            class="tm-search-input"
            id="remindsearch"
            v-model="remindsearch"
            @input="remindSearch"
          />
        </p>
        <div id="remindfriends" class="remindbody">
          <ul class="remindfriends">
            <li class="remind-col" @click.stop="addReMind(atAll)">
              <img
                class="icon_all"
                src="~@/assets/imgs/msglist/@all.png"
                alt=""
              />
              <span>提醒所有人</span>
            </li>
            <li
              class="remind-col"
              @click.stop="addReMind(item)"
              v-for="item in atMemList"
              :key="item.uid"
              v-show="curruid != item.uid"
            >
              <el-image :src="item.avatar" class="remind-col_img">
                <div slot="error" class="image-slot">
                  <img
                    src="~@/assets/imgs/common/avatar.jpg"
                    class="error-img"
                  />
                </div>
              </el-image>
              <span class="flexauto" v-text="item.nick"></span>
            </li>
          </ul>
          <div class="noremind" v-show="atMemList.length == 0">暂无数据</div>
        </div>
      </div>
    </div>

    <!-- 消息右键操作框 -->
    <ContextMenu
      ref="ContextMenu"
      :show="contextmenushow"
      :contextmenu="contextmenu"
      :groupInfo="groupInfo"
      :cmenutype="cmenutype"
      :isFriend="isFriend"
      :isforbidden="isforbidden"
      @rightRemind="rightRemind"
      @userApply="userApply"
      @quoteMsg="quoteMsg"
      @showManySelect="showManySelect"
    ></ContextMenu>
    <!-- 个人名片 -->
    <UserCard
      :show.sync="cardshow"
      :userCard="userCard"
      ref="usercard"
    ></UserCard>
    <!-- 群聊信息 -->
    <GroupMore ref="groupmore" :show.sync="groupmore"></GroupMore>
    <!-- 群聊相关弹框 -->
    <GroupModel
      ref="groupmodel"
      :groupInfo="commGroupInfo"
      :groupApplyInfo="groupApplyInfo"
    ></GroupModel>
    <GroupModel
      v-if="storeGroupInfo"
      ref="groupmodelnotice"
      :type.sync="groupmodelnotice_type"
      :groupInfo="storeGroupInfo"
    ></GroupModel>

    <!-- 合并消息弹窗 -->
    <Dialog v-show="mergeMsgShow">
      <div class="modelbody merge-msg-model">
        <p class="title">
          {{ mergeMsgTitle }}
          <i class="iconfont iconIMweb_cancel_cancel closeicon" @click="mergeMsgShow = false"></i>
        </p>
        <div class="merge-msg-content">
          <div v-for="(msg, index) in mergeMsgList" :key="index" class="merge-msg-item">
            <div class="msg-header" style="display: flex; align-items: center; justify-content: space-between;">
              <span class="msg-nick" style="display: flex; align-items: center;">
                <el-image
                  class="msg-avatar"
                  style="width: 30px; height: 30px; border-radius: 50%; margin-right: 8px;"
                  :src="msg.avatar"
                />
                {{ msg.nick }}
              </span>
              <span class="msg-time">{{ getShowTime(msg.t) }}</span>
            </div>
            <div class="msg-content">
              <!-- 根据消息类型显示不同内容 -->
              <div v-if="msg.ct == 1" v-html="msg.c"></div>
              <div v-else-if="msg.ct == 3">
                <i class="li-icon-file"></i>
                <span>{{ JSON.parse(msg.c).filename }}</span>
              </div>
              <div v-else-if="msg.ct == 4">
                <i class="li-icon-yuyin"></i>
                <span>语音消息</span>
              </div>
              <div v-else-if="msg.ct == 5">
                <i class="li-icon-video"></i>
                <span>{{ JSON.parse(msg.c).filename }}</span>
              </div>
              <div v-else-if="msg.ct == 6">
                <img
                    :src="resUrl(JSON.parse(msg.c).coverurl)"
                    fit="cover"
                    :id="'mergeImg' + JSON.parse(msg.c).mid"
                    class="el-image"
                    @click="imgClick(JSON.parse(msg.c).mid)"
                    :style="{
                        width: JSON.parse(msg.c).showWidth,
                        height: JSON.parse(msg.c).showHeight,
                      }"
                />
              </div>
          <div v-else>
            {{ msg.c }}
          </div>
        </div>
      </div>
    </div>
  </div>
</Dialog>

<!-- 举报弹窗 -->
    <Dialog v-show="reportShow">
      <div class="modelbody reportShow-model">
        <p class="title">
          举报投诉
          <!-- 确定要举报该{{reportmsg}}吗？ -->
        </p>
        <div class="areacontainer">
          <textarea
            maxlength="500"
            v-model="reportInput"
            placeholder="请填写举报投诉原因"
          ></textarea>
          <p class="num-count">{{ reportInput.length }}/500</p>
        </div>
        <ul class="report_imgs _row wrap">
          <li
            class="report_img"
            v-for="(item, ix) in reportImgList"
            :key="ix"
            @click="delReportImg(ix)"
          >
            <img :src="resUrl(item)" alt="" />
            <div class="mask row-c-c c-white">
              <i class="li-icon-garbage"></i>
            </div>
          </li>
          <el-upload
            class="_upload"
            accept=".jpg,.jpeg,.png,.JPG,.JPEG,.PBG"
            action="/mytio/sys/upload.tio_x"
            :file-list="[]"
            name="file"
            :on-success="reportUploadSuccess"
            @on-error="fileError"
            multiple
          >
            <li class="report_upload row-c-c">
              <i class="li-icon-add c-major"></i>
            </li>
            <div slot="progress"></div>
          </el-upload>
        </ul>
        <div class="button-group">
          <button class="primarybtn default" @click="reportShow = false">
            取消
          </button>
          <button class="primarybtn" @click="sureSysReport" :disabled="loading">
            提交
          </button>
        </div>
      </div>
    </Dialog>
  </div>
</template>
<script>
import { mapState, mapMutations, mapActions } from "vuex";

// const generateSampleData = () => {
//   return {
//     chatlinkid: "-1",
//     currNick: "的了",
//     currUid: "-1",
//     item: {
//       avatar: "/avatar/img/20250319107/2145501902355839449178112.jpg",
//       bc: "",
//       c: "{\"ext\":\"jpg\",\"fileicontype\":7,\"filename\":\"Screenshot_2025-03-20-11-29-14-418_xxx.xxx.xxxc.jpg\",\"id\":19,\"session\":\"1902619637619761152\",\"size\":\"69456\",\"uid\":166955,\"url\":\"/202503/wx/upload/file/54/3692/38039/88241135/74541454503/6/151952/1902621096029593600.jpg\"}",
//       ct: 3,
//       d: 2,
//       f: 166955,
//       fc: {
//         ext: "jpg",
//         fileicontype: 7,
//         filename: "Screenshot_2025-03-20-11-29-14-418_xxx.xxx.xxxc.jpg",
//         id: 19,
//         session: "1902619637619761152",
//         size: 69456,
//         uid: 166955,
//         url: "/202503/wx/upload/file/54/3692/38039/88241135/74541454503/6/151952/1902621096029593600.jpg"
//       },
//       g: "1",
//       grouprole: 2,
//       label: "",
//       mid: "168",
//       nick: "YY",
//       opernick: "",
//       quotemsgtype: 0,
//       sendbysys: 2,
//       sigleflag: 2,
//       sigleuid: -1,
//       sysmsgkey: "",
//       t: "2025-03-20 15:19:53",
//       tonicks: "",
//       whereflag: 2,
//       whereuid: ","
//     },
//     grouprole: 0,
//     isCheck: true,
//     isSendMsg: false,
//     showName: true
//   };
// };
import store from "@/store/index.js";
import ShareCard from "@/components/home/ShareCard"; //分享名片
import ContextMenu from "@/components/home/ContextMenu"; //右键操作框
import UserCard from "@/components/UserCard"; //个人名片
import GroupMore from "@/components/home/GroupMore"; //群聊信息
import Viewer from "viewerjs"; //放大图片插件
import "viewerjs/dist/viewer.min.css"; //放大图片插件css
import wsSend from "@/assets/js/ws/send";
import { wscommand } from "@/assets/js/ws/command.js";
import {
  getShowTime,
  defineScroll,
  resUrl,
  checkTimeout,
} from "@/assets/js/common";
import { emojData } from "@public/static/emoji/emojUtil"; //处理表情包方法
import msgMixin from "@/mixins/msgmixin.js"; //发送消息相关逻辑
import UserCardMixins from "@/mixins/usercard.js"; //个人信息卡片
import GroupModel from "@/components/group/GroupModel"; //群聊信息
import NoticeBar from "@/components/common/NoticeBar.vue";
import FileUpload from "vue-upload-component";
import JSONBIG from 'json-bigint';
import {
  group,
  friend,
  msgTips,
  chatcom,
  getCommonConfByName,
} from "@/axios/path";
import { fileTolocalUrl, dataEncode, dataDecode } from "@/assets/js/toolsLi.js";
export default {
  data() {
    return {
      chatLength: 0, //最新页消息列表条数
      nextPage: false, //是否加载消息列表下一页
      $msgcontainer: null, //消息列表dom
      $chatEditor: null, //发送消息输入框dom
      video: {
        url: "",
        show: false,
        title: "",
      },
      emojiList: emojData.emojiList,
      staticUrl: "", //表情包拼接的绝对路径
      showEmoji: false, //表情包布局是否显示
      contextmenushow: false,
      contextmenu: {
        //会话列表右键
        top: 0,
        left: 0,
        data: {},
      },
      cmenutype: "msg", //右键类型
      groupmore: false, //群聊信息框显示状态
      reportShow: false, //举报弹窗
      reportmsg: "", //举报弹窗提示信息
      groupInfo: {},
      // groupUserState:{},
      groupApplyInfo: {}, //审核需要的信息
      reportInput: "", //举报原因
      reportImgList: [],
      atAll: {
        nick: "所有人",
        uid: "all",
      },
      groupmodelnotice_type: "",
      showTimeType: 2,
      dragFiles: [],
      dragFileImgs: [],
      fileShow: false,
      pImgList: [],
      friendOnlineInfo: null, // 好友在线信息
      isManySelect: false, // 消息多选
      manySelectMids: {},
      manySelectMsgItem: [],
      msg: {},
      mergeMsgShow: false,        // 合并消息弹窗显示状态
      mergeMsgList: [],          // 合并消息列表
      mergeMsgTitle: "聊天记录"   // 弹窗标题
    };
    
  },
  computed: {
    ...mapState({
      curruid: (state) => state.User.currUid,
      chatinfo: (state) => {
        console.log("Ws chatinfo", state.Ws.chatInfo);
        return state.Ws.chatInfo;
      }, //会话详情
      chatOn: (state) => state.Ws.chatOn, //当前会话id
      bizId: (state) => state.Ws.bizId, //当前会话-群聊groupid或私聊好友uid
      MessageList: (state) => {
        let list = state.Ws.MessageList; //消息列表
        let timeType = 1;
        if (timeType == 2) {
          let _list = []; //消息列表
          let before_time = "";
          for (var i = 0; i < list.length; i++) {
            // console.log(i);
            let t = list[i].t;
            let is = checkTimeout(before_time, t);
            // console.log(is);
            if (is) {
              let obj = {
                type: 9001,
                time: t,
                delwhere: false,
                singleshow: true,
              };
              _list.push(obj);
            }
            _list.push(list[i]);
            before_time = t;
          }
          return _list;
        }
        // console.log("MessageList", list);
        return list;
      },
      imgList: (state) => state.Ws.imgList, //消息中的图片列表
      isGroup: (state) => state.Ws.isGroup, //当前会话-是否为群聊
      applyThis: (state) => state.Ws.applyThis, //当前页面this
      commGroupInfo: (state) => state.CommonInfo.groupUserInfo, //群消息
      storeGroupInfo: (state) => state.CommonInfo.groupUserInfo.group, //new群消息
      switchConfig: (state) => state.CommonInfo.switchConfig, // 后台控制开关
      myRemarks: (state) => state.User.myRemarks,
      myRemarkState: (state) => state.User.myRemarkState,
    }),
  },
  components: {
    ShareCard,
    ContextMenu,
    UserCard,
    GroupMore,
    GroupModel,
    NoticeBar,
    FileUpload,
  },
  watch: {
    showEmoji(val) {
      if (!val) {
        $(".iconIMweb_expression").removeClass("icon_select"); //移除点击后的样式
      }
    },
    remindshow(val) {
      if (!val) {
        $(".iconIMweb_").removeClass("icon_select"); // 移除点击后的样式
      }
    },
    chatOn(val) {
      // 如果切换到群组那么获取群组信息，用于展示滚动通知
      if (this.isGroup) {
        let groupid = Math.abs(val);
        group.getWxGroupInfo(groupid, 1).then((res) => {
          store.commit("setGetGroupInfo", res.group);
        });
      }
      if (this.switchConfig.isShowOnlineStatus && !this.isGroup) {
        this.checkOnlineStatus();
      }
      // 切换聊天关闭多选状态
      this.hideManySelect();
    },
    myRemarkState(val) {
      this.$forceUpdate();
      // console.log("change myRemarks", val, this.myRemarks);
    },
  },
  mounted() {
    this.staticUrl = process.env.BASE_URL; //绝对路径
    this.$nextTick(() => {
      this.$msgcontainer = $("#msgcontainer");
      this.$chatEditor = $("#chat-editor");
      this.setChatEditor(this.$chatEditor);
      this.setChatSofftop($("#chat-bottom").offset().top - 88);
    });

    // 监听编辑区图片预览事件
    window.dragPreviewImg = (time) => {
      var imgSrcs = [];
      $("#chat-editor")
        .find("img")
        .each(function () {
          imgSrcs.push({
            imgsrc: $(this).attr("src"),
            mid: $(this).attr("data-time"),
          });
        });
      let ix = imgSrcs.findIndex((item) => item.mid == time);
      let obj = {
        ix,
        type: "image",
        list: imgSrcs,
      };
      this.pImgList = obj.list;
      this.$nextTick(() => {
        this.imgClick(null, obj.ix);
      });
    };
    // 监听删除引用事件
    window.clearCite = (event) => {
      event.stopPropagation();
      $("#citeAnchor").remove();
    };
    // 监听预览引用内容
    window.previewCite = (e) => {
      let citeAnchor = $("#citeAnchor");
      let citeData = citeAnchor.attr("data-data");
      if (citeData) {
        let message = dataDecode(citeData);
        this.quotePreviewInput(message);
      }
    };
  },
  mixins: [msgMixin, UserCardMixins],
  methods: {
    ...mapActions(["getChatGroupInfo"]),
    ...mapMutations([
      "setChatOldMsg",
      "setChatSofftop",
      "setGroupMore",
      "setCallType",
      "setChatEditor",
    ]),
    resUrl,
    getRemark() {
      console.log(this.myRemarks);
    },
    handleSuccess(response, file, fileList) {
      console.log(response);
      if (response.ok) {
        msgTips("文件发送成功");
      } else {
        msgTips("发送失败，请重试");
      }
    },
    fileError(msg) {
      msgTips(msg ? msg : "发送失败，请重试");
    },
    /* 历史消息 */
    getMsgList(mid, unshift) {
      if (!this.chatOn) {
        return;
      }
      if (!mid) {
        this.nextPage = false;
      }

      this.setChatOldMsg({
        startmid: mid,
        unshift: unshift,
        chattype: this.isGroup,
        chatlinkid: this.chatOn,
      });
      //请求历史消息
      if (this.isGroup) {
        this.groupChatList(mid, unshift);
      } else {
        this.privaList(mid, unshift);
      }
    },
    /* 群聊历史 */
    groupChatList(mid, unshift) {
      let postdata = {
        chatlinkid: this.chatOn,
        startmid: mid ? mid : "",
      };
      wsSend(wscommand.WxGroupMsgReq, postdata);
    },
    /* 私聊消息 */
    privaList(mid, unshift) {
    console.log("===================", this.$msgcontainer);
      let postdata = {
        chatlinkid: this.chatOn,
        startmid: mid ? mid : "",
      };
      console.log("获取私聊消息记录", postdata);
      wsSend(wscommand.WxFriendMsgReq, postdata);
    },
    /* 初始化聊天定位到底部*/
    initScrollBotm(customTop) {
      let scrollTopPx = 0;
      let _this = this;
      if (this.$msgcontainer.find(".msg-col").length > 0) {
        let topOffsetPx = this.$msgcontainer
            .find(".msg-col:first")
            .offset().top,
          domOffsetPx = this.$msgcontainer.find(".msg-col:last").offset().top;
        // 目标元素相对于文档偏移量 - 第一个元素相对于文档偏移量 就是滚动条要滚动的距离
        scrollTopPx = domOffsetPx - topOffsetPx;

        // console.log("defineScroll $msgcontainer", this.$msgcontainer);
        // console.log("defineScroll", {
        //   customTop,
        //   scrollTopPx,
        //   domOffsetPx,
        //   topOffsetPx,
        // });
      }

      defineScroll(
        this.$msgcontainer,
        (customTop ? customTop : scrollTopPx) + "px",
        {
          whileScrolling: function () {
            if (
              this.mcs.draggerTop <= 30 &&
              this.mcs?.draggerTop >= 5 &&
              !_this.nextPage
            ) {
              _this.nextPage = true;
              console.log("debounce---------------");
              log('_this.$msgcontainer.find("li:first").attr("mid")');
              log(_this.$msgcontainer.find("li:first").attr("mid"));
              log('_this.$msgcontainer.find("li:first").attr("mid")');
            }
          },
        },
        { mouseWheel: { scrollAmount: 200, preventDefault: true } }
      );
    },
    getNextMsg() {
      console.log(this.$msgcontainer.find("li:first"));
      this.getMsgList(
        this.$msgcontainer.find("li:first").attr("mid"),
        "unshift"
      );
    },
    scrollBotm() {
      this.$nextTick(() => {
        this.$msgcontainer.mCustomScrollbar("scrollTo", "bottom", {
          scrollInertia: 1,
        });
        //  this.$msgcontainer.mCustomScrollbar("scrollTo",'last', {scrollInertia:1});
      });
    },
    /* 私聊或群聊处理数据及滚动位置 */
    privorgroup(mid) {
      let self = this;
      if (!mid) {
        self.$nextTick(() => {
          self.initScrollBotm();
          setTimeout(function () {
            self.scrollBotm();
          }, 300); //防止图片类高度加载不完全不能完全滚动到底部
        });
      } else {
        setTimeout(function () {
          let topOffsetPx = $("#msgcontainer li:first").offset().top,
            domOffsetPx = $("#msgcontainer li")
              .eq(self.chatLength)
              .offset().top,
            // 目标元素相对于文档偏移量 - 第一个元素相对于文档偏移量 就是滚动条要滚动的距离
            scrollTopPx = domOffsetPx - topOffsetPx;
          $("#msgcontainer").mCustomScrollbar("scrollTo", scrollTopPx + "px", {
            scrollInertia: 1,
          });
          // self.nextPage = true
        }, 150);
      }
    },
    /* 消息中的图片点击事件 */
    imgClick(mid, ix) {
      console.log("imgClick", this.imgList, this.pImgList, mid, ix);
      let that = this;
      let index =
        ix != undefined
          ? ix
          : this.imgList.findIndex((item) => item.mid == mid); //当前第几张图片显示
      debugger;
      var viewer = new Viewer(document.getElementById("view-container"), {
        hidden: function () {
          that.pImgList = [];
          console.log("imgClick closeed");
          viewer.destroy();
        },
        button: true,
        toolbar: {
          zoomIn: 4,
          zoomOut: 4,
          prev: function () {
            viewer.prev(false); //当前是第一个时是不转向查看最后一个
          },
          next: function () {
            viewer.next(false); //当前是最后一个时是不转向查看第一个
          },
          loop: false,
        },
        loop: false,
        title: false,
        navbar: false,
      });
      viewer.view(index);
      viewer.show();
    },
    /* 消息中的视频点击事件 */
    videoClick(item) {
      let title = item.title;
      this.video.url = item.videourl;
      this.video.title = title;
      let $videoDom = $("#tm-video");
      let realw = item.width,
        realh = item.height,
        lw = $(window).width(),
        lh = $(window).height(),
        area = "";
      if (realw / realh > lw / lh) {
        $videoDom.css({
          width: 0.8 * lw + "px",
          height: (0.8 * lw * realh) / realw + "px",
        });
        $("#videoname").css({ width: 0.8 * lw - 100 + "px" });
      } else {
        $videoDom.css({
          width: (0.8 * lh * realw) / realh + "px",
          height: 0.8 * lh + "px",
        });
        $("#videoname").css({ width: (0.8 * lh * realw) / realh - 100 + "px" });
      }
      this.video.show = true;
    },
    /* 消息播放音频 */
    playAudio(item) {
      let _this = this;
      let audio = document.getElementById("audio" + item.bodyData.id);
      audio.currentTime = 0;
      // audio.volume = 1;
      let allaudio = document.getElementsByClassName("audio");
      $.each(allaudio, (i, v) => {
        if (audio != v) {
          let mid = v.getAttribute("mid");
          _this.MessageList.map((val) => {
            if (val.mid == mid && val.ct == 4) {
              val.bodyData.play = false;
            }
          });
          v.pause();
        }
      });
      if (audio.paused) {
        item.bodyData.play = true;
        audio.play();
        $("#audio" + item.bodyData.id).unbind("ended");
        $("#audio" + item.bodyData.id).on("ended", function () {
          item.bodyData.play = false;
          audio.pause();
        });
      } else {
        item.bodyData.play = false;
        audio.pause();
      }
    },
    /* 关闭视频弹框 */
    closeVideo() {
      this.video.show = false;
      this.video.url = "";
    },
    getShowTime(time) {
      return getShowTime(time);
    },
    /* 群聊设置 */
    chatGroupSet() {
      this.setGroupMore(true);
    },
    /* 添加好友 */
    userApply(uid) {
      this.userCard.data.id = uid;
      this.$refs.usercard.applyFriend();
    },
    /**
     * 解析文件的大小B、KB、MB、GB
     */
    formatFileSize(fileSize) {
      if (fileSize < 1024) {
        return fileSize + "B";
      } else if (fileSize < 1024 * 1024) {
        var temp = fileSize / 1024;
        temp = temp.toFixed(2);
        return temp + "KB";
      } else if (fileSize < 1024 * 1024 * 1024) {
        var temp = fileSize / (1024 * 1024);
        temp = temp.toFixed(2);
        return temp + "MB";
      } else {
        var temp = fileSize / (1024 * 1024 * 1024);
        temp = temp.toFixed(2);
        return temp + "GB";
      }
    },
    IconColor(e) {
      $(`.${e}`).addClass("icon_select");
    },
    openNews(url) {
      window.open(url, "_blank");
    },
    /**审核 */
    goExamine(item) {
      let aid = item.apply.id;
      this.groupInfo = item;
      group.groupApplyInfo({ aid }).then((res) => {
        if (res.ok) {
          res.data.apply.groupavator = resUrl(res.data.apply.groupavator);
          this.groupApplyInfo = res.data;
          this.groupApplyInfo.groupApply = item;
          res.data.items.map((ite) => {
            this.$refs.groupmodel.auditProcessing = true;
            ite.avatar = resUrl(ite.avatar);
          });
        }
      });
    },
    /**展示用户的用户信息 */
    async showCard(e, uid, item) {
      // 关闭互加好友功能时，不能查看不是好友用户信信息，除非时管理员和群主
      if (this.isGroup) {
        let isfriend = await friend.isMyFriend(uid);
        if (isfriend == 2) {
          this.getChatGroupInfo(this.bizId);
          let groupInfo = this.commGroupInfo;
          if (
            groupInfo.group.friendflag == 1 ||
            (groupInfo.group.friendflag == 2 &&
              (groupInfo.groupuser.grouprole == 1 ||
                groupInfo.groupuser.grouprole == 3))
          ) {
            this.showUserCard(e, uid);
          }
        } else {
          this.showUserCard(e, uid);
        }
      } else {
        this.showUserCard(e, uid);
      }
    },
    /* 群用户状态-当前用户下 */
    //  chatForbiddenFlag(uid){
    //    let postdata={
    //       groupid:this.bizId,
    //       uid
    //    }
    //     group.chatForbiddenFlag(postdata).then(res=>{
    //         if(res.ok){
    //          this.groupUserState = res.data
    //         }
    //     })
    // },
    strRep(str) {
      var reg = /<[^<>]+>/g;
      var str1 = str.replace(reg, "");
      var str2 = str1.replace(/\s+/g, "");
      var last = 0;
      var all = str2.length;
      var fisrt = str2.substring(0, 6);
      if (all > 13) {
        return fisrt + "..." + str2.substring(all - 8);
      } else {
        return str2;
      }
    },
    /**举报投诉 */
    sureSysReport() {
      if (this.reportInput == "") {
        msgTips("请填写举报投诉原因");
        return;
      }
      let data = {
        reason: this.reportInput,
        imgs: this.reportImgList.join(","),
      };
      if (this.isGroup) {
        data.groupid = this.bizId;
      } else {
        data.touid = this.bizId;
      }
      chatcom.sysReport(data).then((res) => {
        if (res.ok) {
          this.reportShow = false;
          this.reportInput = "";
          this.reportImgList = [];
          msgTips("举报成功");
        } else {
          msgTips(res.msg);
        }
      });
    },
    // 举报投诉图片上传成功后
    reportUploadSuccess(e) {
      if (e.ok) {
        this.reportImgList.push(e.data);
      } else {
        msgTips(e.msg);
      }
    },
    delReportImg(ix) {
      this.reportImgList.splice(ix, 1);
    },
    setlastPage(isflag) {
      this.lastPage = isflag;
    },
    showNotice() {
      this.groupmodelnotice_type = "groupnotice";
      // this.$refs.groupmodelnotice.auditProcessing = true;
      // this.$refs.groupmore.showModel('editnotice')
      console.log(this.$refs.groupmore);
    },
    handleFileChange(files) {
      console.log("origin files", files);
      // 处理选取的文件
      let citeAnchor = $("#citeAnchor");
      let isCiteAnchor = citeAnchor.length > 0 ? true : false;
      console.log("isCiteAnchor", isCiteAnchor);
      files.map((item) => {
        let arr = item.type.split("/");
        item.genericsType = arr[0];
        item.simpType = arr[1];
        item.localUrl = fileTolocalUrl(item.file);
      });
      files
        .filter((item) => item.genericsType == "image")
        .map((item, ix) => {
          let t = new Date().getTime();
          let str = `<img src="${item.localUrl}" alt="" data-time="${t}" onclick="dragPreviewImg(${t})" style="width: 66px; height: 66px; background-color: #EBEBEB; margin-left: 4px;cursor: pointer" />`;
          if (isCiteAnchor) {
            citeAnchor.before(str);
          } else {
            $("#chat-editor").append(str);
          }
          this.dragFileImgs.push(item);
        });
      this.moveCursorToLast();
      console.log(files);
      this.dragFiles = this.dragFiles.concat(
        files.filter((item) => item.genericsType != "image")
      );
      if (this.dragFiles.length > 0) this.fileShow = true;
      console.log(this.dragFiles);
    },
    // 拖拽模块预览
    dragPreview(e) {
      if (e.type == "image") {
        this.pImgList = e.list;
        this.$nextTick(() => {
          this.imgClick(e.ix);
        });
      } else {
        this.videoClick(e.video);
      }
    },
    // 拖拽模块发送
    async dragSendFiles(files) {
      let successNum = 0;
      for (let i = 0; i < files.length; i++) {
        let item = files[i];
        let params = {
          file: item.file,
        };
        let res;
        if (item.genericsType == "image") {
          res = await this.uploadImg(params);
        } else if (item.genericsType == "video") {
          res = await this.uploadVideo(params);
        } else {
          res = await this.uploadDix(params);
        }
        if (res) successNum++;
      }
      this.pImgList = [];
      this.fileShow = false;
      this.clearDragFileList();
      msgTips("共成功上传" + successNum + "个文件");
    },
    // 清空拖拽列表
    clearDragFileList() {
      this.dragFiles = [];
    },
    handleCallBack(ct) {
      if (ct === 10) {
        // 使用原生JS查找并点击视频通话按钮
        const videoCallBtn = document.querySelector('.iconVideocall');
        if (videoCallBtn) {
          videoCallBtn.click();
        }
      } else {
        // 使用原生JS查找并点击语音通话按钮
        const voiceCallBtn = document.querySelector('.iconvoicecall');
        if (voiceCallBtn) {
          voiceCallBtn.click();
        }
      }
    },
    // 引用消息预览
    quotePreview(item) {
      console.log("引用消息预览", item);
      let quotemsgtype = item.quotemsgtype;
      let quotemsgcontent = item.quotemsgcontent;
      switch (quotemsgtype) {
        case 1:
          this.quoteContextmenu(item.quotemid);
          break;
        case 2:
          this.quoteContextmenu(item.quotemid);
          break;
        case 3:
          this.quoteContextmenu(item.quotemid);
          break;
        case 4:
          let audioItem = this.MessageList.find(
            (msg) => msg.mid == item.quotemid
          );
          this.playAudio(audioItem);
          break;
        case 5:
          this.videoClick({
            title: quotemsgcontent.filename,
            videourl: resUrl(quotemsgcontent.url),
          });
          break;
        case 6:
          this.$nextTick(() => {
            this.imgClick(item.quotemid);
          });
          break;
        case 9:
          this.quoteContextmenu(item.quotemid);
          break;
        case 20:
          this.quoteContextmenu(item.quotemid);
          break;
      }
    },
    // 对话框引用消息预览
    quotePreviewInput(item) {
      console.log(item);
      let quotemsgtype = item.ct;
      let quotemsgcontent = item.c;
      if (![1, 2].includes(quotemsgtype)) {
        quotemsgcontent = JSON.parse(item.c);
      }
      switch (quotemsgtype) {
        case 1:
          this.quoteContextmenu(item.mid);
          break;
        case 2:
          this.quoteContextmenu(item.mid);
          break;
        case 3:
          this.quoteContextmenu(item.mid);
          break;
        case 4:
          let audioItem = this.MessageList.find((msg) => msg.mid == item.mid);
          this.playAudio(audioItem);
          break;
        case 5:
          this.videoClick({
            title: quotemsgcontent.filename,
            videourl: resUrl(quotemsgcontent.url),
          });
          break;
        case 6:
          this.$nextTick(() => {
            this.imgClick(item.mid);
          });
          break;
        case 9:
          this.quoteContextmenu(item.mid);
          break;
        case 20:
          this.quoteContextmenu(item.mid);
          break;
      }
    },
    // 引用消息右键
    quoteContextmenu(quoteMid) {
      console.log(quoteMid);
      // let topOffsetPx = this.$msgcontainer.find("li:first").offset().top;
      let topOffsetPx = $("#msgcontainer li:first").offset().top;
      // console.log('topOffsetPx', topOffsetPx);
      console.log(
        "topOffsetPx",
        this.$msgcontainer.find(`[mid="${quoteMid}"]`).offset().top,
        this.chatLength
      );
      // debugger
      let domOffsetPx = this.$msgcontainer
        .find(`[mid="${quoteMid}"]`)
        // .eq(this.chatLength)
        .offset().top;
      let scrollTopPx = domOffsetPx - topOffsetPx;
      this.initScrollBotm(scrollTopPx);
    },
    // 好友在线信息
    checkOnlineStatus() {
      let params = {
        uid: this.bizId,
      };
      friend.checkOnlineStatus(params).then((res) => {
        this.friendOnlineInfo = res.data;
      });
    },
    /* 消息多选 */
    // 开启消息多选
    showManySelect(msgItem) {
      // this.tset=JSON.stringify(msgItem, null, 2)
      // console.log("=======msgItem========"+msgItem)
      // console.log("=======msgItem-stringify========" + JSON.stringify(msgItem, null, 2));
      this.setManySelectMids(msgItem.mid,msgItem);
      this.isManySelect = true;
    },
    hideManySelect(){
      this.resetManySelectMids();
      this.isManySelect = false;
    },
    resetManySelectMids(){
      this.manySelectMids = {};
      this.manySelectMsgItem = [];
    },
    setManySelectMids(mid, msgItem) {
    let manySelectMids = this.manySelectMids;
    let manySelectMsgItem = this.manySelectMsgItem;
    let is = manySelectMids[mid];
    console.log("=== 3msgItem====",msgItem);
    if (is) {
        // 如果存在，就删除对应的mid和msgItem
        delete manySelectMids[mid];
        // 假设manySelectMsgItem是一个数组，根据msgItem的某个唯一标识来删除
        let index = manySelectMsgItem.findIndex(item => item.id === msgItem.id); // 假设msgItem有id属性
        if (index !== -1) {
            manySelectMsgItem.splice(index, 1);
        }
     
    } else {
        // 如果不存在，就添加对应的mid和msgItem
        manySelectMids[mid] = true;
        manySelectMsgItem.push(msgItem);
        this.msg=msgItem.nick+":"+msgItem.c
        // console.log("=== 2====");
    }
    this.$forceUpdate();
    // console.log(manySelectMids);
    console.log(manySelectMsgItem);
},
    viewMessageRecord(c) {
      try {
        const json = JSONBIG({storeAsString: true})
        let jsonObj = json.parse(c);
        chatcom.showMergeMessage({mergeid: jsonObj.mergeid})
            .then(viewData => {
              if (viewData && viewData.ok) {
                let data = JSON.parse(viewData.data.data);
                console.log("成功获取数据:", data);
                // 设置弹窗数据
                this.mergeMsgTitle = "聊天记录";
                this.mergeMsgList = data || [];

                // 显示弹窗
                this.mergeMsgShow = true;

              } else {
                let errorMsg = (viewData && viewData.msg) || "获取数据失败";
                console.error("请求失败:", errorMsg);
                msgTips(errorMsg);
              }
            })
            .catch(error => {
              console.error("Promise执行出错:", error);
              msgTips("请求处理出错");
            });

      } catch (parseError) {
        console.error("JSON解析错误:", parseError);
        msgTips("数据解析错误");
      }
    },
    
    showManyTransMessage(isMerge){
       let mids = Object.keys(this.manySelectMids).join(',');
      //  console.log("=======mids01========"+mids)
      //  console.log("======= this.tset========"+ this.tset)
       this.tset
       if(!mids){
        msgTips("没有选择需要转发的消息");
        return
       }
       this.$refs.ContextMenu.transType = 1;
       console.log("========this.$refs.ContextMenu.transType======="+this.$refs.ContextMenu.transType)
       this.$refs.ContextMenu.transShow = true;
       this.$refs.ContextMenu.contextmenu.data = {
        mid: mids,
        msgItem: this.manySelectMsgItem,
        msg:this.msg,
        isMerge: isMerge // 添加标识   原版 这样的 接口在哪里
       };
    }

    
  },
};
</script>
<style>
.cite {
  display: inline-block;
  position: relative;
  max-width: 500px;
  color: #999;
  background-color: #f1f1f1;
  width: auto;
  padding: 10px 30px 10px 10px;
  font-size: 30px;
  margin-top: 20px;
  cursor: pointer;
}

.cite .cite_text {
  font-size: 12px;
}
.cite .city_fork_icon {
  position: absolute;
  right: 0;
  top: 0;
  padding: 4px;
  font-size: 13px;
  color: #999;
}
.msg-quote {
  display: flex;
  clear: both;
  padding-top: 6px;
  cursor: pointer;
}
.msg-quote-main {
  font-size: 12px;
  color: #999;
  line-height: 1.4em;
  padding: 7px 10px;
  background-color: #e1e1e1;
}
.msg-quote-nick {
}
.msg-quote-img {
  max-width: 200px;
  max-height: 30px;
}
.msg-quote-video {
  position: relative;
}
.msg-quote-video::before {
  position: absolute;
  content: "";
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
}
.icon-play {
  position: absolute;
  color: #fff;
  font-size: 14px;
  font-weight: 100;
}
.msg-quote-text {
  max-width: 300px;
}
.many-t {
  -webkit-line-clamp: 3 !important;
}
.icon_card {
  margin: 0 4px;
}
</style>
<style lang="less" scoped>
@import "~@/assets/style/less/components/home/msglist.less";
._upload {
  display: inline-block;
}
.notice_bar {
  position: fixed;
  top: 50px;
  width: 100%;
  height: 40px;
  z-index: 10;
}
.notice_bar_null {
  width: 100%;
  height: 40px;
}
// .message-card {
//     width: 220px;
//     background-color: #e0f5f1;
//     border-radius: 8px;
//     padding: 12px;
//     box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
//     font-family: 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
// }

// .message-header {
//     font-size: 16px;
//     font-weight: 500;
//     margin-bottom: 4px;
//     color: #333;
// }

.message-body {
    font-size: 12px;
    color:#fff;
}

.merge-msg-model {
  .title {
    position: relative;
    padding: 15px 20px;
    border-bottom: 1px solid #eee;
    font-size: 16px;
    font-weight: bold;

    .closeicon {
      position: absolute;
      right: 15px;
      top: 50%;
      transform: translateY(-50%);
      cursor: pointer;
      color: #999;

      &:hover {
        color: #333;
      }
    }
  }

  .merge-msg-content {
    max-height: 400px;
    overflow-y: auto;
    padding: 15px 20px;

    .merge-msg-item {
      margin-bottom: 15px;
      padding-bottom: 15px;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        margin-bottom: 0;
        padding-bottom: 0;
        border-bottom: none;
      }

      .msg-header {
        display: flex;
        justify-content: space-between;
        margin-bottom: 8px;

        .msg-nick {
          font-weight: bold;
          color: #333;
        }

        .msg-time {
          font-size: 12px;
          color: #999;
        }
      }

      .msg-content {
        font-size: 14px;
        color: #666;
        line-height: 1.5;

        [class*="li-icon-"] {
          margin-right: 5px;
        }
      }
    }
  }
}

</style>
