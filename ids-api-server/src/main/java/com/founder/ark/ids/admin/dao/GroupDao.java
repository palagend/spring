package com.founder.ark.ids.admin.dao;

import com.founder.ark.ids.bean.keycloak.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupDao extends JpaRepository<Group, String> {
    @Query(value = "SELECT g FROM Group g WHERE (groupName LIKE %:searchString% OR groupDescription LIKE %:searchString%)")
    Page<Group> searchGroups(@Param("searchString") String searchString, Pageable pageable);
    Group findByGroupName(String groupName);
}
