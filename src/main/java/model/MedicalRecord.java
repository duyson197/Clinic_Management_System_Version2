/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.sql.Timestamp;
import java.sql.Time;
import java.util.Date;
import java.util.List;

public class MedicalRecord {

    private long appointmentId;
    private long patientId;
    private String patientName;
    private Date appointmentDate;
    private Time appointmentTime;
    private String doctorName;
    private String symptoms;
    private String diagnosis;
    private String notes;
    private String history;
    private String historyAllergies;
    private String historyChronic;
    private String historyFamily;
    private String historySocial;
    private String historyVaccination;
    private String doctorNote;
    private String treatmentPlan;
    private int prescriptionId;
    private String prescriptionNote;
    private List<PrescriptionItem> prescriptionItems;
    private Timestamp updatedAt;

    public long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public long getPatientId() {
        return patientId;
    }

    public void setPatientId(long patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
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

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }
    
    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
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

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }

    public String getHistoryAllergies() {
        return historyAllergies;
    }

    public void setHistoryAllergies(String historyAllergies) {
        this.historyAllergies = historyAllergies;
    }

    public String getHistoryChronic() {
        return historyChronic;
    }

    public void setHistoryChronic(String historyChronic) {
        this.historyChronic = historyChronic;
    }

    public String getHistoryFamily() {
        return historyFamily;
    }

    public void setHistoryFamily(String historyFamily) {
        this.historyFamily = historyFamily;
    }

    public String getHistorySocial() {
        return historySocial;
    }

    public void setHistorySocial(String historySocial) {
        this.historySocial = historySocial;
    }

    public String getHistoryVaccination() {
        return historyVaccination;
    }

    public void setHistoryVaccination(String historyVaccination) {
        this.historyVaccination = historyVaccination;
    }

    public String getDoctorNote() {
        return doctorNote;
    }

    public void setDoctorNote(String doctorNote) {
        this.doctorNote = doctorNote;
    }

    public String getTreatmentPlan() {
        return treatmentPlan;
    }

    public void setTreatmentPlan(String treatmentPlan) {
        this.treatmentPlan = treatmentPlan;
    }
    
    public int getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(int prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public String getPrescriptionNote() {
        return prescriptionNote;
    }

    public void setPrescriptionNote(String prescriptionNote) {
        this.prescriptionNote = prescriptionNote;
    }

    public List<PrescriptionItem> getPrescriptionItems() {
        return prescriptionItems;
    }

    public void setPrescriptionItems(List<PrescriptionItem> prescriptionItems) {
        this.prescriptionItems = prescriptionItems;
    }
    
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
