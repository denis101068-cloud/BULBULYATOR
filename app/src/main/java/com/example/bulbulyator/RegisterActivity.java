package com.example.bulbulyator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText nameInput, emailInput, passwordInput, confirmPasswordInput;
    private Button registerButton;
    private TextView loginLink;
    private AppDatabase db;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registr);

        db = AppDatabase.getInstance(this);
        prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        nameInput = findViewById(R.id.nameInputEditText);
        emailInput = findViewById(R.id.textInputEditText);
        passwordInput = findViewById(R.id.passwordInputEditText);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInputEditText);
        registerButton = findViewById(R.id.filledButton);
        loginLink = findViewById(R.id.textView3);

        registerButton.setOnClickListener(v -> registerUser());
        loginLink.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Пароль должен быть минимум 6 символов", Toast.LENGTH_SHORT).show();
            return;
        }
        if (db.userDao().findByEmail(email) != null) {
            Toast.makeText(this, "Пользователь с таким email уже существует", Toast.LENGTH_SHORT).show();
            return;
        }

        User newUser = new User();
        newUser.firstName = name;
        newUser.email = email;
        newUser.password = HashUtils.sha256(password);
        db.userDao().insertAll(newUser);

        User createdUser = db.userDao().findByEmail(email);
        prefs.edit()
                .putBoolean("isLoggedIn", true)
                .putString("userName", createdUser.firstName)
                .putString("userEmail", createdUser.email)
                .putInt("userId", createdUser.uid)
                .apply();

        Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
