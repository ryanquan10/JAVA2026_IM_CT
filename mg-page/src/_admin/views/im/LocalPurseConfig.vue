<template>
    <div class="commonright container">

        <!-- 数据表格 -->
        <div class="contentpad">
            <el-form :model="dialog.form" :rules="dialog.rules" label-width="100px" ref="form"
                :hide-required-asterisk="true" class="dialogform sdialogform" id="form">

                <div class="tmcol">
                    <el-form-item label="管理员收款码:" prop="value">
                        <div class="uploadFile_main">
                            <el-upload ref="uploadFile" class="avatar-uploader" drag action="#" :multiple="false"
                                :auto-upload="false" :show-file-list="false"
                                :on-change="($event) => handleFileChange($event, 'wx')">
                                <img v-if="dialog.form.wx.img" :src="resUrl(dialog.form.wx.img)" class="avatar">
                                <i v-else class="el-icon-plus avatar-uploader-icon"></i>
                            </el-upload>
                            <span>微信</span>
                        </div>
                        <div class="uploadFile_main">
                            <el-upload ref="uploadFile" class="avatar-uploader" drag action="#" :multiple="false"
                                :auto-upload="false" :show-file-list="false"
                                :on-change="($event) => handleFileChange($event, 'zfb')">
                                <img v-if="dialog.form.zfb.img" :src="resUrl(dialog.form.zfb.img)" class="avatar">
                                <i v-else class="el-icon-plus avatar-uploader-icon"></i>
                            </el-upload>
                            <span>支付宝</span>
                        </div>
                        <div class="uploadFile_main">
                            <el-upload ref="uploadFile" class="avatar-uploader" drag action="#" :multiple="false"
                                :auto-upload="false" :show-file-list="false"
                                :on-change="($event) => handleFileChange($event, 'ul')">
                                <img v-if="dialog.form.ul.img" :src="resUrl(dialog.form.ul.img)" class="avatar">
                                <i v-else class="el-icon-plus avatar-uploader-icon"></i>
                            </el-upload>
                            <span>USDT</span>
                        </div>
                         <div class="uploadFile_main">
                            <el-upload ref="uploadFile" class="avatar-uploader" drag action="#" :multiple="false"
                                :auto-upload="false" :show-file-list="false"
                                :on-change="($event) => handleFileChange($event, 'bank')">
                                <img v-if="dialog.form.bank.img" :src="resUrl(dialog.form.bank.img)" class="avatar">
                                <i v-else class="el-icon-plus avatar-uploader-icon"></i>
                            </el-upload>
                            <span>银行卡聚合支付</span>
                        </div>
                    </el-form-item>
                </div>

            </el-form>
        </div>



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
            form: {
                imgUrl: '',

            },
            data: {

            },
            dialog: {
                type: 'add',
                title: '',
                visible: false,
                form: {
                    logoUrl: '',
                    logo: '',
                    wx: {
                        img: '',
                        type: 1
                    },
                    zfb: {
                        img: '',
                        type: 2
                    },
                     ul: {
                        img: '',
                        type: 3
                    },
                     bank: {
                        img: '',
                        type: 4
                    },
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
            curroute: '',

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
            let that = this;
            localPurse.PayImgGet(ptdata).then(function (res) {
                if (res.ok) {
                    let data = res.data;
                    if (data) {
                        let info = {
                            wx: {
                                img: '',
                                type: 1
                            },
                            zfb: {
                                img: '',
                                type: 2
                            },
                            ul: {
                                img: '',
                                type: 3
                            },
                            bank: {
                                img: '',
                                type: 4
                            }
                        };

                        for (var i = 0; i < data.length; i++) {
                            let item = data[i];
                            let key;
                            switch(item.type){
                                case 1: 
                                key = 'wx';
                                break;
                                case 2: 
                                key = 'zfb';
                                break;
                                case 3: 
                                key = 'ul';
                                break;
                                case 4: 
                                key = 'bank';
                                break;
                            }
                            info[key].img = item.paymentImg;
                            info[key].id = item.id;
                        }
                        that.dialog.form.wx = info.wx;
                        that.dialog.form.zfb = info.zfb;
                        that.dialog.form.ul = info.ul;
                        that.dialog.form.bank = info.bank;

                    }
                } else {
                    msgTips(res);
                }
                that.data.loading = false;
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
            fileData.append('logo', _file.raw);
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
        },

    },
}
</script>
<style>
.el-upload-dragger {
    width: 178px !important;
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

.uploadFile_main>span {
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

.footer_btns {
    width: 130px;
    height: 38px;
    margin-left: 100px;
    margin-top: 30px;
}
</style>