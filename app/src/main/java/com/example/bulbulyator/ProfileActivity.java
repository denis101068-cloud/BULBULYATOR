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
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class ProfileActivity extends AppCompatActivity {

    private TextView userNameText, userEmailText, userBioText, favoritesBadge;
    private ImageView profileAvatar, profileBanner;
    private LinearLayout ordersButton, favoritesButton, productsButton, addProductButton;
    private SharedPreferences prefs;
    private AppDatabase db;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        db = AppDatabase.getInstance(this);
        userId = prefs.getInt("userId", -1);

        userNameText = findViewById(R.id.userNameText);
        userEmailText = findViewById(R.id.userEmailText);
        userBioText = findViewById(R.id.userBioText);
        favoritesBadge = findViewById(R.id.favoritesBadge);
        profileAvatar = findViewById(R.id.profileAvatar);
        profileBanner = findViewById(R.id.profileBanner);
        ordersButton = findViewById(R.id.ordersButton);
        favoritesButton = findViewById(R.id.favoritesButton);
        productsButton = findViewById(R.id.productsButton);
        addProductButton = findViewById(R.id.addProductButton);

        ordersButton.setOnClickListener(v -> startActivity(new Intent(this, OrdersActivity.class)));
        favoritesButton.setOnClickListener(v -> startActivity(new Intent(this, FavoritesActivity.class)));
        productsButton.setOnClickListener(v -> startActivity(new Intent(this, ProductsActivity.class)));
        addProductButton.setOnClickListener(v -> startActivity(new Intent(this, AddProductActivity.class)));
        findViewById(R.id.editProfileButton).setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));
        findViewById(R.id.logoutButton).setOnClickListener(v -> confirmLogout());
    }

    @Override
    protected void onResume() {
        super.onResume();

        User user = db.userDao().getById(userId);

        userNameText.setText(prefs.getString("userName", "Пользователь"));
        userEmailText.setText(prefs.getString("userEmail", ""));

        if (user != null) {
            // Bio
            if (user.bio != null && !user.bio.isEmpty()) {
                userBioText.setText(user.bio);
                userBioText.setVisibility(View.VISIBLE);
            } else {
                userBioText.setVisibility(View.GONE);
            }

            // Аватар
            if (user.avatarUri != null && !user.avatarUri.isEmpty()) {
                Glide.with(this).load(Uri.parse(user.avatarUri))
                        .circleCrop()
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .into(profileAvatar);
            } else {
                profileAvatar.setImageResource(R.drawable.ic_launcher_foreground);
            }

            // Баннер
            if (user.bannerUri != null && !user.bannerUri.isEmpty()) {
                Glide.with(this).load(Uri.parse(user.bannerUri))
                        .centerCrop()
                        .into(profileBanner);
            }
        }

        // Счётчик избранного
        int count = db.favoriteDao().getUserFavoriteProducts(userId).size();
        favoritesBadge.setText(String.valueOf(count));
        favoritesBadge.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
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
