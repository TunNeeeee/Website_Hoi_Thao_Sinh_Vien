package com.hutech.hoithao.service;

import com.hutech.hoithao.domains.dtos.TeamDTO;
import com.hutech.hoithao.models.*;
import com.hutech.hoithao.repository.MatchRepository;
import com.hutech.hoithao.repository.RoundRepository;
import com.hutech.hoithao.repository.SportRepository;
import com.hutech.hoithao.repository.TeamRepository;
import com.hutech.hoithao.utils.mappers.MatchMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class MatchService {
    MatchRepository matchRepository;
    TeamRepository teamRepository;
    RoundRepository roundRepository;
    MatchMapper matchMapper;
    SportRepository sportRepository;

    public Match saveMatch(Match match) {
        return matchRepository.save(match);
    }
    public List<Match> saveAll(List<Match> matches) {
        return matchRepository.saveAll(matches);
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

    public Match findByTeams(Team team1, Team team2) {
        return matchRepository.findByTeam1AndTeam2(team1, team2)
                .orElse(null); // Nếu không tìm thấy, trả về null
    }

    public Map<String, List<MatchDTO>> getMatchesGroupedByRound() {
        // Lấy danh sách các vòng đấu từ roundId = 2 trở đi
        List<Round> rounds = roundRepository.findByIdGreaterThanEqual(2);

        // Lấy tất cả các trận đấu thuộc những vòng này
        List<Match> matches = matchRepository.findAllByRoundIdIn(
                rounds.stream().map(Round::getId).collect(Collectors.toList())
        );

        // Chuyển đổi Match sang MatchDTO
        List<MatchDTO> matchDTOs = matches.stream()
                .map(matchMapper::toDTO)
                .toList();

        // Nhóm các trận đấu theo tên vòng đấu
        return matchDTOs.stream()
                .collect(Collectors.groupingBy(match -> getRoundName(match.getRound().getId())));
    }


    public String getRoundName(Integer roundId) {
        return roundRepository.findById(roundId)
                .map(Round::getRoundName)
                .orElse("Không xác định");
    }

    public List<Match> getMatchesByRoundIds(List<Integer> roundIds) {
        return matchRepository.findAllByRoundIdIn(roundIds);
    }
    public Optional<Match> findMatch(Team team1, Team team2, Round round) {
        return matchRepository.findByTeam1AndTeam2AndRound(team1, team2, round);
    }
//    public List<Match> getLastThreeMatchesByTeamInRound(Integer teamId, int round) {
//        Pageable pageable = PageRequest.of(0, 3); // Lấy 3 trận đầu tiên
//        return matchRepository.findLastThreeMatchesByTeamIdAndRound(teamId, pageable);
//    }
public void generateMatches(Integer sportId) {
    Sport sport = sportRepository.findById(sportId)
            .orElseThrow(() -> new IllegalArgumentException("Sport không tồn tại"));

    List<Team> teams = teamRepository.findBySportId(sportId);
    if (teams.size() < 2) {
        throw new IllegalStateException("Cần ít nhất 2 đội để tạo trận đấu");
    }

    int rounds = (int) (Math.log(teams.size()) / Math.log(2));
    Queue<Team> teamQueue = new LinkedList<>(teams);

    List<Match> matches = new ArrayList<>();
    for (int round = 1; round <= rounds; round++) {
        int matchesInRound = (int) Math.pow(2, rounds - round);
        for (int i = 0; i < matchesInRound; i++) {
            Match match = new Match();
            Round round1 = new Round();
            round1.setId(round);
            match.setRound(round1);
            match.setTeam1(teamQueue.poll());
            match.setTeam2(teamQueue.poll());
            matches.add(match);
        }
    }
    matchRepository.saveAll(matches);
}
}

