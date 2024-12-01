package com.hutech.hoithao.service;

import com.hutech.hoithao.exceptions.ResourceNotFoundException;
import com.hutech.hoithao.models.Group;
import com.hutech.hoithao.models.Sport;
import com.hutech.hoithao.models.Team;
import com.hutech.hoithao.repository.GroupRepository;
import com.hutech.hoithao.repository.SportRepository;
import com.hutech.hoithao.repository.TeamRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class SportService {
    @Autowired
    private SportRepository sportRepository;
    private GroupRepository groupRepository;
    private TeamRepository teamRepository;
    public List<Sport> getAllSports() {
        return sportRepository.findAll();
    }
    public Sport findById(Integer idSport) {
        return sportRepository.findById(idSport).orElse(null);
    }
    public List<Sport> getSportsByEvent(Integer id) {
        return sportRepository.findByEventId(id);
    }
    public Sport getSportById(Integer id) {
        return sportRepository.findById(id).orElse(null);
    }
    public List<Sport> searchSport(String keyword) {
        return List.of();
    }
    public Page<Sport> getAll(Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, 3);
        return sportRepository.findAll(pageable);
    }
    public void saveSport(Sport sport) {
        sportRepository.save(sport);
    }
    //add event
    public Boolean create(Sport sport) {
        try {
            sport.setStatus(1);
            sportRepository.save(sport);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    //update sport
    public Boolean update(Sport sport) {
        try {
            sportRepository.save(sport);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    // delete sport
    public boolean delete(Integer id) {
        try {
            sportRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Page<Sport> searchSportPage(String keyword, Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, 3);
        List<Sport> sports = sportRepository.findBySportNameContaining(keyword);
        int start = Math.min((int) pageable.getOffset(), sports.size());
        int end = Math.min((start + pageable.getPageSize()), sports.size());
        return new PageImpl<>(sports.subList(start, end), pageable, sports.size());
    }
    // Lấy danh sách các môn thể thao có trạng thái = 1
    public List<Sport> getActiveSports() {
        return sportRepository.findByStatus(1); // Giả sử bạn đã có method này trong repository
    }
    @Transactional
    public void updateSportStatus() {
        // Lấy danh sách môn thể thao có trạng thái 1
        List<Sport> sports = sportRepository.findByStatus(1);

        // Kiểm tra từng môn thể thao
        for (Sport sport : sports) {
            if (sport.getStartDate().isBefore(LocalDate.now()) || sport.getStartDate().isEqual(LocalDate.now())) {
                // Cập nhật trạng thái
                sport.setStatus(0);
                sportRepository.save(sport); // Lưu lại thay đổi
            }
        }
    }
    // Lên lịch kiểm tra trạng thái (nếu muốn tích hợp trực tiếp)
    @Scheduled(cron = "0 0 0 * * ?") // Chạy hàng ngày lúc 00:00
    public void scheduleUpdateSportStatus() {
        updateSportStatus();
    }


}

