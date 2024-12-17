package com.hutech.hoithao.controller.Admin;

import com.hutech.hoithao.domains.dtos.TeamDTO;
import com.hutech.hoithao.models.*;
import com.hutech.hoithao.repository.GroupRepository;
import com.hutech.hoithao.service.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/{idSport}")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class MatchController {
    private GroupRepository groupRepository;
    private GroupService groupService;
    private SportService sportService;
    private ArenaService arenaService;
    private MatchService matchService;
    private TeamService teamService;
    private RoundService roundService;

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
        team1.setHs(team1.getHs() + (point1 - point2));
        team2.setHs(team2.getHs() + point2 - point1);
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

    //-----------------------------------------------------------------------------------------//
    @GetMapping("/updateMatch/{team1Id}/{team2Id}/{roundId}")
    public String showUpdateForm(
            @PathVariable Integer team1Id,
            @PathVariable Integer team2Id,
            @PathVariable("idSport") Integer sportId,
            @PathVariable Integer roundId,
            Model model,
            RedirectAttributes redirectAttributes) {
        // Tìm team1 và team2
        Team team1 = teamService.findTeamById(team1Id);
        Team team2 = teamService.findTeamById(team2Id);

        if (team1 == null || team2 == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đội tham gia không hợp lệ!");
            return "redirect:/admin/sport";
        }

        // Kiểm tra nếu trận đấu đã tồn tại
        Match existingMatch = matchService.findByTeams(team1, team2);

        if (existingMatch == null) {
            // Lấy thông tin môn thi đấu
            Sport sport = sportService.findSportByTeam(team1); // Ví dụ: dựa vào team để lấy môn
            if (sport == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không xác định được môn thi đấu!");
                return "redirect:/admin/sport";
            }

            // Lấy viết tắt môn thi đấu và vòng đấu
            String sportAbbreviation = getAbbreviation(sport.getSportName()); // Viết tắt của môn
            String roundAbbreviation = "PO"; // Vòng Playoff viết tắt

            // Tạo mã trận đấu
            String matchCode = sportAbbreviation + "-" + roundAbbreviation + "-" + team1Id + "-" + team2Id;

            // Tạo mới trận đấu
            Match newMatch = new Match();
            newMatch.setTeam1(team1);
            newMatch.setTeam2(team2);
            newMatch.setMatchName(matchCode); // Đặt tên mặc định
            newMatch.setPoint1(-1);
            newMatch.setPoint2(-1);
            newMatch.setBonuspoint1(-1);
            newMatch.setBonuspoint2(-1);

            // Lưu trận đấu vào DB
            existingMatch = matchService.saveMatch(newMatch);
        }

        model.addAttribute("roundId", roundId);
        model.addAttribute("sportId", sportId);
        model.addAttribute("teams", teamService.findAllTeams());
        // Gửi thông tin trận đấu đến view để cập nhật
        model.addAttribute("match", existingMatch);
        model.addAttribute("arenas", arenaService.findAll());
        return "admin/playoff/create_match";
    }

    /**
     * Hàm lấy viết tắt của tên (Ví dụ: "Bóng Đá" -> "BD").
     */
    private String getAbbreviation(String name) {
        if (name == null || name.isEmpty()) return "";
        return Arrays.stream(name.split("\\s+"))
                .map(word -> word.substring(0, 1).toUpperCase())
                .collect(Collectors.joining());
    }


    @PostMapping("/updateMatch/{matchId}/{roundId}")
    public String updateMatch(@PathVariable Integer matchId,
                              @ModelAttribute Match match,
                              @PathVariable Integer roundId,
                              @RequestParam("arenaId") Integer arenaId,
                              RedirectAttributes redirectAttributes) {
        // Tìm trận đấu theo matchId
        Match existingMatch = matchService.findById(matchId);

//         Tìm kiếm đội 1 và đội 2 từ ID của các đội được gửi từ form
        Team team1 = teamService.findTeamById(match.getTeam1().getId());  // Ensure this gets the team object
        Team team2 = teamService.findTeamById(match.getTeam2().getId());  // Ensure this gets the team object

        // Kiểm tra nếu đội 1 hoặc đội 2 không tồn tại
//        if (team1 == null || team2 == null) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Đội không hợp lệ!");
//            return "redirect:/admin/{idSport}/match";  // Or an appropriate redirect URL
//        }
        if (existingMatch == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Trận đấu không tồn tại!");
            return "redirect:/admin/sport";  // Redirect về trang chính nếu trận đấu không tồn tại
        }

        // Lấy Arena từ arenaId
        Arena arena = arenaService.findById(arenaId);
        if (arena == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Địa điểm không hợp lệ!");
            return "redirect:/admin/sport";
        }

        // Lấy thông tin môn thể thao từ team1 của trận đấu
        Sport sport = team1.getSport();
        if (sport == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Môn thể thao không hợp lệ!");
            return "redirect:/admin/sport";
        }
        // Lấy viết tắt môn thi đấu và vòng đấu
        String sportAbbreviation = getAbbreviation(sport.getSportName()); // Viết tắt của môn
        String roundAbbreviation = "PO"; // Vòng Playoff viết tắt

        // Tạo mã trận đấu
        String matchCode = sportAbbreviation + "-" + roundAbbreviation + "-" + match.getTeam1().getId() + "-" + match.getTeam2().getId();
        Round round = roundService.findById(roundId);
        // Cập nhật thông tin trận đấu
        existingMatch.setTeam1(match.getTeam1());
        existingMatch.setTeam2(match.getTeam2());
        existingMatch.setMatchName(matchCode);
        existingMatch.setTime(match.getTime());
        existingMatch.setTimeStart(match.getTimeStart());
        existingMatch.setArena(arena);
        existingMatch.setRound(round);
        existingMatch.setPoint1(match.getPoint1());
        existingMatch.setPoint2(match.getPoint2());
        existingMatch.setBonuspoint1(match.getBonuspoint1());
        existingMatch.setBonuspoint2(match.getBonuspoint2());
        // Kiểm tra nếu point1 và point2 đều là -1
        if (existingMatch.getPoint1() != null && existingMatch.getPoint1() == -1 && existingMatch.getPoint2() != null && existingMatch.getPoint2() == -1) {
            existingMatch.setWinner(-1); // Nếu điểm của cả hai đội là -1, thiết lập winner = -1
        } else if (existingMatch.getPoint1() != null && existingMatch.getPoint1().equals(existingMatch.getPoint2())) {
            // Kiểm tra nếu điểm của cả hai đội bằng nhau, thì kiểm tra điểm phụ
            if (existingMatch.getBonuspoint1() != null && existingMatch.getBonuspoint2() != null) {
                // So sánh điểm phụ nếu có
                if (existingMatch.getBonuspoint1() > existingMatch.getBonuspoint2()) {
                    existingMatch.setWinner(1); // Đội 1 thắng
                } else if (existingMatch.getBonuspoint1() < existingMatch.getBonuspoint2()) {
                    existingMatch.setWinner(2); // Đội 2 thắng
                } else {
                    // Trường hợp điểm phụ bằng nhau, có thể cần xử lý thêm (ví dụ: hòa)
                    existingMatch.setWinner(0); // Hòa, hoặc xử lý theo cách của bạn
                }
            }
        } else {
            // Nếu điểm của hai đội khác nhau, đội có điểm cao hơn sẽ thắng
            existingMatch.setWinner(existingMatch.getPoint1() > existingMatch.getPoint2() ? 1 : 2);
        }


        // Xác định số đội (noFinal) và trạng thái
        int noFinalCurrent = 0;
        int noFinalNext = 0;
        switch (roundId) {
            case 2: // Vòng 1/16
                noFinalCurrent = 16;
                noFinalNext = 8;
                break;
            case 3: // Tứ kết
                noFinalCurrent = 8;
                noFinalNext = 4;
                break;
            case 4: // Bán kết
                noFinalCurrent = 4;
                noFinalNext = 2;
                break;
            case 5: // Chung kết
                noFinalCurrent = 2;
                noFinalNext = 1; // Thắng sẽ là vô địch
                break;
            default:
                redirectAttributes.addFlashAttribute("errorMessage", "Vòng đấu không hợp lệ!");
                return "redirect:/admin/sport";
        }

        // Cập nhật trạng thái của các đội
        if (existingMatch.getWinner() == 1) { // team1 thắng
            team1.setNoFinal(noFinalNext); // Tứ kết
            team1.setStatus(2);  // Giữ nguyên
            team2.setNoFinal(noFinalCurrent); // Top 8
            team2.setStatus(0);  // Bị loại

            // Kiểm tra nếu noRank == 1 thì chuyển status của team1 về 0
            if (team1.getNoFinal() == 1) {
                team1.setStatus(0);  // Chuyển trạng thái thành bị loại
            }

        } else if (existingMatch.getWinner() == 2) { // team2 thắng
            team2.setNoFinal(noFinalNext); // Tứ kết
            team2.setStatus(2);  // Giữ nguyên
            team1.setNoFinal(noFinalCurrent); // Top 8
            team1.setStatus(0);  // Bị loại

            // Kiểm tra nếu noRank == 1 thì chuyển status của team2 về 0
            if (team2.getNoRank() == 1) {
                team2.setStatus(0);  // Chuyển trạng thái thành bị loại
            }
        }

        // Lưu trạng thái đội
        teamService.saveTeam(team1);
        teamService.saveTeam(team2);
        // Lưu trận đấu đã cập nhật
        matchService.saveMatch(existingMatch);

        // Thêm thông báo thành công
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trận đấu thành công!");

        // Chuyển hướng về trang chi tiết trận đấu hoặc danh sách trận đấu
        return "redirect:/admin/" + sport.getId() + "/playoff";  // Redirect tới trang playoff của môn thể thao
    }
    //------------------------------------------------------------------------------------------------
    @GetMapping("/updateMatchTT/{team1Id}/{team2Id}/{roundId}")
    public String showUpdateFormTT(
            @PathVariable Integer team1Id,
            @PathVariable Integer team2Id,
            @PathVariable("idSport") Integer sportId,
            @PathVariable Integer roundId,
            Model model,
            RedirectAttributes redirectAttributes) {
        // Tìm team1 và team2
        Team team1 = teamService.findTeamById(team1Id);
        Team team2 = teamService.findTeamById(team2Id);

        if (team1 == null || team2 == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đội tham gia không hợp lệ!");
            return "redirect:/admin/sport";
        }

        // Kiểm tra nếu trận đấu đã tồn tại
        Match existingMatch = matchService.findByTeams(team1, team2);

        if (existingMatch == null) {
            // Lấy thông tin môn thi đấu
            Sport sport = sportService.findSportByTeam(team1); // Ví dụ: dựa vào team để lấy môn
            if (sport == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không xác định được môn thi đấu!");
                return "redirect:/admin/sport";
            }

            // Lấy viết tắt môn thi đấu và vòng đấu
            String sportAbbreviation = getAbbreviation(sport.getSportName()); // Viết tắt của môn
            String roundAbbreviation = "PO"; // Vòng Playoff viết tắt

            // Tạo mã trận đấu
            String matchCode = sportAbbreviation + "-" + roundAbbreviation + "-" + team1Id + "-" + team2Id;

            // Tạo mới trận đấu
            Match newMatch = new Match();
            newMatch.setTeam1(team1);
            newMatch.setTeam2(team2);
            newMatch.setMatchName(matchCode); // Đặt tên mặc định
            newMatch.setPoint1(-1);
            newMatch.setPoint2(-1);
            newMatch.setBonuspoint1(-1);
            newMatch.setBonuspoint2(-1);

            // Lưu trận đấu vào DB
            existingMatch = matchService.saveMatch(newMatch);
        }

        model.addAttribute("roundId", roundId);
        model.addAttribute("sportId", sportId);
        model.addAttribute("teams", teamService.findAllTeams());
        // Gửi thông tin trận đấu đến view để cập nhật
        model.addAttribute("match", existingMatch);
        model.addAttribute("arenas", arenaService.findAll());
        return "admin/playoff/create_match_tt";
    }



    @PostMapping("/updateMatchTT/{matchId}/{roundId}")
    public String updateMatchTT(@PathVariable Integer matchId,
                              @ModelAttribute Match match,
                              @PathVariable Integer roundId,
                              @RequestParam("arenaId") Integer arenaId,
                              RedirectAttributes redirectAttributes) {
        // Tìm trận đấu theo matchId
        Match existingMatch = matchService.findById(matchId);

//         Tìm kiếm đội 1 và đội 2 từ ID của các đội được gửi từ form
        Team team1 = teamService.findTeamById(match.getTeam1().getId());  // Ensure this gets the team object
        Team team2 = teamService.findTeamById(match.getTeam2().getId());  // Ensure this gets the team object

        // Kiểm tra nếu đội 1 hoặc đội 2 không tồn tại
//        if (team1 == null || team2 == null) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Đội không hợp lệ!");
//            return "redirect:/admin/{idSport}/match";  // Or an appropriate redirect URL
//        }
        if (existingMatch == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Trận đấu không tồn tại!");
            return "redirect:/admin/sport";  // Redirect về trang chính nếu trận đấu không tồn tại
        }

        // Lấy Arena từ arenaId
        Arena arena = arenaService.findById(arenaId);
        if (arena == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Địa điểm không hợp lệ!");
            return "redirect:/admin/sport";
        }

        // Lấy thông tin môn thể thao từ team1 của trận đấu
        Sport sport = team1.getSport();
        if (sport == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Môn thể thao không hợp lệ!");
            return "redirect:/admin/sport";
        }
        // Lấy viết tắt môn thi đấu và vòng đấu
        String sportAbbreviation = getAbbreviation(sport.getSportName()); // Viết tắt của môn
        String roundAbbreviation = "PO"; // Vòng Playoff viết tắt

        // Tạo mã trận đấu
        String matchCode = sportAbbreviation + "-" + roundAbbreviation + "-" + match.getTeam1().getId() + "-" + match.getTeam2().getId();

        Round round = roundService.findById(roundId);
        // Cập nhật thông tin trận đấu
        existingMatch.setTeam1(match.getTeam1());
        existingMatch.setTeam2(match.getTeam2());
        existingMatch.setMatchName(matchCode);
        existingMatch.setTime(match.getTime());
        existingMatch.setTimeStart(match.getTimeStart());
        existingMatch.setArena(arena);
        existingMatch.setRound(round);
        existingMatch.setPoint1(match.getPoint1());
        existingMatch.setPoint2(match.getPoint2());
        existingMatch.setBonuspoint1(match.getBonuspoint1());
        existingMatch.setBonuspoint2(match.getBonuspoint2());
        // Kiểm tra nếu point1 và point2 đều là -1
        if (existingMatch.getPoint1() != null && existingMatch.getPoint1() == -1 && existingMatch.getPoint2() != null && existingMatch.getPoint2() == -1) {
            existingMatch.setWinner(-1); // Nếu điểm của cả hai đội là -1, thiết lập winner = -1
        } else if (existingMatch.getPoint1() != null && existingMatch.getPoint1().equals(existingMatch.getPoint2())) {
            // Kiểm tra nếu điểm của cả hai đội bằng nhau, thì kiểm tra điểm phụ
            if (existingMatch.getBonuspoint1() != null && existingMatch.getBonuspoint2() != null) {
                // So sánh điểm phụ nếu có
                if (existingMatch.getBonuspoint1() > existingMatch.getBonuspoint2()) {
                    existingMatch.setWinner(1); // Đội 1 thắng
                } else if (existingMatch.getBonuspoint1() < existingMatch.getBonuspoint2()) {
                    existingMatch.setWinner(2); // Đội 2 thắng
                } else {
                    // Trường hợp điểm phụ bằng nhau, có thể cần xử lý thêm (ví dụ: hòa)
                    existingMatch.setWinner(0); // Hòa, hoặc xử lý theo cách của bạn
                }
            }
        } else {
            // Nếu điểm của hai đội khác nhau, đội có điểm cao hơn sẽ thắng
            existingMatch.setWinner(existingMatch.getPoint1() > existingMatch.getPoint2() ? 1 : 2);
        }


        // Xác định số đội (noFinal) và trạng thái
        int noFinalCurrent = 0;
        int noFinalNext = 0;
        switch (roundId) {
            case 2: // Vòng 1/16
                noFinalCurrent = 16;
                noFinalNext = 8;
                break;
            case 3: // Tứ kết
                noFinalCurrent = 8;
                noFinalNext = 4;
                break;
            case 4: // Bán kết
                noFinalCurrent = 4;
                noFinalNext = 2;
                break;
            case 5: // Chung kết
                noFinalCurrent = 2;
                noFinalNext = 1; // Thắng sẽ là vô địch
                break;
            default:
                redirectAttributes.addFlashAttribute("errorMessage", "Vòng đấu không hợp lệ!");
                return "redirect:/admin/sport";
        }

        // Cập nhật trạng thái của các đội
        if (existingMatch.getWinner() == 1) { // team1 thắng
            team1.setNoFinal(noFinalNext); // Tứ kết
            team1.setStatus(2);  // Giữ nguyên
            team2.setNoFinal(noFinalCurrent); // Top 8
            team2.setStatus(0);  // Bị loại

            // Kiểm tra nếu noRank == 1 thì chuyển status của team1 về 0
            if (team1.getNoFinal() == 1) {
                team1.setStatus(0);  // Chuyển trạng thái thành bị loại
            }

        } else if (existingMatch.getWinner() == 2) { // team2 thắng
            team2.setNoFinal(noFinalNext); // Tứ kết
            team2.setStatus(2);  // Giữ nguyên
            team1.setNoFinal(noFinalCurrent); // Top 8
            team1.setStatus(0);  // Bị loại

            // Kiểm tra nếu noRank == 1 thì chuyển status của team2 về 0
            if (team2.getNoFinal() == 1) {
                team2.setStatus(0);  // Chuyển trạng thái thành bị loại
            }
        }

        // Lưu trạng thái đội
        teamService.saveTeam(team1);
        teamService.saveTeam(team2);
        // Lưu trận đấu đã cập nhật
        matchService.saveMatch(existingMatch);

        // Thêm thông báo thành công
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trận đấu thành công!");

        // Chuyển hướng về trang chi tiết trận đấu hoặc danh sách trận đấu
        return "redirect:/admin/" + sport.getId() + "/playoff-tt";  // Redirect tới trang playoff của môn thể thao
    }

}
