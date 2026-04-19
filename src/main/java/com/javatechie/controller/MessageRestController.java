//package com.javatechie.controller;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.javatechie.entity.Message;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/messages")
//public class MessageRestController {
//
//    private final KafkaTemplate<String,String> kafkaTemplate;
//
//    private final ObjectMapper objectMapper;
//
//    public MessageRestController(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
//        this.kafkaTemplate = kafkaTemplate;
//        this.objectMapper = objectMapper;
//    }
//
//    @PostMapping
//    public String sendMessage(@RequestBody Message message) throws JsonProcessingException {
//        String messageJson = objectMapper.writeValueAsString(message);
//        kafkaTemplate.send("chat-messages", message.getReceiverEmail(), messageJson);
//        return "✅ Message sent to Kafka for " + message.getReceiverEmail();
//    }
//}
