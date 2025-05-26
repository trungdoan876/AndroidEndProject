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
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Category.class,
                        parentColumns = "id_category",
                        childColumns = "id_category",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("id_user"), @Index("id_category")})
public class History {
    @PrimaryKey(autoGenerate = true)
    private int id_history;
    private int id_user;
    private int id_category;
    private long dateTime;

    public History() {
    }

    public History(int id_user, int id_category, long dateTime) {
        this.id_user = id_user;
        this.id_category = id_category;
        this.dateTime = dateTime;
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

    public int getId_category() {
        return id_category;
    }

    public void setId_category(int id_category) {
        this.id_category = id_category;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }
}

