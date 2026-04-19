package com.javatechie.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class MessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String recipientEmail;
    private String senderName;
    private String comment;
    private boolean delivered = false;
    private LocalDateTime timestamp = LocalDateTime.now();

    public MessageEntity() {}

    public MessageEntity(String recipientEmail, String senderName, String comment) {
        this.recipientEmail = recipientEmail;
        this.senderName = senderName;
        this.comment = comment;
    }

}
