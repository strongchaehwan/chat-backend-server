package com.example.chatserver.chat.service;

import com.example.chatserver.chat.domain.ChatMessage;
import com.example.chatserver.chat.domain.ChatParticipant;
import com.example.chatserver.chat.domain.ChatRoom;
import com.example.chatserver.chat.domain.ReadStatus;
import com.example.chatserver.chat.dto.ChatMessageRequestDto;
import com.example.chatserver.chat.dto.ChatMessageResponseDto;
import com.example.chatserver.chat.dto.ChatRoomListResponseDto;
import com.example.chatserver.chat.repository.ChatMessageRepository;
import com.example.chatserver.chat.repository.ChatParticipantRepository;
import com.example.chatserver.chat.repository.ChatRoomRepository;
import com.example.chatserver.chat.repository.ReadStatusRepository;
import com.example.chatserver.member.domain.Member;
import com.example.chatserver.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
     *
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

    public void createGroupRoom(String roomName) {
        //채팅방을 만든 사용자의 이메일을 SecurityContextHolder에서 가져오기 (이메일은 AuthFilter에서)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        //채팅방을 만든 사용자
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("멤버가 없습니다"));

        // 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .name(roomName)
                .isGroupChat("Y") // 그룹 채팅
                .build();

        chatRoomRepository.save(chatRoom);

        // 채팅 참여자로 개설자를 추가
        ChatParticipant chatParticipant = ChatParticipant.builder().chatRoom(chatRoom).member(member).build();
        chatParticipantRepository.save(chatParticipant);
    }

    public List<ChatRoomListResponseDto> getGroupChatRooms() {
        List<ChatRoom> chatRooms = chatRoomRepository.findByIsGroupChat("Y"); // 그룹 채팅방만 가져오기
        List<ChatRoomListResponseDto> chatRoomListResponseDtos = new ArrayList<>();
        for (ChatRoom chatRoom : chatRooms) {
            ChatRoomListResponseDto chatRoomListResponseDto = ChatRoomListResponseDto.builder()
                    .roomId(chatRoom.getId())
                    .roomName(chatRoom.getName())
                    .build();
            chatRoomListResponseDtos.add(chatRoomListResponseDto);
        }
        return chatRoomListResponseDtos;

    }

    public void addParticipantToGroupChat(Long roomId) {
        // 내가 누구인지?
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member joinMember = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("멤버가 없습니다"));

        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("채팅방이 없습니다"));

        // 이미 참여자 인지 검증
        Optional<ChatParticipant> optionalChatParticipant = chatParticipantRepository.findByChatRoomAndMember(chatRoom, joinMember);
        if (!optionalChatParticipant.isPresent()) { // 채팅방에 참여하지 않는 사람이라면
            this.addParticipantToRoom(chatRoom, joinMember);
        }
    }


    /**
     * ChatParticipant 객체 생성후 저장
     */
    private void addParticipantToRoom(ChatRoom chatRoom, Member member) {
        ChatParticipant chatParticipant = ChatParticipant.builder().chatRoom(chatRoom).member(member).build();
        chatParticipantRepository.save(chatParticipant);

    }

    public List<ChatMessageResponseDto> getChatHistoryMessage(Long roomId) {
        // 내가 해당 채팅방의 참여자가 아닐경우 에러
        // 내가 누구인지?
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("멤버가 없습니다"));

        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("채팅방이 없습니다"));

        // 채팅방 참여자 조회
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        boolean check = false;
        for (ChatParticipant chatParticipant : chatParticipants) {
            if (chatParticipant.getMember().equals(member)) {
                check = true;
            }
        }

        if (!check) {
            throw new IllegalArgumentException("본인이 속하지 않은 채팅방 입니다.");
        }

        // 특정 룸에 대한 메시지 조회
        List<ChatMessage> chatRoomOrderByCreatedTimeAsc = chatMessageRepository.findByChatRoomOrderByCreatedTimeAsc(chatRoom);
        List<ChatMessageResponseDto> chatMessageResponseDtos = new ArrayList<>();
        for (ChatMessage chatMessage : chatRoomOrderByCreatedTimeAsc) {
            ChatMessageResponseDto chatMessageResponseDto = ChatMessageResponseDto.builder()
                    .senderEmail(chatMessage.getMember().getEmail())
                    .message(chatMessage.getMessage())
                    .build();
            chatMessageResponseDtos.add(chatMessageResponseDto);
        }

        return chatMessageResponseDtos;

    }

    public boolean isRoomParicipant(String email, Long roomId) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("멤버가 없습니다"));

        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("채팅방이 없습니다"));

        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        for (ChatParticipant chatParticipant : chatParticipants) {
            if (chatParticipant.getMember().equals(member)) {
                return true;
            }
        }
        return false;

    }
}