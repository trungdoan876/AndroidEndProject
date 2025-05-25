package hcmute.edu.vn.projectfinalandroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

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

        // Xử lý sự kiện khi chọn tab
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Xử lý khi tab được chọn
                switch (tab.getPosition()) {
                    case 0:
                        // Tab "Text" được chọn
                        break;
                    case 1:
                        // Tab "Camera" được chọn
                        Intent intent = new Intent(MainActivity.this,CameraActivity.class);
                        startActivity(intent);
                        break;
                    case 2:
                        // Tab "Handwriting" được chọn
                        break;
                    case 3:
                        // Tab "Conversation" được chọn
                        break;
                    case 4:
                        // Tab "Voice" được chọn
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
}