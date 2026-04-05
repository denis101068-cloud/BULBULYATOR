package com.example.bulbulyator;

import android.content.res.ColorStateList;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

/**
 * Базовый класс — применяет тему ко всем стандартным элементам.
 * Каждый Activity наследует его и вызывает applyTheme() в onResume().
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected void applyTheme() {
        View root = findViewById(R.id.rootLayout);
        if (root == null) return;

        int bg       = ThemeManager.getBg(this);
        int cardBg   = ThemeManager.getCardBg(this);
        int textPri  = ThemeManager.getTextPrimary(this);
        int textSec  = ThemeManager.getTextSecondary(this);
        int divider  = ThemeManager.getDivider(this);
        int gold     = ThemeManager.getGold();
        int chipBg   = ThemeManager.getChipBg(this);
        int chipText = ThemeManager.getChipText(this);
        int chipStroke = ThemeManager.getChipStroke(this);
        int searchBg = ThemeManager.getSearchBg(this);
        int searchText = ThemeManager.getSearchText(this);
        int searchHint = ThemeManager.getSearchHint(this);
        int bottomBar = ThemeManager.getBottomBar(this);

        // Корневой фон
        root.setBackgroundColor(bg);

        // Нижняя панель (product detail)
        View bb = findViewById(R.id.bottomBar);
        if (bb != null) bb.setBackgroundColor(bottomBar);

        // Рекурсивный обход
        applyGroup((ViewGroup) root, bg, cardBg, textPri, textSec, divider, gold,
                chipBg, chipText, chipStroke, searchBg, searchText, searchHint, bottomBar);
    }

    private void applyGroup(ViewGroup vg, int bg, int cardBg, int textPri, int textSec,
            int divider, int gold, int chipBg, int chipText, int chipStroke,
            int searchBg, int searchText, int searchHint, int bottomBar) {

        for (int i = 0; i < vg.getChildCount(); i++) {
            View v = vg.getChildAt(i);
            applyView(v, bg, cardBg, textPri, textSec, divider, gold,
                    chipBg, chipText, chipStroke, searchBg, searchText, searchHint, bottomBar);
        }
    }

    private void applyView(View v, int bg, int cardBg, int textPri, int textSec,
            int divider, int gold, int chipBg, int chipText, int chipStroke,
            int searchBg, int searchText, int searchHint, int bottomBar) {

        if (v == null) return;
        String tag = v.getTag() != null ? v.getTag().toString() : "";

        // Чипы
        if (v instanceof Chip) {
            Chip c = (Chip) v;
            c.setChipBackgroundColor(ColorStateList.valueOf(chipBg));
            c.setTextColor(ColorStateList.valueOf(chipText));
            c.setChipStrokeWidth(1f);
            c.setChipStrokeColor(ColorStateList.valueOf(chipStroke));
            c.setCheckedIconTint(ColorStateList.valueOf(gold));
            c.setRippleColor(ColorStateList.valueOf(0x33FFD700));
            return;
        }

        // ChipGroup фон
        if (v instanceof ChipGroup) {
            v.setBackgroundColor(bg);
            applyGroup((ViewGroup) v, bg, cardBg, textPri, textSec, divider, gold,
                    chipBg, chipText, chipStroke, searchBg, searchText, searchHint, bottomBar);
            return;
        }

        // Карточки
        if (v instanceof CardView) {
            ((CardView) v).setCardBackgroundColor(cardBg);
            applyGroup((ViewGroup) v, bg, cardBg, textPri, textSec, divider, gold,
                    chipBg, chipText, chipStroke, searchBg, searchText, searchHint, bottomBar);
            return;
        }

        // Кнопки — не трогаем цвет фона, только текст/иконку если нужно
        if (v instanceof MaterialButton) return;

        // Поля ввода
        if (v instanceof EditText) {
            EditText et = (EditText) v;
            et.setBackgroundColor(searchBg);
            et.setTextColor(searchText);
            et.setHintTextColor(searchHint);
            return;
        }

        // Текст
        if (v instanceof TextView) {
            TextView tv = (TextView) v;
            if ("header_title".equals(tag) || "gold_text".equals(tag)) return; // золотые — не трогаем
            int color = tv.getCurrentTextColor();
            if (color == 0xFFFFFFFF || color == 0xFF000000
                    || color == 0xFF1A1A1A || color == 0xFF212121) {
                tv.setTextColor(textPri);
            } else if (color == 0xFFAAAAAA || color == 0xFF888888
                    || color == 0xFF666666 || color == 0xFFCCCCCC) {
                tv.setTextColor(textSec);
            }
            return;
        }

        // ViewGroup (LinearLayout и т.д.)
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            // Хедер — не трогаем
            if ("header".equals(tag)) return;
            // Нижняя панель
            if (v.getId() == R.id.bottomBar) {
                v.setBackgroundColor(bottomBar);
                applyGroup(vg, bg, cardBg, textPri, textSec, divider, gold,
                        chipBg, chipText, chipStroke, searchBg, searchText, searchHint, bottomBar);
                return;
            }
            // Разделитель
            if ("divider".equals(tag)) { v.setBackgroundColor(divider); return; }

            // Красим фон если он был тёмным или светлым (не прозрачным)
            try {
                android.graphics.drawable.Drawable d = vg.getBackground();
                if (d instanceof android.graphics.drawable.ColorDrawable) {
                    int c = ((android.graphics.drawable.ColorDrawable) d).getColor();
                    if (c == 0xFF121212 || c == 0xFF1E1E1E || c == 0xFF2A2A2A
                            || c == 0xFFF5F5F0 || c == 0xFFFFFFFF || c == 0xFFEEEEEE
                            || c == 0xFFFFF0FB) {
                        vg.setBackgroundColor(bg);
                    }
                }
            } catch (Exception ignored) {}

            applyGroup(vg, bg, cardBg, textPri, textSec, divider, gold,
                    chipBg, chipText, chipStroke, searchBg, searchText, searchHint, bottomBar);
        }
    }
}
