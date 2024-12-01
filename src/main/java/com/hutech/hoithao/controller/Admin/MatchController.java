package com.hutech.hoithao.controller.Admin;

import com.hutech.hoithao.models.*;
import com.hutech.hoithao.repository.GroupRepository;
import com.hutech.hoithao.repository.MatchRepository;
import com.hutech.hoithao.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/{idSport}")
public class MatchController {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private GroupService groupService;
    // Trang tạo trận đấu
    @GetMapping("/create-match-vb")
    public String showCreateMatchForm(@PathVariable Integer idSport, Model model) {
        model.addAttribute("groups", groupRepository.findBySportId(idSport));
        model.addAttribute("match", new Match());
        return "/admin/match/create-match";
    }
    // Lấy danh sách đội theo bảng (API)
    @GetMapping("/groups/{groupId}/teams")
    @ResponseBody
    public List<TeamDTO> getTeamsByGroup(@PathVariable Integer groupId) {
        return groupService.getGroupWithTeams(groupId)
                .map(group -> group.getListTeam().stream()
                        .map(team -> new TeamDTO(team.getId(), team.getTeamName()))
                        .toList())
                .orElse(Collections.emptyList());
    }



    // Lưu thông tin trận đấu
    @PostMapping("/create-match-vb")
    public String createMatch(@PathVariable Long idSport, @ModelAttribute Match match, Model model) {
        Round round = new Round();
        round.setId(1);
        match.setRound(round); // Gán idRound = 1 (Vòng bảng - VB)
        matchRepository.save(match);
        return "redirect:/admin/" + idSport + "/groups";
    }
}
