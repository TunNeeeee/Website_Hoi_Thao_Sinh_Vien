package com.hutech.hoithao.repository;


import com.hutech.hoithao.models.Sport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SportRepository extends JpaRepository<Sport, Integer> {
    List<Sport> findByEventId(Integer id);
    List<Sport> findBySportNameContaining(String keyword);
    List<Sport> findByStatus(Integer status);
}