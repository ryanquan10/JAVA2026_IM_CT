<template>
  <div class="commonright container">
    <div class="divBox">
      <!-- 总充值记录/总提现记录/总结存余额 -->
      <el-row :gutter="24" class="baseInfo">
        <el-col :xs="12" :sm="12" :lg="6" class="ivu-mb">
          <el-card :bordered="false" dis-hover :padding="12">
            <div slot="header" class="acea-row row-between-wrapper">
              <span>总充值记录</span>
              <el-tag class="card_tag" type="success">全平台</el-tag>
            </div>
            <div class="content1" v-if="info.rechargeNum">
              <span class="content-number spBlock mb15"
                ><count-to
                  :start-val="0"
                  :end-val="info.rechargeNum"
                  :duration="2600"
                  class="card-panel-num"
              /></span>
              <!-- <el-divider></el-divider>
                            <div class="acea-row row-between-wrapper">
                                <span class="content-time">积分新增</span>
                                <span>200 </span>
                            </div> -->
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="12" :lg="6" class="ivu-mb">
          <el-card :bordered="false" dis-hover :padding="12">
            <div slot="header" class="acea-row row-between-wrapper">
              <span>总提现记录</span>
              <el-tag class="card_tag" type="success">全平台</el-tag>
            </div>
            <div class="content1" v-if="info.withholdNum">
              <span class="content-number spBlock mb15"
                ><count-to
                  :start-val="0"
                  :end-val="info.withholdNum"
                  :duration="2600"
                  class="card-panel-num"
              /></span>
              <!-- <el-divider></el-divider>
                            <div class="acea-row row-between-wrapper">
                                <span class="content-time">积分新增</span>
                                <span>200 </span>
                            </div> -->
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="12" :lg="6" class="ivu-mb">
          <el-card :bordered="false" dis-hover :padding="12">
            <div slot="header" class="acea-row row-between-wrapper">
              <span>总结存余额</span>
              <el-tag class="card_tag" type="success">全平台</el-tag>
            </div>
            <div class="content1" v-if="info.sysBalance">
              <span class="content-number spBlock mb15"
                ><count-to
                  :start-val="0"
                  :end-val="info.sysBalance"
                  :duration="2600"
                  class="card-panel-num"
              /></span>
              <!-- <el-divider></el-divider>
                            <div class="acea-row row-between-wrapper">
                                <span class="content-time">积分新增</span>
                                <span>200 </span>
                            </div> -->
            </div>
          </el-card>
        </el-col>
      </el-row>
      <div id="echarts" class="echarts"></div>
    </div>
  </div>
</template>
<script>
import { order, file, baseTools, msgTips, successTips } from "@_/axios/path";
import { resUrl, btDate } from "@_/utils/common.js";
import CountTo from "vue-count-to";
import * as echarts from "echarts";
export default {
  components: {
    CountTo,
  },
  data() {
    return {
      info: {
        rechargeNum: "",
        sysBalance: "",
        withholdNum: "",
      },
      data: {
        loading: false,
      },
    };
  },
  created() {
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
    resUrl,
    /* 序号 */
    indexMethod(index) {
      return index + 1;
    },
    /* 用户数据 */
    getData() {
      this.data.loading = true;
      let ptdata = {};
      let that = this;
      order.sysRecords(ptdata).then(function (res) {
        if (res.ok) {
          let data = res.data;
          that.info = data;
          that.drawecharts(res.data.res);
        } else {
          msgTips(res);
        }
        that.data.loading = false;
      });
    },
    drawecharts(data) {
      let currDate = new Date();
      let currYear = currDate.getFullYear();
      let topYear = currYear - 1;
      let arr = [
        [],
        []
      ];
      let sortData;
      let formatData = {
        times: [],
        datas: [
          [],
          []
        ]
      };
      data.sort((a, b)=>{
        return a.month - b.month
      }).map((item)=>{
        if(item.year == currYear){
          arr[1].push(item)
        }else{
          arr[0].push(item)
        }
      })
      sortData = [...arr[0], ...arr[1]];
      sortData.map((item)=>{
        formatData.times.push(item.year + '-' + item.month);
        formatData.datas[0].push(item.rechargeNum);
        formatData.datas[1].push(item.withholdNum);
      })
      // 基于准备好的dom，初始化echarts实例
      let dom = document.getElementById("echarts");
      var myChart = echarts.init(dom);
      // 绘制图表
      var option;

      option = {
        title: {
        //   text: "Stacked Line",
        },
        tooltip: {
          trigger: "axis",
        },
        legend: {
          data: ["总充值记录", "总提现记录"],
        },
        grid: {
          left: "3%",
        //   top: "5%",
          right: "3%",
          bottom: "5%",
          containLabel: true,
        },
        toolbox: {
          feature: {
            saveAsImage: {},
          },
        },
        xAxis: {
          type: "category",
          boundaryGap: false,
          data: formatData.times,
        },
        yAxis: {
        //   type: "value",
        },
        series: [
          {
            name: "总充值记录",
            type: "line",
            // stack: "Total",
            data: formatData.datas[0],
          },
          {
            name: "总提现记录",
            type: "line",
            // stack: "Total",
            data:formatData.datas[1],
          }
        ],
      };
      option && myChart.setOption(option);
    },
    /* 取消弹框 */
    hideDialog(dialog, visible) {
      this[dialog][visible] = false;
    },
    /* 提交表单 */
    formSubmit() {
      // this.$refs['uploadFile'].submit();
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
            resf = await file.uploadFileLocalPurse(fileData);
          }

          if (resf.ok) {
            postdata.img = resf.data;
            postdata.id = 2;
            /* 新增 */
            // PayImgUpdata
            res = await localPurse.PayImgSet(baseTools.toFormData(postdata));
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

    handleFileChange: async function (_file, formkey) {
      console.log(_file, formkey);
      this.loading = true;

      let postdata = this.dialog.form[formkey];
      let resf;
      let fileData = new FormData();
      fileData.append("logo", _file.raw);
      resf = await file.uploadFileLocalPurse(fileData);

      if (resf.ok) {
        postdata.img = resf.data;
        // postdata.id = 2;
        /* 新增 */
        // PayImgUpdata
        let res = await localPurse.PayImgSet(baseTools.toFormData(postdata));
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
      return;

      var fr = new FileReader();
      var that = this;
      fr.readAsDataURL(file.raw);
      fr.onload = function (e) {
        that[dialogKey].form[formkey + "Url"] = e.target.result;
        that.$forceUpdate();
      };
      that[dialogKey].form[formkey] = file.raw;
    },
  },
};
</script>
<style>
.el-upload-dragger {
  width: 178px !important;
}

.dashboard-editor-container {
  padding: 18px 22px 22px 22px;
  background-color: rgb(240, 242, 245);
}

.dashboard-editor-container .chart-wrapper {
  background: #fff;
  padding: 16px 16px 0;
  margin-bottom: 32px;
}

.acea-row ::v-deep.el-avatar--small {
  width: 22px;
  height: 22px;
  line-height: 22px;
}

.checkTime ::v-deep.el-radio__input {
  display: none;
}

.ivu-pl-8 {
  margin-left: 8px;
  font-size: 14px;
}

.divBox {
  /* padding: 0 20px !important; */
}

.dashboard-console-visit ::v-deep.el-card__header {
  padding: 14px 20px !important;
}

.dashboard-console-visit ul li {
  list-style-type: none;
  margin-top: 12px;
}

.ivu-mb {
  margin-bottom: 10px;
}

.content-number {
  font-size: 30px;
}

.content-time {
  font-size: 14px;
  /*color: #8C8C8C;*/
}

.spBlock {
  display: block;
}

/* ------------------- */
.el-form-item__content {
  display: flex;
}

.header_main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-right: 20px;
}

.uploadFile_main {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  margin-right: 20px;
}

.uploadFile_main > span {
  text-align: center;
  line-height: 1em;
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

.footer_btns {
  width: 130px;
  height: 38px;
  margin-left: 100px;
  margin-top: 30px;
}

.card_tag {
  margin-left: 10px;
}
.echarts{
    height: 500px;
    background-color: #fff;
    margin-top: 30px;
    padding-top: 20px;
    max-width: 1236px;
}
</style>