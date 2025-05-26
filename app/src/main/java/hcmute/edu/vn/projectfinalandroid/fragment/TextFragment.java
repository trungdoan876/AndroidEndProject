package hcmute.edu.vn.projectfinalandroid.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import hcmute.edu.vn.projectfinalandroid.R;

public class TextFragment extends Fragment {
    private static final String TAG = "TextFragment"; // Tag cho log
    private TextView outputText;
    private Translator translator;
    private boolean isModelReady = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable translateRunnable;
    private String sourceLang;
    private String targetLang;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_text, container, false);
        EditText inputText = view.findViewById(R.id.inputText);
        outputText = view.findViewById(R.id.outputText);
        Bundle args = getArguments();
        if (args != null) {
            sourceLang = args.getString("sourceLang", "en");
            targetLang = args.getString("targetLang", "vi");
            Log.d(TAG, "onCreateView: Received sourceLang=" + sourceLang + ", targetLang=" + targetLang);
        } else {
            sourceLang = "en";
            targetLang = "vi";
        }
        //cấu hình để sử dụng mlkit
        // thiết lập đối tượng Translator của MLKit
        // định nghĩa cấu hình cho bộ dịch, bao gồm nn nguồn và đích
        // TranslateLanguage: enum của ML Kit
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(sourceLang)
                .setTargetLanguage(targetLang)
                .build();
        // tạo đối tương Translator để dịch văn bản
        translator = Translation.getClient(options);

        // tải mô hình ngoon ngữ trước khi dịch
        // mô hình dịch là cần thiết để thực hiện dịch ngoại tuyến sau khi tải lần đầu. Nếu không tải mô hình, việc dịch sẽ thất bại.
        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();

        outputText.setText("Waiting...");
        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(aVoid -> {
                    isModelReady = true;
                    outputText.setText("Ready to translate");
                })
                .addOnFailureListener(e -> {
                    outputText.setText("Failed to download model");
//                    outputText.setText(e.getMessage());
                });

        // dùng TextWatcher để lắng nghe thay đổi văn bản và dịch realtime
        inputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                if (isModelReady && !text.isEmpty()) {
                    translateText(text);
                } else if (text.isEmpty()) {
                    outputText.setText("Enter text...");
                } else {
                    Log.w(TAG, "afterTextChanged: Model not ready yet");
                }
            }
        });
        return view;
    }
    //gọi api của MLKit để dịch văn bản
    private void translateText(String text) {
        if (translateRunnable != null) {
            handler.removeCallbacks(translateRunnable);
        }
        translateRunnable = () -> {
            Log.d(TAG, "translateText: Starting translation for: " + text);
            translator.translate(text)
                    .addOnSuccessListener(translatedText -> {
                        outputText.setText(translatedText);
                    })
                    .addOnFailureListener(e -> {
                        outputText.setText(e.getMessage());
                    });
        };
        handler.postDelayed(translateRunnable, 500);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        translator.close();
        if (translateRunnable != null) {
            handler.removeCallbacks(translateRunnable);
        }
    }
}