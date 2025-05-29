package hcmute.edu.vn.projectfinalandroid.controller;

import android.os.Handler;
import android.os.Looper;

import java.util.List;

import hcmute.edu.vn.projectfinalandroid.adapter.VocabularyAdapter;
import hcmute.edu.vn.projectfinalandroid.model.Vocabulary;
import hcmute.edu.vn.projectfinalandroid.repository.VocabularyRepository;

public class VocabularyController {
    private VocabularyRepository repository;
    private VocabularyAdapter adapter;
    private List<Vocabulary> vocabList;

    private Handler mainHandler = new Handler(Looper.getMainLooper());

    public VocabularyController(VocabularyRepository repository, List<Vocabulary> vocabList, VocabularyAdapter adapter) {
        this.repository = repository;
        this.vocabList = vocabList;
        this.adapter = adapter;
    }

    public void loadVocabByCategory(int idCategory) {
        new Thread(() -> {
            List<Vocabulary> result = repository.getByCategoryId(idCategory);
            mainHandler.post(() -> {
                vocabList.clear();
                vocabList.addAll(result);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    public void updateVocabulary(Vocabulary vocab) {
        new Thread(() -> repository.updateVocabulary(vocab)).start();
    }
}
