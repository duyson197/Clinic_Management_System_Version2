package model;

public class DoctorProductivity {

    private int doctorId;
    private String doctorName;
    private int totalCompletedAppointments;

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public int getTotalCompletedAppointments() {
        return totalCompletedAppointments;
    }

    public void setTotalCompletedAppointments(int totalCompletedAppointments) {
        this.totalCompletedAppointments = totalCompletedAppointments;
    }
}

