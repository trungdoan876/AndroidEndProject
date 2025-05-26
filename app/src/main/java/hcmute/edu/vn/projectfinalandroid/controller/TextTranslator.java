package hcmute.edu.vn.projectfinalandroid.controller;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

public class TextTranslator {
    private static final String TAG = "TextTranslatorHelper";

    private final Translator translator;
    private boolean isModelReady = false;

    public interface TranslationCallback {
        void onModelReady();
        void onModelDownloadFailed(String error);
        void onTranslationSuccess(String translatedText);
        void onTranslationFailed(String errorMessage);
    }

    public TextTranslator(String sourceLang, String targetLang, @NonNull TranslationCallback callback) {
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(sourceLang)
                .setTargetLanguage(targetLang)
                .build();
        translator = Translation.getClient(options);

        // Download ngôn ngữ nếu cần
        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();

        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(aVoid -> {
                    isModelReady = true;
                    callback.onModelReady();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to download model", e);
                    callback.onModelDownloadFailed(e.getMessage());
                });
    }

    public void translateText(String text, @NonNull TranslationCallback callback) {
        if (!isModelReady) {
            callback.onTranslationFailed("Model not ready yet");
            return;
        }

        translator.translate(text)
                .addOnSuccessListener(callback::onTranslationSuccess)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Translation failed", e);
                    callback.onTranslationFailed(e.getMessage());
                });
    }

    public void close() {
        translator.close();
    }
}
