package com.hutech.hoithao.service;

import com.hutech.hoithao.models.Arena;
import com.hutech.hoithao.repository.ArenaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArenaService {
    @Autowired
    private ArenaRepository arenaRepository;
    public List<Arena> findAll(){
        return arenaRepository.findAll();
    }
    public Arena findById(Integer id){
        return arenaRepository.findById(id).get();
    }
}
