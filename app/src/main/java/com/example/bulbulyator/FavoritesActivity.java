package com.example.bulbulyator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private TextView emptyView;
    private AppDatabase db;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        db = AppDatabase.getInstance(this);
        userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getInt("userId", -1);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.favoritesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        swipeRefresh = findViewById(R.id.swipeRefresh);
        emptyView = findViewById(R.id.emptyView);

        swipeRefresh.setColorSchemeColors(0xFFCB11AB);
        swipeRefresh.setOnRefreshListener(() -> {
            loadFavorites();
            swipeRefresh.setRefreshing(false);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadFavorites() {
        List<Product> products = db.favoriteDao().getUserFavoriteProducts(userId);
        emptyView.setVisibility(products.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(products.isEmpty() ? View.GONE : View.VISIBLE);
        recyclerView.setAdapter(new FavoriteAdapter(products));
    }

    class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {
        private final List<Product> products;

        FavoriteAdapter(List<Product> products) { this.products = products; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(products.get(position), position);
        }

        @Override
        public int getItemCount() { return products.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView productImage;
            TextView productName, productDescription, productPrice;
            Button removeButton, buyButton;

            ViewHolder(View itemView) {
                super(itemView);
                productImage = itemView.findViewById(R.id.productImage);
                productName = itemView.findViewById(R.id.productName);
                productDescription = itemView.findViewById(R.id.productDescription);
                productPrice = itemView.findViewById(R.id.productPrice);
                removeButton = itemView.findViewById(R.id.removeButton);
                buyButton = itemView.findViewById(R.id.buyButton);
            }

            void bind(Product product, int position) {
                ImageUtils.load(FavoritesActivity.this, product.imageUrl, productImage);
                productName.setText(product.name);
                productDescription.setText(product.description);
                NumberFormat fmt = NumberFormat.getInstance(new Locale("ru", "RU"));
                productPrice.setText(fmt.format(product.price) + " ₽");

                removeButton.setOnClickListener(v -> {
                    db.favoriteDao().delete(userId, product.id);
                    products.remove(position);
                    notifyDataSetChanged();
                    if (products.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                    Toast.makeText(FavoritesActivity.this, "Удалено из избранного", Toast.LENGTH_SHORT).show();
                });

                buyButton.setOnClickListener(v -> {
                    Intent intent = new Intent(FavoritesActivity.this, CheckoutActivity.class);
                    intent.putExtra("productId", product.id);
                    intent.putExtra("productName", product.name);
                    intent.putExtra("productPrice", product.price);
                    intent.putExtra("productImageUrl", product.imageUrl);
                    startActivity(intent);
                });
            }
        }
    }
}
