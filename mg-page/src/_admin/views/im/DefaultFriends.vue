<template>
    <div class="commonright container">
        <div class="header_main filter">
            <div class="filter-btn" style="padding-top: 15px;">
                <button class="primarybtn" @click="setData('add')" v-auth="'add'">新增</button>
            </div>
            <div class="right_box">
                <div class="tmopera" @click="dialog3.visible = true">默认好友功能配置</div>
            </div>
        </div>
        <!-- 数据表格 -->
        <div class="contentpad">
            <el-table height="700" :data="data.list" v-loading="data.loading"
                :header-cell-style="{ background: $protovar.tbhabg }">
                <el-table-column label="序号" width="80" type="index" :align="$protovar.align"
                    :index="indexMethod"></el-table-column>

                <el-table-column label="用户ID" prop="uid"></el-table-column>
                <el-table-column label="默认消息" prop="msg"></el-table-column>



                <el-table-column label="操作" width="150" v-if="authdisable">
                    <template slot-scope="scope">
                        <span class="tmopera" @click="setData('edit', scope.row)">编辑</span>
                        <span class="tmopera waring" @click="delUser(scope.row)" v-auth="'del'">删除</span>
                    </template>
                </el-table-column>
            </el-table>
        </div>
        <form id="form1" enctype="multipart/form-data">
            <input name="123" value="dialog.form.name" />
        </form>
        <!-- 新增|编辑框 -->
        <el-dialog :visible.sync="dialog.visible" :close-on-click-modal="false" class="tmdialog"
            :width="$protovar.sgtwidth">
            <p class="tmheader">{{ dialog.title }}</p>
            <el-form :model="dialog.form" :rules="dialog.rules" :label-width="$protovar.fmlabwidth" ref="form"
                :hide-required-asterisk="true" class="dialogform sdialogform" id="form">
                <div class="tmcol">
                    <el-form-item label="用户ID" prop="id">
                        <el-input v-model="dialog.form.uid" name="uid"></el-input>
                        <!-- <input type="hidden" v-if="dialog.type == 'edit'" name="name" v-model="dialog.form.name" /> -->
                    </el-form-item>
                </div>
                <div class="tmcol">
                    <el-form-item label="默认消息" prop="msg">
                        <el-input type="textarea" v-model="dialog.form.msg" name="msg" cols="20" rows="10"></el-input>
                    </el-form-item>
                </div>
            </el-form>
            <div class="tmdialog-footer pb30">
                <button class="primarybtn search" @click="hideDialog('dialog', 'visible')">取消</button>
                <button class="primarybtn" @click="formSubmit" :disabled="loading">保存</button>
            </div>
        </el-dialog>


        <!-- 删除 -->
        <el-dialog :visible.sync="dialog2.visible" :width="$protovar.dwidth" class="tmdialog" :close-on-click-modal="false">
            <div class="title">确定删除这一项吗？</div>
            <div class="tmdialog-footer pb60">
                <button class="primarybtn search" @click="hideDialog('dialog2', 'visible')">取消</button>
                <button class="primarybtn" @click="sureDelUser" :disabled="loading">删除</button>
            </div>
        </el-dialog>


        <!-- 新增|编辑框 -->
        <el-dialog :visible.sync="dialog3.visible" :close-on-click-modal="false" class="tmdialog"
            :width="$protovar.sgtwidth">
            <p class="tmheader">{{ dialog3.title }}</p>
            <el-form :model="dialog3.form" :rules="dialog3.rules" :label-width="$protovar.fmlabwidth" ref="configForm"
                :hide-required-asterisk="true" class="dialogform sdialogform" id="configForm">
                <div class="tmcol">
                    <el-form-item label="是否开启默认好友:" prop="isRotation" class="row_c">
                        <el-switch v-model="dialog3.form.isRotation" @change="switchChange"></el-switch>
                    </el-form-item>
                </div>
                <div class="tmcol">
                    <el-form-item label="默认好友轮询次数:" prop="point">
                        <el-input v-model="dialog3.form.point" type="number" name="point"></el-input>
                    </el-form-item>
                </div>
            </el-form>
            <div class="tmdialog-footer pb30">
                <button class="primarybtn search" @click="hideDialog('dialog3', 'visible')">取消</button>
                <button class="primarybtn" @click="configFormSubmit">保存</button>
            </div>
        </el-dialog>



    </div>
</template>
<script>
import { defaultFriends, file, baseTools, msgTips, successTips } from '@_/axios/path';
import {resUrl,btDate} from '@_/utils/common.js';
export default {
    data() {
        // 自定义校验方法  此处必须加callback形成闭环
        const checkNumber = (rule, value, callback) => {
            if (!value) {
                callback(new Error("请输入默认好友轮询次数"));
            } else if (value < 0) {
                callback(new Error("默认好友轮询次数不能小于0"));
            } else {
                callback()
            }
        };
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

                },
                rules: {
                    name: [
                        { required: true, message: "请输入用户id", trigger: "blur" }
                    ],
                    msg: [
                        { required: true, message: "请输入用户默认消息", trigger: "blur" }
                    ],
                }
            },
            dialog2: {
                visible: false
            },
            loading: false,
            dialog3: {
                title: '默认好友功能配置',
                visible: false,
                form: {
                    id: -1,
                    point: '',
                    isRotation: false,
                },
                rules: {
                    point: [
                        { required: true, message: "请输入用户id", trigger: "blur" },
                        { validator: checkNumber, trigger: "blur" }
                    ]
                }
            },
            curroute: '',


        }
    },
    mounted() {
        this.curroute = this.$route.path;
        this.getData();
        this.getDefaultConfig();

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
            let ptdata = { searchkey: this.filters.searchkey, type: this.filters.type };
            defaultFriends.list(ptdata).then(res => {
                if (res.ok) {
                    let data = res.data;
                    if (data) {
                        this.data.list = data.map((item) => {
                            item.msg = item.defaultMsg;
                            return item;
                        });
                    }
                } else {
                    msgTips(res);
                }
                this.data.loading = false;
            })

        },
        getDefaultConfig(e) {
            defaultFriends.getConfig().then(res => {
                if (res.ok) {
                    let data = res.data;
                    if (data) {
                        let item = data[0];
                        this.dialog3.form = {
                            id: item.id,
                            isRotation: item.isRotation == 1 ? true : false,
                            point: item.point
                        }
                    }
                } else {
                    msgTips(res);
                }
            })
        },
        /* 取消弹框 */
        hideDialog(dialog, visible) {
            this[dialog][visible] = false;
        },
        /* 提交表单 */
        formSubmit() {
            this.$refs['form'].validate(async (valid) => {
                if (valid) {
                    this.loading = true;
                    let type = this.dialog.type;
                    let res;
                    let postdata = {
                        ...this.dialog.form,
                    }

                    // let fileData = new FormData();
                    /* 新增 */
                    if (type == "add") {
                        res = await defaultFriends.add(baseTools.toFormData(postdata));
                    }
                    /* 修改 */
                    if (type == "edit") {
                        res = await defaultFriends.edit(baseTools.toFormData(postdata));
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
            defaultFriends.delete(ptdata).then(res => {
                if (res.ok) {
                    this.dialog2.visible = false;
                    this.getData();
                } else {
                    msgTips(res);
                }
                this.loading = false;
            })
        },
        switchChange(state) {
            this.dialog3.form.isRotation = state;
        },
        configFormSubmit() {
            this.$refs['configForm'].validate(async (valid) => {
                if (valid) {
                    this.dialog3.loading = true;
                    let res;
                    let params = {
                        ... JSON.parse(JSON.stringify(this.dialog3.form)),
                    }
                    params.isRotation = params.isRotation? 1 : 0;
                    /* 修改 */
                    res = await defaultFriends.setConfig(baseTools.toFormData(params));
                    if (res.ok) {
                        this.dialog3.visible = false;
                        successTips("保存成功");
                        // this.dialog3.form = this.$options.data().dialog3.form;
                        this.getDefaultConfig();
                    } else {
                        msgTips(res);
                    }
                    this.dialog3.loading = false;
                } else {
                    return false;
                }
            });

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

.right_box {
    display: flex;
    align-items: center;
}

.row_c {
    display: flex;
    align-items: center;
}
</style>