package com.hutech.hoithao.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "RuleDetail")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    @JoinColumn(name="id_team")
    private Team team;
    @ManyToOne
    @JoinColumn(name="id_rule")
    private Rule rule;
    //So lan vi pham
    @Column(name = "count_vp")
    private Integer countVP;

}
