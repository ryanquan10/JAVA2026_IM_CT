<template>
    <div class="tioimcontainer">
        <div class="bodycontent" id="bodycontent">
            <NavBar></NavBar>
            <router-view></router-view>
        </div>
        <Call></Call>
        <!-- <div class="navbar">
        </div> -->
    </div>
</template>
<script>
import NavBar from "@/components/NavBar.vue"
import Call from "@/components/Call.vue"
export default {
    data(){
        return {

        }
    },
    components:{
        NavBar,
        Call
    }
}
</script>
<style lang="less" scoped>
@import "~@/assets/style/less/index.less";
</style>