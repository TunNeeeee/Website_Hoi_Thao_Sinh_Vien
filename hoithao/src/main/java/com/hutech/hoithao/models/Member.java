package com.hutech.hoithao.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Member")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    // Ten thanh vien
    @Column(name = "name_member")
    private String nameMember;
    // Ma so sinh vien
    @Column(name = "mssv_member")
    private String mssv;
    @ManyToOne
    @JoinColumn(name="id_team")
    private Team team;
}
