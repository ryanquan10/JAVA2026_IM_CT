<template>
  <div class="circle_resource">
    <div class="circle_imgs _row wrap" v-if="type == 2">
        <img :class="_class(item.imgs)" :src="img" alt="" v-for="(img, index) in item.imgs" :key="index" @click.stop="seeResource(index)" />
    </div>
    <div id="circle_video" class="circle_video" v-else-if="type == 3" @click.stop="seeResource">
      <video class="video" :src="item.videoUrl"></video>
      <div class="video_mask row-c-c">
        <i class="play li-icon-play"></i>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    type: {
      type: String | Number,
      default: 2,
    },
    item: {
      type: Object,
      default: {},
    },
  },
  data() {
    return {
      count: 0,
    };
  },
  computed: {},
  methods: {
    _class(imgs) {
      let _class = "circle_img1";
      let len = imgs.length;
      if (len > 2) {
        _class = "circle_img3";
      } else {
        _class = "circle_img" + len;
      }
      return _class;
    },
    seeResource(index){
      console.log(this.item);
      let item = this.item;
      let d = {
        type: item.msgType
      };
      if(d.type == 2){
        d.data = {
          imgs: item.imgs,
          index: index 
        };
      }else if(d.type == 3){
        let dom_info = document.querySelector('#circle_video').getBoundingClientRect();
        d.data = {
          title: '详情',
          videoUrl: item.videoUrl,
          width: dom_info.width,
          height: dom_info.height
        }
      }
      this.$emit('seeResource', d)
    
    }
  },
};
</script>

<style lang="less" scoped>
.circle_resource {
  .circle_imgs {
    width: 320px;
    margin-top: 10px;

    img {
      margin-bottom: 10px;
    }
    .circle_img1 {
      width: 250px;
      height: 250px;
      object-fit: contain;
      object-position: top left;
    }
    .circle_img2 {
      width: 150px;
      height: 150px;
      object-fit: cover;
      margin-right: 4px;
    }
    .circle_img3 {
      width: 90px;
      height: 90px;
      object-fit: cover;
      margin-right: 4px;
    }
  }
}
.circle_video {
  display: inline-block;
  position: relative;
  margin-top: 10px;
  .video {
    max-width: 320px;
    max-height: 320px;
    object-fit: contain;
    object-position: top left;

  }
  .video_mask{
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.6);
    .play{
      font-size: 47px;
      color: #efefef;
    }
  }
}
</style>