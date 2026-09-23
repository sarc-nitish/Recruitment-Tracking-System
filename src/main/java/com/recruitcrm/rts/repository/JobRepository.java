package com.recruitcrm.rts.repository;

import com.recruitcrm.rts.entity.Job;
import com.recruitcrm.rts.entity.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByStatusOrderByPostedAtDesc(JobStatus status);

    List<Job> findByCompanyNameIgnoreCaseOrderByPostedAtDesc(String companyName);
}
