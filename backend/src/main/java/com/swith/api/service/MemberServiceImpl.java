package com.swith.api.service;

import com.swith.api.dto.member.request.MemberInfoReq;
import com.swith.api.dto.study.response.MemberStudyRes;
import com.swith.common.util.FirebaseUtil;
import com.swith.common.util.MailUtil;
import com.swith.common.util.SecurityUtil;
import com.swith.config.FirebaseConfig;
import com.swith.db.entity.Member;
import com.swith.db.entity.MemberStudy;
import com.swith.db.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@Transactional
@Service
public class MemberServiceImpl implements MemberService {

    final String PASSWORD_REGEX = "^((?=.*\\d)(?=.*[a-zA-Z])(?=.*[\\W]).{" + 8 + "," + 16 + "})$";

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberStudyRepository memberStudyRepository;

    @Autowired
    private StudyService studyService;

    @Autowired
    private StudyRepositorySupport studyRepositorySupport;

    @Autowired
    private MemberStudyRepositorySupport memberStudyRepositorySupport;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MailUtil mailUtil;

    @Autowired
    private FileServiceImpl fileService;

    @Autowired
    private FirebaseConfig firebaseConfig;

    @Override
    public Member insertMember(Member member) {
        // member password encoding
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        return memberRepository.save(member);
    }

    @Override
    public Member getMemberByMemberId(Long memberId) {
        return memberRepository.findById(memberId).orElse(null);
    }

    @Override
    public Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email).orElse(null);
    }

    @Override
    public Member getMemberByKakaoId(String kakaoId) {
        return memberRepository.findByKakaoId(kakaoId).orElse(null);
    }

    @Override
    public Member getMemberByGoogleId(String googleId) {
        return memberRepository.findByGoogleId(googleId).orElse(null);
    }

    @Override
    public Member getMemberByNickname(String nickname) {
        return memberRepository.findByNickname(nickname).orElse(null);
    }

    @Override
    public void findPassword(String email) {
        // 임시 비밀번호 생성
        String tempPassword = "";
        while (true) {
            tempPassword = makeTempPassword();
            if (Pattern.compile(PASSWORD_REGEX).matcher(tempPassword).find()) {
                break;
            }
        }

        // 인코딩해서 DB에 넣기
        Member member = memberRepository.findByEmail(email).orElse(null);
        member.setPassword(passwordEncoder.encode(tempPassword));

        // 이메일로 임시 비밀번호 전송
        mailUtil.sendTempPassword(email, tempPassword);
    }

    private String makeTempPassword() {
        char[] charSet = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                '!', '@', '#', '$', '%', '^', '&' };

        String str = "";
        int len = (int) Math.random() * 9 + 8;

        int idx = 0;
        for (int i = 0; i < len; i++) {
            idx = (int) (charSet.length * Math.random());
            str += charSet[idx];
        }
        return str;
    }

    public Member getMemberByAuthentication() {
        long id = -1L;
        Optional<String> username = SecurityUtil.getCurrentUsername();
        if (username.isPresent()) {
            id = Long.parseLong(username.get());
        }
        return memberRepository.findById(id).orElse(null);
    }

    @Override
    public Member updateMember(Member member, MemberInfoReq memberInfoReq, MultipartFile multipartFile) throws IOException {
        member.setNickname(memberInfoReq.getNickname());
        member.setGoal(memberInfoReq.getGoal());
        // upload할 image가 존재하는 경우
        if (memberInfoReq.isUpdated()) {
            if (multipartFile != null && !multipartFile.isEmpty()) {
                Tika tika = new Tika();
                String mimeType = tika.detect(multipartFile.getInputStream());
                // MIME type이 image인지 확인, file size가 2MB이내인지 확인
                if (mimeType.startsWith("image") && multipartFile.getSize() < 2 * Math.pow(2, 20)) {
                    member.setImgUrl(fileService.upload(multipartFile, firebaseConfig.getProfile_storage_path(),
                            member.getImgUrl(), "media"));
                }else throw new IOException();
            } else if (member.getImgUrl() != null) {
                log.debug("updateMember - file path: {}", FirebaseUtil.convertUrlToFilePath(member.getImgUrl()));
                fileService.deleteFile(FirebaseUtil.convertUrlToFilePath(member.getImgUrl()));
                member.setImgUrl(null);
            }
        }
        return member;
        //return memberRepository.save(member);
    }

    @Override
    public Member updateMemberPassword(Member member, String password) {
        member.setPassword(passwordEncoder.encode(password));
        return member;
        //return memberRepository.save(member);
    }

    @Override
    public void deleteMember(Member member) {
        List<MemberStudy> memberStudyList = memberStudyRepository.findByMember(member);
        List<MemberStudyRes> studyList = studyRepositorySupport.getStudyList(member).orElse(new ArrayList<>());
        for (MemberStudy memberStudy:memberStudyList) {
            memberStudyRepository.delete(memberStudy);
        }

        for (MemberStudyRes study: studyList) {
            Long count = memberStudyRepositorySupport.getCountByStudyId(study.getStudyId());
            log.debug("deleteMember - {}: {}", study.getStudyId(), count);
            if (count != null && count == 0) {
                studyService.deleteStudy(study.getStudyId());
            }
        }

        member.setIsDeleted("Y");
        //memberRepository.save(member);
    }

}
