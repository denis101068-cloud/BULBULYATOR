package com.example.bulbulyator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ProductsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private TextView emptyView;
    private EditText searchInput;
    private ChipGroup chipGroup;
    private ChipGroup customCatsGroup;
    private AppDatabase db;
    private SharedPreferences prefs;
    private int userId;
    private String currentCategory = null;
    private boolean customCatsExpanded = false;

    // Стандартные категории — всё остальное попадает в "Другое"
    private static final List<String> STANDARD_CATS = Arrays.asList(
            "Смартфоны", "Ноутбуки", "Аудио", "Одежда"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        db = AppDatabase.getInstance(this);
        prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.productsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        swipeRefresh = findViewById(R.id.swipeRefresh);
        emptyView = findViewById(R.id.emptyView);
        searchInput = findViewById(R.id.searchInput);
        chipGroup = findViewById(R.id.chipGroup);
        customCatsGroup = findViewById(R.id.customCatsGroup);

        buildCategoryChips();

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { loadProducts(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        swipeRefresh.setColorSchemeColors(0xFFCB11AB);
        swipeRefresh.setOnRefreshListener(() -> {
            loadProducts();
            swipeRefresh.setRefreshing(false);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        buildCategoryChips(); // обновляем категории (могли добавиться новые товары)
        loadProducts();
    }

    private void buildCategoryChips() {
        chipGroup.removeAllViews();
        customCatsGroup.removeAllViews();

        List<String> allCats = db.productDao().getDistinctCategories();
        List<String> customCats = new ArrayList<>();
        for (String cat : allCats) {
            if (cat == null || cat.isEmpty()) continue;
            if (!STANDARD_CATS.contains(cat)) customCats.add(cat);
        }

        boolean customSelected = currentCategory != null && !STANDARD_CATS.contains(currentCategory);

        // Чип "Все"
        Chip chipAll = new Chip(this);
        chipAll.setText("Все");
        chipAll.setCheckable(true);
        chipAll.setChecked(currentCategory == null);
        chipAll.setChipBackgroundColorResource(android.R.color.white);
        chipGroup.addView(chipAll);

        // Стандартные категории
        for (String cat : STANDARD_CATS) {
            if (!allCats.contains(cat)) continue; // показываем только если есть товары
            Chip chip = new Chip(this);
            chip.setText(cat);
            chip.setCheckable(true);
            chip.setChecked(cat.equals(currentCategory));
            chip.setChipBackgroundColorResource(android.R.color.white);
            chipGroup.addView(chip);
        }

        // Чип "Другое" — всегда если есть пользовательские категории
        if (!customCats.isEmpty()) {
            Chip chipOther = new Chip(this);
            chipOther.setText(customCatsExpanded ? "Другое ▴" : "Другое ▾");
            chipOther.setCheckable(false); // управляем вручную
            chipOther.setChipBackgroundColor(
                    android.content.res.ColorStateList.valueOf(
                            customSelected || customCatsExpanded ? 0xFFEEBBEE : 0xFFFFFFFF));
            chipGroup.addView(chipOther);

            // Заполняем customCatsGroup
            for (String cat : customCats) {
                Chip chip = new Chip(this);
                chip.setText(cat);
                chip.setCheckable(true);
                chip.setChecked(cat.equals(currentCategory));
                chip.setChipBackgroundColorResource(android.R.color.white);
                customCatsGroup.addView(chip);
            }

            customCatsGroup.setVisibility(customCatsExpanded ? View.VISIBLE : View.GONE);

            chipOther.setOnClickListener(v -> {
                customCatsExpanded = !customCatsExpanded;
                customCatsGroup.setVisibility(customCatsExpanded ? View.VISIBLE : View.GONE);
                chipOther.setText(customCatsExpanded ? "Другое ▴" : "Другое ▾");
                // Если закрываем и была выбрана кастомная — сбрасываем
                if (!customCatsExpanded && customSelected) {
                    currentCategory = null;
                    buildCategoryChips();
                    loadProducts();
                }
            });

            customCatsGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.isEmpty()) return;
                Chip sel = group.findViewById(checkedIds.get(0));
                if (sel != null) {
                    currentCategory = sel.getText().toString();
                    // снимаем выбор со стандартных чипов
                    for (int i = 0; i < chipGroup.getChildCount(); i++) {
                        View child = chipGroup.getChildAt(i);
                        if (child instanceof Chip) ((Chip) child).setChecked(false);
                    }
                    loadProducts();
                }
            });
        }

        // Слушатель для стандартных чипов
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            Chip selected = group.findViewById(checkedIds.get(0));
            if (selected == null) return;
            String text = selected.getText().toString();
            if (text.equals("Все")) {
                currentCategory = null;
                // снимаем выбор в customCatsGroup
                customCatsGroup.clearCheck();
                customCatsExpanded = false;
                customCatsGroup.setVisibility(View.GONE);
                loadProducts();
            } else if (!text.startsWith("Другое")) {
                currentCategory = text;
                customCatsGroup.clearCheck();
                loadProducts();
            }
        });
    }

    private void loadProducts() {
        String query = searchInput != null ? searchInput.getText().toString().trim() : "";
        List<Product> products;

        if (!query.isEmpty()) {
            products = db.productDao().search(query);
        } else if (currentCategory != null) {
            products = db.productDao().getByCategory(currentCategory);
        } else {
            products = db.productDao().getAll();
        }

        emptyView.setVisibility(products.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(products.isEmpty() ? View.GONE : View.VISIBLE);
        recyclerView.setAdapter(new ProductAdapter(products));
    }

    class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
        private final List<Product> products;

        ProductAdapter(List<Product> products) { this.products = products; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(products.get(position));
        }

        @Override
        public int getItemCount() { return products.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView productImage;
            TextView productName, productDescription, productSeller, productPrice;
            Button favoriteButton, buyButton;

            ViewHolder(View itemView) {
                super(itemView);
                productImage = itemView.findViewById(R.id.productImage);
                productName = itemView.findViewById(R.id.productName);
                productDescription = itemView.findViewById(R.id.productDescription);
                productSeller = itemView.findViewById(R.id.productSeller);
                productPrice = itemView.findViewById(R.id.productPrice);
                favoriteButton = itemView.findViewById(R.id.favoriteButton);
                buyButton = itemView.findViewById(R.id.buyButton);
            }

            void bind(Product product) {
                ImageUtils.load(ProductsActivity.this, product.imageUrl, productImage);
                productName.setText(product.name);
                productDescription.setText(product.description);
                productSeller.setText("Продавец: " + product.sellerName);

                NumberFormat fmt = NumberFormat.getInstance(new Locale("ru", "RU"));
                productPrice.setText(fmt.format(product.price) + " ₽");

                boolean isFav = db.favoriteDao().findFavorite(userId, product.id) != null;
                favoriteButton.setText(isFav ? "💔" : "❤️");

                favoriteButton.setOnClickListener(v -> {
                    Favorite fav = db.favoriteDao().findFavorite(userId, product.id);
                    if (fav != null) {
                        db.favoriteDao().delete(userId, product.id);
                        favoriteButton.setText("❤️");
                        Toast.makeText(ProductsActivity.this, "Удалено из избранного", Toast.LENGTH_SHORT).show();
                    } else {
                        Favorite newFav = new Favorite();
                        newFav.userId = userId;
                        newFav.productId = product.id;
                        db.favoriteDao().insert(newFav);
                        favoriteButton.setText("💔");
                        Toast.makeText(ProductsActivity.this, "Добавлено в избранное", Toast.LENGTH_SHORT).show();
                    }
                });

                buyButton.setOnClickListener(v -> {
                    Intent intent = new Intent(ProductsActivity.this, CheckoutActivity.class);
                    intent.putExtra("productId", product.id);
                    intent.putExtra("productName", product.name);
                    intent.putExtra("productPrice", product.price);
                    intent.putExtra("productImageUrl", product.imageUrl);
                    startActivity(intent);
                });

                itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(ProductsActivity.this, ProductDetailActivity.class);
                    intent.putExtra("productId", product.id);
                    startActivity(intent);
                });
            }
        }
    }
}
