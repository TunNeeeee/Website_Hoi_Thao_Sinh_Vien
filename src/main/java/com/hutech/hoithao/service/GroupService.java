package com.hutech.hoithao.service;

import com.hutech.hoithao.exceptions.ResourceNotFoundException;
import com.hutech.hoithao.models.Group;
import com.hutech.hoithao.models.Sport;
import com.hutech.hoithao.models.Team;
import com.hutech.hoithao.repository.GroupRepository;
import com.hutech.hoithao.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;
    public List<Group> getGroupsBySport(Integer sportId) {
        return groupRepository.findBySportId(sportId);
    }
    public String generateNextGroupName(Integer idSport) {
        List<Group> groups = groupRepository.findBySportIdOrderByGroupNameAsc(idSport);
        char nextName = (char) ('A' + groups.size());
        return String.valueOf(nextName);
    }
    public void saveGroup(Group group) {
        groupRepository.save(group);
    }
    public Group getGroupById(Integer id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bảng đấu với ID: " + id));
    }
    public Optional<Group> getGroupWithTeams(Integer groupId) {
        return groupRepository.findById(groupId); // Giả sử đây là cách tìm nhóm trong cơ sở dữ liệu
    }
}
