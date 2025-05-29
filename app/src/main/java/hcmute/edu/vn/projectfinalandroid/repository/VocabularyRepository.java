package hcmute.edu.vn.projectfinalandroid.repository;

import java.util.List;

import hcmute.edu.vn.projectfinalandroid.model.AppDatabase;
import hcmute.edu.vn.projectfinalandroid.model.Vocabulary;

public class VocabularyRepository {
    private AppDatabase db;

    public VocabularyRepository(AppDatabase db) {
        this.db = db;
    }

    public List<Vocabulary> getByCategoryId(int idCategory) {
        return db.vocabularyDAO().getByCategoryId(idCategory);
    }

    public void updateVocabulary(Vocabulary vocabulary) {
        db.vocabularyDAO().update(vocabulary);
    }
}
