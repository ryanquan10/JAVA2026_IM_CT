<template>
    <div id="app">
      <router-view/>
           <el-dialog :visible.sync="tioimsShow" :close-on-click-modal="false" class="tmdialog" >
            <p class="tmheader"></p>
            <iframe class="iframe" id="tioim-container-iframe" ref="iframe" border="0" :src="url"  frameborder="no" allowfullscreen ></iframe>
        </el-dialog>
      <!-- <div class="zxicon zxicon1">
          <img @click="goTioims" id="zxicon" class="icon_tioims" src="~@_/assets/img/common/icon_tioims.png" alt="" srcset="">
          <span id="notread" class="notread"></span>
      </div> -->
    </div>
</template>
<script> 
import 'video.js/dist/video-js.min.css' 
export default {
    name: 'App',
    data(){
        return {
          tioimsShow:false,
          url:""
        }
    },
    methods:{
        goTioims(){
          let tio_session = this.getCookie('tio_mg_session'),imsite = this.sysparams.imsite
          if(tio_session){
            this.url = 'https://www.baidu.com/tioims/home?tio_session='+tio_session
            this.tioimsShow = true
          }
        },
        getCookie(name) { 
          var arr,reg=new RegExp("(^| )"+name+"=([^;]*)(;|$)"); 
          return (arr=document.cookie.match(reg))?unescape(arr[2]):null;
        }
    }
}
</script>
<style lang="less" scoped>
.tmdialog{
   /deep/ .tmheader{
      height: 42px;
      padding: 0;
    }
   /deep/ .el-dialog__headerbtn{
      top: 10px;
    }
}

.tmdialog,.el-dialog,.iframe{
  min-width: 970px;
  min-height: 742px;

}
.tmdialog /deep/ .el-dialog{
  z-index: 19891016;
    width: 970px;
    height: 742px;
    left:0;
    right:0;
    position: fixed;
    top: 50%;
    margin-top: -371px !important
}
.v-modal{
  display: none;
}
#app{
	height: 100%;
	width: 100%;
}
.icon_tioims{
    width:98px;
    height:98px;

}
.zxicon{
  position: fixed;
  bottom: 28px;
  right: 32px;
  width:98px;
  height:98px;
  cursor:pointer;
  z-index: 10000;
}
.notread {
  position: absolute;
  background: #fb7b7a;
  color: #fff;
  text-align: center;
  padding: 0 9px;
  height: 16px;
  line-height: 16px;
  font-size: 12px;
  border-radius: 8px;
  z-index: 1000;
  right: 10px;
  top: 10px;
  display: none;
}
</style>