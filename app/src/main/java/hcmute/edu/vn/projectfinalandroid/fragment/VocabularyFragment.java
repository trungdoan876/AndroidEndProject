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
import hcmute.edu.vn.projectfinalandroid.model.AppDatabase;
import hcmute.edu.vn.projectfinalandroid.model.Vocabulary;

public class VocabularyFragment extends Fragment {
    private TextView tvCategoryTitle;
    private RecyclerView recyclerVocabulary;
    private AppDatabase db;
    private List<Vocabulary> vocabList = new ArrayList<>();
    private VocabularyAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vocabulary, container, false);
        tvCategoryTitle = view.findViewById(R.id.tvCategoryTitle);
        recyclerVocabulary = view.findViewById(R.id.recyclerVocabulary);
        db = AppDatabase.getInstance(requireContext());

        Bundle args = getArguments();
        if (args != null) {
            int idCategory = args.getInt("id_category");
            String nameCategory = args.getString("name_category");
            tvCategoryTitle.setText("Danh mục: " + nameCategory);

            adapter = new VocabularyAdapter(vocabList, (vocab, isChecked) -> {
                vocab.setLearned(isChecked);
                new Thread(() -> db.vocabularyDAO().update(vocab)).start();
            });

            recyclerVocabulary.setLayoutManager(new LinearLayoutManager(requireContext()));
            recyclerVocabulary.setAdapter(adapter);

            // Load dữ liệu từ Room
            new Thread(() -> {
                List<Vocabulary> list = db.vocabularyDAO().getByCategoryId(idCategory);
                requireActivity().runOnUiThread(() -> {
                    vocabList.clear();
                    vocabList.addAll(list);
                    adapter.notifyDataSetChanged();
                });
            }).start();
        }

        return view;
    }
}

