package com.hutech.hoithao.controller.Admin;

import com.hutech.hoithao.exceptions.ResourceNotFoundException;
import com.hutech.hoithao.models.*;
import com.hutech.hoithao.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;

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
@RequestMapping("/admin")
public class SportController {
    @Autowired
    private SportService sportService;
    @Autowired
    private EventService eventService;
    @Autowired
    private FormatService formatService;
    @Autowired
    private TeamService teamService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private MatchService matchService;
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
    @GetMapping("/sport-add")
    public String add(Model model) {
        Status_Event status = new Status_Event();
        status.setId(1);
        List<Event> events = eventService.getEventsByStatus(status);
        List<Format> formats = formatService.findAll();
        Sport sport = new Sport();
        model.addAttribute("sport", sport);
        model.addAttribute("events", events);
        model.addAttribute("formats",formats);
        return "admin/sport/add";
    }

    @PostMapping("/sport-add")
    public String save(@ModelAttribute("sport") Sport sport, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            Status_Event status = new Status_Event();
            status.setId(3);
            List<Event> events = eventService.getEventsByStatus(status);
            model.addAttribute("events", events);
            return "admin/sport/add";
        }
        if (sportService.create(sport)) {
            return "redirect:/admin/sport";
        } else {
            model.addAttribute("errorMessage", "Could not save sport");
            Status_Event status = new Status_Event();
            status.setId(0);
            List<Event> events = eventService.getEventsByStatus(status);
            model.addAttribute("events", events);
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
        List<Event> events = eventService.getAllEvents();
        List<Format> formats = formatService.findAll();

        // Đưa các giá trị vào model để hiển thị trên giao diện
        model.addAttribute("sport", sport);
        model.addAttribute("events", events);
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
            model.addAttribute("events", eventService.getAllEvents());
            model.addAttribute("formats", formatService.findAll());
            return "admin/sport/edit";
        }

        // Tìm sport theo ID để kiểm tra hợp lệ
        Sport existingSport = sportService.findById(id);
        if (existingSport == null) {
            model.addAttribute("errorMessage", "Môn thể thao không tồn tại.");
            return "redirect:/admin/sport"; // Nếu không tìm thấy, chuyển hướng về danh sách sport
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
        return "redirect:/admin/sport";
    }
    @GetMapping("/team-list/{sportId}")
    public String getTeamListBySport(@PathVariable Integer sportId, Model model) {
        List<Team> approvedTeams = teamService.findTeamsByStatus(sportId, List.of(2)); // Được duyệt / Không được duyệt
        List<Team> pendingTeams = teamService.findTeamsByStatus(sportId, List.of(1));      // Chưa duyệt
        List<Team> rejectedTeams = teamService.findTeamsByStatus(sportId, List.of(-1));
        Sport sport = sportService.findById(sportId);
        model.addAttribute("sport", sport);
        model.addAttribute("approvedTeams", approvedTeams);
        model.addAttribute("pendingTeams", pendingTeams);
        model.addAttribute("rejectedTeams", rejectedTeams);
        return "admin/team/team-list"; // Tên file Thymeleaf hiển thị danh sách team
    }



    @PostMapping("/delete-sport/{id}")
    public String delete(@PathVariable("id") Integer id) {
        Sport sport =this.sportService.findById(id);
        sport.setStatus(-1);
        sportService.update(sport);
        return "redirect:/admin/event";
    }
    @GetMapping("/{idSport}/groups")
    public String viewGroups(@PathVariable Integer idSport, Model model) {
        Sport sport = sportService.getSportById(idSport);

        if (sport == null) {
            model.addAttribute("errorMessage", "Môn thể thao không tồn tại.");
            return "error/404";
        }

        List<Group> groups = groupService.getGroupsBySport(idSport);
        Map<Integer, List<Team>> sortedTeamsByGroup = new HashMap<>();

        for (Group group : groups) {
            List<Team> sortedTeams = teamService.getSortedTeamsByGroup(group.getId());
            sortedTeamsByGroup.put(group.getId(), sortedTeams);
        }
        model.addAttribute("teamsNotInGroup", teamService.getTeamsNotInAnyGroup(idSport));
        model.addAttribute("matches", matchService.findMatchesBySport(idSport)); // Lấy danh sách trận đấu
        model.addAttribute("sport", sport);
        model.addAttribute("groups", groups);
        model.addAttribute("sortedTeamsByGroup", sortedTeamsByGroup); // Gửi danh sách đã sắp xếp

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

    @PostMapping("/{idSport}/save-results/{groupId}")
    public String saveResults(@PathVariable Integer idSport, @PathVariable Integer groupId, RedirectAttributes redirectAttributes) {
        Sport sport = sportService.findById(idSport);
        Group group = groupService.getGroupById(groupId);
        List<Team> teams = teamService.getSortedTeamsByGroup(groupId);

        if (sport.getFormat().getId() == 1) {
            // Format 1: Cập nhật thứ tự vào noFinal
            for (int i = 0; i < teams.size(); i++) {
                teams.get(i).setNoFinal(i + 1);
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
                .filter(team -> team.getStatus() == 2)
                .sorted(Comparator.comparingInt(Team::getNoFinal))
                .toList();
        model.addAttribute("sport", sport);
        model.addAttribute("teams", rankedTeams); // Toàn bộ đội xếp hạng
        return "admin/rank/ranking"; // Trả về view "ranking.html"
    }

}