package com.hutech.hoithao.models;

import com.hutech.hoithao.domains.dtos.TeamDTO;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchDTO {
    private int id;
    private String matchName;
    private LocalDate time;
    private LocalTime timeStart;
    private TeamDTO team1;
    private Integer point1;
    private Integer bonuspoint1;
    private TeamDTO team2;
    private Integer point2;
    private Integer bonuspoint2;
    private Integer winner;
    private Arena arena;
    private Round round;
}

