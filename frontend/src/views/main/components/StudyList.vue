<template>
    <loading v-model:active="state.loading"
          :can-cancel="false"
          :is-full-page="true"
          :height="height"
          :width="width"
          :color="color"
          :loader="loader"
          :background-color="bgColor"
          :opacity="opacity"
          :lock-scroll="false"
          class="vld-overlay"
          :style="state.loading ? '-webkit-backdrop-filter: blur(2px); backdrop-filter: blur(2px);' : ''"
      ></loading>
  <div class="container">
    <div class="row">
      <p class="study-list-header col-sm-12 col-lg-5">스터디 목록</p>
      <div class="col-12 offset-lg-0 col-lg-7 join-study">
        <div class="row navbar">
          <div class="col-12 col-lg-6 mb-3">
            <input type="text" class="form-control" v-model="studyCode" placeholder="참여코드">
          </div>
          <div class="col-12 col-md-5 offset-lg-0 col-lg-3 mb-3">
            <button class="btn btn-primary" @click="onClickJoin">
              참여하기
              <font-awesome-icon class="icon ms-1" :icon="['fas', 'walking']"></font-awesome-icon>
            </button>
          </div>
          <div class="col-12 col-md-5 offset-lg-0 col-lg-3 mb-3">
            <router-link :to="{ name: 'StudyCreate'}">
              <button class="btn btn-success">
                만들기
                <font-awesome-icon class="icon ms-1" :icon="['fas', 'hammer']"></font-awesome-icon>
              </button>
            </router-link>
          </div>
        </div>
      </div>
      <div v-if="studies.length" class="d-flex flex-wrap">
        <div v-for="study in studies" :key="study.studyId" class="col-sm-12 col-lg-6 col-xl-4 card-box">
          <StudyListItem :study="study" />
        </div>
      </div>
      <div v-if="studies.length == 0" class="none">
        <p>아직 참여중인 스터디가 없습니다🙃</p>
      </div>
    </div>
  </div>
</template>

<script>
import StudyListItem from './StudyListItem.vue';
import { ref } from 'vue';
import { useStore } from 'vuex';
import { reactive } from 'vue';
import { joinStudy } from '@/api/study';
import notifications from '@/composables/notifications'
import Loading from 'vue-loading-overlay';
import 'vue-loading-overlay/dist/vue-loading.css';

export default {
  data() {
    return {
      loader: 'dots',
      color: '#334466',
      bgColor: 'white',
      height: 120,
      width: 120,
      opacity: 0.2,
      lockScroll: true,
    }
  },
  props: {
    studies: Array,
  },
  components: {
    StudyListItem,
    Loading,
  },
  setup() {
    let studyCode = ref('')

    const store = useStore();
    setTimeout(() => {
      store.dispatch('GET_STUDY_LIST')
    },1700)
    // store.dispatch('GET_STUDY_LIST')
    const { notifySuccess, notifyDanger } = notifications();

    const state = reactive({
      loading: false,
    });

    const onClickJoin = function () {
      if (!studyCode.value) {
        return
      }
      const payload = { code: studyCode.value }
      joinStudy(
        payload,
        (res) => {
          // state.loading = true
          loadingCall()
          if (res.data.code === 200) {
            setTimeout(() => {
              store.dispatch('GET_STUDY_LIST')
              studyCode.value =''
              notifySuccess('스터디 참여 완료!😎')
            },1501)
          } else if (res.data.code === 400) {
            setTimeout(() => {
              store.dispatch('GET_STUDY_LIST')
              studyCode.value =''
              notifyDanger('해당 스터디가 존재하지 않습니다.😯')
            },1501)
          } else if (res.data.code === 409) {
            setTimeout(() => {
              store.dispatch('GET_STUDY_LIST')
              studyCode.value =''
              notifyDanger('이미 참여중인 스터디입니다.😓')
            },1501)
          }
        },
        (err) => {
          console.log(err)
          notifyDanger('서버에 문제가 발생했습니다.😰')
        }
      )
    }

    function loadingCall(){
      state.loading = true
      setTimeout(() => {
        state.loading = false
      }, 1500)
    }
    return { state, studyCode, onClickJoin , loadingCall}
  },
  created(){
    this.loadingCall()
  }
};
</script>

<style scoped>
.container {
  margin-top: 90px;
  /* margin-bottom: 90px; */
  padding-inline: 200px;
}
.study-list-header {
  font-weight: 600;
  font-size: 36px;
  text-align: left;
  /* padding-left: 20px;
  margin-bottom: 40px; */
}
.form-control {
  height: 42px;
  font-size: 15px;
  margin-top: 0;
}
.btn{
  width: 94%;
  height: 42px;
  color: #ffffff;
  font-size: 14px;
  margin-top: 0;
}
.btn-success{
  color: #334466;
  font-weight: 500;
}
.icon {
  font-size: 1.1rem;
  margin: 0;
}
.hammer-icon {
  font-size: 1.05rem;
  margin-left: 0.2rem;
}
.flex-wrap{
  padding: 0px;
}
.card-box{
  padding: 20px
}
.searchbar{
  padding-top: 10px;
}
.spinner-border{
  margin-top: 100px;
}
.none p{
  font-size: 30px;
  padding-top: 150px;
  padding-bottom: 150px;
}
.navbar .col-3{
  margin-right: 0px;
  margin-left: 0px;
  padding-right: 0px;
}
.navbar .col-6{
  margin-right: 0px;
  padding: 0px;
}

</style>
