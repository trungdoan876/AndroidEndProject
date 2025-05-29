package hcmute.edu.vn.projectfinalandroid.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.projectfinalandroid.R;
import hcmute.edu.vn.projectfinalandroid.adapter.CategoryAdapter;
import hcmute.edu.vn.projectfinalandroid.controller.PersonalController;
import hcmute.edu.vn.projectfinalandroid.model.AppDatabase;
import hcmute.edu.vn.projectfinalandroid.model.Category;

public class PersonalFragment extends Fragment {
    private TextView tvDate, tvGreeting;
    AppDatabase db;
    PersonalController controller;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_personal, container, false);
        tvDate = view.findViewById(R.id.tvDate);
        tvGreeting = view.findViewById(R.id.tvGreeting);

        showCurrentDate();

        db = AppDatabase.getInstance(requireContext());

        RecyclerView recyclerCategories = view.findViewById(R.id.recyclerCategories);
        List<Category> categories = new ArrayList<>();

        CategoryAdapter adapter = new CategoryAdapter(categories, selectedCategory -> {
            VocabularyFragment fragment = new VocabularyFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("id_category", selectedCategory.getId_category());
            bundle.putString("name_category", selectedCategory.getName_category());
            fragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        recyclerCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerCategories.setAdapter(adapter);

        // Tạo Controller và gọi load dữ liệu
        controller = new PersonalController(requireContext(), db, categories, adapter);
        controller.loadCategories();

        return view;
    }

    private void showCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM", new Locale("vi"));
        String date = dateFormat.format(calendar.getTime());
        tvDate.setText(date);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour >= 5 && hour < 12) {
            greeting = "Good morning! 🌞";
        } else if (hour >= 12 && hour < 18) {
            greeting = "Good afternoon! ☀️";
        } else {
            greeting = "Good evening! 🌙";
        }
        tvGreeting.setText(greeting);
    }
}

