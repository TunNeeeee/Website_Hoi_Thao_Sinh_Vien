package com.hutech.hoithao.service;

import com.hutech.hoithao.models.Format;
import com.hutech.hoithao.repository.FormatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FormatService {
    @Autowired
    private FormatRepository formatRepository;
    public List<Format> findAll(){
        return formatRepository.findAll();
    }
}
