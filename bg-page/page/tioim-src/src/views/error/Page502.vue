<script setup>
// This starter template is using Vue 3 <script setup> SFCs
// Check out https://v3.vuejs.org/api/sfc-script-setup.html#sfc-script-setup
export default {
  data() { },
  methods: {
    reloadFn() {
      location.reload();
    }
  }
}

</script>
<template>
  <div class="error_body">
    <img class="error_img" src="~@/assets/imgs/error/502.png" alt="" srcset="">
    <p>服务器溜了，这就让老板去追！</p>
    <button class="primarybtn error_btn error_btn_load" @click="reloadFn">刷新</button>
  </div>
</template>

<style scoped lang='less'>
@import "~@/assets/style/less/error.less";
@import "~@/assets/style/less/common.less";
</style>