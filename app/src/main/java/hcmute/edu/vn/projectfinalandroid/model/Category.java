package hcmute.edu.vn.projectfinalandroid.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "category",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id_user",
                childColumns = "id_user_category",
                onDelete = ForeignKey.CASCADE),
        indices = @Index("id_user"))
public class Category {
    @PrimaryKey(autoGenerate = true)
    private int id_category;

    private String name_category;
    private int id_user;

    public Category() {
    }

    public Category(String name_category, int id_user) {
        this.name_category = name_category;
        this.id_user = id_user;
    }

    public int getId_category() {
        return id_category;
    }

    public void setId_category(int id_category) {
        this.id_category = id_category;
    }

    public String getName_category() {
        return name_category;
    }

    public void setName_category(String name_category) {
        this.name_category = name_category;
    }

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }
}

