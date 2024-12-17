package com.hutech.hoithao.controller.Student;

import com.hutech.hoithao.models.Member;
import com.hutech.hoithao.models.Team;
import com.hutech.hoithao.service.MemberService;
import com.hutech.hoithao.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class AchievementController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private TeamService teamService;
    @GetMapping("/search")
    public String showSearchForm() {
        return "search";  // Chỉ trả về trang nhập liệu (form)
    }
    @GetMapping("/searchAchievement")
    public String searchAchievement(@RequestParam("nameMember") String name,
                                    @RequestParam("mssv") String mssv, Model model) {

        // Tìm tất cả các member theo tên và MSSV
        List<Member> members = memberService.findMemberByNameAndMSSV(name, mssv);

        if (members.isEmpty()) {
            model.addAttribute("message", "Không tìm thấy sinh viên với tên và MSSV này.");
            return "searchAchievement";  // Quay lại trang tìm kiếm nếu không tìm thấy sinh viên
        }

        // Lọc tất cả các thành tích từ các đội mà sinh viên tham gia
        List<Team> topTeams = members.stream()
                .flatMap(member -> teamService.findTeamsByMember(member).stream())  // Lấy các đội của member
                .filter(team -> team.getStatus() == 0)  // Lọc đội có trạng thái là 0 (đang tham gia)
              // Lọc các đội có thành tích top 4
                .collect(Collectors.toList());

        // Nếu có thành tích, thêm vào model để hiển thị
        if (!topTeams.isEmpty()) {
            model.addAttribute("topTeams", topTeams);
        } else {
            model.addAttribute("message", "Sinh viên không có thành tích nào thỏa mãn yêu cầu.");
        }

        return "searchAchievement";  // Trang hiển thị thông tin thành tích
    }
}
