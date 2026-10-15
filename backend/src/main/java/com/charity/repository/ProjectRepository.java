package com.charity.repository;

import com.charity.entity.CharityProject;
import com.charity.entity.ProjectStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<CharityProject, Long> {
    List<CharityProject> findAllByOrderByCreatedAtDescIdDesc();
    List<CharityProject> findByStatusInOrderByCreatedAtDescIdDesc(Collection<ProjectStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from CharityProject p where p.id = :id")
    Optional<CharityProject> findForUpdateById(@Param("id") Long id);
}
