package com.hutech.hoithao.repository;

import com.hutech.hoithao.models.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRepository extends JpaRepository<Match, Integer> {
    // Bạn có thể thêm query tùy chỉnh nếu cần
}
