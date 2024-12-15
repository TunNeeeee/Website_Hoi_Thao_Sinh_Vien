package com.hutech.hoithao.controller.Admin;

import com.hutech.hoithao.models.AcademicYear;
import com.hutech.hoithao.models.Event;
import com.hutech.hoithao.service.AcademicYearService;
import com.hutech.hoithao.service.EventService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AcademicYearController {

    private final AcademicYearService academicYearService;
    @Autowired
    private EventService eventService;
    public AcademicYearController(AcademicYearService academicYearService) {
        this.academicYearService = academicYearService;
    }

    @GetMapping("/sports-of-academic-year/{id}")
    public String getSportsOfAcademicYear(@PathVariable Integer id, Model model) {
        // Lấy niên khóa theo ID
        AcademicYear academicYear = academicYearService.findById(id);
        if (academicYear == null) {
            // Nếu niên khóa không tồn tại, chuyển hướng về trang lỗi hoặc danh sách niên khóa
            return "redirect:/admin/academic-years";
        }

        // Thêm niên khóa và danh sách môn thể thao vào model
        model.addAttribute("academicYear", academicYear);
        model.addAttribute("sportsList", academicYear.getListSports());

        // Trả về trang hiển thị danh sách môn thể thao
        return "/admin/academic-year/sports-of-academic-year";
    }
    @GetMapping("/add-academic-year-to-event/{id}")
    public String showAddForm(@PathVariable Integer id, Model model) {
        Event event = eventService.getEventById(id);
        if (event == null) {
            model.addAttribute("errorMessage", "Sự kiện không tồn tại!");
            return "/admin/error-page"; // Một trang lỗi thân thiện
        }

        AcademicYear academicYear = new AcademicYear();
        academicYear.setEvent(event); // Gán sự kiện hiện tại vào niên khóa mới
        model.addAttribute("academicYear", academicYear);
        model.addAttribute("eventName", event.getEventName()); // Hiển thị tên sự kiện trong form (nếu cần)

        return "/admin/academic-year/add"; // Tên view cho form thêm mới
    }
    @PostMapping("/add-academic-year-to-event")
    public String addAcademicYear(@Valid @ModelAttribute AcademicYear academicYear, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("errorMessage", "Thông tin niên khóa không hợp lệ! Vui lòng kiểm tra lại.");
            return "/admin/academic-year/add";
        }

        // Kiểm tra tên niên khóa
        if (academicYear.getNameAcademicYear() == null || academicYear.getNameAcademicYear().isEmpty()) {
            model.addAttribute("errorMessage", "Tên niên khóa không được để trống!");
            return "/admin/academic-year/add";
        }

        // Kiểm tra id của Event
        if (academicYear.getEvent() == null || academicYear.getEvent().getId() == null) {
            model.addAttribute("errorMessage", "Sự kiện không hợp lệ! Vui lòng kiểm tra lại.");
            return "/admin/academic-year/add";
        }

        // Truy xuất Event từ cơ sở dữ liệu dựa trên id
        Event event = eventService.getEventById(academicYear.getEvent().getId());
        if (event == null) {
            model.addAttribute("errorMessage", "Sự kiện không tồn tại!");
            return "/admin/error-page";
        }

        // Gán Event đã được quản lý cho AcademicYear
        academicYear.setEvent(event);

        try {
            academicYearService.createAcademicYear(academicYear);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Đã xảy ra lỗi khi thêm niên khóa: " + e.getMessage());
            return "/admin/academic-year/add";
        }
        return "redirect:/admin/event";
    }

    @GetMapping("/edit-academic-year/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        AcademicYear academicYear = academicYearService.getAcademicYearById(id);
        model.addAttribute("academicYear", academicYear);
        model.addAttribute("events", eventService.getAllEvents());
        return "/admin/academic-year/edit"; // Tên view cho form chỉnh sửa
    }

    @PostMapping("/edit-academic-year/{id}")
    public String editAcademicYear(@PathVariable Integer id, @ModelAttribute AcademicYear academicYear) {
        academicYearService.updateAcademicYear(id, academicYear);
        return "redirect:/admin/event";
    }

    @PostMapping("/delete-academic-year/{id}")
    public String deleteAcademicYear(@PathVariable Integer id) {
        academicYearService.deleteAcademicYear(id);
        return "redirect:/admin/event";
    }
}
