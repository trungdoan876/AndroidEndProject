package hcmute.edu.vn.projectfinalandroid.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.util.List;
import hcmute.edu.vn.projectfinalandroid.model.AppDatabase;
import hcmute.edu.vn.projectfinalandroid.model.Category;
import hcmute.edu.vn.projectfinalandroid.model.History;
import hcmute.edu.vn.projectfinalandroid.model.Vocabulary;

public class TextController {
    private final Context context;
    private final AppDatabase db;
    private final LanguageManager languageManager;
    private final int userId;

    public interface TranslationResultCallback {
        void onSuccess(String translatedText);
        void onFailure(String errorMessage);
    }

    public interface HistoryCallback {
        void onHistoryLoaded(List<History> historyList);
    }

    public interface CategoriesCallback {
        void onCategoriesLoaded(List<Category> categoryList);
    }

    public TextController(Context context) {
        this.context = context;
        db = AppDatabase.getInstance(context);
        languageManager = new LanguageManager(context);
        SharedPreferences prefs = context.getSharedPreferences("MyApp", Context.MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);

        languageManager.initializeLanguages(null);
    }

    public void translateText(String inputText, TranslationResultCallback callback) {
        languageManager.getTextTranslator().translateText(inputText, new TextTranslator.TranslationCallback() {
            @Override public void onModelReady() { }
            @Override public void onModelDownloadFailed(String error) { callback.onFailure(error); }
            @Override public void onTranslationSuccess(String translatedText) {
                // Lưu lịch sử ở background
                new Thread(() -> {
                    db.historyDAO().insert(new History(userId, inputText, translatedText, System.currentTimeMillis()));
                }).start();
                callback.onSuccess(translatedText);
            }
            @Override public void onTranslationFailed(String errorMessage) {
                callback.onFailure(errorMessage);
            }
        });
    }

    public void loadHistory(HistoryCallback callback) {
        new Thread(() -> {
            List<History> historyList = db.historyDAO().getHistoryByUserId(userId);
            callback.onHistoryLoaded(historyList);
        }).start();
    }

    public void loadCategories(CategoriesCallback callback) {
        new Thread(() -> {
            List<Category> categories = db.categoryDao().getAllByIdUser(userId);
            callback.onCategoriesLoaded(categories);
        }).start();
    }

    public void addCategory(String categoryName, Runnable onSuccess, Runnable onExists, Runnable onEmpty) {
        if (categoryName.trim().isEmpty()) {
            onEmpty.run();
            return;
        }
        new Thread(() -> {
            Category existing = db.categoryDao().findByName(userId, categoryName);
            if (existing == null) {
                db.categoryDao().insert(new Category(categoryName, userId));
                onSuccess.run();
            } else {
                onExists.run();
            }
        }).start();
    }

    public void addVocabulary(String word, String meaning, int categoryId, Runnable onComplete) {
        new Thread(() -> {
            db.vocabularyDAO().insert(new Vocabulary(word, meaning, categoryId, false));
            onComplete.run();
        }).start();
    }

    public void updateLanguages(Bundle savedInstanceState) {
        languageManager.updateLanguages(savedInstanceState, new TextTranslator.TranslationCallback() {
            @Override public void onModelReady() {}
            @Override public void onModelDownloadFailed(String error) {}
            @Override public void onTranslationSuccess(String translatedText) {}
            @Override public void onTranslationFailed(String errorMessage) {}
        });
    }

    public void close() {
        languageManager.close();
    }
}

