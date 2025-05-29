package hcmute.edu.vn.projectfinalandroid.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import hcmute.edu.vn.projectfinalandroid.model.Category;

@Dao
public interface CategoryDAO {
    @Query("SELECT * FROM category WHERE id_user = :userId")
    List<Category> getAllByIdUser(int userId);
    @Query("SELECT * FROM category WHERE id_user = :userId AND name_category = :categoryName LIMIT 1")
    Category findByName(int userId, String categoryName);
    @Insert
    void insert(Category category);

}
