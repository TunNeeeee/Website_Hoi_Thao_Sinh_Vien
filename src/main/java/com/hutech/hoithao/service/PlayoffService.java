package com.hutech.hoithao.service;

import com.hutech.hoithao.domains.dtos.TeamDTO;
import com.hutech.hoithao.models.Match;
import com.hutech.hoithao.models.MatchDTO;
import com.hutech.hoithao.models.Round;
import com.hutech.hoithao.models.Team;
import com.hutech.hoithao.repository.MatchRepository;
import com.hutech.hoithao.repository.RoundRepository;
import com.hutech.hoithao.repository.TeamRepository;
import com.hutech.hoithao.utils.mappers.TeamMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class PlayoffService {
    MatchRepository matchRepository;
    TeamRepository teamRepository;
    RoundRepository roundRepository;
    TeamMapper teamMapper;

    public Map<String, List<MatchDTO>> getBracketRounds(Integer sportId) {
        // 1. Lấy danh sách các đội của môn thể thao
        List<Team> teams = teamRepository.findBySportId(sportId);
        List<Integer> teamIds = teams.stream().map(Team::getId).toList();

        // 2. Truy vấn các trận đấu, nhóm theo số đội trong mỗi vòng
        Map<String, List<Match>> matchesByRound = new LinkedHashMap<>();
        int teamCount = teamIds.size();
        int round = 1;

        while (teamCount >= 2) { // Dừng khi số đội ít hơn 2
            List<Match> matches = matchRepository.findByRoundAndTeams(round, teamIds);
            String roundName = getRoundName(teamCount);

            matchesByRound.put(roundName, matches);

            teamCount /= 2; // Mỗi vòng số đội giảm đi một nửa
            round++;
        }

        // 3. Chuyển đổi sang DTO
        Map<String, List<MatchDTO>> matchesByRoundDTO = new LinkedHashMap<>();
        for (var entry : matchesByRound.entrySet()) {
            List<MatchDTO> matchDTOs = entry.getValue().stream().map(this::toDTO).toList();
            matchesByRoundDTO.put(entry.getKey(), matchDTOs);
        }

        return matchesByRoundDTO;
    }

    // Giả sử bạn có phương thức để chuyển Match thành MatchDTO
    public MatchDTO toDTO(Match match) {
        if (match == null) {
            return null; // Kiểm tra null cho match nếu cần
        }

        // Lấy team1 và team2 từ match
        TeamDTO team1DTO = teamMapper.toDTO(match.getTeam1());
        TeamDTO team2DTO = teamMapper.toDTO(match.getTeam2());

        // Kiểm tra nếu điểm là null và thay thế giá trị nếu cần
        Integer winnerTeam = null;
        if (match.getPoint1() != null && match.getPoint2() != null) {
            if (match.getPoint1() > match.getPoint2()) {
                winnerTeam = 1;
            } else if (match.getPoint1() < match.getPoint2()) {
                winnerTeam = 2;
            } else {
                winnerTeam = 0; // Hòa
            }
        }

        // Tạo MatchDTO với các giá trị lấy từ Match
        return MatchDTO.builder()
                .id(match.getId())
                .team1(team1DTO)
                .team2(team2DTO)
                .point1(match.getPoint1())
                .point2(match.getPoint2())
                .bonuspoint1(match.getBonuspoint1())
                .bonuspoint2(match.getBonuspoint2())
                .winner(winnerTeam)
                .build();
    }

    private String getRoundName(int teamCount) {
        if (teamCount == 16) {
            return "Vòng 16 đội";
        } else if (teamCount == 8) {
            return "Tứ kết";
        } else if (teamCount == 4) {
            return "Bán kết";
        } else if (teamCount == 2) {
            return "Chung kết";
        } else {
            return "Vòng không xác định";
        }
    }

    public List<MatchDTO> getAllMatchesFromRoundId(Integer startRoundId) {
        // Lấy danh sách roundId từ vòng đấu >= startRoundId
        List<Integer> roundIds = roundRepository.findByIdGreaterThanEqual(startRoundId)
                .stream()
                .map(Round::getId)
                .collect(Collectors.toList());

        // Lấy danh sách các trận đấu thuộc những roundIds này
        List<Match> matches = matchRepository.findAllByRoundIdIn(roundIds);

        // Chuyển đổi Match sang MatchDTO
        return matches.stream()
                .map(this::convertToMatchDTO)
                .collect(Collectors.toList());
    }


    private MatchDTO convertToMatchDTO(Match match) {
        return MatchDTO.builder()
                .id(match.getId())
                .team1(new TeamDTO(match.getTeam1().getId(), match.getTeam1().getTeamName()))
                .team2(new TeamDTO(match.getTeam2().getId(), match.getTeam2().getTeamName()))
                .point1(match.getPoint1())
                .point2(match.getPoint2())
                .round(match.getRound())
                .build();
    }

}
