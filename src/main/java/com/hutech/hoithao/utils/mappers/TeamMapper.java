package com.hutech.hoithao.utils.mappers;


import com.hutech.hoithao.domains.dtos.TeamDTO;
import com.hutech.hoithao.models.Team;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TeamMapper {
    TeamDTO toDTO(Team team);
}
