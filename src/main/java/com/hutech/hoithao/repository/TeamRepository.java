package com.hutech.hoithao.repository;

import com.hutech.hoithao.models.Group;
import com.hutech.hoithao.models.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface TeamRepository extends JpaRepository<Team, Integer> {
    //Tim theo Id sport
    List<Team> findAllByOrderByIdAsc();
    List<Team> findBySportId(Integer id);
    //Tim team theo bang dau
    List<Team> findTeamsByGroup_Id(Integer id);
    List<Team> findByGroupOrderByPointDesc(Group group);
    List<Team> findTeamByStatus(Integer status);
    List<Team> findBySportIdAndStatus(Integer id, Integer status);
    List<Team> findAllByOrderByNoFinalAsc();
    List<Team> findAllByOrderByNoRankAsc();
    List<Team> findBySportIdOrderByNoFinalAsc(Integer id);

    long countBySportIdAndStatus(Integer sportId, int status);
    List<Team> findByStatusIn(List<Integer> statuses);

    List<Team> findBySportIdAndStatusIn(Integer sportId, List<Integer> statuses);
    @Query("SELECT t FROM Team t WHERE t.sport.id = :idSport AND t.group IS NULL AND t.status = 2")
    List<Team> findTeamsNotInAnyGroupBySport(@Param("idSport") Integer idSport);
    @Query("SELECT t FROM Team t WHERE t.group.id = :groupId")
    List<Team> findTeamsByGroupId(@Param("groupId") Integer groupId);
    @Query("SELECT t FROM Team t WHERE t.group.id = :groupId ORDER BY t.point DESC, t.hs DESC")
    List<Team> findTeamsByRankIdSorted(@Param("groupId") int groupId);
    @Query("SELECT t FROM Team t WHERE t.group.id = :groupId ORDER BY t.point DESC, t.hs DESC")
    List<Team> findSortedTeamsByGroup(@Param("groupId") Integer groupId);
}
