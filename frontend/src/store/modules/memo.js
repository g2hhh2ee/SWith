import { setMemoList, getMemoList } from "../../api/memo";

const state = () => ({
  memoList: [],
  memo: {},
  selectedIdx: -1,
  zIndexCount: 0,
});

const getters = {
  selectedMemo(state) {
    if (state.selectedIdx < 0) {
      return null;
    }
    return state.memoList.selectedIdx;
  }
};

const actions = {
  GET_MEMO_LIST({ commit }, studyId) {
    getMemoList(
      studyId,
      (res) => {
        console.log(res)
        commit('SET_MEMO_LIST', res.data.data)
      },
      (err) => {
        console.log(err)
      }
    )
  },
  UPDATE_MEMO_LIST({ commit }, { studyId, memoList }) {
    console.log(studyId, memoList)
    const payload = [];
    memoList.forEach((memo) => {
      const { content, color, transform } = memo;
      payload.push({
        content,
        color,
        transform,
      })
    })
    console.log(payload)
    setMemoList(
      studyId,
      payload,
      (res) => {
        console.log(res.data)
        getMemoList(
          studyId,
          (res) => {
            console.log(res.data)
            commit('SET_MEMO_LIST', res.data.data)
          },
          (err) => {
            console.log(err)
          }
        )
      },
      (err) => {
        console.log(err)
      }
    )
  }
};
const mutations = {
  SET_MEMO_LIST(state, memoList) {
    state.memoList = memoList
  },
  ADD_MEMO(state, memo) {
    state.memoList.push(memo)
  },
  SET_SELECTED_MEMO_INDEX(state, index) {
    state.selectedIdx = index;
  },
  SET_MEMO_BY_INDEX(state, { index, memo }) {
    state.memoList[index] = { ...state.memoList[index], ...memo };
  },
  SET_SELECTED_MEMO(state, memo) {
    state.memo = memo;
  }
};

export default {
  state,
  getters,
  actions,
  mutations,
};
