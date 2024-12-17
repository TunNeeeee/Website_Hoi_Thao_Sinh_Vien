package com.hutech.hoithao.controller.Admin;

import com.hutech.hoithao.models.*;
import com.hutech.hoithao.repository.MatchRepository;
import com.hutech.hoithao.repository.SportRepository;
import com.hutech.hoithao.repository.TeamRepository;
import com.hutech.hoithao.service.*;
import com.hutech.hoithao.utils.mappers.MatchMapper;
import com.hutech.hoithao.utils.mappers.TeamMapper;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@RequestMapping("/admin")
public class SportController {
    private SportService sportService;
    private EventService eventService;
    private FormatService formatService;
    private TeamService teamService;
    private GroupService groupService;
    private MatchService matchService;
    private PlayoffService playoffService;
    private RoundService roundService;
    private MatchRepository matchRepository;
    private TeamRepository teamRepository;
    private SportRepository sportRepository;
    private AcademicYearService academicYearService;
    TeamMapper teamMapper;
    MatchMapper matchMapper;

    @GetMapping("/sport")
    public String index(Model model, @Param("keyword") String keyword, @RequestParam(name="pageNo",defaultValue = "1") Integer pageNo) {
        Page<Sport> page;
        if (keyword != null) {
            page = sportService.searchSportPage(keyword, pageNo);
            model.addAttribute("keyword", keyword);
        } else {
            page = sportService.getAll(pageNo);
        }
        model.addAttribute("totalPage", page.getTotalPages());
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("list", page.getContent());
        return "admin/sport/index";
    }
    @GetMapping("/add-sport")
    public String add_sport(Model model){
        Sport sport = new Sport();
        Status_Event status = new Status_Event();
        status.setId(-1);
        List<Event> events = eventService.getEventsByStatus(status);
        List<Format> formats = formatService.findAll();
        model.addAttribute("events",events);
        model.addAttribute("sport",sport);
        model.addAttribute("formats",formats);
        return "admin/sport/add";
    }
    @GetMapping("/sport-add/{academicYearId}")
    public String add(@PathVariable("academicYearId") Integer academicYearId, Model model) {
        Status_Event status = new Status_Event();
        status.setId(1);
        List<Event> events = eventService.getEventsByStatus(status);
        List<Format> formats = formatService.findAll();
        Sport sport = new Sport();
        AcademicYear academicYear = academicYearService.findById(academicYearId);
        // Gửi đối tượng academicYear vào model
        model.addAttribute("academicYear", academicYear);
        model.addAttribute("sport", sport);
        model.addAttribute("events", events);
        model.addAttribute("formats",formats);
        return "admin/sport/add";
    }


    @PostMapping("/sport-add/{academicYearId}")
    public String save(@ModelAttribute("sport") Sport sport,@PathVariable Integer academicYearId, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/sport/add";
        }
        // Lấy AcademicYear từ ID trong form
        AcademicYear academicYear = academicYearService.findById(academicYearId);
        sport.setAcademicYear(academicYear);

        // Lưu môn thể thao
        if (sportService.create(sport)) {
            return "redirect:/admin/event";
        } else {
            model.addAttribute("errorMessage", "Không thể lưu môn thể thao.");
            return "admin/sport/add";
        }
    }



    @GetMapping("/edit-sport/{id}")
    public String editSport(@PathVariable("id") Integer id, Model model) {
        // Tìm sport theo ID
        Sport sport = sportService.findById(id);
        if (sport == null) {
            model.addAttribute("errorMessage", "Môn thể thao không tồn tại.");
            return "redirect:/admin/sport"; // Nếu không tìm thấy, chuyển hướng về danh sách sport
        }

        // Lấy danh sách events và formats để hiển thị
        List<Format> formats = formatService.findAll();

        // Đưa các giá trị vào model để hiển thị trên giao diện
        model.addAttribute("sport", sport);
        model.addAttribute("formats", formats);

        return "admin/sport/edit"; // Hiển thị trang chỉnh sửa
    }

    @PostMapping("/edit-sport/{id}")
    public String updateSport(
            @PathVariable("id") Integer id,
            @Valid @ModelAttribute("sport") Sport sport,
            BindingResult result,
            Model model) {
        // Kiểm tra lỗi form validation
        if (result.hasErrors()) {
            // Nạp lại dữ liệu danh sách events và formats để hiển thị
            sport.setId(id); // Đảm bảo ID không bị mất
            model.addAttribute("formats", formatService.findAll());
            return "admin/sport/edit";
        }

        // Tìm sport theo ID để kiểm tra hợp lệ
        Sport existingSport = sportService.findById(id);
        if (existingSport == null) {
            model.addAttribute("errorMessage", "Môn thể thao không tồn tại.");
            return "redirect:/admin/sport"; // Nếu không tìm thấy, chuyển hướng về danh sách sport
        }
        if (sport.getAcademicYear() == null) {
            sport.setAcademicYear(existingSport.getAcademicYear());
        }
        // Giữ nguyên ngày nếu người dùng không nhập
        if (sport.getStartDate() == null) {
            sport.setStartDate(existingSport.getStartDate());
        }
        if (sport.getEndDate() == null) {
            sport.setEndDate(existingSport.getEndDate());
        }

        // Giữ nguyên trạng thái cũ nếu không chọn giá trị mới
        if (sport.getStatus() == null) {
            sport.setStatus(existingSport.getStatus());
        }
        // Cập nhật thông tin sport
        sport.setId(existingSport.getId()); // Đảm bảo ID không bị thay đổi
        if (sport.getStatus() == null) {
            sport.setStatus(existingSport.getStatus()); // Giữ nguyên trạng thái cũ nếu không có giá trị mới
        }

        boolean isUpdated = sportService.update(sport);
        if (!isUpdated) {
            model.addAttribute("errorMessage", "Cập nhật môn thể thao thất bại. Vui lòng thử lại.");
            model.addAttribute("events", eventService.getAllEvents());
            model.addAttribute("formats", formatService.findAll());
            return "admin/sport/edit";
        }

        // Chuyển hướng về trang danh sách sự kiện sau khi cập nhật thành công
        return "redirect:/admin/event";
    }
    @GetMapping("/team-list/{sportId}")
    public String getTeamListBySport(@PathVariable Integer sportId, Model model) {
        List<Team> approvedTeams = teamService.findTeamsByStatus(sportId, List.of(2)); // Được duyệt / Không được duyệt
        List<Team> pendingTeams = teamService.findTeamsByStatus(sportId, List.of(1));      // Chưa duyệt
        List<Team> rejectedTeams = teamService.findTeamsByStatus(sportId, List.of(-1));
        List<Team> eliminatedTeams = teamService.findTeamsByStatus(sportId, List.of(0));
        List<Team> allTeams = new ArrayList<>();
        allTeams.addAll(approvedTeams);
        allTeams.addAll(eliminatedTeams);
        Sport sport = sportService.findById(sportId);
        long approvedTeamsCount = teamService.countApprovedTeamsBySport(sportId) + teamService.countFinishTeamBySport(sportId);
        if (approvedTeamsCount >= sport.getNumberTeamMax()) {
            sport.setStatus(0); // Môn thể thao đã đủ đội
            sportService.saveSport(sport);
        }
        model.addAttribute("sport", sport);
        model.addAttribute("eliminatedTeams", eliminatedTeams);
        model.addAttribute("approvedTeams", approvedTeams);
        model.addAttribute("pendingTeams", pendingTeams);
        model.addAttribute("rejectedTeams", rejectedTeams);
        model.addAttribute("allTeams", allTeams);
        return "admin/team/team-list"; // Tên file Thymeleaf hiển thị danh sách team
    }



    @PostMapping("/delete-sport/{id}")
    public String delete(@PathVariable("id") Integer id) {
        Sport sport =this.sportService.findById(id);
        sport.setStatus(-1);
        sportService.update(sport);
        return "redirect:/admin/event";
    }
    //--------------------------------------------------------------------------------------------//


    @GetMapping("/{idSport}/groups")
    public String viewGroups(@PathVariable Integer idSport, Model model) {
        Sport sport = sportService.getSportById(idSport);

        if (sport == null) {
            model.addAttribute("errorMessage", "Môn thể thao không tồn tại.");
            return "error/404";
        }

        List<Group> groups = groupService.getGroupsBySport(idSport);
        Map<Integer, List<Team>> sortedTeamsByGroup = new HashMap<>();
        Map<Integer, List<String>> lastThreeResultsByTeam = new HashMap<>(); // Thêm map để lưu lastThreeResults

        for (Group group : groups) {
            List<Team> sortedTeams = teamService.getSortedTeamsByGroup(group.getId());
            sortedTeamsByGroup.put(group.getId(), sortedTeams);

            // Tính lastThreeResults cho từng team trong group
            for (Team team : sortedTeams) {
                List<String> lastThreeResults = teamService.getLastThreeResults(team.getId()); // Giả sử 1 là round "vòng bảng"
                lastThreeResultsByTeam.put(team.getId(), lastThreeResults);
            }
        }
        Round round = new Round();
        round.setId(1);
        model.addAttribute("teamsNotInGroup", teamService.getTeamsNotInAnyGroup(idSport));
        model.addAttribute("matches", matchService.findMatchesBySportAndRound(idSport,round)); // Lấy danh sách trận đấu
        model.addAttribute("sport", sport);
        model.addAttribute("groups", groups);
        model.addAttribute("sortedTeamsByGroup", sortedTeamsByGroup); // Gửi danh sách đã sắp xếp
        model.addAttribute("lastThreeResultsByTeam", lastThreeResultsByTeam); // Gửi danh sách lastThreeResults

        return "admin/groups/list";
    }



    @PostMapping("/{idSport}/create-group")
    public String createGroup(@PathVariable Integer idSport, RedirectAttributes redirectAttributes) {
        Sport sport = sportService.getSportById(idSport);

        if (sport == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Môn thể thao không tồn tại.");
            return "redirect:/admin/"+idSport+"/groups";
        }

        // Tạo bảng đấu mới
        String nextGroupName = groupService.generateNextGroupName(idSport); // Tự động đặt tên từ A -> Z
        Group newGroup = new Group();
        newGroup.setGroupName(nextGroupName);
        newGroup.setSport(sport);

        groupService.saveGroup(newGroup);

        redirectAttributes.addFlashAttribute("successMessage", "Tạo bảng đấu thành công!");
        return "redirect:/admin/" + idSport + "/groups";
    }
    @PostMapping("/{idSport}/add-teams-to-group")
    public String addTeamsToGroup(@PathVariable Integer idSport,
                                  @RequestParam Integer groupId,
                                  @RequestParam List<Integer> teamIds, // Danh sách ID các đội
                                  RedirectAttributes redirectAttributes) {
        Group group = groupService.getGroupById(groupId);

        if (group == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bảng đấu không tồn tại.");
            return "redirect:/admin/" + idSport + "/groups";
        }

        List<Team> selectedTeams = teamService.getTeamsByIds(teamIds);

        for (Team team : selectedTeams) {
            group.getListTeam().add(team); // Thêm đội vào bảng đấu
            team.setGroup(group);         // Gắn bảng đấu vào đội
        }

        groupService.saveGroup(group);
        teamService.saveAll(selectedTeams);

        redirectAttributes.addFlashAttribute("successMessage", "Thêm đội vào bảng đấu thành công!");
        return "redirect:/admin/" + idSport + "/groups";
    }
    @PostMapping("/{idSport}/add-teams-random")
    public String addTeamsRandom(@PathVariable Integer idSport,
                                 @RequestParam Integer numberOfTeams, // Số lượng đội muốn thêm ngẫu nhiên
                                 RedirectAttributes redirectAttributes) {

        // Lấy danh sách các đội chưa tham gia bảng đấu
        List<Team> teamsNotInGroup = teamService.getTeamsNotInAnyGroup(idSport);

        // Kiểm tra nếu không đủ đội
        if (teamsNotInGroup.size() < numberOfTeams) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không đủ đội để phân bổ ngẫu nhiên.");
            return "redirect:/admin/" + idSport + "/groups";
        }

        // Trộn danh sách đội ngẫu nhiên
        Collections.shuffle(teamsNotInGroup);

        // Lấy danh sách các bảng đấu hiện có
        List<Group> availableGroups = groupService.getGroupsBySport(idSport);

        if (availableGroups.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không có bảng đấu nào để thêm đội.");
            return "redirect:/admin/" + idSport + "/groups";
        }

        // Phân bổ ngẫu nhiên các đội vào bảng
        int groupIndex = 0;
        for (int i = 0; i < numberOfTeams; i++) {
            Team team = teamsNotInGroup.get(i);
            Group group = availableGroups.get(groupIndex);

            // Gán đội vào bảng
            group.getListTeam().add(team);
            team.setGroup(group);

            // Lưu trữ bảng và đội
            groupService.saveGroup(group);
            teamService.saveTeam(team);

            // Chuyển sang bảng kế tiếp (dạng vòng tròn)
            groupIndex = (groupIndex + 1) % availableGroups.size();
        }

        redirectAttributes.addFlashAttribute("successMessage", "Phân bổ đội vào bảng thành công!");
        return "redirect:/admin/" + idSport + "/groups";
    }

    @PostMapping("/{idSport}/save-results/{groupId}")
    public String saveResults(@PathVariable Integer idSport, @PathVariable Integer groupId, RedirectAttributes redirectAttributes) {
        Sport sport = sportService.findById(idSport);
        Group group = groupService.getGroupById(groupId);
        List<Team> teams = teamService.getSortedTeamsByGroup(groupId);

        if (sport.getFormat().getId() == 1) {
            // Format 1: Cập nhật thứ tự vào noFinal
            for (int i = 0; i < teams.size(); i++) {
                teams.get(i).setNoFinal(i + 1);
                teams.get(i).setStatus(0);
                teamService.saveTeam(teams.get(i));
            }
        } else if (sport.getFormat().getId() == 3) {
            // Format 3: Chuyển 2 đội cao nhất vào vòng tiếp theo
            for (int i = 0; i < teams.size(); i++) {
                if (i < 2) {
                    // 2 đội đứng đầu: Đi tiếp
                    teams.get(i).setStatus(2);
                    teams.get(i).setNoRank(i+1);
                } else {
                    // Các đội khác: Bị loại
                    teams.get(i).setStatus(0);
                    teams.get(i).setNoRank(i+1);
                }
                teamService.saveTeam(teams.get(i));
            }
        }

        redirectAttributes.addFlashAttribute("success", "Kết quả đã được lưu thành công!");
        return "redirect:/admin/" + idSport + "/groups";
    }
//--------------------------------------------------------------------------------------------------------------------

    private List<List<Match>> generateBracketRounds(List<Match> firstRound) {
        List<List<Match>> playoffRounds = new ArrayList<>();
        playoffRounds.add(firstRound);

        // Xử lý vòng đấu tiếp theo
        List<Match> currentRound = firstRound;
        while (currentRound.size() > 1) {
            List<Match> nextRound = new ArrayList<>();

            for (int i = 0; i < currentRound.size(); i += 2) {
                Match match1 = currentRound.get(i);
                Match match2 = currentRound.get(i + 1);

                // Xác định đội thắng từ các trận trước
                Team winner1 = getWinner(match1);
                Team winner2 = getWinner(match2);

                if (winner1 != null && winner2 != null) {
                    Round nextRoundType = new Round();
                    nextRoundType.setId(getNextRoundId(match1.getRound().getId()));

                    Match match = matchService.findMatch(winner1, winner2, nextRoundType)
                            .orElseGet(() -> {
                                Match newMatch = new Match();
                                newMatch.setTeam1(winner1);
                                newMatch.setTeam2(winner2);
                                newMatch.setRound(nextRoundType);
                                return newMatch;
                            });

                    nextRound.add(match);
                }
            }

            // Lưu các trận đấu của vòng mới
            nextRound = matchService.saveAll(nextRound);
            playoffRounds.add(nextRound);
            currentRound = nextRound;
        }

        return playoffRounds;
    }
    private int getNextRoundId(int currentRoundId) {
        switch (currentRoundId) {
            case 2: return 3; // 1/16 -> Tứ kết
            case 3: return 4; // Tứ kết -> Bán kết
            case 4: return 5; // Bán kết -> Chung kết
            default: throw new IllegalArgumentException("Vòng đấu không hợp lệ!");
        }
    }


    private Team getWinner(Match match) {
        if (match.getPoint1() != null && match.getPoint2() != null) {
            if (match.getPoint1() > match.getPoint2()) {
                return match.getTeam1();
            } else if (match.getPoint1() < match.getPoint2()) {
                return match.getTeam2();
            } else {
                System.out.println("Match ID: " + match.getId() + " ended in a draw.");
            }
        } else {
            System.out.println("Match ID: " + match.getId() + " has incomplete points.");
        }
        return null; // Hòa hoặc chưa có điểm
    }



    public List<Match> createFirstRoundMatches(List<Team> teamsRank1, List<Team> teamsRank2) {
        List<Match> matches = new ArrayList<>();
        int totalGroups = teamsRank1.size();
        int roundId = getRoundIdByTeams(totalGroups*2);
        for (int i = 0; i < totalGroups / 2; i++) {
            Round round = new Round();
            round.setId(roundId);
            // Nhánh 1
            Team team1 = teamsRank1.get(i);
            Team team2 = teamsRank2.get((i + 1) % totalGroups);
            Match match1 = matchService.findMatch(team1, team2, round) // Round 1
                    .orElseGet(() -> {
                        Match newMatch = new Match();
                        newMatch.setTeam1(team1);
                        newMatch.setTeam2(team2);
                        newMatch.setRound(round); // Vòng 1
                        return newMatch;
                    });
            matches.add(match1);

            // Nhánh 2
            Team team3 = teamsRank1.get((i + totalGroups / 2) % totalGroups);
            Team team4 = teamsRank2.get((i + totalGroups / 2 + 1) % totalGroups);
            Match match2 = matchService.findMatch(team3, team4, round)
                    .orElseGet(() -> {
                        Match newMatch = new Match();
                        newMatch.setTeam1(team3);
                        newMatch.setTeam2(team4);
                        newMatch.setRound(round);
                        return newMatch;
                    });
            matches.add(match2);
        }

        matches = matchService.saveAll(matches);
        return matches;
    }

    @GetMapping("/{idSport}/playoff")
    public String playoffPage(@PathVariable("idSport") Integer idSport, Model model) {
        // Kiểm tra sự tồn tại của môn thể thao
        Sport sport = sportService.findById(idSport);
        if (sport == null) {
            model.addAttribute("error", "Môn thể thao không tồn tại.");
            return "redirect:/admin/sports";
        }

        // Lấy danh sách các đội tham gia playoff
        List<Team> teamsRank1 = teamService.findBySportAndNoRankOrdered(idSport, 1); // Đội nhất bảng
        List<Team> teamsRank2 = teamService.findBySportAndNoRankOrdered(idSport, 2); // Đội nhì bảng

        // Kiểm tra số lượng đội
        if (teamsRank1.size() != teamsRank2.size()) {
            model.addAttribute("error", "Số lượng đội giữa Nhất bảng và Nhì bảng không khớp.");
            return "redirect:/admin/sports";
        }

        // Tạo các trận đấu vòng đầu tiên
        List<Match> firstRound = createFirstRoundMatches(teamsRank1, teamsRank2);
        System.out.println("=== First Round Matches ===");
        firstRound.forEach(match -> {
            System.out.println("Match ID: " + match.getId() + " | Team 1: " + match.getTeam1().getTeamName() + " | Team 2: " + match.getTeam2().getTeamName());
        });
        // Sinh các vòng đấu tiếp theo từ vòng đầu tiên
        List<List<Match>> playoffRounds = generateBracketRounds(firstRound);
        System.out.println("Generated Playoff Rounds: ");
        playoffRounds.forEach((round) -> {
            System.out.println("Round: ");
            round.forEach(match -> {
                System.out.println("Match ID: " + match.getId() + " | Team 1: " + match.getTeam1().getTeamName() + " | Team 2: " + match.getTeam2().getTeamName());
            });
        });
        // Chuyển đổi dữ liệu sang DTO để truyền vào view
        Map<String, List<MatchDTO>> bracketRounds = convertToBracketRoundsDTO(playoffRounds);

        System.out.println(bracketRounds);

        List<MatchDTO> allMatches = playoffService.getAllMatchesFromRoundId(2);

        Map<String, List<MatchDTO>> groupedMatches = allMatches.stream()
                .collect(Collectors.groupingBy(match -> {
                    // Kiểm tra xem round có null không và lấy tên vòng đấu từ roundService
                    if (match.getRound() != null && match.getRound().getId() != null) {
                        // Lấy tên vòng đấu từ roundService
                        return roundService.getRoundName(match.getRound().getId());
                    } else {
                        return "Unknown";  // Nếu không có thông tin vòng đấu
                    }
                }));

        // Truyền dữ liệu vào model
        model.addAttribute("allMatches", allMatches);
        model.addAttribute("groupedMatches", groupedMatches);
        model.addAttribute("sport", sport);
        model.addAttribute("bracketRounds", bracketRounds);

        return "admin/playoff/playoff-vb";
    }

    private Map<String, List<MatchDTO>> convertToBracketRoundsDTO(List<List<Match>> playoffRounds) {
        Map<String, List<MatchDTO>> bracketRoundsDTO = new LinkedHashMap<>();

        // Tổng số đội ban đầu (final để có thể sử dụng trong lambda expression)
        final int totalTeams = playoffRounds.get(0).size() * 2;

        for (int i = 0; i < playoffRounds.size(); i++) {
            List<Match> round = playoffRounds.get(i);

            // Tính số đội còn lại trong vòng này trước khi sử dụng trong lambda
            final int teamsRemaining = totalTeams / (1 << i);  // Dùng bit shifting để thay thế Math.pow

            List<MatchDTO> roundDTOs = round.stream()
                    .map(match -> {
                        // Chuyển đổi match thành MatchDTO
                        MatchDTO matchDTO = matchMapper.toDTO(match);
                        // Gán idRound cho matchDTO
                        int roundId = getRoundIdByTeams(teamsRemaining); // Gán idRound
                        Round round1 = new Round();
                        round1.setId(roundId);
                        matchDTO.setRound(round1);  // Gán idRound cho DTO

                        // Thiết lập đội thắng trong MatchDTO
                        if (match.getPoint1() != null && match.getPoint2() != null) {
                            Integer winnerTeam = 0; // Default value ( hòa )
                            if (match.getPoint1() > match.getPoint2()) {
                                winnerTeam = 1;  // Đội 1 thắng
                            } else if (match.getPoint1() < match.getPoint2()) {
                                winnerTeam = 2;  // Đội 2 thắng
                            }
                            matchDTO.setWinner(winnerTeam); // Gán winner trực tiếp từ winnerTeam
                        }
                        matchDTO.setPoint1(match.getPoint1());
                        matchDTO.setPoint2(match.getPoint2());
                        return matchDTO;
                    })
                    .toList();

            // Tính tên vòng đấu dựa trên số đội còn lại
            String roundName = getRoundNameByTeams(teamsRemaining);

            // Thêm vào map với tên vòng đấu
            bracketRoundsDTO.put(roundName, roundDTOs);
        }

        return bracketRoundsDTO;
    }



    private int getRoundIdByTeams(int teamsRemaining) {
        // Trả về id (kiểu int) của vòng đấu
        switch (teamsRemaining) {
            case 16: return 2;  // Vòng 1/16
            case 8:  return 3;  // Tứ kết
            case 4:  return 4;  // Bán kết
            case 2:  return 5;  // Chung kết
            default: return 0;  // Các vòng khác nếu có
        }
    }

    private String getRoundNameByTeams(int teamsRemaining) {
        // Trả về tên của vòng đấu (String)
        switch (teamsRemaining) {
            case 16: return "Vòng 1/16";  // Với 16 đội
            case 8:  return "Tứ kết";     // Với 8 đội
            case 4:  return "Bán kết";    // Với 4 đội
            case 2:  return "Chung kết";  // Với 2 đội
            default: return "Vòng " + teamsRemaining;  // Với số đội khác
        }
    }
    //---------------------------------------------------------------------------------------------------------//
    @GetMapping("/{idSport}/playoff-tt")
    public String viewPlayoff(@PathVariable Integer idSport, Model model) {
        List<Match> matches = matchRepository.findBySportId(idSport);
        Sport sport = sportService.findById(idSport);
        if (matches.isEmpty()) {
            // Nếu chưa có trận playoff nào, chuyển hướng đến trang tạo playoff đầu tiên
            return "redirect:/admin/" + idSport + "/playoff/create-first-round";
        }
        boolean allMatchesHaveScores = matches.stream()
                .allMatch(m -> m.getPoint1() != null && m.getPoint2() != null && m.getPoint1() != -1 && m.getPoint2() != -1);

        boolean hasWinner = matches.stream()
                .anyMatch(m -> m.getTeam1().getNoFinal() == 1 || m.getTeam2().getNoFinal() == 1);
        model.addAttribute("allMatchesHaveScores", allMatchesHaveScores);
        model.addAttribute("hasWinner", hasWinner);
        model.addAttribute("sport", sport);
        model.addAttribute("matches", matches);
        return "/admin/playoff/playoff-tt";
    }

    @PostMapping("/{idSport}/playoff-tt/generate")
    public String generateMatches(@PathVariable Integer idSport) {
        matchService.generateRandomPlayoffMatches(idSport);
        return "redirect:/admin/{idSport}/playoff-tt";
    }
    @GetMapping("/{idSport}/playoff/create-first-round")
    public String showCreateFirstRoundForm(@PathVariable("idSport") Integer idSport, Model model) {
        Sport sport = sportRepository.findById(idSport)
                .orElseThrow(() -> new IllegalArgumentException("Môn thể thao không tồn tại"));
        String roundName;
        switch (sport.getNumberTeamMax()) {
            case 16:
                roundName = "Vòng 1/16";
                break;
            case 8:
                roundName = "Tứ kêt";
                break;
            case 4:
                roundName = "Bán kết";
                break;
            case 2:
                roundName="Chung kết";
                break;
            default:
                throw new IllegalArgumentException("Số đội tối đa không hợp lệ.");
        }
        model.addAttribute("sport", sport);
        model.addAttribute("numberTeamMax", sport.getNumberTeamMax());
        model.addAttribute("roundName", roundName);
        model.addAttribute("teams", teamRepository.findBySportId(idSport)); // Danh sách đội để chọn
        return "/admin/playoff/create-first-round-tt";
    }
    @PostMapping("/{idSport}/playoff/create-first-round")
    public String createFirstRound(
            @PathVariable("idSport") Integer idSport,
            @RequestParam List<Integer> team1Ids,
            @RequestParam List<Integer> team2Ids,
            Model model) {
        // Lấy thông tin môn thể thao
        Sport sport = sportRepository.findById(idSport)
                .orElseThrow(() -> new IllegalArgumentException("Môn thể thao không tồn tại"));

        // Kiểm tra số lượng đội có hợp lệ không
        if (team1Ids.size() != team2Ids.size() || team1Ids.isEmpty()) {
            model.addAttribute("errorMessage", "Số lượng đội không hợp lệ.");
            return "redirect:/admin/" + idSport + "/playoff-tt";
        }

        // Xác định thông tin vòng đấu đầu tiên
        int numberTeamMax = sport.getNumberTeamMax();
        int roundNumber;
        String roundName;
        switch (numberTeamMax) {
            case 16:
                roundNumber = 2; // Mã định danh vòng
                roundName = "Vòng 1/16";
                break;
            case 8:
                roundNumber = 3;
                roundName = "Tứ kết";
                break;
            case 4:
                roundNumber = 4;
                roundName = "Bán kết";
                break;
            case 2:
                roundNumber = 5;
                roundName = "Chung kết";
                break;
            default:
                model.addAttribute("errorMessage", "Số đội tối đa không hợp lệ.");
                return "redirect:/admin/" + idSport + "/playoff-tt";
        }

        // Kiểm tra trùng lặp đội trong cùng một vòng đấu
        Set<Integer> selectedTeams = new HashSet<>();
        List<Integer> allTeamIds = new ArrayList<>();
        allTeamIds.addAll(team1Ids);
        allTeamIds.addAll(team2Ids);

        for (Integer teamId : allTeamIds) {
            if (!selectedTeams.add(teamId)) {
                model.addAttribute("errorMessage", "Một đội không được xuất hiện nhiều lần trong vòng đấu.");
                return "redirect:/admin/" + idSport + "/playoff-tt";
            }
        }

        // Tạo và lưu vòng đấu đầu tiên
        Round firstRound = new Round();
        firstRound.setId(roundNumber);

        // Tạo danh sách trận đấu
        List<Match> matches = new ArrayList<>();
        for (int i = 0; i < team1Ids.size(); i++) {
            int team1Id = team1Ids.get(i);
            int team2Id = team2Ids.get(i);

            // Lấy thông tin đội
            Team team1 = teamRepository.findById(team1Id)
                    .orElseThrow(() -> new IllegalArgumentException("Đội 1 không tồn tại với ID: " + team1Id));
            Team team2 = teamRepository.findById(team2Id)
                    .orElseThrow(() -> new IllegalArgumentException("Đội 2 không tồn tại với ID: " + team2Id));

            // Tạo trận đấu
            Match match = new Match();
            match.setTeam1(team1);
            match.setTeam2(team2);
            match.setRound(firstRound);
            matches.add(match);
        }

        // Lưu danh sách trận đấu
        matchRepository.saveAll(matches);

        // Chuyển hướng sau khi thành công
        return "redirect:/admin/" + idSport + "/playoff-tt";
    }



    @PostMapping("/{idSport}/playoff/new-round")
    public String createNewRound(@PathVariable("idSport") Integer idSport, Model model) {
        // Lấy danh sách các trận đấu hiện tại cho môn thể thao
        List<Match> currentMatches = matchRepository.findBySportId(idSport);
        // Tìm ID vòng đấu trước đó
        Integer previousRoundId = currentMatches.stream()
                .map(match -> match.getRound().getId())
                .max(Integer::compareTo)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy vòng đấu trước đó."));

        // Lọc các trận đấu thuộc vòng trước đó
        List<Match> previousRoundMatches = currentMatches.stream()
                .filter(match -> match.getRound().getId().equals(previousRoundId))
                .collect(Collectors.toList());
        // Tìm các đội thắng từ vòng trước
        List<Team> advancingTeams = findAdvancingTeams(previousRoundMatches);
        // Tạo vòng đấu mới nếu đủ đội
        if (advancingTeams.size() < 2) {
            throw new IllegalArgumentException("Không đủ đội để tạo vòng đấu mới.");
        }

        List<Match> newMatches = new ArrayList<>();
        for (int i = 0; i < advancingTeams.size() / 2; i++) {
            Match match = new Match();
            match.setTeam1(advancingTeams.get(i * 2));
            match.setTeam2(advancingTeams.get(i * 2 + 1));
            Round round = new Round();
            round.setId(previousRoundMatches.get(0).getRound().getId()+ 1);
            match.setRound(round); // Tăng số vòng
            newMatches.add(match);
        }

        // Lưu các trận đấu mới
        matchRepository.saveAll(newMatches);

        return "redirect:/admin/" + idSport + "/playoff-tt";
    }
    private List<Team> findAdvancingTeams(List<Match> currentMatches) {
        List<Team> advancingTeams = new ArrayList<>();
        for (Match match : currentMatches) {
            if (match.getWinner() != null && match.getWinner() != -1) {
                if (match.getWinner() == 1 && match.getTeam1() != null) {
                    advancingTeams.add(match.getTeam1());
                } else if (match.getWinner() == 2 && match.getTeam2() != null) {
                    advancingTeams.add(match.getTeam2());
                }
            }
        }
        return advancingTeams;
    }
    @PostMapping("/{idSport}/playoff-tt/update")
    public String updateMatch(@RequestParam Integer matchId,
                              @RequestParam(required = false) Integer team1Id,
                              @RequestParam(required = false) Integer team2Id,
                              @RequestParam(required = false) Integer scoreTeam1,
                              @RequestParam(required = false) Integer scoreTeam2) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match không tồn tại"));

        // Lấy thông tin Team từ database nếu ID được cung cấp
        if (team1Id != null) {
            Team team1 = teamRepository.findById(team1Id)
                    .orElseThrow(() -> new IllegalArgumentException("Team1 không tồn tại"));
            match.setTeam1(team1);
        }

        if (team2Id != null) {
            Team team2 = teamRepository.findById(team2Id)
                    .orElseThrow(() -> new IllegalArgumentException("Team2 không tồn tại"));
            match.setTeam2(team2);
        }
        if (scoreTeam1 != null) match.setPoint1(scoreTeam1);
        if (scoreTeam2 != null) match.setPoint2(scoreTeam2);

        matchRepository.save(match);
        return "redirect:/admin/{idSport}/playoff-tt";
    }



    //--------------------------------------------------------------------------------------------------------//
    //Ranking vong tron tinh diem
    @GetMapping("/{idSport}/ranking")
    public String viewRanking(@PathVariable Integer idSport, Model model) {
        Sport sport = sportService.getSportById(idSport);
        List<Team> teams = teamService.getTeamsBySportSortedByRanking(idSport); // Lấy danh sách đội xếp hạng
        if (teams.size() >= 3) {
            model.addAttribute("champion", teams.get(0)); // Vô địch
            model.addAttribute("runnerUp", teams.get(1)); // Á quân
            model.addAttribute("thirdPlace", teams.get(2)); // Hạng 3
        }
        // Lọc các đội có status = 2 và sắp xếp theo noFinal
        List<Team> rankedTeams = teamService.getTeamsBySportSortedByRanking(idSport)
                .stream()
                .filter(team -> team.getStatus() == 0)
                .sorted(Comparator.comparingInt(Team::getNoFinal))
                .toList();
        model.addAttribute("sport", sport);
        model.addAttribute("teams", rankedTeams); // Toàn bộ đội xếp hạng
        return "admin/rank/ranking"; // Trả về view "ranking.html"
    }
    //---------------------------------------------------------------------------------------------------------//
    //Ranking vong bang + playoff
    @GetMapping("/{idSport}/ranking-vbpo")
    public String viewRankingVBPO(@PathVariable Integer idSport, Model model) {
        Sport sport = sportService.getSportById(idSport);

        // Lọc các đội có status = 2 và sắp xếp theo noFinal
        List<Team> rankedTeams = teamService.getTeamsBySportSortedByRanking(idSport)
                .stream()
                .filter(team -> team.getStatus() == 0)
                .sorted(Comparator.comparingInt(Team::getNoFinal))
                .toList();

        // Xác định đội vô địch, á quân và hạng 3 dựa trên noFinal
        Team champion = rankedTeams.stream()
                .filter(team -> team.getNoFinal() == 1)
                .findFirst()
                .orElse(null);

        Team runnerUp = rankedTeams.stream()
                .filter(team -> team.getNoFinal() == 2)
                .findFirst()
                .orElse(null);

        Team thirdPlace = rankedTeams.stream()
                .filter(team -> team.getNoFinal() == 4)
                .findFirst()
                .orElse(null);

        Team thirdPlaceExtra = rankedTeams.stream()
                .filter(team -> team.getNoFinal() == 4 && !team.equals(thirdPlace))
                .findFirst()
                .orElse(null);

        model.addAttribute("thirdPlaceExtra", thirdPlaceExtra);
        // Gán dữ liệu vào model
        model.addAttribute("sport", sport);
        model.addAttribute("teams", rankedTeams); // Toàn bộ đội đã lọc
        model.addAttribute("champion", champion); // Vô địch
        model.addAttribute("runnerUp", runnerUp); // Á quân
        model.addAttribute("thirdPlace", thirdPlace); // Hạng 3

        return "admin/playoff/ranking"; // Trả về view "ranking.html"
    }

}