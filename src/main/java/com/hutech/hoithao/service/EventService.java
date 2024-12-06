package com.hutech.hoithao.service;

import com.hutech.hoithao.models.Event;
import com.hutech.hoithao.models.Status_Event;
import com.hutech.hoithao.repository.EventRepository;
import com.hutech.hoithao.repository.Status_EventRepository;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class EventService {
    private EventRepository eventRepository;
    private Status_EventRepository status_eventRepository;

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public List<Event> getEventsByStatus(Status_Event status) {
        return eventRepository.findByStatus(status);
    }

    public Optional<Event> findById(Integer id) {
        return this.eventRepository.findById(id);
    }

    //add event
    public Event addEvent(Event event) {
        return eventRepository.save(event);
    }

    //update event
    public void updateEvent(@NotNull Event event) {
        Event existingEvent = eventRepository.findById(event.getId())
                .orElseThrow(() -> new IllegalStateException("Category with ID " +
                        event.getId() + " does not exist."));
        existingEvent.setStartDate(event.getStartDate());
        existingEvent.setEndDate(event.getEndDate());
        existingEvent.setStatus(event.getStatus());
        eventRepository.save(existingEvent);
    }

    //delete event
    public void deleteEvent(@NotNull Event event) {
        Event existingEvent = eventRepository.findById(event.getId())
                .orElseThrow(() -> new IllegalStateException("Status with ID " + event.getId() + " does not exist."));
        existingEvent.setStatus(null);
        Status_Event deletedStatus = status_eventRepository.findStatus_EventByStatusName("Đã xóa")
                .orElseThrow(() -> new IllegalStateException("Status 'Đã xóa' not found."));
        existingEvent.setStatus(deletedStatus);
        eventRepository.save(existingEvent);
    }

    //search
    public List<Event> searchEventsByKeyword(String keyword) {
        return eventRepository.findByEventNameContainingIgnoreCase(keyword);
    }
}
