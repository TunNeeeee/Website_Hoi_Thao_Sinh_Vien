package com.hutech.hoithao.repository;

import com.hutech.hoithao.models.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoundRepository extends JpaRepository<Round, Integer> {
    List<Round> findByIdGreaterThanEqual(Integer id);

}
