package com.example.chatserver.chat.domain;

import com.example.chatserver.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ChatRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Builder.Default
    private String isGroupChat = "N";

    @OneToMany(mappedBy = "chatRoom" , cascade = CascadeType.REMOVE)
    private List<ChatParticipant> chatParticipants = new ArrayList<>();// 채팅방이 삭제되면 그 채탕방에 참여했던 사람들도 같이 삭제하게하자

    @OneToMany(mappedBy = "chatRoom",cascade = CascadeType.REMOVE , orphanRemoval = true)
    private List<ChatMessage>  chatMessages = new ArrayList<>(); // 채팅방이 사라지면 채탕방에 있던 메세지도 사라지게하자

}
