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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Arrays;
import java.util.List;

public class AddProductActivity extends AppCompatActivity {

    private TextInputEditText nameInput, descriptionInput, priceInput, customCategoryInput;
    private Button addButton, selectImageButton, addCategoryButton;
    private ImageView imagePreview;
    private ChipGroup categoryChipGroup;
    private LinearLayout customCategoryLayout;
    private AppDatabase db;
    private SharedPreferences prefs;
    private Uri selectedImageUri;
    private String selectedCategory = null;

    // Стандартные категории (без "Другое" — оно особое)
    private static final List<String> STANDARD_CATS = Arrays.asList(
            "Смартфоны", "Ноутбуки", "Аудио", "Одежда"
    );

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    imagePreview.setImageURI(selectedImageUri);
                    try {
                        getContentResolver().takePersistableUriPermission(
                                selectedImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception ignored) {}
                }
            }
    );

    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) openImagePicker();
                else Toast.makeText(this, "Разрешение на доступ к фото отклонено", Toast.LENGTH_SHORT).show();
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        db = AppDatabase.getInstance(this);
        prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        nameInput = findViewById(R.id.productNameInput);
        descriptionInput = findViewById(R.id.productDescriptionInput);
        priceInput = findViewById(R.id.productPriceInput);
        customCategoryInput = findViewById(R.id.customCategoryInput);
        addButton = findViewById(R.id.addProductButton);
        selectImageButton = findViewById(R.id.selectImageButton);
        addCategoryButton = findViewById(R.id.addCategoryButton);
        imagePreview = findViewById(R.id.productImagePreview);
        categoryChipGroup = findViewById(R.id.categoryChipGroup);
        customCategoryLayout = findViewById(R.id.customCategoryLayout);

        buildCategoryChips();

        // Кнопка "+" — подтверждает свою категорию
        addCategoryButton.setOnClickListener(v -> {
            String custom = customCategoryInput.getText().toString().trim();
            if (custom.isEmpty()) {
                Toast.makeText(this, "Введите название категории", Toast.LENGTH_SHORT).show();
                return;
            }
            selectedCategory = custom;
            customCategoryInput.setText("");
            customCategoryLayout.setVisibility(View.GONE);
            // Перестраиваем чипы — новая категория появится как выбранная "Другое: custom"
            buildCategoryChips();
            Toast.makeText(this, "Категория: " + custom, Toast.LENGTH_SHORT).show();
        });

        selectImageButton.setOnClickListener(v -> checkPermissionAndPickImage());
        addButton.setOnClickListener(v -> addProduct());
    }

    private void buildCategoryChips() {
        categoryChipGroup.removeAllViews();

        // Стандартные чипы
        for (String cat : STANDARD_CATS) {
            Chip chip = new Chip(this);
            chip.setText(cat);
            chip.setCheckable(true);
            chip.setChecked(cat.equals(selectedCategory));
            chip.setChipBackgroundColorResource(android.R.color.white);
            chip.setCheckedIconVisible(true);
            categoryChipGroup.addView(chip);
        }

        // Чип "Другое"
        Chip chipOther = new Chip(this);
        boolean isCustom = selectedCategory != null && !STANDARD_CATS.contains(selectedCategory);
        chipOther.setText(isCustom ? "Другое: " + selectedCategory : "Другое");
        chipOther.setCheckable(true);
        chipOther.setChecked(isCustom);
        chipOther.setChipBackgroundColorResource(android.R.color.white);
        chipOther.setCheckedIconVisible(true);
        categoryChipGroup.addView(chipOther);

        categoryChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                selectedCategory = null;
                customCategoryLayout.setVisibility(View.GONE);
                return;
            }
            Chip selected = group.findViewById(checkedIds.get(0));
            if (selected == null) return;
            String text = selected.getText().toString();

            if (text.equals("Другое") || text.startsWith("Другое: ")) {
                // Показываем поле ввода
                customCategoryLayout.setVisibility(View.VISIBLE);
                customCategoryInput.requestFocus();
                // selectedCategory остаётся прежним (если уже была кастомная)
                if (!isCustom) selectedCategory = null;
            } else {
                selectedCategory = text;
                customCategoryLayout.setVisibility(View.GONE);
            }
        });
    }

    private void checkPermissionAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        imagePickerLauncher.launch(intent);
    }

    private void addProduct() {
        String name = nameInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String priceStr = priceInput.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Заполните все обязательные поля", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedImageUri == null) {
            Toast.makeText(this, "Выберите фотографию товара", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedCategory == null || selectedCategory.isEmpty()) {
            Toast.makeText(this, "Выберите категорию товара", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Неверный формат цены", Toast.LENGTH_SHORT).show();
            return;
        }

        Product product = new Product();
        product.name = name;
        product.description = description;
        product.price = price;
        product.imageUrl = selectedImageUri.toString();
        product.category = selectedCategory;
        product.sellerId = prefs.getInt("userId", -1);
        product.sellerName = prefs.getString("userName", "Продавец");

        db.productDao().insert(product);
        Toast.makeText(this, "Товар успешно размещён!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
