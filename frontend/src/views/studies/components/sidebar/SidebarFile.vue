<template>
  <div class="fileDiv">
    <p class="title">📑 첨부 파일 목록</p>
    <!-- <SidebarFileList /> -->
    <div class="file-body">
      <div class="card">
        <div v-if="state.fileList" >
                  <div class="card-body" v-for="file in state.fileList" :key="file.memberId">
                    <div class="card-body-data col-9">
                      <!-- <p class="card-body-data-name" text-truncate>{{ file.originName }}</p> -->
                      <p class="card-body-data-name" text-truncate><b>{{ file.originName }}</b></p>
                      <p class="card-body-data-size" text-truncate>size | {{ convertFileSize(file.fileSize) }}</p>
                      <p class="card-body-data-createdAt" text-truncate>createdAt | {{ file.createdAt }}</p>
                    </div>
                    <div class="card-body-buttons col-3">
                      <img
                        class="file-item-icon"
                        src="@/assets/img/icon_sidebar/file/download_1E304F.svg"
                        @click="onClickDownloadFile(file.fileId, file.originName)">
                      <img
                        class="file-item-icon"
                        src="@/assets/img/icon_sidebar/file/trash-1E304F.svg"
                        @click="onClickDeleteFile(file.fileId)">
                    </div>
                  </div>
        </div>
        <div v-if="state.fileList == 0">
          <div class="card-body-none">
            <div class="card-body-none-data">
            <p class="card-body-none-data-name">등록된 파일이 없습니다.</p>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-if="isVisiableFileUploadPercent" class="progress">
      <div class="progress-bar" role="progressbar" :style="`width: ${state.uploadFilePercent}%`" :aria-valuenow=state.uploadFilePercent aria-valuemin="0" aria-valuemax="100"></div>
    </div>
    <form enctype="multipart/form-data">
      <!-- <span>fileList : {{ state.fileList.createdAt }}</span> -->
      <!-- <span>dropzoneFiles : {{ dropzoneFiles }}</span> -->
      <div v-for="(dropzoneFile, index) in dropzoneFiles" v-bind:key="dropzoneFile.id" class="file-item" >
        <span class="file-info" text-truncate><img class="file-type" src="@/assets/img/icon_sidebar/file/clip_dark.svg" alt=""> {{dropzoneFile.name}} </span>
        <img class="file-type" @click="onClickCancelFile(index)" src="@/assets/img/icon_sidebar/file/trash-1E304F.svg" alt="" >
      </div>
      <div class="file-submit">
        <img class="file-submit-icon" @click="onClickUploadFile" src="@/assets/img/icon_sidebar/file/check-DEE8F9.svg" alt="">
        <span class="file-submit-name" @click="onClickUploadFile">submit</span>
        <!-- <span class="file-submit-name" @click.prevent="onClickUploadFile">submit</span> -->
      </div>
      <DropZone @drop.prevent="drop" @change="selectedFile" />
    </form>
  </div>
</template>

<script>
import { ref } from "vue";
import { deleteFile } from '@/api/study';
import { computed, reactive } from 'vue';
import { useStore } from 'vuex';
import { useRoute } from 'vue-router';
// import SidebarFileDeleteModal from '@/views/studies/components/sidebar/SidebarFileDeleteModal';
import notifications from '@/composables/notifications'
import DropZone from '@/views/studies/components/sidebar/SidebarFileDropzone.vue';
// import SidebarFileList from '@/views/studies/components/sidebar/SidebarFileList.vue';

export default {
  name: "SidebarFile",
  components: {
    DropZone,
    // SidebarFileList,
  },
  setup() {
    const store = useStore();
    const route = useRoute();
    store.dispatch('GET_FILE_LIST', route.params.studyId);
    const state = reactive({
      fileList : computed(() => {
        return store.state.study.fileList;
      }),
      uploadFilePercent : computed(() => {
        return store.state.study.fileUploadPercent;
      }),

      // isAttached : false, // 파일 첨부 여부
      // maxSize : 30 * 1024 * 1024, // 30MB = 31457280 byte
      // fileSize : null,  //
      // file: null,
      // file_rules: [
      //   v => !(v && v.fileSize > 30000000) || '30MB 이상의 파일은 첨부할 수 없습니다.'
      // ],
      // files: null,
      // files_rules: [
      //   v => !(v && v.length > 5) || '파일은 최대 5개까지만 첨부할 수 있습니다.'
      // ]
    });
    store.watch((state) => {
        return state.study.fileUploadPercent;
      },
      (newValue, oldValue) => {
        if (state.uploadFilePercent == 0 || newValue >= oldValue) {
          isVisiableFileUploadPercent.value = true;
        } else {
          isVisiableFileUploadPercent.value = false;
        }
      }
    )
    const isVisiableFileUploadPercent = ref(false);

    // const multi_upload =  async () => {
    //   if(state.files == null) {

    //   } else {
    //     const validate = $refs.form2.validate();
    //     if(validate) {
    //       const cf = await $refs.alertCom.open({
    //         type: 'info',
    //         title: '파일 등록',
    //         text: '${state.files.length} 개의 파일을 등록하시겠습니까?'
    //       });
    //       if (cf) {
    //         let formData = new FormData();
    //         formData.append('files', state.files);
    //         for (let i in state.files) {
    //           formData.append('files', state.files[i]);
    //         }
    //         try {
    //           const rs = await $store.dispatch('file/uploadMultiFile', formData);
    //           if (rs) {
    //             state.files = null;
    //             await $store.dispatch('file/initList', {});
    //           }
    //         } catch (err) {
    //           console.error(err);
    //         }
    //       }
    //     }
    //   }
    // };

    const filesArray = ref([]);
    const uploading = ref(false);
    const percentage = ref(0);

    const { notifyDanger, notifySuccess } = notifications();
    const convertFileSize = function (fileSize) {
      if (fileSize < 1024) {
        return `${fileSize}B`;
      } else if (fileSize < 1024 * 1024) {
        return `${(fileSize / 1024).toFixed(1)}KB`;
      } else {
        return `${(fileSize / (1024 * 1024)).toFixed(1)}MB`;
      }
    }

    // const checkFileSize

    let dropzoneFiles = ref([]);

    const drop = (e) => {
      // dropzoneFiles.value = e.dataTransfer.files[0];
      for (let i = 0; i < e.dataTransfer.files.length; i++) {
        dropzoneFiles.value.push(e.dataTransfer.files[i]);
      }
    };

    const selectedFile = () => {
      // dropzoneFiles.value = document.querySelector('.dropzoneFile').files[0];
      let files = document.querySelector('.dropzoneFile').files;
      for (let i = 0; i < files.length; i++) {
        dropzoneFiles.value.push(files[i]);
      }
    }

    const onClickCancelFile = (index) => {
      dropzoneFiles.value.splice(index, 1);
    }

    const onClickUploadFile = async (e) => {
      e.preventDefault();

      if(dropzoneFiles.value.length === 0) {
        notifyDanger('첨부한 파일이 없습니다.');
        return;
      } else if (dropzoneFiles.value.length !== 0 && dropzoneFiles.value.length > 5) {
          notifyDanger('최대 5개까지 첨부할 수 있습니다.');
          // 하고 5개 이상은 자르기 로직
          return;
      } else if( dropzoneFiles.value.length!== 0 && dropzoneFiles.value.length <=5){
        // 파일 크기 체크
        // 업로드 전 리스트에 파일 크기도 뿌려줘야될듯 ㅂㄷㅂㄷ
        // 올리려고 하는 파일의 전체 크기가 30MB 넘으면 fail
        // 전체 크기 합산한 거 > 30메가 : fail
        // 전체 크기 합산 <= 30mb : success => 신호 보내고 업로드전리스트초기화
        //dropzoneFiles.value[i].size
        let fileSizeTotal = 0;
        var uploadFileData = new FormData();
        for (let i = 0; i < dropzoneFiles.value.length; i++) {
          // let files = document.querySelector('.dropzoneFile').files;
          // let fileSize = dropzoneFiles.value[i].size;
          fileSizeTotal += dropzoneFiles.value[i].size;
          // fileSizeTotal += fileSize;
          // fileSizeTotal += fileSize;
          if(fileSizeTotal > 31457280) {
            notifyDanger('최대 30MB만큼만 첨부할 수 있습니다.');
            return;
          }

          uploadFileData.append("files", dropzoneFiles.value[i]);
        }

          // 모두 충족할 경우, 30MB / 5개 => 아래 함수들 실행.
          store.commit('SET_FILE_UPLOAD_PERCENT_INIT');
          const payload = {
            studyId : route.params.studyId,
            uploadFileData : uploadFileData,
            success : (res) => {
                switch (res.data.code) {
                  case 200:
                    notifySuccess('스터디 파일 업로드 성공')
                    break;
                  case 400:
                    notifyDanger('스터디 파일 업로드 실패')
                    break;
                }
                dropzoneFiles.value = [];
                uploadFileData = '';
                store.dispatch('GET_FILE_LIST', route.params.studyId);
              },
            fail : (err) => {
                console.log(err)
                notifyDanger('서버에 문제가 발생했습니다.😥')
              },
          }
          await store.dispatch("UPLOAD_FILE", payload);
      }
    }

    const onClickDownloadFile = async (fileId, fileName) => {
      store.commit('SET_FILE_UPLOAD_PERCENT_INIT');
      const payload = {
        studyId : route.params.studyId,
        fileId : fileId,
        success : (res) => {
          const url = window.URL.createObjectURL(new Blob([res.data]));
          const link = document.createElement('a');
          link.href = url;
          link.setAttribute('download', fileName);
          document.body.appendChild(link);
          link.click();
        },
        fail : (err) => {
          console.log(err)
          notifyDanger('서버에 문제가 발생했습니다.😰')
        },
      }
      store.dispatch("DOWNLOAD_FILE", payload);
    }
    const onClickDeleteFile = (fileId) => {
      deleteFile(
        route.params.studyId,
        fileId,
        (res) => {
          switch (res.data.code) {
            case 200:
              notifySuccess('스터디 파일 삭제 성공')
              break;
            case 400:
              notifyDanger('스터디 파일 삭제 실패')
              break;
          }
          store.dispatch('GET_FILE_LIST', route.params.studyId);
        },
        (err) => {
          console.log(err)
          notifyDanger('서버에 문제가 발생했습니다.😰')
        },
      )
    }

    return {
      state,
      dropzoneFiles,
      drop,
      selectedFile,
      onClickCancelFile,
      onClickUploadFile,
      onClickDownloadFile,
      onClickDeleteFile,
      convertFileSize,
      isVisiableFileUploadPercent,
      filesArray,
      uploading,
      percentage
    };
  },
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Mulish:ital,wght@0,200;0,300;0,400;0,500;0,600;0,700;0,800;0,900;1,200;1,300;1,400;1,500;1,600;1,700;1,800;1,900&family=Noto+Sans+KR:wght@100;300;400;500;700;900&display=swap');
@import url('https://fonts.googleapis.com/css2?family=Alef&display=swap');

.fileDiv {
  height: 100vh;
  display: flex;
  flex-direction: column;
  font-family:  'Noto Sans KR', 'Mulish';
  width: 100%;
  text-align: left;
}
.title{
  font-size: 3vh;
  font-weight:500;
  margin: 5.5vh 0 1vh 1vw;
}

.file-body {
  flex-grow: 1;
  overflow-x: hidden;
  overflow-y: scroll;
  scroll-behavior: smooth;
  width: 100%;
  padding-left: 0.5vw;
}
::-webkit-scrollbar{
  width: 12px;
}
::-webkit-scrollbar-thumb{
    background-color: #999;
    border-radius: 10px;
}
::-webkit-scrollbar-track{
    background-color: #1E304F;
}
.file-item {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  opacity: 0.5;
  background-color: #F5CEC7;
  padding: 10px;
  border: solid 4px #ffffff;
  margin-top: 5px;
  border-radius: 15px;

  font-family: 'Mulish', 'Alef', 'Noto Sans KR';
  color: #1E304F;
  font-size: 13px;
}

.file-info {
  font-size: 13px;
  padding: 2.5px;
  font-family: 'Mulish';

}

.file-submit {
  display: flex;
  flex-direction: row;
  justify-content: flex-end;
  align-items: center;
  cursor: pointer;
  padding: 0.5rem;
}

.file-submit-icon {
  margin-right: 3px;
}

.file-submit-name {
  font-size: 2vh;
  font-family: 'Mulish';
}

.card {
  display: flex;
  color: black;
  background-color: #1E304F;
  margin-top: 2px;
}

.card-body {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  background-color: #F5CEC7;
  border: solid 4px #ffffff;
  margin-top: 5px;
  border-radius: 24px;
  opacity: 0.85;

  font-family: 'Mulish', 'Alef', 'Noto Sans KR';
  color: 1E304F;
  font-size: 13px;
}
.card-body-none {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  background-color: #F5CEC7;
  border: solid 4px #ffffff;
  margin-top: 5px;
  border-radius: 15px;
  opacity: 0.5;

  font-family: 'Mulish', 'Alef', 'Noto Sans KR';
  color: 1E304F;
  font-size: 13px;
}

.card-body-buttons {
  display: flex;
  flex-direction: row;
  flex-wrap: nowrap;
  justify-content: flex-end;
  align-items: center;
  cursor: pointer;
}

.card-body-data {
  display: inline-flex;
  flex-direction: column;
  flex-wrap: nowrap;
  align-items: flex-start;
}
.card-body-data-name {
  font-size: 1.4vh;
  margin-bottom: 0.6vh;
  overflow: hidden;
  /* white-space: nowrap; */
  display: -webkit-box;
  text-overflow: ellipsis;
  width: 10vw;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.file-info {
  font-size: 1.4vh;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 10vw;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.card-body-none-data {
  display: flex;
  flex-direction: column;
  flex-wrap: nowrap;
  align-items: flex-start;
  margin-top: 16px;
  margin-left: 10px;
}

.card-body-none-data-name {
  font-size: 1.6vh;
}

.card-body-data-size {
  margin: 0;
  font-size: 1.2vh;
}

.card-body-data-createdAt {
  margin: 0;
  font-size: 1.2vh;
}

form{
  padding: 0 1vw 0 0.5vw;
  display: flex;
  flex-direction: column;
}

.file-item-icon:hover {
  color:aliceblue;
}
</style>
