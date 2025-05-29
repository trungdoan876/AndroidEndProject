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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import hcmute.edu.vn.projectfinalandroid.R;
import hcmute.edu.vn.projectfinalandroid.adapter.HistoryAdapter;
import hcmute.edu.vn.projectfinalandroid.controller.LanguageManager;
import hcmute.edu.vn.projectfinalandroid.controller.TextTranslator;
import hcmute.edu.vn.projectfinalandroid.model.AppDatabase;
import hcmute.edu.vn.projectfinalandroid.model.Category;
import hcmute.edu.vn.projectfinalandroid.model.History;
import hcmute.edu.vn.projectfinalandroid.model.Vocabulary;

public class TextFragment extends Fragment {
    private TextView outputText;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable translateRunnable;
    private LanguageManager languageManager;
    private AppDatabase db;
    private MaterialButton btnHistory, btnAddVocab;
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
        addVocabulary();
        return view;
    }
    //ánh xạ
    private void initializeUI(View view) {
        inputText = view.findViewById(R.id.inputText);
        outputText = view.findViewById(R.id.outputText);
        btnHistory = view.findViewById(R.id.btnViewHistory);
        btnAddVocab = view.findViewById(R.id.btnAddVocabulary);
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

                handler.postDelayed(translateRunnable, 1000); // Delay 1s để tránh dịch quá nhiều
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
    private void addVocabulary() {
        btnAddVocab.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Add vocabulary");

            LayoutInflater inflater = LayoutInflater.from(requireContext());
            View dialogView = inflater.inflate(R.layout.dialog_add_vocab, null);

            EditText edtWord = dialogView.findViewById(R.id.edtWord);
            EditText edtMeaning = dialogView.findViewById(R.id.edtMeaning);
            Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
            ImageButton btnAddCategory = dialogView.findViewById(R.id.btnAddCategory); // nút thêm danh mục

            SharedPreferences prefs = requireActivity().getSharedPreferences("MyApp", Context.MODE_PRIVATE);
            int userId = prefs.getInt("userId", -1);

            List<String> categoryNames = new ArrayList<>();
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, categoryNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategory.setAdapter(adapter);

            // Load danh mục ban đầu
            Runnable loadCategories = () -> {
                List<Category> categories = db.categoryDao().getAllByIdUser(userId);
                categoryNames.clear();
                for (Category c : categories) categoryNames.add(c.getName_category());
                requireActivity().runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                    if (!categoryNames.isEmpty()) {
                        spinnerCategory.setSelection(0);
                    }
                });
            };

            new Thread(loadCategories).start();

            // Xử lý nút thêm danh mục mới
            btnAddCategory.setOnClickListener(vAdd -> {
                EditText input = new EditText(requireContext());
                new AlertDialog.Builder(requireContext())
                        .setTitle("New category")
                        .setView(input)
                        .setPositiveButton("Add", (dialog, which) -> {
                            String newCategory = input.getText().toString().trim();
                            if (!newCategory.isEmpty()) {
                                new Thread(() -> {
                                    Category existing = db.categoryDao().findByName(userId, newCategory);
                                    if (existing == null) {
                                        db.categoryDao().insert(new Category(newCategory, userId));
                                        // Reload danh mục
                                        loadCategories.run();
                                        // Chọn category vừa thêm
                                        requireActivity().runOnUiThread(() -> {
                                            int pos = categoryNames.indexOf(newCategory);
                                            if (pos >= 0) spinnerCategory.setSelection(pos);
                                        });
                                    } else {
                                        requireActivity().runOnUiThread(() ->
                                                Toast.makeText(requireContext(), "Category already exists", Toast.LENGTH_SHORT).show()
                                        );
                                    }
                                }).start();
                            } else {
                                Toast.makeText(requireContext(), "Category name cannot be blank", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });

            // Nút Save trong dialog chính
            builder.setView(dialogView);
            builder.setPositiveButton("Save", (dialog, which) -> {
                String word = edtWord.getText().toString().trim();
                String meaning = edtMeaning.getText().toString().trim();
                String selectedCategory = (String) spinnerCategory.getSelectedItem();

                if (!word.isEmpty() && !meaning.isEmpty() && selectedCategory != null) {
                    new Thread(() -> {
                        Category selected = db.categoryDao().findByName(userId, selectedCategory);
                        int categoryId = selected != null ? selected.getId_category() : -1;
                        db.vocabularyDAO().insert(new Vocabulary(word, meaning, categoryId, false));
                    }).start();
                } else {
                    Toast.makeText(requireContext(), "Please fill in the information and select a category.", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        });
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