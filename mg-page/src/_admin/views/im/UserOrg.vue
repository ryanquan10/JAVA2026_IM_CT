<template>
    <div class="commonright container">

        <div class="filter">
            <el-row :gutter="20" justify="space-between">
                <el-col :span="18" :offset="0">
                    <div class="filter-item">
                        <label class="filter-label">名称</label>
                        <el-input type="text" clearable v-model="filters.searchkey" placeholder="名称"></el-input>
                    </div>
                    <div class="filter-btn">
                        <button class="primarybtn search" @click="getData(1)">查询</button>
                        <button class="primarybtn" @click="setData('add')" v-auth="'add'">
                            新增
                        </button>
                    </div>
                </el-col>
            </el-row>
        </div>

        <!-- 数据表格 -->
        <div class="contentpad">
            <el-table height="700" :data="data.list" v-loading="data.loading"
                :header-cell-style="{ background: $protovar.tbhabg }">
                <el-table-column label="序号" width="80" type="index" :align="$protovar.align"
                    :index="indexMethod"></el-table-column>

                <el-table-column label="名称" prop="name" width="200"></el-table-column>
                <el-table-column label="邀请码" prop="invitecode"></el-table-column>
                <el-table-column label="后台管理员" prop="unick"></el-table-column>

                <el-table-column label="操作" width="150" v-if="authdisable">
                    <template slot-scope="scope">
                        <!-- <span class="tmopera" @click="setData('edit', scope.row)"
                >编辑</span
              > -->
                        <span class="tmopera waring" @click="delUser(scope.row)" v-auth="'del'">删除</span>
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
        <el-dialog :visible.sync="dialog.visible" :close-on-click-modal="false" class="tmdialog"
            :width="$protovar.sgtwidth">
            <p class="tmheader">{{ dialog.title }}</p>
            <el-form :model="dialog.form" :rules="dialog.rules" :label-width="$protovar.fmlabwidth" ref="form"
                :hide-required-asterisk="true" class="dialogform sdialogform" id="form">

                <div class="tmcol">
                    <el-form-item label="名称" prop="name">
                        <el-input class="name" v-model="dialog.form.name" name="name"></el-input>
                    </el-form-item>
                </div>


                <div class="tmcol">
                    <el-form-item label="邀请码" prop="invitecode">
                        <el-input class="name" v-model="dialog.form.invitecode" maxlength="6"
                            name="invitecode"></el-input>
                    </el-form-item>
                </div>


                <div class="tmcol">
                    <el-form-item label="后台管理员" prop="mguid">
                        <el-select v-model="dialog.form.mguid">
                            <el-option v-for="item in data.allAdminUser" :key="item.id" :value="item.id"
                                :label="item.loginname"></el-option>
                        </el-select>
                    </el-form-item>
                </div>



            </el-form>
            <div class="tmdialog-footer pb30">
                <button class="primarybtn search" @click="hideDialog('dialog', 'visible')">
                    取消
                </button>
                <button class="primarybtn" @click="formSubmit" :disabled="loading">
                    保存
                </button>
            </div>
        </el-dialog>

        <!-- 删除 -->
        <el-dialog :visible.sync="dialog2.visible" :width="$protovar.dwidth" class="tmdialog"
            :close-on-click-modal="false">
            <div class="title">确定删除这一项吗？</div>
            <div class="tmdialog-footer pb60">
                <button class="primarybtn search" @click="hideDialog('dialog2', 'visible')">
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
import { msgTips, successTips, mguser } from "@_/axios/path";
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
                allAdminUser: [],//所有后台用户列表
            },
            dialog: {
                type: "add",
                title: "",
                visible: false,
                form: {},
                rules: {
                    name: [{ required: false, message: "请输入名称", trigger: "blur" }],
                    invitecode: [{ required: true, message: "请输入邀请码", trigger: "blur" }],
                    mguid: [{ required: true, message: "请选择后台管理员", trigger: "blur" }],
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
            mguser.listMgInviteOrg(ptdata).then((res) => {
                if (res.ok) {
                    let data = res.data;
                    if (data) {
                        this.data.totalRow = data.totalRow;
                        let list = data.list;
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
                        res = await mguser.addMgInviteOrg(postdata);
                    }
                    /* 修改 */
                    if (type == "edit") {
                        // res = await ipBlacks.edit(postdata);
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

            //获取所有后台用户数据
            mguser.getAllAdminUser({}).then((res) => {
                this.data.allAdminUser = res.data
                console.log(res)

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
            this.loading = false;
            let ptdata = { id: this.currdata.id };
            mguser.delMgInviteOrg(ptdata).then((res) => {
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

.right_box {
    display: flex;
    align-items: center;
}

.row_c {
    display: flex;
    align-items: center;
}

.name input {
    width: 286px !important;
}
</style>