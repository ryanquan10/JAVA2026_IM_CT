<template>
  <div class="commonright container">
    <!-- <div class="header_main filter">
      <div class="filter-btn" style="padding-top: 15px">
        <button class="primarybtn" @click="setData('add')" v-auth="'add'">
          新增
        </button>
      </div>
    </div> -->
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
        <el-table-column label="群聊头像" :align="$protovar.align" width="200">
          <template slot-scope="scope">
            <div class="user">
              <div>
                <el-image
                  :src="scope.row.avatar"
                  @click="seeInfo(scope.row, 'groupInfo')"
                  class="imgcol tmopera"
                >
                  <div slot="error" class="image-slot">
                    <img
                      src="~@_/assets/img/im/avatar.jpg"
                      @click="seeInfo(scope.row, 'groupInfo')"
                      class="error-img"
                    />
                  </div>
                </el-image>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          label="群聊名称"
          prop="name"
          :align="$protovar.align"
        ></el-table-column>
        <el-table-column label="群聊ID" :align="$protovar.align" width="150">
          <template slot-scope="scope">
            <span class="bs c-major pointer">{{ scope.row.groupid }}</span>
          </template>
        </el-table-column>
        <el-table-column label="群聊状态" :align="$protovar.align" width="120">
          <template slot-scope="scope">
            <span
              :class="[
                'statuIcon',
                scope.row.status == 1 ? 'statuGreen' : 'statuRed',
              ]"
            ></span>
            <span>{{ scope.row.status == 1 ? "正常" : "已封停" }}</span>
          </template>
        </el-table-column>
        <el-table-column label="开启状态" :align="$protovar.align" width="120">
          <template slot-scope="scope">
            <!-- <span>{{ scope.row.isOpen == 1 ? "开启" : "已关闭" }}</span> -->
            <el-switch
              v-model="scope.row.isopen"
              @change="switchChange(scope.row)"
            ></el-switch>
            <!-- <el-switch
              v-model="scope.row.isOpen"
              active-color="#13ce66"
              inactive-color="#ff4949"
              @change="isOpenChange"
            >
            </el-switch> -->
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" v-if="authdisable">
          <template slot-scope="scope">
            <span class="tmopera" @click="setData('edit', scope.row)"
              >编辑</span
            >
            <span
              class="tmopera waring"
              @click="delUser(scope.row)"
              v-auth="'del'"
              >删除</span
            >
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
          <el-form-item label="IP地址" prop="ip">
            <el-input v-model="dialog.form.uid" name="uid"></el-input>
            <p class="">示例：192.168.32.33</p>
          </el-form-item>
        </div>
        <div class="tmcol">
          <el-form-item label="默认消息" prop="msg">
            <el-input
              type="textarea"
              v-model="dialog.form.msg"
              name="msg"
              cols="20"
              rows="10"
            ></el-input>
          </el-form-item>
        </div>
      </el-form>
      <div class="tmdialog-footer pb30">
        <button
          class="primarybtn search"
          @click="hideDialog('dialog', 'visible')"
        >
          取消
        </button>
        <button class="primarybtn" @click="formSubmit" :disabled="loading">
          保存
        </button>
      </div>
    </el-dialog>

    <!-- 删除 -->
    <el-dialog
      :visible.sync="dialog2.visible"
      :width="$protovar.dwidth"
      class="tmdialog"
      :close-on-click-modal="false"
    >
      <div class="title">确定删除这一项吗？</div>
      <div class="tmdialog-footer pb60">
        <button
          class="primarybtn search"
          @click="hideDialog('dialog2', 'visible')"
        >
          取消
        </button>
        <button class="primarybtn" @click="sureDelUser" :disabled="loading">
          删除
        </button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { defaultGroups, msgTips, successTips } from "@_/axios/path";
import { resUrl } from "@_/utils/common.js";
export default {
  data() {
    return {
      fileList: [],
      filters: {
        searchkey: "",
        type: "",
      },
      //数据表格
      data: {
        pageNumber: 1,
        pageSize: 10,
        totalRow: 0, //总条数
        loading: false, //表单loading
        list: [], //列表
        pagesizes: [10, 20, 30, 40],
      },
      dialog: {
        type: "add",
        title: "",
        visible: false,
        form: {},
        rules: {
          name: [{ required: true, message: "请输入用户id", trigger: "blur" }],
          msg: [
            { required: true, message: "请输入用户默认消息", trigger: "blur" },
          ],
        },
      },
      dialog2: {
        visible: false,
      },
      loading: false,
    };
  },
  mounted() {
    this.getData();
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
    // pageNumber, pageSize
    /* 用户数据 */
    getData(item) {
      if (item) {
        this.data.pageNumber = item;
      }
      this.data.loading = true;
      let { pageNumber, pageSize } = this.data;
      let ptdata = { ...this.filters, pageNumber, pageSize };
      defaultGroups.list(ptdata).then((res) => {
        if (res.ok) {
          let data = res.data;
          if (data) {
            this.data.totalRow = data.totalRow;
            let list = data.list;
            list.map((item) => {
              item.avatar = resUrl(item.avatar);
              item.isopen = item.isopen == 1? true : false;
            });
            this.data.list = list || [];
          }
        } else {
          msgTips(res);
        }
        this.data.loading = false;
      });
    },
    /* 取消弹框 */
    hideDialog(dialog, visible) {
      this[dialog][visible] = false;
    },
    /* 提交表单 */
    formSubmit() {
      this.$refs["form"].validate(async (valid) => {
        if (valid) {
          this.loading = true;
          let type = this.dialog.type;
          let res;
          let postdata = {
            ...this.dialog.form,
          };

          // let fileData = new FormData();
          /* 新增 */
          if (type == "add") {
            res = await defaultGroups.add(baseTools.toFormData(postdata));
          }
          /* 修改 */
          if (type == "edit") {
            res = await defaultGroups.edit(baseTools.toFormData(postdata));
          }
          if (res.ok) {
            this.dialog.visible = false;
            successTips("保存成功");
            this.dialog.form = this.$options.data().dialog.form;
            this.getData();
          } else {
            msgTips(res);
          }
          this.loading = false;
        } else {
          return false;
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
        this.dialog.title = "编辑";
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
      let ptdata = { id: this.currdata.groupid };
      defaultGroups.delete(ptdata).then((res) => {
        if (res.ok) {
          this.dialog2.visible = false;
          this.getData();
        } else {
          msgTips(res);
        }
        this.loading = false;
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
    switchChange(item) {
      let ptdata = {
        // id: item.id,
        groupid: item.groupid,
        isOpen: item.isopen?1 : 0,
      };
      defaultGroups.edit(ptdata).then((res) => {
        if (res.ok) {
          this.getData();
        } else {
          msgTips(res);
        }
      });
    },
  },
};
</script>
<style lang="less" scoped>
@import "~@_/assets/style/less/im/groupChatManage.less";
@import "~@_/assets/style/less/im/groupmanage.less";
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
  width: 178px;
  height: 178px;
  display: block;
}

.imgcol {
  width: 40px;
  height: 40px;
}

.right_box {
  display: flex;
  align-items: center;
}

.row_c {
  display: flex;
  align-items: center;
}
</style>