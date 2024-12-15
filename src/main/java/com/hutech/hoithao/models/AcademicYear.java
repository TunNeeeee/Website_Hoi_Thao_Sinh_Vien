package com.hutech.hoithao.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "AcademicYear")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AcademicYear {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Integer id;

    @Column(name = "name_academic_year", nullable = false, length = 100)
    private String nameAcademicYear;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_event", nullable = false)
    @NotNull(message = "Hội thao không được để trống.")
    private Event event;

    @OneToMany(mappedBy = "academicYear", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Sport> listSports;
}
