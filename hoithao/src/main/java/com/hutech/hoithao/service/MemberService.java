package com.hutech.hoithao.service;

import com.hutech.hoithao.models.Event;
import com.hutech.hoithao.models.Member;
import com.hutech.hoithao.models.Status_Event;
import com.hutech.hoithao.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class MemberService {
    @Autowired
    private MemberRepository memberRepository;
    public void saveMember(Member member) {
        memberRepository.save(member);
    }
    public void saveAllMembers(Set<Member> members) {
        memberRepository.saveAll(members);
    }
    public List<Member> getMemberByTeamId(Integer id) {
        return memberRepository.findMembersByTeamId(id);
    }
}
