package com.recruitcrm.rts.service;

import com.recruitcrm.rts.dto.JobRequest;
import com.recruitcrm.rts.dto.JobResponse;
import com.recruitcrm.rts.entity.Job;
import com.recruitcrm.rts.entity.JobStatus;
import com.recruitcrm.rts.entity.User;
import com.recruitcrm.rts.exception.ForbiddenException;
import com.recruitcrm.rts.exception.ResourceNotFoundException;
import com.recruitcrm.rts.repository.JobApplicationRepository;
import com.recruitcrm.rts.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** CRUD for jobs. A recruiter can only change jobs of his own company. */
@Service
public class JobService {

    private final JobRepository jobRepository;
    private final JobApplicationRepository applicationRepository;

    public JobService(JobRepository jobRepository, JobApplicationRepository applicationRepository) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
    }

    // ---------- READ ----------

    /** Public list: only OPEN jobs. */
    public List<JobResponse> getOpenJobs() {
        return jobRepository.findByStatusOrderByPostedAtDesc(JobStatus.OPEN)
                .stream().map(JobResponse::from).toList();
    }

    /** Recruiter's own company jobs (OPEN and CLOSED). */
    public List<JobResponse> getJobsOfCompany(User recruiter) {
        return jobRepository.findByCompanyNameIgnoreCaseOrderByPostedAtDesc(recruiter.getCompanyName())
                .stream().map(JobResponse::from).toList();
    }

    public JobResponse getJob(Long id) {
        return JobResponse.from(findJob(id));
    }

    // ---------- CREATE ----------

    @Transactional
    public JobResponse createJob(JobRequest request, User recruiter) {
        Job job = new Job();
        job.setCompanyName(recruiter.getCompanyName());   // company always comes from the logged-in recruiter
        copyFields(job, request);
        return JobResponse.from(jobRepository.save(job));
    }

    // ---------- UPDATE ----------

    @Transactional
    public JobResponse updateJob(Long id, JobRequest request, User recruiter) {
        Job job = findOwnedJob(id, recruiter);
        copyFields(job, request);
        return JobResponse.from(jobRepository.save(job));
    }

    // ---------- DELETE ----------

    @Transactional
    public void deleteJob(Long id, User recruiter) {
        Job job = findOwnedJob(id, recruiter);
        applicationRepository.deleteByJobId(job.getId());   // remove its applications first
        jobRepository.delete(job);
    }

    // ---------- helpers ----------

    private void copyFields(Job job, JobRequest request) {
        job.setTitle(request.title().trim());
        job.setLocation(request.location());
        job.setRequiredSkills(request.requiredSkills().trim());
        job.setDescription(request.description());
        if (request.status() != null) {
            job.setStatus(request.status());
        }
    }

    private Job findJob(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id " + id));
    }

    private Job findOwnedJob(Long id, User recruiter) {
        Job job = findJob(id);
        if (!recruiter.worksFor(job.getCompanyName())) {
            throw new ForbiddenException("This job belongs to another company.");
        }
        return job;
    }
}
