package com.hutech.hoithao.service;

import com.hutech.hoithao.models.Match;
import com.hutech.hoithao.models.Round;
import com.hutech.hoithao.models.Team;
import com.hutech.hoithao.repository.MatchRepository;
import com.hutech.hoithao.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamService {
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private MatchRepository matchRepository;
    // Lưu team vào cơ sở dữ liệu
    public void saveTeam(Team team) {
        teamRepository.save(team);
    }
    public List<Team> findAllTeams() {
        return teamRepository.findAllByOrderByIdAsc();
    }
    public List<Team> findTeamsBySportId(Integer sportId) {
        return teamRepository.findBySportId(sportId);
    }
    public Team findTeamById(Integer id) {
        return teamRepository.findById(id).orElse(null);
    }
    public long countApprovedTeamsBySport(Integer sportId) {
        return teamRepository.countBySportIdAndStatus(sportId, 2); // 2 là trạng thái đã duyệt
    }
    public long countFinishTeamBySport(Integer sportId) {
        return teamRepository.countBySportIdAndStatus(sportId, 0);
    }
    public List<Team> findTeamsByStatus(Integer sportId, List<Integer> statuses) {
        if (sportId == null) {
            return teamRepository.findByStatusIn(statuses);
        } else {
            return teamRepository.findBySportIdAndStatusIn(sportId, statuses);
        }
    }
    public List<Team> getTeamsNotInAnyGroup(Integer idSport) {
        return teamRepository.findTeamsNotInAnyGroupBySport(idSport);
    }
    public List<Team> getTeamsByIds(List<Integer> ids) {
        return teamRepository.findAllById(ids);
    }
    // Lưu danh sách các đội
    public List<Team> saveAll(List<Team> teams) {
        return teamRepository.saveAll(teams);
    }
    public List<Team> getSortedTeamsByGroup(Integer groupId) {
        List<Team> teams = teamRepository.findSortedTeamsByGroup(groupId);
        // Sắp xếp theo điểm (giảm dần), nếu bằng thì xét hiệu số
        teams.sort((team1, team2) -> {
            int pointCompare = team2.getPoint().compareTo(team1.getPoint());
            if (pointCompare != 0) {
                return pointCompare;
            }
            // Nếu điểm bằng nhau, so sánh hiệu số
            return team2.getHs().compareTo(team1.getHs());
        });
        return teams;
    }
    public List<Team> getTeamsBySportSortedByRanking(Integer sportId) {
        List<Team> teams = teamRepository.findBySportId(sportId);
        return teams.stream()
                .sorted(Comparator.comparingInt(Team::getPoint).reversed()
                        .thenComparingInt(Team::getHs).reversed())
                .collect(Collectors.toList());
    }
    public List<Team> findBySportAndNoRankAndStatus(Integer sportId, int noRank, int status) {
        return teamRepository.findBySportAndNoRankAndStatus(sportId, noRank, status);
    }
    public List<Team> findBySportAndNoRankAndStatusOrdered(Integer sportId, Integer noRank, Integer status) {
        return teamRepository.findBySportIdAndNoRankAndStatusOrdered(sportId, noRank, status);
    }
    public List<Team> findBySportAndNoRankOrdered(Integer sportId, Integer noRank) {
        return teamRepository.findBySportIdAndNoRankOrdered(sportId, noRank);
    }
    public List<String> getLastThreeResults(Integer teamId) {
        Pageable pageable = PageRequest.of(0, 3); // Giới hạn 3 trận
        Round round = new Round();
        round.setId(1);
        List<Match> matches = matchRepository.findLastThreeMatchesByTeamIdAndRound(teamId, round, pageable);

        return matches.stream()
                .map(match -> {
                    if (match.getTeam1().getId().equals(teamId)) {
                        return match.getPoint1() > match.getPoint2() ? "W" :
                                match.getPoint1().equals(match.getPoint2()) ? "D" : "L";
                    } else {
                        return match.getPoint2() > match.getPoint1() ? "W" :
                                match.getPoint2().equals(match.getPoint1()) ? "D" : "L";
                    }
                })
                .collect(Collectors.toList());
    }
}
