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
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;

public class EditProfileActivity extends BaseActivity {

    private TextInputEditText nameInput, bioInput;
    private ImageView avatarPreview, bannerPreview;
    private SupabaseDb db;
    private SharedPreferences prefs;
    private int userId;
    private Uri selectedAvatarUri, selectedBannerUri;
    private int pickTarget = 0;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    try { getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); }
                    catch (Exception ignored) {}
                    if (pickTarget == 0) {
                        selectedAvatarUri = uri;
                        Glide.with(this).load(uri).circleCrop().into(avatarPreview);
                    } else {
                        selectedBannerUri = uri;
                        Glide.with(this).load(uri).centerCrop().into(bannerPreview);
                    }
                }
            });

    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> { if (granted) openPicker(); else Toast.makeText(this, "Нет доступа к файлам", Toast.LENGTH_SHORT).show(); });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        db     = SupabaseDb.getInstance();
        prefs  = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        nameInput     = findViewById(R.id.nameInput);
        bioInput      = findViewById(R.id.bioInput);
        avatarPreview = findViewById(R.id.avatarPreview);
        bannerPreview = findViewById(R.id.bannerPreview);
        Button saveButton         = findViewById(R.id.saveButton);
        Button selectAvatarButton = findViewById(R.id.selectAvatarButton);
        Button selectBannerButton = findViewById(R.id.selectBannerButton);

        new Thread(() -> {
            User user = db.userDao().getById(userId);
            runOnUiThread(() -> {
                if (user != null) {
                    nameInput.setText(user.firstName);
                    bioInput.setText(user.bio != null ? user.bio : "");
                    if (user.avatarUri != null && !user.avatarUri.isEmpty())
                        Glide.with(this).load(Uri.parse(user.avatarUri)).circleCrop().into(avatarPreview);
                    if (user.bannerUri != null && !user.bannerUri.isEmpty())
                        Glide.with(this).load(Uri.parse(user.bannerUri)).centerCrop().into(bannerPreview);
                }
            });
        }).start();

        selectAvatarButton.setOnClickListener(v -> { pickTarget = 0; checkPermissionAndPick(); });
        selectBannerButton.setOnClickListener(v -> { pickTarget = 1; checkPermissionAndPick(); });
        saveButton.setOnClickListener(v -> {
            saveButton.setEnabled(false);
            saveButton.setText("Сохранение...");
            save(saveButton);
        });
    }

    @Override
    protected void onResume() { super.onResume(); applyTheme(); }

    private void checkPermissionAndPick() {
        String perm = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED) openPicker();
        else permissionLauncher.launch(perm);
    }

    private void openPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        imagePickerLauncher.launch(intent);
    }

    private void save(Button saveButton) {
        String newName = nameInput.getText().toString().trim();
        if (newName.isEmpty()) {
            Toast.makeText(this, "Введите имя", Toast.LENGTH_SHORT).show();
            saveButton.setEnabled(true);
            saveButton.setText("Сохранить");
            return;
        }

        new Thread(() -> {
            User user = db.userDao().getById(userId);
            if (user == null) {
                runOnUiThread(() -> {
                    getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().clear().apply();
                    startActivity(new Intent(this, MainActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                });
                return;
            }
            user.firstName = newName;
            user.bio = bioInput.getText().toString().trim();

            if (selectedAvatarUri != null) {
                String mime = getMime(selectedAvatarUri);
                String ext  = mime.contains("png") ? "png" : "jpg";
                String url  = SupabaseClient.uploadFile(
                        "avatars", userId + "/avatar." + ext,
                        this, selectedAvatarUri, mime);
                if (url != null) user.avatarUri = url;
                else user.avatarUri = selectedAvatarUri.toString(); // fallback
            }

            if (selectedBannerUri != null) {
                String mime = getMime(selectedBannerUri);
                String ext  = mime.contains("png") ? "png" : "jpg";
                String url  = SupabaseClient.uploadFile(
                        "avatars", userId + "/banner." + ext,
                        this, selectedBannerUri, mime);
                if (url != null) user.bannerUri = url;
                else user.bannerUri = selectedBannerUri.toString(); // fallback
            }

            db.userDao().update(user);

            prefs.edit()
                    .putString("userName", newName)
                    .putString("userAvatarUri", user.avatarUri != null ? user.avatarUri : "")
                    .putString("userBannerUri", user.bannerUri != null ? user.bannerUri : "")
                    .apply();

            runOnUiThread(() -> {
                Toast.makeText(this, "Профиль обновлён", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    private String getMime(Uri uri) {
        String mime = getContentResolver().getType(uri);
        if (mime == null || mime.isEmpty()) mime = "image/jpeg";
        return mime;
    }
}
