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
    private boolean isLearned;

    public Vocabulary() {
    }

    public Vocabulary(String vocab, String mean_vocab, int id_category, boolean isLearned) {
        this.vocab = vocab;
        this.mean_vocab = mean_vocab;
        this.id_category = id_category;
        this.isLearned = isLearned;
    }

    public int getId_vocab() {
        return id_vocab;
    }

    public void setId_vocab(int id_vocab) {
        this.id_vocab = id_vocab;
    }

    public String getVocab() {
        return vocab;
    }

    public void setVocab(String vocab) {
        this.vocab = vocab;
    }

    public String getMean_vocab() {
        return mean_vocab;
    }

    public void setMean_vocab(String mean_vocab) {
        this.mean_vocab = mean_vocab;
    }

    public int getId_category() {
        return id_category;
    }

    public void setId_category(int id_category) {
        this.id_category = id_category;
    }

    public boolean isLearned() {
        return isLearned;
    }

    public void setLearned(boolean learned) {
        isLearned = learned;
    }
}
