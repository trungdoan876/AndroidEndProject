package hcmute.edu.vn.projectfinalandroid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import hcmute.edu.vn.projectfinalandroid.R;
import hcmute.edu.vn.projectfinalandroid.model.History;

public class HistoryAdapter extends ArrayAdapter<History> {
    private final Context context;
    private final List<History> historyList;

    public HistoryAdapter(Context context, List<History> historyList) {
        super(context, 0, historyList);
        this.context = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
        }

        History history = historyList.get(position);

        TextView tvOriginal = convertView.findViewById(R.id.tvOriginal);
        TextView tvTranslated = convertView.findViewById(R.id.tvTranslated);

        tvOriginal.setText(history.getOriginalText());
        tvTranslated.setText("Translated: " + history.getTranslatedText());

        return convertView;
    }
}
