package com.hutech.hoithao.service;

import com.hutech.hoithao.models.Round;
import com.hutech.hoithao.repository.RoundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoundService {
    @Autowired
    private RoundRepository roundRepository;
    public Round findById(Integer roundId) {
        return roundRepository.findById(roundId).orElse(null);  // Trả về null nếu không tìm thấy Round
    }

    public String getRoundName(Integer roundId) {
        return roundRepository.findById(roundId)
                .map(Round::getRoundName)
                .orElse("Không xác định");
    }
}
