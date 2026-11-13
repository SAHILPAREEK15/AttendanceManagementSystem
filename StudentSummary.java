package com.attendance.model;

public record StudentSummary(String studentId, String name, String rollNo, String branch,
                             int semester, int present, int total) {

    public static final double LOW_LIMIT = 75.0;

    public static double percent(int present, int total) {
        return total == 0 ? 0 : present * 100.0 / total;
    }

    public boolean hasClasses() {
        return total > 0;
    }

    public double percentage() {
        return percent(present, total);
    }

    // Students with no classes yet are never flagged as low
    public boolean isLow() {
        return hasClasses() && percentage() < LOW_LIMIT;
    }
}
