package hcmute.edu.vn.projectfinalandroid.controller;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.mlkit.nl.translate.TranslateLanguage;

import hcmute.edu.vn.projectfinalandroid.controller.TextTranslator.TranslationCallback;

public class LanguageManager {
    private static final String TAG = "LanguageManager";
    private String sourceLang; // Ngôn ngữ gốc
    private String targetLang; // Ngôn ngữ dịch
    private String sourceLangName; // Tên ngôn ngữ gốc
    private String targetLangName; // Tên ngôn ngữ dịch
    private TextTranslator textTranslator; // Dùng class TextTranslator để dịch
    private final Context context;

    public LanguageManager(Context context) {
        this.context = context;
        // Khởi tạo ngôn ngữ mặc định
        sourceLang = TranslateLanguage.ENGLISH;
        targetLang = TranslateLanguage.VIETNAMESE;
        sourceLangName = "English";
        targetLangName = "Vietnamese";
    }

    // Khởi tạo ngôn ngữ từ Bundle
    public void initializeLanguages(Bundle args) {
        if (args != null && args.containsKey("sourceLang") && args.containsKey("targetLang")) {
            sourceLang = args.getString("sourceLang");
            targetLang = args.getString("targetLang");
            sourceLangName = args.getString("sourceLangName", "Unknown");
            targetLangName = args.getString("targetLangName", "Unknown");
            Log.d(TAG, "Received languages from Bundle: sourceLang=" + sourceLang + ", targetLang=" + targetLang);
        } else {
            sourceLang = TranslateLanguage.ENGLISH;
            targetLang = TranslateLanguage.VIETNAMESE;
            sourceLangName = "English";
            targetLangName = "Vietnamese";
            Log.d(TAG, "No Bundle or missing data, using default: sourceLang=" + sourceLang + ", targetLang=" + targetLang);
        }
        setupTranslator();
    }

    // Khởi tạo hàm dịch ngôn ngữ
    public void setupTranslator() {
        if (textTranslator != null) {
            textTranslator.close();
        }
        textTranslator = new TextTranslator(sourceLang, targetLang, new TranslationCallback() {
            @Override
            public void onModelReady() {
                Log.d(TAG, "Translation model ready: " + sourceLangName + " -> " + targetLangName);
                Toast.makeText(context, "Translation model loaded!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onModelDownloadFailed(String error) {
                Log.e(TAG, "Translation model download failed: " + error);
                Toast.makeText(context, "Translation model download error: " + error, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onTranslationSuccess(String translatedText) {
                // Để fragment xử lý
            }

            @Override
            public void onTranslationFailed(String errorMessage) {
                // Để fragment xử lý
            }
        });
    }

    // Cập nhật ngôn ngữ
    public void updateLanguages(Bundle bundle, @NonNull TranslationCallback callback) {
        String newSourceLang = bundle.getString("sourceLang", "en");
        String newTargetLang = bundle.getString("targetLang", "vi");
        String newSourceLangName = bundle.getString("sourceLangName", "English");
        String newTargetLangName = bundle.getString("targetLangName", "Vietnamese");

        if (!newSourceLang.equals(sourceLang) || !newTargetLang.equals(targetLang)) {
            Log.d(TAG, "Updating languages from " + sourceLangName + " -> " + targetLangName +
                    " to " + newSourceLangName + " -> " + newTargetLangName);
            sourceLang = newSourceLang;
            targetLang = newTargetLang;
            sourceLangName = newSourceLangName;
            targetLangName = newTargetLangName;
            setupTranslator();
        }
    }

    // Getter cho TextTranslator
    public TextTranslator getTextTranslator() {
        return textTranslator;
    }

    // Giải phóng tài nguyên
    public void close() {
        if (textTranslator != null) {
            textTranslator.close();
        }
    }
}