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

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

//@Controller
//public class ChatController {
//
//    private final SimpMessagingTemplate messagingTemplate;
//    @Autowired
//    private UserService userService;
//    
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
//    // Send updated user list to all clients
//    public void broadcastOnlineUsers() {
//    	//Set<String> onlineUsers = userService.getOnlineUsers(); //****
//        messagingTemplate.convertAndSend("/topic/onlineUsers", userService.getOnlineUsers());
//    }
//
//    // Call this method whenever a user logs in or logs out
//    public void userLogin(String username) {
//    	 userService.addUser(username); //***
//        broadcastOnlineUsers();
//    }
//
//    public void userLogout(String username) {
//    	userService.removeUser(username); //***
//        broadcastOnlineUsers();
//    }
//    
//    @MessageMapping("/getOnlineUsers")
//    public void getOnlineUsers(SimpMessageHeaderAccessor headerAccessor) {
//        String username = (String) headerAccessor.getSessionAttributes().get("username");
//        if (username != null) {
//        	Set<String> onlineUsers = userService.getOnlineUsers(); // Get online users from userService
//            messagingTemplate.convertAndSendToUser(username, "/queue/onlineUsers", userService.getOnlineUsers());
//        }
//    }
//
//}

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
    public void sendMessage(ChatMessage chatMessage) {
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

