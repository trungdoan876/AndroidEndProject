package hcmute.edu.vn.projectfinalandroid.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;

import com.bumptech.glide.Glide;

import hcmute.edu.vn.projectfinalandroid.R;
import hcmute.edu.vn.projectfinalandroid.data.AppDatabase;
import hcmute.edu.vn.projectfinalandroid.model.User;

public class SignUpActivity extends AppCompatActivity {
    private AppCompatEditText userName, accountName, password, passwordConfirm, email;
    private AppCompatButton dangNhapBtn, dangKyBtn;
    AppDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.sign_up_activity);

        Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/1v6CpPz76d/bfyhkapi_expires_30_days.png")
                .into((ImageView) findViewById(R.id.earth));

//        Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/1v6CpPz76d/pmryk124_expires_30_days.png")
//                .into((ImageView) findViewById(R.id.logoUTE));

        Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/1v6CpPz76d/zx4uwtrx_expires_30_days.png")
                .into((ImageView) findViewById(R.id.lineAroundEarth));

        //
        userName = findViewById(R.id.nameUserEditText);
        accountName = findViewById(R.id.nameAccountEditText);
        password = findViewById(R.id.passwordEditText);
        passwordConfirm = findViewById(R.id.passwordConfirmEditText);
        email=findViewById(R.id.emailEditText);

        db = AppDatabase.getInstance(this);

        //quay lại trang đăng nhập
        dangNhapBtn = findViewById(R.id.BtnVeDangNhap);
        dangNhapBtn.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
            startActivity(intent);
        });
        //thực hiện đăng kí
        dangKyBtn = findViewById(R.id.BtnDangKy);
        dangKyBtn.setOnClickListener(v->{
            String emailStr = email.getText().toString().trim();
            String userNameStr = userName.getText().toString().trim();
            String accountNameStr = accountName.getText().toString().trim();
            String passwordStr = password.getText().toString().trim();
            String passwordConfirmStr = passwordConfirm.getText().toString().trim();
            if (emailStr.isEmpty() || userNameStr.isEmpty() || accountNameStr.isEmpty() || passwordStr.isEmpty()) {
                Toast.makeText(this, "Please enter complete information", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!passwordStr.equals(passwordConfirmStr)) {
                Toast.makeText(this, "Confirmation password does not match", Toast.LENGTH_SHORT).show();
                return;
            }
            new Thread(() -> {
                boolean isExist = db.userDao().isEmailOrAccountExist(emailStr,accountNameStr) > 0;

                runOnUiThread(() -> {
                    if (isExist) {
                        Toast.makeText(this, "Email or name account already exists!", Toast.LENGTH_SHORT).show();
                    } else {
                        User user = new User(emailStr, userNameStr, accountNameStr, passwordStr);
                        new Thread(() -> db.userDao().insert(user)).start();
                        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                    }
                });
            }).start();
        });
    }
}
