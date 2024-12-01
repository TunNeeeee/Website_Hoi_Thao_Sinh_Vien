package com.hutech.hoithao.repository;


import com.hutech.hoithao.models.Group;
import com.hutech.hoithao.models.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Integer> {
    List<Group> findBySportId(Integer idSport);
    List<Group> findBySportIdOrderByGroupNameAsc(Integer sportId);
}
