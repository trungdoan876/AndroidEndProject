package hcmute.edu.vn.projectfinalandroid.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.projectfinalandroid.R;
import hcmute.edu.vn.projectfinalandroid.adapter.VocabularyAdapter;
import hcmute.edu.vn.projectfinalandroid.controller.VocabularyController;
import hcmute.edu.vn.projectfinalandroid.model.AppDatabase;
import hcmute.edu.vn.projectfinalandroid.model.Vocabulary;
import hcmute.edu.vn.projectfinalandroid.repository.VocabularyRepository;

public class VocabularyFragment extends Fragment {
    private TextView tvCategoryTitle;
    private RecyclerView recyclerVocabulary;
    private VocabularyAdapter adapter;
    private List<Vocabulary> vocabList = new ArrayList<>();
    private VocabularyController controller;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vocabulary, container, false);
        tvCategoryTitle = view.findViewById(R.id.tvCategoryTitle);
        recyclerVocabulary = view.findViewById(R.id.recyclerVocabulary);

        AppDatabase db = AppDatabase.getInstance(requireContext());
        VocabularyRepository repository = new VocabularyRepository(db);

        Bundle args = getArguments();
        if (args != null) {
            int idCategory = args.getInt("id_category");
            String nameCategory = args.getString("name_category");
            tvCategoryTitle.setText("Category: " + nameCategory);

            adapter = new VocabularyAdapter(vocabList, (vocab, isChecked) -> {
                vocab.setLearned(isChecked);
                controller.updateVocabulary(vocab); // cập nhật qua Controller
            });

            recyclerVocabulary.setLayoutManager(new LinearLayoutManager(requireContext()));
            recyclerVocabulary.setAdapter(adapter);

            controller = new VocabularyController(repository, vocabList, adapter);
            controller.loadVocabByCategory(idCategory); // load dữ liệu qua Controller
        }

        return view;
    }
}
