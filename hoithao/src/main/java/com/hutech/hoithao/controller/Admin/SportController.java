package com.hutech.hoithao.controller.Admin;

import com.hutech.hoithao.models.Event;
import com.hutech.hoithao.models.Sport;
import com.hutech.hoithao.models.Status_Event;
import com.hutech.hoithao.service.EventService;
import com.hutech.hoithao.service.SportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class SportController {
    @Autowired
    private SportService sportService;
    @Autowired
    private EventService eventService;
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
        status.setId(1);
        List<Event> events = eventService.getEventsByStatus(status);
        model.addAttribute("events",events);
        model.addAttribute("sport",sport);
        return "admin/sport/add";
    }
    @GetMapping("/sport-add")
    public String add(Model model) {
        Status_Event status = new Status_Event();
        status.setId(1);
        List<Event> events = eventService.getEventsByStatus(status);
        Sport sport = new Sport();
        model.addAttribute("sport", sport);
        model.addAttribute("events", events);
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
            status.setId(1);
            List<Event> events = eventService.getEventsByStatus(status);
            model.addAttribute("events", events);
            return "admin/sport/add";
        }
    }

    @GetMapping("/edit-sport/{id}")
    public String edit(Model model, @PathVariable("id") Integer id) {
        Sport sport = sportService.findById(id);
        List<Event> events = eventService.getAllEvents();
        model.addAttribute("sport", sport);
        model.addAttribute("events", events);
        return "admin/sport/edit";
    }

    @PostMapping("/edit-sport")
    public String update(@ModelAttribute("sport") Sport sport, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            List<Event> events = eventService.getAllEvents();
            model.addAttribute("events", events);
            return "admin/sport/edit";
        }
        if (sportService.update(sport)) {
            return "redirect:/admin/sport";
        } else {
            model.addAttribute("errorMessage", "Could not update sport");
            List<Event> events = eventService.getAllEvents();
            model.addAttribute("events", events);
            return "admin/sport/edit";
        }
    }

    @PostMapping("/delete-sport/{id}")
    public String delete(@PathVariable("id") Integer id) {
        Sport sport =this.sportService.findById(id);
        sport.setStatus(0);
        sportService.update(sport);
        return "redirect:/admin/event";
    }
}