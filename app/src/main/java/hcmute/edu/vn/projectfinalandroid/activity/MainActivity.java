package hcmute.edu.vn.projectfinalandroid.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.tabs.TabLayout;
import com.google.mlkit.nl.translate.TranslateLanguage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hcmute.edu.vn.projectfinalandroid.R;
import hcmute.edu.vn.projectfinalandroid.fragment.CameraFragment;
import hcmute.edu.vn.projectfinalandroid.fragment.ChatFragment;
import hcmute.edu.vn.projectfinalandroid.fragment.TextFragment;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Spinner spSourceLang, spTargetLang; //spiner để hiện ds các ngôn ngữ cho người dùng chọn
    private Map<String, String> languageMap; // ánh xạ tên ngôn ngữ sang mã ML Kit
    private List<String> languageNames; // danh sách tên ngôn ngữ để hiển thị

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // khởi tạo danh sách để lấy ds ngôn ngữ được hỗ trợ từ mlkit
        languageMap = new HashMap<>();
        languageNames = new ArrayList<>();
        //khởi tạo ds ngôn ngữ
        initializeLanguageList();

        // Khởi tạo các thành phần
        spSourceLang = findViewById(R.id.spSourceLang);
        spTargetLang = findViewById(R.id.spTargetLang);
        ImageButton btnSwapLang = findViewById(R.id.btnSwapLang);

        // tạo adapter cho Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, languageNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // gán adapter cho Spinner để hiển thị languageNames trên Spinner.
        spSourceLang.setAdapter(adapter);
        spTargetLang.setAdapter(adapter);

        // thiết lập giá trị mặc định
        spSourceLang.setSelection(languageNames.indexOf("English"));
        spTargetLang.setSelection(languageNames.indexOf("Vietnamese"));

        // xử lý nút hoán đổi ngôn ngữ
        btnSwapLang.setOnClickListener(v -> {
            int sourcePos = spSourceLang.getSelectedItemPosition();
            int targetPos = spTargetLang.getSelectedItemPosition();
            spSourceLang.setSelection(targetPos);
            spTargetLang.setSelection(sourcePos);
            replaceFragmentWithLanguages();
        });

        // Xử lý khi chọn ngôn ngữ trong Spinner
        spSourceLang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                replaceFragmentWithLanguages();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spTargetLang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                replaceFragmentWithLanguages();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Khởi tạo TabLayout
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        // Thêm các tab
        tabLayout.addTab(tabLayout.newTab().setText("Text").setIcon(R.drawable.font));
        tabLayout.addTab(tabLayout.newTab().setText("Camera").setIcon(R.drawable.camera));
        tabLayout.addTab(tabLayout.newTab().setText("Chat").setIcon(R.drawable.chatbox));

        // Hiển thị fragment mặc định
        replaceFragmentWithLanguages();

        // Xử lý sự kiện khi chọn tab
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        replaceFragmentWithLanguages();
                        break;
                    case 1:
                        replaceFragment(new CameraFragment());
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

    private void initializeLanguageList() {
        List<String> languageCodes = TranslateLanguage.getAllLanguages();

        for (String code : languageCodes) {
            String languageName = languageCodeToName(code);
            languageNames.add(languageName);
            languageMap.put(languageName, code);
            Log.d(TAG, "initializeLanguageList: Added language " + languageName + " (" + code + ")");
        }

        // Sắp xếp danh sách ngôn ngữ theo bảng chữ cái
        languageNames.sort(String::compareTo);
    }


    private String languageCodeToName(String code) {
        // Ánh xạ mã ngôn ngữ sang tên dễ đọc
        Map<String, String> codeToName = new HashMap<>();
        codeToName.put(TranslateLanguage.AFRIKAANS, "Afrikaans");
        codeToName.put(TranslateLanguage.ALBANIAN, "Albanian");
        codeToName.put(TranslateLanguage.ARABIC, "Arabic");
        codeToName.put(TranslateLanguage.BELARUSIAN, "Belarusian");
        codeToName.put(TranslateLanguage.BENGALI, "Bengali");
        codeToName.put(TranslateLanguage.BULGARIAN, "Bulgarian");
        codeToName.put(TranslateLanguage.CATALAN, "Catalan");
        codeToName.put(TranslateLanguage.CHINESE, "Chinese");
        codeToName.put(TranslateLanguage.CROATIAN, "Croatian");
        codeToName.put(TranslateLanguage.CZECH, "Czech");
        codeToName.put(TranslateLanguage.DANISH, "Danish");
        codeToName.put(TranslateLanguage.DUTCH, "Dutch");
        codeToName.put(TranslateLanguage.ENGLISH, "English");
        codeToName.put(TranslateLanguage.ESTONIAN, "Estonian");
        codeToName.put(TranslateLanguage.FINNISH, "Finnish");
        codeToName.put(TranslateLanguage.FRENCH, "French");
        codeToName.put(TranslateLanguage.GALICIAN, "Galician");
        codeToName.put(TranslateLanguage.GERMAN, "German");
        codeToName.put(TranslateLanguage.GREEK, "Greek");
        codeToName.put(TranslateLanguage.HEBREW, "Hebrew");
        codeToName.put(TranslateLanguage.HINDI, "Hindi");
        codeToName.put(TranslateLanguage.HUNGARIAN, "Hungarian");
        codeToName.put(TranslateLanguage.ICELANDIC, "Icelandic");
        codeToName.put(TranslateLanguage.INDONESIAN, "Indonesian");
        codeToName.put(TranslateLanguage.ITALIAN, "Italian");
        codeToName.put(TranslateLanguage.JAPANESE, "Japanese");
        codeToName.put(TranslateLanguage.KOREAN, "Korean");
        codeToName.put(TranslateLanguage.LATVIAN, "Latvian");
        codeToName.put(TranslateLanguage.LITHUANIAN, "Lithuanian");
        codeToName.put(TranslateLanguage.MACEDONIAN, "Macedonian");
        codeToName.put(TranslateLanguage.MALAY, "Malay");
        codeToName.put(TranslateLanguage.NORWEGIAN, "Norwegian");
        codeToName.put(TranslateLanguage.PERSIAN, "Persian");
        codeToName.put(TranslateLanguage.POLISH, "Polish");
        codeToName.put(TranslateLanguage.PORTUGUESE, "Portuguese");
        codeToName.put(TranslateLanguage.ROMANIAN, "Romanian");
        codeToName.put(TranslateLanguage.RUSSIAN, "Russian");
        codeToName.put(TranslateLanguage.SLOVAK, "Slovak");
        codeToName.put(TranslateLanguage.SLOVENIAN, "Slovenian");
        codeToName.put(TranslateLanguage.SPANISH, "Spanish");
        codeToName.put(TranslateLanguage.SWAHILI, "Swahili");
        codeToName.put(TranslateLanguage.SWEDISH, "Swedish");
        codeToName.put(TranslateLanguage.THAI, "Thai");
        codeToName.put(TranslateLanguage.TURKISH, "Turkish");
        codeToName.put(TranslateLanguage.UKRAINIAN, "Ukrainian");
        codeToName.put(TranslateLanguage.VIETNAMESE, "Vietnamese");
        codeToName.put(TranslateLanguage.WELSH, "Welsh");
        codeToName.put(TranslateLanguage.ESPERANTO, "Esperanto");
        codeToName.put(TranslateLanguage.IRISH, "Irish");
        codeToName.put(TranslateLanguage.GUJARATI, "Gujarat");
        codeToName.put(TranslateLanguage.HAITIAN_CREOLE, "Creole Haiti");
        codeToName.put(TranslateLanguage.GEORGIAN, "Georgian");
        codeToName.put(TranslateLanguage.KANNADA, "Kannada");
        codeToName.put(TranslateLanguage.MARATHI, "Marathi");
        codeToName.put(TranslateLanguage.MALTESE, "Maltese");
        codeToName.put(TranslateLanguage.TAMIL, "Tamil");
        codeToName.put(TranslateLanguage.TELUGU, "Telugu");
        codeToName.put(TranslateLanguage.TAGALOG, "Tagalog");
        codeToName.put(TranslateLanguage.URDU, "Urdu");

        return codeToName.getOrDefault(code, code);
    }
    //cập nhật fragment với ngôn ngữ đã chọn
    private void replaceFragmentWithLanguages() {
        String sourceLang = (String) spSourceLang.getSelectedItem();
        String targetLang = (String) spTargetLang.getSelectedItem();
        // Ánh xạ sang mã ngôn ngữ ML Kit
        String sourceLangCode = languageMap.get(sourceLang);
        String targetLangCode = languageMap.get(targetLang);

        // Tạo Bundle để gửi ngôn ngữ
        Bundle bundle = new Bundle();
        bundle.putString("sourceLang", sourceLangCode);
        bundle.putString("targetLang", targetLangCode);

        // Tạo và thay thế TextFragment
        TextFragment fragment = new TextFragment();
        fragment.setArguments(bundle);
        replaceFragment(fragment);
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}