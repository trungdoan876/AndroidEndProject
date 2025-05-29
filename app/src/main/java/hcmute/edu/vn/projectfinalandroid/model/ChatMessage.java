package hcmute.edu.vn.projectfinalandroid.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import javax.annotation.processing.Generated;

@Entity(tableName = "chat_message")
public class ChatMessage {
    @PrimaryKey(autoGenerate = true)
    private int id_chat;
    private int id_conversation;
    private String role;
    private String content;

    public ChatMessage() {
    }

    public ChatMessage(int id_conversation, String role, String content) {
        this.id_conversation = id_conversation;
        this.role = role;
        this.content = content;
    }

    public int getId_chat() {
        return id_chat;
    }

    public void setId_chat(int id_chat) {
        this.id_chat = id_chat;
    }

    public int getId_conversation() {
        return id_conversation;
    }

    public void setId_conversation(int id_conversation) {
        this.id_conversation = id_conversation;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
