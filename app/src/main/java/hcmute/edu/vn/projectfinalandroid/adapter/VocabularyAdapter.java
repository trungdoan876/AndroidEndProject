package hcmute.edu.vn.projectfinalandroid.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hcmute.edu.vn.projectfinalandroid.R;
import hcmute.edu.vn.projectfinalandroid.model.Vocabulary;

public class VocabularyAdapter extends RecyclerView.Adapter<VocabularyAdapter.VocabViewHolder> {
    private List<Vocabulary> vocabList;
    private OnCheckedChangeListener onCheckedChangeListener;

    public interface OnCheckedChangeListener {
        void onCheckedChanged(Vocabulary vocab, boolean isChecked);
    }

    public VocabularyAdapter(List<Vocabulary> vocabList, OnCheckedChangeListener listener) {
        this.vocabList = vocabList;
        this.onCheckedChangeListener = listener;
    }

    @NonNull
    @Override
    public VocabViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vocab, parent, false);
        return new VocabViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VocabViewHolder holder, int position) {
        Vocabulary vocab = vocabList.get(position);
        holder.tvWord.setText(vocab.getVocab());
        holder.tvMeaning.setText(vocab.getMean_vocab());
        holder.cbLearned.setChecked(vocab.isLearned());

        holder.cbLearned.setOnCheckedChangeListener((buttonView, isChecked) -> {
            onCheckedChangeListener.onCheckedChanged(vocab, isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return vocabList.size();
    }

    static class VocabViewHolder extends RecyclerView.ViewHolder {
        TextView tvWord, tvMeaning;
        CheckBox cbLearned;

        public VocabViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWord = itemView.findViewById(R.id.tvVocab);
            tvMeaning = itemView.findViewById(R.id.tvMean);
            cbLearned = itemView.findViewById(R.id.checkLearned);
        }
    }
}


