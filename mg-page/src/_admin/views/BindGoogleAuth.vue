<template>
    <div class="bind-google-auth">
        <h2>绑定谷歌身份验证器</h2>
        <div class="content">
            <!-- 二维码展示 -->
            <div class="qr-code-section">
                <p>请使用谷歌身份验证器扫描以下二维码：</p>
                <img :src="qrCodeBase64" alt="Google Authenticator QR Code" class="qr-code" />
                <p v-if="manualEntryKey">手动输入密钥：{{ manualEntryKey }}</p>
            </div>

            <!-- 验证码输入 -->
            <div class="code-input-section">
                <el-form :model="form" :rules="rules" ref="form" label-width="120px">
                    <el-form-item label="验证码" prop="code">
                        <el-input v-model="form.code" placeholder="请输入6位验证码"></el-input>
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary" @click="submitForm">绑定</el-button>
                        <el-button @click="cancelBinding">取消</el-button>
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>

<script>
import { mguser } from '@_/axios/path'; // 引入 API 请求路径

export default {
    data() {
        return {
            qrCodeBase64: '', // Base64 编码的二维码图片
            manualEntryKey: '', // 手动输入密钥
            form: {
                code: '' // 用户输入的验证码
            },
            rules: {
                code: [
                    { required: true, message: '请输入验证码', trigger: 'blur' },
                    { len: 6, message: '验证码必须为6位', trigger: 'blur' }
                ]
            }
        };
    },
    created() {
        this.fetchGoogleAuthData();
    },
    methods: {
        // 获取绑定所需数据（二维码和手动密钥）
        fetchGoogleAuthData() {
            mguser.bindGoogleData()
                .then(response => {
                    const { qrCodeBase64, manualEntryKey } = response.data;
                    if (!qrCodeBase64 || !manualEntryKey) {
                        throw new Error('后端未返回有效的绑定数据');
                    }
                    this.qrCodeBase64 = `data:image/png;base64,${qrCodeBase64}`; // 设置二维码图片
                    this.manualEntryKey = manualEntryKey; // 设置手动密钥
                })
                .catch(error => {
                    this.$message.error('获取绑定数据失败，请稍后重试');
                    console.error(error);
                });
        },

        // 提交绑定表单
        submitForm() {
            this.$refs.form.validate(valid => {
                if (valid) {
                    this.bindGoogleAuth();
                } else {
                    this.$message.error('请正确填写验证码');
                    return false;
                }
            });
        },

        // 绑定谷歌身份验证器
        bindGoogleAuth() {
            const { code } = this.form;
            mguser.bindGoogle({
                manualEntryKey: this.manualEntryKey, // 提交手动密钥
                code // 提交用户输入的验证码
            })
                .then(response => {
                    this.$message.success('绑定成功');
                    this.$router.push({ path: '/' }); // 返回首页或其他页面
                })
                .catch(error => {
                    this.$message.error('绑定失败，请检查验证码是否正确');
                    console.error(error);
                });
        },

        // 取消绑定操作
        cancelBinding() {
            this.$router.push({ path: '/' }); // 返回首页或其他页面
        }
    }
};
</script>


<style lang="less" scoped>
.bind-google-auth {
    max-width: 600px;
    margin: 50px auto;
    padding: 20px;
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

    h2 {
        text-align: center;
        margin-bottom: 20px;
        font-size: 24px;
        color: #333;
    }

    .content {
        text-align: center;

        .qr-code-section {
            margin-bottom: 20px;

            .qr-code {
                width: 200px;
                height: 200px;
                margin: 10px auto;
                display: block;
            }

            p {
                font-size: 14px;
                color: #666;
            }
        }

        .code-input-section {
            text-align: left;
        }
    }
}
</style>