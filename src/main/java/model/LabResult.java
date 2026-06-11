package model;

import java.sql.Timestamp;

public class LabResult {
    private int resultId;
    private int requestId;
    private Integer technicianId;
    private String resultFile;
    private String notes;
    private Timestamp completedAt;

    public LabResult() {
    }

    public LabResult(int resultId, int requestId, Integer technicianId, String resultFile, String notes, Timestamp completedAt) {
        this.resultId = resultId;
        this.requestId = requestId;
        this.technicianId = technicianId;
        this.resultFile = resultFile;
        this.notes = notes;
        this.completedAt = completedAt;
    }

    public int getResultId() {
        return resultId;
    }

    public void setResultId(int resultId) {
        this.resultId = resultId;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public Integer getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(Integer technicianId) {
        this.technicianId = technicianId;
    }

    public String getResultFile() {
        return resultFile;
    }

    public void setResultFile(String resultFile) {
        this.resultFile = resultFile;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Timestamp getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Timestamp completedAt) {
        this.completedAt = completedAt;
    }
}

