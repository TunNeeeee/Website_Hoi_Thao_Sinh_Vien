package com.hutech.hoithao.service;

import com.hutech.hoithao.models.Team;
import com.hutech.hoithao.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamService {
    @Autowired
    private TeamRepository teamRepository;

    // Lưu team vào cơ sở dữ liệu
    public void saveTeam(Team team) {
        teamRepository.save(team);
    }
    public List<Team> findAllTeams() {
        return teamRepository.findAllByOrderByIdAsc();
    }

    public Team findTeamById(Integer id) {
        return teamRepository.findById(id).orElse(null);
    }
}
