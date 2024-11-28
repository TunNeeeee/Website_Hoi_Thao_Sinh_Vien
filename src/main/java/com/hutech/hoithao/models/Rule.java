package com.hutech.hoithao.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "Rule")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Rule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    //Ten loi vi pham
    @Column(name = "name_rule")
    private String nameRule;
    //Tien phat
    @Column(name = "fines")
    private Integer fines;
    @OneToMany(mappedBy = "rule")
    private Set<RuleDetail> listRuleDetails;

}
