package hcmute.edu.vn.projectfinalandroid.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;

import com.bumptech.glide.Glide;

import hcmute.edu.vn.projectfinalandroid.R;
import hcmute.edu.vn.projectfinalandroid.model.AppDatabase;
import hcmute.edu.vn.projectfinalandroid.model.User;

public class MainActivity extends AppCompatActivity {
    private AppCompatEditText username, password;
    private AppCompatButton dangNhapBtn, dangKyBtn;
    AppDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/1v6CpPz76d/bfyhkapi_expires_30_days.png")
                .into((ImageView) findViewById(R.id.earth));

        Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/1v6CpPz76d/pmryk124_expires_30_days.png")
                .into((ImageView) findViewById(R.id.logoUTE));

        Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/1v6CpPz76d/zx4uwtrx_expires_30_days.png")
                .into((ImageView) findViewById(R.id.lineAroundEarth));

        username = findViewById(R.id.usernameEditText);
        password = findViewById(R.id.passwordEditText);
        db = AppDatabase.getInstance(this);


        //đăng nhập tài khoản
        dangNhapBtn = findViewById(R.id.DangNhapBtn);
        dangNhapBtn.setOnClickListener(v->{
            String usernameStr = username.getText().toString().trim();
            String passwordStr = password.getText().toString().trim();
            if (usernameStr.isEmpty() || passwordStr.isEmpty()) {
                Toast.makeText(this, "Please enter complete information!", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                User user = db.userDao().getUserByNameAccount(usernameStr);  // Giả sử có hàm này trong UserDAO

                runOnUiThread(() -> {
                    if (user == null) {
                        Toast.makeText(this, "Account does not exist!", Toast.LENGTH_SHORT).show();
                    } else {
                        if (user.getPassword().equals(passwordStr)) {
                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                            startActivity(intent);
                            SharedPreferences prefs = getSharedPreferences("MyApp", MODE_PRIVATE);
                            prefs.edit().putInt("userId", user.getId_user()).apply();
                            finish();
                        } else {
                            Toast.makeText(this, "Incorrect password!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }).start();
        });
        // chuyển qua trang thực hiện đăng kí tài khoản
        dangKyBtn = findViewById(R.id.DangKyBtn);
        dangKyBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }
}