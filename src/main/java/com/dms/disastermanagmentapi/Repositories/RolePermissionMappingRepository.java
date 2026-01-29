package com.dms.disastermanagmentapi.Repositories;

import org.springframework.data.jpa.repository.JpaRepository; 
import org.springframework.stereotype.Repository;

import com.dms.disastermanagmentapi.entities.*;

import java.util.List;



@Repository
public interface RolePermissionMappingRepository extends JpaRepository<RolePermissionMapping, com.dms.disastermanagmentapi.entities.RolePermissionId> {
    List<RolePermissionMapping> findByRole(Role role);
}
