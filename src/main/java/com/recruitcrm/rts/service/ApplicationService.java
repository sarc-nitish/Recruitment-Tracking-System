package com.recruitcrm.rts.service;

import com.recruitcrm.rts.dto.ApplicationResponse;
import com.recruitcrm.rts.dto.ApplyRequest;
import com.recruitcrm.rts.dto.RejectRequest;
import com.recruitcrm.rts.dto.ScheduleInterviewRequest;
import com.recruitcrm.rts.dto.SendTestRequest;
import com.recruitcrm.rts.entity.ApplicationStatus;
import com.recruitcrm.rts.entity.Job;
import com.recruitcrm.rts.entity.JobApplication;
import com.recruitcrm.rts.entity.JobStatus;
import com.recruitcrm.rts.entity.Role;
import com.recruitcrm.rts.entity.User;
import com.recruitcrm.rts.exception.BadRequestException;
import com.recruitcrm.rts.exception.ConflictException;
import com.recruitcrm.rts.exception.ForbiddenException;
import com.recruitcrm.rts.exception.ResourceNotFoundException;
import com.recruitcrm.rts.repository.JobApplicationRepository;
import com.recruitcrm.rts.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Applying to a job, and moving an application through the pipeline:
 *
 *   APPLIED -> TEST -> INTERVIEW -> SELECTED      (REJECTED from any stage before SELECTED)
 *
 * Every step first checks the current status, so no stage can be skipped.
 */
@Service
public class ApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final JobRepository jobRepository;

    public ApplicationService(JobApplicationRepository applicationRepository, JobRepository jobRepository) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
    }

    // ---------- Candidate: apply + view own ----------

    @Transactional
    public ApplicationResponse apply(Long jobId, ApplyRequest request, User candidate) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id " + jobId));

        if (job.getStatus() != JobStatus.OPEN) {
            throw new BadRequestException("This job is closed. You cannot apply to it.");
        }
        if (applicationRepository.existsByCandidateIdAndJobId(candidate.getId(), jobId)) {
            throw new ConflictException("You have already applied to this job.");
        }

        JobApplication application = new JobApplication(candidate, job);
        application.setSkills(request.skills().trim());
        application.setCoverNote(request.coverNote());
        application.setMatchScore(calculateMatchScore(request.skills(), job.getRequiredSkills()));
        return ApplicationResponse.from(applicationRepository.save(application));
    }

    public List<ApplicationResponse> getMyApplications(User candidate) {
        return applicationRepository.findByCandidateIdOrderByAppliedAtDesc(candidate.getId())
                .stream().map(ApplicationResponse::from).toList();
    }

    // ---------- Recruiter: view (only own company) ----------

    /** jobId and status are optional filters. */
    public List<ApplicationResponse> getApplicationsForRecruiter(User recruiter, Long jobId, ApplicationStatus status) {
        return applicationRepository
                .findByJob_CompanyNameIgnoreCaseOrderByAppliedAtDesc(recruiter.getCompanyName())
                .stream()
                .filter(a -> jobId == null || a.getJob().getId().equals(jobId))
                .filter(a -> status == null || a.getStatus() == status)
                .map(ApplicationResponse::from)
                .toList();
    }

    /** A candidate can see only his own application; a recruiter only his company's. */
    public ApplicationResponse getApplication(Long id, User user) {
        JobApplication application = findApplication(id);
        if (user.getRole() == Role.CANDIDATE) {
            checkOwnedByCandidate(application, user);
        } else {
            checkBelongsToCompany(application, user);
        }
        return ApplicationResponse.from(application);
    }

    // ---------- Pipeline ----------

    /** APPLIED -> TEST: recruiter sends the online test link with a deadline. */
    @Transactional
    public ApplicationResponse sendTest(Long id, SendTestRequest request, User recruiter) {
        JobApplication application = findApplication(id);
        checkBelongsToCompany(application, recruiter);
        requireStatus(application, ApplicationStatus.APPLIED);

        application.setStatus(ApplicationStatus.TEST);
        application.setTestLink(request.testLink());
        application.setTestDeadline(request.deadline());
        application.setTestCompletedAt(null);
        return ApplicationResponse.from(applicationRepository.save(application));
    }

    /**
     * Candidate presses "Mark test as completed". The status stays TEST; only the time is stamped.
     * That tells the recruiter the test is finished and the candidate can be processed further.
     */
    @Transactional
    public ApplicationResponse completeTest(Long id, User candidate) {
        JobApplication application = findApplication(id);
        checkOwnedByCandidate(application, candidate);
        requireStatus(application, ApplicationStatus.TEST);
        if (application.getTestCompletedAt() != null) {
            throw new BadRequestException("You have already marked this test as completed.");
        }

        application.setTestCompletedAt(LocalDateTime.now());
        return ApplicationResponse.from(applicationRepository.save(application));
    }

    /** TEST -> INTERVIEW: only after the candidate has completed the test. */
    @Transactional
    public ApplicationResponse scheduleInterview(Long id, ScheduleInterviewRequest request, User recruiter) {
        JobApplication application = findApplication(id);
        checkBelongsToCompany(application, recruiter);
        requireStatus(application, ApplicationStatus.TEST);
        if (application.getTestCompletedAt() == null) {
            throw new BadRequestException("The candidate has not marked the test as completed yet.");
        }

        application.setTestScore(request.testScore());
        application.setInterviewLink(request.meetLink());
        application.setInterviewAt(request.interviewAt());
        application.setStatus(ApplicationStatus.INTERVIEW);
        return ApplicationResponse.from(applicationRepository.save(application));
    }

    /** INTERVIEW -> SELECTED: final decision, hired. */
    @Transactional
    public ApplicationResponse select(Long id, String notes, User recruiter) {
        JobApplication application = findApplication(id);
        checkBelongsToCompany(application, recruiter);
        requireStatus(application, ApplicationStatus.INTERVIEW);

        application.setInterviewNotes(notes);
        application.setStatus(ApplicationStatus.SELECTED);
        return ApplicationResponse.from(applicationRepository.save(application));
    }

    /** Any stage before the final decision -> REJECTED. Remembers the stage it happened at. */
    @Transactional
    public ApplicationResponse reject(Long id, RejectRequest request, User recruiter) {
        JobApplication application = findApplication(id);
        checkBelongsToCompany(application, recruiter);
        if (application.getStatus().isFinal()) {
            throw new BadRequestException("This application is already " + application.getStatus() + ".");
        }

        application.setRejectedAtStage(application.getStatus());
        application.setStatus(ApplicationStatus.REJECTED);
        application.setRejectionReason(request.reason());
        return ApplicationResponse.from(applicationRepository.save(application));
    }

    // ---------- helpers ----------

    private JobApplication findApplication(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id " + id));
    }

    private void checkOwnedByCandidate(JobApplication application, User candidate) {
        if (!application.getCandidate().getId().equals(candidate.getId())) {
            throw new ForbiddenException("This application does not belong to you.");
        }
    }

    private void checkBelongsToCompany(JobApplication application, User recruiter) {
        if (!recruiter.worksFor(application.getJob().getCompanyName())) {
            throw new ForbiddenException("This application belongs to another company.");
        }
    }

    private void requireStatus(JobApplication application, ApplicationStatus required) {
        if (application.getStatus() != required) {
            throw new BadRequestException("Action not allowed: application is in status "
                    + application.getStatus() + ", but this action needs status " + required + ".");
        }
    }

    /** Percentage of the job's required skills that the candidate has, e.g. 2 of 3 = 66.7 */
    private Double calculateMatchScore(String candidateSkills, String requiredSkills) {
        Set<String> required = splitSkills(requiredSkills);
        if (required.isEmpty()) {
            return 0.0;
        }
        Set<String> have = splitSkills(candidateSkills);
        long matched = required.stream().filter(have::contains).count();
        return Math.round(matched * 1000.0 / required.size()) / 10.0;
    }

    private Set<String> splitSkills(String skills) {
        if (skills == null || skills.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(skills.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
}
