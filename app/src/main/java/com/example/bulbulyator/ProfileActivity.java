package com.example.bulbulyator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

public class ProfileActivity extends BaseActivity {

    private TextView userNameText, userEmailText, userBioText, favoritesBadge;
    private ImageView profileBanner;
    private ShapeableImageView profileAvatar;
    private LinearLayout ordersButton, favoritesButton, productsButton, addProductButton;
    private SharedPreferences prefs;
    private SupabaseDb db;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        prefs  = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        db     = SupabaseDb.getInstance();
        userId = prefs.getInt("userId", -1);

        userNameText   = findViewById(R.id.userNameText);
        userEmailText  = findViewById(R.id.userEmailText);
        userBioText    = findViewById(R.id.userBioText);
        favoritesBadge = findViewById(R.id.favoritesBadge);
        profileAvatar  = findViewById(R.id.profileAvatar);
        profileBanner  = findViewById(R.id.profileBanner);
        ordersButton   = findViewById(R.id.ordersButton);
        favoritesButton= findViewById(R.id.favoritesButton);
        productsButton = findViewById(R.id.productsButton);
        addProductButton = findViewById(R.id.addProductButton);

        ordersButton.setOnClickListener(v -> startActivity(new Intent(this, OrdersActivity.class)));
        favoritesButton.setOnClickListener(v -> startActivity(new Intent(this, FavoritesActivity.class)));
        productsButton.setOnClickListener(v -> startActivity(new Intent(this, ProductsActivity.class)));
        addProductButton.setOnClickListener(v -> startActivity(new Intent(this, AddProductActivity.class)));
        findViewById(R.id.editProfileButton).setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));
        findViewById(R.id.logoutButton).setOnClickListener(v -> confirmLogout());
        findViewById(R.id.settingsButton).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        applyTheme();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyTheme();
        loadUserData();
    }

    private void loadUserData() {
        userNameText.setText(prefs.getString("userName", "Пользователь"));
        userEmailText.setText(prefs.getString("userEmail", ""));

        new Thread(() -> {
            User user = db.userDao().getById(userId);
            int favCount = db.favoriteDao().getUserFavoriteProducts(userId).size();

            runOnUiThread(() -> {
                if (user == null && userId != -1) {
                    prefs.edit().clear().apply();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    return;
                }
                if (user != null) {
                    if (user.bio != null && !user.bio.isEmpty()) {
                        userBioText.setText(user.bio);
                        userBioText.setVisibility(View.VISIBLE);
                    } else {
                        userBioText.setVisibility(View.GONE);
                    }
                    if (user.avatarUri != null && !user.avatarUri.isEmpty()) {
                        Glide.with(this).load(Uri.parse(user.avatarUri))
                                .circleCrop().placeholder(R.drawable.ic_launcher_foreground).into(profileAvatar);
                    } else {
                        profileAvatar.setImageResource(R.drawable.ic_launcher_foreground);
                    }
                    if (user.bannerUri != null && !user.bannerUri.isEmpty()) {
                        Glide.with(this).load(Uri.parse(user.bannerUri)).centerCrop().into(profileBanner);
                    }
                }
                favoritesBadge.setText(String.valueOf(favCount));
                favoritesBadge.setVisibility(favCount > 0 ? View.VISIBLE : View.GONE);
            });
        }).start();
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Выход")
                .setMessage("Вы уверены, что хотите выйти?")
                .setPositiveButton("Выйти", (d, w) -> {
                    prefs.edit().clear().apply();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}
