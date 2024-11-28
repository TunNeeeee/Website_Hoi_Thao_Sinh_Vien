package com.hutech.hoithao.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "status")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Status_Event {
    @Id
    private Integer id;
    private String statusName;
    @OneToMany(mappedBy = "status")
    private Set<Event> listEvent;
}
