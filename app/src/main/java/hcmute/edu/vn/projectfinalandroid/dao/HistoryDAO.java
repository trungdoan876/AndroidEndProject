package hcmute.edu.vn.projectfinalandroid.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import hcmute.edu.vn.projectfinalandroid.model.History;

@Dao
public interface HistoryDAO {
    @Insert
    void insert(History history);

    @Query("SELECT * FROM history ORDER BY dateTime DESC")
    List<History> getAll();
}
