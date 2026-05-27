<template>
  <div class="commonright container">
    <div class="filter">
          <div class="filter-item">
            <label class="filter-label">关键字</label>
            <el-input
              type="text"
              clearable
              v-model="filters.searchkey"
              placeholder="相册名称/用户ID"
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
        <el-table-column label="相册名称" prop="name"></el-table-column>
        <el-table-column label="资源总数">
          <template slot-scope="scope">
            <span class="tmopera" v-if="scope.row.photo_num > 0"
              >{{ scope.row.photo_num }}（张）</span
            >
            <span class="c-red" v-if="scope.row.photo_num == 0">0（张）</span>
          </template>
        </el-table-column>
        <el-table-column label="是否公开" prop="permission">
          <template slot-scope="scope">
            <span class="wx-color" v-if="scope.row.permission == 1">公开</span>
            <span class="tmopera" v-if="scope.row.permission == 2">私密</span>
            <span class="c-red" v-if="scope.row.permission == 3">密码访问</span>
          </template>
        </el-table-column>
        <el-table-column label="访问密码" prop="password" width="160">
          <template slot-scope="scope">
            <div v-if="scope.row.password" class="row_c" @click="showPwd(scope.row)">
                <span v-if="scope.row.password_show" class="tmopera" >{{scope.row.password}}</span>
                <i class="icon_pwds" v-show="scope.row.password_show"></i>
                <i class="icon_pwdh" v-show="!scope.row.password_show"></i>
            </div>
            <span class="" v-else>无</span>
          </template>
        </el-table-column>
        <el-table-column label="用户ID" prop="uid"></el-table-column>
        <el-table-column label="用户名称" prop="nick"></el-table-column>
        <el-table-column
          label="创建时间"
          prop="update_time"
          width="200"
        ></el-table-column>
        <!-- <el-table-column label="状态">
                    <template slot-scope="scope">
                        <el-tag v-if="scope.row.status == 0" type="info">
                            待处理
                        </el-tag>
                        <el-tag v-if="scope.row.status == 1" type="success">
                            已同意
                        </el-tag>
                        <el-tag v-if="scope.row.status == 2" type="danger">
                            已拒绝
                        </el-tag>

                    </template>
                </el-table-column> -->

        <el-table-column label="操作" width="150" v-if="authdisable">
          <template slot-scope="scope">
            <template>
              <span class="tmopera" @click="setData('see', scope.row)"
                >查看</span
              >
              <span class="tmopera c-red" @click="delAlbum(scope.row)"
                >删除</span
              >
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
      width="800px"
    >
      <p class="tmheader">{{ dialog.title }}</p>
      <div class="imgs" v-if="dialog.photos && dialog.photos.length > 0">
        <div class="res_box" v-for="(img, index) in dialog.photos" :key="index">
          <el-image
            v-if="img.type == 1"
            class="img"
            :src="img.img"
            lazy
            fit=“cover” 
            :preview-src-list="dialog.photos.map(item=>item.img)"
          >
          </el-image>
          <video v-if="img.type == 2" class="video" :src="img.img" @click="playVideo(img.img)"></video>
          <i v-if="img.type == 2" class="el-icon-video-play video_play" @click="playVideo(img.img)"></i>
        </div>
        
      </div>
      <!-- <el-empty :image-size="200"></el-empty> -->
      <!-- <el-form
        :model="dialog.form"
        :rules="dialog.rules"
        :label-width="$protovar.fmlabwidth"
        ref="form"
        :hide-required-asterisk="true"
        class="dialogform sdialogform"
        id="form"
      >
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
      <div class="tmdialog-footer pb30" v-if="dialog.type != 'see'">
        <button class="primarybtn search" @click="handleLog(-1)">拒绝</button>
        <button class="primarybtn" @click="handleLog(1)" :disabled="loading">
          通过
        </button>
      </div> -->
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

    <el-image
      style="width: 0px; height: 0px; z-index: 2004"
      class="my-img"
      ref="myImg"
      :src="previewImageObj.src"
      :preview-src-list="previewImageObj.list"
    >
    </el-image>
    <div class="video-mask" v-if="videoOptions.sources">
       <video-player class="video-play" :options="videoOptions"/>
       <i class="video-close el-icon-circle-close" @click="closeVideo"></i>
    </div>
    
  </div>
</template>
<script>
import { album, file, baseTools, msgTips, successTips } from "@_/axios/path";
import { resUrl, btDate } from "@_/utils/common.js";
import VideoPlayer from "@_/components/VideoPlayer.vue";
export default {
  components: {
		VideoPlayer
	},
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
        photos: null,
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
      videoOptions: {
				autoplay: true,
				controls: true,
				sources: null
			}
      
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
      album.albumList(ptdata).then((res) => {
        if (res.ok) {
          let data = res.data;
          if (data) {
            this.data.totalRow = data.totalRow;
            data.list.map((item)=>{
                item.password_show = false;
            });
            console.log(data.list);
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
          this.$refs.myImg.showViewer = true;
        } else {
          msgTips(res);
        }
      });
    },
    /* 新增|编辑 */
    setData(type, item) {
      this.dialog.photos = null;
      this.dialog.visible = true;
      this.dialog.type = type;
      // this.$nextTick(() => {
      //   this.$refs.form.clearValidate();
      // });

      if (type == "add") {
        this.dialog.form = this.$options.data().dialog.form;
        this.dialog.title = "新增";
      }
      if (type == "edit") {
        let data = { ...item };
        this.dialog.form = data;
        this.dialog.title = "实名信息审核";
      }
      if (type == "see") {
        let data = { ...item };
        this.dialog.form = data;
        this.dialog.title = "相册详情";
        this.getPhotos(item.id);
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
    // 获取所有图片
    getPhotos(albumId) {
      let params = { albumId };
      album.photos(params).then((res) => {
        if (res.ok) {
            let photos =  res.data.albumPhotos;
            photos.map(item=>{
                item.img = resUrl(item.img);
            })
          this.dialog.photos = photos;
          console.log(this.dialog.photos);
        } else {
          msgTips(res);
        }
      });
    },
    // 删除相册
    delAlbum(item) {
      let albumIds = [item.id];
      this.$alert("确认要删除这个相册吗？", "提示", {
        confirmButtonText: "确定",
        callback: (action) => {
          if (action == "confirm") {
            let params = { albumIds: albumIds.join(",") };
            album.albumDel(params).then((res) => {
              if (res.ok) {
                successTips("删除成功");
                this.getData();
              } else {
                msgTips(res);
              }
            });
          }
        },
      });
    },

    // 删除图片
    deletePhoto(item) {
      let photoIds = [item.id];
      this.$alert("确认要删除这张图片吗？", "提示", {
        confirmButtonText: "确定",
        callback: (action) => {
          if (action == "confirm") {
            let params = { photoIds: photoIds.join(",") };
            album.delPhotos(params).then((res) => {
              if (res.ok) {
                successTips("删除成功");
                this.getData();
              } else {
                msgTips(res);
              }
            });
          }
        },
      });
    },
    showPwd(item){
        item.password_show = !item.password_show;
    },
    playVideo(videoUrl){
      this.videoOptions.sources = [
        {
          src: videoUrl
				}
      ]
    },
    closeVideo(){
      this.videoOptions.sources = null;
    }

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
  width: 178px;
  height: 178px;
  display: block;
}

.imgcol {
  width: 40px;
  height: 40px;
}

.wx-color {
  color: green;
}
.c_gray {
  color: #666;
}
.c-red {
  color: #f01d1d;
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
.imgs{
  display: flex;
  flex-wrap: wrap;
  padding: 10px;
}
.img{
  width: 100px;
  height: 100px;
  margin-right: 10px;
  margin-bottom: 10px;
}
.video{
  width: 100px;
  height: 100px;
  margin-right: 10px;
  margin-bottom: 10px;
  background-color: #f1f1f1;
}
.res_box{
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
}
.video_play{
  position: absolute;
  font-size: 30px;
  color: #fff;
}
.row_c{
    display: flex;
    align-items: center;
}
.icon_pwdh, .icon_pwds{
    cursor: pointer;
    padding: 12px;
    background: url(~@_/assets/img/login/pwdh.png) no-repeat right  center;
    background-size: 24px 24px !important;
    margin-left: 10px
}
.icon_pwds{
    background: url(~@_/assets/img/login/pwds.png) no-repeat right  center;
    background-size: 12px 12px;
}
</style>