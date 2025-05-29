package hcmute.edu.vn.projectfinalandroid.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_message")
public class ChatMessage {
    @PrimaryKey(autoGenerate = true)
    private int id_chat;
    private String role;
    private String content;

    public ChatMessage() {
    }

    public ChatMessage( String role, String content) {
        this.role = role;
        this.content = content;
    }

    public int getId_chat() {
        return id_chat;
    }

    public void setId_chat(int id_chat) {
        this.id_chat = id_chat;
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
