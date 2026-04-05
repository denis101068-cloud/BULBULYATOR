package com.example.bulbulyator;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class FavoritesActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private View emptyView;
    private SupabaseDb db;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        db     = SupabaseDb.getInstance();
        userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getInt("userId", -1);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        recyclerView = findViewById(R.id.favoritesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        swipeRefresh = findViewById(R.id.swipeRefresh);
        emptyView    = findViewById(R.id.emptyView);

        swipeRefresh.setColorSchemeColors(0xFFFFD700);
        swipeRefresh.setOnRefreshListener(() -> { loadFavorites(); swipeRefresh.setRefreshing(false); });
    }

    @Override
    protected void onResume() { super.onResume(); applyTheme(); loadFavorites(); }

    private void loadFavorites() {
        new Thread(() -> {
            List<Product> products = db.favoriteDao().getUserFavoriteProducts(userId);
            runOnUiThread(() -> {
                emptyView.setVisibility(products.isEmpty() ? View.VISIBLE : View.GONE);
                recyclerView.setVisibility(products.isEmpty() ? View.GONE : View.VISIBLE);
                recyclerView.setAdapter(new FavoriteAdapter(products));
            });
        }).start();
    }

    class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {
        private final List<Product> products;
        FavoriteAdapter(List<Product> p) { this.products = p; }

        @NonNull public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite, parent, false));
        }
        public void onBindViewHolder(@NonNull ViewHolder h, int pos) { h.bind(products.get(pos), pos); }
        public int getItemCount() { return products.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView productImage;
            TextView productName, productDescription, productPrice;
            Button removeButton, buyButton;

            ViewHolder(View v) {
                super(v);
                productImage       = v.findViewById(R.id.productImage);
                productName        = v.findViewById(R.id.productName);
                productDescription = v.findViewById(R.id.productDescription);
                productPrice       = v.findViewById(R.id.productPrice);
                removeButton       = v.findViewById(R.id.removeButton);
                buyButton          = v.findViewById(R.id.buyButton);
            }

            void bind(Product product, int position) {
                ((androidx.cardview.widget.CardView) itemView).setCardBackgroundColor(ThemeManager.getCardBg(FavoritesActivity.this));
                productName.setTextColor(ThemeManager.getTextPrimary(FavoritesActivity.this));
                productDescription.setTextColor(ThemeManager.getTextSecondary(FavoritesActivity.this));

                ImageUtils.load(FavoritesActivity.this, product.imageUrl, productImage);
                productName.setText(product.name);
                productDescription.setText(product.description);
                productPrice.setText(NumberFormat.getInstance(new Locale("ru", "RU")).format(product.price) + " ₽");

                removeButton.setOnClickListener(v -> new Thread(() -> {
                    db.favoriteDao().delete(userId, product.id);
                    runOnUiThread(() -> { products.remove(position); notifyDataSetChanged();
                        if (products.isEmpty()) { emptyView.setVisibility(View.VISIBLE); recyclerView.setVisibility(View.GONE); }
                        Toast.makeText(FavoritesActivity.this, "Удалено из избранного", Toast.LENGTH_SHORT).show();
                    });
                }).start());

                buyButton.setOnClickListener(v -> {
                    Intent i = new Intent(FavoritesActivity.this, CheckoutActivity.class);
                    i.putExtra("productId", product.id); i.putExtra("productName", product.name);
                    i.putExtra("productPrice", product.price); i.putExtra("productImageUrl", product.imageUrl);
                    startActivity(i);
                });
            }
        }
    }
}
