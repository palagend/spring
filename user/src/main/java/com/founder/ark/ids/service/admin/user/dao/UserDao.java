package com.founder.ark.ids.service.admin.user.dao;

import com.founder.ark.ids.service.core.bean.keycloak.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserDao extends JpaRepository<User, String> {
    @Query(value = "SELECT u FROM User u WHERE company = :company AND (username LIKE %:searchString% OR email LIKE %:searchString% OR mobilePhone LIKE %:searchString% OR firstName LIKE %:searchString%)")
    Page<User> searchUsers(@Param("searchString") String searchString, @Param("company") String company, Pageable pageable);

    @Query(value = "SELECT u FROM User u WHERE company = :company AND (username LIKE %:searchString% OR firstName LIKE %:searchString%)")
    List<User> searchUsersByUsernameOrFirstName(@Param("searchString") String searchString, @Param("company") String company);

    User findByMobilePhone(String mobilePhone);

    User findUserByEmail(String email);

    User findByUsername(String username);

    User findById(String id);

    @Query(value = "SELECT u FROM User u WHERE company = :company")
    Page<User> findAllInTheSameCompany(@Param("company") String company, Pageable pageable);
}
