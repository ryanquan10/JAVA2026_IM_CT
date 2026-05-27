<template>
  <div class="tioimcontainer">
    <!-- 新登录样式 -->
    <div class="bodycontent">
      <div class="form-content">
        <div
          class="form-content-left animate__animated animate__fadeInDown"
          :style="{ height: bgImgSize[currentIndex] + 'px' }"
        ></div>
        <!-- 登录 -->
        <div
          class="login-content animate__animated animate__fadeInUp"
          :style="{ height: bgImgSize[currentIndex] + 'px' }"
        >
          <p v-show="sendSuccess && currentIndex == 3" class="smsTips">
            已发送6位验证码至<span>{{ forget.form.loginName }}</span>
          </p>
          <p v-show="currentIndex === 3" class="title">忘记密码</p>
          <p v-show="currentIndex === 1" class="title">欢迎注册</p>
          <div
            class="form-tab"
            id="tab"
            v-show="currentIndex != 3 && currentIndex != 1"
          >
            <ul>
              <li
                @click="change(item.index, index, item.title)"
                :class="currentIndex == item.index ? 'form-tab-active' : ''"
                :key="item.id"
                v-for="(item, index) in list"
              >
                <p>{{ item.title }}</p>
                <p
                  v-show="currentIndex == item.index"
                  class="tab-border-bottom"
                ></p>
              </li>
            </ul>
          </div>
          <!-- IM登录/业务登录 -->
          <!-- <div class="loginTypeBox" v-show="currentIndex == 0">
            <p v-for="(item,index) in loginTypeArr" class="cursor" :key="index" @click="chooesLoginType(index)">
              <img :src="item.img" alt="">
              <span :class="item.class">{{item.title}}</span>
            </p>
          </div> -->
          <!-- tab -->
          <!-- 登录 -->
          <div v-show="currentIndex == 0" class="img">
            <el-form :model="login.form" class="user-form" ref="loginform">
              <el-form-item prop="loginName">
                <div class="input-row">
                  <i class="iconfont iconttubiao_personal"></i>
                  <el-input
                    class="user-input"
                    type="text"
                    v-model="login.form.loginName"
                    placeholder="请输入您的账号/手机号"
                  ></el-input>
                </div>
              </el-form-item>
              <el-form-item prop="passWord">
                <div class="input-row">
                  <i class="iconfont iconttubiao_password"></i>
                  <el-input
                    class="user-input"
                    :type="seepwd ? 'text' : 'password'"
                    v-model="login.form.passWord"
                    placeholder="请输入您的密码"
                  ></el-input>
                  <i
                    :class="[
                      'iconfont cursor icon-pwd',
                      seepwd ? 'iconttubiao_display' : 'iconttubiao_hide',
                    ]"
                    @click="changeSeePwd"
                  ></i>
                </div>
              </el-form-item>
              <div class="bot-group">
                <span>{{ errorMsg }}</span>
              </div>
              <button
                class="primarybtn bigbtn"
                @click.prevent="
                  loginType == 'im-login'
                    ? toLogin()
                    : loginType == 'bus-login'
                    ? adminlogin()
                    : ''
                "
              >
                登录
              </button>
            </el-form>
          </div>
          <!-- 注册 -->
          <div v-show="currentIndex == 1" class="img">
            <el-form
              :model="register.form"
              class="user-form"
              ref="registerform"
            >
              <el-form-item v-if="switchConfig.registerType.value == 1">
                <div class="input-row">
                  <i class="iconfont iconttubiao_personal"></i>
                  <el-input
                    class="user-input"
                    type="text"
                    v-model="register.form.loginName"
                    @input="
                      accountInput($event, switchConfig.registerType.value)
                    "
                    placeholder="请输入已有邮箱账号"
                    autoComplete="off"
                  ></el-input>
                </div>
              </el-form-item>

              <template v-else-if="switchConfig.registerType.value == 2">
                <el-form-item prop="loginName">
                  <div class="input-row">
                    <div class="input_icon" @click="showAreaSelect()">
                      <div v-if="smsType.icon == 'china'" class="icon_china">
                        <i class="li-icon-star"></i>
                      </div>
                      <i
                        v-else
                        :class="[
                          '_icon',
                          'icon_' + smsType.icon,
                          'li-icon-' + smsType.icon,
                        ]"
                      ></i>
                    </div>

                    <ul
                      v-if="areaSelectVisible"
                      class="area_select animate__animated animate__fadeInDown"
                    >
                      <li
                        class="area_option row_c"
                        v-for="(item, index) in smsTypeList"
                        :key="index"
                        @click="selectArea(index)"
                      >
                        <div class="area_icon row-c-c">
                          <div v-if="item.icon == 'china'" class="icon_china">
                            <i class="li-icon-star"></i>
                          </div>
                          <i
                            v-else
                            :class="[
                              '_icon',
                              'icon_' + item.icon,
                              'li-icon-' + item.icon,
                            ]"
                          ></i>
                        </div>

                        <div class="mg-l1">{{ item.name }}</div>
                      </li>
                    </ul>
                    <!-- <i
                    :class="[
                      'li-icon-global c-major size-max',
                    ]"
                    @click="changeSeePwd"
                  ></i> -->
                    <!-- <img
                    class="iconfont"
                    src="@/assets/imgs/login/mobile_icon.png"
                    alt=""
                    srcset=""
                  /> -->
                    <el-input
                      class="user-input"
                      type="text"
                      :maxlength="smsType.type == 1 ? 11 : 15"
                      v-model="register.form.loginName"
                      @input="mobileInput"
                      placeholder="请输入您的手机号"
                      autoComplete="off"
                    ></el-input>
                  </div>
                </el-form-item>
                <el-form-item
                  prop="mobileCode"
                  v-if="switchConfig.isOpenVerifyCode.value"
                >
                  <div class="input-row">
                    <img
                      class="iconfont"
                      src="@/assets/imgs/login/check_icon.png"
                      alt=""
                      srcset=""
                    />
                    <el-input
                      class="user-input"
                      type="text"
                      v-model="register.form.mobileCode"
                      placeholder="请输入您的验证码"
                      @input="codeInput"
                      autoComplete="off"
                    ></el-input>
                    <p
                      :class="[
                        mobileTipsFlag ? 'countDown' : '',
                        'get-mobileCode',
                        'cursor',
                      ]"
                      @click.stop="
                        mobileTipsFlag ? '' : getVerify(register.form.loginName)
                      "
                    >
                      {{ mobileTips }}
                    </p>
                  </div>
                </el-form-item>
              </template>

              <el-form-item v-else-if="switchConfig.registerType.value == 3">
                <div class="input-row">
                  <i class="iconfont iconttubiao_personal"></i>
                  <el-input
                    class="user-input"
                    type="text"
                    v-model="register.form.loginName"
                    @input="
                      accountInput($event, switchConfig.registerType.value)
                    "
                    placeholder="账户名由字母和数字组成"
                    autoComplete="off"
                  ></el-input>
                </div>
              </el-form-item>

              <el-form-item v-show="!emailCheck">
                <div class="input-row">
                  <i class="iconfont iconttubiao_personal"></i>
                  <el-input
                    class="user-input"
                    type="text"
                    v-model="register.form.nick"
                    placeholder="请设置您的昵称"
                    autoComplete="off"
                  ></el-input>
                </div>
              </el-form-item>
              <el-form-item v-show="!emailCheck">
                <div class="input-row">
                  <i class="iconfont iconttubiao_password"></i>
                  <el-input
                    :class="['user-input', seepwd ? '' : 'inputPsd']"
                    v-model="register.form.passWord"
                    placeholder="请设置您的密码"
                    autoComplete="off"
                  ></el-input>
                  <i
                    :class="[
                      'iconfont cursor icon-pwd',
                      seepwd ? 'iconttubiao_display' : 'iconttubiao_hide',
                    ]"
                    @click="changeSeePwd"
                  ></i>
                </div>
              </el-form-item>
              <el-form-item
                prop="parentinvitecode"
                v-if="switchConfig.isOpenInviteCode.value"
              >
                <div class="input-row">
                  <img
                    class="iconfont"
                    src="@/assets/imgs/login/invite_icon.png"
                    alt=""
                    srcset=""
                  />
                  <el-input
                    class="user-input"
                    type="text"
                    v-model="register.form.parentinvitecode"
                    placeholder="请输入您的邀请码"
                    autoComplete="off"
                  ></el-input>
                </div>
              </el-form-item>
              <!-- 邮箱账号-邮箱密码 -->
              <el-form-item v-show="emailCheck">
                <div class="input-row">
                  <i class="iconfont iconttubiao_personal"></i>
                  <el-input
                    class="user-input"
                    type="text"
                    v-model="register.form.email"
                    placeholder="请输入已有邮箱账号"
                    autoComplete="off"
                  ></el-input>
                </div>
              </el-form-item>
              <el-form-item v-show="emailCheck">
                <div class="input-row">
                  <i class="iconfont iconttubiao_password"></i>
                  <el-input
                    :class="['user-input', emailSeepwd ? '' : 'inputPsd']"
                    v-model="register.form.emailPassword"
                    placeholder="请输入邮箱账号密码"
                    autoComplete="off"
                  ></el-input>
                  <i
                    :class="[
                      'iconfont cursor icon-pwd',
                      emailSeepwd ? 'iconttubiao_display' : 'iconttubiao_hide',
                    ]"
                    @click="emailSeepwd = !emailSeepwd"
                  ></i>
                </div>
              </el-form-item>
              <!-- 勾选绑定邮箱 -->
              <!-- <div class="email-check cursor">
                <img @click="emailCheckClick" :src="emailCheckIcon" alt="">
                <span>绑定已有邮箱账号</span>
              </div> -->
              <div class="bot-group" style="margin: 3px 0 0 28px">
                <span>{{ errorMsg }}</span>
              </div>
              <button
                class="primarybtn bigbtn"
                @click.prevent="emailCheck ? toRegbindemail() : toRegister()"
                :disabled="registerDisabled"
              >
                {{ emailCheck ? "绑定邮箱账号" : "同意协议并注册" }}
              </button>
            </el-form>
          </div>
          <!-- 忘记密码 -->
          <div class="forget-content" v-show="currentIndex == 3">
            <el-form :model="forget.form" class="user-form" ref="forgetform">
              <el-form-item prop="loginName">
                <div class="input-row">
                  <div class="input_icon" @click="showAreaSelect()">
                    <div v-if="smsType.icon == 'china'" class="icon_china">
                      <i class="li-icon-star"></i>
                    </div>
                    <i
                      v-else
                      :class="[
                        '_icon',
                        'icon_' + smsType.icon,
                        'li-icon-' + smsType.icon,
                      ]"
                    ></i>
                  </div>

                  <ul
                    v-if="areaSelectVisible"
                    class="area_select animate__animated animate__fadeInDown"
                  >
                    <li
                      class="area_option row_c"
                      v-for="(item, index) in smsTypeList"
                      :key="index"
                      @click="selectArea(index)"
                    >
                      <div class="area_icon row-c-c">
                        <div v-if="item.icon == 'china'" class="icon_china">
                          <i class="li-icon-star"></i>
                        </div>
                        <i
                          v-else
                          :class="[
                            '_icon',
                            'icon_' + item.icon,
                            'li-icon-' + item.icon,
                          ]"
                        ></i>
                      </div>

                      <div class="mg-l1">{{ item.name }}</div>
                    </li>
                  </ul>

                  <el-input
                    class="user-input"
                    type="text"
                    :maxlength="smsType.type == 1 ? 11 : 15"
                    v-model="forget.form.loginName"
                    placeholder="请输入您的手机号"
                    @input="mobileInput"
                  ></el-input>
                </div>
              </el-form-item>
              <el-form-item prop="mobileCode">
                <div class="input-row">
                  <img
                    class="iconfont"
                    src="@/assets/imgs/login/check_icon.png"
                    alt=""
                    srcset=""
                  />
                  <el-input
                    class="user-input"
                    type="text"
                    v-model="forget.form.mobileCode"
                    placeholder="请输入您的验证码"
                    @input="codeInput"
                  ></el-input>
                  <p
                    :class="[
                      mobileTipsFlag ? 'countDown' : '',
                      'get-mobileCode',
                      'cursor',
                    ]"
                    @click.stop="
                      mobileTipsFlag ? '' : getVerify(forget.form.loginName)
                    "
                  >
                    {{ mobileTips }}
                  </p>
                </div>
              </el-form-item>
              <el-form-item prop="passWord">
                <div class="input-row">
                  <i class="iconfont iconttubiao_password"></i>
                  <el-input
                    :class="['user-input', seepwd ? '' : 'inputPsd']"
                    v-model="forget.form.passWord"
                    placeholder="请输入至少6位新密码"
                    autoComplete="off"
                  ></el-input>
                  <i
                    :class="[
                      'iconfont cursor icon-pwd',
                      seepwd ? 'iconttubiao_display' : 'iconttubiao_hide',
                    ]"
                    @click="changeSeePwd"
                  ></i>
                </div>
              </el-form-item>
              <el-form-item prop="passWord">
                <div class="input-row">
                  <i class="iconfont iconttubiao_password"></i>
                  <el-input
                    :class="['user-input', seepwdNews ? '' : 'inputPsd']"
                    v-model="forget.form.newspassWord"
                    placeholder="再次确认新密码"
                    autoComplete="off"
                  ></el-input>
                  <i
                    :class="[
                      'iconfont cursor icon-pwd',
                      seepwdNews ? 'iconttubiao_display' : 'iconttubiao_hide',
                    ]"
                    @click="seepwdNews = !seepwdNews"
                  ></i>
                </div>
              </el-form-item>
              <div class="bot-group">
                <span>{{ errorMsg }}</span>
              </div>
              <button
                class="primarybtn bigbtn"
                @click.prevent="findPassWord"
                :disabled="
                  (checkMobileFlag && !checkSmsFlag) ||
                  (!checkMobileFlag && checkSmsFlag) ||
                  (!checkMobileFlag && !checkMobileFlag)
                "
              >
                提交
              </button>
            </el-form>
          </div>
          <!-- 验证码登录 -->
          <div v-show="currentIndex == 4" class="img">
            <el-form :model="login.form" class="user-form" ref="loginform">
              <el-form-item prop="loginName">
                <div class="input-row">
                  <div class="input_icon" @click="showAreaSelect()">
                    <div v-if="smsType.icon == 'china'" class="icon_china">
                      <i class="li-icon-star"></i>
                    </div>
                    <i
                      v-else
                      :class="[
                        '_icon',
                        'icon_' + smsType.icon,
                        'li-icon-' + smsType.icon,
                      ]"
                    ></i>
                  </div>

                  <ul
                    v-if="areaSelectVisible"
                    class="area_select animate__animated animate__fadeInDown"
                  >
                    <li
                      class="area_option row_c"
                      v-for="(item, index) in smsTypeList"
                      :key="index"
                      @click="selectArea(index)"
                    >
                      <div class="area_icon row-c-c">
                        <div v-if="item.icon == 'china'" class="icon_china">
                          <i class="li-icon-star"></i>
                        </div>
                        <i
                          v-else
                          :class="[
                            '_icon',
                            'icon_' + item.icon,
                            'li-icon-' + item.icon,
                          ]"
                        ></i>
                      </div>

                      <div class="mg-l1">{{ item.name }}</div>
                    </li>
                  </ul>
                  <el-input
                    class="user-input"
                    type="text"
                    :maxlength="smsType.type == 1 ? 11 : 15"
                    v-model="login.form.loginName"
                    @input="mobileInput"
                    placeholder="请输入您的手机号"
                    autoComplete="off"
                  ></el-input>
                </div>
              </el-form-item>
              <el-form-item prop="mobileCode">
                <div class="input-row">
                  <img
                    class="iconfont"
                    src="@/assets/imgs/login/check_icon.png"
                    alt=""
                    srcset=""
                  />
                  <el-input
                    class="user-input"
                    type="text"
                    v-model="login.form.authcode"
                    placeholder="请输入您的验证码"
                    @input="codeInput"
                  ></el-input>
                  <p
                    :class="[
                      mobileTipsFlag ? 'countDown' : '',
                      'get-mobileCode',
                      'cursor',
                    ]"
                    @click.stop="
                      mobileTipsFlag ? '' : getVerify(login.form.loginName)
                    "
                  >
                    {{ mobileTips }}
                  </p>
                </div>
              </el-form-item>
              <div class="bot-group">
                <span>{{ errorMsg }}</span>
              </div>
              <button
                class="primarybtn bigbtn"
                @click.prevent="toLogin"
                :disabled="
                  (checkMobileFlag && !checkSmsFlag) ||
                  (!checkMobileFlag && checkSmsFlag) ||
                  (!checkMobileFlag && !checkMobileFlag)
                "
              >
                登录
              </button>
            </el-form>
          </div>
          <!-- currentIndex 0:登录，1:注册 3:忘记密码 4:验证码登录  -->
          <div
            v-show="currentIndex == 1 || currentIndex == 0"
            class="mobile-login box-flex"
          >
            <span
              class="span-after cursor"
              v-show="currentIndex == 0"
              @click="switchClick(1)"
              >账号注册</span
            >
            <span
              class="span-after cursor"
              v-show="currentIndex == 0 && switchConfig.isOpenVerifyCode.value"
              @click="switchClick(3)"
              >忘记密码</span
            >
            <span
              class="span-after cursor"
              v-show="currentIndex == 1"
              @click="change(item.index, index, item.title)"
              :key="item.id"
              v-for="(item, index) in list"
              >{{ item.title }}</span
            >
          </div>
          <div class="mobile-login">
            <span
              class="span-after cursor"
              v-show="currentIndex == 3"
              @click="loginClick"
              >返回登录</span
            >
            <span
              class="span-after cursor"
              v-show="currentIndex == 4"
              @click="switchClick(1)"
              >账号注册</span
            >
          </div>
          <!-- 第三方 -->
          <!-- <div class="otherLogin" :style="{'padding-top':currentIndex===1?'3vh':currentIndex == 0?'46px':currentIndex==3?'6vh':'17vh'}"> -->
          <!-- <div class="otherLogin" style="margin-top: 3vh;">
            <div class="otherLogin_tips">
              <P></P>
              <span class="tips_span">其它方式登录</span>
              <P></P>
            </div>
            <div class="otherLogin_list">
              <a href="/mytio/tlogin/5.tio_x" class="icon_a">
                <img class="otherLogin_icon" src="@/assets/imgs/login/icon_oschina.png" alt="">
              </a>
              <a href="/mytio/tlogin/1.tio_x" class="icon_a">
                <img class="otherLogin_icon" src="@/assets/imgs/login/icon_qq.png" alt="">
              </a>
              <a href="/mytio/tlogin/4.tio_x" class="icon_a">
                <img class="otherLogin_icon" style="width:38px;height:38px" src="@/assets/imgs/login/icon_dy.png" alt="">
              </a>
              <a href="/mytio/tlogin/2.tio_x" class="icon_a">
                <img class="otherLogin_icon" src="@/assets/imgs/login/icon_wx.png" alt="">
              </a>
            </div>
            <div class="bothref" v-show="currentIndex !== 3 && currentIndex !== 1">
              使用即代表同意<a href="/appinsert/useragreement.html" target="_blank">《用户服务协议》</a>和<a
                href="/appinsert/privacy.html" target="_blank">《隐私政策》</a>
            </div>
         
          </div> -->
        </div>
      </div>
    </div>
    <!-- 找回密码成功提示弹框 -->
    <Dialog v-show="show">
      <div class="modelbody">
        <div class="model-success-icon">
          <img src="@/assets/imgs/login/success-icon.png" alt="" srcset="" />
        </div>
        <p class="success-password">密码设置成功！</p>
        <p class="success-login">请重新登录</p>
        <div class="button-group">
          <button class="primarybtn" @click="hideDialog">返回登录</button>
        </div>
      </div>
    </Dialog>
    <!-- 注册成功提示弹框 -->
    <Dialog v-show="regshow">
      <div class="modelbody">
        <div class="model-success-icon">
          <img src="@/assets/imgs/login/success-icon.png" alt="" srcset="" />
        </div>
        <p class="success-password">{{ regmsg }}</p>
        <p class="success-register">请重新登录</p>
        <div class="button-group">
          <button class="primarybtn" @click="hideRegDialog">返回登录</button>
        </div>
      </div>
    </Dialog>
    <!-- 图片验证 -->
    <Verify
      @success="verifySuccess"
      :captchaType="'blockPuzzle'"
      :imgSize="{ width: '400px', height: '200px' }"
      ref="verify"
    >
    </Verify>
  </div>
</template>
<script>
import { mapState } from "vuex";
import {user, msgTips, getCommonConfByName, agora} from "@/axios/path";
import CryptoJS from "crypto-js"; //加密
import Dialog from "@/components/Dialog.vue"; //弹框
import Verify from "@/components/verifition/Verify";
import login from "@/mixins/login.js"; //发送消息相关逻辑

export default {
  data() {
    let validateName = (rule, value, callback) => {
      if (!value) {
        return callback(new Error("请输入您的邮箱"));
      } else {
        callback();
      }
    };
    return {
      isPc: true,
      showtype: 1, //1:登录，2:注册，3:找回密码
      seepwd: false, //密码可见状态
      seepwdNews: false, //新密码可见状态
      loading: false,
      carousel: {
        //轮播配置
        arrow: "never",
        interval: 5000,
        height: "75vh",
        trigger: "click",
      },
      login: {
        //登录表单和规则
        form: {
          loginName: "",
          passWord: "",
          authcode: "",
          parentinvitecode: "",
        },
      },
      register: {
        //注册表单和规则
        form: {
          loginName: "",
          passWord: "",
          nick: "",
          mobileCode: "",
          email: "",
          emailPassword: "",
          parentinvitecode: "",
        },
      },
      forget: {
        //找回密码表单和规则
        form: {
          loginName: "",
          mobileCode: "",
          passWord: "",
          newspassWord: "",
          parentinvitecode: "",
        },
      },
      show: false, //弹框显示状态
      regshow: false, //注册成功提示弹框
      regmsg: "", //注册成功提示信息
      sendSuccess: false, //短信是否发送成功
      emailCheckIcon: require("@/assets/imgs/login/agreen_icon.png"),
      emailCheck: false, //勾选绑定已有邮箱状态
      emailSeepwd: false, //邮箱密码可见状态
      tabIndex: 0,
      tabTitle: "欢迎登录",
      changeFlage: false,
      loginType: "im-login",
      loginTypeArr: [
        {
          img: require("@/assets/imgs/login/icon-radio-select.png"),
          isClicl: true,
          title: "IM账号",
          class: "loginType-select",
          type: "im-login",
        },
        {
          img: require("@/assets/imgs/login/icon-radio.png"),
          isClicl: false,
          title: "业务账号",
          type: "bus-login",
          class: "",
        },
      ],
      bgImgSize: {
        0: 458,
        1: 568,
        3: 527,
        4: 438,
      },
      isInvitationCode: false,
      areaSelectVisible: false,
      smsTypeList: [
        {
          type: 1,
          name: "中国",
          icon: "china",
        },
        {
          type: 2,
          name: "国际",
          icon: "global",
        },
      ],
      smsIx: 0,
      smsType: {
        type: 1,
        name: "中国",
        icon: "china",
      },
    };
  },
  computed: {
    ...mapState({
      switchConfig: (state) => state.CommonInfo.switchConfig, // 后台控制开关
    }),
    registerDisabled() {
      let registerType = this.switchConfig.registerType.value;
      let { checkMobileFlag, checkSmsFlag } = this;
      if (registerType == 1) {
        return !this.checkEmailFlag;
      } else if (registerType == 2) {
        if (
          (checkMobileFlag && !checkSmsFlag) ||
          (!checkMobileFlag && checkSmsFlag) ||
          (!checkMobileFlag && !checkMobileFlag)
        ) {
          return true;
        }
      } else if (registerType == 3) {
        return !this.checkAccountFlag;
      }
    },
  },
  components: {
    Dialog: Dialog,
    Verify,
  },
  created() {
    let code = this.$route.query.code;
    if (!this.IsPC() && code) {
      location.href = "https://www.baidu.com?code=" + code;
      return;
    }
    if (code) {
      this.switchClick(1);
      this.register.form.parentinvitecode = code;
    }
    console.log(code);
    // (function() {
    //   var hm = document.createElement("script");
    //   hm.src = "https://res.wx.qq.com/connect/zh_CN/htmledition/js/wxLogin.js";
    //   var s = document.getElementsByTagName("script")[0];
    //   s.parentNode.insertBefore(hm, s);
    // })();
    getCommonConfByName({ name: "isOpenInviteCode" }).then((res) => {
      if (res.ok) {
        this.isInvitationCode = res.data.value == 1 ? true : false;
      }
    });
    console.log("switchConfig", this.switchConfig);
    if (!this.switchConfig.isOpenVerifyCode.value) {
      // 如果后台设置没有开启验证码，那么直接跳过，将验证码默认设为888888
      this.list = [
        {
          title: "密码登录",
          index: 0,
        },
      ];
    }
  },
  mixins: [login],
  methods: {
    /**登录/注册tab切换 */
    change(index, e, title) {
      this.tabIndex = e;
      this.tabTitle = title;
      this.changeFlage = false;
      this.currentIndex = index;
      this.errorMsg = "";
      this.login = this.$options.data().login;
      this.register = this.$options.data().register;
      this.sendSuccess = false;
      this.checkMobileFlag = false;
      this.checkSmsFlag = false;
      this.mobileTipsFlag = false;
      this.mobileTips = "获取验证码";
      this.emailCheck = false;
      this.emailCheckIcon = require("@/assets/imgs/login/agreen_icon.png");
      clearInterval(this.deltimer);
    },
    /* 更改密码可见状态 */
    changeSeePwd() {
      this.seepwd = !this.seepwd;
    },

    /* 登录按钮点击事件 */
    toLogin() {
      var that = this;
      that.debounce(function () {
        if (that.showtype == 1) {
          let rules = that.rules();
          if (!rules) return;
          that.userLogin();
        }
      }, 300);
    },
    /* 注册按钮点击事件 */
    toRegister() {
      let rules = this.rules(),
        checkSmsFlag = this.checkSmsFlag,
        checkMobileFlag = this.checkMobileFlag,
        registerType = this.switchConfig.registerType.value;
      if (!rules) return;

      if (registerType == 1) {
      } else if (registerType == 2) {
        this.checkMobile(2, this.register.form.loginName);
        if (!checkMobileFlag) return;
        this.smsCheck(
          2,
          this.register.form.loginName,
          this.register.form.mobileCode
        );
        if (!checkSmsFlag) return;
      } else if (registerType == 3) {
      }
      this.userRegister();
    },
    /* 手机注册绑定邮箱按钮点击事件*/
    toRegbindemail() {
      let rules = this.rules(),
        checkSmsFlag = this.checkSmsFlag,
        checkMobileFlag = this.checkMobileFlag;
      if (!rules) return;
      this.regbindemail();
    },
    /* 手机注册绑定邮箱 */
    regbindemail() {
      let { loginName, mobileCode, email, emailPassword } = this.register.form;
      //防止build后不能正确拼接的问题
      let key1 = "$",
        key2 = "{",
        key3 = "}";
      let palinstr = `${key1}${key2}${email}${key3}${emailPassword}`;
      let emailpwd = CryptoJS.MD5(
        CryptoJS.enc.Latin1.parse(palinstr)
      ).toString();
      let palinstr1 = `${key1}${key2}${loginName}${key3}${emailPassword}`;
      let phonepwd = CryptoJS.MD5(
        CryptoJS.enc.Latin1.parse(palinstr1)
      ).toString();

      let postdata = {
        phone: loginName,
        emailpwd, //邮箱+密码
        phonepwd, //手机+密码
        code: mobileCode, //手机号验证码
        email,
        agreement: "on",
      };
      user.userRegbindemail(postdata).then((res) => {
        if (res.ok) {
          this.regmsg = res.msg || "注册成功";
          this.regshow = true;
        } else {
          msgTips(res.msg);
        }
      });
    },
    /* 登录 */
    userLogin() {
      this.$refs.loginform.validate(async (valid) => {
        if (valid) {
          let { loginName, passWord, authcode } = this.login.form;
          //防止build后不能正确拼接的问题
          let key1 = "$",
            key2 = "{",
            key3 = "}";
          let palinstr = `${key1}${key2}${loginName}${key3}${passWord}`;
          let pd5 = CryptoJS.MD5(
            CryptoJS.enc.Latin1.parse(palinstr)
          ).toString();
          if (authcode != "") {
            // 验证码登录时，密码置空
            pd5 = "";
          }
          let postdata = { loginname: loginName, pd5: pd5, authcode };
          user.userLogin(postdata).then((res) => {
            if (res.ok) {
              var redirect = this.getQueryString("redirect_uri_after_login");
              if (redirect) {
                window.location.href = redirect;
              } else {
                this.$router.push({ path: "/home" });
              }
            } else {
              msgTips(res.msg);
            }
          });
        }
      });
    },
    /* 注册 */
    userRegister() {
      this.$refs.registerform.validate(async (valid) => {
        if (valid) {
          let { loginName, passWord, nick, mobileCode, parentinvitecode } =
            this.register.form;
          //防止build后不能正确拼接的问题
          let key1 = "$",
            key2 = "{",
            key3 = "}";
          let palinstr = `${key1}${key2}${loginName}${key3}${passWord}`;
          let pwd = CryptoJS.MD5(
            CryptoJS.enc.Latin1.parse(palinstr)
          ).toString();
          let type = this.smsType.type;
          let registerType = this.switchConfig.registerType.value;

          let postdata = {
            loginname: loginName,
            pwd: pwd, //密码
            code: mobileCode, //手机号验证码
            nick,
            agreement: "on",
            parentinvitecode,
            type,
          };
          user.userRegister(postdata, registerType).then((res) => {
            if (res.ok) {
              this.regmsg = res.msg || "注册成功";
              this.regshow = true;
            } else {
              msgTips(res.msg);
            }
          });
        }
      });
    },
    /* 校验新老密码是否匹配 */
    checkPassword() {
      this.rules();
    },
    /* 找回密码 */
    findPassWord() {
      let rules = this.rules();
      if (!rules) return;
      this.smsCheck(6, this.forget.form.loginName, this.forget.form.mobileCode);
      if (!this.checkSmsFlag) return;
      let { loginName, passWord, mobileCode } = this.forget.form;
      let beforeData = { phone: loginName, code: mobileCode };
      let key1 = "$",
        key2 = "{",
        key3 = "}";
      let palinstr = `${key1}${key2}${loginName}${key3}${passWord}`;
      let pwd = CryptoJS.MD5(CryptoJS.enc.Latin1.parse(palinstr)).toString();
      let resetPwd = {
        code: mobileCode,
        phone: loginName,
        phonepwd: pwd,
        emailpwd: "",
      };
      user.userResetPwdBefore(beforeData).then((res) => {
        if (res.ok) {
          if (res.data.email != "") {
            let palinstr_1 = `${key1}${key2}${res.data.email}${key3}${passWord}`;
            let emailpwd = CryptoJS.MD5(
              CryptoJS.enc.Latin1.parse(palinstr_1)
            ).toString();
            resetPwd.emailpwd = emailpwd;
          }
          user.userResetPwd(resetPwd).then((res) => {
            if (res.ok) {
              this.regmsg = res.msg || "密码重置成功";
              this.regshow = true;
            } else {
              this.regshow = true;
            }
          });
        } else {
          msgTips(res.msg);
        }
      });
    },
    /**图形验证成功之后 */
    verifySuccess(e) {
      let currentIndex = this.currentIndex,
        captchaVerification = e.captchaVerification;
      if (currentIndex === 4) {
        this.getMobileCode(3, this.login.form.loginName, captchaVerification); //登录验证
      } else if (currentIndex === 3) {
        this.getMobileCode(6, this.forget.form.loginName, captchaVerification); //找回密码
      } else if (currentIndex === 1) {
        this.getMobileCode(
          2,
          this.register.form.loginName,
          captchaVerification
        ); //注册验证
      }
    },
    /* 隐藏弹框 */
    hideDialog() {
      this.show = false;
      this.currentIndex = 0;
      this.errorMsg = "";
    },
    hideRegDialog() {
      this.regshow = false;
      this.currentIndex = 0;
    },
    loginClick() {
      this.list = [
        {
          title: "密码登录",
          index: 0,
        },
        {
          title: "验证码登录",
          index: 4,
        },
      ];
      this.currentIndex = 0;
      this.tabIndex = 0;
      this.tabTitle = "密码登录";
      this.changeFlage = false;
      (this.sendSuccess = false), //短信是否发送成功
        (this.emailCheckIcon = require("@/assets/imgs/login/agreen_icon.png")),
        (this.emailCheck = false); //勾选绑定已有邮箱状态
      this.emailSeepwd = false; //邮箱密码可见状态
      this.errorMsg = "";
      this.login = this.$options.data().login;
      this.register = this.$options.data().register;
      this.forget = this.$options.data().forget;
      this.seepwd = false;
      this.seepwdNews = false;
    },
    /**切换登录 */
    switchClick(currentIndex) {
      this.currentIndex = currentIndex;
      this.errorMsg = "";
      this.login = this.$options.data().login;
      this.register = this.$options.data().register;
      this.forget = this.$options.data().forget;
      this.sendSuccess = false;
      this.checkMobileFlag = false;
      this.checkSmsFlag = false;
      this.mobileTipsFlag = false;
      this.mobileTips = "获取验证码";
      this.emailCheck = false;
      this.emailCheckIcon = require("@/assets/imgs/login/agreen_icon.png");
      if (!this.switchConfig.isOpenVerifyCode.value) {
        // 如果后台设置没有开启验证码，那么直接跳过，将验证码默认设为888888
        this.codeInput("888888", true);
      }
      clearInterval(this.deltimer);
    },
    /**勾选绑定已有邮箱 */
    emailCheckClick() {
      this.emailCheck = !this.emailCheck;
      this.errorMsg = "";
      if (this.emailCheck) {
        this.emailCheckIcon = require("@/assets/imgs/login/agreen_icon_select.png");
      } else {
        this.emailCheckIcon = require("@/assets/imgs/login/agreen_icon.png");
      }
    },
    /**
     * 获取url的参数值
     * @param {*} name
     */
    getQueryString(name) {
      var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
      var r = window.location.search.substr(1).match(reg);
      if (r != null) {
        var v = r[2];
        return decodeURIComponent(v);
      }
      return null;
    },
    adminlogin() {
      let { loginName, passWord, authcode } = this.login.form;
      if (loginName == "") {
        this.errormsg = "请输入用户名";
        return;
      }
      if (passWord == "") {
        this.errormsg = "请输入密码";
        return;
      }
      this.loading = true;
      //登录加密
      let key1 = "$",
        key2 = "{",
        key3 = "}";
      let palinstr = `${key1}${key2}${loginName}${key3}${passWord}`;
      let pd5 = CryptoJS.MD5(CryptoJS.enc.Latin1.parse(palinstr)).toString();
      let postdata = { username: loginName, password: pd5 };
      user.ndapiLogin(postdata).then((res) => {
        if (res.ok) {
          var redirect = this.getQueryString("redirect_uri_after_login");
          if (redirect) {
            window.location.href = redirect;
          } else {
            this.$router.push({ path: "/home" });
          }
        } else {
          msgTips("账号不存在");
        }
      });
    },
    chooesLoginType(e) {
      let i = e == 0 ? 1 : 0;
      this.loginType = this.loginTypeArr[e].type;
      this.loginTypeArr[
        e
      ].img = require("@/assets/imgs/login/icon-radio-select.png");
      this.loginTypeArr[e].class = "loginType-select";
      this.loginTypeArr[i].img = require("@/assets/imgs/login/icon-radio.png");
      this.loginTypeArr[i].class = "";
    },
    IsPC() {
      var userAgentInfo = navigator.userAgent;
      var Agents = [
        "Android",
        "iPhone",
        "SymbianOS",
        "Windows Phone",
        "iPad",
        "iPod",
      ];
      var flagPc = true;
      for (var v = 0; v < Agents.length; v++) {
        if (userAgentInfo.indexOf(Agents[v]) > 0) {
          flagPc = false;
          break;
        }
      }
      return flagPc;
    },
    // 注册选择区域
    showAreaSelect() {
      this.areaSelectVisible = !this.areaSelectVisible;
    },
    hideAreaSelect() {
      this.areaSelectVisible = false;
    },
    selectArea(ix) {
      if (ix == this.smsIx) {
        this.hideAreaSelect();
        return;
      }
      let item = this.smsTypeList[ix];
      this.smsIx = ix;
      this.smsType = item;
      this.register.form.loginName = "";
      this.checkMobileFlag = false;
      this.hideAreaSelect();
      console.log(this.register.form.loginName);
    },
  },
};
</script>
<style lang="less" scoped>
@import "~@/assets/style/less/login/login.less";

.form-content-left {
  // border-top-left-radius: 20px;
  // border-top-right-radius: 50% 195%;
  // border-bottom-left-radius: 20px;
  // border-bottom-right-radius: 110% 195%;
  // box-shadow: 4px 4px 8px rgba(0, 0, 0, 0.3);
}

.login-content,
.form-content-left {
  transition: height 0.18s;
}
</style>


