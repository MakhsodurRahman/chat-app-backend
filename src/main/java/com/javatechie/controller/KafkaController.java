//package com.javatechie.controller;
//
//import com.javatechie.service.KafkaProducerService;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/kafka")
//public class KafkaController {
//
//    private final KafkaProducerService producerService;
//
//    public KafkaController(KafkaProducerService producerService) {
//        this.producerService = producerService;
//    }
//
//    @GetMapping("/send/{message}")
//    public String send(@PathVariable String message) {
//        producerService.sendMessage("my-topic", message);
//        return "Sent: " + message;
//    }
//}
