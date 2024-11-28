package com.hutech.hoithao.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "round")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Round {
    @Id
    private Integer id;
    private String roundName;
    @OneToMany(mappedBy = "round")
    private Set<Match> listMatch;
}
