<template>
  <div class="commonright container">
    <div class="filter">
      <div class="filter-item">
        <label class="filter-label">名称</label>
        <el-input
          type="text"
          clearable
          v-model="filters.searchkey"
          placeholder="圈子名称/圈子ID"
        ></el-input>
      </div>
      <!-- <div class="filter-item">
            <label class="filter-label">状态</label>
            <el-select v-model="filters.type" clearable placeholder="全部">
                <el-option v-for="item in typeList" :key="item.id" :value="item.id" :label="item.label"></el-option>
            </el-select>
          </div> -->
      <div class="filter-btn">
        <button class="primarybtn search" @click="getData(1)">查询</button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="contentpad">
      <el-table
        height="700"
        :data="data.list"
        v-loading="data.loading"
        :header-cell-style="{ background: $protovar.tbhabg }"
      >
        <el-table-column
          label="序号"
          width="80"
          type="index"
          :align="$protovar.align"
          :index="indexMethod"
        ></el-table-column>

        <el-table-column label="圈子ID" prop="show_id"></el-table-column>
        <!-- <el-table-column label="成员数量" prop="show_id"></el-table-column> -->
        <el-table-column label="圈子名称" prop="name"></el-table-column>
        <!-- <el-table-column label="圈子描述" prop="describe"></el-table-column> -->
        <el-table-column label="是否公开" prop="isOpen">
          <template slot-scope="scope">
            <span class="tmopera" v-if="scope.row.is_open == 1">公开</span>
            <span class="c-red" v-else>私有</span>
          </template>
        </el-table-column>
        <el-table-column label="审核加入" prop="isExamine">
          <template slot-scope="scope">
            <span class="tmopera" v-if="scope.row.is_examine == 1">是</span>
            <span class="c-red" v-else>否</span>
          </template>
        </el-table-column>
        <el-table-column label="邀请码状态" prop="isInvite">
          <template slot-scope="scope">
            <span class="tmopera" v-if="scope.row.is_invite == 1">开启</span>
            <span class="c-red" v-else>关闭</span>
          </template>
        </el-table-column>
        <el-table-column label="邀请码数量" prop="invite_num"></el-table-column>
        <el-table-column label="推荐圈子" prop="is_recommend">
          <template slot-scope="scope">
            <el-switch
              :value="scope.row.is_recommend"
              active-color="#13ce66"
              inactive-color="#ff4949"
              :active-value="1"
              :inactive-value="0"
              @change="recommendChange($event, scope.row)"
            >
            </el-switch>
          </template>
        </el-table-column>
        <!-- <el-table-column label="支付方式">
                    <template slot-scope="scope">
                        <span class="wx-color" v-if="scope.row.type == 1">微信支付</span>
                        <span class="c-red" v-else>支付宝支付</span>
                    </template>
                </el-table-column> -->
        <el-table-column
          label="创建时间"
          prop="create_time"
          width="200"
        ></el-table-column>
        <el-table-column label="圈子状态" width="90" :align="$protovar.align">
          <template slot-scope="scope">
            <span
              :class="[
                scope.row.status == 1 ? 'tmdisabledfalse' : 'tmdisabledtrue',
              ]"
              >{{ scope.row.status == 1 ? "正常" : "封禁" }}</span
            >
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" v-if="authdisable">
          <template slot-scope="scope">
            <template>
              <span
                :class="[
                  'tmopera',
                  scope.row.status == 1 ? 'stateDisabled' : 'statesuccess',
                ]"
                v-auth="'disable'"
                @click="operStatus(scope.row)"
                >{{ scope.row.status == 1 ? "封禁" : "解封" }}</span
              >
              <a class="tmopera" @click="setData('edit', scope.row)">管理</a>
              <a class="tmopera" @click="viewMember(scope.row)">成员</a>
              <!-- <span class="tmopera waring" @click="handleLog(2, scope.row)" v-auth="'del'">拒绝</span> -->
            </template>
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

    <!-- 新增|编辑框 -->
    <el-dialog
      :visible.sync="dialog.visible"
      :close-on-click-modal="false"
      class="tmdialog"
      top="10vh"
      width="700px"
    >
      <p class="tmheader">{{ dialog.title }}</p>
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
          <el-form-item label="圈子名称" prop="name">
            <el-input
              class="form_row_val"
              v-model="dialog.form.name"
            ></el-input>
          </el-form-item>
        </div>
        <div class="tmcol">
          <el-form-item label="圈子ID" prop="name">
            <el-input
              class="form_row_val"
              v-model="dialog.form.show_id"
            ></el-input>
          </el-form-item>
        </div>
        <div class="tmcol">
          <el-form-item label="圈子描述" prop="describe">
            <el-input
              class="form_row_val"
              type="textarea"
              rows="10"
              v-model="dialog.form.describe"
            ></el-input>
          </el-form-item>
        </div>
        <div class="tmcol">
          <el-form-item label="是否公开" prop="isOpen">
            <el-switch
              class="form_row_val"
              v-model="dialog.form.is_open"
              active-color="#13ce66"
              inactive-color="#ff4949"
            >
            </el-switch>
            <!-- <span class="form_row_val">{{ dialog.form.isOpen == 1? '公开' :  '私有' }}</span> -->
          </el-form-item>
        </div>
        <div class="tmcol">
          <el-form-item label="审核加入" prop="is_examine">
            <el-switch
              class="form_row_val"
              v-model="dialog.form.is_examine"
              active-color="#13ce66"
              inactive-color="#ff4949"
            >
            </el-switch>
            <!-- <span class="form_row_val">{{ dialog.form.isExamine == 1? '是' :  '否'  }}</span> -->
          </el-form-item>
        </div>
        <div class="tmcol">
          <el-form-item label="邀请码状态" prop="is_invite">
            <el-switch
              class="form_row_val"
              v-model="dialog.form.is_invite"
              active-color="#13ce66"
              inactive-color="#ff4949"
            >
            </el-switch>
            <!-- <span class="form_row_val">{{ dialog.form.isInvite == 1? '是' :  '否'  }}</span> -->
          </el-form-item>
        </div>
        <div class="tmcol" v-if="dialog.form.is_invite == 1">
          <el-form-item label="邀请码数量" prop="invite_num">
            <el-input
              class="form_row_val"
              v-model="dialog.form.invite_num"
            ></el-input>
          </el-form-item>
        </div>

        <div class="tmcol">
          <el-form-item label="圈子封面图" prop="real_name">
            <img
              class="avatar"
              :src="resUrl(dialog.form.avatar)"
              @click="previewImgs(dialog.form.avatar)"
            />
          </el-form-item>
        </div>
      </el-form>
      <div class="tmdialog-footer pb30">
        <button class="primarybtn search" @click="dialog.visible = false">
          取消
        </button>
        <button class="primarybtn" @click="handleLog(1)" :disabled="loading">
          保存
        </button>
        <button class="primarybtn red" @click="delCircle" :disabled="loading">
          解散圈子
        </button>
      </div>
      <el-image
        style="width: 0px; height: 0px; z-index: 2004"
        class="my-img"
        ref="myImg"
        :src="previewImageObj.src"
        :preview-src-list="previewImageObj.list"
      >
      </el-image>
    </el-dialog>

    <!-- handle -->
    <el-dialog
      :visible.sync="dialog2.visible"
      :width="$protovar.dwidth"
      class="tmdialog"
      :close-on-click-modal="false"
    >
      <div class="title">
        {{ dialog2.type == 1 ? "确认通过审核？" : "确认拒绝掉这一项吗" }}
      </div>
      <div class="tmdialog-footer pb60">
        <button
          class="primarybtn search"
          @click="hideDialog('dialog2', 'visible')"
        >
          取消
        </button>
        <button class="primarybtn" @click="handleLogDone" :disabled="loading">
          确认
        </button>
      </div>
    </el-dialog>

    <el-dialog
      :visible.sync="dialog3.visible"
      width="500"
      class="tmdialog"
      :close-on-click-modal="false"
    >
      <p class="tmheader">拒绝通过，请填写原因</p>
      <div class="dialogform sdialogform">
        <el-input
          type="textarea"
          placeholder="请输入拒绝原因"
          v-model="dialog.form.mark"
          maxlength="30"
          :rows="4"
          show-word-limit
        ></el-input>
      </div>
      <div class="tmdialog-footer pb60" style="margin-top: 50px">
        <button
          class="primarybtn search"
          @click="hideDialog('dialog3', 'visible')"
        >
          取消
        </button>
        <button class="primarybtn" @click="handleLogDone" :disabled="loading">
          确认
        </button>
      </div>
    </el-dialog>
    <!-- 成员列表 -->
    <el-dialog
      :visible.sync="dialog4.visible"
      class=""
      :title="memberList ? '共' + memberList.length + '位成员' : '成员列表'"
      :close-on-click-modal="false"
      width="900px"
    >
      <!-- <div class="title">成员列表</div> -->
      <!-- <div class="table_main" style="overflow:auto">
    <li v-for="i in count" class="infinite-list-item">{{ i }}</li>
  </div> -->
      <!-- v-infinite-scroll="loadList('add')" -->
      <div class="table_main" style="overflow: auto">
        <el-table :data="memberList" style="width: 100%">
          <el-table-column prop="uid" label="用户ID" width="120">
          </el-table-column>
          <el-table-column label="头像" width="80">
            <template slot-scope="scope">
              <el-avatar
                :src="resUrl(scope.row.avatar)"
                @click.native="previewImgs(scope.row.avatar)"
              ></el-avatar>
            </template>
          </el-table-column>
          <el-table-column prop="nick" label="姓名" width="150">
          </el-table-column>
          <el-table-column label="身份" width="100">
            <template slot-scope="scope">
              <span :class="['user-color' + scope.row.role]">{{
                userRole[scope.row.role]
              }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="加入时间" width="180">
          </el-table-column>
          <el-table-column label="操作" width="150" v-if="authdisable">
            <template slot-scope="scope">
              <template>
                <!-- <span
                            :class="[
                            'tmopera',
                            scope.row.status == 1 ? 'stateDisabled' : 'statesuccess',
                            ]"
                            v-auth="'disable'"
                            @click="operStatus(scope.row)"
                            >{{ scope.row.status == 1 ? "封禁" : "解封" }}</span> -->
                <!-- <a class="tmopera" @click="setData('edit', scope.row)">查看</a> -->
                <a class="tmopera" @click="openMemberCard(scope.row)"
                  >更换身份</a
                >
                <span
                  class="tmopera waring"
                  @click="delCircleMember(scope.row, scope.$index)"
                  v-auth="'del'"
                  >踢出</span
                >
              </template>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
    <el-dialog
      :visible.sync="dialog5.visible"
      :close-on-click-modal="false"
      class="tmdialog"
      top="10vh"
      width="500px"
    >
      <p class="tmheader">更换成员身份</p>
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
          <el-form-item label="成员名称" prop="name">
            <span class="form_row_val">{{ dialog5.form.nick }}</span>
          </el-form-item>
        </div>
        <div class="tmcol">
          <el-form-item label="成员身份" prop="name">
            <el-select
              class="form_row_val"
              v-model="dialog5.form.role"
              placeholder="请选择"
            >
              <el-option
                v-for="item in userRoleOption"
                :key="item.value"
                :value="item.value"
                :label="item.label"
              ></el-option>
            </el-select>
          </el-form-item>
        </div>
      </el-form>
      <div class="tmdialog-footer pb60" style="margin-top: 50px">
        <button
          class="primarybtn search"
          @click="hideDialog('dialog5', 'visible')"
        >
          取消
        </button>
        <button
          class="primarybtn"
          @click="changeMemberCard"
          :disabled="loading"
        >
          确认
        </button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import {
  circleCenter,
  file,
  baseTools,
  msgTips,
  successTips,
} from "@_/axios/path";
import { resUrl, btDate } from "@_/utils/common.js";
export default {
  data() {
    return {
      count: 0,
      fileList: [],
      filters: {
        searchkey: "",
        status: "",
      },
      data: {
        //数据表格
        loading: false, //表单loading
        list: [], //列表
        pageNumber: 1,
        pageSize: 10,
        totalRow: 0, //总条数
        pagesizes: [10, 20, 30, 40],
      },
      dialog: {
        type: "add",
        title: "",
        visible: false,
        form: {
          logoUrl: "",
        },
        rules: {
          name: [{ required: true, message: "请输入参数名", trigger: "blur" }],
          url: [{ required: true, message: "请输入参数值", trigger: "blur" }],
        },
      },
      dialog2: {
        visible: false,
        item: null,
        type: 1,
      },
      dialog3: {
        visible: false,
      },
      dialog4: {
        visible: false,
      },
      dialog5: {
        visible: false,
        form: {
          name: "",
          role: "",
        },
      },
      typeList: [
        { id: 1, label: "系统" },
        { id: 2, label: "业务" },
      ],
      loading: false,
      curroute: "",
      previewImageObj: {
        src: "",
        list: [],
      },
      memberList: null,
      tabs: [
        {
          name: "",
          type: 1,
          list: [],
          page: 1,
          load: false,
          loadState: 1,
        },
      ],
      c_index: 0,
      currCircleId: -1,
      userRole: {
        1: "圈主",
        2: "管理员",
        3: "成员",
      },
      userRoleOption: [
        { value: 1, label: "圈主" },
        { value: 2, label: "管理员" },
        { value: 3, label: "成员" },
      ],
    };
  },
  mounted() {
    this.curroute = this.$route.path;
    this.getData();
    if (!window.FileReader) {
      alert("暂不支持FileReader, 图片可能将无法回显，但对功能没有影响");
    }
  },
  computed: {
    authdisable() {
      return this.authDisable(["update"]);
    },
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
    "dialog.form.is_invite"(newVal) {
      console.log(newVal);
      if (newVal && this.dialog.form.invite_num <= 0) {
        this.dialog.form.invite_num = 1;
      }
      if(!newVal && this.dialog.form.invite_num > 0){
        this.dialog.form.invite_num = 0;
      }
    },
    "dialog.form.is_open"(newVal) {
      if(!newVal){
        if(!this.dialog.form.is_invite){
          this.dialog.form.is_invite = true;
          if (this.dialog.form.invite_num <= 0) {
            this.dialog.form.invite_num = 1;
          }
        }
      }
    },
  },
  methods: {
    // this.$router.push({path:'/login'});
    resUrl,
    load() {
      this.count += 2;
    },
    /* 序号 */
    indexMethod(index) {
      return index + 1;
    },
    /* 用户数据 */
    getData(item) {
      if (item) {
        this.data.pageNumber = item;
      }
      this.data.loading = true;
      let { pageNumber, pageSize } = this.data;
      let ptdata = { ...this.filters, pageNumber, pageSize };
      circleCenter.circleList(ptdata).then((res) => {
        if (res.ok) {
          let data = res.data;
          if (data) {
            this.data.totalRow = data.totalRow;
            this.data.list = data.list;
          }
        } else {
          msgTips(res);
        }
        this.data.loading = false;
      });
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
    /* 取消弹框 */
    hideDialog(dialog, visible) {
      this[dialog][visible] = false;
    },
    /* 提交表单 */
    formSubmit() {
      // this.$refs['uploadFile'].submit();
    },
    // 处理订单操作
    handleLog(type) {
      this.dialog.form.status = type == 1 ? 1 : -1;
      if (type == 1) {
        let ptdata = JSON.parse(JSON.stringify(this.dialog.form));
        ptdata.isOpen = ptdata.is_open ? 1 : 0;
        ptdata.isExamine = ptdata.is_examine ? 1 : 0;
        ptdata.isInvite = ptdata.is_invite ? 1 : 0;
        ptdata.inviteNum = ptdata.invite_num;
        ptdata.circleId = ptdata.id;
        ptdata.showId = ptdata.show_id;
        circleCenter.updateCircle(ptdata).then((res) => {
          if (res.ok) {
            successTips("修改成功");
            this.getData();
            this.dialog.visible = false;
          } else {
            msgTips(res);
          }
        });

        return;
      } else {
        this.dialog3.visible = true;
      }
      this.dialog2.type = type;
    },
    handleLogDone() {
      this.loading = true;
      let dialog2 = this.dialog;
      console.log(dialog2);
      let ptdata = {
        circleApplyId: dialog2.form.id,
        status: dialog2.form.status,
        refuseReason: dialog2.form.mark,
      };
      circleCenter.update(ptdata).then((res) => {
        if (res.ok) {
          this.dialog.visible = false;
          this.dialog2.visible = false;
          this.dialog3.visible = false;
          this.getData();
        } else {
          msgTips(res);
        }
        this.loading = false;
      });
    },
    readQrcode: function (uid) {
      let ptdata = { uid };
      localPurse.getUserPayQrcode(ptdata).then((res) => {
        if (res.ok) {
          let data = res.data;
          this.previewImageObj.src = data.userPaymentUrl;
          this.previewImageObj.list = [resUrl(data.userPaymentUrl)];
          this.$refs.myImg.showViewer = true;
        } else {
          msgTips(res);
        }
      });
    },
    /* 新增|编辑 */
    setData(type, item) {
      this.dialog.visible = true;
      this.dialog.type = type;
      this.$nextTick(() => {
        this.$refs.form.clearValidate();
      });

      if (type == "add") {
        this.dialog.form = this.$options.data().dialog.form;
        this.dialog.title = "新增";
      }
      if (type == "edit") {
        let data = { ...item };
        data.is_examine = data.is_examine == 1 ? true : false;
        data.is_invite = data.is_invite == 1 ? true : false;
        data.is_open = data.is_open == 1 ? true : false;

        this.dialog.form = data;
        this.dialog.title = "管理圈子信息";
      }
      if (type == "see") {
        let data = { ...item };
        this.dialog.form = data;
        this.dialog.title = "实名信息审核";
      }
    },
    /* 删除 */
    delUser(item) {
      this.currdata = item;
      this.dialog2.visible = true;
    },
    /* 确定删除 */
    sureDelUser() {
      this.loading = true;
      let ptdata = { id: this.currdata.id };
      lowerNav.delete(ptdata).then((res) => {
        if (res.ok) {
          this.dialog2.visible = false;
          this.getData();
        } else {
          msgTips(res);
        }
        this.loading = false;
      });
    },

    previewImgs: function (url) {
      this.previewImageObj.src = url;
      this.previewImageObj.list = [resUrl(url)];
      this.$refs.myImg.showViewer = true;
    },

    /**上传 */
    // 自定义上传事件
    handleFileUpload(e, type) {
      console.log(this.dialog.form);
      this.$refs["form"].validate(async (valid) => {
        if (valid) {
          this.loading = true;
          let type = this.dialog.type;
          let res;
          let postdata = {
            ...this.dialog.form,
          };
          delete postdata.logoUrl;

          var resf = {
            ok: true,
            data: this.dialog.form.logo,
          };
          if (this.dialog.form.logoUrl) {
            let fileData = new FormData();
            fileData.append("logo", this.dialog.form.logo);
            resf = await file.uploadFile(fileData);
          }

          if (resf.ok) {
            postdata.logo = resf.data;
            /* 新增 */
            if (type == "add") {
              res = await lowerNav.add(baseTools.toFormData(postdata));
            }
            /* 修改 */
            if (type == "edit") {
              res = await lowerNav.edit(baseTools.toFormData(postdata));
            }
            if (res.ok) {
              this.dialog.visible = false;
              successTips("保存成功");
              this.dialog.form = this.$options.data().dialog.form;
              this.getData();
            } else {
              msgTips(res);
            }
          } else {
            msgTips(resf);
          }
          this.loading = false;
        } else {
          return false;
        }
      });
    },

    handleFileChange: function (file, dialogKey, formkey) {
      console.log(file, dialogKey, formkey);
      var fr = new FileReader();
      var that = this;
      fr.readAsDataURL(file.raw);
      fr.onload = function (e) {
        that[dialogKey].form[formkey + "Url"] = e.target.result;
        that.$forceUpdate();
      };
      that[dialogKey].form[formkey] = file.raw;
    },
    /* 启用|禁用 */
    operStatus(item) {
      let ptdata = {
        circleId: item.id,
        status: item.status == 1 ? 2 : 1,
      };
      circleCenter.updateCircleStatus(ptdata).then((res) => {
        if (res.ok) {
          successTips("修改成功");
          this.getData();
        } else {
          msgTips(res);
        }
      });
    },
    // 查看成员
    viewMember(item) {
      this.currCircleId = item.id;
      this.loadList("refresh");
      // let ptdata = {
      //     circleId: item.id
      // };
      // circleCenter.circleMemberList(ptdata).then((res) => {
      //     if (res.ok) {
      //         this.memberList = res.data;
      //         this.dialog4.visible = true;

      //     } else {
      //     msgTips(res);
      //     }
      // });
    },
    loadList(type) {
      let tabs = this.tabs;
      let c_index = this.c_index;
      let tab = tabs[c_index];
      let list = tab.list;
      if (type == "refresh") {
        tab.page = 1;
      } else {
        if (tab.loadState == 0) {
          return;
        }
        tab.page++;
      }
      let params = {
        pageNumber: tab.page,
        pageSize: 10,
        circleId: this.currCircleId,
      };
      tab.loadState = 2;
      circleCenter.circleMemberList(params).then((e) => {
        let _list = this.handleList(e.data.length > 0 ? e.data : e.data.list);
        if (!_list) _list = [];
        if (type == "refresh") {
          tab.list = _list;
        } else {
          tab.list = list.concat(_list);
        }
        tab.loadState = _list.length < params.pageSize ? 0 : 1;
        tab.load = true;
        tabs[c_index] = tab;
        this.memberList = tab.list;
        this.tabs = tabs;
        this.dialog4.visible = true;
      });
    },
    handleList(list) {
      console.log(list);
      return list;
      let arr = [];
      for (var i = 0; i < 10; i++) {
        arr.push(list[0]);
      }
      // list.map((item) => {
      // 	item.cover = resUrl(item.cover);
      // });
      return arr;
    },
    recommendChange(val, item) {
      let ptdata = {
        circleId: item.id,
        status: val,
      };
      circleCenter.updateCircleRecommend(ptdata).then((res) => {
        if (res.ok) {
          successTips("修改成功");
          this.getData();
        } else {
          msgTips(res);
        }
      });
    },
    openMemberCard(item) {
      this.dialog5.visible = true;
      this.dialog5.form = JSON.parse(JSON.stringify(item));
    },
    delCircleMember(item, ix) {
      let memberList = this.memberList;
      this.$alert("确认踢出这个成员吗？", "提示", {
        confirmButtonText: "确定",
        callback: (action) => {
          if (action == "confirm") {
            let params = { circleId: this.currCircleId, uids: item.uid };
            circleCenter.delMember(params).then((res) => {
              if (res.ok) {
                successTips("操作成功");
                memberList.splice(ix, 1);
              } else {
                msgTips(res);
              }
            });
          }
        },
      });
    },
    changeMemberCard() {
      let form = this.dialog5.form;
      let params = {
        circleId: this.currCircleId,
        uid: form.uid,
        role: form.role,
      };
      circleCenter.setMemberRole(params).then((res) => {
        if (res.ok) {
          successTips("操作成功");
          this.loadList("refresh");
          this.dialog5.visible = false;
        } else {
          msgTips(res);
        }
      });
    },
    delCircle() {
      this.$alert(
        "确认解散这个圈子吗？选择确定将一并删除这个圈子的所有信息，无法恢复，请谨慎操作！！！",
        "高危操作",
        {
          confirmButtonText: "确定",
          callback: (action) => {
            if (action == "confirm") {
              let params = { circleId: this.dialog.form.id};
              circleCenter.delCircle(params).then((res) => {
                if (res.ok) {
                  successTips("操作成功");
                  this.getData();
                  this.dialog.visible = false;
                } else {
                  msgTips(res);
                }
              });
            }
          },
        }
      );
    },
  },
};
</script>
<style>
.el-upload-dragger {
  width: 178px !important;
}

/* ------------------- */
.header_main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-right: 20px;
}

.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.avatar-uploader .el-upload:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  line-height: 178px !important;
  text-align: center;
}

.avatar {
  width: 80px;
  height: 80px;
  display: block;
}

.imgcol {
  width: 40px;
  height: 40px;
}
.c-red {
  color: #f01d1d;
}
.wx-color {
  color: green;
}
.c_gray {
  color: #666;
}

.el-icon-bank-card {
  font-size: 24px;
}

.form_row_val {
  display: flex;
  min-height: 30px;
  padding: 0px 12px;
  align-items: center;
}
.user-color1 {
  color: #06cf99;
}
.user-color2 {
  color: #e6a23c;
}
.user-color3 {
  color: #666;
}
.table_main {
  width: 100%;
  max-height: 500px;
}
</style>