package hcmute.edu.vn.projectfinalandroid.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import hcmute.edu.vn.projectfinalandroid.model.Vocabulary;

@Dao
public interface VocabularyDAO {
    @Insert
    void insert(Vocabulary vocabulary);

    @Query("SELECT * FROM vocabulary WHERE id_category = :categoryId")
    List<Vocabulary> getAllByCategoryId(int categoryId);

    @Update
    void update(Vocabulary vocabulary);

    @Query("SELECT * FROM vocabulary WHERE id_category = :idCategory")
    List<Vocabulary> getByCategoryId(int idCategory);
}
