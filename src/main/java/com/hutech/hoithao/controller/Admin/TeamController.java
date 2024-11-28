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
    public ResponseEntity<byte[]> getPaymentProof(@PathVariable Integer id) {
        Team team = teamService.findTeamById(id);
        if (team != null && team.getPaymentProof() != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG) // Hoặc định dạng hình ảnh tương ứng
                    .body(team.getPaymentProof());
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
        long approvedTeamCount = teamService.countApprovedTeamsBySport(team.getSport().getId());

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
                // Kiểm tra số lượng đội đã duyệt cho môn thể thao này
                long approvedTeamsCount = teamService.countApprovedTeamsBySport(sport.getId());

                // Nếu số lượng đội đã đạt giới hạn cho môn thể thao này
                if (approvedTeamsCount >= sport.getNumberTeamMax()) {
                    // Trả về thông báo lỗi qua URL
                    return "redirect:/admin/team?error=maxApproved&sport=" + sport.getSportName();
                }

                // Duyệt đội
                team.setStatus(2);  // Trạng thái 'Đã duyệt'
                teamService.saveTeam(team);

                // Cập nhật trạng thái môn thể thao nếu số lượng đội đã duyệt đạt giới hạn
                approvedTeamsCount = teamService.countApprovedTeamsBySport(sport.getId());
                if (approvedTeamsCount >= sport.getNumberTeamMax()) {
                    sport.setStatus(0); // Môn thể thao đã đủ đội
                    sportService.saveSport(sport);
                }
            }
        }
        return "redirect:/admin/team";
    }



    @PostMapping("/reject/{id}")
    public String rejectTeam(@PathVariable Integer id) {
        // Xử lý logic không duyệt đội
        Team team = teamService.findTeamById(id);
        if (team != null) {
            team.setStatus(-1);
        }
        teamService.saveTeam(team);
        return "redirect:/admin/team"; // Chuyển hướng về trang danh sách đội
    }
}
