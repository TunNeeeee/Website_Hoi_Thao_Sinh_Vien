package com.hutech.hoithao.repository;


import com.hutech.hoithao.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IRoleRepository extends JpaRepository<Role, Integer> {
    Role findRoleById(Integer id);
}
