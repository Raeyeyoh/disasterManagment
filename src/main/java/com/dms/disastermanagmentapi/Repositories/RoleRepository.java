package com.dms.disastermanagmentapi.Repositories;

import org.springframework.data.jpa.repository.JpaRepository; 
import org.springframework.stereotype.Repository;

import com.dms.disastermanagmentapi.entities.*;

import java.util.Optional;



@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleName(String roleName);
}

