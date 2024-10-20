package com.hutech.hoithao.repository;

import com.hutech.hoithao.models.Status_Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface Status_EventRepository extends JpaRepository<Status_Event, Integer> {
    Status_Event findStatus_EventById(int id);
    Optional<Status_Event> findStatus_EventByStatusName(String statusName);
}
