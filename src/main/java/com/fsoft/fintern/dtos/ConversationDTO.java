package com.fsoft.fintern.dtos;

public class ConversationDTO {
    private int conversation_id;
    private String conversation_name;
    private String conversation_avatar;
    private boolean is_Active;

    public String getConversation_avatar() {
        return conversation_avatar;
    }

    public void setConversation_avatar(String conversation_avatar) {
        this.conversation_avatar = conversation_avatar;
    }

    public String getConversation_name() {
        return conversation_name;
    }

    public void setConversation_name(String conversation_name) {
        this.conversation_name = conversation_name;
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
