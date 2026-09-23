package com.recruitcrm.rts.dto;

import com.recruitcrm.rts.entity.ApplicationStatus;
import com.recruitcrm.rts.entity.JobApplication;

import java.time.LocalDateTime;

public record ApplicationResponse(
        Long id,
        Long jobId,
        String jobTitle,
        String companyName,
        String candidateName,
        String candidateEmail,
        String skills,
        String coverNote,
        Double matchScore,
        ApplicationStatus status,
        String testLink,
        LocalDateTime testDeadline,
        LocalDateTime testCompletedAt,
        Double testScore,
        String interviewLink,
        LocalDateTime interviewAt,
        String interviewNotes,
        String rejectionReason,
        ApplicationStatus rejectedAtStage,
        LocalDateTime appliedAt
) {

    public static ApplicationResponse from(JobApplication a) {
        return new ApplicationResponse(
                a.getId(),
                a.getJob().getId(),
                a.getJob().getTitle(),
                a.getJob().getCompanyName(),
                a.getCandidate().getName(),
                a.getCandidate().getEmail(),
                a.getSkills(),
                a.getCoverNote(),
                a.getMatchScore(),
                a.getStatus(),
                a.getTestLink(),
                a.getTestDeadline(),
                a.getTestCompletedAt(),
                a.getTestScore(),
                a.getInterviewLink(),
                a.getInterviewAt(),
                a.getInterviewNotes(),
                a.getRejectionReason(),
                a.getRejectedAtStage(),
                a.getAppliedAt());
    }
}
