package com.fsoft.fintern.models;

import com.fsoft.fintern.models.Shared.BaseModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "Conversation", schema = "dbo")
public class Conversation extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conversation_id", nullable = false)
    private Integer id;

    @Size(max = 255)
    @Nationalized
    @Column(name = "conversation_name")
    private String conversationName;

    @Size(max = 255)
    @Nationalized
    @Column(name = "conversation_avatar")
    private String conversationAvatar;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getConversationName() {
        return conversationName;
    }

    public void setConversationName(String conversationName) {
        this.conversationName = conversationName;
    }

    public String getConversationAvatar() {
        return conversationAvatar;
    }

    public void setConversationAvatar(String conversationAvatar) {
        this.conversationAvatar = conversationAvatar;
    }
}