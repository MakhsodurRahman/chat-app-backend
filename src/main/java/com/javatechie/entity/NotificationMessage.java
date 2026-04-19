package com.javatechie.entity;

import java.time.Instant;

public class NotificationMessage {
    private String recipientEmail;
    private String senderName;
    private String comment;
    private Instant timestamp;

    public NotificationMessage() {}

    public NotificationMessage(String recipientEmail, String senderName, String comment, Instant timestamp) {
        this.recipientEmail = recipientEmail;
        this.senderName = senderName;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
