/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author Admin
 */
public class ReviewAnswer {
     private int id;
  
    private int questionid;
    private int rating;
    private int userid;
    private int doctorid;
    private String note;
    private int appointmentid;
    public ReviewAnswer() {
    }

    public ReviewAnswer( int questionid, int rating, int userid, int doctorid) {
        
       
        this.questionid = questionid;
        this.rating = rating;
        this.userid = userid;
        this.doctorid = doctorid;
    }

    public ReviewAnswer(int id, int questionid, int rating, int userid, int doctorid, String note, int appointmentid) {
        this.id = id;
        this.questionid = questionid;
        this.rating = rating;
        this.userid = userid;
        this.doctorid = doctorid;
        this.note = note;
        this.appointmentid = appointmentid;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getAppointmentid() {
        return appointmentid;
    }

    public void setAppointmentid(int appointmentid) {
        this.appointmentid = appointmentid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    

    public int getQuestionid() {
        return questionid;
    }

    public void setQuestionid(int questionid) {
        this.questionid = questionid;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public int getDoctorid() {
        return doctorid;
    }

    public void setDoctorid(int doctorid) {
        this.doctorid = doctorid;
    }

    
}
