package hcmute.edu.vn.projectfinalandroid.repository;

import java.util.List;

import hcmute.edu.vn.projectfinalandroid.model.AppDatabase;
import hcmute.edu.vn.projectfinalandroid.model.Category;

public class CategoryRepository {
    private AppDatabase db;

    public CategoryRepository(AppDatabase db) {
        this.db = db;
    }

    public List<Category> getCategoriesByUserId(int userId) {
        return db.categoryDao().getAllByIdUser(userId);
    }
}
