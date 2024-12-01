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
    public List<Team> findTeamsBySportId(Integer sportId) {
        return teamRepository.findBySportId(sportId);
    }
    public Team findTeamById(Integer id) {
        return teamRepository.findById(id).orElse(null);
    }
    public long countApprovedTeamsBySport(Integer sportId) {
        return teamRepository.countBySportIdAndStatus(sportId, 2); // 2 là trạng thái đã duyệt
    }
    public List<Team> findTeamsByStatus(Integer sportId, List<Integer> statuses) {
        if (sportId == null) {
            return teamRepository.findByStatusIn(statuses);
        } else {
            return teamRepository.findBySportIdAndStatusIn(sportId, statuses);
        }
    }
    public List<Team> getTeamsNotInAnyGroup(Integer idSport) {
        return teamRepository.findTeamsNotInAnyGroupBySport(idSport);
    }
    public List<Team> getTeamsByIds(List<Integer> ids) {
        return teamRepository.findAllById(ids);
    }
    // Lưu danh sách các đội
    public List<Team> saveAll(List<Team> teams) {
        return teamRepository.saveAll(teams);
    }
}
