package com.example.bulbulyator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText emailInput, passwordInput;
    private SharedPreferences prefs;
    private SupabaseDb db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        // Засеиваем тестовые товары в фоне (только один раз)
        new Thread(() -> SeedProducts.seedIfNeeded(getApplicationContext())).start();

        if (prefs.getBoolean("isLoggedIn", false)) {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        db = SupabaseDb.getInstance();

        emailInput    = findViewById(R.id.textInputEditText);
        passwordInput = findViewById(R.id.passwordInputEditText);

        findViewById(R.id.filledButton).setOnClickListener(v -> loginUser());
        findViewById(R.id.gosButton).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void loginUser() {
        String email    = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            User user = db.userDao().login(email, HashUtils.sha256(password));
            runOnUiThread(() -> {
                if (user != null) {
                    prefs.edit()
                            .putBoolean("isLoggedIn", true)
                            .putString("userName", user.firstName)
                            .putString("userEmail", user.email)
                            .putInt("userId", user.uid)
                            .apply();
                    Toast.makeText(this, "Добро пожаловать, " + user.firstName + "!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, ProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Неверный email или пароль", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}
