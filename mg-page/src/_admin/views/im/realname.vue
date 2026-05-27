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
            <el-table height="700" :data="data.list" v-loading="data.loading"
                :header-cell-style="{ background: $protovar.tbhabg }">
                <el-table-column label="序号" width="80" type="index" :align="$protovar.align"
                    :index="indexMethod"></el-table-column>

                <el-table-column label="用户ID" prop="uid"></el-table-column>
                <el-table-column label="用户昵称" prop="nick"></el-table-column>
                <el-table-column label="手机号码" prop="phone"></el-table-column>
                <el-table-column label="真实姓名" prop="real_name"></el-table-column>
                <el-table-column label="创建时间" prop="update_time" width="200"></el-table-column>
                <el-table-column label="状态">
                    <template slot-scope="scope">
                        <el-tag v-if="scope.row.status == 0" type="info">
                            待处理
                        </el-tag>
                        <el-tag v-if="scope.row.status == 1" type="success">
                            已同意
                        </el-tag>
                        <el-tag v-if="scope.row.status == -1" type="danger">
                            已拒绝
                        </el-tag>

                    </template>
                </el-table-column>

                <el-table-column label="操作" width="150" v-if="authdisable">
                    <template slot-scope="scope">
                        <template>
                            <span v-if="scope.row.status == 0" class="tmopera" @click="setData('edit', scope.row)">审核</span>
                            <a v-else class="c_gray" @click="setData('see', scope.row)">查看</a>
                            <!-- <span class="tmopera waring" @click="handleLog(2, scope.row)" v-auth="'del'">拒绝</span> -->
                        </template>
                    </template>
                </el-table-column>
            </el-table>
        </div>
        <!-- 分页 -->
        <div class="pagecontainer" v-show="data.totalRow > 0">
            <el-pagination layout="total,prev, pager, next,sizes,jumper" background :page-size="data.pageSize"
                :page-sizes="data.pagesizes" :total="data.totalRow" :current-page="data.pageNumber"
                @current-change="handleCurrentChange" @size-change="handleSizeChange">
            </el-pagination>
        </div>
       
        <!-- 新增|编辑框 -->
        <el-dialog :visible.sync="dialog.visible" :close-on-click-modal="false" class="tmdialog" top="10vh"
            :width="$protovar.sgtwidth">
            <p class="tmheader">{{ dialog.title }}</p>
            <el-form :model="dialog.form" :rules="dialog.rules" :label-width="$protovar.fmlabwidth" ref="form"
                :hide-required-asterisk="true" class="dialogform sdialogform" id="form">
                <div class="tmcol">
                    <el-form-item label="用户昵称" prop="nick">
                        <span class="form_row_val">{{ dialog.form.nick }}</span>
                    </el-form-item>
                </div>
                <div class="tmcol">
                    <el-form-item label="手机号码" prop="phone">
                        <span class="form_row_val">{{ dialog.form.phone }}</span>
                    </el-form-item>
                </div>
                <div class="tmcol">
                    <el-form-item label="真实姓名" prop="real_name">
                        <span class="form_row_val">{{ dialog.form.real_name }}</span>
                    </el-form-item>
                </div>
                <div class="tmcol">
                    <el-form-item label="身份证号" prop="real_name">
                        <span class="form_row_val">{{ dialog.form.id_card_number }}</span>
                    </el-form-item>
                </div>

                <div class="tmcol">
                    <el-form-item label="身份证正面" prop="real_name">
                        <img class="avatar" :src="resUrl(dialog.form.id_card_front)" @click="previewImgs(dialog.form.id_card_front)" />
                    </el-form-item>
                </div>

                <div class="tmcol">
                    <el-form-item label="身份证反面" prop="real_name">
                        <img class="avatar" :src="resUrl(dialog.form.id_card_behind)" @click="previewImgs(dialog.form.id_card_behind)" />
                    </el-form-item>
                </div>

            </el-form>
            <div class="tmdialog-footer pb30" v-if="dialog.type != 'see'">
                <button class="primarybtn search" @click="handleLog(-1)">拒绝</button>
                <button class="primarybtn" @click="handleLog(1)" :disabled="loading">通过</button>
            </div>
        </el-dialog>


        <!-- handle -->
        <el-dialog :visible.sync="dialog2.visible" :width="$protovar.dwidth" class="tmdialog" :close-on-click-modal="false">
            <div class="title">{{ dialog2.type == 1 ? '确认通过审核？' : '确认拒绝掉这一项吗' }}</div>
            <div class="tmdialog-footer pb60">
                <button class="primarybtn search" @click="hideDialog('dialog2', 'visible')">取消</button>
                <button class="primarybtn" @click="handleLogDone" :disabled="loading">确认</button>
            </div>
        </el-dialog>

        <el-dialog :visible.sync="dialog3.visible" width="500" class="tmdialog" :close-on-click-modal="false">
            <p class="tmheader">拒绝通过，请填写原因</p>
            <div class="dialogform sdialogform">
                <el-input type="textarea" placeholder="请输入拒绝原因" v-model="dialog.form.mark" maxlength="30" :rows="4"
                    show-word-limit></el-input>
            </div>
            <div class="tmdialog-footer pb60" style="margin-top: 50px;">
                <button class="primarybtn search" @click="hideDialog('dialog3', 'visible')">取消</button>
                <button class="primarybtn" @click="handleLogDone" :disabled="loading">确认</button>
            </div>
        </el-dialog>

        <el-image style="width: 0px;height: 0px;z-index: 2004;" class="my-img" ref="myImg" :src="previewImageObj.src"
            :preview-src-list="previewImageObj.list">
        </el-image>

    </div>
</template>
<script>
import { realname, file, baseTools, msgTips, successTips } from '@_/axios/path';
import { resUrl, btDate } from '@_/utils/common.js';
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
                pageNumber:1,
                pageSize:10,
                totalRow:0,//总条数
                pagesizes:[10,20,30,40]
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
                visible: false,
            },
            typeList: [{ id: 1, label: "系统" }, { id: 2, label: "业务" }],
            loading: false,
            curroute: '',
            previewImageObj: {
                src: '',
                list: []
            }
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
        getData(item) {
            if(item){
                this.data.pageNumber=item;
            }
            this.data.loading=true;
            let {pageNumber,pageSize}=this.data;
            let ptdata={...this.filters,pageNumber,pageSize};
            realname.list(ptdata).then(res => {
                if (res.ok) {
                    let data = res.data;
                    if (data) {
                        this.data.totalRow=data.totalRow;
                        this.data.list = data.list;
                    }
                } else {
                    msgTips(res);
                }
                this.data.loading = false;
            })
        },
         /* 切换分页 */
		handleCurrentChange(val){
            this.data.pageNumber = val;
            this.getData();
        },
        /* 调整每页显示条数 */
        handleSizeChange(val) {
            this.data.pageNumber=1;
            this.data.pageSize=val;
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
                this.dialog3.visible = true
            }
            this.dialog2.type = type;

        },
        handleLogDone() {
            this.loading = true;
            let dialog2 = this.dialog;
            console.log(dialog2);
            let ptdata = { uid: dialog2.form.uid, status: dialog2.form.status, mark: dialog2.form.mark };
            realname.update(ptdata).then(res => {
                if (res.ok) {
                    this.dialog.visible = false;
                    this.dialog2.visible = false;
                    this.dialog3.visible = false;
                    this.getData();
                } else {
                    msgTips(res);
                }
                this.loading = false;
            })
        },
        readQrcode: function (uid) {
            let ptdata = { uid };
            localPurse.getUserPayQrcode(ptdata).then(res => {
                if (res.ok) {
                    let data = res.data;
                    this.previewImageObj.src = data.userPaymentUrl;
                    this.previewImageObj.list = [resUrl(data.userPaymentUrl)];
                    this.$refs.myImg.showViewer = true
                } else {
                    msgTips(res);
                }
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
                this.dialog.title = "实名信息审核";
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

        previewImgs: function (url) {
            this.previewImageObj.src = url;
            this.previewImageObj.list = [resUrl(url)];
            this.$refs.myImg.showViewer = true;
        },


        /**上传 */
        // 自定义上传事件
        handleFileUpload(e, type) {
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
.c_gray{
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
</style>