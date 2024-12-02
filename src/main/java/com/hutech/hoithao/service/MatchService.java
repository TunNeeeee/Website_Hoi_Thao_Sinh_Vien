package com.hutech.hoithao.service;

import com.hutech.hoithao.models.Match;
import com.hutech.hoithao.models.Round;
import com.hutech.hoithao.models.Team;
import com.hutech.hoithao.repository.MatchRepository;
import com.hutech.hoithao.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MatchService {

    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private TeamRepository teamRepository;
    public Match saveMatch(Match match) {
        return matchRepository.save(match);
    }
    public List<Match> getMatchesByRound(Round round) {
        return matchRepository.findMatchByRound(round);
    }
    public List<Match> findMatchesBySport(Integer idSport) {
        List<Team> teams = teamRepository.findBySportId(idSport); // Lấy danh sách các đội thuộc môn thể thao
        List<Integer> teamIds = teams.stream().map(Team::getId).toList(); // Lấy danh sách ID của các đội

        // Lấy danh sách trận đấu có liên quan đến các đội này
        return matchRepository.findByTeam1IdInOrTeam2IdIn(teamIds, teamIds);
    }
    public Match findById(Integer id) {
        return matchRepository.findById(id).orElse(null);
    }
}

