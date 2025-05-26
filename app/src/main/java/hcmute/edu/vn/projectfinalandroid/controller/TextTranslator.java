package hcmute.edu.vn.projectfinalandroid.controller;

import androidx.annotation.NonNull;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

public class TextTranslator {
    //translator là đối tượng dịch được tạo từ ML kit
    private final Translator translator;
    private boolean isModelReady = false;
    //interface này là cầu nối giữa TextTranslator và lớp gọi nó - TextFragment
    public interface TranslationCallback {
        void onModelReady();
        void onModelDownloadFailed(String error);
        void onTranslationSuccess(String translatedText);
        void onTranslationFailed(String errorMessage);
    }
    //tạo constructor và tải model
    public TextTranslator(String sourceLang, String targetLang, @NonNull TranslationCallback callback) {
        //khởi tạo translator với ngôn ngữ nguồn và đích
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(sourceLang)
                .setTargetLanguage(targetLang)
                .build();
        translator = Translation.getClient(options);

        // Download ngôn ngữ khi mới chọn
        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();

        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(aVoid -> {
                    isModelReady = true;
                    callback.onModelReady();
                })
                .addOnFailureListener(e -> callback.onModelDownloadFailed(e.getMessage()));
    }
    //hàm dịch văn bản
    public void translateText(String text, @NonNull TranslationCallback callback) {
        if (!isModelReady) {
            callback.onTranslationFailed("Model not ready yet");
            return;
        }
        //truyền vào text cần dịch và trả về kết quả đã dịch
        translator.translate(text)
                .addOnSuccessListener(callback::onTranslationSuccess)
                .addOnFailureListener(e -> callback.onTranslationFailed(e.getMessage()));
    }
    //giải phóng bộ nhớ khi ko dùng nữa
    public void close() {
        translator.close();
    }
}
