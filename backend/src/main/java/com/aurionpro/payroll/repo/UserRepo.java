package com.aurionpro.payroll.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aurionpro.payroll.entity.User;
import com.aurionpro.payroll.enums.Role;
import com.aurionpro.payroll.enums.UserStatus;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

	// Check if email exists
    boolean existsByEmail(String email);
    
    //  Find user by email
    Optional<User> findByEmail(String email);
    
    // Find users by role
    List<User> findByRole(Role role);
    
    // Find users by status
    List<User> findByStatus(UserStatus status);
    
    // Find active users by role
    List<User> findByRoleAndStatus(Role role, UserStatus status);
    
    // Custom query to find user with organization
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.organization WHERE u.email = :email")
    Optional<User> findByEmailWithOrganization(@Param("email") String email);
    
    // Custom query to find user with employee
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.employee WHERE u.email = :email")
    Optional<User> findByEmailWithEmployee(@Param("email") String email);

}
