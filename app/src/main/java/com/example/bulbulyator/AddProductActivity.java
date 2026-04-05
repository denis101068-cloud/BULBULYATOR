package com.example.bulbulyator;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Arrays;
import java.util.List;

public class AddProductActivity extends BaseActivity {

    private TextInputEditText nameInput, descriptionInput, priceInput, customCategoryInput;
    private Button addButton, selectImageButton, addCategoryButton;
    private ImageView imagePreview;
    private ChipGroup categoryChipGroup;
    private LinearLayout customCategoryLayout;
    private SupabaseDb db;
    private SharedPreferences prefs;
    private Uri selectedImageUri;
    private String selectedCategory = null;

    private static final List<String> STANDARD_CATS = Arrays.asList(
            "Электроника", "Автотовары", "Услуги", "Игры", "Одежда",
            "Спорт", "Детям", "Для дома", "Красота", "Здоровье",
            "Продукты", "Мебель", "Цветы", "Товары для взрослых", "Книги",
            "Бытовая техника", "Канцтовары", "Ювелирные изделия", "Для ремонта", "Зоотовары"
    );

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    imagePreview.setImageURI(selectedImageUri);
                    try { getContentResolver().takePersistableUriPermission(selectedImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION); }
                    catch (Exception ignored) {}
                }
            });

    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> { if (granted) openImagePicker(); else Toast.makeText(this, "Разрешение отклонено", Toast.LENGTH_SHORT).show(); });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        db    = SupabaseDb.getInstance();
        prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        nameInput           = findViewById(R.id.productNameInput);
        descriptionInput    = findViewById(R.id.productDescriptionInput);
        priceInput          = findViewById(R.id.productPriceInput);
        customCategoryInput = findViewById(R.id.customCategoryInput);
        addButton           = findViewById(R.id.addProductButton);
        selectImageButton   = findViewById(R.id.selectImageButton);
        addCategoryButton   = findViewById(R.id.addCategoryButton);
        imagePreview        = findViewById(R.id.productImagePreview);
        categoryChipGroup   = findViewById(R.id.categoryChipGroup);
        customCategoryLayout= findViewById(R.id.customCategoryLayout);

        buildCategoryChips();

        addCategoryButton.setOnClickListener(v -> {
            String custom = customCategoryInput.getText().toString().trim();
            if (custom.isEmpty()) { Toast.makeText(this, "Введите название категории", Toast.LENGTH_SHORT).show(); return; }
            selectedCategory = custom;
            customCategoryInput.setText("");
            customCategoryLayout.setVisibility(View.GONE);
            buildCategoryChips();
            Toast.makeText(this, "Категория: " + custom, Toast.LENGTH_SHORT).show();
        });

        selectImageButton.setOnClickListener(v -> checkPermissionAndPickImage());
        addButton.setOnClickListener(v -> addProduct());
    }

    @Override
    protected void onResume() { super.onResume(); applyTheme(); }

    private Chip makeChip(String text, boolean checkable, boolean checked) {
        Chip chip = new Chip(this);
        chip.setText(text); chip.setCheckable(checkable); chip.setChecked(checked);
        chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(ThemeManager.getChipBg(this)));
        chip.setTextColor(android.content.res.ColorStateList.valueOf(ThemeManager.getChipText(this)));
        chip.setChipStrokeWidth(1f);
        chip.setChipStrokeColor(android.content.res.ColorStateList.valueOf(ThemeManager.getChipStroke(this)));
        chip.setCheckedIconTint(android.content.res.ColorStateList.valueOf(ThemeManager.getGold()));
        chip.setRippleColor(android.content.res.ColorStateList.valueOf(0x33FFD700));
        return chip;
    }

    private void buildCategoryChips() {
        new Thread(() -> {
            List<String> allCats = db.productDao().getDistinctCategories();
            runOnUiThread(() -> {
                categoryChipGroup.removeAllViews();
                for (String cat : STANDARD_CATS)
                    categoryChipGroup.addView(makeChip(cat, true, cat.equals(selectedCategory)));

                for (String cat : allCats) {
                    if (cat == null || cat.isEmpty() || STANDARD_CATS.contains(cat)) continue;
                    categoryChipGroup.addView(makeChip(cat, true, cat.equals(selectedCategory)));
                }

                boolean isCustom = selectedCategory != null && !STANDARD_CATS.contains(selectedCategory) && !allCats.contains(selectedCategory);
                Chip chipOther = makeChip(isCustom ? "Другое: " + selectedCategory : "Другое", true, isCustom);
                categoryChipGroup.addView(chipOther);

                categoryChipGroup.setOnCheckedStateChangeListener((group, ids) -> {
                    if (ids.isEmpty()) { selectedCategory = null; customCategoryLayout.setVisibility(View.GONE); return; }
                    Chip sel = group.findViewById(ids.get(0));
                    if (sel == null) return;
                    String text = sel.getText().toString();
                    if (text.equals("Другое") || text.startsWith("Другое: ")) {
                        customCategoryLayout.setVisibility(View.VISIBLE);
                        customCategoryInput.requestFocus();
                        if (!isCustom) selectedCategory = null;
                    } else {
                        selectedCategory = text;
                        customCategoryLayout.setVisibility(View.GONE);
                    }
                });
            });
        }).start();
    }

    private void checkPermissionAndPickImage() {
        String perm = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED) openImagePicker();
        else permissionLauncher.launch(perm);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        imagePickerLauncher.launch(intent);
    }

    private void addProduct() {
        String name        = nameInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String priceStr    = priceInput.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty() || priceStr.isEmpty()) { Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show(); return; }
        if (selectedImageUri == null) { Toast.makeText(this, "Выберите фото товара", Toast.LENGTH_SHORT).show(); return; }
        if (selectedCategory == null || selectedCategory.isEmpty()) { Toast.makeText(this, "Выберите категорию", Toast.LENGTH_SHORT).show(); return; }

        double price;
        try { price = Double.parseDouble(priceStr); }
        catch (NumberFormatException e) { Toast.makeText(this, "Неверный формат цены", Toast.LENGTH_SHORT).show(); return; }

        Product product = new Product();
        product.name        = name;
        product.description = description;
        product.price       = price;
        product.imageUrl    = selectedImageUri.toString();
        product.category    = selectedCategory;
        product.sellerId    = prefs.getInt("userId", -1);
        product.sellerName  = prefs.getString("userName", "Продавец");

        new Thread(() -> {
            db.productDao().insert(product);
            runOnUiThread(() -> { Toast.makeText(this, "Товар успешно размещён!", Toast.LENGTH_SHORT).show(); finish(); });
        }).start();
    }
}
