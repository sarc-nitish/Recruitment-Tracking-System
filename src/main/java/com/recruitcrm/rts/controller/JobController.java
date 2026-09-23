package com.recruitcrm.rts.controller;

import com.recruitcrm.rts.dto.JobRequest;
import com.recruitcrm.rts.dto.JobResponse;
import com.recruitcrm.rts.entity.User;
import com.recruitcrm.rts.service.JobService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Job CRUD.  Read = public, Create/Update/Delete = RECRUITER of the same company. */
@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    /** GET /api/jobs  - all OPEN jobs (public) */
    @GetMapping
    public List<JobResponse> getOpenJobs() {
        return jobService.getOpenJobs();
    }

    /** GET /api/jobs/my  - all jobs of the recruiter's company */
    @GetMapping("/my")
    @PreAuthorize("hasRole('RECRUITER')")
    public List<JobResponse> getMyCompanyJobs(@AuthenticationPrincipal User recruiter) {
        return jobService.getJobsOfCompany(recruiter);
    }

    /** GET /api/jobs/{id}  (public) */
    @GetMapping("/{id}")
    public JobResponse getJob(@PathVariable Long id) {
        return jobService.getJob(id);
    }

    /** POST /api/jobs */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('RECRUITER')")
    public JobResponse createJob(@Valid @RequestBody JobRequest request,
                                 @AuthenticationPrincipal User recruiter) {
        return jobService.createJob(request, recruiter);
    }

    /** PUT /api/jobs/{id} */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    public JobResponse updateJob(@PathVariable Long id,
                                 @Valid @RequestBody JobRequest request,
                                 @AuthenticationPrincipal User recruiter) {
        return jobService.updateJob(id, request, recruiter);
    }

    /** DELETE /api/jobs/{id} */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('RECRUITER')")
    public void deleteJob(@PathVariable Long id, @AuthenticationPrincipal User recruiter) {
        jobService.deleteJob(id, recruiter);
    }
}
