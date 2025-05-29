package hcmute.edu.vn.projectfinalandroid.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "conversation")
public class Conversation {
    @PrimaryKey(autoGenerate = true)
    private int id_conversation;
    private int id_user;
    private long timestamp;

    public Conversation() {
    }

    public Conversation(int id_user, long timestamp) {
        this.id_user = id_user;
        this.timestamp = timestamp;
    }

    public int getId_conversation() {
        return id_conversation;
    }

    public void setId_conversation(int id_conversation) {
        this.id_conversation = id_conversation;
    }

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
