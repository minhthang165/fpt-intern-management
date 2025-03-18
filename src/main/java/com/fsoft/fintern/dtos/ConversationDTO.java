package com.fsoft.fintern.dtos;

public class ConversationDTO {
    private int conversation_id;
    private String conversationName;
    private String conversationAvatar;
    private boolean is_Active;

    public String getConversation_avatar() {
        return conversationAvatar;
    }

    public void setConversation_avatar(String conversation_avatar) {
        this.conversationAvatar = conversation_avatar;
    }

    public String getConversation_name() {
        return conversationName;
    }

    public void setConversation_name(String conversation_name) {
        this.conversationName = conversation_name;
    }

    public int getConversation_id() {
        return conversation_id;
    }

    public void setConversation_id(int conversation_id) {
        this.conversation_id = conversation_id;
    }

    public boolean isIs_Active() {
        return is_Active;
    }

    public void setIs_Active(boolean is_Active) {
        this.is_Active = is_Active;
    }
}
