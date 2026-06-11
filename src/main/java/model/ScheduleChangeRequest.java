/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.sql.Timestamp;
import java.time.LocalTime;
import java.sql.Date;

/**
 *
 * @author anngu
 */
public class ScheduleChangeRequest {

    private int requestId;
    private int doctorId;
    private String requestType;
    private String scopeType;
    private String reason;
    private String status;
    private Timestamp requestedAt;
    private String adminNote;

    private String actionType;
    private Integer targetShiftId;
    private Date workDate;
    private Integer dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer maxPatients;
    private String doctorName;
    private String oldDoctorName;
    private Integer oldDayOfWeek;
    private LocalTime oldStartTime;
    private LocalTime oldEndTime;
    private String newDoctorName;
    private Date oldWorkDate;
    private Integer counterpartShiftId;
    private Integer counterpartDoctorId;

    public ScheduleChangeRequest() {
    }

    public ScheduleChangeRequest(int requestId, int doctorId, String requestType, String scopeType, String reason, String status, Timestamp requestedAt, String adminNote, String actionType, Integer targetShiftId, Date workDate, Integer dayOfWeek, LocalTime startTime, LocalTime endTime, Integer maxPatients) {
        this.requestId = requestId;
        this.doctorId = doctorId;
        this.requestType = requestType;
        this.scopeType = scopeType;
        this.reason = reason;
        this.status = status;
        this.requestedAt = requestedAt;
        this.adminNote = adminNote;
        this.actionType = actionType;
        this.targetShiftId = targetShiftId;
        this.workDate = workDate;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxPatients = maxPatients;
    }

    
    
    
    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getScopeType() {
        return scopeType;
    }

    public void setScopeType(String scopeType) {
        this.scopeType = scopeType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(Timestamp requestedAt) {
        this.requestedAt = requestedAt;
    }

    public String getAdminNote() {
        return adminNote;
    }

    public void setAdminNote(String adminNote) {
        this.adminNote = adminNote;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public Integer getTargetShiftId() {
        return targetShiftId;
    }

    public void setTargetShiftId(Integer targetShiftId) {
        this.targetShiftId = targetShiftId;
    }

    public Date getWorkDate() {
        return workDate;
    }

    public void setWorkDate(Date workDate) {
        this.workDate = workDate;
    }

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Integer getMaxPatients() {
        return maxPatients;
    }

    public void setMaxPatients(Integer maxPatients) {
        this.maxPatients = maxPatients;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getOldDoctorName() {
        return oldDoctorName;
    }

    public void setOldDoctorName(String oldDoctorName) {
        this.oldDoctorName = oldDoctorName;
    }

    public Integer getOldDayOfWeek() {
        return oldDayOfWeek;
    }

    public void setOldDayOfWeek(Integer oldDayOfWeek) {
        this.oldDayOfWeek = oldDayOfWeek;
    }

    public LocalTime getOldStartTime() {
        return oldStartTime;
    }

    public void setOldStartTime(LocalTime oldStartTime) {
        this.oldStartTime = oldStartTime;
    }

    public LocalTime getOldEndTime() {
        return oldEndTime;
    }

    public void setOldEndTime(LocalTime oldEndTime) {
        this.oldEndTime = oldEndTime;
    }

    public String getNewDoctorName() {
        return newDoctorName;
    }

    public void setNewDoctorName(String newDoctorName) {
        this.newDoctorName = newDoctorName;
    }

    public Date getOldWorkDate() {
        return oldWorkDate;
    }

    public void setOldWorkDate(Date oldWorkDate) {
        this.oldWorkDate = oldWorkDate;
    }

    public Integer getCounterpartShiftId() {
        return counterpartShiftId;
    }

    public void setCounterpartShiftId(Integer counterpartShiftId) {
        this.counterpartShiftId = counterpartShiftId;
    }

    public Integer getCounterpartDoctorId() {
        return counterpartDoctorId;
    }

    public void setCounterpartDoctorId(Integer counterpartDoctorId) {
        this.counterpartDoctorId = counterpartDoctorId;
    }
}
