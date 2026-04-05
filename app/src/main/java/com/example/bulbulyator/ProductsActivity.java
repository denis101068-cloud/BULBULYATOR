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

public class ProductsActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private View emptyView;
    private EditText searchInput;
    private ChipGroup chipGroup, customCatsGroup;
    private SupabaseDb db;
    private SharedPreferences prefs;
    private int userId;
    private String currentCategory = null;
    private boolean customCatsExpanded = false;

    private static final List<String> STANDARD_CATS = Arrays.asList(
            "Электроника", "Автотовары", "Услуги", "Игры", "Одежда",
            "Спорт", "Детям", "Для дома", "Красота", "Здоровье",
            "Продукты", "Мебель", "Цветы", "Товары для взрослых", "Книги",
            "Бытовая техника", "Канцтовары", "Ювелирные изделия", "Для ремонта", "Зоотовары"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        db     = SupabaseDb.getInstance();
        prefs  = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        recyclerView    = findViewById(R.id.productsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        swipeRefresh    = findViewById(R.id.swipeRefresh);
        emptyView       = findViewById(R.id.emptyView);
        searchInput     = findViewById(R.id.searchInput);
        chipGroup       = findViewById(R.id.chipGroup);
        customCatsGroup = findViewById(R.id.customCatsGroup);

        searchInput.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void afterTextChanged(Editable s) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) { loadProducts(); }
        });

        swipeRefresh.setColorSchemeColors(0xFFFFD700);
        swipeRefresh.setOnRefreshListener(() -> { loadProducts(); swipeRefresh.setRefreshing(false); });
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyTheme();
        buildCategoryChips();
        loadProducts();
    }

    private Chip makeChip(String text, boolean checkable, boolean checked) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setCheckable(checkable);
        chip.setChecked(checked);
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
            List<String> customCats = new ArrayList<>();
            for (String cat : allCats) {
                if (cat != null && !cat.isEmpty() && !STANDARD_CATS.contains(cat)) customCats.add(cat);
            }
            runOnUiThread(() -> {
                chipGroup.removeAllViews();
                customCatsGroup.removeAllViews();
                boolean customSelected = currentCategory != null && !STANDARD_CATS.contains(currentCategory);

                Chip chipAll = makeChip("Все", true, currentCategory == null);
                chipGroup.addView(chipAll);

                for (String cat : STANDARD_CATS) {
                    chipGroup.addView(makeChip(cat, true, cat.equals(currentCategory)));
                }

                if (!customCats.isEmpty()) {
                    Chip chipOther = makeChip(customCatsExpanded ? "Другое ▴" : "Другое ▾", false, false);
                    if (customSelected || customCatsExpanded)
                        chipOther.setChipStrokeColor(android.content.res.ColorStateList.valueOf(0xFFFFD700));
                    chipGroup.addView(chipOther);

                    for (String cat : customCats)
                        customCatsGroup.addView(makeChip(cat, true, cat.equals(currentCategory)));

                    customCatsGroup.setVisibility(customCatsExpanded ? View.VISIBLE : View.GONE);

                    chipOther.setOnClickListener(v -> {
                        customCatsExpanded = !customCatsExpanded;
                        customCatsGroup.setVisibility(customCatsExpanded ? View.VISIBLE : View.GONE);
                        chipOther.setText(customCatsExpanded ? "Другое ▴" : "Другое ▾");
                        if (!customCatsExpanded && customSelected) { currentCategory = null; buildCategoryChips(); loadProducts(); }
                    });

                    customCatsGroup.setOnCheckedStateChangeListener((group, ids) -> {
                        if (ids.isEmpty()) return;
                        Chip sel = group.findViewById(ids.get(0));
                        if (sel != null) {
                            currentCategory = sel.getText().toString();
                            for (int i = 0; i < chipGroup.getChildCount(); i++) {
                                View child = chipGroup.getChildAt(i);
                                if (child instanceof Chip) ((Chip) child).setChecked(false);
                            }
                            loadProducts();
                        }
                    });
                }

                chipGroup.setOnCheckedStateChangeListener((group, ids) -> {
                    if (ids.isEmpty()) return;
                    Chip sel = group.findViewById(ids.get(0));
                    if (sel == null) return;
                    String text = sel.getText().toString();
                    if (text.equals("Все")) { currentCategory = null; customCatsGroup.clearCheck(); customCatsExpanded = false; customCatsGroup.setVisibility(View.GONE); loadProducts(); }
                    else if (!text.startsWith("Другое")) { currentCategory = text; customCatsGroup.clearCheck(); loadProducts(); }
                });
            });
        }).start();
    }

    private void loadProducts() {
        String query = searchInput != null ? searchInput.getText().toString().trim() : "";
        new Thread(() -> {
            List<Product> products;
            if (!query.isEmpty()) products = db.productDao().search(query);
            else if (currentCategory != null) products = db.productDao().getByCategory(currentCategory);
            else products = db.productDao().getAll();

            runOnUiThread(() -> {
                emptyView.setVisibility(products.isEmpty() ? View.VISIBLE : View.GONE);
                recyclerView.setVisibility(products.isEmpty() ? View.GONE : View.VISIBLE);
                recyclerView.setAdapter(new ProductAdapter(products));
            });
        }).start();
    }

    class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
        private final List<Product> products;
        ProductAdapter(List<Product> p) { this.products = p; }

        @NonNull public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false));
        }
        public void onBindViewHolder(@NonNull ViewHolder h, int pos) { h.bind(products.get(pos)); }
        public int getItemCount() { return products.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView productImage;
            TextView productName, productDescription, productSeller, productPrice;
            Button favoriteButton, buyButton;

            ViewHolder(View v) {
                super(v);
                productImage       = v.findViewById(R.id.productImage);
                productName        = v.findViewById(R.id.productName);
                productDescription = v.findViewById(R.id.productDescription);
                productSeller      = v.findViewById(R.id.productSeller);
                productPrice       = v.findViewById(R.id.productPrice);
                favoriteButton     = v.findViewById(R.id.favoriteButton);
                buyButton          = v.findViewById(R.id.buyButton);
            }

            void bind(Product product) {
                ((androidx.cardview.widget.CardView) itemView).setCardBackgroundColor(ThemeManager.getCardBg(ProductsActivity.this));
                productName.setTextColor(ThemeManager.getTextPrimary(ProductsActivity.this));
                productDescription.setTextColor(ThemeManager.getTextSecondary(ProductsActivity.this));
                productSeller.setTextColor(ThemeManager.getTextSecondary(ProductsActivity.this));

                ImageUtils.load(ProductsActivity.this, product.imageUrl, productImage);
                productName.setText(product.name);
                productDescription.setText(product.description);
                productSeller.setText("Продавец: " + product.sellerName);
                productPrice.setText(NumberFormat.getInstance(new Locale("ru", "RU")).format(product.price) + " ₽");

                new Thread(() -> {
                    boolean isFav = db.favoriteDao().findFavorite(userId, product.id) != null;
                    runOnUiThread(() -> {
                        com.google.android.material.button.MaterialButton favBtn =
                                (com.google.android.material.button.MaterialButton) favoriteButton;
                        favBtn.setIconResource(isFav ? R.drawable.ic_favorite : R.drawable.ic_empty_heart);
                        favBtn.setIconTint(android.content.res.ColorStateList.valueOf(isFav ? 0xFFFFD700 : 0xFF888888));
                    });
                }).start();

                favoriteButton.setOnClickListener(v -> new Thread(() -> {
                    Favorite fav = db.favoriteDao().findFavorite(userId, product.id);
                    com.google.android.material.button.MaterialButton favBtn =
                            (com.google.android.material.button.MaterialButton) favoriteButton;
                    if (fav != null) {
                        db.favoriteDao().delete(userId, product.id);
                        runOnUiThread(() -> { favBtn.setIconResource(R.drawable.ic_empty_heart); favBtn.setIconTint(android.content.res.ColorStateList.valueOf(0xFF888888)); Toast.makeText(ProductsActivity.this, "Удалено из избранного", Toast.LENGTH_SHORT).show(); });
                    } else {
                        Favorite nf = new Favorite(); nf.userId = userId; nf.productId = product.id;
                        db.favoriteDao().insert(nf);
                        runOnUiThread(() -> { favBtn.setIconResource(R.drawable.ic_favorite); favBtn.setIconTint(android.content.res.ColorStateList.valueOf(0xFFFFD700)); Toast.makeText(ProductsActivity.this, "Добавлено в избранное", Toast.LENGTH_SHORT).show(); });
                    }
                }).start());

                buyButton.setOnClickListener(v -> {
                    Intent i = new Intent(ProductsActivity.this, CheckoutActivity.class);
                    i.putExtra("productId", product.id); i.putExtra("productName", product.name);
                    i.putExtra("productPrice", product.price); i.putExtra("productImageUrl", product.imageUrl);
                    startActivity(i);
                });

                itemView.setOnClickListener(v -> {
                    Intent i = new Intent(ProductsActivity.this, ProductDetailActivity.class);
                    i.putExtra("productId", product.id); startActivity(i);
                });
            }
        }
    }
}
