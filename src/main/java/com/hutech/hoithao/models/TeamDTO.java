package com.hutech.hoithao.models;

import java.util.Objects;

public class TeamDTO {
    private Integer id;        // ID của đội
    private String teamName;   // Tên của đội

    // Constructor không tham số
    public TeamDTO() {
    }

    // Constructor đầy đủ tham số
    public TeamDTO(Integer id, String teamName) {
        this.id = id;
        this.teamName = teamName;
    }

    // Getter và Setter
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    // Override equals() và hashCode()
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamDTO teamDTO = (TeamDTO) o;
        return Objects.equals(id, teamDTO.id) && Objects.equals(teamName, teamDTO.teamName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, teamName);
    }

    // Override toString()
    @Override
    public String toString() {
        return "TeamDTO{" +
                "id=" + id +
                ", teamName='" + teamName + '\'' +
                '}';
    }
}
