package com.recruitcrm.rts.dto;

import com.recruitcrm.rts.entity.Job;
import com.recruitcrm.rts.entity.JobStatus;

import java.time.LocalDateTime;

public record JobResponse(Long id, String title, String companyName, String location,
                          String requiredSkills, String description,
                          JobStatus status, LocalDateTime postedAt) {

    public static JobResponse from(Job job) {
        return new JobResponse(job.getId(), job.getTitle(), job.getCompanyName(), job.getLocation(),
                job.getRequiredSkills(), job.getDescription(), job.getStatus(), job.getPostedAt());
    }
}
