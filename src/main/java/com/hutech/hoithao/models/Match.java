package com.hutech.hoithao.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "`match`")
@Getter
@Setter
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
    private LocalTime timeStart;
    @ManyToOne
    private Team team1;
    @Column(name = "point1")
    private Integer point1;
    @Column(name = "bonuspoint1")
    private Integer bonuspoint1;
    @ManyToOne
    private Team team2;
    @Column(name = "point2")
    private Integer point2;
    @Column(name = "bonuspoint2")
    private Integer bonuspoint2;
    @Column(name = "winner")
    private Integer winner;
    @ManyToOne
    @JoinColumn(name = "id_arena")
    private Arena arena;
    @ManyToOne
    @JoinColumn(name = "id_round")
    private Round round;
}
