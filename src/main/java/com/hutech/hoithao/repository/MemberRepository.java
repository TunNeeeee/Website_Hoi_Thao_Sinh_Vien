package com.hutech.hoithao.repository;

import com.hutech.hoithao.models.Member;
import com.hutech.hoithao.models.Sport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, Integer> {
    List<Member> findMembersByTeamId(Integer id);
    List<Member> findByNameMemberAndMssv(String nameMember, String mssv);
}
