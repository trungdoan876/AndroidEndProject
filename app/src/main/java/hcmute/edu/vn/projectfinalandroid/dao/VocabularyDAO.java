package hcmute.edu.vn.projectfinalandroid.dao;

import androidx.room.Dao;
import androidx.room.Insert;

import hcmute.edu.vn.projectfinalandroid.model.Vocabulary;

@Dao
public interface VocabularyDAO {
    @Insert
    void insert(Vocabulary vocabulary);
}
