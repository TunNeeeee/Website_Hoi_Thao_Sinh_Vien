package com.hutech.hoithao.controller.Admin;
import com.hutech.hoithao.models.Member;
import com.hutech.hoithao.models.Team;
import com.hutech.hoithao.service.MemberService;
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
        List<Member> memberss = memberService.getMemberByTeamId(id);
        model.addAttribute("members", memberss);
        if (team != null) {
            model.addAttribute("team", team);
            return "admin/team/edit"; // Đường dẫn tới trang chi tiết
        }
        return "admin/team/index"; // Tên của view cho chi tiết team
    }
    @PostMapping("/approve/{id}")
    public String approveTeam(@PathVariable Integer id) {
        // Xử lý logic duyệt đội
        Team team = teamService.findTeamById(id);
        if (team != null) {
            team.setStatus(2);
        }
        teamService.saveTeam(team);
        return "redirect:/admin/team"; // Chuyển hướng về trang danh sách đội
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
