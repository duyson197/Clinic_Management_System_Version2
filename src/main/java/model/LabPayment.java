package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Model for Lab Payment - uses existing payments table
 */
public class LabPayment {
    private long paymentId;
    private long appointmentId;
    private BigDecimal amount;
    private String method; // cash, online
    private String status; // pending, paid, failed
    private String transactionId;
    private Timestamp createdAt;

    // Joined data
    private LabRequest labRequest;

    public LabPayment() {
    }

    public long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(long paymentId) {
        this.paymentId = paymentId;
    }

    public long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public LabRequest getLabRequest() {
        return labRequest;
    }

    public void setLabRequest(LabRequest labRequest) {
        this.labRequest = labRequest;
    }
}
