package com.hutech.hoithao.repository;

import com.hutech.hoithao.models.Group;
import com.hutech.hoithao.models.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface TeamRepository extends JpaRepository<Team, Integer> {
    //Tim theo Id sport
    List<Team> findBySportId(Integer id);
    //Tim team theo bang dau
    List<Team> findTeamsByGroup_Id(Integer id);
    List<Team> findByGroupOrderByPointDesc(Group group);
    List<Team> findTeamByStatus(Integer status);
    List<Team> findBySportIdAndStatus(Integer id, Integer status);
    List<Team> findAllByOrderByNoFinalAsc();
    List<Team> findAllByOrderByNoRankAsc();
    List<Team> findBySportIdOrderByNoFinalAsc(Integer id);


}
