//package com.javatechie.service;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.javatechie.entity.Message;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Service;
//
//@Service
//public class KafkaMessageConsumer {
//
//    private final SimpMessagingTemplate messagingTemplate;
//    private final MemberStore memberStore;
//    private final ObjectMapper objectMapper;
//
//    public KafkaMessageConsumer(SimpMessagingTemplate messagingTemplate, MemberStore memberStore, ObjectMapper objectMapper) {
//        this.messagingTemplate = messagingTemplate;
//        this.memberStore = memberStore;
//        this.objectMapper = objectMapper;
//    }
//
//    @KafkaListener(topics = "chat-messages", groupId = "chat-group")
//    public void consume(String messageJson) throws JsonProcessingException {
//        Message message = objectMapper.readValue(messageJson, Message.class);
//        String targetEmail = message.getReceiverEmail();
//        if (targetEmail == null) return;
//
//        if (memberStore.isOnline(targetEmail)) {
//            messagingTemplate.convertAndSend("/topic/messages/" + targetEmail, message);
//        } else {
//            memberStore.storeOfflineMessage(targetEmail, message);
//        }
//    }
//}
