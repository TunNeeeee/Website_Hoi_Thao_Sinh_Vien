package com.hutech.hoithao.service;

import com.hutech.hoithao.models.Sport;
import com.hutech.hoithao.repository.SportRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
@Service
public class TransactionService {
    @Autowired
    private SportRepository sportRepository;
    @Transactional
    public void updateSportStatus() {
        // Lấy danh sách môn thể thao có trạng thái 1
        List<Sport> sports = sportRepository.findByStatus(1);

        // Lọc các môn thể thao cần cập nhật
        List<Sport> updatedSports = new ArrayList<>();
        for (Sport sport : sports) {
            if (sport.getStartDate().isBefore(LocalDate.now()) || sport.getStartDate().isEqual(LocalDate.now())) {
                // Cập nhật trạng thái nếu cần thiết
                if (sport.getStatus() != 0) {
                    sport.setStatus(0);
                    updatedSports.add(sport); // Thêm môn thể thao vào danh sách cập nhật
                }
            }
        }

        // Lưu tất cả các môn thể thao đã thay đổi trạng thái
        if (!updatedSports.isEmpty()) {
            sportRepository.saveAll(updatedSports); // Lưu tất cả thay đổi trong một lần gọi
        }
    }
}
