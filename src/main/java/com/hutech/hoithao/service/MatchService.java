package com.hutech.hoithao.service;

import com.hutech.hoithao.models.*;
import com.hutech.hoithao.repository.MatchRepository;
import com.hutech.hoithao.repository.RoundRepository;
import com.hutech.hoithao.repository.SportRepository;
import com.hutech.hoithao.repository.TeamRepository;
import com.hutech.hoithao.utils.mappers.MatchMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

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
    SportService sportService;
    RoundService roundService;
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
    public List<Match> findMatchesBySportAndRound(Integer idSport, Round round) {
        // Lấy danh sách các đội thuộc môn thể thao
        List<Team> teams = teamRepository.findBySportId(idSport);

        // Lấy danh sách ID của các đội
        List<Integer> teamIds = teams.stream().map(Team::getId).toList();

        // Lấy danh sách trận đấu có liên quan đến các đội này và round = round
        return matchRepository.findByTeam1IdInOrTeam2IdInAndRound(teamIds, teamIds, round);
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
    public void generateRandomPlayoffMatches(Integer idSport) {
        // 1. Lấy danh sách các đội đã được duyệt và có status = 2
        List<Team> eligibleTeams = teamRepository.findBySportIdAndStatus(idSport, 2);

        if (eligibleTeams.size() < 2) {
            throw new IllegalArgumentException("Không đủ đội với status = 2 để tạo playoff.");
        }

        // 2. Xáo trộn đội để tạo cặp đấu ngẫu nhiên
        Collections.shuffle(eligibleTeams);

        // 3. Lấy thông tin môn thể thao
        Sport sport = sportService.findById(idSport);
        if (sport == null) {
            throw new IllegalArgumentException("Môn thể thao không tồn tại.");
        }

        int numberTeamMax = sport.getNumberTeamMax();
        String roundName;
        int roundNumber;
        switch (numberTeamMax) {
            case 16:
                roundName = "Vòng 1/16";
                roundNumber = 2; // Ví dụ: mã định danh vòng
                break;
            case 8:
                roundName = "Tứ kết";
                roundNumber = 3;
                break;
            case 4:
                roundName = "Bán kết";
                roundNumber = 4;
                break;
            case 2:
                roundName = "Chung kết";
                roundNumber = 5;
                break;
            default:
                throw new IllegalArgumentException("Số đội tối đa không hợp lệ.");
        }

        // 4. Lấy đối tượng Round (cần đảm bảo rằng Round đã tồn tại trong DB)
        Round firstRound = roundService.findById(roundNumber);

        // 5. Tạo danh sách trận đấu
        List<Match> matches = new ArrayList<>();
        for (int i = 0; i < eligibleTeams.size(); i += 2) {
            if (i + 1 < eligibleTeams.size()) {
                Team team1 = eligibleTeams.get(i);
                Team team2 = eligibleTeams.get(i + 1);

                Match match = new Match();
                // Không thiết lập ID thủ công
                match.setTeam1(team1);
                match.setTeam2(team2);
                match.setMatchName("PO-" + team1.getId() + "-" + team2.getId());
                match.setRound(firstRound);
                // Thiết lập các thuộc tính khác nếu cần, ví dụ:
                // match.setArena(arena);
                // match.setMatchDate(LocalDateTime.now());
                matches.add(match);
            } else {
                // Nếu số đội lẻ, tạo trận đấu với đội cuối cùng và đối thủ là "BOT" (Bị loại)
                Team team = eligibleTeams.get(i);
                Match match = new Match();
                match.setTeam1(team);
                match.setTeam2(null); // Không đối thủ
                match.setMatchName("PO-" + team.getId() + "-BOT");
                match.setRound(firstRound);
                matches.add(match);
            }
        }

        // 6. Lưu danh sách trận đấu vào DB
        matchRepository.saveAll(matches);
    }
}

