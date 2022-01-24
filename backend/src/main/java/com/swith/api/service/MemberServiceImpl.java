package com.swith.api.service;

import com.swith.api.dto.member.request.MemberInfoReq;
import com.swith.common.util.MailUtil;
import com.swith.db.entity.AuthMail;
import com.swith.common.util.SecurityUtil;
import com.swith.db.entity.Member;
import com.swith.db.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Transactional
@Service
public class MemberServiceImpl implements MemberService {
    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    MailUtil mailUtil;

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
        String tempPassword = makeTempPassword();

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
        return memberRepository.findByEmail(SecurityUtil.getCurrentUsername().orElse(null)).orElse(null);
    }

    @Override
    public Member updateMember(Member member, MemberInfoReq memberInfoReq) {
        member.setNickname(memberInfoReq.getNickname());
        member.setGoal(memberInfoReq.getGoal());
        return memberRepository.save(member);
    }

    @Override
    public Member updateMemberPassword(Member member, String password) {
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        return memberRepository.save(member);
    }

    @Override
    public void deleteMember(Member member) {
        memberRepository.delete(member);
    }

}
