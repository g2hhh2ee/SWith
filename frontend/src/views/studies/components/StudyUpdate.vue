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
  <div>
    <Navbar />
    <div class="container">
      <h1 class="form-title">스터디룸 수정</h1>
      <div class="row d-flex justify-content-center">
        <div class="col-md-6 col-lg-5 col-xl-4">
          <section>
            <form class="studyInfo-wrapper" enctype="multipart/form-data">
              <div>
                <label for="studyName" class="form-label">스터디 이름</label>
                <input type="text" class="form-control" id="studyName" v-model="state.studyInfo.studyName" required maxlength="20">
                <div :style="{ visibility: (state.isValidStudyName || !state.wasInputed.studyName )? 'hidden' : 'visible' }"
                  class="invalid-feedback">
                  2~20자 사이로 작성해주세요.
                </div>
              </div>
              <div class="mb-3">
                <label for="goal" class="form-label">스터디 목표</label>
                <textarea
                  class="form-control form-goal"
                  id="studyGoal" rows="3"
                  v-model="state.studyInfo.studyGoal"
                  placeholder="스터디 목표를 한 줄로 표현해 보세요!"
                  maxlength="40"
                ></textarea>
              </div>
              <div class="d-flex justify-content-start mb-4">
                <div class="dropend">
                  <div class="image-wrapper scale" role="button" id="dropdownMenuLink" data-bs-toggle="dropdown" aria-expanded="false" >
                    <img class="study-img" :src="state.studyImgSrc">
                  </div>
                  <ul class="dropdown-menu" aria-labelledby="dropdownMenuLink">
                    <li>
                      <label for="changeStudyImg" class="dropdown-item img-form-label">스터디 이미지 선택</label>
                    </li>
                    <li>
                      <span @click="onClickDefaultImg" class="dropdown-item img-revert-default">기본 이미지로 변경</span>
                    </li>
                  </ul>
                </div>
                <div class="form-group">
                  <input @change="onClickUploadFile" id="changeStudyImg" type="file" ref="file" class="form-control" accept="image/*" style="display: none;" />
                </div>
              </div>
              <button @click="onClickUpdateStudy" class="btn btn-primary col-12">변경사항 수정</button>
            </form>
          </section>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed } from 'vue';
import { useStore } from 'vuex';
import { useRoute } from 'vue-router';
import { useRouter } from 'vue-router';
import Navbar from '@/views/common/Navbar.vue';
import notifications from '@/composables/notifications'
import Loading from 'vue-loading-overlay';
import 'vue-loading-overlay/dist/vue-loading.css';

export default {
  name: '',
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
  components: { Navbar, Loading },
  setup() {
    const store = useStore();
    const route = useRoute();
    const router = useRouter();
    const state = ref({
      // studyInfo : store.getters.getStudyInfo,
      studyInfo : computed(() => {
        return store.state.study.studyInfo
      }),
      studyImage: '',
      updated: false,
      studyImgSrc: computed(()=> {
        if (state.value.studyInfo.studyImgUrl) {
          return state.value.studyInfo.studyImgUrl
        } else {
          return require(`@/assets/img/study_room/studyroom.png`)
        }
      }),
      wasInputed: {
        studyName: false,
      },
      isValidStudyName: computed(() => {
        if (state.value.studyInfo.studyName !== '') {
          state.value.wasInputed.studyName = true;
        }
        if (state.value.studyInfo.studyName && validateStudyName(state.value.studyInfo.studyName)) {
          return true;
        }
        return false;
      }),
      loading: false,
    });

    const { notifySuccess, notifyDangerDescription } = notifications();
    store.dispatch('GET_STUDY_INFO', route.params.studyId)

    const onClickUploadFile = (e) => {
      const file = e.target.files[0]
      if (file.size > 2097152) {
        e.preventDefault();
        notifyDangerDescription('파일 사이즈가 너무 큽니다.😯', '최대 2MB')
        return;
      } else {
        state.value.studyInfo.studyImgUrl = URL.createObjectURL(file);
        state.value.studyImage = file;
        state.value.updated = true;
      }
    };
    const onClickDefaultImg = () => {
      state.value.studyInfo.studyImgURL = '';
      state.value.studyInfo.studyImgUrl = '';
      state.value.updated = true;
    };

    const onClickUpdateStudy = (e) => {
      e.preventDefault();
      const updateStudyData = new FormData();
      updateStudyData.append("studyName", state.value.studyInfo.studyName)
      updateStudyData.append("studyGoal", state.value.studyInfo.studyGoal)
      updateStudyData.append("studyImage", state.value.studyImage)
      updateStudyData.append("updated", state.value.updated)

      loadingCall()
      store.dispatch('updateStudyInfo', { studyId: state.value.studyInfo.studyId, payload:updateStudyData})
      setTimeout(() => {
        notifySuccess('스터디 정보 수정 완료!😙')
        router.push({ name: 'StudyDetail' })
      },1000)
    }

    const validateStudyName = function (studyName) {
      if (studyName.length >= 2 && studyName.length <= 20) {
        return true;
      }
      return false;
    };

    function loadingCall(){
      state.value.loading = true
      setTimeout(() => {
        state.value.loading = false
      }, 1000)
    }

    return {
      state, onClickUploadFile, onClickDefaultImg, onClickUpdateStudy, loadingCall
    }
  },

}
</script>

<style scoped>
.container{
  margin-top: 50px;
  justify-content: center;
}
form{
  text-align: left;
}
section{
  margin-bottom: 100px;
}
p{
  font-size: 18px;
  font-weight: 700;
  display: flex;
}
.btn-change-pw {
  text-align: center;
  font-size: 0.7rem;
}
.uploadImage{
  margin-bottom: 20px;
}

.study-img{
    width: 100%;
    height: 100%;
    object-fit: cover;
    background: #fff;
    border: 1px solid rgba(0, 0, 0, 0.125);
    border-radius: 4%;
}
.base-url{
  font-size: 14px;
  font-weight: 400;
  color: #737B7D;
  margin-bottom: 5px;
}
.studyroom-img p{
  font-size: 16px;
  font-weight: 400;
  margin-bottom: 5px;
}
.invalid-feedback {
  display: block;
  font-size: 0.75rem;
  margin-top: 0;
  margin-bottom: 0;
}
/* basic setting */
button{
  font-size: 14px;
  text-align: center;
}
.form-label{
  margin-bottom: 3px;
}
.form-title {
  font-size: 24px;
  text-align: center;
  margin-bottom: 30px;
}
.container {
  margin-top: 100px;
  margin-bottom: 120px;
}
input::placeholder {
  font-size: 12px;
  padding: auto;
  vertical-align: middle;
}
input{
  background-color: #F4F5F4;
  vertical-align: middle;
}
textarea{
  background-color: #F4F5F4;
  vertical-align: middle;
}
.img-form-label{
  margin-bottom: 3px;
}
.image-wrapper{
  width: 200px;
  height: 150px;
  border-radius: 4%;
  overflow: hidden;
  justify-content: center;
  cursor: pointer;
  display: block;
}
.study-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  background: #BDBDBD;
}
.dropdown-menu{
  text-align: center;
}
.dropdown-item{
  cursor: pointer;
}
.scale {
  transform: scale(1);
  -webkit-transform: scale(1);
  -moz-transform: scale(1);
  -ms-transform: scale(1);
  -o-transform: scale(1);
  transition: all 0.3s ease-in-out;   /* 부드러운 모션을 위해 추가*/
}
.scale:hover {
  transform: scale(1.05);
  -webkit-transform: scale(1.05);
  -moz-transform: scale(1.05);
  -ms-transform: scale(1.05);
  -o-transform: scale(1.05);
}
</style>
