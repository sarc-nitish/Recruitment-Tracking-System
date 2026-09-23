package com.recruitcrm.rts.controller;

import com.recruitcrm.rts.dto.ApplicationResponse;
import com.recruitcrm.rts.dto.ApplyRequest;
import com.recruitcrm.rts.dto.NotesRequest;
import com.recruitcrm.rts.dto.RejectRequest;
import com.recruitcrm.rts.dto.ScheduleInterviewRequest;
import com.recruitcrm.rts.dto.SendTestRequest;
import com.recruitcrm.rts.entity.ApplicationStatus;
import com.recruitcrm.rts.entity.User;
import com.recruitcrm.rts.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    // ================= CANDIDATE =================

    /** POST /api/jobs/{jobId}/apply */
    @PostMapping("/jobs/{jobId}/apply")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApplicationResponse apply(@PathVariable Long jobId,
                                     @Valid @RequestBody ApplyRequest request,
                                     @AuthenticationPrincipal User candidate) {
        return applicationService.apply(jobId, request, candidate);
    }

    /** GET /api/applications/my */
    @GetMapping("/applications/my")
    @PreAuthorize("hasRole('CANDIDATE')")
    public List<ApplicationResponse> myApplications(@AuthenticationPrincipal User candidate) {
        return applicationService.getMyApplications(candidate);
    }

    /** PUT /api/applications/{id}/complete-test  - "Mark as Completed" button */
    @PutMapping("/applications/{id}/complete-test")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApplicationResponse completeTest(@PathVariable Long id, @AuthenticationPrincipal User candidate) {
        return applicationService.completeTest(id, candidate);
    }

    // ================= RECRUITER =================

    /** GET /api/applications?jobId=1&status=TEST   (both filters optional) */
    @GetMapping("/applications")
    @PreAuthorize("hasRole('RECRUITER')")
    public List<ApplicationResponse> applications(@RequestParam(required = false) Long jobId,
                                                  @RequestParam(required = false) ApplicationStatus status,
                                                  @AuthenticationPrincipal User recruiter) {
        return applicationService.getApplicationsForRecruiter(recruiter, jobId, status);
    }

    /** PUT /api/applications/{id}/send-test        APPLIED -> TEST */
    @PutMapping("/applications/{id}/send-test")
    @PreAuthorize("hasRole('RECRUITER')")
    public ApplicationResponse sendTest(@PathVariable Long id,
                                        @Valid @RequestBody SendTestRequest request,
                                        @AuthenticationPrincipal User recruiter) {
        return applicationService.sendTest(id, request, recruiter);
    }

    /** PUT /api/applications/{id}/schedule-interview   TEST -> INTERVIEW (only after the candidate completed the test) */
    @PutMapping("/applications/{id}/schedule-interview")
    @PreAuthorize("hasRole('RECRUITER')")
    public ApplicationResponse scheduleInterview(@PathVariable Long id,
                                                 @Valid @RequestBody ScheduleInterviewRequest request,
                                                 @AuthenticationPrincipal User recruiter) {
        return applicationService.scheduleInterview(id, request, recruiter);
    }

    /** PUT /api/applications/{id}/select          INTERVIEW -> SELECTED  (body with notes is optional) */
    @PutMapping("/applications/{id}/select")
    @PreAuthorize("hasRole('RECRUITER')")
    public ApplicationResponse select(@PathVariable Long id,
                                      @Valid @RequestBody(required = false) NotesRequest request,
                                      @AuthenticationPrincipal User recruiter) {
        String notes = request == null ? null : request.notes();
        return applicationService.select(id, notes, recruiter);
    }

    /** PUT /api/applications/{id}/reject          any stage -> REJECTED */
    @PutMapping("/applications/{id}/reject")
    @PreAuthorize("hasRole('RECRUITER')")
    public ApplicationResponse reject(@PathVariable Long id,
                                      @Valid @RequestBody RejectRequest request,
                                      @AuthenticationPrincipal User recruiter) {
        return applicationService.reject(id, request, recruiter);
    }

    // ================= BOTH =================

    /** GET /api/applications/{id}  - the candidate who owns it, or a recruiter of the same company */
    @GetMapping("/applications/{id}")
    public ApplicationResponse getApplication(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return applicationService.getApplication(id, user);
    }
}
