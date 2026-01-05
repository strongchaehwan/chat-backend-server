package com.example.chatserver.chat.repository;

import com.example.chatserver.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

//    @Query("select cr from ChatRoom cr where cr.isGroupChat = :isGroupChat")
    List<ChatRoom> findByIsGroupChat(String isGroupChat);
}
