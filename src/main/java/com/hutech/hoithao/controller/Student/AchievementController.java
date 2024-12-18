package com.hutech.hoithao.controller.Student;

import com.hutech.hoithao.models.Member;
import com.hutech.hoithao.models.Team;
import com.hutech.hoithao.service.MemberService;
import com.hutech.hoithao.service.TeamService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
        model.addAttribute("nameMember", name); // Pass name
        model.addAttribute("mssv", mssv); // Pass MSSV
        return "searchAchievement";  // Trang hiển thị thông tin thành tích
    }
    @GetMapping("/createCertificate/{teamId}")
    public String createCertificate(@RequestParam("nameMember") String name,
                                    @RequestParam("mssv") String mssv,
                                    @PathVariable("teamId") Integer teamId, Model model) {
        // Tìm team theo ID
        Team team = teamService.findTeamById(teamId);

        if (team != null) {
            Member member = team.getListMember().stream()
                    .filter(m -> m.getNameMember().equalsIgnoreCase(name) && m.getMssv().equals(mssv))
                    .findFirst()
                    .orElse(null);

            // Thêm thông tin team vào model
            model.addAttribute("nameMember", name);
            model.addAttribute("mssv", mssv);
            model.addAttribute("team", team);
            return "certificate";  // Chuyển đến trang tạo giấy chứng nhận
        } else {
            model.addAttribute("message", "Không tìm thấy thông tin đội.");
            return "searchAchievement";  // Quay lại trang tìm kiếm nếu không tìm thấy team
        }
    }
//    @GetMapping("/downloadCertificate")
//    public void downloadCertificate(@RequestParam("nameMember") String name, @RequestParam("mssv") String mssv, HttpServletResponse response) throws DocumentException, IOException {
//        // Tìm kiếm member theo tên và MSSV
//        Member member = memberService.findMemberByNameAndMSSV(name, mssv);
//
//        if (member != null) {
//            // Lấy đội của sinh viên
//            Team team = teamService.findTeamByMember(member);
//
//            if (team != null) {
//                // Tạo tài liệu PDF
//                Document document = new Document();
//                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//                PdfWriter.getInstance(document, byteArrayOutputStream);
//
//                document.open();
//                document.add(new Paragraph("Giấy chứng nhận tham gia"));
//                document.add(new Paragraph("Tên sinh viên: " + member.getNameMember()));  // Lấy tên sinh viên từ Member
//                document.add(new Paragraph("MSSV: " + member.getMssv()));  // Lấy MSSV từ Member
//                document.add(new Paragraph("Môn thể thao: " + team.getSport().getSportName()));
//                document.add(new Paragraph("Hội thao: " + team.getSport().getAcademicYear().getEvent().getEventName()));
//                document.add(new Paragraph("Hạng: " + (team.getNoFinal() <= 4 ? "Hạng " + team.getNoFinal() : "Tham gia")));
//
//                document.close();
//
//                // Cấu hình headers cho trình duyệt tải file PDF
//                response.setContentType("application/pdf");
//                response.setHeader("Content-Disposition", "inline; filename=GiayChungNhan.pdf");
//                response.getOutputStream().write(byteArrayOutputStream.toByteArray());
//                response.getOutputStream().flush();
//            } else {
//                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy đội của sinh viên.");
//            }
//        } else {
//            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy sinh viên với tên và MSSV này.");
//        }
//    }


}
