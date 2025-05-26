package hcmute.edu.vn.projectfinalandroid.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "history",
        foreignKeys = {
                @ForeignKey(entity = User.class,
                        parentColumns = "id_user",
                        childColumns = "id_user",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("id_user")})
public class History {
    @PrimaryKey(autoGenerate = true)
    private int id_history;
    private int id_user;
    private String originalText;
    private String translatedText;
    private long dateTime;

    public History() {
    }

    public History(int id_user, String originalText, String translatedText, long dateTime) {
        this.id_user = id_user;
        this.originalText = originalText;
        this.translatedText = translatedText;
        this.dateTime = dateTime;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }

    public int getId_history() {
        return id_history;
    }

    public void setId_history(int id_history) {
        this.id_history = id_history;
    }

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }
}

