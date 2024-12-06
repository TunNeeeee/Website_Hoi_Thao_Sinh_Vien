package com.hutech.hoithao.repository;

import com.hutech.hoithao.models.Match;
import com.hutech.hoithao.models.Round;
import com.hutech.hoithao.models.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Integer> {
    // Bạn có thể thêm query tùy chỉnh nếu cần
    List<Match> findMatchByRound(Round round);
    List<Match> findByTeam1IdInOrTeam2IdIn(List<Integer> team1Ids, List<Integer> team2Ids);
    @Query("SELECT m FROM Match m " +
            "WHERE (m.team1.id IN :teamIds OR m.team2.id IN :teamIds) " +
            "AND m.round.id = :roundId")
    List<Match> findByRoundAndTeams(@Param("roundId") Integer roundId, @Param("teamIds") List<Integer> teamIds);

    Optional<Match> findByTeam1AndTeam2(Team team1, Team team2);
    List<Match> findAllByRoundId(Integer roundId);
    List<Match> findAllByRoundIdIn(List<Integer> roundIds);
    Optional<Match> findByTeam1AndTeam2AndRound(Team team1, Team team2, Round round);
}
