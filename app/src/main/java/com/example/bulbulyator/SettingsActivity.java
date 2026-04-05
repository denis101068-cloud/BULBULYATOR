package com.example.bulbulyator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class SettingsActivity extends AppCompatActivity {

    private LinearLayout themeDarkOption, themeLightOption;
    private TextView themeDarkCheck, themeLightCheck;
    private View rootLayout, themeDivider;
    private CardView themeCard;
    private TextView themeSectionLabel, themeDarkLabel, themeDarkDesc, themeLightLabel, themeLightDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        rootLayout = findViewById(R.id.rootLayout);
        themeCard = findViewById(R.id.themeCard);
        themeDivider = findViewById(R.id.themeDivider);
        themeSectionLabel = findViewById(R.id.themeSectionLabel);
        themeDarkOption = findViewById(R.id.themeDarkOption);
        themeLightOption = findViewById(R.id.themeLightOption);
        themeDarkCheck = findViewById(R.id.themeDarkCheck);
        themeLightCheck = findViewById(R.id.themeLightCheck);
        themeDarkLabel = findViewById(R.id.themeDarkLabel);
        themeDarkDesc = findViewById(R.id.themeDarkDesc);
        themeLightLabel = findViewById(R.id.themeLightLabel);
        themeLightDesc = findViewById(R.id.themeLightDesc);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        applyColors();
        updateChecks();

        themeDarkOption.setOnClickListener(v -> selectTheme(ThemeManager.THEME_BLACK_GOLD));
        themeLightOption.setOnClickListener(v -> selectTheme(ThemeManager.THEME_WHITE_GOLD));
    }

    private void selectTheme(String theme) {
        ThemeManager.setTheme(this, theme);
        applyColors();
        updateChecks();
        // Перезапускаем весь стек активити
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void updateChecks() {
        boolean dark = ThemeManager.isDark(this);
        themeDarkCheck.setVisibility(dark ? View.VISIBLE : View.GONE);
        themeLightCheck.setVisibility(dark ? View.GONE : View.VISIBLE);
    }

    private void applyColors() {
        int bg = ThemeManager.getBg(this);
        int cardBg = ThemeManager.getCardBg(this);
        int textPrimary = ThemeManager.getTextPrimary(this);
        int textSecondary = ThemeManager.getTextSecondary(this);
        int divider = ThemeManager.getDivider(this);
        int gold = ThemeManager.getGold();

        rootLayout.setBackgroundColor(bg);
        themeCard.setCardBackgroundColor(cardBg);
        themeDivider.setBackgroundColor(divider);
        themeSectionLabel.setTextColor(textSecondary);
        themeDarkLabel.setTextColor(textPrimary);
        themeDarkDesc.setTextColor(textSecondary);
        themeLightLabel.setTextColor(textPrimary);
        themeLightDesc.setTextColor(textSecondary);
    }
}
