/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author Admin
 */
public class Rating_note {
    private int appointment_id;
   private String userName;
   private String note;
   private int user_id;

    public Rating_note() {
    }
   
    public Rating_note(String userName, String note) {
        this.userName = userName;
        this.note = note;
    }

    public Rating_note(int appointment_id, String userName, String note) {
        this.appointment_id = appointment_id;
        this.userName = userName;
        this.note = note;
    }

    public Rating_note(int appointment_id, String userName, String note, int user_id) {
        this.appointment_id = appointment_id;
        this.userName = userName;
        this.note = note;
        this.user_id = user_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }
    

    public int getAppointment_id() {
        return appointment_id;
    }

    public void setAppointment_id(int appointment_id) {
        this.appointment_id = appointment_id;
    }

   
    
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
   
}
