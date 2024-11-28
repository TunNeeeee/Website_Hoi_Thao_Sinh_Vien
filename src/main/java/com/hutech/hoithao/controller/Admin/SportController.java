package com.hutech.hoithao.controller.Admin;

import com.hutech.hoithao.models.Event;
import com.hutech.hoithao.models.Format;
import com.hutech.hoithao.models.Sport;
import com.hutech.hoithao.models.Status_Event;
import com.hutech.hoithao.service.EventService;
import com.hutech.hoithao.service.FormatService;
import com.hutech.hoithao.service.SportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class SportController {
    @Autowired
    private SportService sportService;
    @Autowired
    private EventService eventService;
    @Autowired
    private FormatService formatService;
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




    @PostMapping("/delete-sport/{id}")
    public String delete(@PathVariable("id") Integer id) {
        Sport sport =this.sportService.findById(id);
        sport.setStatus(-1);
        sportService.update(sport);
        return "redirect:/admin/event";
    }
}