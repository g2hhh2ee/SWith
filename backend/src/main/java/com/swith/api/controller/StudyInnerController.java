package com.swith.api.controller;

import com.swith.api.dto.study.request.KanbanUpdateReq;
import com.swith.api.dto.study.request.MemoUpdateReq;
import com.swith.api.dto.study.response.FileRes;
import com.swith.api.dto.study.response.KanbanIsUsedRes;
import com.swith.api.dto.study.response.MemoRes;
import com.swith.api.dto.study.response.StudyMemberRes;
import com.swith.api.service.*;
import com.swith.common.response.BaseDataResponse;
import com.swith.common.response.BaseResponse;
import com.swith.db.entity.Member;
import com.swith.db.entity.Memo;
import com.swith.db.entity.Study;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/studies")
public class StudyInnerController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private StudyService studyService;

    @Autowired
    private KanbanService kanbanService;

    @Autowired
    private MemberStudyService memberStudyService;

    @Autowired
    private FileService fileService;

    @Autowired
    private MemoService memoService;

    @GetMapping("/{studyId}/files")
    public ResponseEntity<BaseDataResponse<List<FileRes>>> getStudyFileList(@PathVariable long studyId) {
        List<com.swith.db.entity.File> fileList = fileService.getStudyFileList(studyService.getStudyById(studyId));
        List<FileRes> fileResList = fileList.stream().map(file -> new FileRes(file.getFileId(), file.getOriginName(),
                file.getFileUrl(), file.getFileSize(), file.getCreatedAt())).collect(Collectors.toList());
        log.debug("getStudyFileList - {}", fileResList);
        return ResponseEntity.status(200).body(new BaseDataResponse<>(true, 200, "스터디 파일 목록 조회 성공", fileResList));
    }

    @PostMapping("/{studyId}/files")
    public ResponseEntity<BaseResponse> uploadStudyFiles(@PathVariable long studyId, @RequestParam("files") List<MultipartFile> files) {
        log.debug("uploadStudyFiles - studyId: {}", studyId);
        Member member = memberService.getMemberByAuthentication();
        Study study = studyService.getStudyById(studyId);
        // 다중 파일 업로드, file data 등록
        try {
            fileService.uploadStudyFiles(member, study, files);
        } catch (IOException e) {
            return ResponseEntity.status(200).body(new BaseResponse(false, 400, "스터디 파일 업로드 실패"));
        }
        File tempFile = new File("temp");
        if (tempFile.exists() && !tempFile.delete()) log.debug("updateMember - tempFile.delete() failed");
        // 다중 파일 업로드 성공
        return ResponseEntity.status(200).body(new BaseResponse(true, 200, "스터디 파일 업로드 성공"));
    }

    @GetMapping("/{studyId}/files/{fileId}")
    public ResponseEntity<Object> downloadStudyFile(@PathVariable long studyId, @PathVariable long fileId) {
        log.debug("downloadStudyFile - studyId: {}, fileId: {}", studyId, fileId);
        Map<String, ByteArrayResource> map;
        try {
            map = fileService.downloadStudyFile(studyService.getStudyById(studyId), fileId);
            if (map == null) return ResponseEntity.status(200).body(new BaseDataResponse<>(false, 400, "스터디 파일 다운로드 실패", null));
        } catch (IOException e) {
            return ResponseEntity.status(200).body(new BaseResponse(false, 400, "스터디 파일 다운로드 실패"));
        }
        String fileName = String.valueOf(map.keySet().toArray()[0]);
        ByteArrayResource resource = map.get(fileName);
        log.debug("downloadStudyFile - fileName: {}", fileName);
        return ResponseEntity.ok()
                .contentLength(resource.contentLength())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @DeleteMapping("/{studyId}/files/{fileId}")
    public ResponseEntity<BaseResponse> deleteStudyFile(@PathVariable long studyId, @PathVariable long fileId) {
        try {
            fileService.deleteStudyFile(fileId);
        } catch (IOException e) {
            return ResponseEntity.status(200).body(new BaseResponse(false, 400, "스터디 파일 삭제 실패"));
        }
        return ResponseEntity.status(200).body(new BaseResponse(true, 200, "스터디 파일 삭제 성공"));
    }

    @GetMapping("/{studyId}/kanbans")
    public ResponseEntity<BaseDataResponse> getStudyIsUsed(@PathVariable long studyId) {

        //칸반보드가 수정중인지 확인
        Study study = studyService.getStudyById(studyId);
        Member member = memberService.getMemberByAuthentication();

        if (study.getIsUsed().equals("N")) {
            studyService.updateStudyIsUsed(study, "Y", member);
            return ResponseEntity.status(200).body(new BaseDataResponse(true, 200, "칸반보드 수정 가능", null));
        } else {
            KanbanIsUsedRes result = new KanbanIsUsedRes(study.getLockUseMember().getNickname());
            if (member == study.getLockUseMember()) {
                return ResponseEntity.status(200).body(new BaseDataResponse(true, 200, "칸반보드 수정 가능", null));
            } else {
                return ResponseEntity.status(200).body(new BaseDataResponse(true, 400, "이미 칸반보드 수정중", result));
            }
        }
    }

    @PatchMapping("/{studyId}/kanbans/right")
    public ResponseEntity<BaseResponse> updateKanban(@PathVariable long studyId) {

        Study study = studyService.getStudyById(studyId);
        Member member = memberService.getMemberByAuthentication();
        studyService.updateKanbanRight(study, member);

        return ResponseEntity.status(200).body(new BaseResponse(true, 200, "칸반보드 권한 수정 성공"));
    }

    @PutMapping("/{studyId}/kanbans")
    public ResponseEntity<BaseResponse> updateKanban(@PathVariable long studyId, @RequestBody List<KanbanUpdateReq> kanbanUpdateReqList) {

        Study study = studyService.getStudyById(studyId);
        
        //칸반보드 수정 권한 있는지 확인
        Member member = memberService.getMemberByAuthentication();
        if (study.getLockUseMember() != member) {
            return ResponseEntity.status(200).body(new BaseResponse(true, 401, "칸반보드 수정 권한 없음"));
        }

        //칸반보드 모두 삭제
        kanbanService.deleteKanban(study);

        //칸반보드 등록
        kanbanService.insertKanban(kanbanUpdateReqList);

        //isUsed 변경
        studyService.updateStudyIsUsed(study, "N", null);

        return ResponseEntity.status(200).body(new BaseResponse(true, 200, "칸반보드 수정 성공"));
    }

    @GetMapping("/{studyId}/members")
    public ResponseEntity<BaseDataResponse<List<StudyMemberRes>>> getStudyMemberList(@PathVariable long studyId) {

        Study study = studyService.getStudyById(studyId);
        List<StudyMemberRes> list = memberStudyService.getStudyMemberList(study);

        return ResponseEntity.status(200).body(new BaseDataResponse<>(true, 200, "스터디 회원 목록 조회 성공", list));
    }

    @GetMapping("/{studyId}/memos")
    public ResponseEntity<BaseDataResponse<List<MemoRes>>> getMemoList(@PathVariable long studyId) {

        Study study = studyService.getStudyById(studyId);
        Member member = memberService.getMemberByAuthentication();
        List<Memo> list = memoService.getMemoList(study, member);
        List<MemoRes> result = list.stream().map(memo -> new MemoRes(memo.getContent(),
                memo.getColor(), memo.getTransform())).collect(Collectors.toList());

        return ResponseEntity.status(200).body(new BaseDataResponse<>(true, 200, "스터디 메모 조회 성공", result));
    }

    @PutMapping("/{studyId}/memos")
    public ResponseEntity<BaseResponse> updateMemo(@PathVariable long studyId, @RequestBody List<MemoUpdateReq> memoList) {

        Study study = studyService.getStudyById(studyId);
        Member member = memberService.getMemberByAuthentication();

        // 메모 모두 삭제
        memoService.deleteMemo(study, member);

        // 메모 등록
        memoService.insertMemo(study, member, memoList);

        return ResponseEntity.status(200).body(new BaseResponse(true, 200, "스터디 메모 수정 성공"));
    }
}
