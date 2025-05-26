package hcmute.edu.vn.projectfinalandroid.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import hcmute.edu.vn.projectfinalandroid.R;
import hcmute.edu.vn.projectfinalandroid.controller.TextTranslator;
import hcmute.edu.vn.projectfinalandroid.data.AppDatabase;
import hcmute.edu.vn.projectfinalandroid.model.History;

public class TextFragment extends Fragment {
    private TextView outputText;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable translateRunnable;

    private String sourceLang, targetLang;
    private TextTranslator translator;
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_text, container, false);

        EditText inputText = view.findViewById(R.id.inputText);
        outputText = view.findViewById(R.id.outputText);
        //khởi tạo database
        db = AppDatabase.getInstance(requireContext());
        //lấy tham số được guwir từ main activity
        Bundle args = getArguments();
        sourceLang = (args != null) ? args.getString("sourceLang", "en") : "en";
        targetLang = (args != null) ? args.getString("targetLang", "vi") : "vi";

        outputText.setText("Downloading model...");

        // Khởi tạo translator - để truyền callback xử lý
        translator = new TextTranslator(sourceLang, targetLang, new TextTranslator.TranslationCallback() {
            //khi model dịch được tải xong -> sẵn sàng để dịch
            @Override
            public void onModelReady() {
                outputText.setText("Ready to translate");
            }
            //tải thất bại
            @Override
            public void onModelDownloadFailed(String error) {
                outputText.setText("Download failed: " + error);
            }
            //dịch thành công -> gắn output = vban được dịch
            @Override
            public void onTranslationSuccess(String translatedText) {
                outputText.setText(translatedText);
            }
            // dịch thất bại
            @Override
            public void onTranslationFailed(String errorMessage) {
                outputText.setText("Translation error: " + errorMessage);
            }
        });

        // Lắng nghe nhập văn bản
        // sử dụng textwatcher để theo dõi người dùng gõ -> dịch theo thời gian thực
        inputText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();

                if (text.isEmpty()) {
                    outputText.setText("");
                    return;
                }

                if (translateRunnable != null) {
                    handler.removeCallbacks(translateRunnable);
                }

                translateRunnable = () -> translator.translateText(text, new TextTranslator.TranslationCallback() {
                    @Override public void onModelReady() {}
                    @Override public void onModelDownloadFailed(String error) {}
                    @Override public void onTranslationSuccess(String translatedText) {
                        outputText.setText(translatedText);
//                        new Thread(() -> {
//                            db.historyDAO().insert(
//                                    new History(inputText.getText().toString(), translatedText, System.currentTimeMillis())
//                            );
//                        }).start();
                    }
                    @Override public void onTranslationFailed(String errorMessage) {
                        outputText.setText(errorMessage);
                    }
                });

                handler.postDelayed(translateRunnable, 500); // delay 500ms để tránh dịch quá nhiều khi người dùng đang gõ
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (translateRunnable != null) {
            handler.removeCallbacks(translateRunnable);
        }
        if (translator != null) {
            translator.close();
        }
    }
}
