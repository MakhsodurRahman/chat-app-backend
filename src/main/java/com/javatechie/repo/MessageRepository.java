package com.javatechie.repo;
import com.javatechie.entity.Message;
import com.javatechie.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
//    List<Message> findByRecipientEmailAndDeliveredFalse(String email);
    List<Message> findByReceiverEmailAndReadFalse(String receiverEmail);
}
