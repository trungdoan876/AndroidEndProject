package hcmute.edu.vn.projectfinalandroid.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "vocabulary",
        foreignKeys = @ForeignKey(
                entity = Category.class,
                parentColumns = "id_category",
                childColumns = "id_category",
                onDelete = ForeignKey.CASCADE),
        indices = @Index("id_category"))
public class Vocabulary {
    @PrimaryKey(autoGenerate = true)
    private int id_vocab;

    private String vocab;
    private String mean_vocab;
    private int id_category;
}
