<template>
  <div class="commonright container">
    <!-- 筛选 -->
    <!-- <crudOperation :permission="permission" /> -->

    <div class="filter">
      <el-row :gutter="20" justify="space-between">
        <el-col :span="18" :offset="0">
          <div class="filter-item">
            <label class="filter-label">用户</label>
            <el-input
              type="text"
              clearable
              v-model="filters.searchkey"
              placeholder="昵称/账号/ID"
            ></el-input>
          </div>
          <div class="filter-item">
            <label class="filter-label">状态</label>
            <el-select v-model="filters.status" clearable>
              <el-option
                v-for="item in statusSelect"
                :key="item.id"
                :value="item.id"
                :label="item.label"
              ></el-option>
            </el-select>
          </div>
          <div class="filter-btn">
            <button class="primarybtn search" @click="getData(1)">查询</button>
            <button
              class="primarybtn"
              @click="dialog.visible = true"
              v-auth="'add'"
            >
              新增
            </button>

            <button
                class="primarybtn"
                @click="dialogBatch.visible = true"
                v-auth="'add'"
            >
              批量新增
            </button>
            <button class="primarybtn" @click="exportExcel">导出</button>
          </div>
        </el-col>
      </el-row>
    </div>
    <!-- 数据表格 -->
    <div class="contentpad">
      <div class="row-b-c">
        <div class="operate">
          <el-button
              class="mg-l1"
              type="danger"
              size="mini"
              @click="changeInvite(1, true)"
          >开启全部邀请码</el-button>
          <el-button
              class="mg-l1"
              type="danger"
              size="mini"
              @click="changeInvite(0, true)">禁用全部邀请码</el-button>
          <template v-if="ids.length > 0">

            <el-button
              class="mg-l1"
              type="danger"
              size="mini"
              @click="changeInvite(1, false)"
              >开启邀请码</el-button
            >
            <el-button
              class="mg-l1"
              type="danger"
              size="mini"
              @click="changeInvite(0, false)"
              >禁用邀请码</el-button
            >
            <el-button
                class="mg-l1"
                type="warning"
                size="mini"
                @click="batchDisable(1)"
            >启用用户</el-button>
            <el-button
                class="mg-l1"
                type="warning"
                size="mini"
                @click="batchDisable(5)"
            >禁用用户</el-button>
          </template>
        </div>

        <el-button-group class="crud-opts-right">
          <el-button size="mini" icon="el-icon-refresh" @click="getData(1)" />
          <el-popover placement="bottom-end" width="150" trigger="click">
            <el-button slot="reference" size="mini" icon="el-icon-s-grid">
              <i class="fa fa-caret-down" aria-hidden="true" />
            </el-button>
            <el-checkbox
              v-model="allColumnsSelected"
              :indeterminate="allColumnsSelectedIndeterminate"
              @change="handleCheckAllChange"
            >
              全选
            </el-checkbox>
            <el-checkbox
              v-for="item in tableColumns"
              :key="item.label"
              v-model="item.visible"
              @change="handleCheckedTableColumnsChange(item)"
            >
              {{ item.label }}
            </el-checkbox>
          </el-popover>
        </el-button-group>
      </div>

      <el-table
        class="mg-t1"
        :data="data.list"
        v-loading="data.loading"
        :header-cell-style="{ background: $protovar.tbhabg }"
        @selection-change="tableSelect"
        :default-sort="{ prop: 'createtime', order: 'descending' }"
        @sort-change="sortChange"
      >
        <el-table-column type="selection"></el-table-column>
        <el-table-column type="expand">
          <template slot-scope="props">
            <el-form class="other_info" label-position="left" size="mini">
              <el-form-item label="个人邀请码状态">
                <span :class="{ 'c-major': props.row.inviteshow == 1 }">{{
                  props.row.inviteshow == 1 ? "开启" : "关闭"
                }}</span>
              </el-form-item>
              <!-- <el-form-item label="个人邀请码">
                <span>{{props.row.invitecode}}</span>
              </el-form-item> -->
            </el-form>
          </template>
        </el-table-column>
        <el-table-column label="用户" :align="$protovar.align" width="210px">
          <template slot-scope="scope">
            <div class="user">
              <div class="user_avatar">
                <el-image
                  :src="scope.row.avatar"
                  class="imgcol tmopera"
                  @click="seeInfo(scope.row)"
                ></el-image>
                <el-popover
                  v-if="scope.row.is_beautiful_id == 1"
                  trigger="hover"
                  placement="top"
                >
                  <p class="tmopera">靓号：{{ scope.row.beautiful_id }}</p>
                  <p class="">
                    到期时间：{{ scope.row.beautiful_id_expire_time }}
                  </p>
                  <div class="hot_number" slot="reference">靓</div>
                </el-popover>
              </div>
              <div class="userInfo">
                <span class="userInfo-nick color_666"
                  >{{ scope.row.nick }}
                </span>
                <span class="color_999">ID:{{ scope.row.id }} </span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          label="账号/邮箱/手机号"
          :align="$protovar.align"
          width="150"
        >
          <template slot-scope="scope">
            <div class="accountInfo">
              <span><i class="el-icon-user"></i>{{ scope.row.loginname }}</span>
              <span><i class="el-icon-eleme"></i>{{ scope.row.email }}</span>
              <span
                ><i class="el-icon-mobile-phone"></i
                >{{ scope.row.phone || "--" }}</span
              >
            </div>
          </template>
        </el-table-column>
        <el-table-column label="邀请码" :align="$protovar.align" width="100">
          <template slot-scope="scope">
            <div
              class="accountInf pointer"
              @click="getRecommendUserList(scope.row)"
            >
              <span class="c-major bs">{{ scope.row.invitecode }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="三方绑定" width="150" :align="$protovar.align">
          <template slot-scope="scope">
            <div class="thirdTypeBox" v-if="scope.row.thirdtype">
              <img
                v-for="(item, index) in scope.row.thirdtype"
                :key="index"
                :src="thirdArr[item].img"
                :class="[thirdArr[item].css]"
                alt=""
                srcset=""
              />
            </div>
            <span v-else>暂无</span>
          </template>
        </el-table-column>
        <el-table-column label="地区/IP" width="180" :align="$protovar.align">
          <template slot-scope="scope">
            <span class="color_666" v-show="scope.row.region">{{
              scope.row.region
            }}</span
            ><br />
            <a
              class="a_ip"
              v-if="scope.row.ip"
              :href="`https://www.baidu.com/s?wd=${scope.row.ip}&from=t-io`"
              target="_blank"
              >{{ scope.row.ip }}
            </a>
            <span v-else>未知</span>
          </template>
        </el-table-column>
        <el-table-column
          label="注册时间"
          width="210"
          prop="createtime"
          :align="$protovar.align"
          sortable="custom"
        >
          <template slot-scope="scope">
            <span class="color_666">{{ scope.row.createdays }} </span><br />
            <span class="color_999">{{ scope.row.createtime }} </span>
          </template>
        </el-table-column>
        <el-table-column
          label="最近登录"
          width="180"
          prop="lastlogintime"
          :align="$protovar.align"
          sortable="custom"
        >
          <template slot-scope="scope">
            <span class="color_666">{{ scope.row.lastlogindays }} </span><br />
            <span class="color_999">{{ scope.row.lastlogintime }} </span>
          </template>
        </el-table-column>
        <el-table-column label="账号状态" width="90" :align="$protovar.align">
          <template slot-scope="scope">
            <span
              :class="[
                scope.row.status == 1 ? 'tmdisabledfalse' : 'tmdisabledtrue',
              ]"
              >{{ scope.row.status == 1 ? "正常" : "已禁用" }}</span
            >
          </template>
        </el-table-column>
        <el-table-column label="实名状态" width="90" :align="$protovar.align">
          <template slot-scope="scope">
            <span
              :class="[
                scope.row.realnameflag == 1
                  ? 'tmdisabledfalse'
                  : 'tmdisabledtrue',
              ]"
              >{{ scope.row.realnameflag == 1 ? "已实名" : "未实名" }}</span
            >
          </template>
        </el-table-column>
        <el-table-column label="来源"  prop="source" align="center"></el-table-column>
        <el-table-column
          style="text-align: center"
          label="操作"
          fixed="right"
          align="center"
          width="250"
        >
          <template slot-scope="scope">
            <div style="display: flex; justify-content: center">
              <span
                class="tmopera"
                @click="changePrice(scope.row)"
                v-auth="'reset'"
                >修改余额</span
              >
              <span
                :class="[
                  'tmopera',
                  scope.row.status == 1 ? 'stateDisabled' : 'statesuccess',
                ]"
                v-auth="'disable'"
                @click="operStatus(scope.row)"
                >{{ scope.row.status == 1 ? "禁用" : "启用" }}</span
              >
              <el-popover placement="bottom" width="40" trigger="click">
                <div class="other_btns">
                  <el-button
                    class="other_btn"
                    type="danger"
                    size="mini"
                    plain
                    @click="resetPwd(scope.row)"
                    >重置密码</el-button
                  >
                  <el-button
                    class="other_btn mg-t1"
                    :type="scope.row.inviteshow == 1 ? 'danger' : 'success'"
                    size="mini"
                    plain
                    @click="
                      changeInviteSingle(
                        scope.row.id,
                        scope.row.inviteshow == 1 ? 0 : 1
                      )
                    "
                    >{{
                      scope.row.inviteshow == 1 ? "禁用邀请码" : "开启邀请码"
                    }}</el-button
                  >
                  <el-button
                    class="other_btn mg-t1"
                    :type="
                      scope.row.is_beautiful_id == 0 ? 'danger' : 'success'
                    "
                    size="mini"
                    plain
                    @click="openHotNumber(scope.row)"
                    >{{
                      scope.row.is_beautiful_id == 0 ? "设置靓号" : "修改靓号"
                    }}</el-button
                  >
                  <el-button
                    class="other_btn mg-t1"
                    type="danger"
                    size="mini"
                    plain
                    @click="openEditInviteCode(scope.row)"
                    >修改邀请码</el-button
                  >
                </div>
                <el-button
                  class="more_btn"
                  v-auth="'reset'"
                  type="primary"
                  size="mini"
                  slot="reference"
                  >更多操作</el-button
                >
              </el-popover>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <!-- 分页 -->
    <div class="pagecontainer" v-show="data.totalRow > 0">
      <el-pagination
        layout="total,prev, pager, next,sizes,jumper"
        background
        :page-size="data.pageSize"
        :page-sizes="data.pagesizes"
        :total="data.totalRow"
        :current-page="data.pageNumber"
        @current-change="handleCurrentChange"
        @size-change="handleSizeChange"
      >
      </el-pagination>
    </div>
    <!-- 充值/扣除 -->
    <el-dialog
      :visible.sync="dialog4.visible"
      :width="'600px'"
      class="tmdialog"
    >
      <p class="tmheader">充值/扣除</p>
      <el-form
        :model="dialog4.data"
        :label-width="$protovar.fmlabwidth"
        ref="form"
        :hide-required-asterisk="true"
        class="dialogform sdialogform"
        id="form"
      >
        <div class="tmcol">
          <el-form-item label="余额" prop="price">
            <div style="height: 30px; display: flex; align-items: center">
              <span class="">{{ dialog4.data.cny / 100 }} (元)</span>
            </div>
          </el-form-item>
        </div>
        <div class="tmcol">
          <el-form-item label="金额" prop="price">
            <el-input
              v-model="dialog4.data.price"
              name="price"
              placeholder="(单位：元)"
            ></el-input>
          </el-form-item>
        </div>
        <div class="tmcol">
          <el-form-item label="类型" prop="type">
            <div style="height: 30px; display: flex; align-items: center">
              <el-radio-group v-model="dialog4.data.type">
                <el-radio :label="1">充值</el-radio>
                <el-radio :label="0">扣除</el-radio>
              </el-radio-group>
            </div>
          </el-form-item>
        </div>
      </el-form>

      <div class="tmdialog-footer pb30" style="margin-top: 50px">
        <button class="primarybtn search" @click="dialog4.visible = false">
          取消
        </button>
        <button class="primarybtn" @click="sureChangePrice">确认</button>
      </div>
    </el-dialog>
    <!-- 重置密码-->
    <el-dialog
      :visible.sync="dialog3.visible"
      :width="$protovar.dwidth"
      class="tmdialog"
    >
      <div class="title">确认重置当前用户登录密码吗？</div>
      <div class="tmdialog-footer pb60">
        <button
          class="primarybtn search"
          @click="hideDialog('dialog3', 'visible')"
        >
          取消
        </button>
        <button class="primarybtn" @click="sureResetPwd">重置</button>
      </div>
    </el-dialog>
    <!-- 提示成功 -->
    <el-dialog
      :visible.sync="dialog3.visible2"
      :width="$protovar.dwidth"
      class="tmdialog"
    >
      <div class="title">
        <p>
          已成功重置为默认密码：{{ sysparams ? sysparams["resetpwd"] : "" }}
        </p>
        <p>请尽快通知用户进行修改！</p>
      </div>
      <div class="tmdialog-footer pb60">
        <button class="primarybtn" @click="hideDialog('dialog3', 'visible2')">
          确定
        </button>
      </div>
    </el-dialog>
    <!-- 新增|编辑框 -->
    <el-dialog
      :visible.sync="dialog.visible"
      :close-on-click-modal="false"
      class="tmdialog"
      :width="$protovar.sgtwidth"
    >
      <p class="tmheader">生成客户端账户</p>
      <el-form
        :model="dialog.form"
        :rules="dialog.rules"
        :label-width="$protovar.fmlabwidth"
        ref="form"
        :hide-required-asterisk="true"
        class="dialogform sdialogform"
        id="form"
      >
        <div class="tmcol">
          <el-form-item label="昵称" prop="nick">
            <el-input
              v-model="dialog.form.nick"
              name="nick"
              placeholder="请输入昵称"
            ></el-input>
          </el-form-item>
        </div>
        <div class="tmcol">
          <el-form-item label="手机号码" prop="loginname">
            <el-input
              v-model="dialog.form.loginname"
              name="loginname"
              placeholder="请输入手机号码"
            ></el-input>
          </el-form-item>
        </div>
        <div class="tmcol">
          <el-form-item label="密码" prop="pwd">
            <el-input
              v-model="dialog.form.pwd"
              name="pwd"
              placeholder="请输入密码"
            ></el-input>
          </el-form-item>
        </div>
      </el-form>

      <div class="tmdialog-footer pb30" style="margin-top: 50px">
        <button
          class="primarybtn search"
          @click="hideDialog('dialog', 'visible')"
        >
          取消
        </button>
        <button class="primarybtn" @click="createUser" :disabled="loading">
          生成
        </button>
      </div>
    </el-dialog>

    <el-dialog
        :visible.sync="dialogBatch.visible"
        :close-on-click-modal="false"
        class="tmdialog"
        :width="$protovar.sgtwidth"
    >
      <p class="tmheader">批量生成测试账户</p>
      <p>账号: 创建成功会弹窗显示   统一密码: 123456 </p>
      <el-form
          :model="dialogBatch.form"
          :rules="dialogBatch.rules"
          :label-width="$protovar.fmlabwidth"
          ref="form"
          :hide-required-asterisk="true"
          class="dialogform sdialogform"
          id="formBatch"
      >
        <div class="tmcol">
          <el-form-item label="数量" prop="num">
            <el-input
                v-model.number="dialogBatch.form.num"
                name="num"
                placeholder="请输入数量"
                type="number"
            ></el-input>
          </el-form-item>
        </div>
      </el-form>

      <div class="tmdialog-footer pb30" style="margin-top: 50px">
        <button
            class="primarybtn search"
            @click="hideDialogBatch('dialogBatch', 'visible')"
        >
          取消
        </button>
        <button class="primarybtn" @click="createUserBatch" :disabled="loading">
          生成
        </button>
      </div>
    </el-dialog>
    <!-- 设置靓号 -->
    <el-dialog
      :visible.sync="dialog5.visible"
      :close-on-click-modal="false"
      class="tmdialog"
      :width="$protovar.sgtwidth"
    >
      <p class="tmheader">设置靓号</p>
      <el-form
        :model="dialog5.form"
        :rules="dialog5.rules"
        :label-width="$protovar.fmlabwidth"
        ref="form"
        :hide-required-asterisk="true"
        class="dialogform sdialogform"
        id="form"
      >
        <div class="tmcol">
          <el-form-item label="靓号" prop="beautifulId">
            <el-input
              v-model="dialog5.form.beautifulId"
              name="beautifulId"
              placeholder="请输入靓号"
            ></el-input>
          </el-form-item>
        </div>
        <div class="tmcol">
          <el-form-item label="到期时间" prop="expireTime">
            <el-date-picker
              v-model="dialog5.form.expireTime"
              type="date"
              format="yyyy-MM-dd"
              placeholder="选择日期"
            >
            </el-date-picker>
          </el-form-item>
        </div>
      </el-form>
      <div class="tmdialog-footer pb30" style="margin-top: 50px">
        <button
          class="primarybtn search"
          @click="hideDialog('dialog5', 'visible')"
        >
          取消
        </button>
        <button class="primarybtn" @click="setHotNumber" :disabled="loading">
          设置
        </button>
      </div>
    </el-dialog>
    <!-- 直推下级 -->
    <el-dialog
      :visible.sync="dialog6.visible"
      :width="'800px'"
      class="tmdialog"
    >
      <p class="tmheader">{{ dialog6.title }}</p>
      <el-card class="box-card">
        <el-table
          :data="dialog6.userList"
          v-loading="dialog6.loading"
          stripe
          style="width: 100%"
        >
           <el-table-column label="序号" width="80" type="index" :align="$protovar.align"
                    :index="indexMethod"></el-table-column>
          <el-table-column label="用户" :align="$protovar.align" width="210px">
            <template slot-scope="scope">
              <div class="user">
                <div class="user_avatar">
                  <el-image
                    :src="scope.row.avatar"
                    class="imgcol tmopera"
                    @click="seeInfo(scope.row.uid)"
                  ></el-image>
                </div>
                <div class="userInfo">
                  <span class="userInfo-nick color_666"
                    >{{ scope.row.nick }}
                  </span>
                  <span class="color_999">ID:{{ scope.row.uid }} </span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column
            label="余额"
            prop="cny"
            :align="$protovar.align"
            width="210px"
          ></el-table-column>
          <el-table-column
            label="注册时间"
            prop="createTime"
            :align="$protovar.align"
          ></el-table-column>
        </el-table>
      </el-card>
    </el-dialog>

    <!-- 修改邀请码 -->
    <el-dialog
      :visible.sync="dialog7.visible"
      :close-on-click-modal="false"
      class="tmdialog"
      :width="$protovar.sgtwidth"
    >
      <p class="tmheader">修改邀请码</p>
      <el-form
        :model="dialog7.form"
        :rules="dialog7.rules"
        :label-width="$protovar.fmlabwidth"
        :hide-required-asterisk="true"
        class="dialogform sdialogform"
        v-if="dialog7.form"
      >
        <div class="tmcol">
          <el-form-item label="邀请码" prop="invitecode">
            <el-input
              v-model="dialog7.form.invitecode"
              name="invitecode"
              placeholder="请输入新的邀请码"
            ></el-input>
          </el-form-item>
        </div>
      </el-form>
      <div class="tmdialog-footer pb30" style="margin-top: 50px">
        <button
          class="primarybtn search"
          @click="hideDialog('dialog7', 'visible')"
        >
          取消
        </button>
        <button class="primarybtn" @click="editInviteCode" :disabled="loading">
          保存
        </button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import {mapMutations} from "vuex";
import {imuser, mguser, msgTips, successTips} from "@_/axios/path";
import {btDate, resUrl} from "@_/utils/common.js";
import CryptoJS from "crypto-js"; //加密
export default {
  data() {
    return {
      filters: {
        searchkey: "",
        status: "",
        orderby: 1, // (1:createtime, 2:lastlogintime)
        sort: 2, // sort(1:升序, 2:降序)
      },
      statusSelect: [
        { id: 1, label: "正常" },
        { id: 5, label: "禁用" },
      ],
      data: {
        //数据表格
        pageNumber: 1,
        pageSize: 10,
        totalRow: 0, //总条数
        loading: false, //表单loading
        list: [], //列表
        pagesizes: [10, 20, 30, 40],
      },
      currdata: null,
      dialog3: {
        visible: false,
        visible2: false,
      },
      loading: false,
      curroute: "",
      thirdArr: [
        { img: "", css: "" },
        { img: require("@_/assets/img/im/icon_qq.png"), css: "qq" },
        { img: require("@_/assets/img/im/icon_wx.png"), css: "wx" },
        { img: require("@_/assets/img/im/icon_weibo.png"), css: "wb" },
        { img: require("@_/assets/img/im/icon_dy.png"), css: "dy" },
        { img: require("@_/assets/img/im/icon_kaiyuan.png"), css: "kaiyuan" },
      ],
      dialog: {
        type: "add",
        title: "",
        visible: false,
        form: {
          loginname: "",
          pwd: "",
          agreement: "on",
          nick: "",
        },
        rules: {
          name: [{ required: true, message: "请输入参数名", trigger: "blur" }],
          url: [{ required: true, message: "请输入参数值", trigger: "blur" }],
        },
      },
      dialogBatch: {
        type: "add",
        title: "",
        visible: false,
        form: {
          agreement: "on",
          num: 1,
        },
        rules: {
          name: [{ required: true, message: "请输入参数名", trigger: "blur" }],
          url: [{ required: true, message: "请输入参数值", trigger: "blur" }],
        },
      },
      // 充值/扣除
      dialog4: {
        visible: false,
        visible2: false,
        data: {
          price: "",
          type: 1,
          cny: 0,
        },
      },
      ids: [],
      dialog5: {
        visible: false,
        form: {
          beautifulId: "",
        },
        rules: {
          beautifulId: [
            { required: true, message: "请输入设置的靓号", trigger: "blur" },
          ],
        },
      },
      dialog6: {
        visible: false,
        username: "",
        userList: [
          {
            date: "2016-05-02",
            name: "王小虎",
            address: "上海市普陀区金沙江路 1518 弄",
          },
          {
            date: "2016-05-04",
            name: "王小虎",
            address: "上海市普陀区金沙江路 1517 弄",
          },
          {
            date: "2016-05-01",
            name: "王小虎",
            address: "上海市普陀区金沙江路 1519 弄",
          },
          {
            date: "2016-05-03",
            name: "王小虎",
            address: "上海市普陀区金沙江路 1516 弄",
          },
        ],
      },
      dialog7: {
        visible: false,
        form: null,
      },
      // 筛选导出功能
      allColumnsSelected: false,
      allColumnsSelectedIndeterminate: false,
      tableColumns: [
        {
          prop: "id",
          label: "用户ID",
          visible: true,
        },
        {
          prop: "nick",
          label: "昵称",
          visible: true,
        },
        {
          prop: "loginname",
          label: "账号",
          visible: true,
        },
        {
          prop: "phone",
          label: "手机号",
          visible: true,
        },
        {
          prop: "email",
          label: "邮箱",
          visible: true,
        },

        {
          prop: "invitecode",
          label: "邀请码",
          visible: true,
        },
        {
          prop: "parentinvitecode",
          label: "上级邀请码",
          visible: true,
        },

        {
          prop: "province",
          label: "省份",
          visible: true,
        },
        {
          prop: "city",
          label: "地区",
          visible: true,
        },
        {
          prop: "ip",
          label: "IP",
          visible: true,
        },
        {
          prop: "sex",
          label: "性别",
          visible: true,
        },
        {
          prop: "realnameflag",
          label: "实名状态",
          visible: true,
        },
        {
          prop: "_status",
          label: "用户状态",
          visible: true,
        },
        {
          prop: "createtime",
          label: "注册时间",
          visible: true,
        },
        {
          prop: "source",
          label: "来源",
          visible: true,
        }
      ],
    };
  },
  mounted() {
    this.curroute = this.$route.path;
    this.getData();
  },
  watch: {
    $route(to, from) {
      if (to.path == this.curroute) {
        if (this.$protovar.routehasopen != -1 && !to.query.random) {
          return;
        }
        Object.assign(this.$data, this.$options.data());
        this.curroute = this.$route.path;
        this.getData();
      }
    },
  },
  methods: {
    ...mapMutations(["setUserInfoUid", "setUserInfoShow"]),
     /* 序号 */
        indexMethod(index) {
            return (index + 1);
        },
    /* 用户数据 */
    getData(item) {
      if (item) {
        this.data.pageNumber = item;
      }
      this.data.loading = true;
      let { pageNumber, pageSize } = this.data;
      let ptdata = { ...this.filters, pageNumber, pageSize };
      imuser.userList(ptdata).then((res) => {
        if (res.ok) {
          let data = res.data;
          if (data) {
            this.data.totalRow = data.totalRow;
            let list = data.list;
            list.map((item) => {
              item.avatar = resUrl(item.avatar);
              item.province = item.province || "";
              item.city = item.city || "";
              item.region = item.province + " " + item.city;
              item.createdays = btDate(item.createtime);
              item.lastlogindays = btDate(item.lastlogintime);
              if (item.thirdtype) {
                item.thirdtype = item.thirdtype.replace(/11/g, 1);
                item.thirdtype = item.thirdtype.replace(/22/g, 2);
                item.thirdtype = item.thirdtype.replace(/33/g, 3);
                item.thirdtype = item.thirdtype.split(",");
                item.thirdtype = Array.from(new Set(item.thirdtype));
              }
            });
            this.data.list = list || [];
          }
        } else {
          msgTips(res);
        }
        this.data.loading = false;
      });
    },
    sortChange(e) {
      console.log(e);
      let { order, prop } = e;
      if (prop && order) {
        this.filters.orderby = prop == "createtime" ? 1 : 2;
        this.filters.sort = order == "descending" ? 2 : 1;
      } else {
        this.filters.orderby = 1;
        this.filters.sort = 2;
      }
      this.getData();
    },
    /* 切换分页 */
    handleCurrentChange(val) {
      this.data.pageNumber = val;
      this.getData();
    },
    /* 调整每页显示条数 */
    handleSizeChange(val) {
      this.data.pageNumber = 1;
      this.data.pageSize = val;
      this.getData();
    },
    /* 查看 */
    seeInfo(item) {
      let uid = typeof item == "object" ? item.id : item;
      this.setUserInfoUid(uid);
      this.setUserInfoShow(true);
    },
    /* 取消弹框 */
    hideDialog(dialog, visible) {
      this[dialog][visible] = false;
    },
    /* 取消弹框 */
    hideDialogBatch(dialogBatch, visible) {
      this[dialogBatch][visible] = false;
    },
    /* 重置密码 */
    resetPwd(item) {
      this.currdata = item;
      this.dialog3.visible = true;
    },
    /* 确定重置密码 */
    sureResetPwd() {
      let ptdata = { uid: this.currdata.id };
      imuser.resetPwd(ptdata).then((res) => {
        if (res.ok) {
          this.dialog3.visible = false;
          this.dialog3.visible2 = true;
        } else {
          msgTips(res);
        }
      });
    },
    changePrice(item) {
      this.currdata = item;
      this.dialog4.data.price = 0;
      this.dialog4.data.type = 1;
      this.dialog4.data.cny = item.cny ? item.cny : 0;
      this.dialog4.visible = true;
    },
    /* 确定重置密码 */
    sureChangePrice() {
      let data = this.dialog4.data;
      let ptdata = {
        uid: this.currdata.id,
        money: data.price * 100,
        type: data.type,
      };
      mguser.changeUserPrice(ptdata).then((res) => {
        if (res.ok) {
          successTips("修改成功");
          this.getData();
          this.dialog4.visible = false;
        } else {
          msgTips(res);
        }
      });
    },
    /* 启用|禁用 */
    operStatus(item) {
      let ptdata = {
        uid: item.id,
        status: item.status == 1 ? 5 : 1,
      };
      imuser.userDisable(ptdata).then((res) => {
        if (res.ok) {
          successTips("修改成功");
          this.getData();
        } else {
          msgTips(res);
        }
      });
    },
    createUser() {
      let params = JSON.parse(JSON.stringify(this.dialog.form));
      let passWord = params.pwd;
      let loginName = params.loginname;
      if (!params.nick) {
        this.$message.error("昵称不能为空");
        return;
      }
      if (!params.loginname) {
        this.$message.error("手机号码不能为空");
        return;
      }
      if (!params.pwd) {
        this.$message.error("密码不能为空");
        return;
      }
      let key1 = "$",
        key2 = "{",
        key3 = "}";
      let palinstr = `${key1}${key2}${loginName}${key3}${passWord}`;
      params.pwd = CryptoJS.MD5(CryptoJS.enc.Latin1.parse(palinstr)).toString();
      mguser.addUser(params).then((res) => {
        if (res.ok) {
          successTips("账户生成成功");
          this.dialog.visible = false;
          this.getData();
        } else {
          msgTips(res);
        }
      });
    },
    createUserBatch() {
      let params = JSON.parse(JSON.stringify(this.dialogBatch.form));
      if (!params.num) {
        this.$message.error("数量不能为空");
        return;
      }
      mguser.addUserBatch(params).then((res) => {
        if (res.ok) {
          let lose = res.data.lose || [];
          let succeed = res.data.succeed || [];
          this.showBatchResult(lose, succeed);
          this.dialog.visible = false;
          this.getData();
        } else {
          msgTips(res);
        }
      }).catch(error => {
        msgTips('操作失败');
        console.error(error);
      });
      this.hideDialogBatch('dialogBatch', 'visible')
    },

    // 显示批量操作结果 - 更详细的提示方式
    showBatchResult(lose, succeed) {
      let successCount = succeed ? succeed.length : 0;
      let failCount = lose ? lose.length : 0;

      // 显示通知
      if (successCount > 0 && failCount > 0) {
        this.$notify({
          title: '批量生成完成',
          message: `成功: ${successCount} 个，失败: ${failCount} 个`,
          type: 'warning',
          duration: 0 // 不自动关闭
        });
      } else if (successCount > 0) {
        this.$notify({
          title: '批量生成成功',
          message: `成功创建 ${successCount} 个账户`,
          type: 'success',
          duration: 0
        });
      } else if (failCount > 0) {
        this.$notify({
          title: '批量生成失败',
          message: `全部 ${failCount} 个账户创建失败`,
          type: 'error',
          duration: 0
        });
      }
      this.showDetailedResult(lose, succeed);

      // 如果有失败项，显示详细失败信息
      if (failCount > 0 && lose) {
        let failDetails = lose.map((k, v) => {
          return `${k}: ${v || '创建失败'}`;
        }).join('<br/>');

      }
    },

    // 显示详细结果对话框
    showDetailedResult(lose, succeed) {
      let content = '';

      // 添加成功项
      if (succeed && succeed.length > 0) {
        content += `========== 成功项 (${succeed.length} 个) ==========\n`;
        content += succeed.join(',');
        content += '\n';
      }

      console.log(lose)
      // 添加失败项
      if (lose && Object.keys(lose).length > 0) {
        content += `========== 失败项 (${Object.keys(lose).length} 个) ==========\n`;
        Object.entries(lose).forEach(([k, v]) => {
          content += `${k} - 原因: ${v}\n`;
        });
      }

      // 如果有内容则显示对话框
      if (content) {
        this.$alert(
            `<div style="max-height: 500px; overflow: hidden;">
         <textarea
           readonly
           style="width: 100%; height: 400px; font-family: 'Courier New', monospace; font-size: 12px; line-height: 1.5; resize: none; border: 1px solid #ddd; padding: 10px; box-sizing: border-box;"
           onclick="this.select()"
         >${content}</textarea>
       </div>`,
            '批量生成详细结果',
            {
              dangerouslyUseHTMLString: true,
              confirmButtonText: '确定',
              customClass: 'batch-result-dialog'
            }
        );
      }
    },
    tableSelect(e) {
      let ids = e.map((item) => {
        return item.id;
      });
      this.ids = ids;
    },
    changeInviteSingle(id, type) {
      this.changeInvite(type, false, [id]);
    },
    // 邀请码显示开关设置  参数 uids(参数用逗号分开，如: 10000,10001), inviteshow(0:关闭，1:开启)
    // 注：修改所有人的状态，uid参数为0
    changeInvite(type, isAll, _ids) {
      let ids = _ids ? _ids : this.ids;
      let params = {
        uids: isAll ? 0 : ids.join(","),
        inviteshow: type,
      };
      mguser.changeInvite(params).then((res) => {
        if (res.ok) {
          successTips("操作成功");
          this.getData();
        } else {
          msgTips(res);
        }
      });
    },

    batchDisable(status, _ids) {
      let ids = _ids ? _ids : this.ids;
      // if (!ids || ids.length === 0) {
      //   this.$message.warning('请选择要操作的用户');
      //   return;
      // }
      let params = {
        uids: ids.join(","),
        status: status,
      };
      mguser.batchDisable(params).then((res) => {
        if (res.ok) {
          successTips("操作成功");
          this.getData();
        } else {
          msgTips(res);
        }
      });
    },
    openHotNumber(item) {
      this.dialog5.visible = true;
      if (item.is_beautiful_id) {
        this.dialog5.form = {
          beautifulId: item.beautiful_id,
          id: item.id,
          expireTime: new Date(item.beautiful_id_expire_time), // 过期时间
        };
      } else {
        this.dialog5.form = {
          beautifulId: "",
          id: item.id,
          expireTime: "", // 过期时间
        };
      }
    },
    setHotNumber() {
      this.loading = true;
      let form = this.dialog5.form;
      let params = {
        ...form,
      };
      params.expireTime = this.formatDate(params.expireTime);
      mguser.setUserBeautifulId(params).then((res) => {
        this.loading = false;
        if (res.ok) {
          this.dialog5.visible = false;
          successTips("靓号设置成功");
          this.getData();
        } else {
          msgTips(res);
        }
      });
    },
    formatDate(date) {
      function add(num) {
        return num < 10 ? "0" + num : num;
      }
      let year = date.getFullYear();
      let month = date.getMonth() + 1;
      let day = date.getDate();
      return year + "-" + add(month) + "-" + add(day);
    },
    getRecommendUserList(user) {
      this.dialog6.title = user.nick + "的直推下级";
      this.dialog6.visible = true;
      let praams = {
        uid: user.id,
      };
      mguser.getUnderUserInfo(praams).then((res) => {
        if (res.ok) {
          let userList = res.data  ? res.data : [];
          userList.map((item) => {
            item.avatar = resUrl(item.avatar);
          });

          this.dialog6.userList = userList;
          this.dialog6.loading = false;
        }
      });
    },
    // 打开邀请码输入框
    openEditInviteCode(user) {
      this.dialog7.form = {
        uid: user.id,
        invitecode: user.invitecode,
      };
      this.dialog7.visible = true;
    },
    // 修改邀请码
    editInviteCode() {
      let ptdata = this.dialog7.form;
      mguser.updateInvitecode(ptdata).then((res) => {
        if (res.ok) {
          successTips("设置成功");
          this.getData();
          this.dialog7.visible = false;
          this.dialog7.form = null;
        } else {
          msgTips(res);
        }
      });
    },
    // 导出excel
    exportExcel() {
      let { pageNumber, pageSize } = this.data;
      let ptdata = { ...this.filters, pageNumber, pageSize };
      let tableColumns = this.tableColumns;
      tableColumns.map((item) => {
        ptdata[item.prop] = item.visible ? 1 : 0;
      });
      mguser.exportExcal(ptdata).then((res) => {
        if (res.ok) {
          location.href = res.data.download;
        }
      });
    },
    handleCheckAllChange(val) {
      if (val === false) {
        this.allColumnsSelected = true;
        return;
      }
      for (const key in this.tableColumns) {
        this.tableColumns[key].visible = val;
      }
      this.allColumnsSelected = val;
      this.allColumnsSelectedIndeterminate = false;
    },
    handleCheckedTableColumnsChange(item) {
      let totalCount = 0;
      let selectedCount = 0;
      for (const key in this.tableColumns) {
        ++totalCount;
        selectedCount += this.tableColumns[key].visible ? 1 : 0;
      }
      if (selectedCount === 0) {
        msgTips("请至少选择一列");
        this.$nextTick(function () {
          item.visible = true;
        });
        return;
      }
      this.allColumnsSelected = selectedCount === totalCount;
      this.allColumnsSelectedIndeterminate =
        selectedCount !== totalCount && selectedCount !== 0;
    },
    toggleSearch() {
      this.searchToggle = !this.searchToggle;
    },
  },
};
</script>
<style lang="less" scoped>
@import "~@_/assets/style/less/im/usermanage.less";
.el-button + .el-button {
  margin-left: 0;
}
// ----------------

.r_content {
  padding: 30px 40px;
}
.more_btn {
  margin-left: 10px;
}
.other_btns {
  display: flex;
  flex-direction: column;
}
.other_btn {
  flex: 1;
}
.row-e-c {
  display: flex;
  justify-content: flex-end;
  align-items: center;
}
.mg-l1 {
  margin-left: 10px !important;
  margin-top: 0px;
}
.mg-t1 {
  margin-left: 0px;
  margin-top: 6px !important;
}

.operate {
  .title {
    color: #000;
    font-weight: bold;
  }
}
.other_info {
  padding: 6px 10px;
  background-color: rgb(237, 246, 255);
}
.user_avatar {
  position: relative;
}
.hot_number {
  position: absolute;
  left: 0;
  top: 0;
  height: 18px;
  padding: 0px 6px;
  border-radius: 4px;
  line-height: 18px;
  color: #fff;
  font-size: 12px;
  background-color: #f01d1d;
  cursor: pointer;
  // #06CF99
}
.c-red {
  color: #f01d1d;
}
.box-card{
  max-height: 800px;
  overflow-y: scroll;
}
</style>