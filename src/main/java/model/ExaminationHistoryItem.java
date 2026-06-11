package model;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class ExaminationHistoryItem {

    private long appointmentId;
    private Date appointmentDate;
    private Time appointmentTime;
    private String symptom;
    private String appointmentStatus;
    private String queueStatus;
    private String diagnosis;
    private String notes;
    private Timestamp recordUpdatedAt;

    public long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Date getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(Date appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public Time getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(Time appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getSymptom() {
        return symptom;
    }

    public void setSymptom(String symptom) {
        this.symptom = symptom;
    }

    public String getAppointmentStatus() {
        return appointmentStatus;
    }

    public void setAppointmentStatus(String appointmentStatus) {
        this.appointmentStatus = appointmentStatus;
    }

    public String getQueueStatus() {
        return queueStatus;
    }

    public void setQueueStatus(String queueStatus) {
        this.queueStatus = queueStatus;
    }
    
    
    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Timestamp getRecordUpdatedAt() {
        return recordUpdatedAt;
    }

    public void setRecordUpdatedAt(Timestamp recordUpdatedAt) {
        this.recordUpdatedAt = recordUpdatedAt;
    }
}