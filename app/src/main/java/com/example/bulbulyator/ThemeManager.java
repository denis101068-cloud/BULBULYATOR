package com.example.bulbulyator;

import android.content.Context;

public class ThemeManager {

    public static final String THEME_BLACK_GOLD = "black_gold";
    public static final String THEME_WHITE_GOLD = "white_gold";

    private static final String PREFS = "ThemePrefs";
    private static final String KEY = "theme";

    public static String getTheme(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY, THEME_BLACK_GOLD);
    }

    public static void setTheme(Context ctx, String theme) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY, theme).apply();
    }

    public static boolean isDark(Context ctx) {
        return THEME_BLACK_GOLD.equals(getTheme(ctx));
    }

    public static int getBg(Context ctx)           { return isDark(ctx) ? 0xFF121212 : 0xFFF5F5F0; }
    public static int getCardBg(Context ctx)       { return isDark(ctx) ? 0xFF1E1E1E : 0xFFFFFFFF; }
    public static int getCardBg2(Context ctx)      { return isDark(ctx) ? 0xFF2A2A2A : 0xFFEEEEEE; }
    public static int getTextPrimary(Context ctx)  { return isDark(ctx) ? 0xFFFFFFFF : 0xFF1A1A1A; }
    public static int getTextSecondary(Context ctx){ return isDark(ctx) ? 0xFFAAAAAA : 0xFF666666; }
    public static int getDivider(Context ctx)      { return isDark(ctx) ? 0xFF333333 : 0xFFDDDDDD; }
    public static int getGold()                    { return 0xFFFFD700; }
    public static int getChipBg(Context ctx)       { return isDark(ctx) ? 0xFF1E1E1E : 0xFFFFFFFF; }
    public static int getChipText(Context ctx)     { return isDark(ctx) ? 0xFFFFFFFF : 0xFF1A1A1A; }
    public static int getChipStroke(Context ctx)   { return isDark(ctx) ? 0xFF444444 : 0xFFCCCCCC; }
    public static int getSearchBg(Context ctx)     { return isDark(ctx) ? 0xFF1E1E1E : 0xFFFFFFFF; }
    public static int getSearchText(Context ctx)   { return isDark(ctx) ? 0xFFFFFFFF : 0xFF1A1A1A; }
    public static int getSearchHint(Context ctx)   { return isDark(ctx) ? 0xFF888888 : 0xFF999999; }
    public static int getBottomBar(Context ctx)    { return isDark(ctx) ? 0xFF1E1E1E : 0xFFFFFFFF; }
}
