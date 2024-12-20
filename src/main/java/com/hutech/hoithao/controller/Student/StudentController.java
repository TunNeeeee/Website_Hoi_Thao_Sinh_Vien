package com.hutech.hoithao.controller.Student;

import com.hutech.hoithao.models.*;
//import com.hutech.hoithao.service.RankService;
//import com.hutech.hoithao.service.SportService;
//import com.hutech.hoithao.service.TeamService;
import com.hutech.hoithao.repository.*;
import com.hutech.hoithao.service.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StudentController {
    final GroupService groupService;
    final TeamService teamService;
    final SportService sportService;
    final UserRepository userRepository;
    final EventRepository eventRepository;
    final MemberService memberService;
    final MatchService matchService;
    final RoundRepository roundRepository;
    final MatchRepository matchRepository;
    // Đường dẫn thư mục lưu trữ ảnh
    private final String UPLOAD_DIR = "src/main/resources/static/uploads/payment-proofs";
    @GetMapping
    public String index(Model model) {
        List<Event> events = eventRepository.findAll();
        model.addAttribute("events", events);
        return "redirect:/student/";
    }

    @GetMapping("/")
    public String student(Model model) {
        List<Event> events = eventRepository.findAll();
        model.addAttribute("events", events);
        return "student/index";
    }

    @GetMapping("/logout")
    public String studentlogout() {
        return "login";
    }
    @GetMapping("/about")
    public String about() {
        return "student/about/about"; // Trả về file about.html
    }

    @GetMapping("/News")
    public String news() {
        return "student/News/index"; // Trả về file news.html
    }

    @GetMapping("/register-team/{idSport}")
    public String registerSport(@PathVariable Integer idSport, Model model) {
        Sport sport = sportService.findById(idSport);

        if (sport == null) {
            throw new IllegalArgumentException("Môn thể thao không tồn tại.");
        }
        List<Event> events = eventRepository.findAll();
        model.addAttribute("events", events);
        model.addAttribute("sport", sport); // Truyền thông tin môn thể thao vào view
        return "student/registersport/index"; // Tên file Thymeleaf
    }

    @PostMapping("/register-team")
    public String registerTeam(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam String teamName,
            @RequestParam Integer sportId,
            @RequestParam List<String> memberNames,
            @RequestParam("paymentProof") MultipartFile paymentProofFile,
            @RequestParam List<String> mssvMembers,
            RedirectAttributes redirectAttributes) {

        // Tìm User và Sport
        User currentUser = userRepository.findByUsername(user.getUsername());
        Sport sport = sportService.findById(sportId);

        if (currentUser == null || sport == null) {
            redirectAttributes.addFlashAttribute("error", "User hoặc Sport không hợp lệ.");
            return "redirect:/student"; // Chuyển hướng về trang chủ
        }

        // Kiểm tra số lượng tên thành viên và MSSV
        if (memberNames.size() != mssvMembers.size()) {
            redirectAttributes.addFlashAttribute("error", "Số lượng tên thành viên và MSSV không khớp.");
            return "redirect:/student"; // Chuyển hướng về trang chủ
        }

        // Kiểm tra kích thước file ảnh
        long maxFileSize = 5 * 1024 * 1024; // 5 MB
        if (paymentProofFile.getSize() > maxFileSize) {
            redirectAttributes.addFlashAttribute("error", "File ảnh minh chứng quá lớn. Vui lòng chọn ảnh nhỏ hơn 5 MB.");
            return "redirect:/student"; // Chuyển hướng về trang chủ
        }

        // Lưu file ảnh vào thư mục
        String fileName = System.currentTimeMillis() + "-" + paymentProofFile.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR, fileName);
        try {
            Files.createDirectories(filePath.getParent()); // Tạo thư mục nếu chưa tồn tại
            Files.write(filePath, paymentProofFile.getBytes()); // Lưu file ảnh
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi lưu ảnh minh chứng: " + e.getMessage());
            return "redirect:/student"; // Chuyển hướng về trang chủ
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

        // Thêm thông báo thành công và chuyển hướng về trang chủ
        redirectAttributes.addFlashAttribute("success", "Đăng ký thành công!");
        return "redirect:/student";
    }


    @GetMapping("/sportranking")
    public String sportRanking() {
        return "student/sportranking/index"; // File sportranking.html
    }

    @GetMapping("/sportSchedule")
    public String sportSchedule() {
        return "student/sportSchedule/index";
    }

    @GetMapping("/contact")
    public String contact () {
        return "student/contact/index"; // File sportranking.html
    }

    @GetMapping("/VB/{id}")
    public String vb(@PathVariable("id") Integer sportId, Model model) {
        List<Event> events = eventRepository.findAll();
        Sport sport = sportService.getSportById(sportId);


        List<Group> groups = groupService.getGroupsBySport(sportId);
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
        model.addAttribute("events", events);
        model.addAttribute("teamsNotInGroup", teamService.getTeamsNotInAnyGroup(sportId));
        model.addAttribute("matches", matchService.findMatchesBySportAndRound(sportId,round)); // Lấy danh sách trận đấu
        model.addAttribute("sport", sport);
        model.addAttribute("groups", groups);
        model.addAttribute("sortedTeamsByGroup", sortedTeamsByGroup); // Gửi danh sách đã sắp xếp
        model.addAttribute("lastThreeResultsByTeam", lastThreeResultsByTeam); // Gửi danh sách lastThreeResults
        return "/student/vb"; // Thymeleaf template for Table Tennis
    }

    @GetMapping("/TT/{id}")
    public String tt(@PathVariable("id") Integer sportId, Model model) {
        List<Event> events = eventRepository.findAll();
        Sport sport = sportService.getSportById(sportId);
        List<Match> matches = matchRepository.findBySportId(sportId);
        Map<String, List<Match>> bracketRounds = matches.stream()
                .filter(match -> match.getRound() != null)
                .collect(Collectors.groupingBy(match -> match.getRound().getRoundName(),
                        LinkedHashMap::new,
                        Collectors.toList()));
        model.addAttribute("events", events);
        model.addAttribute("bracketRounds", bracketRounds);
        model.addAttribute("matches", matches);
        model.addAttribute("sport", sport);
        return "/student/tt"; // Thymeleaf template for Table Tennis
    }

    // Route for /student/VB-TT/{id}
    @GetMapping("/VB-TT/{id}")
    public String vbtt(@PathVariable("id") Integer sportId, Model model) {
        List<Event> events = eventRepository.findAll();
        Sport sport = sportService.getSportById(sportId);


        List<Group> groups = groupService.getGroupsBySport(sportId);
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
        List<Match> matches = matchRepository.findBySportId(sportId);
        Map<String, List<Match>> bracketRounds = matches.stream()
                .filter(match -> match.getRound() != null)
                .collect(Collectors.groupingBy(match -> match.getRound().getRoundName(),
                        LinkedHashMap::new,
                        Collectors.toList()));
        Round round = new Round();
        round.setId(1);
        model.addAttribute("events", events);
        model.addAttribute("teamsNotInGroup", teamService.getTeamsNotInAnyGroup(sportId));
        model.addAttribute("matches", matchService.findMatchesBySportAndRound(sportId,round)); // Lấy danh sách trận đấu
        model.addAttribute("sport", sport);
        model.addAttribute("groups", groups);
        model.addAttribute("sortedTeamsByGroup", sortedTeamsByGroup); // Gửi danh sách đã sắp xếp
        model.addAttribute("lastThreeResultsByTeam", lastThreeResultsByTeam);
        model.addAttribute("bracketRounds", bracketRounds);
        return "/student/vb-tt"; // Thymeleaf template for Volleyball-Tennis
    }

    // Add POST mappings for form submissions if necessary
}