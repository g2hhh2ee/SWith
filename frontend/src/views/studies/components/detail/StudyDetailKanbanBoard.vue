<template>
<div class="StudyDetailKanbanBoard">
  <div class="kanban-title">
    <h1>Todo List</h1>
    <!-- <p>A board to keep track of your study progress.</p> -->
    <p class="kanban-detail ms-3 text-muted">Todo List는 스터디룸 내부에서 수정할 수 있습니다.</p>
  </div>
  <div class="container" style="height: 28rem;">
    <div class="p-3 bg-light-grey tasks h-100 row">
      <div
        v-for="tasks in state.kanbanBoard"
        :key="tasks.taskId"
        class="p-2 col-4"
      >
        <p class="text-start">
          <span class="taskname px-2 py-1"
            :class="{ 'bg-grey' : tasks.taskId === 1, 'bg-pink' : tasks.taskId === 2, 'bg-purple' : tasks.taskId === 3 }">
            {{ tasks.taskName }}
          </span>
          </p>
        <div class="overflow-auto" style="max-height: 23rem">
          <StudyDetailKanbanBoardCard
            v-for="(task) in tasks.kanban"
            :key="task.kanbanId"
            :task="task"
            class="d-flex align-items-center kanban-card"
            @onClickCard="selectedTask.value=$event"
          />
        </div>
      </div>
    </div>
  </div>
  <div class="text-end mt-3 me-3">
    <button type="button" class="btn btn-secondary text-light" data-bs-toggle="modal" data-bs-target="#exitModal">
      탈퇴하기
    </button>
  </div>
  <StudyDetailKanbanBoardModal :selectedTask="selectedTask"/>
</div>
</template>
<script>
import { ref, reactive, computed } from 'vue'
import StudyDetailKanbanBoardCard from '@/views/studies/components/detail/StudyDetailKanbanBoardCard.vue';
import StudyDetailKanbanBoardModal from '@/views/studies/components/detail/StudyDetailKanbanBoardModal.vue';

export default {
  name: 'StudyDetailKanbanBoard',
  components: {
    StudyDetailKanbanBoardCard,
    StudyDetailKanbanBoardModal,
  },
  props: {
    studyInfo: Object,
  },
  setup(props) {
    const state = reactive({
      kanbanBoard: computed(() => {
        return props.studyInfo.kanbanBoard
      }),
    })
    const selectedTask = ref({})

    return { state, selectedTask }
  },
}
</script>
<style scoped>
.kanban-title h1 {
  font-family: 'Mulish';
  font-size: 32px;
  font-weight: 700;
  display: block;
  text-align: start;
}
.kanban-title p {
  font-family: 'Mulish';
  text-align: start;
}
.tasks {
  border-radius: 1rem;
}
.taskname {
  border-radius: 3rem;
  font-size: 0.8rem;
}
.bg-grey {
  background-color: #E1E4E8;
}
.bg-purple {
  background-color: #F0E7F6;
}
.bg-pink {
  background-color: #FFDCE0;
}
.bg-light-grey {
  background-color: #F8F8F8;
}
::-webkit-scrollbar{
  width: 6px;
}
</style>
