package com.hutech.hoithao.controller.Student;

import com.hutech.hoithao.models.Member;
import com.hutech.hoithao.models.Team;
import com.hutech.hoithao.models.User;
import com.hutech.hoithao.models.Sport;
import com.hutech.hoithao.repository.UserRepository;
import com.hutech.hoithao.service.MemberService;
import com.hutech.hoithao.service.TeamService;
import com.hutech.hoithao.service.UserService;
import com.hutech.hoithao.service.SportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/student/team")
public class StudentTeamController {

    @Autowired
    private TeamService teamService;

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SportService sportService;
    @Autowired
    private MemberService memberService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        // Thêm một danh sách các môn thể thao vào model để hiển thị trong form
        List<Sport> activeSports = sportService.getActiveSports();
        model.addAttribute("sports", activeSports); // Thêm danh sách các môn thể thao vào model


        // Trả về tên của view (tệp HTML) để hiển thị form đăng ký
        return "student/registration"; // Đây là tên của tệp HTML (ví dụ: register_team.html)
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerTeam(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam String teamName,
            @RequestParam Integer sportId,
            @RequestParam List<String> memberNames,
            @RequestParam("paymentProof") MultipartFile paymentProofFile,
            @RequestParam List<String> mssvMembers) {

        // Tìm kiếm User và Sport từ database
        User currentUser = userRepository.findByUsername(user.getUsername());
        Sport sport = sportService.findById(sportId);

        if (currentUser == null || sport == null) {
            return ResponseEntity.badRequest().body("User hoặc Sport không hợp lệ");
        }

        // Kiểm tra kích thước file ảnh
        long maxFileSize = 5 * 1024 * 1024; // 5 MB
        if (paymentProofFile.getSize() > maxFileSize) {
            return ResponseEntity.badRequest().body("File ảnh minh chứng quá lớn. Vui lòng chọn ảnh nhỏ hơn " + (maxFileSize / (1024 * 1024)) + " MB.");
        }

        // Chuyển đổi file ảnh sang mảng byte
        byte[] paymentProof = null;
        try {
            paymentProof = paymentProofFile.getBytes();  // Chuyển đổi MultipartFile thành mảng byte
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Lỗi khi xử lý ảnh minh chứng");
        }

        // Tạo đối tượng Team
        Team team = Team.builder()
                .teamName(teamName)
                .sport(sport)
                .user(currentUser)
                .status(1)
                .noFinal(sport.getNumberTeamMax())
                .paymentProof(paymentProof)  // Lưu ảnh dưới dạng byte[]
                .build();

        // Lưu Team vào database
        teamService.saveTeam(team);

        // Lưu danh sách thành viên
        Set<Member> members = new HashSet<>();
        for (int i = 0; i < memberNames.size(); i++) {
            String memberName = memberNames.get(i);
            String mssvMember = mssvMembers.get(i);

            Member member = new Member();
            member.setNameMember(memberName);
            member.setMssv(mssvMember);
            member.setTeam(team);  // Gán team cho member
            members.add(member);
        }

        // Lưu tất cả thành viên cùng một lúc
        memberService.saveAllMembers(members);  // Giả sử bạn có phương thức này trong MemberService

        // Gán danh sách thành viên cho team và cập nhật lại
        team.setListMember(members);
        teamService.saveTeam(team);  // Cập nhật lại team với danh sách thành viên

        return ResponseEntity.ok("Đăng ký thành công!");  // Trả về phản hồi thành công
    }
}
