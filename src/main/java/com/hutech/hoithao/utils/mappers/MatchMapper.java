package com.hutech.hoithao.utils.mappers;

import com.hutech.hoithao.models.Match;
import com.hutech.hoithao.models.MatchDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MatchMapper {
    MatchDTO toDTO(Match match);
}
