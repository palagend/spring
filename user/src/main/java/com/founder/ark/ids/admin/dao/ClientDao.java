package com.founder.ark.ids.admin.dao;

import com.founder.ark.ids.bean.keycloak.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Created by cheng.ly on 2018/3/13.
 */
public interface ClientDao extends JpaRepository<Client,String> {

    @Query(value = "SELECT c FROM Client c WHERE appName LIKE %:appName%")
    Page<Client> searchClientByName(@Param("appName") String appName, Pageable pageable);

    Client findByAppName(String appName);
}
