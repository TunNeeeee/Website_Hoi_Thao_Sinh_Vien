package com.hutech.hoithao.repository;

import com.hutech.hoithao.models.Match;
import com.hutech.hoithao.models.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Integer> {
    // Bạn có thể thêm query tùy chỉnh nếu cần
    List<Match> findMatchByRound(Round round);
    List<Match> findByTeam1IdInOrTeam2IdIn(List<Integer> team1Ids, List<Integer> team2Ids);
}
