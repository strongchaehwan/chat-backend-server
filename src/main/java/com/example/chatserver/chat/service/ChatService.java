package com.example.chatserver.chat.service;

import com.example.chatserver.chat.domain.ChatMessage;
import com.example.chatserver.chat.domain.ChatParticipant;
import com.example.chatserver.chat.domain.ChatRoom;
import com.example.chatserver.chat.domain.ReadStatus;
import com.example.chatserver.chat.dto.ChatMessageRequestDto;
import com.example.chatserver.chat.repository.ChatMessageRepository;
import com.example.chatserver.chat.repository.ChatParticipantRepository;
import com.example.chatserver.chat.repository.ChatRoomRepository;
import com.example.chatserver.chat.repository.ReadStatusRepository;
import com.example.chatserver.member.domain.Member;
import com.example.chatserver.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ReadStatusRepository readStatusRepository;
    private final MemberRepository memberRepository;

    /**
     * 메시지 저장
     * @param roomId
     * @param chatMessageRequestDto
     */
    public void saveMessage(Long roomId, ChatMessageRequestDto chatMessageRequestDto) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("채팅방이 없습니다"));

        // 보낸사람조회
        Member sender = memberRepository.findByEmail(chatMessageRequestDto.getSenderEmail()).orElseThrow(() -> new EntityNotFoundException("멤버가 없습니다"));

        // 메시지 저장
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .member(sender)
                .message(chatMessageRequestDto.getMessage())
                .build();

        chatMessageRepository.save(chatMessage);

        // 사용자별로 읽음 여부 저장
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom); // 채팅방의 참여자 목록 가져오기
        for (ChatParticipant chatParticipant : chatParticipants) {
            ReadStatus readStatus = ReadStatus.builder()
                    .chatRoom(chatRoom)
                    .member(chatParticipant.getMember()) // 참여자들
                    .chatMessage(chatMessage)
                    .isRead(chatParticipant.getMember().equals(sender)) // 메시지 보낸사람은 바로 읽음여부하게함
                    .build();

            readStatusRepository.save(readStatus);
        }
    }
}
