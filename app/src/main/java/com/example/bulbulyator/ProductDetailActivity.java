package com.example.bulbulyator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    private AppDatabase db;
    private int userId;
    private Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        db = AppDatabase.getInstance(this);
        userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getInt("userId", -1);

        int productId = getIntent().getIntExtra("productId", -1);
        product = db.productDao().getById(productId);

        if (product == null) { finish(); return; }

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        ImageView productImage = findViewById(R.id.productImage);
        TextView productName = findViewById(R.id.productName);
        TextView productDescription = findViewById(R.id.productDescription);
        TextView productSeller = findViewById(R.id.productSeller);
        TextView productPrice = findViewById(R.id.productPrice);
        Button favoriteButton = findViewById(R.id.favoriteButton);
        Button buyButton = findViewById(R.id.buyButton);

        ImageUtils.load(this, product.imageUrl, productImage);
        productName.setText(product.name);
        productDescription.setText(product.description);
        productSeller.setText("Продавец: " + product.sellerName);

        NumberFormat fmt = NumberFormat.getInstance(new Locale("ru", "RU"));
        productPrice.setText(fmt.format(product.price) + " ₽");

        updateFavButton(favoriteButton);

        favoriteButton.setOnClickListener(v -> {
            Favorite fav = db.favoriteDao().findFavorite(userId, product.id);
            if (fav != null) {
                db.favoriteDao().delete(userId, product.id);
                Toast.makeText(this, "Удалено из избранного", Toast.LENGTH_SHORT).show();
            } else {
                Favorite newFav = new Favorite();
                newFav.userId = userId;
                newFav.productId = product.id;
                db.favoriteDao().insert(newFav);
                Toast.makeText(this, "Добавлено в избранное", Toast.LENGTH_SHORT).show();
            }
            updateFavButton(favoriteButton);
        });

        buyButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, CheckoutActivity.class);
            intent.putExtra("productId", product.id);
            intent.putExtra("productName", product.name);
            intent.putExtra("productPrice", product.price);
            intent.putExtra("productImageUrl", product.imageUrl);
            startActivity(intent);
        });
    }

    private void updateFavButton(Button btn) {
        boolean isFav = db.favoriteDao().findFavorite(userId, product.id) != null;
        btn.setText(isFav ? "❤️ Удалить из избранного" : "❤️ В избранное");
    }
}
