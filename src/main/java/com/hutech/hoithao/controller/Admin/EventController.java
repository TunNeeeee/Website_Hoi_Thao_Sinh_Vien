package com.hutech.hoithao.controller.Admin;

import com.hutech.hoithao.models.Event;
import com.hutech.hoithao.models.Status_Event;
import com.hutech.hoithao.service.EventService;
import com.hutech.hoithao.service.Status_EventService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor()
public class EventController {
    EventService eventService;
    Status_EventService statusService;

    @GetMapping("/event")
    public String index(Model model, @Param("keyword") String keyword) {
        List<Event> list = this.eventService.getAllEvents();
        if (keyword != null) {
            list = this.eventService.searchEventsByKeyword(keyword);
            model.addAttribute("keyword", keyword);
        }
        model.addAttribute("list", list);
        return "admin/event/index";
    }

    @GetMapping("/add-event")
    public String add_event(Model model) {
        Event event = new Event();
        model.addAttribute("status", statusService.getAll());
        model.addAttribute("event", event);
        return "admin/event/add";
    }

    @PostMapping("/add-event")
    public String add_event(@ModelAttribute("event") Event event) {
        eventService.addEvent(event);
        return "redirect:/admin/event";
    }

    @GetMapping("/edit-event/{id}")
    public String edit(Model model, @PathVariable("id") Integer id) {
        Event event = eventService.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid event Id:" + id));
        model.addAttribute("statuses", statusService.getAll());
        model.addAttribute("event", event);
        return "/admin/event/edit";
    }

    @PostMapping("/edit-event/{id}")
    public String update(@PathVariable("id") Integer id, @Valid Event event,
                         BindingResult result, Model model) {
        if (result.hasErrors()) {
            event.setId(id);
            return "/admin/event/edit";
        }
        eventService.updateEvent(event);
        model.addAttribute("events", eventService.getAllEvents());
        return "redirect:/admin/event";
    }

    @PostMapping("/delete-event/{id}")
    public String delete(@PathVariable("id") Integer id, @Valid Event event,
                         BindingResult result, Model model) {
        if (result.hasErrors()) {
            event.setId(id);
            return "/admin/event/edit";
        }
        eventService.deleteEvent(event);
        model.addAttribute("events", eventService.getAllEvents());
        return "redirect:/admin/event";
    }

    @GetMapping("/search")
    public List<Event> searchEventsByKeyword(@RequestParam String keyword) {
        return eventService.searchEventsByKeyword(keyword);
    }

}
