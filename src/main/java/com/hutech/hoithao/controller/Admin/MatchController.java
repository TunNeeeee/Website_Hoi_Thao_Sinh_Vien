package com.hutech.hoithao.controller.Admin;

import com.hutech.hoithao.models.*;
import com.hutech.hoithao.repository.ArenaRepository;
import com.hutech.hoithao.repository.GroupRepository;
import com.hutech.hoithao.repository.MatchRepository;
import com.hutech.hoithao.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/{idSport}")
public class MatchController {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private GroupService groupService;

    @Autowired
    private SportService sportService;

    @Autowired
    private ArenaService arenaService;

    @Autowired
    private MatchService matchService;

    @Autowired
    private TeamService teamService;
    // Trang tạo trận đấu
    @GetMapping("/create-match-vb")
    public String showCreateMatchForm(@PathVariable Integer idSport, Model model) {
        model.addAttribute("groups", groupRepository.findBySportId(idSport));
        model.addAttribute("match", new Match());
        model.addAttribute("arenas", arenaService.findAll());
        return "/admin/match/create-match";
    }
    // Lấy danh sách đội theo bảng (API)
    @GetMapping("/groups/{groupId}/teams")
    @ResponseBody
    public List<TeamDTO> getTeamsByGroup(@PathVariable Integer groupId) {
        return groupService.getGroupWithTeams(groupId)
                .map(group -> group.getListTeam().stream()
                        .map(team -> new TeamDTO(team.getId(), team.getTeamName()))
                        .toList())
                .orElse(Collections.emptyList());
    }



    // Lưu thông tin trận đấu
    @PostMapping("/create-match-vb")
    public String saveMatch(@PathVariable Integer idSport,
                            @ModelAttribute Match match,
                            @RequestParam("groupId") Integer groupId,
                            RedirectAttributes redirectAttributes) {
        Sport sport = sportService.findById(idSport);
        Group group = groupService.getGroupById(groupId);
        Round round = new Round();
        round.setId(1);
        List<Match> existingMatches = matchService.getMatchesByRound(round); // Chỉ lấy các trận VB

        // Lấy tên viết tắt môn thể thao
        String sportAbbreviation = Arrays.stream(sport.getSportName().split(" "))
                .map(word -> word.substring(0, 1).toUpperCase())
                .collect(Collectors.joining());

        // Tạo số trận đấu tiếp theo
        int nextMatchNumber = existingMatches.size() + 1;

        // Tạo tên trận đấu
        String matchName = String.format("%s-VB%s-T%d", sportAbbreviation, group.getGroupName().toUpperCase(), nextMatchNumber);

        // Gán tên trận đấu
        match.setMatchName(matchName);
        match.setRound(round);
        match.setPoint1(-1);
        match.setPoint2(-1);
        match.setBonuspoint1(-1);
        match.setBonuspoint2(-1);
        // Lưu trận đấu
        matchService.saveMatch(match);

        redirectAttributes.addFlashAttribute("successMessage", "Lưu trận đấu thành công!");
        return "redirect:/admin/" + idSport + "/groups";
    }
    @GetMapping("/update-match/{matchId}")
    public String showUpdateMatchForm(@PathVariable Integer idSport, @PathVariable Integer matchId, Model model) {
        Match match = matchService.findById(matchId); // Tìm trận đấu theo ID
        if (match == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Match not found");
        }
        model.addAttribute("match", match);
        model.addAttribute("sportId", idSport);
        return "admin/match/update-match"; // Tên view Thymeleaf
    }

    @PostMapping("/update-match/{matchId}")
    public String updateMatchResult(
            @PathVariable Integer idSport,
            @PathVariable Integer matchId,
            @RequestParam int point1,
            @RequestParam int point2,
            RedirectAttributes redirectAttributes) {

        if (point1 < 0 || point2 < 0) {
            redirectAttributes.addFlashAttribute("error", "Điểm số không được nhỏ hơn 0!");
            return "redirect:/admin/" + idSport + "/update-match/" + matchId;
        }

        Match match = matchService.findById(matchId);
        if (match == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Match not found");
        }
        match.setPoint1(point1);
        match.setPoint2(point2);
        Team team1 = match.getTeam1();
        Team team2 = match.getTeam2();
        // Tăng số trận đấu đã chơi
        team1.setNumberGame(team1.getNumberGame() + 1);
        team2.setNumberGame(team2.getNumberGame() + 1);

        // Cập nhật điểm số dựa trên kết quả trận đấu
        if (point1 > point2) {
            team1.setPoint(team1.getPoint() + 3); // Đội 1 thắng
        } else if (point1 < point2) {
            team2.setPoint(team2.getPoint() + 3); // Đội 2 thắng
        } else {
            team1.setPoint(team1.getPoint() + 1); // Hòa
            team2.setPoint(team2.getPoint() + 1); // Hòa
        }
        team1.setHs(team1.getHs()+(point1-point2));
        team2.setHs(team2.getHs()+point2-point1);
        // Lưu các thay đổi
        teamService.saveTeam(team1);
        teamService.saveTeam(team2);
//         Cập nhật thứ hạng của các đội trong cùng một group
//        int rankId = team1.getGroup().getId();
//        teamService.updateTeamRankings(rankId);
        matchService.saveMatch(match);

        redirectAttributes.addFlashAttribute("success", "Cập nhật kết quả thành công!");
        return "redirect:/admin/" + idSport + "/groups"; // Điều hướng về danh sách trận đấu
    }
    @GetMapping("/update-point/{matchId}")
    public String showUpdatePointForm(@PathVariable Integer idSport, @PathVariable Integer matchId, Model model) {
        Match match = matchService.findById(matchId); // Tìm trận đấu theo ID
        if (match == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Match not found");
        }
        model.addAttribute("match", match);
        model.addAttribute("sportId", idSport);
        return "admin/match/update-point"; // Tên view Thymeleaf
    }
    @PostMapping("/update-point/{matchId}")
    public String updateMatchResult(@PathVariable Integer idSport, @PathVariable Integer matchId,
                                    @RequestParam int newPoint1, @RequestParam int newPoint2) {
        Match match = matchService.findById(matchId);

        if (match.getPoint1() != -1 && match.getPoint2() != -1) {
            // Lấy đội liên quan
            Team team1 = match.getTeam1();
            Team team2 = match.getTeam2();

            // Hoàn tác ảnh hưởng từ kết quả cũ
            undoMatchResult(match, team1, team2);

            // Cập nhật kết quả mới
            applyMatchResult(match, team1, team2, newPoint1, newPoint2);
        }

        // Lưu lại thay đổi
        match.setPoint1(newPoint1);
        match.setPoint2(newPoint2);
        matchService.saveMatch(match);

        return "redirect:/admin/" + idSport + "/groups";
    }

    private void undoMatchResult(Match match, Team team1, Team team2) {
        // Giảm số trận của cả hai đội
        team1.setNumberGame(team1.getNumberGame() - 1);
        team2.setNumberGame(team2.getNumberGame() - 1);

        // Hoàn tác hiệu số và điểm
        int oldPoint1 = match.getPoint1();
        int oldPoint2 = match.getPoint2();

        team1.setHs(team1.getHs() - (oldPoint1 - oldPoint2));
        team2.setHs(team2.getHs() - (oldPoint2 - oldPoint1));

        if (oldPoint1 > oldPoint2) {
            team1.setPoint(team1.getPoint() - 3);
        } else if (oldPoint1 < oldPoint2) {
            team2.setPoint(team2.getPoint() - 3);
        } else {
            team1.setPoint(team1.getPoint() - 1);
            team2.setPoint(team2.getPoint() - 1);
        }
    }

    private void applyMatchResult(Match match, Team team1, Team team2, int newPoint1, int newPoint2) {
        // Tăng số trận
        team1.setNumberGame(team1.getNumberGame() + 1);
        team2.setNumberGame(team2.getNumberGame() + 1);

        // Cập nhật hiệu số và điểm
        team1.setHs(team1.getHs() + (newPoint1 - newPoint2));
        team2.setHs(team2.getHs() + (newPoint2 - newPoint1));

        if (newPoint1 > newPoint2) {
            team1.setPoint(team1.getPoint() + 3);
        } else if (newPoint1 < newPoint2) {
            team2.setPoint(team2.getPoint() + 3);
        } else {
            team1.setPoint(team1.getPoint() + 1);
            team2.setPoint(team2.getPoint() + 1);
        }
    }
}
