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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    // Đường dẫn thư mục lưu trữ ảnh
    private final String UPLOAD_DIR = "src/main/resources/static/uploads/payment-proofs";

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        // Lấy danh sách các môn thể thao đang hoạt động
        List<Sport> activeSports = sportService.getActiveSports();
        model.addAttribute("sports", activeSports);

        return "student/registration";
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerTeam(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam String teamName,
            @RequestParam Integer sportId,
            @RequestParam List<String> memberNames,
            @RequestParam("paymentProof") MultipartFile paymentProofFile,
            @RequestParam List<String> mssvMembers) {

        // Tìm User và Sport
        User currentUser = userRepository.findByUsername(user.getUsername());
        Sport sport = sportService.findById(sportId);

        if (currentUser == null || sport == null) {
            return ResponseEntity.badRequest().body("User hoặc Sport không hợp lệ");
        }

        // Kiểm tra số lượng tên thành viên và MSSV
        if (memberNames.size() != mssvMembers.size()) {
            return ResponseEntity.badRequest().body("Số lượng tên thành viên và MSSV không khớp");
        }

        // Kiểm tra kích thước file ảnh
        long maxFileSize = 5 * 1024 * 1024; // 5 MB
        if (paymentProofFile.getSize() > maxFileSize) {
            return ResponseEntity.badRequest().body("File ảnh minh chứng quá lớn. Vui lòng chọn ảnh nhỏ hơn 5 MB.");
        }

        // Lưu file ảnh vào thư mục
        String fileName = System.currentTimeMillis() + "-" + paymentProofFile.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR, fileName);
        try {
            Files.createDirectories(filePath.getParent()); // Tạo thư mục nếu chưa tồn tại
            Files.write(filePath, paymentProofFile.getBytes()); // Lưu file ảnh
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Lỗi khi lưu ảnh minh chứng: " + e.getMessage());
        }

        // Tạo đối tượng Team
        Team team = Team.builder()
                .teamName(teamName)
                .sport(sport)
                .user(currentUser)
                .status(1)
                .noFinal(sport.getNumberTeamMax())
                .paymentProofPath("/uploads/payment-proofs/" + fileName) // Đường dẫn tương đối
                .build();

        // Lưu Team vào database
        teamService.saveTeam(team);

        // Lưu danh sách thành viên
        Set<Member> members = new HashSet<>();
        for (int i = 0; i < memberNames.size(); i++) {
            Member member = new Member();
            member.setNameMember(memberNames.get(i));
            member.setMssv(mssvMembers.get(i));
            member.setTeam(team);
            members.add(member);
        }
        memberService.saveAllMembers(members); // Lưu tất cả thành viên

        // Gán danh sách thành viên cho team và cập nhật lại
        team.setListMember(members);
        teamService.saveTeam(team);

        return ResponseEntity.ok("Đăng ký thành công!");
    }
}
