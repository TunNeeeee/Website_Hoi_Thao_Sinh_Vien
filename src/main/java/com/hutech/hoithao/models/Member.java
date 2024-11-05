package com.hutech.hoithao.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Member")
@Getter
@Setter
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
    @JsonIgnore // Ngăn không cho serialize trường này để tránh StackOverflowError
    private Team team;
}
