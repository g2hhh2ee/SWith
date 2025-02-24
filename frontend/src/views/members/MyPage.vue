<template>
  <div>
    <Navbar />
    <div class="container">
      <h1 class="form-title">내 정보 수정</h1>
      <div class="row d-flex justify-content-center">
        <div class="col-md-6 col-lg-5 col-xl-4" style="">
          <form class="userInfo-wrapper" enctype="multipart/form-data">
            <div class="d-flex justify-content-center">
              <div class="dropend">
                <div class="image-wrapper" role="button" id="dropdownMenuLink" data-bs-toggle="dropdown" aria-expanded="false" >
                  <img class="profile-img scale" :src="state.profileImgSrc" alt=""  >
                </div>
                <ul class="dropdown-menu" aria-labelledby="dropdownMenuLink">
                  <li>
                    <label for="changeProfile" class="img-form-label">프로필 이미지 선택</label>
                  </li>
                  <li>
                    <span @click="onClickDefaultImg" class="img-revert-default">기본 이미지로 변경</span>
                  </li>
                </ul>
              </div>
              <div class="form-group mt-3 mb-3">
                <input @change="onClickUploadFile" id="changeProfile" type="file" ref="file" class="form-control" accept="image/*" style="display: none;" />
              </div>
            </div>

            <!-- Google이나 Kakao 로그인인 경우 안 보이게 하기 -->
            <div class="row d-flex justify-content-between">
              <p class="col-6" style="padding: 0px;">{{ state.userInfo.email }}</p>
              <div class="col-4 d-flex justify-content-end" v-if="state.userInfo.path === 'O'" style="padding: 0px">
                <button type="button" class="btn btn-change-pw btn-success"
                  data-bs-toggle="modal" data-bs-target="#changePasswordModal">
                  비밀번호 변경
                </button>
              </div>
            </div>
            <div class="row">
              <label for="nickname" class="form-label">닉네임</label>
              <input type="text" class="form-control" id="nickname" v-model="state.nickname" required placeholder="사용할 닉네임을 입력해주세요." maxlength="16">
              <div
                :style="{ visibility: (state.isValidNickname || !state.wasInputed.nickname)? 'hidden' : 'visible' }"
                class="invalid-feedback"
              >
                닉네임을 입력해주세요.
              </div>
            </div>
            <div class="row">
              <label for="goal" class="form-label">상태 메시지</label>
              <input type="text" class="form-control form-goal" id="goal" rows="3" v-model="state.goal" placeholder="상태 메세지를 입력해주세요." maxlength="16">
            </div>
            <div class="row">
              <button @click="onClickUpdateUserInfo" class="btn btn-primary btn-save">변경 사항 저장</button>
            </div>
          </form>
          <!-- Button trigger modal -->
          <span class="text-decoration-underline signout-btn" data-bs-toggle="modal" data-bs-target="#signOutModal">
            회원 탈퇴
          </span>
        </div>
      </div>
    </div>
    <ChangePasswordModal />
    <SignOutModal />
  </div>
</template>

<script>
import { ref, computed } from 'vue';
import { useStore } from 'vuex';
import Navbar from '@/views/common/Navbar.vue';
import SignOutModal from '@/views/members/components/SignOutModal.vue';
import ChangePasswordModal from './components/ChangePasswordModal.vue';
import notifications from '@/composables/notifications'

export default {
  name: '',
  components: { Navbar, SignOutModal, ChangePasswordModal },
  setup() {
    const store = useStore();
    const state = ref({
      userInfo : store.getters.getUserInfo,
      nickname : store.getters.getUserInfo.nickname,
      goal: store.getters.getUserInfo.goal,
      profileImg: store.getters.getUserInfo.profileImg,
      profileImgFile: '',
      updated: false,
      profileImgSrc : computed(() => {
        if (state.value.profileImg) {
          return state.value.profileImg
        } else {
          return require(`@/assets/img/navbar/profile.png`)
        }
      }),
      wasInputed: {
        nickname: false,
      },
      isValidNickname: computed(() => {
        if (state.value.nickname !== '') {
          state.value.wasInputed.nickname = true;
        }
        if (state.value.nickname && validateNickname(state.value.nickname)) {
          return true;
        }
        return false;
      }),
    });
    const { notifyDangerDescription, } = notifications();

    const onClickUploadFile = function(e) {
      const file = e.target.files[0];
      if (file.size > 2097152) {
        e.preventDefault();
        notifyDangerDescription('파일 사이즈가 너무 큽니다.😯', '최대 2MB');
        return;
      } else {
        state.value.profileImg = URL.createObjectURL(file);
        state.value.profileImgFile = file;
        state.value.updated = true;
      }
    };

    const onClickDefaultImg = () => {
      state.value.profileImg = '';
      state.value.updated = true;
    }

    const onClickUpdateUserInfo = (e) => {
      e.preventDefault();
      if (state.value.nickname === '') {
        state.value.wasInputed.nickname = true;
        state.value.wasInputed.goal = true;
        return;
      }
      if (!state.value.isValidNickname ) {
        return;
      }
      const updateUserData = new FormData();
      updateUserData.append("nickname", state.value.nickname)
      updateUserData.append("goal", state.value.goal)
      updateUserData.append("profileImg", state.value.profileImgFile)
      updateUserData.append("updated", state.value.updated)
      store.dispatch('updateUserInfo', updateUserData)
    }

    const validateNickname = function (nickname) {
      if (nickname.length >= 1 && nickname.length <= 16) {
        return true;
      } return false;
    };
    const validateGoal = function (goal) {
      if ( goal.length <= 16 ) {
        return true;
      } return false;
    };

    return {
      state,
      onClickUpdateUserInfo,
      onClickUploadFile,
      onClickDefaultImg,
      validateNickname,
      validateGoal,
      notifyDangerDescription,
    }
  },

}
</script>

<style scoped>
form{
  text-align: left;
}
p{
  font-size: 18px;
  font-weight: 700;
  display: flex;
  margin-bottom: 0px
}
.btn-change-pw {
  text-align: center;
  font-size: 0.7rem;
  margin-right: 0px;
}
.btn-save{
  margin: 50px 0 50px;
}
.uploadImage{
  margin-bottom: 20px;
}

.img-form-label{
  padding-bottom: 10px;
  cursor: pointer;
}
.img-revert-default{
  padding-bottom: 10px;
  cursor: pointer;
}
.image-wrapper{
  display: flex;
  margin-bottom: 30px;
  width: 150px;
  height: 150px;
  border-radius: 70%;
  overflow: hidden;
  justify-content: center;
}
.profile-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  background: #ffffff;
}
.signout-btn:hover {
  cursor: pointer;
  font-weight: bold;
}
.signout-btn{
  color: #BDBDBD;
}
.dropdown-menu{
  text-align: center;
}
.invalid-feedback {
  display: block;
  font-size: 0.75rem;
  margin-top: 0;
  margin-bottom: 0.5rem;
}
/* setting */
.form-label{
  margin-bottom: 3px;
  padding: 1px;
}
.form-title {
  font-size: 24px;
  text-align: center;
  margin-bottom: 30px;
}
.container {
  margin-top: 100px;
  /* margin-bottom: 120px; */
}
button{
  font-size: 14px;
  text-align: center;
}
input::placeholder {
  font-size: 12px;
  padding: auto;
  vertical-align: middle;
}
textarea::placeholder {
  font-size: 12px;
  padding: auto;
  vertical-align: middle;
}
input {
  background-color: #F4F5F4;
  vertical-align: middle;
}
textarea {
  background-color: #F4F5F4;
  vertical-align: middle;
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
