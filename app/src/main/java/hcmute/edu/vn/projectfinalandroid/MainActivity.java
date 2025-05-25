package hcmute.edu.vn.projectfinalandroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {
    private Spinner spSourceLang, spTargetLang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Khởi tạo các thành phần
        spSourceLang = findViewById(R.id.spSourceLang);
        spTargetLang = findViewById(R.id.spTargetLang);
        ImageButton btnSwapLang = findViewById(R.id.btnSwapLang);

        // Danh sách ngôn ngữ
        String[] languages = {"English", "Vietnamese", "Spanish", "French", "German", "Chinese", "Japanese"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Gán adapter cho Spinner
        spSourceLang.setAdapter(adapter);
        spTargetLang.setAdapter(adapter);

        // Thiết lập giá trị mặc định
        spSourceLang.setSelection(0); // English
        spTargetLang.setSelection(1); // Hindi

        // Xử lý nút hoán đổi ngôn ngữ
        btnSwapLang.setOnClickListener(v -> {
            int sourcePos = spSourceLang.getSelectedItemPosition();
            int targetPos = spTargetLang.getSelectedItemPosition();
            spSourceLang.setSelection(targetPos);
            spTargetLang.setSelection(sourcePos);
        });
        // Khởi tạo TabLayout
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        // Thêm các tab
        tabLayout.addTab(tabLayout.newTab().setText("Text").setIcon(R.drawable.font));
        tabLayout.addTab(tabLayout.newTab().setText("Camera").setIcon(R.drawable.camera));
        tabLayout.addTab(tabLayout.newTab().setText("Chat").setIcon(R.drawable.chatbox));

        //hien thị fragment mặc định
        replaceFragment(new TextFragment());
        // Xử lý sự kiện khi chọn tab
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        replaceFragment(new TextFragment());
                        break;
                    case 1:
                        break;
                    case 2:
                        replaceFragment(new ChatFragment());
                        break;
                }
            }


            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

}