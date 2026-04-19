package com.javatechie.controller;

import com.javatechie.entity.Message;
import com.javatechie.entity.WsUser;
import com.javatechie.service.MemberStore;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.List;

@Controller
public class WebsocketController {

    private final MemberStore memberStore;
    private final SimpMessagingTemplate messagingTemplate;

    public WebsocketController(MemberStore memberStore, SimpMessagingTemplate messagingTemplate) {
        this.memberStore = memberStore;
        this.messagingTemplate = messagingTemplate;
    }

    // User registers and connects via WebSocket
    @MessageMapping("/user")
    public void registerUser(WsUser user, SimpMessageHeaderAccessor headerAccessor) {
        if (user.email() == null || user.username() == null) return;

        headerAccessor.getSessionAttributes().put("email", user.email());
        memberStore.addMember(user);

        // Send all offline/unread messages immediately
        List<Message> unread = memberStore.getOfflineMessages(user.email());
        unread.forEach(msg ->
                messagingTemplate.convertAndSend("/topic/messages/" + user.email(), msg)
        );
        memberStore.clearOfflineMessages(user.email());

        // Notify admin about all registered users (online + offline)
        messagingTemplate.convertAndSend("/topic/users", memberStore.getAllUsers());
    }

    // Admin sends message to specific user
    @MessageMapping("/message")
    public void sendMessage(Message message) {
        String targetEmail = message.getReceiverEmail();
        if (targetEmail == null) return;

        if (memberStore.isOnline(targetEmail)) {
            messagingTemplate.convertAndSend("/topic/messages/" + targetEmail, message);
        } else {
            memberStore.storeOfflineMessage(targetEmail, message);
        }
    }

    // Admin joins and requests all users
    @MessageMapping("/admin/join")
    public void adminJoin() {
        messagingTemplate.convertAndSend("/topic/users", memberStore.getAllUsers());
    }

    // Handle disconnect
    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String email = (String) sha.getSessionAttributes().get("email");

        if (email != null) {
            memberStore.removeMember(email);
            messagingTemplate.convertAndSend("/topic/users", memberStore.getAllUsers());
        }
    }
}
