package com.hutech.hoithao.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Table(name = "round")
@Data
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
