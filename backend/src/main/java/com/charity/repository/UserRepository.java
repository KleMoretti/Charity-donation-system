package com.charity.repository;

import com.charity.entity.SysUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<SysUser, Long> {
    Optional<SysUser> findByUsername(String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from SysUser u where u.username = :username")
    Optional<SysUser> findForUpdateByUsername(@Param("username") String username);
}
