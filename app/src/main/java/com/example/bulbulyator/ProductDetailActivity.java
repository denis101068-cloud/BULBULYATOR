package com.example.bulbulyator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.Locale;

public class ProductDetailActivity extends BaseActivity {

    private SupabaseDb db;
    private int userId;
    private Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        db     = SupabaseDb.getInstance();
        userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getInt("userId", -1);
        int productId = getIntent().getIntExtra("productId", -1);

        ImageView productImage       = findViewById(R.id.productImage);
        TextView productName         = findViewById(R.id.productName);
        TextView productDescription  = findViewById(R.id.productDescription);
        TextView productSeller       = findViewById(R.id.productSeller);
        TextView productPrice        = findViewById(R.id.productPrice);
        Button favoriteButton        = findViewById(R.id.favoriteButton);
        Button buyButton             = findViewById(R.id.buyButton);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        new Thread(() -> {
            product = db.productDao().getById(productId);
            runOnUiThread(() -> {
                if (product == null) { finish(); return; }

                ImageUtils.load(this, product.imageUrl, productImage);
                productName.setText(product.name);
                productDescription.setText(product.description);
                productSeller.setText("Продавец: " + product.sellerName);
                productPrice.setText(NumberFormat.getInstance(new Locale("ru", "RU")).format(product.price) + " ₽");

                updateFavButton(favoriteButton);

                favoriteButton.setOnClickListener(v -> new Thread(() -> {
                    Favorite fav = db.favoriteDao().findFavorite(userId, product.id);
                    if (fav != null) { db.favoriteDao().delete(userId, product.id); runOnUiThread(() -> { Toast.makeText(this, "Удалено из избранного", Toast.LENGTH_SHORT).show(); updateFavButton(favoriteButton); }); }
                    else { Favorite nf = new Favorite(); nf.userId = userId; nf.productId = product.id; db.favoriteDao().insert(nf); runOnUiThread(() -> { Toast.makeText(this, "Добавлено в избранное", Toast.LENGTH_SHORT).show(); updateFavButton(favoriteButton); }); }
                }).start());

                buyButton.setOnClickListener(v -> {
                    Intent i = new Intent(this, CheckoutActivity.class);
                    i.putExtra("productId", product.id); i.putExtra("productName", product.name);
                    i.putExtra("productPrice", product.price); i.putExtra("productImageUrl", product.imageUrl);
                    startActivity(i);
                });
            });
        }).start();
    }

    private void updateFavButton(Button btn) {
        new Thread(() -> {
            boolean isFav = db.favoriteDao().findFavorite(userId, product.id) != null;
            runOnUiThread(() -> btn.setText(isFav ? "❤️ Удалить из избранного" : "❤️ В избранное"));
        }).start();
    }

    @Override
    protected void onResume() { super.onResume(); applyTheme(); }
}
