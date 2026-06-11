package model;

import java.sql.Timestamp;

public class LabRequest {
    private int requestId;
    private long appointmentId;
    private int doctorId;
    private String status; // pending, processing, completed
    private Timestamp createdAt;
    
    // Joined data
    private Patient patient;
    private Doctor doctor;
    private Appointment appointment;
    private String testTypes; // Comma-separated test types
    private String testDetails; // Comma-separated test details
    private String priority; // high, normal
    private String notes;

    public LabRequest() {
    }

    public LabRequest(int requestId, long appointmentId, int doctorId, String status, Timestamp createdAt) {
        this.requestId = requestId;
        this.appointmentId = appointmentId;
        this.doctorId = doctorId;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public String getTestTypes() {
        return testTypes;
    }

    public void setTestTypes(String testTypes) {
        this.testTypes = testTypes;
    }

    public String getTestDetails() {
        return testDetails;
    }

    public void setTestDetails(String testDetails) {
        this.testDetails = testDetails;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

