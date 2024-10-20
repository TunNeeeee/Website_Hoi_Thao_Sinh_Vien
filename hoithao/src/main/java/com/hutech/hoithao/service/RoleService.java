package com.hutech.hoithao.service;


import com.hutech.hoithao.models.Role;
import com.hutech.hoithao.repository.IRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

    @Autowired
    private IRoleRepository roleRepository;

    public Role findByRoleID(Integer idRole) {
        return roleRepository.findRoleById(idRole);
    }

    public Role saveRole(Role role) {
        return roleRepository.save(role);
    }
}
