package com.hutech.hoithao.service;

import com.hutech.hoithao.models.Status_Event;
import com.hutech.hoithao.repository.Status_EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Status_EventService {
    @Autowired
    private Status_EventRepository statusRepository;
    public List<Status_Event> getAll() {
        return statusRepository.findAll();
    }
    public Status_Event getById(int id) {
        return statusRepository.findStatus_EventById(id);
    }
}
