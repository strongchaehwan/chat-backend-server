package com.example.chatserver.chat.controller;

import com.example.chatserver.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 그룹 채팅방 개설
     */
    @PostMapping("/room/group/create")
    public ResponseEntity<?> createGroupRoom(@RequestParam(name = "roomName") String roomName) {
        System.out.println("roomName = " + roomName);
        chatService.createGroupRoom(roomName);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
