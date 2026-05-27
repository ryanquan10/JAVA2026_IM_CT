<template>
  <div class="commonright container">
    <!-- <div class="header_main filter">
            <div class="filter-item">
                    <label class="filter-label">配置项</label>
                    <el-input type="text" clearable v-model="filters.searchkey" placeholder="输入名称/参数名"></el-input>
                </div>
                <div class="filter-item">
                    <label class="filter-label">类型</label>
                    <el-select v-model="filters.type" clearable placeholder="全部">
                        <el-option v-for="item in typeList" :key="item.id" :value="item.id" :label="item.label"></el-option>
                    </el-select>
                </div>
            <div class="filter-btn" style="padding-top: 15px;">
                <button class="primarybtn search" @click="getData(1)" >查询</button>
                <button class="primarybtn" @click="setData('add')" v-auth="'add'">新增</button>
            </div>
            <div class="tmopera" @click="dialog3.visible = true">底部导航配置</div>

        </div> -->
    <!-- 数据表格 -->
    <div class="contentpad">
      <div class="operate">
        <!-- <el-button
          v-if="multipleSelection.length > 0"
          class="mg-l1"
          type="primary"
          size="mini"
          @click="successAll()"
          >全部通过</el-button
        > -->
      </div>
      <el-table
        height="700"
        :data="data.list"
        v-loading="data.loading"
        :header-cell-style="{ background: $protovar.tbhabg }"
        @selection-change="handleSelectionChange"
      >
        <!-- <el-table-column type="selection" width="55"></el-table-column> -->
        <el-table-column type="expand">
      <template slot-scope="props">
        <el-form label-position="left" inline class="demo-table-expand">
          <el-form-item label="群聊ID">
            <span>{{ props.row.groupid }}</span>
          </el-form-item>
          <el-form-item label="投诉用户ID">
            <span>{{ props.row.reportUserId }}</span>
          </el-form-item>
          <el-form-item v-if="props.row.adminnick" label="处理人">
            <span>{{ props.row.adminnick }}</span>
          </el-form-item>
        </el-form>
      </template>
    </el-table-column>
        <el-table-column
          label="序号"
          width="80"
          type="index"
          :align="$protovar.align"
          :index="indexMethod"
        ></el-table-column>

        <el-table-column label="群聊名称" prop="groupname"></el-table-column>
        <el-table-column label="群聊状态">
          <template slot-scope="scope">
            <span v-if="scope.row.groupstatus == 1" class="tmopera">正常</span>
            <span v-if="scope.row.groupstatus == 2" class="tmopera waring">已停封</span>
          </template>
        </el-table-column>
        <el-table-column label="投诉用户" prop="reportUserNick"></el-table-column>
        <el-table-column label="投诉内容" prop="reason" width="500px"></el-table-column>
        <el-table-column label="图片">
          <template slot-scope="scope">
            <span v-if="scope.row.imgs.length > 0" class="tmopera" @click="previewImgs(scope.row.imgs)">{{scope.row.imgs.length}}张</span>
            <span v-else class="c-gray">0张</span>
          </template>
        </el-table-column>
        <el-table-column
          label="投诉时间"
          prop="createtime"
          width="200"
        ></el-table-column>
        <el-table-column label="状态">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.status == 2" type="info">待处理</el-tag>
            <el-tag v-if="scope.row.status == 3" type="primary"> 已处理 </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" v-if="authdisable">
          <template slot-scope="scope">
            <template v-if="scope.row.status == 2">
              <!-- <span
                class="tmopera"
                @click="setData('edit', scope.row)"
                >审核</span
              > -->
              <a class="tmopera" @click="handleReport(scope.row.id, 0)"
                >忽略</a
              >
              <span class="tmopera waring" @click="handleReport(scope.row.id, 1)">封停</span>
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
      :width="$protovar.sgtwidth"
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
            <span class="form_row_val">{{ dialog.form.name }}</span>
          </el-form-item>
        </div>
        <div class="tmcol">
          <el-form-item label="圈子描述" prop="describe">
            <span class="form_row_val">{{ dialog.form.describe }}</span>
          </el-form-item>
        </div>
        <div class="tmcol">
          <el-form-item label="是否公开" prop="isOpen">
            <span class="form_row_val">{{
              dialog.form.is_open == 1 ? "公开" : "私有"
            }}</span>
          </el-form-item>
        </div>
        <div class="tmcol">
          <el-form-item label="审核加入" prop="isExamine">
            <span class="form_row_val">{{
              dialog.form.is_examine == 1 ? "是" : "否"
            }}</span>
          </el-form-item>
        </div>
        <div class="tmcol">
          <el-form-item label="是否开启邀请码" prop="isInvite">
            <span class="form_row_val">{{
              dialog.form.is_invite == 1 ? "是" : "否"
            }}</span>
          </el-form-item>
        </div>
        <div class="tmcol" v-if="dialog.form.isInvite == 1">
          <el-form-item label="邀请码数量" prop="inviteNum">
            <span class="form_row_val">{{ dialog.form.invite_num }}</span>
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
        <div class="tmcol">
          <el-form-item label="已拒绝" prop="real_name">
            <span class="" style="line-height: 30px">{{dialog.form.refuse_reason}}</span>
          </el-form-item>
        </div>
        
      </el-form>
      <div class="tmdialog-footer pb30" v-if="dialog.type != 'see'">
        <button class="primarybtn search" @click="handleLog(2)">拒绝</button>
        <button class="primarybtn" @click="handleLog(1)" :disabled="loading">
          通过
        </button>
      </div>
      <el-image
        style="width: 0px; height: 0px; z-index: 2004"
        class="my-img"
        ref="myImg"
        :src="previewImageObj.src"
        :preview-src-list="previewImageObj.list"
      ></el-image>
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
    <!-- <el-image
      style="width: 0px; height: 0px; z-index: 2004"
      class="my-img"
      ref="myImg"
      :src="previewImageObj.src"
      :preview-src-list="previewImageObj.list"
    >
    </el-image> -->
    <el-image ref="imgPreview" style="width: 0px; height: 0px;  z-index: 2004" :preview-src-list="previewImageObj.list"></el-image>
  </div>
</template>
<script>
import {
  circleCenter,
  file,
  baseTools,
  msgTips,
  successTips,
  report
} from "@_/axios/path";
import { resUrl, btDate } from "@_/utils/common.js";
export default {
  data() {
    return {
      fileList: [],
      filters: {
        searchkey: "",
        type: "",
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
      multipleSelection: [],
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
  },
  methods: {
    resUrl,
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
      report.groupList(ptdata).then((res) => {
        if (res.ok) {
          let data = res.data;
          if (data) {
            this.data.totalRow = data.totalRow;
            data.list.map(item=>{
              item.imgs = item.imgs? item.imgs.split(',') : [];
            })
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
    // 处理投诉
    handleReport(id, status){
      let ids = [id];
      let title = status == 1? '确认要封停这个群组吗？' : '忽略这条消息?';
      this.$alert(title, "提示", {
        confirmButtonText: "确定",
        callback: (action) => {
          console.log(action);
          if (action == "confirm") {
            let ptdata = {
                ids: ids.join(","),
                status
            };
            report.groupHandle(ptdata).then((res) => {
              if (res.ok) {
                successTips("操作成功");
                this.getData();
              } else {
                msgTips(res);
              }
            });
          }
        },
      });
    },
    previewReportImg(item){
      console.log(item);
    },

    // 处理订单操作
    handleLog(type) {
      this.dialog.form.status = type == 1 ? 1 : 2;
      if (type == 1) {
        this.dialog2.visible = true;
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
          this.$refs.imgPreview.showViewer = true;
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
        this.dialog.form = data;
        this.dialog.title = "圈子信息审核";
      }
      if (type == "see") {
        let data = { ...item };
        this.dialog.form = data;
        this.dialog.title = "圈子信息审核";
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

    previewImgs: function (imgs) {
      console.log(imgs)
      imgs = imgs.map(img=>{
                return resUrl(img);
            })
            console.log(imgs)
      this.previewImageObj.src = imgs[0];
      this.previewImageObj.list = imgs;
      this.$refs.imgPreview.showViewer = true;
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

    handleSelectionChange(val) {
      this.multipleSelection = val;
    },

    successAll(id) {
      let ids = [];
      if (id) {
        ids = [id];
      } else {
        ids = this.multipleSelection.map((item) => item.id);
      }
      console.log(ids);
      if (ids.length == 0) {
        msgTips("请选择要删除的数据");
        return;
      }
      this.$alert("确认要通过这" + ids.length + "项吗？", "提示", {
        confirmButtonText: "确定",
        callback: (action) => {
          console.log(action);
          if (action == "confirm") {
            let ptdata = {
                circleApplyIds: ids.join(","),
                status: 1,
                refuseReason: '',
            };
            circleCenter.circleArticleDel(ptdata).then((res) => {
              if (res.ok) {
                successTips("操作成功");
                this.getData();
              } else {
                msgTips(res);
              }
            });
          }
        },
      });
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


.demo-table-expand {
    font-size: 0;
    padding-left: 60px;
  }
  .demo-table-expand label {
    width: 90px;
    color: #99a9bf;
  }
  .demo-table-expand .el-form-item {
    margin-right: 0;
    margin-bottom: 0;
    width: 100%;
  }
</style>