package com.hutech.hoithao.repository;


import com.hutech.hoithao.models.Event;
import com.hutech.hoithao.models.Status_Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
    List<Event> findByEventNameContainingIgnoreCase(String keyword);
    List<Event> findByStatus(Status_Event status);
}
