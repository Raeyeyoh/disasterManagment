package com.dms.disastermanagmentapi.Repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dms.disastermanagmentapi.entities.Region;
import com.dms.disastermanagmentapi.entities.Role;
import com.dms.disastermanagmentapi.entities.User;
import com.dms.disastermanagmentapi.enums.UserStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    
   Optional<User> findByUsername(String username);

  @Query("SELECT u FROM User u " +
       "JOIN u.userRoles ur " +
       "JOIN ur.role r " +
       "WHERE u.region = :region AND r.roleName IN :roleNames")
List<User> findByRegionAndRoleNames(
    @Param("region") Region region, 
    @Param("roleNames") List<String> roleNames
);

  List<User> findByStatus(UserStatus pending);
  @Query("SELECT u FROM User u LEFT JOIN FETCH u.userRoles ur LEFT JOIN FETCH ur.role WHERE u.status = :status")
List<User> findByStatusWithRoles(@Param("status") UserStatus status);

@Query("SELECT COUNT(u) FROM User u JOIN u.userRoles ur JOIN ur.role r " +
       "WHERE u.region.regionId = :regionId " +
       "AND (r.roleName = 'ROLE_REGIONAL_ADMIN' OR r.roleName = 'REGIONAL_ADMIN')")
long countRegionalAdmins(@Param("regionId") Integer regionId);

@Query("""
  SELECT u FROM User u 
  JOIN u.userRoles ur
  WHERE u.status = :status 
    AND ur.role IN :roles 
    AND u.region = :region
""")
List<User> findPendingByRolesAndRegion(
    @Param("status") UserStatus status,
    @Param("roles") List<Role> roles,
    @Param("region") Region region
);
@Query("SELECT u FROM User u JOIN u.userRoles ur WHERE u.status = :status AND ur.role.roleName IN :roles")
List<User> findPendingByRoles(@Param("status") UserStatus status, @Param("roles") List<String> roles);

}