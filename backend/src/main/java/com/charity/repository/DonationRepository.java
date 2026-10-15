package com.charity.repository;

import com.charity.entity.Donation;
import java.util.Optional;
import java.util.List;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DonationRepository extends JpaRepository<Donation, Long> {
    Optional<Donation> findByUserIdAndRequestId(Long userId, String requestId);

    @EntityGraph(attributePaths = {"user", "project"})
    List<Donation> findByUserUsernameOrderByCreatedAtDescIdDesc(String username);

    @EntityGraph(attributePaths = {"user", "project"})
    List<Donation> findAllByOrderByCreatedAtDescIdDesc();

    @Query("select d.project.id as projectId, sum(d.amount) as raisedAmount, count(d) as donationCount "
            + "from Donation d where d.project.id in :ids group by d.project.id")
    List<ProjectDonationSummary> summarizeProjects(@Param("ids") Collection<Long> ids);

    @Query("select count(d) as donationCount, coalesce(sum(d.amount), 0) as totalAmount from Donation d")
    DonationSummary summarizeAll();
}
