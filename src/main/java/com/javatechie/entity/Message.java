package com.javatechie.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private String content;
    private String senderEmail;
    private String senderName;
    private String receiverEmail;
    private java.time.Instant timestamp = java.time.Instant.now();
    private boolean read;
}

