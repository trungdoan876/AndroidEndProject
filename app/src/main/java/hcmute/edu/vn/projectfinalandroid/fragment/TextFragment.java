package hcmute.edu.vn.projectfinalandroid.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import java.util.List;
import hcmute.edu.vn.projectfinalandroid.R;
import hcmute.edu.vn.projectfinalandroid.adapter.HistoryAdapter;
import hcmute.edu.vn.projectfinalandroid.controller.LanguageManager;
import hcmute.edu.vn.projectfinalandroid.controller.TextTranslator;
import hcmute.edu.vn.projectfinalandroid.data.AppDatabase;
import hcmute.edu.vn.projectfinalandroid.model.History;

public class TextFragment extends Fragment {
    private TextView outputText;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable translateRunnable;
    private LanguageManager languageManager;
    private AppDatabase db;
    private ImageButton btnHistory;
    private EditText inputText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_text, container, false);
        initializeUI(view);
        initializeDatabase();
        setupLanguageManager(savedInstanceState);
        translateText();
        historyTranslate();
        return view;
    }
    //ánh xạ
    private void initializeUI(View view) {
        inputText = view.findViewById(R.id.inputText);
        outputText = view.findViewById(R.id.outputText);
        btnHistory = view.findViewById(R.id.btnViewHistory);
        outputText.setText("");
    }
    // Khởi tạo database
    private void initializeDatabase() {
        db = AppDatabase.getInstance(requireContext());
    }
    private void setupLanguageManager(Bundle savedInstanceState) {
        // Khởi tạo translator
        languageManager = new LanguageManager(requireContext());
        languageManager.initializeLanguages(savedInstanceState);
    }

    private void translateText() {
        // dùng text watcher để lắng nghe nguoi dùng nhập văn bảng
        // -> thực hiện dịch realtime
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

                translateRunnable = () -> languageManager.getTextTranslator().translateText(text, new TextTranslator.TranslationCallback() {
                    @Override public void onModelReady() {}
                    @Override public void onModelDownloadFailed(String error) {}
                    @Override public void onTranslationSuccess(String translatedText) {
                        outputText.setText(translatedText);
                        new Thread(() -> {
                            SharedPreferences prefs = requireActivity().getSharedPreferences("MyApp", Context.MODE_PRIVATE);
                            int userId = prefs.getInt("userId", -1);
                            db.historyDAO().insert(
                                    new History(userId, inputText.getText().toString(), translatedText, System.currentTimeMillis())
                            );
                        }).start();
                    }
                    @Override public void onTranslationFailed(String errorMessage) {
                        outputText.setText(errorMessage);
                    }
                });

                handler.postDelayed(translateRunnable, 1000); // Delay 2s để tránh dịch quá nhiều
            }
        });
    }
    //xem lịch sử dịch của người dùng
    private void historyTranslate() {
        btnHistory.setOnClickListener(v -> new Thread(() -> {
            SharedPreferences prefs = requireActivity().getSharedPreferences("MyApp", Context.MODE_PRIVATE);
            int userId = prefs.getInt("userId", -1);
            List<History> historyList = db.historyDAO().getHistoryByUserId(userId);
            requireActivity().runOnUiThread(() -> {
                if (historyList.isEmpty()) {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Translation history")
                            .setMessage("No history yet.")
                            .setPositiveButton("Close", null)
                            .show();
                } else {
                    LayoutInflater inflater1 = LayoutInflater.from(requireContext());
                    View dialogView = inflater1.inflate(R.layout.dialog_history, null);
                    ListView listView = dialogView.findViewById(R.id.listViewHistory);
                    HistoryAdapter adapter = new HistoryAdapter(requireContext(), historyList);
                    listView.setAdapter(adapter);
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Translation history")
                            .setView(dialogView)
                            .setPositiveButton("Close", null)
                            .show();
                }
            });
        }).start());
    }

    public void updateLanguages(Bundle bundle) {
        languageManager.updateLanguages(bundle, new TextTranslator.TranslationCallback() {
            @Override public void onModelReady() {}
            @Override public void onModelDownloadFailed(String error) {}
            @Override public void onTranslationSuccess(String translatedText) {
                outputText.setText(translatedText);
            }
            @Override public void onTranslationFailed(String errorMessage) {
                outputText.setText(errorMessage);
            }
        });
        // Dịch lại văn bản hiện tại nếu có
        String currentText = inputText.getText().toString();
        if (!currentText.isEmpty()) {
            languageManager.getTextTranslator().translateText(currentText, new TextTranslator.TranslationCallback() {
                @Override public void onModelReady() {}
                @Override public void onModelDownloadFailed(String error) {}
                @Override public void onTranslationSuccess(String translatedText) {
                    outputText.setText(translatedText);
                }
                @Override public void onTranslationFailed(String errorMessage) {
                    outputText.setText(errorMessage);
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (translateRunnable != null) {
            handler.removeCallbacks(translateRunnable);
        }
        languageManager.close();
    }
}