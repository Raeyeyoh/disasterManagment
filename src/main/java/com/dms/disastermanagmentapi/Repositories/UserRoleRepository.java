package com.dms.disastermanagmentapi.Repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dms.disastermanagmentapi.entities.*;

import java.util.List;
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, com.dms.disastermanagmentapi.entities.UserRoleId> {
    List<UserRole> findByUser(User user);
@Query("SELECT u FROM User u " +
       "JOIN u.userRoles ur " +
       "JOIN ur.role r " +
       "WHERE u.region = :region AND r.roleName IN :roleNames")
List<User> findByRegionAndRoleNames(
    @Param("region") Region region, 
    @Param("roleNames") List<String> roleNames
);
boolean existsByUser_Region_RegionIdAndRole_RoleName(Integer regionId, String roleName);}
