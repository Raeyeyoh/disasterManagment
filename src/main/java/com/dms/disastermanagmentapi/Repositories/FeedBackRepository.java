package com.dms.disastermanagmentapi.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dms.disastermanagmentapi.entities.FeedBack;

@Repository
public interface FeedBackRepository extends JpaRepository<FeedBack, Integer> {
    List<FeedBack> findByReport_ReportIdOrderByCreatedAtDesc(Integer reportId);
    List<FeedBack> findByRegion_RegionIdOrderByCreatedAtDesc(Integer regionId);
    List<FeedBack> findAllByOrderByCreatedAtDesc();
    Optional<FeedBack> findByReport_ReportId(Integer reportId);
}
