package com.fsoft.fintern.models.EmbedableID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ConversationUserId implements Serializable {
    private static final long serialVersionUID = -1481890229345169771L;
    @NotNull
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @NotNull
    @Column(name = "conversation_id", nullable = false)
    private Integer conversationId;

    public ConversationUserId(Integer userId, Integer conversationId) {
        this.userId = userId;
        this.conversationId = conversationId;
    }

    public ConversationUserId() {
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getConversationId() {
        return conversationId;
    }

    public void setConversationId(Integer conversationId) {
        this.conversationId = conversationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ConversationUserId entity = (ConversationUserId) o;
        return Objects.equals(this.conversationId, entity.conversationId) &&
                Objects.equals(this.userId, entity.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(conversationId, userId);
    }

}