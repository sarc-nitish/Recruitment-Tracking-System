package com.recruitcrm.rts.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

/** One candidate's application to one job. A candidate can apply to a job only once. */
@Entity
@Table(name = "applications",
        uniqueConstraints = @UniqueConstraint(columnNames = {"candidate_id", "job_id"}))
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    // what the candidate submitted for THIS job
    @Column(length = 1000)
    private String skills;

    @Column(length = 1000)
    private String coverNote;

    // % of the job's required skills that the candidate has (0-100)
    private Double matchScore;

    // ----- TEST stage -----
    @Column(length = 1000)
    private String testLink;
    private LocalDateTime testDeadline;
    // set when the candidate marks the test as completed; null = not done yet
    private LocalDateTime testCompletedAt;
    private Double testScore;

    // ----- INTERVIEW stage -----
    @Column(length = 1000)
    private String interviewLink;
    private LocalDateTime interviewAt;
    @Column(length = 2000)
    private String interviewNotes;

    // ----- REJECTED -----
    @Column(length = 1000)
    private String rejectionReason;
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ApplicationStatus rejectedAtStage;

    private LocalDateTime appliedAt;

    public JobApplication() {
    }

    public JobApplication(User candidate, Job job) {
        this.candidate = candidate;
        this.job = job;
    }

    @PrePersist
    protected void onCreate() {
        this.appliedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getCandidate() { return candidate; }
    public void setCandidate(User candidate) { this.candidate = candidate; }

    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public String getCoverNote() { return coverNote; }
    public void setCoverNote(String coverNote) { this.coverNote = coverNote; }

    public Double getMatchScore() { return matchScore; }
    public void setMatchScore(Double matchScore) { this.matchScore = matchScore; }

    public String getTestLink() { return testLink; }
    public void setTestLink(String testLink) { this.testLink = testLink; }

    public LocalDateTime getTestDeadline() { return testDeadline; }
    public void setTestDeadline(LocalDateTime testDeadline) { this.testDeadline = testDeadline; }

    public LocalDateTime getTestCompletedAt() { return testCompletedAt; }
    public void setTestCompletedAt(LocalDateTime testCompletedAt) { this.testCompletedAt = testCompletedAt; }

    public Double getTestScore() { return testScore; }
    public void setTestScore(Double testScore) { this.testScore = testScore; }

    public String getInterviewLink() { return interviewLink; }
    public void setInterviewLink(String interviewLink) { this.interviewLink = interviewLink; }

    public LocalDateTime getInterviewAt() { return interviewAt; }
    public void setInterviewAt(LocalDateTime interviewAt) { this.interviewAt = interviewAt; }

    public String getInterviewNotes() { return interviewNotes; }
    public void setInterviewNotes(String interviewNotes) { this.interviewNotes = interviewNotes; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public ApplicationStatus getRejectedAtStage() { return rejectedAtStage; }
    public void setRejectedAtStage(ApplicationStatus rejectedAtStage) { this.rejectedAtStage = rejectedAtStage; }

    public LocalDateTime getAppliedAt() { return appliedAt; }
}
