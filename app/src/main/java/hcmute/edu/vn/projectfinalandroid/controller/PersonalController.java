package hcmute.edu.vn.projectfinalandroid.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import java.util.List;

import hcmute.edu.vn.projectfinalandroid.adapter.CategoryAdapter;
import hcmute.edu.vn.projectfinalandroid.model.AppDatabase;
import hcmute.edu.vn.projectfinalandroid.model.Category;
import hcmute.edu.vn.projectfinalandroid.repository.CategoryRepository;

public class PersonalController {
    private Context context;
    private CategoryRepository repository;
    private CategoryAdapter adapter;
    private List<Category> categories;

    private Handler mainHandler = new Handler(Looper.getMainLooper());

    public PersonalController(Context context, AppDatabase db, List<Category> categories, CategoryAdapter adapter) {
        this.context = context;
        this.repository = new CategoryRepository(db);
        this.categories = categories;
        this.adapter = adapter;
    }

    public int getUserId() {
        SharedPreferences prefs = context.getSharedPreferences("MyApp", Context.MODE_PRIVATE);
        return prefs.getInt("userId", -1);
    }

    public void loadCategories() {
        int userId = getUserId();
        new Thread(() -> {
            List<Category> result = repository.getCategoriesByUserId(userId);
            mainHandler.post(() -> {
                categories.clear();
                categories.addAll(result);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }
}
