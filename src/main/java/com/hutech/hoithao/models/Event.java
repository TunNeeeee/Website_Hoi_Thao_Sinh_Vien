package com.hutech.hoithao.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "Event")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    // Ten su kien the thao
    @Column(name = "eventName")
    private String eventName;
    // Ten don vi to chuc
    @Column(name = "organizationsName")
    private String organizationsName;
    // Ngay khoi tranh
    @Column(name = "time_start")
    private LocalDate startDate;
    // Ngay ket thuc
    @Column(name = "time_end")
    private LocalDate endDate;
    @ManyToOne
    @JoinColumn(name="id_status")
    private Status_Event status;
    @OneToMany(mappedBy = "event")
    private Set<Sport> listSport;
}
