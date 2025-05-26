package hcmute.edu.vn.projectfinalandroid.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import hcmute.edu.vn.projectfinalandroid.dao.CategoryDAO;
import hcmute.edu.vn.projectfinalandroid.dao.HistoryDAO;
import hcmute.edu.vn.projectfinalandroid.dao.UserDAO;
import hcmute.edu.vn.projectfinalandroid.dao.VocabularyDAO;
import hcmute.edu.vn.projectfinalandroid.model.Category;
import hcmute.edu.vn.projectfinalandroid.model.User;

// AppDatabase.java
@Database(entities = {User.class, Category.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract UserDAO userDao();
    public abstract CategoryDAO categoryDao();
    public abstract HistoryDAO historyDAO();
    public abstract VocabularyDAO vocabularyDAO();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "final_project_1")
                            .fallbackToDestructiveMigration() // nếu cần reset db khi version thay đổi
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
