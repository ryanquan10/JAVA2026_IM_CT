<template>
  <div class="commonright container">
    <div class="filter">
          <div class="filter-item">
            <label class="filter-label">关键字</label>
            <el-input
              type="text"
              clearable
              v-model="filters.searchkey"
              placeholder="圈子名称/用户ID"
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
      <div class="operate">
        <el-button
          v-if="multipleSelection.length > 0"
          class="mg-l1"
          type="danger"
          size="mini"
          @click="deleteArticle()"
          >全部删除</el-button
        >
      </div>
      <el-table
        ref="multipleTable"
        height="700"
        :data="data.list"
        v-loading="data.loading"
        :header-cell-style="{ background: $protovar.tbhabg }"
        @selection-change="handleSelectionChange"
      >
        <el-table-column
          label="序号"
          width="80"
          type="index"
          :align="$protovar.align"
          :index="indexMethod"
        ></el-table-column>

        <el-table-column type="selection" width="55"></el-table-column>

        <el-table-column
          label="文字内容"
          width="300"
          prop="content"
        ></el-table-column>
        <el-table-column label="图片资源" width="120" prop="content">
          <template slot-scope="scope">
            <div class="article_cover">
              <el-image 
                v-if="scope.row.imgs"
                style="width: 80px; height: 80px"
                :src="scope.row.imgs[0]" 
                flt="cover"
                :preview-src-list="scope.row.imgs">
              </el-image>
              <span v-else>无</span>
              <!-- <img v-if="scope.row.imgs.length > 0" class="img" :src="scope.row.imgs[0]" alt=""> -->
            </div>
          </template>
        </el-table-column>
        <el-table-column label="圈子" prop="circleName"></el-table-column>
        <el-table-column label="发布城市" prop="city"></el-table-column>
        <!-- <el-table-column label="是否公开" prop="isOpen">
                    <template slot-scope="scope">
                        <span class="wx-color" v-if="scope.row.isOpen == 1">公开</span>
                        <span class="tmopera" v-else>私有</span>
                    </template>
                </el-table-column> -->
        <el-table-column label="发布者" prop="nick"></el-table-column>
        <el-table-column
          label="发布时间"
          prop="create_time"
          width="180"
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
              <!-- <span v-if="scope.row.status == 0" class="tmopera" @click="setData('edit', scope.row)">审核</span> -->
              <a class="tmopera" @click="setData('see', scope.row)">查看</a>
              <a class="tmopera c-red" @click="deleteArticle(scope.row.id)"
                >删除</a
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
          <el-form-item label="帖子内容" prop="content">
            <span class="form_row_val">{{ dialog.form.content }}</span>
          </el-form-item>
        </div>
        <div class="tmcol">
          <el-form-item label="帖子图片" prop="real_name">
            <div v-if="dialog.form.imgs" class="form_row_val imgs">
              <el-image 
                class="avatar"
                :src="dialog.form.imgs[ix]" 
                flt="cover"
                :preview-src-list="dialog.form.imgs"
                v-for="(img, ix) in dialog.form.imgs"
                :key="ix">
              </el-image>
              <!-- <img
               
                class="avatar"
                :src="img"
                @click="previewImgs(img)"
                
              /> -->
            </div>
            <span v-else class="form_row_val">暂无</span>
          </el-form-item>
        </div>
        <div class="tmcol">
          <el-form-item label="所属圈子" prop="name">
            <span class="form_row_val">{{ dialog.form.circleName }}</span>
          </el-form-item>
        </div>
        <div class="tmcol">
          <el-form-item label="所属城市" prop="name">
            <span class="form_row_val">{{ dialog.form.city }}</span>
          </el-form-item>
        </div>

        <el-card class="box-card" v-if="dialog.comments">
          <div slot="header" class="clearfix">
            <span>评论列表</span>
            <el-button
              style="float: right; padding: 3px 0"
              type="text"
              @click="dialog.comments_visble = !dialog.comments_visble"
              >共{{ dialog.comments.length }}条评论</el-button
            >
          </div>
          <ul class="comment_list" v-if="dialog.comments_visble">
            <li class="comment_item">
              <!-- <div class="zan_list row-b-c" v-if="item.likes.length > 0">
                <i class="li-icon-zan size-n c-major"></i>
                <div class="zan_main _row wrap auto">
                  <div
                    class="zan_item c-major"
                    v-for="like in item.likes"
                    :key="like.id"
                  >
                    {{ like.nick }}
                  </div>
                </div>
              </div> -->
              <ul
                class="comment_list"
                v-if="dialog.comments"
                @click.stop="&quot;#&quot;;"
              >
                <div v-for="(cm, ix) in dialog.comments" :key="ix">
                  <li class="comment_item _row del">
                    <template v-if="cm.parent">
                      <div class="name c-major">
                        {{ cm.remarkName ? cm.remarkName : cm.nick }}
                      </div>
                      <div class="name">回复</div>

                      <div class="name c-major">
                        {{
                          cm.parent.remarkName
                            ? cm.parent.remarkName
                            : cm.parent.nick
                        }}
                      </div>
                    </template>
                    <div v-else class="name c-major">
                      {{ cm.remarkName ? cm.remarkName : cm.nick }}
                    </div>

                    <div>：</div>
                    <div class="" v-html="handleToHtml(cm.content)"></div>
                    <div
                      class="delete row-c-c pointer"
                      @click.stop="deleteComment(ix)"
                    >
                      <i class="el-icon-close"></i>
                    </div>
                  </li>
                </div>
              </ul>
            </li>
          </ul>
          <div
            class="row-c-c"
            @click="dialog.comments_visble = !dialog.comments_visble"
          >
            <i v-show="!dialog.comments_visble" class="el-icon-arrow-down"></i>
            <i v-show="dialog.comments_visble" class="el-icon-arrow-up"></i>
          </div>
        </el-card>
        <el-card class="box-card" v-if="dialog.likes">
          <div slot="header" class="clearfix">
            <span>点赞列表</span>
            <el-button
              style="float: right; padding: 3px 0"
              type="text"
              @click="dialog.likes_visble = !dialog.likes_visble"
              >共{{ dialog.likes.length }}条点赞</el-button
            >
          </div>
          <el-table v-show="dialog.likes_visble" :data="dialog.likes" style="width: 100%">
            <el-table-column prop="uid" label="用户ID"> </el-table-column>
            <el-table-column prop="nick" label="用户昵称"> </el-table-column>
            <el-table-column prop="like_time" label="点赞时间" width="180"></el-table-column>
          </el-table>
          <div
            class="row-c-c"
            @click="dialog.likes_visble = !dialog.likes_visble"
          >
            <i v-show="!dialog.likes_visble" class="el-icon-arrow-down"></i>
            <i v-show="dialog.likes_visble" class="el-icon-arrow-up"></i>
          </div>
        </el-card>
        <!-- <div class="container">
          <p class="container_title">评论列表</p>
          <div class="container_body"></div>
        </div>
        <div class="container">
          <p class="container_title">点赞列表</p>
          <div class="container_body"></div>
        </div> -->
      </el-form>
      <div class="tmdialog-footer pb30" v-if="dialog.type != 'see'">
        <button class="primarybtn search" @click="handleLog(-1)">拒绝</button>
        <button class="primarybtn" @click="handleLog(1)" :disabled="loading">
          通过
        </button>
      </div>
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
import { resUrl, btDate, messageEmoji } from "@_/utils/common.js";

export default {
  data() {
    return {
      fileList: [],
      filters: {
        searchkey: "",
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
        comments: null,
        likes: null,
        comments_visble: false,
        likes_visble: false,
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
      circleCenter.circleArticleList(ptdata).then((res) => {
        if (res.ok) {
          let data = res.data;
          if (data) {
            this.data.totalRow = data.totalRow;
            this.data.list = this.handleList(data.list);
            console.log(this.data.list);
          }
        } else {
          msgTips(res);
        }
        this.data.loading = false;
      });
    },
    handleList(list) {
      list.map((item) => {
        if (item.img_url) {
          let imgs = item.img_url.split(",");
          item.imgs = imgs.map((item) => resUrl(item));
        } else {
          item.imgs = null;
        }
      });
      return list;
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
        this.dialog.title = "实名信息审核";
      }
      if (type == "see") {
        let data = { ...item };
        this.dialog.form = data;
        this.dialog.title = "帖子详情";
        this.getCommentAndZan(item.id);
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
    toggleSelection(rows) {
      if (rows) {
        rows.forEach((row) => {
          this.$refs.multipleTable.toggleRowSelection(row);
        });
      } else {
        this.$refs.multipleTable.clearSelection();
      }
    },
    handleSelectionChange(val) {
      this.multipleSelection = val;
    },

    deleteArticle(id) {
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
      this.$alert("确认要删除这" + ids.length + "项吗？", "提示", {
        confirmButtonText: "确定",
        callback: (action) => {
          console.log(action);
          if (action == "confirm") {
            let ptdata = { articleIds: ids.join(",") };
            circleCenter.circleArticleDel(ptdata).then((res) => {
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
    getCommentAndZan(articleId) {
      let params = { articleId };
      circleCenter.articleDetails(params).then((res) => {
        if (res.ok) {
          let { comments, likes } = res.data;
          this.handleCommentAndZan(comments.list, likes.list);
        } else {
          msgTips(res);
        }
      });
      //   circleCenter.circleArticleDel(params).then((res) => {
      //     if (res.ok) {
      //       this.dialog.comments = res.data;
      //     } else {
      //       msgTips(res);
      //     }
      //   });
    },
    handleCommentAndZan(comments, likes) {
      //   let comments = this.dialog.comments;
      //   let likes = this.dialog.likes;
      // 处理评论
      comments.map((comment) => {
        comment.parent = null;
        if (comment.pid) {
          let parent = comments.find((cm) => cm.id == comment.pid);
          comment.parent = parent ? parent : null;
        }
      });
      this.dialog.comments = comments;
      this.dialog.likes = likes;
    },
    // 处理内容拼接emoji、处理链接等
    handleToHtml(content) {
      if (!content) {
        return "";
      }
      let html = "";
      // let bodyc = "[微笑]";
      html = content.replace(/\n/g, "<br>");
      //表情编译-begin
      html = messageEmoji(html);
      //网址处理
      let reg = /(http:\/\/|https:\/\/)((\w|=|\?|\.|\/|&|-|:)+)/g;
      html = html.replace(
        reg,
        "<a href='$1$2' target='_blank' class='texthttp'>$1$2</a>"
      );
      return html;
    },
    deleteComment(ix) {
      let comments = this.dialog.comments;
      this.$alert("确认要删除这条评论吗？", "提示", {
        confirmButtonText: "确定",
        callback: (action) => {
          if (action == "confirm") {
            let params = { commentId: comments[ix].id };
            circleCenter.articleCommentDel(params).then((res) => {
              if (res.ok) {
                successTips("删除成功");
                comments.splice(ix, 1);
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
  margin-right: 4px;
  margin-bottom: 4px;
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

.el-icon-bank-card {
  font-size: 24px;
}

.form_row_val {
  display: flex;
  min-height: 30px;
  padding: 0px 12px;
  align-items: center;
}
.article_cover {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  width: 80px;
  height: 80px;
  background-color: #f6f6f6;
}
.article_cover .img {
  width: 100%;
  height: 100%;
  border-radius: 8px;
}
.article_cover .dot {
  padding: 8px 10px;
  font-size: 12px;
  color: #fff;
  background-color: #06cf99;
}
.c-red {
  color: #f01d1d;
}
.container {
  padding: 10px;
}
.container .container_title {
  font-size: 16px;
  color: #000;
}
.container .container_body {
  padding: 10px;
}
</style>
<style lang="less" scoped>
.comment_list {
  padding: 4px 0 10px;
  .comment_item {
    position: relative;
    padding: 4px;
    line-height: 20px;
    &:hover {
      background-color: #fff;
    }
    .delete {
      position: absolute;
      bottom: 0;
      right: 0;
      display: none;
      height: 28px;
      padding: 0 10px;
      color: #30d2b2;
      &:hover {
        font-weight: bold;
      }
    }
    .name {
      margin: 0 2px;
      white-space: nowrap;
    }
  }
  .del {
    &:hover {
      .delete {
        display: flex;
      }
    }
  }
}
.zan_list + .send_main,
.comment_list + .send_main {
  margin-top: 10px;
  &::before {
    position: absolute;
    top: -10px;
    content: "";
    width: 100%;
    height: 1px;
    background-color: #e1e1e1;
  }
}
.c-major {
  color: #30d2b2;
}
._row {
  display: flex;
}
.row-c-c {
  display: flex;
  justify-content: center;
  align-items: center;
}
.box-card {
  margin-bottom: 10px;
}
.imgs{
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}
</style>