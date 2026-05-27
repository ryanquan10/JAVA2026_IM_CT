<template>
    <div class="commonright container">
        <div class="header_main filter">
            <!-- <div class="filter-item">
                    <label class="filter-label">配置项</label>
                    <el-input type="text" clearable v-model="filters.searchkey" placeholder="输入名称/参数名"></el-input>
                </div>
                <div class="filter-item">
                    <label class="filter-label">类型</label>
                    <el-select v-model="filters.type" clearable placeholder="全部">
                        <el-option v-for="item in typeList" :key="item.id" :value="item.id" :label="item.label"></el-option>
                    </el-select>
                </div> -->
            <div class="filter-btn" style="padding-top: 15px;">
                <!-- <button class="primarybtn search" @click="getData()" >查询</button> -->
                <button class="primarybtn" @click="setData('add')" v-auth="'add'">新增</button>
            </div>
            <!-- <div class="tmopera" @click="dialog3.visible = true">底部导航配置</div> -->

        </div>
        <!-- 数据表格 -->
        <div class="contentpad">
            <el-table height="700" :data="data.list" v-loading="data.loading"
                :header-cell-style="{ background: $protovar.tbhabg }">
                <el-table-column label="序号" width="80" type="index" :align="$protovar.align"
                    :index="indexMethod"></el-table-column>


                <el-table-column label="订单编号" prop="serialnumber"></el-table-column>
                
               
                <el-table-column label="姓名" prop="name"></el-table-column>
                <el-table-column label="账号" prop="paymentAccount"></el-table-column>
                <el-table-column label="数量">
                    <template slot-scope="scope">
                        <span>{{(scope.row.amount / 100).toFixed(2)}}</span>
                    </template>
                </el-table-column>
                <el-table-column label="支付方式">
                    <template slot-scope="scope">
                        <span class="wx-color" v-if="scope.row.type == 1">微信支付</span>
                        <span class="tmopera" v-if="scope.row.type == 2">支付宝支付</span>
                        <span class="ul-color" v-if="scope.row.type == 3">USDT支付</span>
                         <span class="color4" v-if="scope.row.type == 4">银行卡支付</span>
                    </template>
                </el-table-column>
                <el-table-column label="创建时间" prop="createTime" width="200"></el-table-column>
                <el-table-column label="状态">
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
                </el-table-column>

                <el-table-column label="操作" width="150" v-if="authdisable">
                    <template slot-scope="scope">
                        <template v-if="scope.row.status == 0">
                            <span class="tmopera" @click="handleLog(1, scope.row)">确认</span>
                            <span class="tmopera waring" @click="handleLog(2, scope.row)" v-auth="'del'">拒绝</span>
                        </template>
                    </template>
                </el-table-column>
            </el-table>
        </div>
        <!-- 新增|编辑框 -->
        <el-dialog :visible.sync="dialog.visible" :close-on-click-modal="false" class="tmdialog"
            :width="$protovar.sgtwidth">
            <p class="tmheader">{{ dialog.title }}</p>
            <el-form :model="dialog.form" :rules="dialog.rules" :label-width="$protovar.fmlabwidth" ref="form"
                :hide-required-asterisk="true" class="dialogform sdialogform" id="form">
                <div class="tmcol">
                    <el-form-item label="名称" prop="name">
                        <el-input v-model="dialog.form.name" name="name"></el-input>
                        <input type="hidden" v-if="dialog.type == 'edit'" name="name" v-model="dialog.form.name" />
                    </el-form-item>
                </div>
                <div class="tmcol">
                    <el-form-item label="URL" prop="url">
                        <el-input v-model="dialog.form.url" name="url"></el-input>
                    </el-form-item>
                </div>
                <div class="tmcol">
                    <el-form-item label="图片" prop="value">
                        <el-upload ref="uploadFile" class="avatar-uploader" drag action="#" :multiple="false"
                            :auto-upload="false" :show-file-list="false"
                            :on-change="($event) => handleFileChange($event, 'dialog', 'logo')">
                            <img v-if="dialog.form.logoUrl || dialog.form.logo"
                                :src="dialog.form.logoUrl ? dialog.form.logoUrl : resUrl(dialog.form.logo)"
                                class="avatar">
                            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
                        </el-upload>

                    </el-form-item>
                </div>

            </el-form>
            <div class="tmdialog-footer pb30">
                <button class="primarybtn search" @click="hideDialog('dialog', 'visible')">取消</button>
                <button class="primarybtn" @click="handleFileUpload" :disabled="loading">保存</button>
            </div>
        </el-dialog>


        <!-- handle -->
        <el-dialog :visible.sync="dialog2.visible" :width="$protovar.dwidth" class="tmdialog" :close-on-click-modal="false">
            <div class="title">{{ dialog2.type == 1 ? '确认已完成了吗' : '确认拒绝掉这一项吗' }}</div>
            <div class="tmdialog-footer pb60">
                <button class="primarybtn search" @click="hideDialog('dialog2', 'visible')">取消</button>
                <button class="primarybtn" @click="handleLogDone" :disabled="loading">确认</button>
            </div>
        </el-dialog>

    </div>
</template>
<script>
import { localPurse, file, baseTools, msgTips, successTips } from '@_/axios/path';
import {resUrl,btDate} from '@_/utils/common.js';
export default {
    data() {
        return {
            fileList: [],
            filters: {
                searchkey: '',
                type: ''
            },
            data: {//数据表格
                loading: false,//表单loading
                list: [],//列表
            },
            dialog: {
                type: 'add',
                title: '',
                visible: false,
                form: {
                    logoUrl: ''
                },
                rules: {
                    name: [
                        { required: true, message: "请输入参数名", trigger: "blur" }
                    ],
                    url: [
                        { required: true, message: "请输入参数值", trigger: "blur" }
                    ],
                }
            },
            dialog2: {
                visible: false,
                item: null,
                type: 1
            },
            dialog3: {
                type: 'add',
                title: '',
                visible: false,
                form: {
                    logoUrl: ''
                },
                rules: {
                    name: [
                        { required: true, message: "请输入参数名", trigger: "blur" }
                    ],
                    url: [
                        { required: true, message: "请输入参数值", trigger: "blur" }
                    ],
                }
            },
            typeList: [{ id: 1, label: "系统" }, { id: 2, label: "业务" }],
            loading: false,
            curroute: ''
        }
    },
    mounted() {
        this.curroute = this.$route.path;
        this.getData();
        if (!window.FileReader) {
            alert('暂不支持FileReader, 图片可能将无法回显，但对功能没有影响');
        }

    },
    computed: {
        authdisable() {
            return this.authDisable(['update']);
        }
    },
    watch: {
        '$route'(to, from) {
            if (to.path == this.curroute) {
                if (this.$protovar.routehasopen != -1 && !to.query.random) {
                    return;
                }
                Object.assign(this.$data, this.$options.data());
                this.curroute = this.$route.path;
                this.getData();
            }
        }
    },
    methods: {
        resUrl,
        /* 序号 */
        indexMethod(index) {
            return (index + 1);
        },
        /* 用户数据 */
        getData() {
            this.data.loading = true;
            let ptdata = {};
            localPurse.getLogFormRecharge(ptdata).then(res => {
                if (res.ok) {
                    let data = res.data;
                    if (data) {
                        this.data.list = data;
                    }
                } else {
                    msgTips(res);
                }
                this.data.loading = false;
            })
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
        handleLog(type, item) {
            this.dialog2.visible = true;
            this.dialog2.type = type;
            this.dialog2.item = item;
        },
        handleLogDone() {
            this.loading = true;
            let dialog2 = this.dialog2;
            console.log(dialog2);
            let ptdata = { id: dialog2.item.id, status: dialog2.type };
            localPurse.confirmLog(ptdata).then(res => {
                if (res.ok) {
                    this.dialog2.visible = false;
                    this.getData();
                } else {
                    msgTips(res);
                }
                this.loading = false;
            })
        },
        /* 新增|编辑 */
        setData(type, item) {
            this.dialog.visible = true;
            this.dialog.type = type;
            this.$nextTick(() => {
                this.$refs.form.clearValidate();
            })

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
            lowerNav.delete(ptdata).then(res => {
                if (res.ok) {
                    this.dialog2.visible = false;
                    this.getData();
                } else {
                    msgTips(res);
                }
                this.loading = false;
            })
        },


        /**上传 */
        // 自定义上传事件
        handleFileUpload(e, type) {
            // ;
            console.log(this.dialog.form);
            this.$refs['form'].validate(async (valid) => {
                if (valid) {
                    this.loading = true;
                    let type = this.dialog.type;
                    let res;
                    let postdata = {
                        ...this.dialog.form,
                    }
                    delete postdata.logoUrl;


                    var resf = {
                        ok: true,
                        data: this.dialog.form.logo
                    }
                    if (this.dialog.form.logoUrl) {
                        let fileData = new FormData();
                        fileData.append('logo', this.dialog.form.logo);
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
                that[dialogKey].form[formkey + 'Url'] = e.target.result;
                that.$forceUpdate();
            };
            that[dialogKey].form[formkey] = file.raw;

        },

    },
}
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
    border-color: #409EFF;
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
.ul-color{
    color: orange
}
.color4 {
  color: rgb(15, 134, 245);
}
</style>