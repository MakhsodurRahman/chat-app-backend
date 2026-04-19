package com.javatechie.entity;

import com.javatechie.repo.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {
    private final MessageRepository repo;

    public MessageService(MessageRepository repo) {
        this.repo = repo;
    }

    public Message save(Message message) {
        return repo.save(message);
    }

    public List<Message> getUnreadMessages(String email) {
        return repo.findByReceiverEmailAndReadFalse(email);
    }

    public void markAsRead(List<Message> messages) {
        messages.forEach(m -> m.setRead(true));
        repo.saveAll(messages);
    }
}
