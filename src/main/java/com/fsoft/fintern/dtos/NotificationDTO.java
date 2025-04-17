package com.fsoft.fintern.dtos;

import com.fsoft.fintern.enums.NotificationType;

import java.util.List;

public class NotificationDTO {
    private int id;
    private int actorId;
    private String content;
    private String url;
    private String actor_avatar;
    private NotificationType type;
    private List<Integer> recipientIds;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getActorId() {
        return actorId;
    }

    public void setActorId(int actorId) {
        this.actorId = actorId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getActor_avatar() {
        return actor_avatar;
    }

    public void setActor_avatar(String actor_avatar) {
        this.actor_avatar = actor_avatar;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public List<Integer> getRecipientIds() {
        return recipientIds;
    }

    public void setRecipientIds(List<Integer> recipientIds) {
        this.recipientIds = recipientIds;
    }
}
