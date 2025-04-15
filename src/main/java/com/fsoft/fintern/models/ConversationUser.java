package com.fsoft.fintern.models;

import com.fsoft.fintern.models.EmbedableID.ConversationUserId;
import com.fsoft.fintern.models.Shared.BaseModel;
import jakarta.persistence.*;

@Entity
@Table(name = "Conversation_user", schema = "dbo")
public class ConversationUser extends BaseModel {
    @EmbeddedId
    private ConversationUserId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @MapsId("conversationId")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Column(name = "is_admin")
    private Boolean isAdmin;

    public ConversationUserId getId() {
        return id;
    }

    public void setId(ConversationUserId id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public Boolean getAdmin() {
        return isAdmin;
    }

    public void setAdmin(Boolean admin) {
        isAdmin = admin;
    }
}