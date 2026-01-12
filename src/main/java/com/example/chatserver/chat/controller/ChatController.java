package com.example.chatserver.chat.controller;

import com.example.chatserver.chat.dto.ChatMessageResponseDto;
import com.example.chatserver.chat.dto.ChatRoomListResponseDto;
import com.example.chatserver.chat.dto.MyChatListResponseDto;
import com.example.chatserver.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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


    /**
     * 그룹 채팅 목록 조회
     */
    @GetMapping("/room/group/list")
    public ResponseEntity<?> getGroupChatRooms() {
        List<ChatRoomListResponseDto> groupChatRooms = chatService.getGroupChatRooms(); // 그룹 채팅 목록만 조회
        return ResponseEntity.status(HttpStatus.OK).body(groupChatRooms);
    }

    /**
     * 그룹 채팅방 참여
     */
    @PostMapping("/room/group/{roomId}/join")
    public ResponseEntity<?> joinGroupChatRoom(@PathVariable(name = "roomId") Long roomId) {
        chatService.addParticipantToGroupChat(roomId);
        return ResponseEntity.ok().build();
    }

    /**
     * 이전 메세지 조회
     */
    @GetMapping("/history/{roomId}")
    public ResponseEntity<?> getChatHistoryMessage(@PathVariable(name = "roomId") Long roomId) {
        List<ChatMessageResponseDto> chatMessageResponseDtos = chatService.getChatHistoryMessage(roomId);
        return ResponseEntity.status(HttpStatus.OK).body(chatMessageResponseDtos);
    }

    /**
     * 채팅 메시지 읽음처리
     */
    @PostMapping("/room/{roomId}/read")
    public ResponseEntity<?> messageRead(@PathVariable(name = "roomId") Long roomId) {
        chatService.messageRead(roomId);
        return ResponseEntity.ok().build();
    }

    /**
     * 내 채팅방 목록 조회
     */
    @GetMapping("/my/rooms")
    public ResponseEntity<?> getMyChatRooms() {
        List<MyChatListResponseDto> myChatListResponseDtos = chatService.getMyChatRooms();
        return ResponseEntity.status(HttpStatus.OK).body(myChatListResponseDtos);
    }

    /**
     * 채팅방 나가기
     */
    @DeleteMapping("/room/group/{roomId}/leave")
    public ResponseEntity<?> leaveGroupChatRoom(@PathVariable(name = "roomId") Long roomId){
        chatService.leaveGroupChatRoom(roomId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
