//package com.java.controllers;
//
//
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.handler.annotation.SendTo;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Controller;
//
//import com.java.Entity.ChatMessage;
//import com.java.services.UserService;
//
//@Controller
//public class ChatController {
//
//    private SimpMessagingTemplate messagingTemplate;
//    private UserService userService;
//
//    public ChatController(SimpMessagingTemplate messagingTemplate, UserService userService) {
//        this.messagingTemplate = messagingTemplate;
//        this.userService = userService;
//    }
//
//    @MessageMapping("/chat.sendMessage")
//    public void sendMessage(ChatMessage chatMessage) {
//        messagingTemplate.convertAndSendToUser(chatMessage.getRecipient(), "/queue/messages", chatMessage);
//    }
//
//    @MessageMapping("/chat.addUser")
//    @SendTo("/topic/public")
//    public ChatMessage addUser(ChatMessage chatMessage) {
//        userService.addUser(chatMessage.getSender());
//        chatMessage.setType(ChatMessage.MessageType.JOIN);
//        return chatMessage;
//    }
//}
//
//
package com.java.controllers;

import com.java.Entity.ChatMessage;
import com.java.services.UserService;

import java.security.Principal;
import java.util.*;

import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;



@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

    @Autowired
    public ChatController(SimpMessagingTemplate messagingTemplate, UserService userService) {
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(ChatMessage chatMessage, Principal principal) {
    	 if (principal == null || !chatMessage.getSender().equals(principal.getName())) {
    	        throw new IllegalArgumentException("Message sender doesn't match authenticated user");
    	    }
    	    
    	
        // Save message to database if needed
        messagingTemplate.convertAndSendToUser(
            chatMessage.getRecipient(),
            "/queue/messages",
            chatMessage
        );
        
        // Also send the message back to the sender
        messagingTemplate.convertAndSendToUser(
            chatMessage.getSender(),
            "/queue/messages",
            chatMessage
        );
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        String username = chatMessage.getSender();
        headerAccessor.getSessionAttributes().put("username", username);
        userService.addUser(username);
        broadcastOnlineUsers();
    }

//    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        // You can add additional connect logic here
    }

//    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        
        if (username != null) {
            userService.removeUser(username);
            broadcastOnlineUsers();
        }
    }

    public void broadcastOnlineUsers() {
        Set<String> onlineUsers = userService.getOnlineUsers();
        messagingTemplate.convertAndSend("/topic/onlineUsers", onlineUsers);
    }
}

