package com.recruitcrm.rts.repository;

import com.recruitcrm.rts.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    List<JobApplication> findByCandidateIdOrderByAppliedAtDesc(Long candidateId);

    // all applications for jobs posted under one company
    List<JobApplication> findByJob_CompanyNameIgnoreCaseOrderByAppliedAtDesc(String companyName);

    boolean existsByCandidateIdAndJobId(Long candidateId, Long jobId);

    void deleteByJobId(Long jobId);
}
