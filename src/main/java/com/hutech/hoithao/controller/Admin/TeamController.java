package com.hutech.hoithao.controller.Admin;
import com.hutech.hoithao.models.Member;
import com.hutech.hoithao.models.Sport;
import com.hutech.hoithao.models.Team;
import com.hutech.hoithao.service.MemberService;
import com.hutech.hoithao.service.SportService;
import com.hutech.hoithao.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin/team")
public class TeamController {
    @Autowired
    private TeamService teamService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private SportService sportService;

    @GetMapping("/image/{id}")
    @ResponseBody
    public ResponseEntity<String> getPaymentProofPath(@PathVariable Integer id) {
        Team team = teamService.findTeamById(id);
        if (team != null && team.getPaymentProofPath() != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG) // Chỉ định định dạng file trả về
                    .body(team.getPaymentProofPath());
        }
        return ResponseEntity.notFound().build();
    }
    @GetMapping
    public String listTeams(Model model) {
        List<Team> teams = teamService.findAllTeams();
        model.addAttribute("teams", teams);
        return "admin/team/index"; // Tên của view cho danh sách team
    }

    @GetMapping("/{id}")
    public String viewTeamDetail(@PathVariable Integer id, Model model) {
        Team team = teamService.findTeamById(id);
        long  approvedTeamCount = teamService.countApprovedTeamsBySport(team.getSport().getId());

        List<Member> memberss = memberService.getMemberByTeamId(id);
        model.addAttribute("members", memberss);
        Sport sport = team.getSport();
        if (approvedTeamCount >= sport.getNumberTeamMax()) {
            sport.setStatus(0); // Môn thể thao đã đủ đội
            sportService.saveSport(sport);
        }
        if (team != null) {
            model.addAttribute("approvedTeamsCount", approvedTeamCount);
            model.addAttribute("team", team);
            return "admin/team/edit"; // Đường dẫn tới trang chi tiết
        }
        return "admin/team/index"; // Tên của view cho chi tiết team
    }
    @PostMapping("/approve/{id}")
    public String approveTeam(@PathVariable Integer id) {
        Team team = teamService.findTeamById(id);

        if (team != null) {
            Sport sport = team.getSport();

            if (sport != null) {
                Integer idSport = sport.getId(); // Lấy idSport từ đối tượng Sport

                // Kiểm tra số lượng đội đã duyệt cho môn thể thao này
                long approvedTeamsCount = teamService.countApprovedTeamsBySport(idSport) + teamService.countFinishTeamBySport(idSport);

                // Nếu số lượng đội đã đạt giới hạn cho môn thể thao này
                if (approvedTeamsCount >= sport.getNumberTeamMax()) {
                    // Trả về thông báo lỗi qua URL
                    return "redirect:/admin/team?error=maxApproved&sport=" + sport.getSportName();
                }

                // Duyệt đội
                team.setStatus(2);  // Trạng thái 'Đã duyệt'
                team.setNumberGame(0);
                team.setPoint(0);
                team.setHs(0);
                teamService.saveTeam(team);

                // Cập nhật trạng thái môn thể thao nếu số lượng đội đã duyệt đạt giới hạn
                approvedTeamsCount = teamService.countApprovedTeamsBySport(idSport) + teamService.countFinishTeamBySport(idSport);
                if (approvedTeamsCount >= sport.getNumberTeamMax()) {
                    sport.setStatus(0); // Môn thể thao đã đủ đội
                    sportService.saveSport(sport);
                }

                // Redirect về danh sách đội của môn thể thao này
                return "redirect:/admin/team-list/" + idSport;
            }
        }
        // Nếu không tìm thấy team hoặc sport
        return "redirect:/admin/team?error=notFound";
    }

    @PostMapping("/reject/{id}")
    public String rejectTeam(@PathVariable Integer id) {
        // Tìm đội theo ID
        Team team = teamService.findTeamById(id);

        if (team != null) {
            // Cập nhật trạng thái đội thành không duyệt (ví dụ: -1)
            team.setStatus(-1);

            // Lưu thay đổi vào cơ sở dữ liệu
            teamService.saveTeam(team);

            // Lấy idSport từ đối tượng Team
            Integer idSport = team.getSport().getId(); // Giả sử Team có phương thức getSport() trả về đối tượng Sport

            // Chuyển hướng về trang danh sách đội với idSport tương ứng
            return "redirect:/admin/team-list/" + idSport;
        }

        // Nếu không tìm thấy đội, có thể chuyển hướng về một trang lỗi hoặc trang danh sách chung
        // Ví dụ: chuyển hướng về trang danh sách đội chung mà không cần idSport
        return "redirect:/admin/team-list";
    }

}
