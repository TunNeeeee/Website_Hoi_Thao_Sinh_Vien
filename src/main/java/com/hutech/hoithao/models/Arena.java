package com.hutech.hoithao.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "Arena")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Arena {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    // Ten san thi dau
    @Column(name = "name_arena")
    private String nameArena;
    // Dia diem
    @Column(name = "capacity")
    private String capacity;
    @OneToMany(mappedBy = "arena")
    private Set<Match> listMatch;

}
