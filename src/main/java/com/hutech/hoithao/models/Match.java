package com.hutech.hoithao.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "`match`")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    //Ten tran dau
    @Column(name = "match_name")
    private String matchName;
    //Ngay thi dau
    @Column(name = "`time`")
    private LocalDate time;
    //Thoi gian thi dau
    @Column(name = "timestart")
    private String timeStart;
    @ManyToOne
    private Team team1;
    @Column(name = "point1")
    private int point1;
    @ManyToOne
    private Team team2;
    @Column(name = "point2")
    private int point2;
    @ManyToOne
    @JoinColumn(name="id_arena")
    private Arena arena;
    @ManyToOne
    @JoinColumn(name="id_round")
    private Round round;
}