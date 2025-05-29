package hcmute.edu.vn.projectfinalandroid.activity;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.google.mlkit.nl.translate.TranslateLanguage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hcmute.edu.vn.projectfinalandroid.R;
import hcmute.edu.vn.projectfinalandroid.fragment.CameraFragment;
import hcmute.edu.vn.projectfinalandroid.fragment.ChatFragment;
import hcmute.edu.vn.projectfinalandroid.fragment.PersonalFragment;
import hcmute.edu.vn.projectfinalandroid.fragment.TextFragment;
import hcmute.edu.vn.projectfinalandroid.receiver.ReminderReceiver;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Spinner spSourceLang, spTargetLang;
    private Map<String, String> languageMap;
    private List<String> languageNames;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.tab_layout);
        requestNotificationPermissionIfNeeded();
        scheduleMultipleReminders(this);

        // Khởi tạo danh sách ngôn ngữ
        languageMap = new HashMap<>();
        languageNames = new ArrayList<>();
        initializeLanguageList();

        // Khởi tạo các thành phần
        spSourceLang = findViewById(R.id.spSourceLang);
        spTargetLang = findViewById(R.id.spTargetLang);
        ImageButton btnSwapLang = findViewById(R.id.btnSwapLang);

        // Tạo adapter cho Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, languageNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Gán adapter cho Spinner
        spSourceLang.setAdapter(adapter);
        spTargetLang.setAdapter(adapter);

        // Thiết lập giá trị mặc định
        spSourceLang.setSelection(languageNames.indexOf("English"));
        spTargetLang.setSelection(languageNames.indexOf("Vietnamese"));

        // Xử lý nút hoán đổi ngôn ngữ
        btnSwapLang.setOnClickListener(v -> {
            int sourcePos = spSourceLang.getSelectedItemPosition();
            int targetPos = spTargetLang.getSelectedItemPosition();
            spSourceLang.setSelection(targetPos);
            spTargetLang.setSelection(sourcePos);
            updateCurrentFragmentWithLanguages();
        });

        // Xử lý khi chọn ngôn ngữ trong Spinner
        spSourceLang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateCurrentFragmentWithLanguages();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spTargetLang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateCurrentFragmentWithLanguages();
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
        tabLayout.addTab(tabLayout.newTab().setText("Personal").setIcon(R.drawable.user));

        // Hiển thị TextFragment mặc định
        replaceFragmentWithLanguages();

        // Xử lý sự kiện khi chọn tab
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment fragment;
                switch (tab.getPosition()) {
                    case 0:
                        fragment = new TextFragment();
                        break;
                    case 1:
                        fragment = new CameraFragment();
                        break;
                    case 2:
                        fragment = new ChatFragment();
                        break;
                    case 3:
                        fragment = new PersonalFragment();
                        break;
                    default:
                        return;
                }
                // Gán ngôn ngữ cho fragment mới
                Bundle bundle = createLanguageBundle();
                fragment.setArguments(bundle);
                replaceFragment(fragment);
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

    private Bundle createLanguageBundle() {
        String sourceLang = (String) spSourceLang.getSelectedItem();
        String targetLang = (String) spTargetLang.getSelectedItem();
        String sourceLangCode = languageMap.get(sourceLang);
        String targetLangCode = languageMap.get(targetLang);

        Bundle bundle = new Bundle();
        bundle.putString("sourceLang", sourceLangCode);
        bundle.putString("targetLang", targetLangCode);
        return bundle;
    }

    private void updateCurrentFragmentWithLanguages() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (currentFragment != null) {
            Bundle bundle = createLanguageBundle();
            currentFragment.setArguments(bundle);
            if (currentFragment instanceof CameraFragment) {
                ((CameraFragment) currentFragment).updateLanguages(bundle);
            } else if (currentFragment instanceof TextFragment) {
                ((TextFragment) currentFragment).updateLanguages(bundle);
            }
            Log.d(TAG, "Cập nhật ngôn ngữ cho fragment hiện tại: " + currentFragment.getClass().getSimpleName());
        } else {
            // Hiển thị TextFragment mặc định nếu không có fragment
            replaceFragmentWithLanguages();
        }
    }

    private void replaceFragmentWithLanguages() {
        TextFragment fragment = new TextFragment();
        Bundle bundle = createLanguageBundle();
        fragment.setArguments(bundle);
        replaceFragment(fragment);
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
    private void scheduleMultipleReminders(Context context) {
        Log.d(TAG, "Scheduling fixed-time reminders by hour & minute...");

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Mỗi phần tử là {giờ, phút}
        int[][] times = {
                {7, 30},   // 07:30 sáng
                {12, 15},  // 12:15 trưa
                {20, 30}   // 20:00 tối
        };

        for (int i = 0; i < times.length; i++) {
            int hour = times[i][0];
            int minute = times[i][1];

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // Nếu giờ này đã qua trong ngày hôm nay thì đặt cho ngày mai
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DATE, 1);
            }

            Intent intent = new Intent(context, ReminderReceiver.class);
            intent.putExtra("reminder_id", i);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, i, intent, PendingIntent.FLAG_IMMUTABLE
            );

            if (alarmManager != null) {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                Log.e(TAG, "AlarmManager is null!");
            }
        }
    }


    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permission", "Notification permission granted");
            } else {
                Log.d("Permission", "Notification permission denied");
            }
        }
    }

}