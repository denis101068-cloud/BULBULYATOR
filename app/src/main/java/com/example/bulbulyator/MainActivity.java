package com.example.bulbulyator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText emailInput, passwordInput;
    private Button loginButton;
    private TextView registerLink;
    private AppDatabase db;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        if (prefs.getBoolean("isLoggedIn", false)) {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        db = AppDatabase.getInstance(this);

        emailInput = findViewById(R.id.textInputEditText);
        passwordInput = findViewById(R.id.passwordInputEditText);
        loginButton = findViewById(R.id.filledButton);
        registerLink = findViewById(R.id.textView3);

        loginButton.setOnClickListener(v -> loginUser());
        registerLink.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = db.userDao().login(email, HashUtils.sha256(password));

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
    }
}
