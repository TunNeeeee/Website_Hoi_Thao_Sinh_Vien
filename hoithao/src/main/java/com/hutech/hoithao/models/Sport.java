package com.hutech.hoithao.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "Sport")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Sport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    // Ten mon the thao
    @Column(name = "sport_name")
    private String sportName;
    // Thoi gian bat dau thi dau
    @Column(name = "time_start")
    private LocalDate startDate;
    // Thoi gian ket thuc
    @Column(name = "time_end")
    private LocalDate endDate;
    // So luong team toi da
    @Column(name = "number_team")
    private Integer numberTeamMax;
    // So luong thanh vien cua 1 team
    @Column(name = "number_member")
    private Integer numberMember;
    // Phi ky quy : phi sau khi tham gia se duoc nhan lai ( tru bo giai)
    @Column(name = "PhiKyQuy")
    private Integer phiKyQuy;
    // Phi tham gia
    @Column(name = "PhiThamGia")
    private Integer phiThamGia;
    // Trang thai cua mon the thao -1: Chua bat dau 0: Dang dien ra 1: Da ket thuc
    @Column
    private Integer status;
    @ManyToOne
    @JoinColumn(name="idEvent")
    private Event event;
//    @OneToMany(mappedBy = "sport")
//    private Set<Location> listLocation;
    @OneToMany(mappedBy = "sport")
    private Set<Team> listTeam;
    @ManyToOne
    @JoinColumn(name="idFormat")
    private Format format;


}
