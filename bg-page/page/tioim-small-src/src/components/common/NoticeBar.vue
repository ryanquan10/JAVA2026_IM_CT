<template>
  <div class="my-outbox" :style="{height: height + 'px', backgroundColor: background, color: color}">
    <i class="el-icon-bell"></i>
    <div class="scroll_box">
      <div class="my-inbox" ref="box">
        <div
          class="my-list"
          :style="note"
          v-for="(item, index) in list"
          :key="index"
          ref="list"
        >
          <!--{{item.name}}刚刚购买了产品-->
          <span class="my-uname">{{ item.content }}</span>
        </div>
      </div>
    </div>
  </div>
</template>
    
    <script>
export default {
  props: {
    list: {
      type: Array,
      default: [],
    },
    height: {
        type: Number | String,
        default: 40
    },
    background: {
        type: String,
        default: 'rgba(231, 242, 239)'
    },
    color: {
        type: String,
        default: '#30d2b5'
    },
  },
  data() {
    return {
      note: {
        // backgroundImage:
        //   "url(" + require("../../assets/imgs/common/icon_msgfree.png") + ")",
        // backgroundSize: "20px 20px",
        // backgroundRepeat: "no-repeat",
        // backgroundPosition: "1%  50%",
      },
      // 定时器标识
      nowTime: null,
      // 每一个内容的宽度
      disArr: [],
    };
  },
  mounted() {
    // var that = this
    var item = this.$refs.list;
    var len = this.list.length;
    var arr = [];
    // 因为设置的margin值一样，所以取第一个就行。
    var margin = this.getMargin(item[0]);
    for (var i = 0; i < len; i++) {
      arr.push(item[i].clientWidth + margin); // 把宽度和 margin 加起来就是每一个元素需要移动的距离
    }
    this.disArr = arr;
    this.moveLeft();
  },
  beforeDestroy() {
    // 页面关闭清除定时器
    clearInterval(this.nowTime);
    // 清除定时器标识
    this.nowTime = null;
  },
  methods: {
    // 获取margin属性
    getMargin(obj) {
      var marg = window.getComputedStyle(obj, null)["margin-right"];
      marg = marg.replace("px", "");
      return Number(marg); // 强制转化成数字
    },
    // 移动的方法
    moveLeft() {
      var that = this;
      var outbox = this.$refs.box;
      // 初始位置
      var startDis = 0;
      //   console.log('that.disArr: ', that.disArr)
      this.nowTime = setInterval(function () {
        startDis -= 0.5;
        // console.log('初始化移动：', startDis)
        if (Math.abs(startDis) > Math.abs(that.disArr[0])) {
          // 每次移动完一个元素的距离，就把这个元素的宽度
          that.disArr.push(that.disArr.shift());
          // 每次移动完一个元素的距离，就把列表数据的第一项放到最后一项
          // console.log('that.list: ', that.list)
          // console.log('that.list: ', that.list.shift())
          that.list.push(that.list.shift());
          startDis = 0;
          // console.log('移动')
        } else {
          // console.log('不来不来就不来...')
        }
        // 每次都让盒子移动指定的距离，在我自己做的项目中，这种字符串拼接的方法并没有生效
        // outbox.style = 'transfORM: translateX3D(' + startDis + 'px)'
        // 后面换了es6的模板字符串就可以了
        outbox.style = `transform: translateX(${startDis}px)`;
        // outbox.style = 'transform: translateX(\' + startDis + \' px)'
        // outbox.style.marginLeft = 'startDis'
        // console.log('这里:', startDis)
      }, 1000 / 60);
    },
  },
};
</script>
    
<style lang="less" scoped>
.my-outbox {
  display: flex;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid #e1e1e1;
  .scroll_box{
    overflow: hidden;
    margin-left: 6px;
    .my-inbox {
    white-space: nowrap;
    .my-list {
      margin-right: 100px;
      display: inline-block;
      font-size: 14px;
      // height: 20px;
      text-indent: 30px;
      line-height: 40px;
      .my-uname {

      }
    }
  }
  }
  
}
</style>