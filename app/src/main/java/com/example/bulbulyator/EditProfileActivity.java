package com.example.bulbulyator;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText nameInput, bioInput;
    private ImageView avatarPreview, bannerPreview;
    private AppDatabase db;
    private SharedPreferences prefs;
    private int userId;

    private Uri selectedAvatarUri;
    private Uri selectedBannerUri;

    // 0 = аватар, 1 = баннер
    private int pickTarget = 0;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    try {
                        getContentResolver().takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception ignored) {}

                    if (pickTarget == 0) {
                        selectedAvatarUri = uri;
                        Glide.with(this).load(uri).circleCrop().into(avatarPreview);
                    } else {
                        selectedBannerUri = uri;
                        Glide.with(this).load(uri).centerCrop().into(bannerPreview);
                    }
                }
            }
    );

    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) openPicker();
                else Toast.makeText(this, "Нет доступа к файлам", Toast.LENGTH_SHORT).show();
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        db = AppDatabase.getInstance(this);
        prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        nameInput = findViewById(R.id.nameInput);
        bioInput = findViewById(R.id.bioInput);
        avatarPreview = findViewById(R.id.avatarPreview);
        bannerPreview = findViewById(R.id.bannerPreview);
        Button saveButton = findViewById(R.id.saveButton);
        Button selectAvatarButton = findViewById(R.id.selectAvatarButton);
        Button selectBannerButton = findViewById(R.id.selectBannerButton);

        // Загружаем текущие данные
        User user = db.userDao().getById(userId);
        if (user != null) {
            nameInput.setText(user.firstName);
            bioInput.setText(user.bio != null ? user.bio : "");
            if (user.avatarUri != null && !user.avatarUri.isEmpty()) {
                Glide.with(this).load(Uri.parse(user.avatarUri)).circleCrop().into(avatarPreview);
            }
            if (user.bannerUri != null && !user.bannerUri.isEmpty()) {
                Glide.with(this).load(Uri.parse(user.bannerUri)).centerCrop().into(bannerPreview);
            }
        }

        selectAvatarButton.setOnClickListener(v -> {
            pickTarget = 0;
            checkPermissionAndPick();
        });

        selectBannerButton.setOnClickListener(v -> {
            pickTarget = 1;
            checkPermissionAndPick();
        });

        saveButton.setOnClickListener(v -> save());
    }

    private void checkPermissionAndPick() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openPicker();
        } else {
            permissionLauncher.launch(permission);
        }
    }

    private void openPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // Принимаем и изображения и GIF
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        imagePickerLauncher.launch(intent);
    }

    private void save() {
        String newName = nameInput.getText().toString().trim();
        if (newName.isEmpty()) {
            Toast.makeText(this, "Введите имя", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = db.userDao().getById(userId);
        if (user != null) {
            user.firstName = newName;
            user.bio = bioInput.getText().toString().trim();
            if (selectedAvatarUri != null) user.avatarUri = selectedAvatarUri.toString();
            if (selectedBannerUri != null) user.bannerUri = selectedBannerUri.toString();
            db.userDao().update(user);
        }

        prefs.edit()
                .putString("userName", newName)
                .putString("userAvatarUri", user != null && user.avatarUri != null ? user.avatarUri : "")
                .putString("userBannerUri", user != null && user.bannerUri != null ? user.bannerUri : "")
                .apply();

        Toast.makeText(this, "Профиль обновлён", Toast.LENGTH_SHORT).show();
        finish();
    }
}
