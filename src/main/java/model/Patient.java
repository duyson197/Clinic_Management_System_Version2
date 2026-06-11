package model;

import java.sql.Date;

public class Patient {
    private long patientId;     
    private Integer userId;     
    private String fullName;    
    private String phone;       
    private Date dob;           
    private String email;       
    private String gender;      
   

    public Patient() {
    }

    
    public Patient(Integer userId, String fullName, String phone, Date dob, String email, String gender) {
        this.userId = userId;
        this.fullName = fullName;
        this.phone = phone;
        this.dob = dob;
        this.email = email;
        this.gender = gender;
    }

    public long getPatientId() {
        return patientId;
    }

    public void setPatientId(long patientId) {
        this.patientId = patientId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

   

    
}

