package com.recruitcrm.rts.entity;

/**
 * Hiring pipeline:
 *
 *   APPLIED -> TEST -> INTERVIEW -> SELECTED
 *                         (REJECTED is possible from any stage before SELECTED)
 */
public enum ApplicationStatus {
    APPLIED,
    TEST,
    INTERVIEW,
    SELECTED,
    REJECTED;

    public boolean isFinal() {
        return this == SELECTED || this == REJECTED;
    }
}
